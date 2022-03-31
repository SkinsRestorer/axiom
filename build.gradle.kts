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
version = "1.1.2-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

dependencies {
    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.12.0")

    implementation("org.jetbrains:annotations:23.0.0")

    // https://mvnrepository.com/artifact/org.yaml/snakeyaml
    api("org.yaml:snakeyaml:1.30")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
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
