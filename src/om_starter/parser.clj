(ns om-starter.parser)

(defmulti readf (fn [_ k _] k))

(defmethod readf :app/title
  [{:keys [state] :as env} k params]
  (let [st @state]
    (if-let [[_ value] (find st k)]
      {:value value}
      {:value "not-found"})))

(defmulti mutatef (fn [_ k _] k))

(defmethod mutatef 'app/update-title
  [{:keys [state]} _ {:keys [new-title]}]
  {:value [:app/title]
   :action (fn [] (swap! state assoc :app/title (str new-title " server")))})
