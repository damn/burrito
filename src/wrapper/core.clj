(ns wrapper.core
  (:require [clojure.string :as str]
            tortilla.wrap))

; TODO do not use 'sh' but directly call the generator by code

; => extend tortilla.main

; TODO defwrapper Reflections itself too ! <<<OO.OO>>>

; TODO add javadoc automatically !_!_!_!_!_! O.O.O.O

#_(doseq [p (filter #(str/includes? (.getName %) "com.badlogic") (Package/getPackages))]
  (println p))

(import 'org.reflections.Reflections)
(import '[org.reflections.scanners Scanner SubTypesScanner])

(def refl (Reflections. "com.badlogic.gdx" (into-array [(SubTypesScanner. false)])))

(defn- class->namespace [^Class klass]
  (-> (str (.getName (.getPackage klass)) "."
           (tortilla.wrap/camel->kebab (.getSimpleName klass)))
      (str/replace "com.badlogic.gdx" "wrapper.libgdx")
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

#_(time
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

; (/ (float 3094575.107905) 1000 60)
; 51 minutes !


#_(defn wrap-class [klass]
  (in-ns (class->namespace klass))
  (require 'tortilla.wrap)
  ;(println "In " *ns*)
  (try (eval `(tortilla.wrap/defwrapper ~klass))
       (catch Exception e
         (println "Exception at " klass)
         (println e))))


#_(doseq [t classes]
  (wrap-class t))

#_(in-ns 'wrapper.libgdx.wrappers)

; Looking for the namespace FILE which does not exist...
#_(doseq [nmspace (map class->namespace classes)]
  (require nmspace))

; whats that:
;com.badlogic.gdx.scenes.scene2d.ui.window$1
;com.badlogic.gdx.scenes.scene2d.ui.tooltip$1


; TODO
; -> generate folders/files & output the wrappers there
; => then lein codox can read it.
