@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        compilerOptions {
            jvmTarget.value(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    macosX64()
    linuxX64()
    mingwX64()
    jvm {
//        withJava()
        compilerOptions {
            jvmTarget.value(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }
    }
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/commonMain/kotlin")
        }
    }
}


kotlin {
    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xexpect-actual-classes", // remove warnings for expect classes
            "-Xskip-prerelease-check",
            "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
            "-opt-in=org.jetbrains.compose.resources.InternalResourceApi",
        )
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
    }
}

android{
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "org.uooc.annotation"
}