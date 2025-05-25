Migración BICE
=============

[![License](https://img.shields.io/github/license/bennu/bice-routes-back?label=License&logo=opensourceinitiative)](https://opensource.org/license/mit-0)
[![Supported JVM Versions](https://img.shields.io/badge/JVM-17--21-brightgreen.svg?label=JVM&logo=openjdk)](https://adoptium.net/es/temurin/releases/)

BICE Migration is a suite of support services for load testing, performance, and application migration.

🚀 Building
--------

Building requires a Java JDK and [Apache Maven](https://maven.apache.org/).
The required Java version is found in the `pom.xml` as the `maven.compiler.source` property.

From a command shell, run `mvn` without arguments to invoke the default Maven goal to run all tests and checks.

📝 License
-------

This code is licensed under the [MIT License](https://opensource.org/license/mit).

🔧 Dependencies
------------

- quarkus 3.22.2
- bennu-commons 1.5.0
- lombok 1.18.38