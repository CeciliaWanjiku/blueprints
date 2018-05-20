(ns blueprints.datomic.schema.note
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.note/add-schema-02242017
  (concat
   (s/generate-schema
    [(s/schema
      note
      (s/fields
       [uuid :uuid :unique-identity
        "Unique identifier for this note."]

       [author :ref :index
        "The author (account) of this note."]

       [subject :string :fulltext
        "The subject line (i.e. title) of this note."]

       [content :string :fulltext
        "The content of this note."]

       [children :ref :many :index :component
        "Child notes of this note. Used to implement threads/comments."]

       [tags :ref :many :index
        "Tags used to categorize this note."]))

     ;; a note can be treated as a ticket by giving it a status and other
     ;; optional attributes
     (s/schema
      ticket
      (s/fields
       [status :ref :index
        "The status of this ticket."]

       [assigned-to :ref :index
        "The account that this ticket is assigned to."]))])

   [{:db/id    (tds/tempid)
     :db/ident :ticket.status/open}
    {:db/id    (tds/tempid)
     :db/ident :ticket.status/closed}]))


(tds/defschema :schema.note/index-attrs-and-add-ref-10222017
  [:schema.note/add-schema-02242017]
  (concat
   (s/generate-schema
    [(s/schema
      note
      (s/fields
       [ref :ref :indexed
        "Reference to another entity--assumed to be the topic of this note."]))])

   [{:db/id               :note/author
     :db/index            true
     :db.alter/_attribute :db.part/db}
    {:db/id               :note/children
     :db/index            true
     :db.alter/_attribute :db.part/db}
    {:db/id               :note/tags
     :db/index            true
     :db.alter/_attribute :db.part/db}
    {:db/id               :ticket/status
     :db/index            true
     :db.alter/_attribute :db.part/db}
    {:db/id               :ticket/assigned-to
     :db/index            true
     :db.alter/_attribute :db.part/db}]))
