(ns blueprints.datomic.schema.event
  "The `event` entity replaces `cmd` and `msg` and is used by `reactor`."
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.event/add-schema-06142017
  (concat
   (s/generate-schema
    [(s/schema
      event
      (s/fields
       [uuid :uuid :unique-identity
        "UUID to uniquely identify this event."]

       [id :string :unique-identity
        "An external (to the system) identifier for this event."]

       [key :keyword :indexed
        "Identifies the event for dispatching."]

       [topic :keyword :indexed
        "Categorizes the event."]

       [triggered-by :ref :indexed
        "Reference to the event that spawned this event."]

       [params :string
        "The serialized parameters for the handler."]

       [meta :string
        "Serielized metadata about this event."]

       [status :ref :indexed
        "pending, successful, failed"]))])

   [{:db/id    (tds/tempid)
     :db/ident :event.status/pending}
    {:db/id    (tds/tempid)
     :db/ident :event.status/successful}
    {:db/id    (tds/tempid)
     :db/ident :event.status/failed}
    {:db/id    (tds/tempid)
     :db/ident :event.status/seen}]))
