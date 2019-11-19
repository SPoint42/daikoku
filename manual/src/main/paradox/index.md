# Daikoku

**Daikoku** is developer portal for Otoroshi written in <a href="https://www.scala-lang.org/" target="_blank">Scala</a> and developped by the <a href="https://maif.github.io" target="_blank">MAIF OSS</a> team.


> *In Japan, <a href="https://en.wikipedia.org/wiki/File:Daikoku.jpg" target="blank">Daikokuten</a> (大黒天), the god of great darkness or blackness, or the god of five cereals, is one of the Seven Lucky Gods (Fukujin). Daikokuten evolved from the Buddhist form of the Indian deity Shiva intertwined with the Shinto god Ōkuninushi. The name is the Japanese equivalent of Mahākāla, the Hindu name for Shiva.*

@@@ div { .centered-img }
[![Build Status](https://travis-ci.org/MAIF/daikoku.svg?branch=master)](https://travis-ci.org/MAIF/daikoku) [![Join the chat at https://gitter.im/MAIF/daikoku](https://badges.gitter.im/MAIF/daikoku.svg)](https://gitter.im/MAIF/daikoku?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [ ![Download](https://img.shields.io/github/release/MAIF/daikoku.svg) ](hhttps://github.com/MAIF/daikoku/releases/download/v1.0.0/daikoku.jar)
@@@

@@@ div { .centered-img }
<img src="https://github.com/MAIF/daikoku/raw/master/resources/daikoku-logo.png" width="300"></img>
@@@

## Installation

You can download the latest build of Daikoku as a [fat jar](https://github.com/MAIF/daikoku/releases/download/v1.0.0/daikoku.jar), as a [zip package](https://github.com/MAIF/daikoku/releases/download/v1.0.0/daikoku-dist.zip) 

You can install and run Otoroshi with this little bash snippet

```sh
curl -L -o otoroshi.jar 'https://github.com/MAIF/daikoku/releases/download/v1.0.0./daikoku.jar'
java -jar otoroshi.jar
```

or using docker

```sh
docker run -p "8080:8080" maif/daikoku:1.0.0
```

now open your browser to <a href="http://localhost:8080/" target="_blank">http://localhost:8080/</a>, **log in with the credential generated in the logs** and explore by yourself, if you want better instructions, just go to the @ref:[Quick Start](./quickstart.md)

## Documentation

* @ref:[About Daikoku](./about.md)
* @ref:[Architecture](./archi.md)

## Discussion

Join the [Daikoku](https://gitter.im/MAIF/daikoku) channel on the [MAIF Gitter](https://gitter.im/MAIF)

## Sources

The sources of Daikoku are available on [Github](https://github.com/MAIF/daikoku).

## Logo

You can find the official Daikoku logo [on GitHub](https://github.com/MAIF/daikoku/blob/master/resources/daikoku-logo.png). The Daikoku logo has been created by François Galioto ([@fgalioto](https://twitter.com/fgalioto))

## Changelog

Every release, along with the migration instructions, is documented on the [Github Releases](https://github.com/MAIF/daikoku/releases) page.

## Patrons

The work on Daikoku was funded by <a href="https://www.maif.fr/" target="_blank">MAIF</a> with the help of the community.

## Licence

Daikoku is Open Source and available under the [Apache 2 License](https://opensource.org/licenses/Apache-2.0)

@@@ index

* [About Daikoku](about.md)
* [Architecture](archi.md)

@@@