pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.tapsell.ir") }   // SDK تپسل
        google()
        mavenCentral()
    }
}

rootProject.name = "Chai"
include(":app")
