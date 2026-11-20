import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import java.net.URI

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
    signing
}

plugins.withType<NodeJsPlugin> {
    the<NodeJsEnvSpec>().download.set(false)
}

kotlin {
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm()

    js {
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.okio)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jsTest.dependencies {
            implementation(libs.okio.nodefilesystem)
        }
    }
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
}

publishing {
    publications.withType<MavenPublication> {
        groupId = project.group.toString()
        version = project.version.toString()

        artifact(javadocJar.get())

        pom {
            name.set("Kotlinx Gettext")
            description.set("Kotlin Gettext i18n library")
            url.set("https://github.com/kropp/kotlinx-gettext")

            scm {
                connection.set("scm:git:git://github.com/kropp/kotlinx-gettext")
                developerConnection.set("scm:git:git://github.com/kropp/kotlinx-gettext")
                url.set("https://github.com/kropp/kotlinx-gettext/tree/main")
            }

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("kropp")
                    name.set("Victor Kropp")
                    email.set("victor@kropp.name")
                }
            }
        }
    }

    repositories {
        maven {
            url = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials(PasswordCredentials::class.java)
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(tasks.withType<Sign>())
}