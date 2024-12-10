import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.internal.types.error.ErrorModuleDescriptor.platform
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.cocoapods.get().pluginId)
    id(libs.plugins.jetbrains.compose.get().pluginId)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { it ->
        @Suppress("unused")
        val platform = when (it.name) {
            "iosX64", "iosSimulatorArm64" -> "iphonesimulator"
            "iosArm64" -> "iphoneos"
            else -> error("Unsupported target ${it.name}")
        }
        it.binaries {
            framework {
                baseName = "mediaPlayer"
                isStatic = false
                freeCompilerArgs += "-Xverbose-phases=Linker"
                freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
                freeCompilerArgs += "-Xbinary=bundleId=org.uooc.compose.videoplayer"
//                export(project(":composeApp"))
//                export("com.vickyleu.video_player:1.0.0")
            }
            configureEach {
                it.compilerOptions {
                    val podRoot = project.layout.buildDirectory.get()
                        .asFile.resolve("../composeApp/cocoapods/synthetic/ios/build/Release-$platform/")
                    val intermediatesDir = podRoot.resolve("XCFrameworkIntermediates")

                    // 找到包含 `.framework` 的顶层父目录
                    fun findTopLevelParentDirectories(
                        root: File,
                        exclude: File? = null
                    ): Set<File> {
                        val result = mutableSetOf<File>()
                        root.walkTopDown()
                            .filter { it.isDirectory && it.name.endsWith(".framework") }
                            .map { it.parentFile } // 获取每个 .framework 的父目录
                            .filter { parent ->
                                // 过滤掉 exclude 目录及其子目录
                                exclude == null || !parent.canonicalPath.startsWith(exclude.canonicalPath)
                            }
                            .toCollection(result) // 去重并存入结果集合
                        return result
                    }

//                    val frameworkRootSearchPaths =
//                        findTopLevelParentDirectories(podRoot, exclude = intermediatesDir)
//                    val frameworkSearchPaths = findTopLevelParentDirectories(intermediatesDir)
//                    frameworkSearchPaths.forEach { searchPath ->
//                        linkerOpts += ("-F${searchPath.absolutePath}")
//                    }
//                    frameworkRootSearchPaths.forEach { searchPath ->
//                        println("root linkerOpts -F${searchPath.absolutePath}")
//                        linkerOpts += ("-F${searchPath.absolutePath}")
//                    }
                    linkerOpts += "-ld_classic"
                    freeCompilerArgs.add(
                        "-Xoverride-konan-properties=osVersionMin.ios_simulator_arm64=${libs.versions.iosDeploymentTarget.get()};" +
                                "osVersionMin.ios_x64=${libs.versions.iosDeploymentTarget.get()};" +
                                "osVersionMin.ios_arm64=${libs.versions.iosDeploymentTarget.get()}"
                    )
                    // The notification-service-extension is limited to 24 MB of memory.
                    // With mimalloc we can easily hit the 24 MB limit, and the OS kills the process.
                    // But with standard allocation, we're using less then half the limit.
                    freeCompilerArgs.add("-Xallocator=std")
                    freeCompilerArgs.addAll(listOf("-linker-options", "-application_extension"))
                    // workaround for xcode 15 and kotlin < 1.9.10:
                    // https://youtrack.jetbrains.com/issue/KT-60230/Native-unknown-options-iossimulatorversionmin-sdkversion-with-Xcode-15-beta-3
//                    linkerOpts += "-ld_classic"
                }
            }
        }
    }

    cocoapods {
        summary = "MediaPlayer"
        homepage = "."
        version = "1.0.0"
        license = "MIT"
        ios.deploymentTarget = libs.versions.iosDeploymentTarget.get()
        source = "https://cdn.cocoapods.org"
        framework {
            baseName = "MediaPlayer"
            isStatic = false
            optimized = true
            debuggable = false
            val podRoot = project.layout.buildDirectory.get()
                .asFile.resolve("../composeApp/cocoapods/synthetic/ios/build/Release-$platform/")
            val intermediatesDir = podRoot.resolve("XCFrameworkIntermediates")

            // 找到包含 `.framework` 的顶层父目录
            fun findTopLevelParentDirectories(root: File, exclude: File? = null): Set<File> {
                val result = mutableSetOf<File>()
                root.walkTopDown()
                    .filter { it.isDirectory && it.name.endsWith(".framework") }
                    .map { it.parentFile } // 获取每个 .framework 的父目录
                    .filter { parent ->
                        // 过滤掉 exclude 目录及其子目录
                        exclude == null || !parent.canonicalPath.startsWith(exclude.canonicalPath)
                    }
                    .toCollection(result) // 去重并存入结果集合
                return result
            }

            val frameworkRootSearchPaths =
                findTopLevelParentDirectories(podRoot, exclude = intermediatesDir)
            val frameworkSearchPaths = findTopLevelParentDirectories(intermediatesDir)
            frameworkSearchPaths.forEach { searchPath ->
                linkerOpts += ("-F${searchPath.absolutePath}")
            }
            frameworkRootSearchPaths.forEach { searchPath ->
                println("root linkerOpts -F${searchPath.absolutePath}")
                linkerOpts += ("-F${searchPath.absolutePath}")
            }
        }
        pod("VIMediaCache") {
            source = git("https://github.com/vickyleu/VIMediaCache.git") {
                branch = "master"
            }
            moduleName = "VIMediaCache"
            packageName = "what.the.fuck.with.vimediacache"
            linkOnly = true
        }
        noPodspec()
        extraSpecAttributes["frameworks"] =
            "['SystemConfiguration',  'CoreText', 'UIKit']" //导入系统库
    }
    targets.withType<KotlinNativeTarget> {
        // observer.def
        @Suppress("unused")
        compilations.getByName("main") {
            cinterops.create("MediaObserver") {
                definitionFile = projectDir.resolve("src/nativeInterop/cinterop/MediaObserver.def")
            }
            cinterops.create("VIMediaCache") {
                this.packageName = "what.the.fuck.with.vimediacache"
                this.includeDirs(project.file("src/nativeInterop/cinterop/VIMediaCache"))
                definitionFile = projectDir.resolve("src/nativeInterop/cinterop/VIMediaCache.def")
            }
        }
    }


    sourceSets {
        androidMain.get().apply {
            kotlin.srcDirs("src/androidMain/kotlin")
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.exoplayer.dash)
            implementation(libs.androidx.media3.exoplayer.hls)
            implementation(libs.androidx.media3.mediasession)
            implementation(libs.androidx.media3.ui)


            // TODO 忘记这里是否需要精简了,解码器
            //noinspection UseTomlInstead
            implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-core:1.0.19")
            //noinspection UseTomlInstead
            implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-armeabi-v7a:1.0.19")
//            implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-x86:1.0.19")
//            implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-x86_64:1.0.19")
            //noinspection UseTomlInstead
            implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-arm64-v8a:1.0.19")


            // for some devices, compose is not working with exoplayer when Transition is working, so we need to use GSYVideoPlayer
            //noinspection UseTomlInstead
            api("com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer:v9.0.0-release-jitpack")
            //noinspection UseTomlInstead
            compileOnly("org.checkerframework:checker-qual:3.46.0")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)


            implementation(project.dependencies.platform(libs.ktor.bom))
            implementation(project.dependencies.platform(libs.coil.bom))
            implementation(libs.coil.compose)
            implementation(libs.coil.core)
            implementation(libs.coil.network.ktor)
            implementation(libs.compose.filepicker)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "mediaplayer.generated.resources"
    generateResClass = always
}

android {
    namespace = "org.example.videoplayer"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].java.srcDirs("src/androidMain/kotlin")
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    lint {
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
        debugImplementation(compose.preview)
    }
}

