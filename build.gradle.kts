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
    implementation("net.dv8tion:JDA:6.3.2") {
        exclude(module="opus-java")
        exclude(module="tink")
    }
    implementation("dev.arbjerg:lavalink-client:3.4.0")
    implementation("tools.jackson.core:jackson-databind:3.1.0")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml:3.1.0")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.32")
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