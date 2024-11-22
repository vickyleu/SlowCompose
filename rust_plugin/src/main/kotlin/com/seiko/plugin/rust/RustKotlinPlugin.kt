package com.seiko.plugin.rust

import com.android.build.gradle.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

fun String.capitalizedCopy(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
@Suppress("Unused")
class RustKotlinPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val proDir = target.projectDir.absolutePath
        val tasks = target.project.tasks
        with(target) {
            val cargoExtension = extensions.create("cargo", CargoExtension::class.java)
            afterEvaluate {
                if (!project.pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                    throw GradleException("Completing a library with a multiplatform library requires Kotlin Multiplatform")
                }
                val kmpExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)
                configurePlugin(
                    kmpExtension = kmpExtension,
                    cargoExtension = cargoExtension,
                    proDir=proDir,
                    tasks=tasks
                )
            }
        }
    }

    private fun Project.configurePlugin(
        kmpExtension: KotlinMultiplatformExtension,
        cargoExtension: CargoExtension,
        proDir: String,
        tasks: TaskContainer,
    ) {
        check(cargoExtension.module.isNotEmpty()) { "module cannot be empty" }
        check(cargoExtension.libName.isNotEmpty()) { "libName cannot be empty" }

        val toolchains = mutableSetOf<Pair<Toolchain, KotlinTarget>>()
        kmpExtension.targets.forEach { target: KotlinTarget ->
            val name = target.targetName.capitalizedCopy()
            when (target.targetName) {
                "android" -> {
                    val androidExtension = extensions.getByType(LibraryExtension::class.java)
                    val abiFilters = androidExtension.defaultConfig.ndk.abiFilters
                    if (abiFilters.isEmpty()) {
                        // if not config, support all android targets
                        abiFilters.addAll(listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a"))
                    }
                    toolchains.add(
                        Toolchain.Android(
                            name = name,
                            targets = abiFilters.mapTo(mutableSetOf()) { abi ->
                                when (abi) {
                                    "x86" -> "i686-linux-android" to target.targetName
                                    "x86_64" -> "x86_64-linux-android" to target.targetName
                                    "armeabi-v7a" -> "armv7-linux-androideabi" to target.targetName
                                    "arm64-v8a" -> "aarch64-linux-android" to target.targetName
                                    else -> "" to target.targetName
                                }
                            },
                            abiFilters = abiFilters,
                        ) to target
                    )
                }

                "jvm", "desktop" -> {
                    toolchains.add(
                        Toolchain.Jvm(
                            name = name,
                            targets = setOf(
                                getCurrentOsTargetTriple(cargoExtension) to target.targetName,
                            ),
                        ) to target
                    )
                }

                "macosX64", "macosArm64" -> {
                    toolchains.add(
                        Toolchain.Darwin(
                            name = name,
                            targets = setOf(
                                when (target.targetName) {
                                    "macosX64" -> "x86_64-apple-darwin" to target.targetName
                                    "macosArm64" -> "aarch64-apple-darwin" to target.targetName
                                    else -> "" to target.targetName
                                }
                            ),
                        ) to target
                    )
                }

                "iosX64", "iosArm64", "iosSimulatorArm64" -> {
                    toolchains.add(
                        Toolchain.IOS(
                            name = name,
                            targets = setOf(
                                when (target.targetName) {
                                    "iosX64" -> "x86_64-apple-ios" to target.targetName
                                    "iosArm64" -> "aarch64-apple-ios" to target.targetName
                                    "iosSimulatorArm64" -> "aarch64-apple-ios-sim" to target.targetName
                                    else -> "" to target.targetName
                                }
                            ),
                        ) to target
                    )
                }

                else -> {
                }
            }
        }

        toolchains.forEach { toolchain ->
            dependTask(toolchain,proDir,cargoExtension,tasks=tasks)
        }
    }

    private fun Project.dependTask(
        toolchain: Pair<Toolchain, KotlinTarget>,
        proDir: String,
        cargoExtension: CargoExtension,
        tasks: TaskContainer
    ) {
        val targetBuildTask = tasks.maybeCreate(
            "cargoBuild${toolchain.first.name}",
            CargoBuildTask::class.java,
        ).also {
            it.extension = cargoExtension
            it.projectDir = proDir
            it.group = RUST_TASK_GROUP
            it.description = "Build library (${toolchain.first.name})"
            it.toolchain = toolchain.first
        }

        when (toolchain.first) {
            is Toolchain.Android -> {
                try {
                    val javaPreCompileDebug by tasks.getting
                    javaPreCompileDebug.dependsOn(targetBuildTask)
                    val javaPreCompileRelease by tasks.getting
                    javaPreCompileRelease.dependsOn(targetBuildTask)
                } catch (e: Exception) {
                    logger.warn("Android plugin not found ${e.message}")
                }
            }

            is Toolchain.Jvm, is Toolchain.Darwin, is Toolchain.IOS -> {
                try {
                    if (toolchain.second is KotlinNativeTarget) {
                        val cinterops =
                            (toolchain.second as KotlinNativeTarget).compilations.getByName("main").cinterops
                        val interopList = cinterops.mapNotNull {
                            val interopProcessingTaskName = it.interopProcessingTaskName
                            if (interopProcessingTaskName.contains(toolchain.first.targets.first().second.capitalizedCopy())) {
                                interopProcessingTaskName
                            } else null
                        }
                        interopList.forEach {
                            val task = tasks.getByPath(it)
                            task.dependsOn(targetBuildTask)
                            task.setMustRunAfter(listOf(targetBuildTask))
                        }
                    }
                } catch (e: Exception) {
                    val task = tasks.getByName("compileKotlin${toolchain.first.name.capitalizedCopy()}")
                    task.dependsOn(targetBuildTask)
                    task.setMustRunAfter(listOf(targetBuildTask))
                }

            }
        }
    }

    companion object {
        const val RUST_TASK_GROUP = "rust"
    }
}
