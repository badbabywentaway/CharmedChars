
plugins {
    kotlin("jvm") version "2.2.21"
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"


}

group = property("group")!!
version = property("version")!!

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc-repo" }
    maven("https://oss.sonatype.org/content/groups/public/") { name = "sonatype" }
    maven("https://jitpack.io") { name = "jitpack" }
    maven ("https://maven.enginehub.org/repo/" )
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${property("paperVersion")}")
    compileOnly ("com.sk89q.worldguard:worldguard-bukkit:${property("worldGuardVersion")}")
    implementation("com.github.GriefPrevention:GriefPrevention:${property("griefPreventionVersion")}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${property("kotlinVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("coroutinesVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${property("coroutinesVersion")}")

    testImplementation("org.jetbrains.kotlin:kotlin-test:${property("kotlinVersion")}")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
}
    tasks.shadowJar {
        archiveBaseName.set("CharmedChars")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
        relocate("kotlin", "org.stephanosbad.charmedchars.kotlin")
        relocate("kotlinx.coroutines", "org.stephanosbad.charmedchars.kotlinx.coroutines")
        minimize()

        // Output location
        destinationDirectory.set(file("${project.buildDir}/libs"))
    }

    tasks.build { dependsOn(tasks.shadowJar) }
    tasks.runServer { minecraftVersion("1.21.10") }
    tasks.test { useJUnitPlatform() }