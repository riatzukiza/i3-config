;;; Lint rules for the i3 config IR.
;;;
;;; Regex rules are pure data (pattern + message), so new rules of that
;;; class are added by editing the table, not the engine. Structural rules
;;; are pure functions IR -> findings.
;;;
;;; Finding shape:
;;;   {:rule :keyword :severity :error|:warn|:info
;;;    :file "..." :line n :message "..." :snippet "..."}

(ns i3.lint
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def regex-rules
  "Data-driven rules. Each: applies to directives whose :type is in
   :applies-to, matching :pattern against the directive's command text
   (:resolved, falling back to :raw). Optional :unless-pattern suppresses
   the finding when it matches (still data, still declarative)."
  [{:id :comma-in-exec-command
    :severity :error
    :applies-to #{:bindsym :bindcode :for-window}
    :when-pattern #"^exec(_always)?\s"
    :pattern #","
    :unless-pattern #"^exec(_always)?\s+(--no-startup-id\s+)?[\"']"
    :message "commas are command separators in i3's binding-command parser; quotes only protect them when they wrap the ENTIRE command (verified 2026-08-09 via i3-msg: exec echo a \"b,c\" fails, exec \"i3-nagbar -B 'Yes, exit'\" works). Quote the whole exec command or remove the commas"}

   {:id :tilde-mid-word
    :severity :error
    :applies-to #{:bindsym :bindcode :exec}
    :pattern #"(?<=\S)~"
    :message "~ not at a word start is not expanded by sh (seen 2026-08-09: web:~/... passed literally to rofi); use $HOME instead"}

   {:id :exec-with-sudo
    :severity :warn
    :applies-to #{:exec}
    :pattern #"(^|\s)sudo\s"
    :message "sudo in an i3 exec will block on a password prompt with no tty"}])

(defn- applicable-findings
  [{:keys [id severity pattern when-pattern unless-pattern message]} d]
  (let [text (or (:resolved d) (:command d) (:raw d))]
    (when (and text
               (re-find pattern text)
               (or (nil? when-pattern) (re-find when-pattern text))
               (not (and unless-pattern (re-find unless-pattern text))))
      {:rule id
       :severity severity
       :file (:file d)
       :line (:line d)
       :message message
       :snippet (:raw d)})))

(defn regex-findings
  [ir]
  (for [d (:directives ir)
        rule regex-rules
        :when (contains? (:applies-to rule) (:type d))
        :let [finding (applicable-findings rule d)]
        :when finding]
    finding))

(defn duplicate-bindings
  "Same key bound twice in the same scope (top level or one mode)."
  [ir]
  (->> (:directives ir)
       (filter #(#{:bindsym :bindcode} (:type %)))
       (filter :command)
       (group-by (juxt :key :scope))
       (filter #(< 1 (count (val %))))
       (mapcat (fn [[[key _] ds]]
                 (for [d (rest ds)]
                   {:rule :duplicate-binding
                    :severity :warn
                    :file (:file d)
                    :line (:line d)
                    :message (str "key " key " is bound more than once in this scope")
                    :snippet (:raw d)})))))

(def shell-env-vars
  "Env vars sh expands; i3 passes unknown $vars through to sh, so these are
   never unresolved-variable findings."
  #{"HOME" "PATH" "USER" "LOGNAME" "SHELL" "DISPLAY" "WAYLAND_DISPLAY"
    "XAUTHORITY" "TERM" "LANG" "SSH_AUTH_SOCK" "DBUS_SESSION_BUS_ADDRESS"
    "XDG_RUNTIME_DIR" "XDG_CONFIG_HOME" "XDG_DATA_HOME" "XDG_CACHE_HOME"
    "XDG_SESSION_TYPE" "XDG_SESSION_ID" "XDG_SEAT" "XDG_VTNR"})

(defn unresolved-variables
  "Commands or binding keys still containing $vars after substitution,
   excluding known shell env vars (which sh, not i3, expands)."
  [ir]
  (for [d (:directives ir)
        :let [text (str (:resolved d) " " (:resolved-key d))
              leftover (re-find #"\$[\w-]+" text)
              var-name (some-> leftover (str/replace #"^\$" ""))]
        :when (and leftover (not (contains? shell-env-vars var-name)))]
    {:rule :unresolved-variable
     :severity :error
     :file (:file d)
     :line (:line d)
     :message (str "variable " leftover " is never `set` and is not a known env var; i3 passes it through to sh verbatim")
     :snippet (:raw d)}))

(defn empty-includes
  [ir]
  (for [d (:directives ir)
        :when (and (= :include (:type d)) (zero? (:matched d 0)))]
    {:rule :include-matched-nothing
     :severity :warn
     :file (:file d)
     :line (:line d)
     :message (str "include pattern matched no files: " (:pattern d))
     :snippet (:raw d)}))

(defn- expand-home [token]
  (-> token
      (str/replace-first #"^\$HOME" (System/getProperty "user.home"))
      (str/replace-first #"^~" (System/getProperty "user.home"))))

(defn missing-exec-paths
  "Exec commands referencing filesystem paths that do not exist."
  [ir]
  (for [d (:directives ir)
        :when (or (= :exec (:type d))
                  (and (#{:bindsym :bindcode} (:type d))
                       (:resolved d)
                       (str/starts-with? (:resolved d) "exec")))
        :let [cmd (some-> (:resolved d)
                          (str/replace #"^exec(_always)?\s+" "")
                          (str/replace #"^--no-startup-id\s+" ""))]
        :when cmd
        token (str/split cmd #"\s+")
        :let [token (str/replace token #"^[\"']|[\"']$" "")]
        :when (and (re-find #"^(/|~|\$HOME|\./)" token)
                   (not (str/includes? token "://"))
                   (not (.exists (io/file (expand-home token)))))]
    {:rule :exec-path-missing
     :severity :warn
     :file (:file d)
     :line (:line d)
     :message (str "exec references a path that does not exist: " token)
     :snippet (:raw d)}))

(defn lint
  "All findings for a loaded IR, sorted by severity then file/line."
  [ir]
  (let [sev-rank {:error 0 :warn 1 :info 2}]
    (->> (concat (regex-findings ir)
                 (duplicate-bindings ir)
                 (unresolved-variables ir)
                 (empty-includes ir)
                 (missing-exec-paths ir))
         (sort-by (juxt (comp sev-rank :severity) :file :line))
         vec)))
