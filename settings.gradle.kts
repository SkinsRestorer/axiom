pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        id("net.kyori.indra") version "2.1.1"
        id("net.kyori.indra.git") version "2.0.6"
        id("net.kyori.indra.publishing") version "2.1.1"
    }
}

rootProject.name = "axiom"

include("lib")
