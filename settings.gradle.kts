pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.+"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(version: String, vararg loaders: String) {
            for (loader in loaders) vers("$version-$loader", version)
        }

        // 対応バージョン
        // NeoForge support from 1.21+ only (1.20.1 NeoForge was essentially Forge)
        mc("1.21.1", "fabric", "neoforge")
        mc("1.20.1", "fabric", "forge")

        vcsVersion = "1.20.1-fabric"
    }
    create(rootProject)
}

rootProject.name = "SignPicture"
