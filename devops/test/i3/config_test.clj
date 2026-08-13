(ns i3.config-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [i3.config :as cfg]))

(def sample
  "# comment
set $mod Mod4
set $term xterm
bindsym $mod+Return exec $term
bindsym $mod+space exec rofi -show combi
exec --no-startup-id firefox
for_window [class=\"plasmashell\"] floating enable
mode \"resize\" {
    bindsym h resize shrink width 10 px
    bindsym Escape mode \"default\"
}
bar {
    status_command i3blocks
}
")

(defn- write-tmp [name content]
  (let [f (io/file (System/getProperty "java.io.tmpdir") name)]
    (spit f content)
    (.getPath f)))

(deftest parses-sets-and-vars
  (let [path (write-tmp "i3test-basic.conf" sample)
        ir (cfg/load-config path)]
    (is (= {"$mod" "Mod4" "$term" "xterm"} (:vars ir)))
    (is (= :i3-config-ir (:type ir)))))

(deftest bindsym-resolution
  (let [path (write-tmp "i3test-bindsym.conf" sample)
        ir (cfg/load-config path)
        ret (first (filter #(= "$mod+Return" (:key %)) (:directives ir)))]
    (is (= "exec $term" (:command ret)))
    (is (= "exec xterm" (:resolved ret)))))

(deftest mode-blocks-flatten-with-scope
  (let [path (write-tmp "i3test-mode.conf" sample)
        ir (cfg/load-config path)
        in-mode (filter #(= "resize" (:scope %)) (:directives ir))]
    (is (= 2 (count (filter #(= :bindsym (:type %)) in-mode))))
    (is (some #(= "mode \"default\"" (:command %)) in-mode))))

(deftest for-window-parsed
  (let [path (write-tmp "i3test-fw.conf" sample)
        ir (cfg/load-config path)
        fw (first (filter #(= :for-window (:type %)) (:directives ir)))]
    (is (= "[class=\"plasmashell\"]" (:criteria fw)))
    (is (= "floating enable" (:command fw)))))

(deftest include-expansion
  (let [dir (io/file (System/getProperty "java.io.tmpdir") "i3test-inc")]
    (.mkdirs (io/file dir "conf.d"))
    (spit (io/file dir "conf.d" "extra.conf") "bindsym $mod+x exec xterm\n")
    (spit (io/file dir "config") "set $mod Mod4\ninclude \"./conf.d/*.conf\"\n")
    (let [ir (cfg/load-config (.getPath (io/file dir "config")))]
      (is (= 2 (count (:files ir))))
      (is (some #(= "$mod+x" (:key %)) (:directives ir)))
      (is (some #(= 1 (:matched %)) (:directives ir))))))

(deftest include-nothing-matched
  (let [path (write-tmp "i3test-emptyinc.conf" "include ./nope/*.conf\n")]
    (is (= 0 (:matched (first (:directives (cfg/load-config path))))))))
