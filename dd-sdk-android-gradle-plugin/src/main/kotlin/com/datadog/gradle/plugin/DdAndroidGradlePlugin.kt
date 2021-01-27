/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.gradle.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import java.io.File
import java.lang.IllegalStateException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.LoggerFactory

/**
 * Plugin adding tasks for Android projects using Datadog's SDK for Android.
 */
class DdAndroidGradlePlugin : Plugin<Project> {

    // region Plugin

    /** @inheritdoc */
    override fun apply(target: Project) {
        val androidExtension = target.extensions.findByType(AppExtension::class.java)
        if (androidExtension == null) {
            System.err.println(ERROR_NOT_ANDROID)
            return
        }

        val extension = target.extensions.create(EXT_NAME, DdExtension::class.java)
        val apiKey = resolveApiKey(target)

        target.afterEvaluate {
            androidExtension.applicationVariants.forEach {
                configureVariant(target, it, apiKey, extension)
            }
        }
    }

    // endregion

    // region Internal

    internal fun resolveApiKey(target: Project): String {
        val propertyKey = target.findProperty(DD_API_KEY)?.toString()
        if (!propertyKey.isNullOrBlank()) return propertyKey

        val environmentKey = System.getenv(DD_API_KEY)
        if (!environmentKey.isNullOrBlank()) return environmentKey

        throw IllegalStateException(
            "Make sure you define an API KEY to upload your mapping files to Datadog. " +
                    "Create a DD_API_KEY environment variable or gradle property."
        )
    }

    @Suppress("DefaultLocale")
    internal fun configureVariant(
        target: Project,
        variant: ApplicationVariant,
        apiKey: String,
        extension: DdExtension
    ): Task {
        val flavorName = if (variant.name.endsWith(SUFFIX_DEBUG)) {
            variant.name.removeSuffix(SUFFIX_DEBUG)
        } else if (variant.name.endsWith(SUFFIX_RELEASE)) {
            variant.name.removeSuffix(SUFFIX_RELEASE)
        } else {
            variant.name
        }
        val uploadTaskName = UPLOAD_TASK_NAME + variant.name.capitalize()

        val uploadTask = target.tasks.create(
            uploadTaskName,
            DdMappingFileUploadTask::class.java
        )
        uploadTask.apiKey = apiKey
        uploadTask.site = extension.site
        uploadTask.envName = extension.environmentName
        uploadTask.variantName = flavorName
        uploadTask.versionName = extension.versionName ?: variant.versionName
        uploadTask.serviceName = extension.serviceName ?: variant.applicationId

        val outputsDir = File(target.buildDir, "outputs")
        val mappingDir = File(outputsDir, "mapping")
        val flavorDir = File(mappingDir, variant.name)
        uploadTask.mappingFilePath = File(flavorDir, "mapping.txt").path

        return uploadTask
    }

    // endregion

    companion object {

        const val DD_API_KEY = "DD_API_KEY"

        internal val LOGGER = LoggerFactory.getLogger("DdAndroidGradlePlugin")

        private const val SUFFIX_DEBUG = "Debug"
        private const val SUFFIX_RELEASE = "Release"

        private const val EXT_NAME = "datadog"

        private const val UPLOAD_TASK_NAME = "uploadMapping"

        private const val ERROR_NOT_ANDROID = "The dd-android-gradle-plugin has been applied on " +
            "a non android application project"
    }
}
