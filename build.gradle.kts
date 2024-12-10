import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

//需要判断是否是jitpack的构建，如果是jitpack的构建，需要将build目录设置到项目根目录下
if (System.getenv("JITPACK") == null) {
    rootProject.layout.buildDirectory.set(
        file(
            "${
                rootProject.rootDir.parentFile.parentFile
                    .absolutePath
            }/buildOut/${rootProject.name}"
        )
    )
}

buildscript {
    dependencies {
        classpath(platform(libs.kotlin.plugins.bom))
        classpath("org.jetbrains.compose:compose-gradle-plugin:${libs.versions.compose.plugin.get()}")
    }
}

plugins {
    id(libs.plugins.android.application.get().pluginId) apply false
    id(libs.plugins.kotlin.multiplatform.get().pluginId) apply false
    id(libs.plugins.android.library.get().pluginId) apply false
    id(libs.plugins.kotlin.jvm.get().pluginId) apply false
    id(libs.plugins.kotlin.android.get().pluginId) apply false


    id(libs.plugins.kotlin.cocoapods.get().pluginId) apply false
    id(libs.plugins.kotlin.kapt.get().pluginId) apply false
    id(libs.plugins.kotlin.parcelize.get().pluginId) apply false
    // jetbrains compose插件
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    // kotlin 原子操作插件
    alias(libs.plugins.kotlinx.atomicfu) apply false
    // google ksp插件
    alias(libs.plugins.ksp) apply false
    // RUST 插件没必要每次都编译
//    alias(libs.plugins.rust) apply false
    //  sqldelight插件
    alias(libs.plugins.sqldelight) apply false
    // kotlin serialization插件
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.buildkonfig) apply false
}


val javaVersion = JavaVersion.toVersion(libs.versions.jvmTarget.get())
check(JavaVersion.current().isCompatibleWith(javaVersion)) {
    "This project needs to be run with Java ${javaVersion.getMajorVersion()} or higher (found: ${JavaVersion.current()})."
}

// https://kotlinlang.org/docs/js-project-setup.html#use-pre-installed-node-js
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    project.extensions.findByType<NodeJsRootExtension>()?.apply {
//        this.download = false
    }
}

subprojects {
    if (System.getenv("JITPACK") == null) {
        this.layout.buildDirectory.set(
            file(
                "${
                    rootProject.layout.buildDirectory.get().asFile.absolutePath
                }/subprojects/${project.name}"
            )
        )
    }
    afterEvaluate {
        // Remove log pollution until Android support in KMP improves.
        project.extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
            ?.let { kmpExt ->
                kmpExt.sourceSets.removeAll {
                    setOf(
                        "androidAndroidTestRelease",
                        "androidTestFixtures",
                        "androidTestFixturesDebug",
                        "androidTestFixturesRelease",
                    ).contains(it.name)
                }
            }
        if (project.hasProperty("android")) {
            (project.property("android") as BaseExtension).apply {
                compileSdkVersion(libs.versions.android.compileSdk.get().toInt())
                defaultConfig {
                    this.minSdk = libs.versions.android.minSdk.get().toInt()
                    this.targetSdk = libs.versions.android.targetSdk.get().toInt()
                }
            }
        }
    }
    configurations.all {
        val listForceRefreshDependencies: List<String> = listOf(
//            "com.vickyleu.image_picker"
        )
        resolutionStrategy {
            eachDependency { // # 移除掉版本不相同的依赖,保留项目中使用的版本,加快下载速度,也避免乱七八糟的问题
                if(listForceRefreshDependencies.contains(requested.group)){
                    cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
                    // cacheChangingModulesFor的作用是缓存变化的模块，避免每次构建都去下载
                    cacheChangingModulesFor(0, TimeUnit.SECONDS)
                }
                if (requested.group.startsWith("io.ktor")) {
                    useVersion(libs.versions.ktor.bom.get())
                } else if (requested.group.startsWith("androidx.exifinterface")) {
                    useVersion("1.3.7")
                } else if (requested.group.startsWith("androidx.media3")) {
                    useVersion(libs.versions.media3.get())
                } else if (requested.group=="androidx.media") {
                    useVersion(libs.versions.androidx.appcompat.get())
                }  else if (requested.group.startsWith("androidx.lifecycle")) {
                    useVersion(libs.versions.lifecycleCommonJvm.get())
                } else if (requested.group == "androidx.activity" && requested.name.startsWith("activity")) {
                    useVersion(libs.versions.androidx.activityCompose.get())
                } else if (requested.group == "org.jetbrains.kotlin") {
                    useVersion(libs.versions.kotlin.get())
                } else if (requested.group == "org.jetbrains.kotlinx" && requested.name == "atomicfu") {
                    useVersion(libs.versions.atomicfu.get())
                } else if (requested.group.startsWith("org.jetbrains.compose")) {
                    useVersion(libs.versions.compose.plugin.get())
                } else if (requested.group.startsWith("org.jetbrains.kotlinx") && requested.name == "kotlinx-datetime") {
                    useVersion(libs.versions.kotlinxDatetime.get())
                } else if (requested.group.startsWith("org.jetbrains.kotlinx") && requested.name.startsWith(
                        "kotlinx-coroutines"
                    )
                ) {
                    useVersion(libs.versions.coroutines.bom.get())
                }
            }
            this.disableDependencyVerification()
            // cacheDynamic0VersionsFor的作用是缓存动态版本，避免每次构建都去下载
            cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
            // cacheChangingModulesFor的作用是缓存变化的模块，避免每次构建都去下载
            cacheChangingModulesFor(0, TimeUnit.SECONDS)
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions{
            jvmTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
            this.freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}

tasks.register("delete", Delete::class) {
    delete(rootProject.layout.buildDirectory.get().asFile)
}