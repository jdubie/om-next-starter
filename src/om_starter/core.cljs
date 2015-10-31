(ns om-starter.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om-starter.util :as util]
            [om.dom :as dom]))

(enable-console-print!)

(defmulti mutate om/dispatch)

(defmethod mutate 'app/update-title
  [{:keys [state]} _ {:keys [new-title]}]
  {:remote true
   :value [:app/title]
   :action (fn [] (swap! state assoc :app/title new-title))})

(defmethod mutate 'app/loading?
  [{:keys [state]} _ _]
  {:value [:loading?]
   :action (fn [] (swap! state assoc :loading? true))})

(defmulti read om/dispatch)

(defmethod read :app/title
  [{:keys [state] :as env} _ {:keys [remote?]}]
  (let [st @state]
    (if-let [v (get st :app/title)]
      {:value v :remote true}
      {:remote true})))

(defmethod read :loading?
  [{:keys [state] :as env} _ _]
  (let [st @state]
    (let [v (get st :loading? false)]
      (if v
        {:value v :remote true}
        {:value v}))))

(defui Root
  static om/IQuery
  (query [this]
    '[:app/title :loading?])
  Object
  (render [this]
    (let [{:keys [app/title loading?]} (om/props this)]
      (dom/div nil
        (dom/p nil title)
        (dom/p nil (pr-str loading?))
        (dom/input #js {:ref :title})
        (dom/button #js {:onClick
                         (fn [e] (let [new-title (.-value (dom/node this :title))]
                                   (om/transact! this `[(app/update-title {:new-title ~new-title})
                                                        (app/loading?)
                                                        :app/title
                                                        :loading?
                                                        ])))} "update")))))

(def parser (om/parser {:read read :mutate mutate}))

(def reconciler
  (om/reconciler
    {:state (atom {})
     :normalize true
     :merge-tree (fn [a b] (println "|merge" a b) (merge a b))
     :parser parser
     :send (util/transit-post "/api")}))

(om/add-root! reconciler Root (gdom/getElement "app"))
