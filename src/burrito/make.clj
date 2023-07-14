(ns burrito.make
  (:require [clojure.string :as str]
            tortilla.wrap)
  (:import org.reflections.Reflections
           (org.reflections.scanners Scanner SubTypesScanner)))

; TODO do not use 'sh' but directly call tortilla.main
; TODO call defwrapper Reflections
; TODO add javadoc automatically
; TODO sub-classes handle
;com.badlogic.gdx.scenes.scene2d.ui.window$1
;com.badlogic.gdx.scenes.scene2d.ui.tooltip$1
; TODO lein codox? other docs

(def java-package "com.badlogic.gdx")
(def clj-package "clj.libgdx")

(def refl (Reflections. package (into-array [(SubTypesScanner. false)])))

(defn- class->namespace [^Class klass]
  (-> (str (.getName (.getPackage klass)) "."
           (tortilla.wrap/camel->kebab (.getSimpleName klass)))
      (str/replace java-package clj-package)
      str/lower-case
      symbol))

(use '[clojure.java.shell :only [sh]])

(def classes (.getSubTypesOf refl Object))

; ./tortilla
; --class com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer
; --namespace com.badlogic.gdx.maps.tiled.renderers.batchtiledmaprenderer
; --out com.badlogic.gdx.maps.tiled.renderers.batchtiledmaprenderer.clj
; --dep '[com.badlogicgames.gdx/gdx "1.11.0"]'

(defn- namespace->file [nmspace]
  (-> (str "src/" nmspace)
      (str/replace "." "/")
      (str/replace "-" "_")
      (str  ".clj")))

(defn make []
  (doseq [klass classes]
     (println "\nGenerate " klass)
     (let [command ["./tortilla"
                    "--class"
                    (.getName klass)
                    "--namespace"
                    (str (class->namespace klass))
                    "--out"
                    (str (namespace->file (class->namespace klass)))
                    "--dep"
                    "com.badlogicgames.gdx:gdx:1.11.0"]]
       (println "Command:\n" (str/join " " command))
       (println "RESULT: \n" (apply sh command)))))
