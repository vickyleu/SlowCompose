import com.base.syntheticPodfileGen
import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodGenTask

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
//    id(libs.plugins.kotlin.cocoapods.get().pluginId)
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
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "mediaPlayer"
            isStatic = true

            freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
            freeCompilerArgs += "-Xbinary=bundleId=org.uooc.compose.videoplayer"
//            freeCompilerArgs += listOf("-Xswift-version=5")
//            export(project(":composeApp"))
            export("com.vickyleu.video_player:1.0.0")
        }
    }

//    cocoapods {
//        summary = "mediaPlayer"
//        homepage = "https://example.com"
//        version = "1.0.0"
//        ios.deploymentTarget = libs.versions.iosDeploymentTarget.get()
//        framework {
//            baseName = "mediaPlayer"
//            isStatic = true
////            export(project(":composeApp"))
//        }
//    }

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



            implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-core:1.0.19")
            implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-armeabi-v7a:1.0.19")
//            implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-x86:1.0.19")
//            implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-x86_64:1.0.19")
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

