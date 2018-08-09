(ns blueprints.schema.file
  (:require [datomic-schema.schema :as s]))


(def ^{:added "1.0.x"} schema
  (s/generate-schema
   [(s/schema
     income-file
     (s/fields
      [account :ref
       "The account that this income file belongs to."]

      ;; TODO: The following three attributes could be better generalized to
      ;; `:file/#{content-type size path}`.
      [content-type :string
       "The type of content that this file holds."]
      [size :long
       "The size of this file in bytes."]
      [path :string
       "The path to this file on the filesystem."]))]))


(def ^{:added "2.7.0"} add-indexes
  "Add indexes to attributes that do not have them."
  [{:db/id               :income-file/content-type
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :income-file/size
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :income-file/path
    :db/index            true
    :db.alter/_attribute :db.part/db}])


(def ^{:added "2.7.0"} rename-attributes
  "Renaming `income-file` to `file` for a more generic use case."
  [{:db/id               :income-file/content-type
    :db/ident            :file/content-type
    :db.alter/_attribute :db.part/db}
   {:db/id               :income-file/size
    :db/ident            :file/size
    :db.alter/_attribute :db.part/db}
   {:db/id               :income-file/path
    :db/ident            :file/uri
    :db.alter/_attribute :db.part/db}])


(def ^{:added "2.7.0"} add-filename
  (s/generate-schema
   [(s/schema
     file
     (s/fields
      [name :string :indexed
       "The name of the file."]))]))


(defn norms [part]
  {:starcity/add-income-files-schema-8-3-16
   {:txes [schema]}

   :starcity/edit-attr-and-add-filename-7-26-18
   {:txes     [add-indexes
               rename-attributes
               add-filename]
    :requires [:starcity/add-income-files-schema-8-3-16]}})
