plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish)
    signing
    alias(libs.plugins.ksp)
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)

    compileOnly(libs.autoService.annotations)
    ksp(libs.autoService.ksp)

    testImplementation(kotlin("test"))
    testCompileOnly(libs.autoService.annotations)
    testImplementation(libs.kctfork.core)
}

tasks.test {
    useJUnitPlatform()
}

signing {
    useGpgCmd()
}
