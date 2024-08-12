/*
 * Copyright © 2017-2024. Broadcom Inc. and its subsidiaries. All Rights Reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.modularassertionbuilder

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class ModularAssertionBuilderTest {

    @Test
    void demo_plugin_should_add_task_to_project() {
        Project project = ProjectBuilder.builder().build()
        project.getPlugins().apply 'com.ca.apim.gateway.modular-assertion-builder'
    }
}