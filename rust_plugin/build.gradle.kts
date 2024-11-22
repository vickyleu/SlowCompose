@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

//需要判断是否是jitpack的构建，如果是jitpack的构建，需要将build目录设置到项目根目录下
if (System.getenv("JITPACK") == null) {
    val realRootProject = rootProject.rootDir.parentFile
    val buildDir = file(
        "${
            rootProject.rootDir
                .parentFile.parentFile.parentFile
                .absolutePath
        }/buildOut/${realRootProject.name}/buildCollection/${project.name}"
    )
    rootProject.layout.buildDirectory.set(buildDir)
}
plugins {
    `kotlin-dsl` version "5.1.0"
    alias(libs.plugins.kotlin.jvm)
}


java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
}

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }
    }
    jvmToolchain(libs.versions.jvmTarget.get().toInt())
    sourceSets {
        named("main") {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
                implementation(kotlin("gradle-plugin"))
            }
        }
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(libs.versions.kotlin.get())
            } else if (requested.group == "org.jetbrains.kotlinx"
                && requested.name.lowercase().contains("coroutines")
            ) {
                useVersion(libs.versions.coroutines.bom.get())
            }
        }
    }
}

gradlePlugin {
    plugins {
        register("HelloRustPlugin") {
            id = "com.seiko.plugin.rust"
            version = "1.0.0"
            implementationClass = "com.seiko.plugin.rust.RustKotlinPlugin"
        }
    }
}




