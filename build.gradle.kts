import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.9.10"
}

group = "com.example"
version = "1.0"

application {
    mainClass.set("com.example.AppKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.4")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.4")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("${project.name}-all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.example.AppKt"
    }
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    with(tasks.jar.get() as CopySpec)
}
