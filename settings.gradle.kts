pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories.apply {
        google()
        mavenCentral()
    }
}

rootProject.name = "Personal Money Management"
include(":app")

include(":core")
include(":core:domain")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:common")
include(":core:security")
include(":core:ui")
include(":core:navigation")
include(":core:export")
include(":core:ai")

include(":feature:auth")
include(":feature:accounts")
include(":feature:dashboard")
include(":feature:reports")
include(":feature:settings")
include(":feature:transactions")
include(":feature:assistant")
