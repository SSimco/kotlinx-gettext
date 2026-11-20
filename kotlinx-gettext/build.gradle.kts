import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.mavenPublish)
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
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// JS tests read fixtures from this directory through the resource() test helper.
tasks.withType<KotlinJsTest>().configureEach {
    environment("KOTLINX_GETTEXT_TEST_RESOURCES", file("src/commonTest/resources").absolutePath)
}

signing {
    useGpgCmd()
}
