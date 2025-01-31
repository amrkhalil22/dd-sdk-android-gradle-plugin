/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2020-Present Datadog, Inc.
 */

package com.datadog.gradle.config

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

@Suppress("UnstableApiUsage")
fun Project.junitConfig() {
    tasks.withType(Test::class.java) {
        jvmArgs(
            "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
            "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED"
        )
        useJUnitPlatform {
            includeEngines("spek", "junit-jupiter", "junit-vintage")
        }
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
    }
}
