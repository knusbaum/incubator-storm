;; Licensed to the Apache Software Foundation (ASF) under one
;; or more contributor license agreements.  See the NOTICE file
;; distributed with this work for additional information
;; regarding copyright ownership.  The ASF licenses this file
;; to you under the Apache License, Version 2.0 (the
;; "License"); you may not use this file except in compliance
;; with the License.  You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns org.apache.storm.util
  (:import [java.net InetAddress])
  (:import [java.util Map Map$Entry List ArrayList Collection Iterator HashMap])
  (:import [java.io FileReader FileNotFoundException])
  (:import [java.nio.file Paths])
  (:import [org.apache.storm Config])
  (:import [org.apache.storm.utils Time Container ClojureTimerTask Utils
            MutableObject])
  (:import [org.apache.storm.security.auth NimbusPrincipal])
  (:import [javax.security.auth Subject])
  (:import [java.util UUID Random ArrayList List Collections])
  (:import [java.util.zip ZipFile])
  (:import [java.util.concurrent.locks ReentrantReadWriteLock])
  (:import [java.util.concurrent Semaphore])
  (:import [java.nio.file Files Paths])
  (:import [java.nio.file.attribute FileAttribute])
  (:import [java.io File FileOutputStream RandomAccessFile StringWriter
            PrintWriter BufferedReader InputStreamReader IOException])
  (:import [java.lang.management ManagementFactory])
  (:import [org.apache.commons.exec DefaultExecutor CommandLine])
  (:import [org.apache.commons.io FileUtils])
  (:import [org.apache.commons.exec ExecuteException])
  (:import [org.json.simple JSONValue])
  (:import [org.yaml.snakeyaml Yaml]
           [org.yaml.snakeyaml.constructor SafeConstructor])
  (:require [clojure [string :as str]])
  (:import [clojure.lang RT])
  (:require [clojure [set :as set]])
  (:require [clojure.java.io :as io])
  (:use [clojure walk])
  (:require [ring.util.codec :as codec])
  (:use [org.apache.storm log]))

;(defn wrap-in-runtime
;  "Wraps an exception in a RuntimeException if needed"
;  [^Exception e]
;  (if (instance? RuntimeException e)
;    e
;    (RuntimeException. e)))

(def on-windows?
  (= "Windows_NT" (System/getenv "OS")))

;(def file-path-separator
;  (System/getProperty "file.separator"))

;(def class-path-separator
;  (System/getProperty "path.separator"))

(defn is-absolute-path? [path]
  (.isAbsolute (Paths/get path (into-array String []))))

(defmacro defalias
  "Defines an alias for a var: a new var with the same root binding (if
  any) and similar metadata. The metadata of the alias is its initial
  metadata (as provided by def) merged into the metadata of the original."
  ([name orig]
   `(do
      (alter-meta!
        (if (.hasRoot (var ~orig))
          (def ~name (.getRawRoot (var ~orig)))
          (def ~name))
        ;; When copying metadata, disregard {:macro false}.
        ;; Workaround for http://www.assembla.com/spaces/clojure/tickets/273
        #(conj (dissoc % :macro)
               (apply dissoc (meta (var ~orig)) (remove #{:macro} (keys %)))))
      (var ~name)))
  ([name orig doc]
   (list `defalias (with-meta name (assoc (meta name) :doc doc)) orig)))

;; name-with-attributes by Konrad Hinsen:
(defn name-with-attributes
  "To be used in macro definitions.
  Handles optional docstrings and attribute maps for a name to be defined
  in a list of macro arguments. If the first macro argument is a string,
  it is added as a docstring to name and removed from the macro argument
  list. If afterwards the first macro argument is a map, its entries are
  added to the name's metadata map and the map is removed from the
  macro argument list. The return value is a vector containing the name
  with its extended metadata map and the list of unprocessed macro
  arguments."
  [name macro-args]
  (let [[docstring macro-args] (if (string? (first macro-args))
                                 [(first macro-args) (next macro-args)]
                                 [nil macro-args])
        [attr macro-args] (if (map? (first macro-args))
                            [(first macro-args) (next macro-args)]
                            [{} macro-args])
        attr (if docstring
               (assoc attr :doc docstring)
               attr)
        attr (if (meta name)
               (conj (meta name) attr)
               attr)]
    [(with-meta name attr) macro-args]))

