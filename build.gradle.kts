plugins {
    `java-library`
    `maven-publish`
    id("net.kyori.indra")
    id("net.kyori.indra.publishing")
}

repositories {
    mavenCentral()
}

group = "net.skinsrestorer"
version = "1.1.3-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

dependencies {
    implementation("org.jetbrains:annotations:24.0.1")

    // https://mvnrepository.com/artifact/org.yaml/snakeyaml
    api("org.yaml:snakeyaml:2.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

indra {
    github("SkinsRestorer", "axiom") {
        ci(true)
    }

    gpl3OnlyLicense()
    publishReleasesTo("codemc-releases", "https://repo.codemc.org/repository/maven-releases/")
    publishSnapshotsTo("codemc-snapshots", "https://repo.codemc.org/repository/maven-snapshots/")

    configurePublications {
        pom {
            name.set("axiom")
            url.set("https://skinsrestorer.net/")
            organization {
                name.set("SkinsRestorer")
                url.set("https://skinsrestorer.net")
            }
            developers {
                developer {
                    id.set("xknat")
                    timezone.set("Europe/Amsterdam")
                }
                developer {
                    id.set("AlexProgrammerDE")
                    timezone.set("Europe/Berlin")
                    url.set("https://pistonmaster.net")
                }
            }
        }

        versionMapping {
            usage(Usage.JAVA_API) { fromResolutionOf(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME) }
            usage(Usage.JAVA_RUNTIME) { fromResolutionResult() }
        }
    }
}

val repoName = if (version.toString().endsWith("SNAPSHOT")) "maven-snapshots" else "maven-releases"
publishing {
    repositories {
        maven("https://repo.codemc.org/repository/${repoName}/") {
            credentials.username = System.getenv("CODEMC_USERNAME")
            credentials.password = System.getenv("CODEMC_PASSWORD")
            name = "codemc"
        }
    }
}
