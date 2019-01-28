# Freecoin-lib - library to facilitate blockchain functions

The freecoin lib is the basic component of the social wallet toolkit. It contains implementations to an interface based on blockchain interactions. It is backed up by mongoDB and it contains one to one interactions, higher level interactions and metadata.

Social wallet (aka Freecoin) is a toolkit to build participatory budget management wallets on top of multiple crypto-currency backends. It is open source and written in Clojure. Freecoin's main use-case is that of developing "social wallets" where balances and transactions are trasparent to entire groups of people to help participatory budgeting activities and organisational awareness.


<a href="https://www.dyne.org"><img
src="https://secrets.dyne.org/static/img/swbydyne.png"
alt="software by Dyne.org"
title="software by Dyne.org" class="pull-right"></a>

[Getting started](#Getting-Started) | [Prerequisites](#Prerequisites) | [Running](#Running) | [Running the tests](#Running-the-tests) | [Deployment](#Deployment) | [Todos](#Todos) | [Acknowledgements](#Acknowledgements) | [Licence](#Licence) | [change log](https://github.com/Commonfare-net/social-wallet-api/blob/master/CHANGELOG.markdown) 

[![Build Status](https://travis-ci.org/Commonfare-net/freecoin-lib.svg?branch=master)](https://travis-ci.org/Commonfare-net/freecoin-lib)
[![Clojars Project](https://clojars.org/org.clojars.dyne/freecoin-lib/latest-version.svg)](https://clojars.org/org.clojars.dyne/freecoin-lib)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

This library is the base component for the social wallet (previously called Freecoin): https://freecoin.dyne.org

[![Freecoin.dyne.org](https://freecoin.dyne.org/images/freecoin_logo.png)](https://freecoin.dyne.org)

## Getting started
<img class="pull-right"
src="https://secrets.dyne.org/static/img/clojure.png">

The Freecoin-lib is written in Clojure and is fully
cross-platform: one can run it locally on a GNU/Linux machine, as well
on Apple/OSX and MS/Windows.

### Prerequisites

Install all necessary dependencies, for instance using the following packages found on APT based systems:

```
openjdk version > 7,  mongodb, libversioneer-clojure, haveged, mongodb-server
```

then install Leiningen which will take care of all Clojure dependencies

```
mkdir ~/bin
wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -O ~/bin/lein
chmod +x ~/bin/lein
```

then start the MongoDB server in which Freecoin will store its data:

```
sudo service mongod start
```

### Running

From inside the Freecoin source, start it with

```
lein repl
(start)
```
This is useful for development. For production we have wrapped the base lib in a Swagger API which, the [social wallet API](https://github.com/Commonfare-net/social-wallet-api).

## Running the tests

To run all tests one need to run
` lein midje`
on the project dir

## Deployment

Look at the [social wallet API](https://github.com/Commonfare-net/social-wallet-api)

## Todos

- Implement interfacing with more blockchain endpoints like other than the Bitcoin and its forks.
- Implement interfacing with more DBs like GraphDB.


## Acknowledgements

Freecoin-lib development is lead by Aspasia Beneti <aspra@dyne.org>

Freecoin-lib co-design is lead by Denis Roio <jaromil@dyne.org> and Marco Sachy <radium@dyne.org>

With expert contributions by Carlo Sciolla, Duncan Mortimer, Arjan Scherpenisse, Amy Welch, Gareth Rogers, Joonas Pekkanen, Thomas König and Enric Duran.

The Freecoin-lib is Free and Open Source research and development
activity funded by the European Commission in the context of
the
[Collective Awareness Platforms for Sustainability and Social Innovation (CAPSSI)](https://ec.europa.eu/digital-single-market/en/collective-awareness) program. It is used as the
underlying 
blockchain implementation library and adopted as a component of the
social wallet toolkit being developed for
the [Commonfare project](https://pieproject.eu) (grant nr. 687922) .


## Licence

This project is licensed under the AGPL 3 License - see the [LICENCE](LICENCE) file for details
