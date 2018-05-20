(ns blueprints.datomic.schema.referral
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.referral/add-referral-schema-03232017
  (concat
   (s/generate-schema
    [(s/schema
      referral
      (s/fields
       [source :string :fulltext
        "The referral source, i.e. the way in which this referral came to us."]

       [account :ref :index
        "The account associated with this referral."]

       [from :ref :index
        "The place that this referral came from within our product(s) (enum)."]

       [tour-for :ref :index
        "The community that this tour referral was booked for."]))])

   [{:db/id    (tds/tempid)
     :db/ident :referral.from/apply}
    {:db/id    (tds/tempid)
     :db/ident :referral.from/tour}]))
