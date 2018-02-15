# Freecoin crypto wallet toolkit - core library


[![software by Dyne.org](https://www.dyne.org/wp-content/uploads/2015/12/software_by_dyne.png)](http://www.dyne.org)

The freecoin lib is the basic component of the social wallet toolkit. It contains implementations to an interface based on blockchain interactions. It is backed up by mongoDB and it contains one to one interactions, higher level interactions and metadata.

Social wallet is a toolkit to build participatory budget management wallets on top of multiple crypto-currency backends. It is open source and written in Clojure. Freecoin's main use-case is that of developing "social wallets" where balances and transactions are trasparent to entire groups of people to help participatory budgeting activities and organisational awareness.

[![Clojars Project](https://clojars.org/org.clojars.dyne/freecoin-lib/latest-version.svg)](https://clojars.org/org.clojars.dyne/freecoin-lib)

[![Build Status](https://travis-ci.org/Commonfare-net/freecoin-lib.svg?branch=master)](https://travis-ci.org/Commonfare-net/freecoin-lib)

For more informations see: https://freecoin.dyne.org

[![Freecoin.dyne.org](https://freecoin.dyne.org/images/freecoin_logo.png)](https://freecoin.dyne.org)

## Running the app locally

Install all necessary dependencies, for instance using the following packages found on APT based systems:

```
openjdk-7-jdk mongodb libversioneer-clojure haveged mongodb-server
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

then from inside the Freecoin source, start it with

```
lein repl
(start)
```

## License


This Free and Open Source research and development activity is funded by the European Commission in the context of Collective Awareness Platforms for Sustainability and Social Innovation (CAPSSI) grants nr.610349 and nr.687922.

The Freecoin toolkit is Copyright (C) 2015-2017 by the Dyne.org Foundation, Amsterdam

Freecoin development is lead by Aspasia Beneti <aspra@dyne.org>

Freecoin co-design is lead by Denis Roio <jaromil@dyne.org> and Marco Sachy <radium@dyne.org>

With expert contributions by Carlo Sciolla, Duncan Mortimer, Arjan Scherpenisse, Amy Welch, Gareth Rogers, Joonas Pekkanen, Thomas König and Enric Duran.

The Freecoin "cornucopia" logo is an artwork by Andrea Di Cesare.


```
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
