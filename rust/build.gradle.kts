@file:Suppress("OPT_IN_USAGE")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
//    id(libs.plugins.rust.get().pluginId)
}
tasks
    .withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>()
    .configureEach {
        compilerOptions
            .jvmTarget
            .set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
    }

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
    }
    /*wasmJs { // TODO wasm 不支持cinterop,会中断编译
        moduleName = "rust"
        browser()
//        nodejs()
    }*/
    androidTarget()
    jvm("desktop")
    applyDefaultHierarchyTemplate()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()


    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
            }
        }
        commonMain {
            dependencies {
            }
        }

        val engineMain by creating {
            dependsOn(commonMain.get())
        }
        val jniMain by creating {
            dependsOn(engineMain)
        }
        androidMain {
            dependsOn(jniMain)
            dependencies {
            }
        }
        val desktopMain by getting {
            dependsOn(jniMain)
            dependencies {
            }
        }

        nativeMain {
             // cinterop生成的文件无法找到，需要手动添加
            // 通过 this.kotlin.srcDir() 添加到源码目录
           /* val kmm = file("${project.layout.buildDirectory.get().asFile.absolutePath}/classes/kotlin/commonizer/hellorust/DjLwbDknjBV86P1x4SfsZNYSr2Q=/(ios_arm64, ios_simulator_arm64, ios_x64, macos_arm64, macos_x64)/KMMCompose_rust-cinterop-hellorust/")
            this.kotlin.srcDir(kmm)*/
            dependsOn(engineMain)
        }

        iosMain {
            dependencies {
            }
        }
//        wasmJsMain {
//            dependsOn(nativeMain.get())
//        }
        macosMain {
        }
        targets.withType<KotlinNativeTarget> {
            val targetName = this.name
            val main by compilations.getting {
                cinterops {
                    val hellorust by creating {
                        defFile(file("src/nativeInterop/cinterop/hellorust.def"))
                        header(file("rs/hellorust-native/hellorust.h"))
                        extraOpts(
                            "-libraryPath",
                            file("src/nativeInterop/cinterop/hellorust/$targetName/").absolutePath
                        )
                    }
                }
            }
        }
    }
}
//cargo {
//    module = "./rs"
//    libName = "hellorust"
//    profile = "release"
//    isVerbose = false
//    cargoHome = "/Users/vickyleu/.cargo/bin/"
//    jvmJniDir = "./src/desktopMain/resources/jni"
//}



android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "com.seiko.rust"
    ndkVersion = "23.0.7599858"
    defaultConfig {
        minSdk = 24
        ndk {
            abiFilters += listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        }
    }
    lint {
        targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
}

//rust {
//    // Setting the global "default" vars
//    release.set(true)
//
//    // you can also use tools like "cross"
//    command.set("cargo")
//
//    // this will make cargo install automatically the required targets
//    // `rustup targets add x86_64-pc-windows-gnu`
//    cargoInstallTargets.set(true)
//
//    // Adding a simple target with default options
//    targets += target("i686-unknown-linux-gnu", "libtest.so")
//
//    // Adding a target with modfified options
//    targets += target("i686-pc-windows-gnu", "test.dll").apply {
//        release = false
//    }
//
//    // Defining per-targets
//    targets {
//        // Adds the default target
//        this += defaultTarget()
//
//        // Creates a named target with a custom file output
//        create("win64") {
//            target = "x86_64-pc-windows-gnu"
//            outputName = "test64.dll"
//        }
//
//        // Custom target with different params than default
//        create("macOS-x86") {
//            target = "x86_64-apple-darwin"
//            outputName = "libtest64.dylib"
//
//            // Use other command for this target
//            command = "cargo"
//            env += "CC" to "o64-clang"
//            env += "CXX" to "o64-clang++"
//        }
//
//        create("macOS-aarch64") {
//            target = "aarch64-apple-darwin"
//            outputName = "libtest64.dylib"
//
//            command = "cargo"
//            env += "CC" to "oa64-clang"
//            env += "CXX" to "oa64-clang++"
//        }
//    }
//}