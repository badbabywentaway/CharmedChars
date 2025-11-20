
plugins {
    kotlin("jvm") version "2.2.21"
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    jacoco
}

jacoco {
    toolVersion = "0.8.12"
}

group = property("group")!!
version = property("version")!!

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc-repo" }
    maven("https://oss.sonatype.org/content/groups/public/") { name = "sonatype" }
    maven("https://jitpack.io") { name = "jitpack" }
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.dmulloy2.net/repository/public/") { name = "protocollib-repo" }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${property("paperVersion")}")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:${property("worldGuardVersion")}")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.3-beta-14")
    compileOnly("com.github.GriefPrevention:GriefPrevention:${property("griefPreventionVersion")}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${property("kotlinVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("coroutinesVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${property("coroutinesVersion")}")

    // Database dependencies for structure tracking
    implementation("org.jetbrains.exposed:exposed-core:0.48.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.48.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.48.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    // Unit testing framework
    testImplementation("org.jetbrains.kotlin:kotlin-test:${property("kotlinVersion")}")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")

    // MockBukkit for Paper API testing
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.9.0")

    // AssertJ for fluent assertions
    testImplementation("org.assertj:assertj-core:3.25.1")
}
    tasks.processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    tasks.shadowJar {
        archiveBaseName.set("CharmedChars")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())

        // Explicitly include dependencies from runtimeClasspath
        configurations = listOf(project.configurations.runtimeClasspath.get())

        // Relocate Kotlin and coroutines to avoid conflicts with other plugins
        relocate("kotlin", "org.stephanosbad.charmedchars.kotlin")
        relocate("kotlinx.coroutines", "org.stephanosbad.charmedchars.kotlinx.coroutines")

        // NOTE: Do NOT relocate Exposed or SQLite!
        // - SQLite JDBC contains native libraries (.so/.dll/.dylib) that break when relocated
        // - Exposed uses reflection and needs original package names
        // These are kept at their original paths to avoid UnsatisfiedLinkError

        // Merge service files to preserve ServiceLoader functionality (JDBC drivers)
        mergeServiceFiles()

        // Output location
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
    }

    tasks.build { dependsOn(tasks.shadowJar) }
    tasks.runServer { minecraftVersion("1.21.10") }

    tasks.test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    // JaCoCo test coverage reporting
    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    // Exclude generated classes and data classes
                    exclude("**/*\$*.class")
                }
            })
        )
    }

    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.80".toBigDecimal()
                }
            }
        }
    }

    // Version management task
    tasks.register("version") {
        group = "versioning"
        description = "Display current plugin version"
        doLast {
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("  CharmedChars - Version Info")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("  Version: ${project.version}")
            println("  Group: ${project.group}")
            println("  Name: ${project.name}")
            println("  Minecraft: 1.21.10")
            println("  Kotlin: ${property("kotlinVersion")}")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("  See VERSION.md for full changelog")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }
    }

    // Build with version info task
    tasks.register("buildWithVersion") {
        group = "build"
        description = "Build plugin and display version information"
        dependsOn(tasks.named("version"), tasks.build)
        doLast {
            println("\n✓ Build completed successfully!")
            println("  Output: ${tasks.shadowJar.get().archiveFile.get().asFile.absolutePath}")
        }
    }