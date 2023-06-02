# cloak

Small utility library that makes it possible to wrap sensitive values to prevent
their potential display in context where they shouldn't appear such as logs &
others.

It simply provides a wrapper type `exoscale.cloak.Secret` that can encapsulate
any value and overrides the various methods that could expose it.

It also provides a few helpers to `unmask` these values. 

The main idea is that you would have to unmask value at **the last possible**
moment (ex when using them for some signature) and **not** when you pass them to
some component or right after you read the config. 

## Examples


``` clj
(def s (cloak/mask "password1234"))
(prn s)

=> "<< cloaked >>"

;; you can just deref a masked value
@s 
=> "password1234"

;; or use the unmask function
(cloak/unmask s)
=> "password1234"

;; unmask will work recursively
(cloak/unmask {:config {:key s}})
=> {:config {:key "password1234"}}
```

### Aero 

If you were to use this with aero you might want to create a custom tag for it: 

``` clj
(defmethod aero/reader 'secret
  [_ _ value]
  (exoscale.cloak/mask value)) 
```

You can then have aero files such as this:

``` clj
{:stuff {:apikey #secret #env API_KEY}}
```

## Installation

cloak is [available on Clojars](https://clojars.org/exoscale/cloak).

Add this to your dependencies:

[![Clojars Project](https://img.shields.io/clojars/v/exoscale/cloak.svg)](https://clojars.org/exoscale/cloak)

## License

Copyright © 2022 [Exoscale](https://exoscale.com)
