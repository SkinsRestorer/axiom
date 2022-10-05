pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        id("net.kyori.indra") version "3.0.0"
        id("net.kyori.indra.git") version "3.0.0"
        id("net.kyori.indra.publishing") version "2.2.0"
    }
}

rootProject.name = "axiom"

include("lib")
