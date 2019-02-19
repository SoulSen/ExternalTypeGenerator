plugins {
    application
    kotlin("jvm") version "1.2.61"
}

application {
    mainClassName = "com.chattriggers.Generator"
}

dependencies {
    compile(kotlin("stdlib"))
    compile("com.github.FalseHonesty:kotlin-overview-parser:master-SNAPSHOT")
    compile("org.eclipse.jgit:org.eclipse.jgit:2.2.0.201212191850-r")
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://jitpack.io")
}

java.sourceSets["main"].withConvention(KotlinSourceSet::class) {
    kotlin.srcDir(file("src/main/kotlin"))
}

task("createGlue", JavaExec::class) {
    group = "ChatTriggers"
    description = "Create Glue for CTKotlinWorkspace"
    run {
        main = "com.chattriggers.GeneratorKt"
        classpath = java.sourceSets["main"].runtimeClasspath
    }
}
