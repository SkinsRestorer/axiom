pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        id("net.kyori.indra") version "3.0.1"
        id("net.kyori.indra.git") version "3.0.1"
        id("net.kyori.indra.publishing") version "3.0.1"
    }
}

rootProject.name = "axiom"

include("lib")
