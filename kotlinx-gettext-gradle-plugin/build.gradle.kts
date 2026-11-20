plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.pluginPublish)
    signing
}

dependencies {
    implementation(libs.kotlin.gradle.plugin.api)
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("kotlinx-gettext") {
            id = "io.github.ssimco.kotlinx-gettext"

            displayName = "Kotlinx Gettext"
            description = "Extract strings for i18n from Kotlin files in Gettext format"
            website = "https://github.com/ssimco/kotlinx-gettext"
            vcsUrl = "https://github.com/ssimco/kotlinx-gettext"
            tags = listOf("kotlin", "i18n", "gettext")

            implementationClass = "name.kropp.kotlinx.gettext.gradle.KotlinxGettextGradlePlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

signing {
    useGpgCmd()
}
