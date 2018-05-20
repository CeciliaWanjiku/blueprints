(ns blueprints.datomic.schema.onboard
  "The `onboard` schema specifies an entity that holds in-progress information
  during the onboarding flow. This is primarily used to hold data pertaining to
  service options that will be converted to service requests upon completion of
  onboarding."
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.onboard/add-schema-04102017
  (s/generate-schema
   [(s/schema
     onboard
     (s/fields
      [account :ref :indexed
       "The account that this onboarding information is for."]

      [move-in :instant :indexed
       "The move-in date and time that has been selected."]

      [seen :keyword :many :indexed
       "Steps in the onboarding flow that have been 'seen'."]))]))
