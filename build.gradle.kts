import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.10"
    id("cl.franciscosolis.sonatype-central-upload") version "1.0.3"
    id("org.jetbrains.dokka") version "2.0.0"
    `maven-publish`
}

group = "net.ririfa"
version = "1.2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.asFile.get().resolve("dokka/html"))
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("")
}

publishing {
    publications {
        //maven
        create<MavenPublication>("maven") {

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("IGF")
                description.set("Minecraft Paper Plugin makes it easy to build InventoryGUI PAI")
                url.set("https://github.com/SwiftStorm-Studio/IGF")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/license/mit")
                    }
                }
                developers {
                    developer {
                        id.set("ruxy")
                        name.set("Ruxy")
                        email.set("main@rk4z.net")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/SwiftStorm-Studio/IGF.git")
                    developerConnection.set("scm:git:ssh://github.com/SwiftStorm-Studio/IGF.git")
                    url.set("https://github.com/SwiftStorm-Studio/IGF")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://repo.ririfa.net/maven2-rel/")
            val snapshotsRepoUrl = uri("https://repo.ririfa.net/maven2-snap/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials {
                username = findProperty("nxUN").toString()
                password = findProperty("nxPW").toString()
            }
        }
    }
}