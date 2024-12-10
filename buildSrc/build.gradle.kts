//需要判断是否是jitpack的构建，如果是jitpack的构建，需要将build目录设置到项目根目录下
if (System.getenv("JITPACK") == null) {
    val realRootProject = rootProject.rootDir.parentFile
    val buildDir = file("${rootProject.rootDir.parentFile.parentFile
            .parentFile.absolutePath}/buildOut/${realRootProject.name}/buildCollection/${project.name}")
    rootProject.layout.buildDirectory.set(buildDir)
}
buildscript {
    dependencies {
        classpath("com.android.library:com.android.library.gradle.plugin:${libs.versions.agp.get()}")
    }
}

plugins {
    `java-library`
    `kotlin-dsl` version "5.1.0"
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.cocoapods) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}

sourceSets {
    main {
        java.srcDir("src/main/kotlin")
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(libs.versions.kotlin.get())
            }
        }
    }
}
afterEvaluate {
    tasks.named("clean", Delete::class) {
        delete(rootDir.resolve("**/.idea"))
        delete(rootDir.resolve("**/.gradle"))
        delete(rootDir.resolve("**/.kotlin"))
    }
}
dependencies {
    implementation(project.dependencies.platform(libs.compose.bom))
    implementation(project.dependencies.platform(libs.coroutines.bom))
    implementation(project.dependencies.platform(libs.kotlin.bom))
    implementation(libs.coroutines.jvm)

    //noinspection UseTomlInstead
    implementation("com.diffplug.spotless:spotless-lib:2.44.0")
    //noinspection UseTomlInstead
    implementation("com.diffplug.spotless:spotless-lib-extra:2.44.0")

    //noinspection UseTomlInstead
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:2.3.3")
    //noinspection UseTomlInstead
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.3") // JAXB 的运行时
    //noinspection UseTomlInstead
    implementation("commons-codec:commons-codec:1.16.0")
    // 添加 commons-lang3
    implementation("org.apache.commons:commons-lang3:3.13.0")

    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation(libs.javapoet) // https://github.com/google/dagger/issues/3068
}

