plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta13"
}

group = "net.fab3F"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
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
    archiveVersion.set("1.0")
    archiveClassifier.set("")
}

tasks.register("cleanBuild") {
    dependsOn("clean", "shadowJar")
}