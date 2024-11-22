@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
    }
    jvm {
        withJava()
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.annotation)
                implementation(libs.kotlin.stdlib)
                implementation(project.dependencies.platform(libs.coroutines.bom))
                implementation(libs.javapoet)
                implementation(libs.symbol.processing.api)
                implementation(libs.coroutines.jvm)
            }
        }

    }
}
