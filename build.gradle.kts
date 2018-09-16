plugins {
    application
    kotlin("jvm") version "1.2.61"
}

application {
    mainClassName = "com.chattriggers.Generator"
}

dependencies {
    compile(kotlin("stdlib"))
    compile("com.github.FalseHonesty:kotlin-parser:07cf24f257")
    compile("org.eclipse.jgit:org.eclipse.jgit:2.2.0.201212191850-r")
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://jitpack.io")
}