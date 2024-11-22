plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    // jetbrains compose插件
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    compilerOptions {
//        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
    applyDefaultHierarchyTemplate()
    androidTarget()

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets{
        commonMain.get().apply {
            dependencies{
                implementation(libs.compose.imagepicker)
            }
        }
    }
}
configurations.all {
    resolutionStrategy {
        eachDependency { // # 移除掉版本不相同的依赖,保留项目中使用的版本,加快下载速度,也避免乱七八糟的问题
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
        // preferProjectModules的作用是优先使用项目中的模块，而不是从远程仓库中下载
//            preferProjectModules()
        this.disableDependencyVerification()
        // cacheDynamic0VersionsFor的作用是缓存动态版本，避免每次构建都去下载
        cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
        // cacheChangingModulesFor的作用是缓存变化的模块，避免每次构建都去下载
        cacheChangingModulesFor(0, TimeUnit.SECONDS)
        // failOnVersionConflict的作用是当版本冲突时，抛出异常
        //            failOnVersionConflict()
    }
    exclude("com.vickyleu.filepicker", module = "filepicker")
    exclude("com.vickyleu.sonner")
    exclude("io.coil-kt.coil3")
}
android{
    namespace = "org.uooc.dependencies"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig{
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    lint{
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
}