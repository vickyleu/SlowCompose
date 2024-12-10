@file:Suppress("UnstableApiUsage")




rootProject.name = "SlowCompose"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories.apply {
        removeAll(this)
    }
    dependencyResolutionManagement.repositories.apply {
        removeAll(this)
    }
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            gradlePluginPortal {
                content {
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("androidx.databinding.*")
                    // 避免无效请求,加快gradle 同步依赖的速度
                    excludeGroupByRegex("com.github.(?!johnrengelman).*")
                }
            }
            maven{
                setUrl("https://repo1.maven.org/maven2")
            }
            google {
                content {
                    excludeGroupByRegex("org.jogamp.*")
                    includeGroupByRegex(".*google.*")
                    includeGroupByRegex(".*android.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
            mavenCentral {
                content {
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.android.tools.*")
                    excludeGroupByRegex("androidx.compose.*")
                    excludeGroupByRegex("com.github.(?!johnrengelman|oshi).*")
                }
            }
//            includeBuild("rust_plugin")
            maven(url = "https://androidx.dev/storage/compose-compiler/repository") {
                content {
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
            maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev") {
                content {
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }

            maven {
                setUrl("https://jogamp.org/deployment/maven")
                content {
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    includeGroupByRegex("org.jogamp.*")
                    includeGroupByRegex("dev.datlag.*")
                }
            }

            maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        }
    }
    resolutionStrategy {
        val properties = java.util.Properties()
        rootDir.resolve("gradle/libs.versions.toml").inputStream().use(properties::load)
        val kotlinVersion = properties.getProperty("kotlin").removeSurrounding("\"")
        eachPlugin {
            if (requested.id.id == "dev.icerock.mobile.multiplatform-resources") {
                useModule("dev.icerock.moko:resources-generator:${requested.version}")
            } else if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion(kotlinVersion)
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}


dependencyResolutionManagement {
    //FAIL_ON_PROJECT_REPOS
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()
        google {
            content {
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
            }
        }
        maven {
            setUrl("https://jitpack.io")
        }

        val properties = java.util.Properties().apply {
            runCatching { rootProject.projectDir.resolve("local.properties") }
                .getOrNull()
                .takeIf { it?.exists() ?: false }
                ?.reader()
                ?.use(::load)
        }
        val environment: Map<String, String?> = System.getenv()
        extra["githubToken"] = properties["github.token"] as? String
            ?: environment["GITHUB_TOKEN"] ?: ""

        maven {
            url = uri("https://maven.pkg.github.com/vickyleu/compose2")
            credentials {
                username = "vickyleu"
                password = extra["githubToken"]?.toString()
            }
            // github packages cached previously downloaded artifacts, we need to redirect to the maven pom metadata
//            metadataSources {
////                gradleMetadata()
////                mavenPom()
////                ignoreGradleMetadataRedirection()
//            }
            content {
                excludeGroupByRegex("com.finogeeks.*")
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("(?!com|cn).github.(?!vickyleu).*")
            }
        }


        ivy {
            name = "Node.js"
            setUrl("https://nodejs.org/dist")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("org.nodejs", "node")
            }
            isAllowInsecureProtocol = false
        }
        maven{
            setUrl("http://maven.aliyun.com/nexus/content/repositories/releases/")
            isAllowInsecureProtocol = true
        }
        maven(url = "https://maven.aliyun.com/repository/public")

        // 保利威阿里云效
        maven {
            credentials {
                username = "609cc5623a10edbf36da9615"
                password = "EbkbzTNHRJ=P"
            }
            setUrl("https://packages.aliyun.com/maven/repository/2102846-release-8EVsoM/")
        }

        maven {
            setUrl("https://maven.aliyun.com/repository/public/")
        }
        maven {
            setUrl("https://maven.aliyun.com/nexus/content/repositories/jcenter/")
        }
        maven {
            setUrl("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        }

        maven {
            setUrl("https://dl.bintray.com/kotlin/kotlin-dev")
        }
        maven {
            setUrl("https://dl.bintray.com/kotlin/kotlin-eap")
        }
        maven {
            setUrl("https://jogamp.org/deployment/maven")
        }
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://raw.github.com/vickyleu/wemeet_maven/main/")

        maven("https://git2.baijiashilian.com/open-android/maven/raw/master/") {
            content {
                includeGroupByRegex("com.baijiayun.*")
                includeGroupByRegex("com.baijia.*")
            }
        }
        maven("http://nexus.baijiayun.com/nexus/content/groups/android-public/") {
            isAllowInsecureProtocol = true
            content {
                includeGroupByRegex("com.baijiayun.*")
                includeGroupByRegex("com.baijia.*")
            }
        }
        maven(url = "https://artifact.bytedance.com/repository/Volcengine/")
        maven("https://gradle.finogeeks.club/repository/applet/") {
            credentials {
                username = "applet"
                password = "123321"
            }
            content {
                includeGroupByRegex("com.finogeeks.*")
            }
        }

        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")

    }
}

include(
    ":composeApp",
    ":rust",
    ":processor",
    ":annotation"
)

include(":mediaPlayer")