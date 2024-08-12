/*
 * Copyright © 2017-2024. Broadcom Inc. and its subsidiaries. All Rights Reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.modularassertionbuilder

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.file.DuplicatesStrategy
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
            implementation {
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

            def getAssertionClasses = { sources, Map<String, ?> args ->
                sources.collectMany { File dir ->
                    FileTree tree = project.fileTree(dir: dir, *: args)
                    tree.collect { File file ->
                        def path = file.path
                                .replace(dir.toString(), '') // removing base directory
                                .replace("\\", "/") // for win machines
                        if (path.startsWith("/")) {
                            // removing leading slash
                            return path.substring(1)
                        }
                        path
                    }
                }
            }

            doLast {
                def assertionClasses = getAssertionClasses(
                    project.sourceSets.main.java.getSrcDirs(),
                    [
                        include: '**/*Assertion.java',
                        exclude: ['**/console/**/*', '**/client/**/*', '**/server/**/*'],
                    ],
                ).collect { it.replaceAll('\\.java', '').replaceAll('/', '.') }.join(' ')
                project.jar.manifest {
                    attributes('ModularAssertion-List': assertionClasses)
                }

                assertionClasses = getAssertionClasses(
                        project.sourceSets.main.output.classesDirs + project.sourceSets.main.output.resourcesDir,
                        [
                            exclude: ['**/console/**/*', '**/client/**/*', '**/server/**/*'],
                        ],
                ).join('\n')

                def assertionsIndexFile = new File("${project.jar.temporaryDir}/assertion.index")
                assertionsIndexFile.text = "$assertionClasses"
                project.jar.into('AAR-INF') {
                    from assertionsIndexFile
                }

                assertionClasses = getAssertionClasses(
                    project.sourceSets.main.output.classesDirs + project.sourceSets.main.output.resourcesDir,
                    [
                        include: ['**/console/**/*'],
                    ],
                ).join('\n')

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
            archiveVersion = "$project.version"
            archiveBaseName = "$modularAssertionBuilder.assertionName"
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
            archiveExtension = "aar"
            setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE)
            into('AAR-INF/lib') {
                from project.configurations.releaseJars
            }
        }
    }
}
