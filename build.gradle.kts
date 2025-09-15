plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta13"
}

group = "net.fab3F"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.lavalink.dev/releases")
}

dependencies {
    implementation("net.dv8tion:JDA:5.6.1")
    implementation("dev.arbjerg:lavalink-client:3.2.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    manifest {
        attributes["Main-Class"] = "net.fab3F.Main"
    }
    archiveBaseName.set("funk3F")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")
}

tasks.register("cleanBuild") {
    dependsOn("clean", "shadowJar")
}