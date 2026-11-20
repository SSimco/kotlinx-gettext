import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.mavenPublish) apply false
    wrapper
}

allprojects {
    group = "io.github.ssimco"
    version = "0.8.0"

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "11"
        targetCompatibility = "11"

        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            languageVersion.set(KotlinVersion.KOTLIN_2_2)
            apiVersion.set(KotlinVersion.KOTLIN_2_2)
        }
    }
}

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            pom {
                developers {
                    developer {
                        id.set("ssimco")
                        name.set("SSimco")
                        url.set("https://github.com/ssimco")
                        roles.add("maintainer")
                    }
                    developer {
                        id.set("kropp")
                        name.set("Victor Kropp")
                        email.set("victor@kropp.name")
                        url.set("https://github.com/kropp")
                        roles.add("original author")
                    }
                }
            }
        }
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = "9.9.2"
    distributionUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
    distributionSha256Sum = "971fcd7150ca4ba04852c0b2ca0d2d0ab9468e14b03473eab19528809914c76b"
}
