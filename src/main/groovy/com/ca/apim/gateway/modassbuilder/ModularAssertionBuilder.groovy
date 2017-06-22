package com.ca.apim.gateway.modassbuilder

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree

class ModularAssertionBuilder implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'java'

        project.sourceCompatibility = 1.8

        project.extensions.create("ModularAssertionBuilder", ExtensionBuilderExtension)

        project.repositories {
            //mavenCentral()
            //jcenter()
            maven {
                url "http://artifactory-van.ca.com/artifactory/maven-repo"
            }
        }

        project.configurations {
            releaseJars {
                transitive = false
            }
            compile {
                extendsFrom releaseJars
            }
            antTask
        }

        project.dependencies {
            //Below are required modular assertion dependencies
            compile(
                    "com.l7tech:layer7-utility:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-common:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-policy:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-policy-exporter:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-gui:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-gateway-common:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-gateway-server:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-proxy:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-gateway-console:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-kerberos:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-identity:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.l7tech:layer7-gui:$project.ModularAssertionBuilder.gatewayBaseVersion",
                    "com.intellij:annotations:13.1.6",
                    "com.intellij:forms_rt:11.1.2",
                    "javax.inject:javax.inject:1"
            )

            testCompile(
                    "junit:junit:4.12",
                    "org.mockito:mockito-core:1.10.19",
                    "com.l7tech:layer7-test:$project.ModularAssertionBuilder.gatewayBaseVersion"
            )

            antTask(
                    "com.intellij:javac2:1.8.0_112-release-287-b2",
                    "com.intellij:forms_rt:11.1.2",
                    "org.objectweb:asm-all:1.8.0_72-b15",
                    "org.jdom:jdom:1.1"
            )
        }

        project.task('compileJava', overwrite: true, dependsOn: project.configurations.compile.getTaskDependencyFromProjectDependency(true, 'jar')) {
            doLast {
                project.sourceSets.main.output.classesDir.mkdirs()
                project.ant.taskdef name: 'javac2', classname: 'com.intellij.ant.Javac2', classpath: project.configurations.antTask.asPath
                project.ant.javac2 srcdir: project.sourceSets.main.java.srcDirs.join(':'),
                        classpath: project.sourceSets.main.compileClasspath.asPath,
                        destdir: project.sourceSets.main.output.classesDir,
                        source: project.sourceCompatibility,
                        target: project.targetCompatibility,
                        includeAntRuntime: false
            }
        }


        project.task('configureAAR') {
            doLast {
                FileTree tree = project.fileTree(
                        include: '**/*Assertion.java',
                        exclude: ['**/console/**/*', '**/client/**/*', '**/server/**/*']
                )
                project.sourceSets.main.java.getSrcDirs().each { tree.dir = it }
                def assertionClasses = ''
                tree.each { File file ->
                    assertionClasses = assertionClasses + ' ' + file.path.replaceAll('.*/src/main/java/', '').replaceAll('\\.java', '').replaceAll('/', '.')
                }
                if (assertionClasses.length() > 0) {
                    assertionClasses = assertionClasses.substring(1)
                }
                project.jar.manifest {
                    attributes('ModularAssertion-List': assertionClasses)
                }

                tree = project.fileTree(
                        dir: "${project.sourceSets.main.output.classesDir}",
                        exclude: ['**/console/**/*', '**/client/**/*', '**/server/**/*']
                )
                assertionClasses = ''
                tree.each { File file ->
                    assertionClasses = assertionClasses + '\n' + file.path.replaceAll('.*/build/classes/main/', '')
                }
                if (assertionClasses.length() > 0) {
                    assertionClasses = assertionClasses.substring(1)
                }

                def assertionsIndexFile = new File("${project.jar.temporaryDir}/assertion.index")
                assertionsIndexFile.text = "$assertionClasses"
                project.jar.into('AAR-INF') {
                    from assertionsIndexFile
                }

                tree = project.fileTree(
                        dir: "${project.sourceSets.main.output.classesDir}",
                        include: ['**/console/**/*']
                ) + project.fileTree(
                        dir: "${project.sourceSets.main.output.resourcesDir}",
                        include: ['**/console/**/*']
                )


                assertionClasses = ''
                tree.each { File file ->
                    assertionClasses = assertionClasses + '\n' + file.path.replaceAll('.*/build/(classes|resources)/main/', '')
                }
                if (assertionClasses.length() > 0) {
                    assertionClasses = assertionClasses.substring(1)
                }

                def consoleIndexFile = new File("${project.jar.temporaryDir}/console.index")
                consoleIndexFile.text = "$assertionClasses"
                project.jar.into('AAR-INF') {
                    from consoleIndexFile
                }
            }
        }

        project.jar {
            dependsOn project.configureAAR
            version = "$project.version"
            manifest {
                attributes(
                        "Specification-Title": "ModularAssertion",
                        "Specification-Version": "3.7.0",
                        "Specification-Vendor": "Layer 7 Technologies",
                        "Specification-endor-Id": "com.l7tech",
                        "Implementation-Title": "$project.ModularAssertionBuilder.assertionName",
                        "Implementation-Version": "$project.version",
                        "Implementation-Vendor": "Layer 7 Technologies",
                        "Implementation-Vendor-Id": "com.l7tech",
                        "ModularAssertion-Private-Libraries": ""
                )
            }
            extension = "aar"
            into('AAR-INF/lib') {
                from project.configurations.releaseJars
            }
        }
    }
}

class ExtensionBuilderExtension {
    String assertionName
    String gatewayBaseVersion = '9.2.00'
}