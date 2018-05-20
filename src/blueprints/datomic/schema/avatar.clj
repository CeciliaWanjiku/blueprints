(ns blueprints.datomic.schema.avatar
  "This was added 4/23/2017, and isn't currently used anywhere. It was designed
  this way so that we could have avatars for non-account 'users', e.g. the
  `:system` (using `:avatar/name`)."
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.avatar/add-avatar-schema
  (s/generate-schema
   [(s/schema
     avatar
     (s/fields
      [name :keyword :unique-identity
       "The unique identity of this avatar. Internal convenience."]

      [url :string :index
       "The url of the image resource."]

      [account :ref :index
       "The account that this avatar belongs to."]))]))
