package com.seiko.plugin.rust

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class CargoBuildTask : DefaultTask() {
    @Input
    lateinit var extension: CargoExtension


    private lateinit var projectDirFile: File
    @Input
    lateinit var projectDir: String


    @get:Inject
    abstract val fs: FileSystemOperations

    @Input
    lateinit var toolchain: Toolchain

    @TaskAction
    fun build() {
        if (::extension.isInitialized.not()) {
            println("CargoExtension not found")
            return // unreachable
        }
        projectDirFile = File(projectDir)
        val cargoExtension = extension
        execRustcTarget(toolchain, cargoExtension)
        when (val toolchain = toolchain) {
            is Toolchain.Android -> execRustAndroid(toolchain, cargoExtension)
            is Toolchain.Jvm -> execRustJvm(toolchain, cargoExtension)
            is Toolchain.Darwin -> execRustDarwin(toolchain, cargoExtension)
            is Toolchain.IOS -> execRustIOS(toolchain, cargoExtension)
        }
    }

    private fun execRustAndroid(
        toolchain: Toolchain.Android,
        cargoExtension: CargoExtension,
    ) {
        val moduleDir = getModuleDir(toolchain, cargoExtension)

        ProcessBuilder().command(
            *(buildList {
                add(cargoExtension.cargoHome + cargoExtension.cargoCommand)
                if (!cargoExtension.isVerbose) {
                    add("--quiet")
                }
                add(cargoExtension.ndkCommand)
                toolchain.abiFilters.forEach { abi ->
                    add("-t")
                    add(abi)
                }
                add("-o")
                add(getDir(cargoExtension.androidJniDir).absolutePath)
                add("build")
                addCommonArgs(cargoExtension)
            }.toTypedArray())
        ).directory(moduleDir).start().waitFor()
    }

    private fun execRustJvm(
        toolchain: Toolchain.Jvm,
        cargoExtension: CargoExtension,
    ) {
        val target = toolchain.targets.first()
        execRustBuild(
            toolchain = toolchain,
            cargoExtension = cargoExtension,
            target = target,
        )
        execMoveLib(
            toolchain = toolchain,
            cargoExtension = cargoExtension,
            target = target,
            intoDir = File(getDir(cargoExtension.jvmJniDir), target.first.split('-').first()),
        )
    }

    private fun execRustDarwin(
        toolchain: Toolchain.Darwin,
        cargoExtension: CargoExtension,
    ) {
        val target = toolchain.targets.first()
        execRustBuild(
            toolchain = toolchain,
            cargoExtension = cargoExtension,
            target = target,
        )
        execMoveLib(
            toolchain = toolchain,
            cargoExtension = cargoExtension,
            target = target,
            intoDir = File(
                getDir(cargoExtension.cinteropDir),
                "${cargoExtension.libName}/${target.second}"
            ),
        )
    }

    private fun execRustIOS(
        toolchain: Toolchain.IOS,
        cargoExtension: CargoExtension,
    ) {
        val target = toolchain.targets.first()
        execRustBuild(
            toolchain = toolchain,
            cargoExtension = cargoExtension,
            target = target,
        )
        execMoveLib(
            toolchain = toolchain,
            cargoExtension = cargoExtension,
            target = target,
            intoDir = File(
                getDir(cargoExtension.cinteropDir),
                "${cargoExtension.libName}/${target.second}"
            ),
        )
    }

    private fun execMoveLib(
        toolchain: Toolchain,
        cargoExtension: CargoExtension,
        target: Pair<String, String>,
        intoDir: File,
    ) {
        val moduleDir = getModuleDir(toolchain, cargoExtension)
        val fromDir = File(moduleDir, "target/${target.first}/${cargoExtension.profile}")
        fs.delete {
            delete(intoDir)
        }
        fs.copy {
            from(fromDir)
            into(intoDir)
            val libName = cargoExtension.libName
            include("lib$libName.a")
            include("$libName.lib")
            include("lib$libName.so")
            include("lib$libName.dylib")
            include("$libName.dll")
        }
    }

    private fun execRustcTarget(
        toolchain: Toolchain,
        cargoExtension: CargoExtension,
    ) {
        ProcessBuilder().command(
            *(buildList {
                add(cargoExtension.cargoHome + cargoExtension.rustUpCommand)
                if (!cargoExtension.isVerbose) {
                    add("--quiet")
                }
                add("target")
                add("add")
                addAll(toolchain.targets.map { it.first })
            }.toTypedArray())
        ).start().waitFor()
    }

    private fun execRustBuild(
        toolchain: Toolchain,
        cargoExtension: CargoExtension,
        target: Pair<String, String>,
    ) {
        ProcessBuilder()
            .directory(getModuleDir(toolchain, cargoExtension))
            .command(
                *(buildList {
                    add(cargoExtension.cargoHome + cargoExtension.cargoCommand)
                    if (!cargoExtension.isVerbose) {
                        add("--quiet")
                    }
                    add("build")
                    add("--target")
                    add(target.first)
                    addCommonArgs(cargoExtension)
                }.toTypedArray())
            ).start().waitFor()
    }

    private fun getModuleDir(toolchain: Toolchain, cargoExtension: CargoExtension): File {
        val libName = cargoExtension.libName + when (toolchain) {
            is Toolchain.Android, is Toolchain.Jvm -> cargoExtension.jniSuffix
            is Toolchain.IOS, is Toolchain.Darwin -> cargoExtension.nativeSuffix
        }
        return File(getDir(cargoExtension.module), libName)
    }

    private fun getDir(path: String): File {
        val file = File(path)
        return if (file.isAbsolute) file else {
            File(projectDirFile, file.path)
        }.canonicalFile
    }

    private fun <T> buildList(block: MutableList<T>.() -> Unit) = mutableListOf<T>().apply(block)

    private fun MutableList<String>.addCommonArgs(cargoExtension: CargoExtension) {
        if (cargoExtension.isVerbose) {
            add("--verbose")
        }
        if (cargoExtension.profile != "debug") {
            add("--${cargoExtension.profile}")
        }
    }
}