(defmacro defnk
  "Define a function accepting keyword arguments. Symbols up to the first
  keyword in the parameter list are taken as positional arguments.  Then
  an alternating sequence of keywords and defaults values is expected. The
  values of the keyword arguments are available in the function body by
  virtue of the symbol corresponding to the keyword (cf. :keys destructuring).
  defnk accepts an optional docstring as well as an optional metadata map."
  [fn-name & fn-tail]
  (let [[fn-name [args & body]] (name-with-attributes fn-name fn-tail)
        [pos kw-vals] (split-with symbol? args)
        syms (map #(-> % name symbol) (take-nth 2 kw-vals))
        values (take-nth 2 (rest kw-vals))
        sym-vals (apply hash-map (interleave syms values))
        de-map {:keys (vec syms) :or sym-vals}]
    `(defn ~fn-name
       [~@pos & options#]
       (let [~de-map (apply hash-map options#)]
         ~@body))))

(defn find-first
  "Returns the first item of coll for which (pred item) returns logical true.
  Consumes sequences up to the first match, will consume the entire sequence
  and return nil if no match is found."
  [pred coll]
  (first (filter pred coll)))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(defn positions
  "Returns a lazy sequence containing the positions at which pred
  is true for items in coll."
  [pred coll]
  (for [[idx elt] (indexed coll) :when (pred elt)] idx))

(defn exception-cause?
  [klass ^Throwable t]
  (->> (iterate #(.getCause ^Throwable %) t)
       (take-while identity)
       (some (partial instance? klass))
       boolean))

(defmacro thrown-cause?
  [klass & body]
  `(try
     ~@body
     false
     (catch Throwable t#
       (Utils/exceptionCauseIsInstanceOf ~klass t#))))

(defmacro thrown-cause-with-msg?
  [klass re & body]
  `(try
     ~@body
     false
     (catch Throwable t#
       (and (re-matches ~re (.getMessage t#))
            (exception-cause? ~klass t#)))))

(defmacro forcat
  [[args aseq] & body]
  `(mapcat (fn [~args]
             ~@body)
           ~aseq))

(defmacro try-cause
  [& body]
  (let [checker (fn [form]
                  (or (not (sequential? form))
                      (not= 'catch (first form))))
        [code guards] (split-with checker body)
        error-local (gensym "t")
        guards (forcat [[_ klass local & guard-body] guards]
                       `((Utils/exceptionCauseIsInstanceOf ~klass ~error-local)
                         (let [~local ~error-local]
                           ~@guard-body
                           )))]
    `(try ~@code
       (catch Throwable ~error-local
         (cond ~@guards
               true (throw ~error-local)
               )))))

;(defn local-hostname
;  []
;  (.getCanonicalHostName (InetAddress/getLocalHost)))
;
;(def memoized-local-hostname (memoize local-hostname))

;; checks conf for STORM_LOCAL_HOSTNAME.
;; when unconfigured, falls back to (memoized) guess by `local-hostname`.
;(defn hostname
;  [conf]
;  (conf Config/STORM_LOCAL_HOSTNAME (memoized-local-hostname)))

(letfn [(try-port [port]
                  (with-open [socket (java.net.ServerSocket. port)]
                    (.getLocalPort socket)))]
  (defn available-port
    ([] (try-port 0))
    ([preferred]
     (try
       (try-port preferred)
       (catch java.io.IOException e
         (available-port))))))

;(defn uuid []
;  (str (UUID/randomUUID)))

;(defn current-time-secs
;  []
;  (Time/currentTimeSecs))

;(defn current-time-millis
;  []
;  (Time/currentTimeMillis))

;(defn secs-to-millis-long
;  [secs]
;  (long (* (long 1000) secs)))

(defn clojurify-structure
  [s]
  (prewalk (fn [x]
             (cond (instance? Map x) (into {} x)
                   (instance? List x) (vec x)
                   ;; (Boolean. false) does not evaluate to false in an if.
                   ;; This fixes that.
                   (instance? Boolean x) (boolean x)
                   true x))
           s))

(defmacro with-file-lock
  [path & body]
  `(let [f# (File. ~path)
         _# (.createNewFile f#)
         rf# (RandomAccessFile. f# "rw")
         lock# (.. rf# (getChannel) (lock))]
     (try
       ~@body
       (finally
         (.release lock#)
         (.close rf#)))))

;(defn tokenize-path
;  [^String path]
;  (let [toks (.split path "/")]
;    (vec (filter (complement empty?) toks))))

;(defn assoc-conj
;  [m k v]
;  (merge-with concat m {k [v]}))

;; returns [ones in first set not in second, ones in second set not in first]
;(defn set-delta
;  [old curr]
;  (let [s1 (set old)
;        s2 (set curr)]
;    [(set/difference s1 s2) (set/difference s2 s1)]))

;(defn parent-path
;  [path]
;  (let [toks (Utils/tokenizePath path)]
;    (str "/" (str/join "/" (butlast toks)))))

;(defn toks->path
;  [toks]
;  (str "/" (str/join "/" toks)))

;(defn normalize-path
;  [^String path]
;  (Utils/toksToPath (Utils/tokenizePath path)))

;TODO: We're keeping this function around until all the code using it is properly tranlated to java
;TODO: by properly having the for loop IN THE JAVA FUNCTION that originally used this function.
(defn map-val
  [afn amap]
  (into {}
        (for [[k v] amap]
          [k (afn v)])))

;TODO: We're keeping this function around until all the code using it is properly tranlated to java
;TODO: by properly having the for loop IN THE JAVA FUNCTION that originally used this function.
(defn filter-val
  [afn amap]
  (into {} (filter (fn [[k v]] (afn v)) amap)))

;TODO: We're keeping this function around until all the code using it is properly tranlated to java
;TODO: by properly having the for loop IN THE JAVA FUNCTION that originally used this function.
(defn filter-key
  [afn amap]
  (into {} (filter (fn [[k v]] (afn k)) amap)))

;TODO: We're keeping this function around until all the code using it is properly tranlated to java
;TODO: by properly having the for loop IN THE JAVA FUNCTION that originally used this function.
(defn map-key
  [afn amap]
  (into {} (for [[k v] amap] [(afn k) v])))

;(defn full-path
;  [parent name]
;  (let [toks (Utils/tokenizePath parent)]
;    (Utils/toksToPath (conj toks name))))

;TODO: Once all the other clojure functions (100+ locations) are translated to java, this function becomes moot.
(def not-nil? (complement nil?))

;(defn exit-process!
;  [val & msg]
;  (log-error (RuntimeException. (str msg)) "Halting process: " msg)
;  (.exit (Runtime/getRuntime) val))

;(defn sum
;  [vals]
;  (reduce + vals))

;(defn repeat-seq
;  ([aseq]
;   (apply concat (repeat aseq)))
;  ([amt aseq]
;   (apply concat (repeat amt aseq))))

;(defn div
;  "Perform floating point division on the arguments."
;  [f & rest]
;  (apply / (double f) rest))

;(defn defaulted
;  [val default]
;  (if val val default))

;(defn mk-counter
;  ([] (mk-counter 1))
;  ([start-val]
;   (let [val (atom (dec start-val))]
;     (fn [] (swap! val inc)))))

;(defmacro for-times [times & body]
;  `(for [i# (range ~times)]
;     ~@body))

(defmacro dofor [& body]
  `(doall (for ~@body)))

;(defn reverse-map
;  "{:a 1 :b 1 :c 2} -> {1 [:a :b] 2 :c}"
;  [amap]
;  (reduce (fn [m [k v]]
;            (let [existing (get m v [])]
;              (assoc m v (conj existing k))))
;          {} amap))

(defmacro print-vars [& vars]
  (let [prints (for [v vars] `(println ~(str v) ~v))]
    `(do ~@prints)))

;(defn process-pid
;  "Gets the pid of this JVM. Hacky because Java doesn't provide a real way to do this."
;  []
;  (let [name (.getName (ManagementFactory/getRuntimeMXBean))
;        split (.split name "@")]
;    (when-not (= 2 (count split))
;      (throw (RuntimeException. (str "Got unexpected process name: " name))))
;    (first split)))

;(defn exec-command! [command]
;  (let [[comm-str & args] (seq (.split command " "))
;        command (CommandLine. comm-str)]
;    (doseq [a args]
;      (.addArgument command a))
;    (.execute (DefaultExecutor.) command)))

;(defn extract-dir-from-jar [jarpath dir destdir]
;  (try-cause
;    (with-open [jarpath (ZipFile. jarpath)]
;      (let [entries (enumeration-seq (.entries jarpath))]
;        (doseq [file (filter (fn [entry](and (not (.isDirectory entry)) (.startsWith (.getName entry) dir))) entries)]
;          (.mkdirs (.getParentFile (File. destdir (.getName file))))
;          (with-open [out (FileOutputStream. (File. destdir (.getName file)))]
;            (io/copy (.getInputStream jarpath file) out)))))
;    (catch IOException e
;      (log-message "Could not extract " dir " from " jarpath))))

;(defn sleep-secs [secs]
;  (when (pos? secs)
;    (Time/sleep (* (long secs) 1000))))

;(defn sleep-until-secs [target-secs]
;  (Time/sleepUntil (* (long target-secs) 1000)))

;(def ^:const sig-kill 9)
;
;(def ^:const sig-term 15)

;(defn send-signal-to-process
;  [pid signum]
;  (try-cause
;    (Utils/execCommand (str (if on-windows?
;                          (if (== signum sig-kill) "taskkill /f /pid " "taskkill /pid ")
;                          (str "kill -" signum " "))
;                     pid))
;    (catch ExecuteException e
;      (log-message "Error when trying to kill " pid ". Process is probably already dead."))))

;(defn read-and-log-stream
;  [prefix stream]
;  (try
;    (let [reader (BufferedReader. (InputStreamReader. stream))]
;      (loop []
;        (if-let [line (.readLine reader)]
;                (do
;                  (log-warn (str prefix ":" line))
;                  (recur)))))
;    (catch IOException e
;      (log-warn "Error while trying to log stream" e))))

;(defn force-kill-process
;  [pid]
;  (send-signal-to-process pid sig-kill))

;(defn kill-process-with-sig-term
;  [pid]
;  (send-signal-to-process pid sig-term))

;(defn add-shutdown-hook-with-force-kill-in-1-sec
;  "adds the user supplied function as a shutdown hook for cleanup.
;   Also adds a function that sleeps for a second and then sends kill -9 to process to avoid any zombie process in case
;   cleanup function hangs."
;  [func]
;  (.addShutdownHook (Runtime/getRuntime) (Thread. #(func)))
;  (.addShutdownHook (Runtime/getRuntime) (Thread. #((Time/sleepSecs 1)
;                                                    (.halt (Runtime/getRuntime) 20)))))

(defprotocol SmartThread
  (start [this])
  (join [this])
  (interrupt [this])
  (sleeping? [this]))

;; afn returns amount of time to sleep
(defnk async-loop [afn
                   :daemon false
                   :kill-fn (fn [error] (Utils/exitProcess 1 "Async loop died!"))
                   :priority Thread/NORM_PRIORITY
                   :factory? false
                   :start true
                   :thread-name nil]
  (let [thread (Thread.
                 (fn []
                   (try-cause
                     (let [afn (if factory? (afn) afn)]
                       (loop []
                         (let [sleep-time (afn)]
                           (when-not (nil? sleep-time)
                             (Time/sleepSecs sleep-time)
                             (recur))
                           )))
                     (catch InterruptedException e
                       (log-message "Async loop interrupted!")
                       )
                     (catch Throwable t
                       (log-error t "Async loop died!")
                       (kill-fn t)))))]
    (.setDaemon thread daemon)
    (.setPriority thread priority)
    (when thread-name
      (.setName thread (str (.getName thread) "-" thread-name)))
    (when start
      (.start thread))
    ;; should return object that supports stop, interrupt, join, and waiting?
    (reify SmartThread
      (start
        [this]
        (.start thread))
      (join
        [this]
        (.join thread))
      (interrupt
        [this]
        (.interrupt thread))
      (sleeping?
        [this]
        (Time/isThreadWaiting thread)))))

;(defn shell-cmd
;  [command]
;  (->> command
;    (map #(str \' (clojure.string/escape % {\' "'\"'\"'"}) \'))
;      (clojure.string/join " ")))

;(defn script-file-path [dir]
;  (str dir Utils/filePathSeparator "storm-worker-script.sh"))

;(defn container-file-path [dir]
;  (str dir Utils/filePathSeparator "launch_container.sh"))

(defnk write-script
  [dir command :environment {}]
  (let [script-src (str "#!/bin/bash\n" (clojure.string/join "" (map (fn [[k v]] (str (Utils/shellCmd ["export" (str k "=" v)]) ";\n")) environment)) "\nexec " (Utils/shellCmd command) ";")
        script-path (Utils/scriptFilePath dir)
        _ (spit script-path script-src)]
    script-path
  ))

(defnk launch-process
  [command :environment {} :log-prefix nil :exit-code-callback nil :directory nil]
  (let [builder (ProcessBuilder. command)
        process-env (.environment builder)]
    (when directory (.directory builder directory))
    (.redirectErrorStream builder true)
    (doseq [[k v] environment]
      (.put process-env k v))
    (let [process (.start builder)]
      (if (or log-prefix exit-code-callback)
        (async-loop
         (fn []
           (if log-prefix
             (Utils/readAndLogStream log-prefix (.getInputStream process)))
           (when exit-code-callback
             (try
               (.waitFor process)
               (catch InterruptedException e
                 (log-message log-prefix " interrupted.")))
             (exit-code-callback (.exitValue process)))
           nil)))                    
      process)))
   
;; (defn exists-file?
;;   [path]
;;   (.exists (File. path)))
;; 
;; (defn rmr
;;   [path]
;;   (log-debug "Rmr path " path)
;;   (when (exists-file? path)
;;     (try
;;       (FileUtils/forceDelete (File. path))
;;       (catch FileNotFoundException e))))
;; 
;; (defn rmpath
;;   "Removes file or directory at the path. Not recursive. Throws exception on failure"
;;   [path]
;;   (log-debug "Removing path " path)
;;   (when (exists-file? path)
;;     (let [deleted? (.delete (File. path))]
;;       (when-not deleted?
;;         (throw (RuntimeException. (str "Failed to delete " path)))))))
;; 
;; (defn local-mkdirs
;;   [path]
;;   (log-debug "Making dirs at " path)
;;   (FileUtils/forceMkdir (File. path)))
;; 
;; (defn touch
;;   [path]
;;   (log-debug "Touching file at " path)
;;   (let [success? (do (if on-windows? (.mkdirs (.getParentFile (File. path))))
;;                    (.createNewFile (File. path)))]
;;     (when-not success?
;;       (throw (RuntimeException. (str "Failed to touch " path))))))
;; 
;; (defn create-symlink!
;;   "Create symlink is to the target"
;;   ([path-dir target-dir file-name]
;;     (create-symlink! path-dir target-dir file-name file-name))
;;   ([path-dir target-dir from-file-name to-file-name]
;;     (let [path (str path-dir Utils/filePathSeparator from-file-name)
;;           target (str target-dir Utils/filePathSeparator to-file-name)
;;           empty-array (make-array String 0)
;;           attrs (make-array FileAttribute 0)
;;           abs-path (.toAbsolutePath (Paths/get path empty-array))
;;           abs-target (.toAbsolutePath (Paths/get target empty-array))]
;;       (log-debug "Creating symlink [" abs-path "] to [" abs-target "]")
;;       (if (not (.exists (.toFile abs-path)))
;;         (Files/createSymbolicLink abs-path abs-target attrs)))))
;; 
;; (defn read-dir-contents
;;   [dir]
;;   (if (exists-file? dir)
;;     (let [content-files (.listFiles (File. dir))]
;;       (map #(.getName ^File %) content-files))
;;     []))
;; 
;; (defn compact
;;   [aseq]
;;   (filter (complement nil?) aseq))
;; 
;; (defn current-classpath
;;   []
;;   (System/getProperty "java.class.path"))
;; 
;; (defn get-full-jars
;;   [dir]
;;   (map #(str dir Utils/filePathSeparator %) (filter #(.endsWith % ".jar") (read-dir-contents dir))))
;; 
;; (defn worker-classpath
;;   []
;;   (let [storm-dir (System/getProperty "storm.home")
;;         storm-lib-dir (str storm-dir Utils/filePathSeparator "lib")
;;         storm-conf-dir (if-let [confdir (System/getenv "STORM_CONF_DIR")]
;;                          confdir 
;;                          (str storm-dir Utils/filePathSeparator "conf"))
;;         storm-extlib-dir (str storm-dir Utils/filePathSeparator "extlib")
;;         extcp (System/getenv "STORM_EXT_CLASSPATH")]
;;     (if (nil? storm-dir) 
;;       (current-classpath)
;;       (str/join Utils/classPathSeparator
;;                 (remove nil? (concat (get-full-jars storm-lib-dir) (get-full-jars storm-extlib-dir) [extcp] [storm-conf-dir]))))))
;; 
;; (defn add-to-classpath
;;   [classpath paths]
;;   (if (empty? paths)
;;     classpath
;;     (str/join Utils/classPathSeparator (cons classpath paths))))
;; 
;; (defn ^ReentrantReadWriteLock mk-rw-lock
;;   []
;;   (ReentrantReadWriteLock.))
;; 
;; (defmacro read-locked
;;   [rw-lock & body]
;;   (let [lock (with-meta rw-lock {:tag `ReentrantReadWriteLock})]
;;     `(let [rlock# (.readLock ~lock)]
;;        (try (.lock rlock#)
;;          ~@body
;;          (finally (.unlock rlock#))))))
;; 
;; (defmacro write-locked
;;   [rw-lock & body]
;;   (let [lock (with-meta rw-lock {:tag `ReentrantReadWriteLock})]
;;     `(let [wlock# (.writeLock ~lock)]
;;        (try (.lock wlock#)
;;          ~@body
;;          (finally (.unlock wlock#))))))
;; 
;; (defn time-delta
;;   [time-secs]
;;   (- (Utils/currentTimeSecs) time-secs))
;; 
;; (defn time-delta-ms
;;   [time-ms]
;;   (- (System/currentTimeMillis) (long time-ms)))
;; 
;; (defn parse-int
;;   [str]
;;   (Integer/valueOf str))
;; 
;; (defn integer-divided
;;   [sum num-pieces]
;;   (clojurify-structure (Utils/integerDivided sum num-pieces)))
;; 
;; (defn collectify
;;   [obj]
;;   (if (or (sequential? obj) (instance? Collection obj))
;;     obj
;;     [obj]))
;; 
;; (defn to-json
;;   [obj]
;;   (JSONValue/toJSONString obj))
;; 
;; (defn from-json
;;   [^String str]
;;   (if str
;;     (clojurify-structure
;;       (JSONValue/parse str))
;;     nil))
;; 
;; (defmacro letlocals
;;   [& body]
;;   (let [[tobind lexpr] (split-at (dec (count body)) body)
;;         binded (vec (mapcat (fn [e]
;;                               (if (and (list? e) (= 'bind (first e)))
;;                                 [(second e) (last e)]
;;                                 ['_ e]
;;                                 ))
;;                             tobind))]
;;     `(let ~binded
;;        ~(first lexpr))))
;; 
;; (defn remove-first
;;   [pred aseq]
;;   (let [[b e] (split-with (complement pred) aseq)]
;;     (when (empty? e)
;;       (throw (IllegalArgumentException. "Nothing to remove")))
;;     (concat b (rest e))))
;; 
;; (defn assoc-non-nil
;;   [m k v]
;;   (if v (assoc m k v) m))
;; 
;; (defn multi-set
;;   "Returns a map of elem to count"
;;   [aseq]
;;   (apply merge-with +
;;          (map #(hash-map % 1) aseq)))
;; 
;; (defn set-var-root*
;;   [avar val]
;;   (alter-var-root avar (fn [avar] val)))
;; 
;; (defmacro set-var-root
;;   [var-sym val]
;;   `(set-var-root* (var ~var-sym) ~val))
;; 
;; (defmacro with-var-roots
;;   [bindings & body]
;;   (let [settings (partition 2 bindings)
;;         tmpvars (repeatedly (count settings) (partial gensym "old"))
;;         vars (map first settings)
;;         savevals (vec (mapcat (fn [t v] [t v]) tmpvars vars))
;;         setters (for [[v s] settings] `(set-var-root ~v ~s))
;;         restorers (map (fn [v s] `(set-var-root ~v ~s)) vars tmpvars)]
;;     `(let ~savevals
;;        ~@setters
;;        (try
;;          ~@body
;;          (finally
;;            ~@restorers)))))


;(defn rotating-random-range
;  [choices]
;  (let [rand (Random.)
;        choices (ArrayList. choices)]
;    (Collections/shuffle choices rand)
;    [(MutableInt. -1) choices rand]))

;(defn acquire-random-range-id
;  [[^MutableInt curr ^List state ^Random rand]]
;  (when (>= (.increment curr) (.size state))
;    (.set curr 0)
;    (Collections/shuffle state rand))
;  (.get state (.get curr)))

;(defmacro benchmark
;  [& body]
;  `(let [l# (doall (range 1000000))]
;     (time
;       (doseq [i# l#]
;         ~@body))))

;(defn rand-sampler
;  [freq]
;  (let [r (java.util.Random.)]
;    (fn [] (= 0 (.nextInt r freq)))))


;(defn sampler-rate
;  [sampler]
;  (:rate (meta sampler)))

(defmacro with-error-reaction
  [afn & body]
  `(try ~@body
     (catch Throwable t# (~afn t#))))


(defn uptime-computer []
  (let [start-time (Utils/currentTimeSecs)]
    (fn [] (Time/delta start-time))))

;(defn nil-to-zero
;  [v]
;  (or v 0))


;(defn container
;  []
;  (Container.))

;(defn container-set! [^Container container obj]
;  (set! (. container object) obj)
;  container)

;(defn container-get [^Container container]
;  (. container object))

;(defn throw-runtime [& strs]
;  (throw (RuntimeException. (apply str strs))))

(defn redirect-stdio-to-slf4j!
  []
  ;; set-var-root doesn't work with *out* and *err*, so digging much deeper here
  ;; Unfortunately, this code seems to work at the REPL but not when spawned as worker processes
  ;; it might have something to do with being a child process
  ;; (set! (. (.getThreadBinding RT/OUT) val)
  ;;       (java.io.OutputStreamWriter.
  ;;         (log-stream :info "STDIO")))
  ;; (set! (. (.getThreadBinding RT/ERR) val)
  ;;       (PrintWriter.
  ;;         (java.io.OutputStreamWriter.
  ;;           (log-stream :error "STDIO"))
  ;;         true))
  (log-capture! "STDIO"))

;(defn spy
;  [prefix val]
;  (log-message prefix ": " val)
;  val)

;; end of batch 4

;; The following two will go away when worker, task, executor go away.
(defn assoc-apply-self [curr key afn]
  (assoc curr key (afn curr)))

(defmacro recursive-map
  [& forms]
    (->> (partition 2 forms)
         (map (fn [[key form]] `(assoc-apply-self ~key (fn [~'<>] ~form))))
         (concat `(-> {}))))

; These six following will go away later. To be replaced by native java loops.
(defmacro fast-list-iter
  [pairs & body]
  (let [pairs (partition 2 pairs)
        lists (map second pairs)
        elems (map first pairs)
        iters (map (fn [_] (gensym)) lists)
        bindings (->> (map (fn [i l] [i `(if ~l (.iterator ~l))]) iters lists)
                      (apply concat))
        tests (map (fn [i] `(and ~i (.hasNext ^Iterator ~i))) iters)
        assignments (->> (map (fn [e i] [e `(.next ^Iterator ~i)]) elems iters)
                         (apply concat))]
    `(let [~@bindings]
       (while (and ~@tests)
         (let [~@assignments]
           ~@body)))))

(defmacro fast-list-for
  [[e alist] & body]
  `(let [ret# (ArrayList.)]
     (fast-list-iter [~e ~alist]
                     (.add ret# (do ~@body)))
     ret#))

(defmacro fast-map-iter
  [[bind amap] & body]
  `(let [iter# (if ~amap (.. ^Map ~amap entrySet iterator))]
     (while (and iter# (.hasNext ^Iterator iter#))
       (let [entry# (.next ^Iterator iter#)
             ~bind [(.getKey ^Map$Entry entry#) (.getValue ^Map$Entry entry#)]]
         ~@body))))

(defn fast-group-by
  [afn alist]
  (let [ret (HashMap.)]
    (fast-list-iter
      [e alist]
      (let [key (afn e)
            ^List curr (let [curr (.get ret key)]
                         (if curr
                           curr
                           (let [default (ArrayList.)]
                             (.put ret key default)
                             default)))]
        (.add curr e)))
    ret))

(defmacro -<>
  ([x] x)
  ([x form] (if (seq? form)
              (with-meta
                (let [[begin [_ & end]] (split-with #(not= % '<>) form)]
                  (concat begin [x] end))
                (meta form))
              (list form x)))
  ([x form & more] `(-<> (-<> ~x ~form) ~@more)))

(defn hashmap-to-persistent [^HashMap m]
  (zipmap (.keySet m) (.values m)))

