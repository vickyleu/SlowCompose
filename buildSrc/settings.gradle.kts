@file:Suppress("UnstableApiUsage")

rootProject.name = "buildSrc"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            gradlePluginPortal()

            mavenCentral()
            google {
                content {
                    includeGroupByRegex(".*google.*")
                    includeGroupByRegex(".*android.*")
                }
            }
            maven(url = "https://androidx.dev/storage/compose-compiler/repository")
            maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
            maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        }
    }
}



dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        google {
            content {
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
            }
        }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven {
            setUrl("https://jitpack.io")
            content {
                includeGroupByRegex("com.github.*")
            }
        }
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

