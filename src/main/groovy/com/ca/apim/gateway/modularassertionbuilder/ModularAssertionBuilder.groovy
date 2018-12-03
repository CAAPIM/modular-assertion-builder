/*
 * Copyright (c) 2017 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.modularassertionbuilder

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.file.FileTree

class ModularAssertionBuilder implements Plugin<Project> {
    Project project

    void apply(Project project) {
        this.project = project
        project.apply plugin: 'java'

        project.sourceCompatibility = 1.8

        // Creates an extension for holding configuration for the plugin
        ModularAssertionExtension modularAssertionBuilder = project.extensions.create("modularAssertionBuilder", ModularAssertionExtension)

        project.configurations {
            releaseJars {
                //makes it so that release jars do bring in their dependencies
                transitive = true
            }
            compile {
                extendsFrom releaseJars
            }
        }

        project.getGradle().addListener(new DependencyResolutionListener() {
            @Override
            void beforeResolve(ResolvableDependencies resolvableDependencies) {
                addJar(modularAssertionBuilder)
                project.getGradle().removeListener(this)
            }

            @Override
            void afterResolve(ResolvableDependencies resolvableDependencies) {}
        })

        //builds the aar configuration
        project.task('configureAAR') {
            description = 'Packages the .aar file'
            group = 'build'
            
            doLast {
                FileTree tree = project.fileTree(
                        include: '**/*Assertion.java',
                        exclude: ['**/console/**/*', '**/client/**/*', '**/server/**/*']
                )
                project.sourceSets.main.java.getSrcDirs().each { tree.dir = it }
                def assertionClasses = ''
                tree.each { File file ->
                    assertionClasses = assertionClasses + ' ' + file.path.replaceAll('.*/src/main/java/', '').replaceAll('\\.java', '').replaceAll('/', '.').replaceAll('.*\\\\src\\\\main\\\\java\\\\','').replaceAll('\\\\','.')
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
                    def path = file.path.replaceAll('.*/build/classes/java/main/', '')
                            .replace(project.sourceSets.main.output.classesDir.toString(),'')
                            .replace("\\", "/")     // for win machines
                    if (path.startsWith("/")) {
                        // removing leading slash
                        path.substring(1)
                    }
                    assertionClasses = assertionClasses + '\n' + path
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
                    def path = file.path.replaceAll('.*/build/(classes/java|resources)/main/', '')
                            .replace(project.sourceSets.main.output.classesDir.toString(),'')
                            .replace("\\", "/") // for win machines
                    if (path.startsWith("/")) {
                        // removing leading slash
                        path.substring(1)
                    }
                    assertionClasses = assertionClasses + '\n' + path
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
        project.jar.dependsOn project.configureAAR
    }

    def addJar(modularAssertionBuilder) {
        // creates the aar
        project.jar {
            version = "$project.version"
            baseName = "$modularAssertionBuilder.assertionName"
            manifest {
                attributes(
                        "Specification-Title": "ModularAssertion",
                        "Specification-Version": "3.7.0",
                        "Specification-Vendor": "Layer 7 Technologies",
                        "Specification-endor-Id": "com.l7tech",
                        "Implementation-Title": "$modularAssertionBuilder.assertionName",
                        "Implementation-Version": "$project.version",
                        "Implementation-Vendor": "Layer 7 Technologies",
                        "Implementation-Vendor-Id": "com.l7tech",
                        "ModularAssertion-Private-Libraries": "",
                        "Revision": "$modularAssertionBuilder.revision",
                        "GatewayBaseVersion": "$modularAssertionBuilder.gatewayBaseVersion"
                )
            }
            extension = "aar"
            into('AAR-INF/lib') {
                from project.configurations.releaseJars
            }
        }
    }
}

