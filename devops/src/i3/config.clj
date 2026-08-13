;;; i3 config -> EDN IR parser.
;;;
;;; The IR is pure data: a map of :vars, :files, and a flat :directives
;;; vector in load order. Every directive carries :file, :line, and :raw.
;;; All downstream tooling (lint, checks, review agents) operates on this
;;; IR, so the hard-to-test environment (a live i3) becomes an easy-to-test
;;; one (plain clojure data).

(ns i3.config
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.nio.file FileSystems PathMatcher]))

(defn- strip-comment
  "i3 treats # as comment start outside quoted strings; the configs we
   lint never quote #, so strip from the first # at a word boundary."
  [line]
  (str/replace line #"(^|\s)#.*$" "$1"))

(defn- block-opener?
  "Lines ending in { open a block (mode ..., bar { ... })."
  [s]
  (str/ends-with? (str/trim s) "{"))

(defn- parse-binding-args
  "Split a bindsym/bindcode rest into [key command]. Flags like
   --release/--whole-window precede the key."
  [rest-str]
  (let [tokens (str/split (str/trim rest-str) #"\s+")
        [flags remainder] (split-with #(str/starts-with? % "--") tokens)]
    {:flags (vec flags)
     :key (first remainder)
     :command (when (seq (rest remainder))
                (str/join " " (rest remainder)))}))

(defn- line->directive
  "Classify a single non-block line of i3 config."
  [text file line-no]
  (let [base {:file file :line line-no :raw text}
        trimmed (str/trim text)
        [head & more] (str/split trimmed #"\s+")
        rest-str (str/join " " more)]
    (cond
      (str/blank? trimmed)
      (assoc base :type :blank)

      (= head "set")
      (let [[_ name value] (re-matches #"set\s+(\$\S+)\s+(.*)" trimmed)]
        (assoc base :type :set :name name :value value))

      (#{"bindsym" "bindcode"} head)
      (merge base {:type (keyword head)}
             (parse-binding-args rest-str))

      (#{"exec" "exec_always"} head)
      (assoc base
             :type :exec
             :always? (= head "exec_always")
             :command (str/replace rest-str #"^--no-startup-id\s+" ""))

      (= head "include")
      (assoc base :type :include
             :pattern (str/replace rest-str #"^[\"']|[\"']$" ""))

      (= head "for_window")
      (let [[_ criteria cmd] (re-matches #"for_window\s+(\[.*\])\s+(.*)" trimmed)]
        (assoc base :type :for-window :criteria criteria :command cmd))

      (= head "mode")
      ;; single-line `mode "name"` without a block
      (assoc base :type :mode-switch :name (str/replace rest-str #"\"" ""))

      :else
      (assoc base :type :directive :name head :args rest-str))))

(defn- glob-matcher ^PathMatcher [pattern]
  (.getPathMatcher (FileSystems/getDefault) (str "glob:" pattern)))

(defn- search-root
  "Deepest wildcard-free directory of a glob; file-seq starts here, not at
   the config dir (which may contain .git or worse)."
  ^java.io.File [base pattern]
  (let [p (str (io/file base pattern))
        idx (.indexOf ^String p "*")
        idx (if (neg? idx) (count p) idx)
        prefix (subs p 0 idx)
        slash (.lastIndexOf ^String prefix "/")]
    (io/file (if (neg? slash) base (subs prefix 0 slash)))))

(defn expand-include
  "Resolve an include pattern relative to the including file's directory.
   Supports * and ** like i3 itself."
  [from-file pattern]
  (let [base (.getParent (io/file from-file))
        abs-pattern (-> (io/file base pattern) .toPath .normalize .toString)
        matcher (glob-matcher abs-pattern)]
    (->> (file-seq (search-root base pattern))
         (filter #(.isFile ^java.io.File %))
         (filter #(.matches matcher (-> ^java.io.File % .toPath .normalize)))
         (map #(.getPath ^java.io.File %))
         sort)))

(declare parse-lines)

(defn- parse-block
  "Parse a block starting at idx (opener line). Returns [directive next-idx]."
  [lines idx file]
  (let [opener (nth lines idx)
        trimmed (str/trim opener)
        head (first (str/split trimmed #"\s+"))]
    (loop [i (inc idx), body [], depth 1]
      (if (>= i (count lines))
        [{:type (keyword head) :file file :line (inc idx) :raw trimmed :body body}
         i]
        (let [line (nth lines i)
              t (str/trim (strip-comment line))]
          (cond
            (block-opener? t)
            (let [[sub next-i] (parse-block lines i file)]
              (recur next-i (conj body sub) depth))

            (= t "}")
            (if (zero? (dec depth))
              [{:type (keyword head)
                :file file :line (inc idx) :raw trimmed
                :name (when (= head "mode")
                        (-> trimmed
                            (str/replace #"^mode\s+" "")
                            (str/replace #"\s*\{$" "")
                            (str/replace #"\"" "")))
                :body body}
               (inc i)]
              (recur (inc i) body (dec depth)))

            :else
            (recur (inc i)
                   (conj body (line->directive line file (inc i)))
                   depth)))))))

(defn- parse-lines
  "Parse raw lines of one file into directives (blocks nested in :body)."
  [lines file]
  (loop [i 0, out []]
    (if (>= i (count lines))
      out
      (let [line (nth lines i)
            t (str/trim (strip-comment line))]
        (if (block-opener? t)
          (let [[dir next-i] (parse-block lines i file)]
            (recur next-i (conj out dir)))
          (recur (inc i) (conj out (line->directive line file (inc i)))))))))

(defn- flatten-directives
  "Depth-first flatten; block bodies inline after their opener, tagged with
   the block's scope so rules can distinguish e.g. bindings inside modes."
  [dirs scope]
  (mapcat (fn [d]
            (let [d (cond-> d scope (assoc :scope scope))]
              (if (:body d)
                (concat [d]
                        (flatten-directives (:body d)
                                            (or (:name d) (name (:type d)))))
                [d])))
          dirs))

(defn parse-file
  "Parse an i3 config file (following includes) into the IR map."
  ([path] (parse-file path #{} 0))
  ([path seen depth]
   (let [f (.getAbsoluteFile (io/file path))]
     (if (or (> depth 10) (contains? seen (.getPath f)))
       {:files [] :directives [] :includes []}
       (let [seen' (conj seen (.getPath f))
             lines (str/split-lines (slurp f))
             dirs (parse-lines lines (.getPath f))]
         (loop [ds dirs
                acc {:files [(.getPath f)] :directives [] :includes []}]
           (if (empty? ds)
             acc
             (let [d (first ds)]
               (if (= :include (:type d))
                 (let [matched (expand-include (.getPath f) (:pattern d))
                       subs (map #(parse-file % seen' (inc depth)) matched)
                       d' (assoc d :matched (count matched))]
                   (recur (rest ds)
                          (-> acc
                              (update :files into (mapcat :files subs))
                              (update :directives conj d')
                              (update :directives into (mapcat :directives subs)))))
                 (recur (rest ds)
                        (update acc :directives conj d)))))))))))

(defn- resolve-vars
  "Substitute $vars in a string; longest names first to avoid prefix clashes.
   Literal replacement via String.replace (regex-free, SCI-safe)."
  [vars s]
  (reduce (fn [^String acc [k v]] (.replace acc k v))
          s
          (sort-by (comp count key) #(compare %2 %1) vars)))

(defn load-config
  "Full IR for a config path: flattened directives in load order, vars,
   and :resolved command text on every directive that has a :command."
  [path]
  (let [{:keys [files directives]} (parse-file path)
        flat (vec (flatten-directives directives nil))
        vars (into {}
                   (comp (filter #(= :set (:type %)))
                         (map (juxt :name :value)))
                   flat)
        resolved (mapv (fn [d]
                         (cond-> d
                           (:command d) (assoc :resolved (resolve-vars vars (:command d)))
                           (:key d) (assoc :resolved-key (resolve-vars vars (:key d)))))
                       flat)]
    {:type :i3-config-ir
     :root path
     :files (vec (distinct files))
     :vars vars
     :directives resolved}))
