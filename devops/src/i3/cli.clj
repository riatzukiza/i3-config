(ns i3.cli
  (:require [babashka.process :refer [shell]]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [i3.config :as cfg]
            [i3.lint :as lint]))

(def default-config
  (str (System/getProperty "user.home") "/.config/i3/config"))

(def devops-root
  (.getParent (io/file (.getParent (io/file (.getParent (io/file *file*)))))))

(def sandbox-dir
  (str devops-root "/../sandbox"))

(defn- usage []
  (println "i3 devops kit — bb tasks in" devops-root)
  (println)
  (println "  bb lint [config]     static lint of config (default ~/.config/i3/config)")
  (println "  bb parse [config]    dump config IR as EDN")
  (println "  bb check [config]    lint + i3 -C + reload + error-log scan")
  (println "  bb test              run clojure.test suite")
  (println "  bb sandbox <cmd>     docker sandbox: build|up|down|shell|smoke|snapshot|shot|key|regression"))

(defn- print-findings [findings]
  (if (empty? findings)
    (println "no findings")
    (doseq [{:keys [severity rule file line message snippet]} findings]
      (println (format "%-5s %-24s %s:%s" (name severity) (name rule)
                       (str/replace file (str (System/getProperty "user.home")) "~") line))
      (println (str "       " message))
      (when snippet (println (str "       > " (str/trim snippet)))))))

(defn- cli-args [args]
  ;; bb exec passes a task-opts map positionally and binds
  ;; *command-line-args*; prefer real strings, fall back to the binding.
  (let [kept (remove map? args)]
    (if (seq kept) kept *command-line-args*)))

(defn lint-main [& args]
  (let [args (cli-args args)
        path (or (first args) default-config)
        ir (cfg/load-config path)
        findings (lint/lint ir)
        errors (filter #(= :error (:severity %)) findings)]
    (print-findings findings)
    (println)
    (println (count findings) "finding(s)," (count errors) "error(s)"
             "across" (count (:files ir)) "file(s)")
    (System/exit (if (seq errors) 1 0))))

(defn parse-main [& args]
  (let [path (or (first (cli-args args)) default-config)]
    (pp/pprint (cfg/load-config path))))

(defn- sh [& cmd]
  (let [{:keys [exit out err]} (apply shell {:out :string :err :string :continue true} cmd)]
    {:exit exit :out (str out) :err (str err)}))

(defn check-main [& args]
  (let [args (cli-args args)
        path (or (first args) default-config)
        ir (cfg/load-config path)
        findings (lint/lint ir)
        errors (filter #(= :error (:severity %)) findings)]
    (println "== static lint ==")
    (print-findings findings)
    (println)
    (println "== i3 -C (config syntax check) ==")
    (let [{:keys [exit out err]} (sh "i3" "-C" "-c" path)]
      (if (zero? exit)
        (println "i3 -C: OK (note: passes even for bugs like double-quoted binding commands; trust lint above)")
        (do (println "i3 -C FAILED") (println out err))))
    (println)
    (println "== live reload + error-log scan ==")
    (let [{:keys [exit out]} (sh "i3-msg" "-t" "command" "reload")]
      (println "i3-msg reload:" (str/trim out) (when-not (zero? exit) "(IPC failed — is i3 running?)"))
      (Thread/sleep 500)
      (let [{:keys [out]} (sh "i3-dump-log")
            error-lines (->> (str/split-lines out)
                             (filter #(str/includes? % "ERROR"))
                             (take-last 10))]
        (if (seq error-lines)
          (do (println "recent ERROR lines in i3 log:")
              (run! #(println "  " %) error-lines))
          (println "no ERROR lines in i3 log"))))
    (System/exit (if (seq errors) 1 0))))

(defn- compose [& args]
  (apply shell {:dir sandbox-dir :continue true}
         "docker" "compose" args))

(defn sandbox-main [& args]
  (let [args (cli-args args)]
    (case (first args)
    "build" (compose "build")
    "up" (compose "up" "-d" "--wait")
    "down" (compose "down")
    "shell" (compose "exec" "sandbox" "bash")
    "smoke" (compose "exec" "sandbox" "smoke")
    "snapshot" (compose "exec" "sandbox" "snapshot")
    "shot" (compose "exec" "sandbox" "screenshot")
    "key" (apply compose "exec" "sandbox" "send-key" (rest args))
    "probe" (apply compose "exec" "sandbox" "probe-bindings" (rest args))
    "regression"
    (let [fixture (str sandbox-dir "/fixtures/broken-quotes")]
      (println "booting sandbox with deliberately broken config:" fixture)
      (compose "down")
      (shell {:dir sandbox-dir :env (merge (into {} (System/getenv))
                                           {"I3_CONFIG_DIR" fixture})}
             "docker" "compose" "up" "-d" "--wait")
      (let [{:keys [exit out]} (shell {:dir sandbox-dir :out :string :continue true}
                                      "docker" "compose" "exec" "sandbox"
                                      "probe-bindings" "Super+space")]
        (compose "down")
        (if (and (= 1 exit) (str/includes? out "Expected one of these tokens"))
          (println "regression harness OK: comma bug reproduced via synthetic keypress and detected")
          (do (println "REGRESSION HARNESS UNEXPECTED RESULT (exit" exit ")")
              (println out)
              (System/exit 1)))))
    (do (println "unknown sandbox command:" (first args)) (System/exit 2)))))

(defn -main [& args]
  (case (first args)
    "lint" (lint-main (rest args))
    "parse" (parse-main (rest args))
    "check" (check-main (rest args))
    "sandbox" (sandbox-main (rest args))
    (usage)))
