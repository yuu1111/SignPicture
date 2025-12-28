import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("dev.architectury.loom") version "1.7.+"
    id("architectury-plugin") version "3.4.+"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "2.0.0"
}

// Stonecutter version parameters
val mcVersion = stonecutter.current.version
val loader = stonecutter.current.project.substringAfterLast("-")

val modId: String by project
val modName: String by project
val modVersion: String by project
val modGroup: String by project

group = modGroup
version = "$modVersion+$mcVersion"
base.archivesName.set(modId)

// Configure Architectury based on loader
architectury {
    minecraft = mcVersion
}

// Configure platform and create necessary configurations
when (loader) {
    "fabric" -> architectury.fabric()
    "forge" -> {
        architectury.forge()
        // Create forge configuration if it doesn't exist
        if (configurations.findByName("forge") == null) {
            configurations.create("forge")
        }
    }
    "neoforge" -> {
        architectury.neoForge()
        // Create neoForge configuration if it doesn't exist
        if (configurations.findByName("neoForge") == null) {
            configurations.create("neoForge")
        }
    }
}

// Configure Loom
loom {
    silentMojangMappingsLicense()

    runConfigs.configureEach {
        ideConfigGenerated(true)
    }
}

repositories {
    mavenCentral()
    maven("https://maven.architectury.dev/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.neoforged.net/releases/")
}

// Version-specific dependencies
val fabricLoaderVersion = when {
    mcVersion.startsWith("1.21") -> "0.16.0"
    mcVersion.startsWith("1.20") -> "0.15.11"
    else -> "0.14.24"
}

val fabricApiVersion = when {
    mcVersion == "1.21.1" -> "0.102.0+1.21.1"
    mcVersion == "1.20.1" -> "0.92.2+1.20.1"
    else -> "0.92.2+1.20.1"
}

val forgeVersion = when {
    mcVersion == "1.20.1" -> "47.2.0"
    else -> ""
}

val neoforgeVersion = when {
    mcVersion == "1.21.1" -> "21.1.1"
    mcVersion == "1.20.1" -> "47.1.100"
    else -> ""
}

// Architectury API version mapping
// Note: architectury-neoforge artifact only exists for 1.20.2+
val architecturyVersion = when {
    mcVersion.startsWith("1.21") -> "13.0.6"
    mcVersion.startsWith("1.20") -> "9.2.14"
    else -> "9.2.14"
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())

    // Fabric dependencies
    if (loader == "fabric") {
        modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
        modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
        modImplementation("dev.architectury:architectury-fabric:$architecturyVersion")
    }
}

// Forge dependencies
if (loader == "forge") {
    dependencies.add("forge", "net.minecraftforge:forge:$mcVersion-$forgeVersion")
    dependencies.add("modImplementation", "dev.architectury:architectury-forge:$architecturyVersion")
}

// NeoForge dependencies
if (loader == "neoforge") {
    dependencies.add("neoForge", "net.neoforged:neoforge:$neoforgeVersion")
    dependencies.add("modImplementation", "dev.architectury:architectury-neoforge:$architecturyVersion")
}

// Configure source sets - use root project paths for Stonecutter
val rootDir = rootProject.projectDir
val versionProject = stonecutter.current.project
sourceSets {
    main {
        java {
            srcDir(rootDir.resolve("common/src/main/java"))
            srcDir(rootDir.resolve("$loader/src/main/java"))
            // Add version-specific sources (overrides platform sources)
            srcDir(rootDir.resolve("versions/$versionProject/src/main/java"))
        }
        resources {
            srcDir(rootDir.resolve("common/src/main/resources"))
            srcDir(rootDir.resolve("$loader/src/main/resources"))
            srcDir(rootDir.resolve("versions/$versionProject/src/main/resources"))
        }
    }
}

// Configure Stonecutter to process common sources
stonecutter {
    swap("mcVersion", mcVersion)
    const("fabric", loader == "fabric")
    const("forge", loader == "forge")
    const("neoforge", loader == "neoforge")
}

// Java configuration - MC 1.21+ requires Java 21
val javaVersionNum = when {
    mcVersion.startsWith("1.21") -> 21
    else -> 17
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersionNum))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(
            if (javaVersionNum >= 21) JvmTarget.JVM_21 else JvmTarget.JVM_17
        )
    }
}

tasks.processResources {
    val props = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to version,
        "minecraft_version" to mcVersion
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand(props)
    }
}

// Handle duplicate sources in sourcesJar (version-specific overrides)
tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
