pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "CharmedChars"

// Build against a local Integrare checkout when one sits next to this repo;
// otherwise the published artifact is resolved from JitPack.
if (file("../Integrare/settings.gradle.kts").exists()) {
    includeBuild("../Integrare") {
        dependencySubstitution {
            substitute(module("com.github.badbabywentaway:Integrare")).using(project(":"))
        }
    }
}
