# CUBA Full Text Search Add-on

[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/cuba-platform/reports.svg?branch=master)](https://travis-ci.org/cuba-platform/reports)
[![Documentation](https://img.shields.io/badge/documentation-online-03a9f4.svg)](https://doc.cuba-platform.com/reports-latest)

Full Text Search Add-on provides unstructured search within the values of entity attributes and content of uploaded files.

For more information see [github.com/cuba-platform/cuba](https://github.com/cuba-platform/cuba).

## Build and install

In order to build the add-on from source, you need to install the following:
* Java 8 Development Kit (JDK)
* [CUBA Gradle Plugin](https://github.com/cuba-platform/cuba-gradle-plugin)
* [CUBA](https://github.com/cuba-platform/cuba)

Let's assume that you have cloned sources into the following directories:
```
work/
    cuba/
    cuba-gradle-plugin/
    reports/
```

Open terminal in the `work` directory and run the following command to build and install the plugin into your local Maven repository (`~/.m2`):
```
cd cuba-gradle-plugin
gradlew install
```

After that, go to the cuba directory and build and install it with the same command:
```
cd ../cuba
gradlew install
```

Finally, go to the reports directory and build and install it with the same command:
```
cd ../reports
gradlew install
```