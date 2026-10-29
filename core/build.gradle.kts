import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.21"
    `java-library`
    `maven-publish`
}

group = "io.github.skrpld"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
}

// LICENSE and NOTICE live at the repository root, shared with the Android app.
tasks.withType<Jar>().configureEach {
    from(rootDir.parentFile) {
        include("LICENSE", "NOTICE")
        into("META-INF")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("Fiscal Nest Core")
                description.set(
                    "Stateless Kotlin engine that distributes income across expenses, a safety cushion and savings."
                )
                url.set("https://github.com/skrpld/fiscal-nest")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("skrpld")
                        name.set("skrpld")
                    }
                }
                scm {
                    url.set("https://github.com/skrpld/fiscal-nest")
                    connection.set("scm:git:https://github.com/skrpld/fiscal-nest.git")
                }
            }
        }
    }
}
