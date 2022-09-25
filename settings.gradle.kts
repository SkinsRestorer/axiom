pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        id("net.kyori.indra") version "2.2.0"
        id("net.kyori.indra.git") version "2.0.6"
        id("net.kyori.indra.publishing") version "2.2.0"
    }
}

rootProject.name = "axiom"

include("lib")
