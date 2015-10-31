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

(defmulti read om/dispatch)

(defmethod read :app/title
  [{:keys [state] :as env} _ {:keys [remote?]}]
  (let [st @state]
    (if-let [v (get st :app/title)]
      {:value v :remote true}
      {:remote true})))

(defui Root
  static om/IQuery
  (query [this]
    '[:app/title])
  Object
  (render [this]
    (let [{:keys [app/title]} (om/props this)]
      (dom/div nil
        (dom/p nil title)
        (dom/input #js {:ref :title})
        (dom/button #js {:onClick
                         (fn [e] (let [new-title (.-value (dom/node this :title))]
                                   (om/transact! this `[(app/update-title {:new-title ~new-title})
                                                        :app/title])))} "update")))))

(defonce parser (om/parser {:read read :mutate mutate}))

(defonce reconciler
  (om/reconciler
    {:state (atom {})
     :normalize true
     :merge-tree merge
     :parser parser
     :send (util/transit-post "/api")}))

(om/add-root! reconciler Root (gdom/getElement "app"))
