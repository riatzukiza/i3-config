(ns i3.lint-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [i3.config :as cfg]
            [i3.lint :as lint]))

(defn- ir-of [content]
  (let [f (io/file (System/getProperty "java.io.tmpdir")
                   (str "i3lint-" (System/nanoTime) ".conf"))]
    (spit f content)
    (cfg/load-config (.getPath f))))

(defn- rules-hit [ir]
  (set (map :rule (lint/lint ir))))

(deftest the-bug-that-started-all-this
  ;; empirical matrix, verified via i3-msg 2026-08-09:
  ;;   exec echo a "b,c"        -> PARSE ERROR (comma, mid-command quotes)
  ;;   exec echo a b,c          -> PARSE ERROR (bare comma)
  ;;   exec echo a "bc"         -> ok (no comma)
  ;;   exec "i3-nagbar -B 'Yes, exit' 'true'" -> ok (whole command quoted)
  (testing "comma in mid-command quotes is an error"
    (let [ir (ir-of "set $mod Mod4\nbindsym $mod+space exec rofi -show combi -modi \"combi,window,drun\"\n")]
      (is (contains? (rules-hit ir) :comma-in-exec-command))))
  (testing "bare comma is an error too (i3 command separator)"
    (let [ir (ir-of "set $mod Mod4\nbindsym $mod+space exec rofi -modi combi,window\n")]
      (is (contains? (rules-hit ir) :comma-in-exec-command))))
  (testing "comma protected by whole-command quoting is fine (stock nagbar)"
    (let [ir (ir-of "set $mod Mod4\nbindsym $mod+Shift+e exec \"i3-nagbar -m 'really?' -B 'Yes, exit' 'i3-msg exit'\"\n")]
      (is (not (contains? (rules-hit ir) :comma-in-exec-command)))))
  (testing "mid-command quotes without commas are fine"
    (let [ir (ir-of "set $mod Mod4\nbindsym $mod+a exec curl -s \"http://localhost:3323/api/vol?delta=-5\"\n")]
      (is (not (contains? (rules-hit ir) :comma-in-exec-command)))))
  (testing "non-exec quoted args are fine (mode, workspace)"
    (let [ir (ir-of "set $mod Mod4\nmode \"m\" {\n bindsym Return mode \"default\"\n}\nbindsym $mod+1 workspace number \"1: web\"\n")]
      (is (not (contains? (rules-hit ir) :comma-in-exec-command)))))
  (testing "comma in a non-exec command is a legal i3 command chain (not flagged)"
    (let [ir (ir-of "set $mod Mod4\nbindsym $mod+a focus left, focus right\n")]
      (is (not (contains? (rules-hit ir) :comma-in-exec-command))))))

(deftest tilde-mid-word
  (testing "web:~/... is an error"
    (let [ir (ir-of "set $mod Mod4\nbindsym $mod+slash exec rofi -show web -modi web:~/.local/bin/x\n")]
      (is (contains? (rules-hit ir) :tilde-mid-word))))
  (testing "~ at word start is fine"
    (let [ir (ir-of "exec --no-startup-id ~/bin/foo\n")]
      (is (not (contains? (rules-hit ir) :tilde-mid-word)))))
  (testing "$HOME is fine"
    (let [ir (ir-of "set $mod Mod4\nbindsym $mod+slash exec rofi -modi web:$HOME/.local/bin/x\n")]
      (is (not (contains? (rules-hit ir) :tilde-mid-word))))))

(deftest duplicates
  (let [ir (ir-of "set $mod Mod4\nbindsym $mod+a exec foo\nbindsym $mod+a exec bar\n")]
    (is (contains? (rules-hit ir) :duplicate-binding)))
  (testing "same key in different modes is not a duplicate"
    (let [ir (ir-of "set $mod Mod4\nbindsym $mod+a exec foo\nmode \"m\" {\n bindsym $mod+a exec bar\n}\n")]
      (is (not (contains? (rules-hit ir) :duplicate-binding))))))

(deftest unresolved-var
  (testing "unknown var in key"
    (let [ir (ir-of "bindsym $nope+a exec foo\n")]
      (is (contains? (rules-hit ir) :unresolved-variable))))
  (testing "shell env vars are not unresolved"
    (let [ir (ir-of "set $mod Mod4\nbindsym $mod+slash exec rofi -modi web:$HOME/bin/x\n")]
      (is (not (contains? (rules-hit ir) :unresolved-variable))))))

(deftest empty-include
  (let [ir (ir-of "include ./definitely-not-here/*.conf\n")]
    (is (contains? (rules-hit ir) :include-matched-nothing))))

(deftest missing-exec-path
  (let [ir (ir-of "exec --no-startup-id /definitely/not/a/real/binary --flag\n")]
    (is (contains? (rules-hit ir) :exec-path-missing)))
  (testing "existing path (/bin/sh) is clean"
    (let [ir (ir-of "exec --no-startup-id /bin/sh -c true\n")]
      (is (not (contains? (rules-hit ir) :exec-path-missing))))))
