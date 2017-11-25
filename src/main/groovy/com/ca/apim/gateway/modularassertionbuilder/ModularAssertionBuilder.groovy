package com.ca.apim.gateway.modularassertionbuilder

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.file.FileTree

class ModularAssertionBuilder implements Plugin<Project> {
    def compileDeps
    def testCompileDeps
    Project project

    void apply(Project project) {
        this.project = project
        project.apply plugin: 'java'

        project.sourceCompatibility = 1.8

        // Creates an extension for holding configuration for the plugin
        ModularAssertionExtension modularAssertionBuilder = project.extensions.create("modularAssertionBuilder", ModularAssertionExtension)

        project.configurations {
            releaseJars {
                //makes it so that release jars do not bring in their dependencies, they must all be explicitly specified
                transitive = false
            }
            compile {
                extendsFrom releaseJars
            }
            antTask
        }
        project.configurations.all {
            resolutionStrategy.eachDependency {
                DependencyResolveDetails details ->
                    if ("com.l7tech" == details.requested.group && !(details.requested.name in ["layer7-api", "layer7-gateway-api"])) {
                        project.logger.info "Overriding dependency ${details.requested.group}:${details.requested.name} version ${details.requested.version} --> $modularAssertionBuilder.gatewayBaseVersion"
                        details.useVersion "$modularAssertionBuilder.gatewayBaseVersion"
                    }
            }
            // check for dependency updates every build
            resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
        }

        compileDeps = project.getConfigurations().getByName("compile").getDependencies()
        testCompileDeps = project.getConfigurations().getByName("testCompile").getDependencies()
        // Need to add a dependency resolution listener in order to add the gateway dependencies because the modularAssertionBuilder is not available till later
        project.getGradle().addListener(new DependencyResolutionListener() {
            @Override
            void beforeResolve(ResolvableDependencies resolvableDependencies) {
                addDeps(modularAssertionBuilder)
                addJar(modularAssertionBuilder)
                project.getGradle().removeListener(this)
            }

            @Override
            void afterResolve(ResolvableDependencies resolvableDependencies) {}
        })


        project.dependencies {
            //Below are required modular assertion dependencies
            compile(
                    "com.intellij:annotations:12.0",
                    "com.intellij:forms_rt:7.0.3",
                    "javax.inject:javax.inject:1"
            )

            antTask(
                    "com.intellij:javac2:7.0.3",
                    "com.intellij:forms_rt:7.0.3",
                    "asm:asm:2.2.3",
                    "jdom:jdom:1.1.3"
            )
        }

        // This is used to compile the intellij forms
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

        //builds the aar configuration
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
        project.jar.dependsOn project.configureAAR
    }

    def addDeps(modularAssertionBuilder) {
        compileDeps.addAll(
                project.getDependencies().create("com.l7tech:layer7-utility:$modularAssertionBuilder.gatewayBaseVersion"),
                project.getDependencies().create("com.l7tech:layer7-common:$modularAssertionBuilder.gatewayBaseVersion"),
                project.getDependencies().create("com.l7tech:layer7-policy:$modularAssertionBuilder.gatewayBaseVersion"),
                project.getDependencies().create("com.l7tech:layer7-gateway-common:$modularAssertionBuilder.gatewayBaseVersion"),
                project.getDependencies().create("com.l7tech:layer7-gateway-server:$modularAssertionBuilder.gatewayBaseVersion"),
        )

        testCompileDeps.addAll( 
//                project.getDependencies().create("com.l7tech:layer7-test:$modularAssertionBuilder.gatewayBaseVersion")
        )
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

