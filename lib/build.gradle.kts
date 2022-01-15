plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

dependencies {
    // https://mvnrepository.com/artifact/org.yaml/snakeyaml
    api("org.yaml:snakeyaml:1.30")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}