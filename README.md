# Freecoin crypto wallet toolkit - core library


[![software by Dyne.org](https://www.dyne.org/wp-content/uploads/2015/12/software_by_dyne.png)](http://www.dyne.org)

Freecoin is a toolkit to build participatory budget management wallets on top of multiple crypto-currency backends. It is open source and written in Clojure. Freecoin's main use-case is that of developing "social wallets" where balances and transactions are trasparent to entire groups of people to help participatory budgeting activities and organisational awareness.

[![Clojars Project](https://clojars.org/org.clojars.dyne/freecoin-lib/latest-version.svg)](https://clojars.org/org.clojars.dyne/freecoin-lib)


## Design

The design of Freecoin is informed by an extensive economic and user-centered research conducted by the D-CENT project and documented in deliverables that are available to the public:

- [Design of Social Digital Currency (D4.4)](http://dcentproject.eu/wp-content/uploads/2015/10/design_of_social_digital_currency_publication.pdf)
- [Implementation of digital social currency infrastructure (D5.5)](http://dcentproject.eu/wp-content/uploads/2015/10/D5.5-Implementation-of-digital-social-currency-infrastructure-.pdf).

More resources can be found on the D-CENT webpage: http://dcentproject.eu/resource_category/publications/

Furthermore, Freecoin's first social wallet pilots are informed by the research made in the [PIE Project](http://pieproject.eu).


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
