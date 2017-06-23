# About

The modular assertion builder plugin can be used to build modular assertions for the CA API Gateway

> TODO: Add the below sections

# Usage
In order to use this plugin to build a modular assertion add the following you your gradle file:

```groovy
apply plugin: 'com.ca.apim.gateway.modassbuilder.modular-assertion-builder'

buildscript {
    repositories {
        maven {
            url "http://artifactory-van.ca.com/artifactory/maven-repo"
        }
    }
    dependencies {
        classpath 'com.ca.apim.gateway.modassbuilder:ModularAssertionBuilder:0.1.00-SNAPSHOT'
    }
}

ModularAssertionBuilder {
    gatewayBaseVersion = '9.2.00'
    assertionName='My-Modular-Assertion'
}

dependencies {
    // These are libraries that are required to be packaged in the Modular Assertion
    releaseJars(
    )
}
```

# Building the Plugin
![Build Status](https://apim-teamcity.l7tech.com:8443/app/rest/builds/buildType:(id:ApiGateway_Utilities_ModularAssertionBuilder)/statusIcon)

# Contributing

