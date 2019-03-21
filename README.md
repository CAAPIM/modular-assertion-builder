[![Build Status](https://travis-ci.com/ca-api-gateway/modular-assertion-builder.svg?branch=master)](https://travis-ci.com/ca-api-gateway/modular-assertion-builder)
[![Maven Central](https://img.shields.io/maven-central/v/com.ca.apim.gateway/modular-assertion-builder.svg)](https://search.maven.org/artifact/com.ca.apim.gateway/modular-assertion-builder)
[![Gradle Plugin](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/ca/apim/gateway/modular-assertion-builder/com.ca.apim.gateway.modular-assertion-builder.gradle.plugin/maven-metadata.xml.svg?label=gradle-plugin)](https://plugins.gradle.org/plugin/com.ca.apim.gateway.modular-assertion-builder)

# About
The modular assertion builder is a gradle plugin that can be used to build modular assertions for the CA API Gateway

# Usage
In order to use this plugin to build a modular assertion add the following you your gradle file:

```groovy
plugins {
    id "com.ca.apim.gateway.modular-assertion-builder" version "<version>"
}

group 'com.ca'
version '<assertion-version>'

repositories {
    maven {
        url "<repository-url>"
    }
}

modularAssertionBuilder {
    gatewayBaseVersion = '<gateway-version>'
    assertionName='My-Modular-Assertion'
    revision='<source code revision id>' // optional property to id the source commit, e.g. git commit hash / svn revision number
}

dependencies {
    // These are libraries that are required to be packaged in the Modular Assertion
    releaseJars(
    )
}
```

# Building the Plugin
The build is done using gradle. The following targets are exposed:

## clean
This removes the build directory.

## build
This target builds the Modular Assertion Builder. Once built it is available in the `build/libs` directory. 
This target uses the `version` property to set the version in the package file name.

## publish
This target publishes the built package to artifact repository. 

The following properties are used in the `publish` target:

Property       | Description
-------------- | -----------
mavenUser      | This is the name of the artifactory user to authenticate with
mavenPassword  | This is the password of the artifactory user to authenticate with
mavenUrl       | This is the artifact repository url to publish to.

### publish to local
You can also publish the modular assertion builder to your local maven repository by running:
`gradle install`

## How You Can Contribute
Contributions are welcome and much appreciated. To learn more, see the [Contribution Guidelines][contributing].

## License

Copyright (c) 2017 CA. All rights reserved.

This software may be modified and distributed under the terms
of the MIT license. See the [LICENSE][license-link] file for details.


 [license-link]: /LICENSE
 [contributing]: /CONTRIBUTING.md
