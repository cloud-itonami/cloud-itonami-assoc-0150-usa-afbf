(ns association-facts-test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is testing]]
            [kotoba.compiler.core :as compiler]
            [kotoba.compiler.ir :as ir]))

(def source (slurp "src/association_facts.kotoba"))
(defn call [kir function & args] (ir/execute kir function (vec args)))
(defn present [option] (when (second option) (nth option 2)))
(def fields ["id" "title" "association" "isic" "country" "kind" "url"
             "url-provenance" "established-date" "retrieved-at"])
(def expected
  [{"id" "afbf.who-we-are" "title" "Who We Are" "association" "afbf"
    "isic" "0150" "country" "USA" "kind" "governance-program"
    "url" "https://www.fb.org/about/who-we-are"
    "url-provenance" "official-association-site"
    "established-date" "1919-11-12" "retrieved-at" "2026-07-16"}
   {"id" "afbf.what-we-do" "title" "What We Do" "association" "afbf"
    "isic" "0150" "country" "USA" "kind" "governance-program"
    "url" "https://www.fb.org/about/what-we-do"
    "url-provenance" "official-association-site" "retrieved-at" "2026-07-16"}])

(deftest reference-preserves-complete-catalog-and-absence
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        observed (mapv (fn [index]
                         (into {} (keep (fn [field]
                                          (when-let [value (present (call kir 'entry-field "afbf" index field))]
                                            [field value]))
                                        fields)))
                       (range (call kir 'entry-count "afbf")))]
    (is (= expected observed))
    (is (= ["governance" "advocacy"]
           (mapv #(present (call kir 'topic "afbf" % 0)) [0 1])))
    (is (= 1 (call kir 'by-topic-count "afbf" "governance")))
    (is (= 1 (call kir 'by-topic-count "afbf" "advocacy")))
    (is (= "afbf.who-we-are" (present (call kir 'by-topic-id "afbf" "governance" 0))))
    (is (= "afbf.what-we-do" (present (call kir 'by-topic-id "afbf" "advocacy" 0))))
    (is (nil? (present (call kir 'entry-field "afbf" 1 "established-date"))))
    (is (= #{} (set (:effects kir))))
    (testing "unknown values and invalid indexes fail closed"
      (is (zero? (call kir 'entry-count "wef")))
      (is (nil? (present (call kir 'entry-field "wef" 0 "id"))))
      (is (nil? (present (call kir 'entry-field "afbf" -1 "id"))))
      (is (nil? (present (call kir 'entry-field "afbf" 2 "id"))))
      (is (nil? (present (call kir 'entry-field "afbf" 0 "unknown"))))
      (is (nil? (present (call kir 'topic "afbf" 0 1))))
      (is (zero? (call kir 'by-topic-count "afbf" "labor")))
      (is (nil? (present (call kir 'by-topic-id "afbf" "advocacy" 1)))))))

(defn compiler-root []
  (nth (iterate #(.getParent ^java.nio.file.Path %)
                (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [value] (.encodeToString (java.util.Base64/getEncoder) value))

(deftest restricted-javascript-and-typed-wasm-conform-semantically
  (let [javascript (compiler/compile-source source :js-kotoba-v1)
        wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source javascript) "UTF-8"))
        wasm64 (base64 ^bytes (:bytes wasm))
        probe (shell/sh
                "node" "--input-type=module" "-e"
                (str "import(process.argv[1]).then(async host=>{"
                     "const j=await import('data:text/javascript;base64," js64 "');"
                     "const w=await host.instantiateKotoba(Buffer.from(process.argv[2],'base64'));"
                     "const run=x=>{if(x['entry-count']('afbf')!==2n||x['entry-count']('wef')!==0n)throw Error('count');"
                     "if(x['entry-field']('afbf',0n,'established-date')[2]!=='1919-11-12')throw Error('date');"
                     "if(x['entry-field']('afbf',1n,'established-date')[1]!==false)throw Error('absence');"
                     "if(x['topic']('afbf',0n,0n)[2]!=='governance'||x['topic']('afbf',1n,0n)[2]!=='advocacy')throw Error('topic');"
                     "if(x['by-topic-id']('afbf','advocacy',0n)[2]!=='afbf.what-we-do')throw Error('filter');"
                     "if(x['entry-field']('afbf',2n,'id')[1]!==false||x['by-topic-id']('afbf','advocacy',1n)[1]!==false)throw Error('reject');};"
                     "run(j.instantiateKotoba({}));run(w.instance.exports);"
                     "}).catch(e=>{console.error(e);process.exit(99)})")
                (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit probe)) (str (:out probe) (:err probe)))))

(deftest production-source-authority
  (is (= ["src/association_facts.kotoba"]
         (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
