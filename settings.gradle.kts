pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // REPOSITORIO JITPACK para Toasty y Holograph
        maven { url = uri("https://jitpack.io") }
    }
}

include(":app")