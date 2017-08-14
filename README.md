# About

The modular assertion builder is a gradle plugin that can be used to build modular assertions for the CA API Gateway

# Usage
In order to use this plugin to build a modular assertion add the following you your gradle file:

```groovy
group 'com.ca'
version '<assertion-version>'

apply plugin: 'com.ca.apim.gateway.modassbuilder.modular-assertion-builder'

buildscript {
    repositories {
        maven {
            url "http://artifactory-van.ca.com/artifactory/isl-maven-proxy-cache"
        }
    }
    dependencies {
        classpath 'com.ca.apim.gateway:ModularAssertionBuilder:<version>'
    }
}

modassBuilder {
    gatewayBaseVersion = '<gateway-version>'
    assertionName='My-Modular-Assertion'
}

dependencies {
    // These are libraries that are required to be packaged in the Modular Assertion
    releaseJars(
    )
}
```

# Branching and Tags

Branch or Tag | Name | Description
------------- | ---- | -----------
Branch | `master` | This branch contains the latest version of the Modular Assertion Builder. It must always be stable and ready for release. 
Branch | `feature/[RALLY#]<short-description>` | These branches contain feature work. Examples: `feature/F12345-cool-feature` or  `feature/awesome-change` 
Tag    | `release/<version>` | These are release tags. Add these tags to track released versions of the Modular Assertion Builder. Example: `release/2.0.00`

# Building the Plugin
![Build Status](https://apim-teamcity.l7tech.com:8443/app/rest/builds/buildType:(id:ApiGateway_Utilities_ModularAssertionBuilder)/statusIcon)
The build is done using gradle. The following targets are exposed:

## clean
This removes the build directory.

## build
This target builds the Modular Assertion Builder. Once built it is available in the `build/libs` directory. 
This target uses the `version` property to set the version in the package file name.

## publish
This target publishes the built package to artifactory. 

Snapshots are published to [maven-integration-local][maven-integration-local-ModularAssertionBuilder]

Releases are published to [maven-release-candidate-local][maven-release-candidate-local-ModularAssertionBuilder]

The following properties are used in the `publish` target:

Property       | Description
-------------- | -----------
version        | This is the version of the artifact to publish. During snapshot builds it is retrieved from the [gradle.properties](gradle.properties) file, for release builds it comes from the tag name.
mavenUser      | This is the name of the artifactory user to authenticate with
mavenPassword  | This is the password of the artifactory user to authenticate with
mavenUrl       | This is the artifactory url to publish to. The default location is [maven-integration-local][maven-integration-local], which is the snapshots repository. The release location should be [maven-release-candidate-local][maven-release-candidate-local]

# Publishing & Teamcity
All the builds are located in [teamcity][teamcity].
There are 4 different builds:

### Pull Request Builder
Build Property | Details
-------------- | -------
Link           | [Pull Request Builder][pull-request-builder]
Description    | Builds all the pull requests
Gradle Command | `./gradlew clean build`
Trigger        | Triggered automatically whenever there is an update to a pull request. 
Publishes      | Does not publish 
Config         | Branch Filter: `+:pull/*` <br/> Branch Spec: `+:refs/(pull/*)/merge`

### Branch Builder
Build Property | Details
-------------- | -------
Link           | [Branch Builder][branch-builder]
Description    | Builds different branches
Gradle Command | `./gradlew clean build`
Trigger        | This build must be manually triggered.
Publishes      | Does not publish 
Config         | Branch Spec: `+:refs/heads/(*) -:refs/heads/master`

### Snapshot Builder
Build Property | Details
-------------- | -------
Link           | [Snapshot Builder][snapshot-builder]
Description    | Builds snapshots from the master branch.
Gradle Command | `./gradlew clean build publish`
Trigger        | Triggered automatically whenever there is an update to the master branch
Publishes      | Published to [maven-integration-local][maven-integration-local-ModularAssertionBuilder]
Config         | Branch Filter: `+:master` <br/> Branch Spec: `+:refs/heads/(master)`

### Release Builder
Build Property | Details
-------------- | -------
Link           | [Release Builder][release-builder]
Description    | Builds release from release tags. See more details [below](#publishing-releases)
Gradle Command | `./gradlew clean build publish`
Trigger        | Triggered automatically whenever there is an update to a release tag.
Publishes      | Published to [maven-release-candidate-local][maven-release-candidate-local-ModularAssertionBuilder]
Config         | Branch Filter: `-:master +:*` <br/> Branch Spec: `+:refs/tags/release/(*)`


<a name="publishing-releases"></a>
## Publishing Releases
In Git tags are used to track releases. Because of this the builds are also setup to use tags to publish release artifacts. In order to trigger the build and publishing of a release create a release tag. Release tags have the following format: `release/<version>`. This will trigger a release build with the version specified in the tag.

# Versioning
The version of the Modular Assertion Builder is stored in the [gradle.properties](gradle.properties) file. This is the process that should be followed when incrementing the version:
1) Commit all features and items intended for the current version into master
2) Tag master with a release tag. For example: `release/2.0.00`
   1) This will trigger a [release build][release-builder] and publish the release to [artifactory][maven-release-candidate-local-ModularAssertionBuilder] 
3) Update the version in the [gradle.properties](gradle.properties) file. Note, this version should always have the format: `X.X.XX-SNAPSHOT`


[teamcity]: https://apim-teamcity.l7tech.com:8443/project.html?projectId=ApiGateway_Utilities_ModularAssertionBuilder
[pull-request-builder]: https://apim-teamcity.l7tech.com:8443/viewType.html?buildTypeId=ApiGateway_Utilities_ModularAssertionBuilder_PullRequestBuilder
[branch-builder]: https://apim-teamcity.l7tech.com:8443/viewType.html?buildTypeId=ApiGateway_Utilities_ModularAssertionBuilder_BranchBuilder
[snapshot-builder]: https://apim-teamcity.l7tech.com:8443/viewType.html?buildTypeId=ApiGateway_Utilities_ModularAssertionBuilder
[release-builder]: https://apim-teamcity.l7tech.com:8443/viewType.html?buildTypeId=ApiGateway_Utilities_ModularAssertionBuilder_Rel
[maven-integration-local]: http://isl-dsdc.ca.com/artifactory/maven-integration-local
[maven-release-candidate-local]: http://isl-dsdc.ca.com/artifactory/maven-release-candidate-local
[maven-integration-local-ModularAssertionBuilder]: http://isl-dsdc.ca.com/artifactory/webapp/#/artifacts/browse/tree/General/maven-integration-local/com/ca/apim/gateway/ModularAssertionBuilder
[maven-release-candidate-local-ModularAssertionBuilder]: http://isl-dsdc.ca.com/artifactory/webapp/#/artifacts/browse/tree/General/maven-release-candidate-local/com/ca/apim/gateway/ModularAssertionBuilder

