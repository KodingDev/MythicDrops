import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.github.johnrengelman.shadow") version Versions.com_github_johnrengelman_shadow_gradle_plugin
    id("io.pixeloutlaw.gradle.buildconfigkt") version Versions.io_pixeloutlaw_gradle_buildconfigkt_gradle_plugin
    id("org.jetbrains.dokka") version Versions.org_jetbrains_dokka_gradle_plugin
    id("de.undercouch.download")
    `maven-publish`
}

description = "MythicDrops"

dependencies {
    compileOnly(Libs.spigot_api)
    compileOnly(Libs.worldguard_bukkit)

    implementation(Libs.kotlin_stdlib_jdk8)
    implementation(Libs.config)
    implementation(project(":config-migrator"))
    implementation(Libs.fanciful)
    implementation(Libs.hilt)
    implementation(Libs.method_command)
    implementation(Libs.commons_text)
    implementation(Libs.bstats_bukkit)
    implementation(project(":spigot-plugin-yml-annotations"))
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")

    kapt(project(":spigot-plugin-yml-compiler"))
    kapt(Libs.moshi_kotlin_codegen)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.getByName<DokkaTask>("dokka") {
    outputFormat = "html"
    jdkVersion = 8
}
val dokkaJavadoc = tasks.create("dokkaJavadoc", DokkaTask::class.java) {
    outputDirectory = "${project.buildDir}/javadoc"
    outputFormat = "javadoc"
    jdkVersion = 8
}
val javadocJar = tasks.create("javadocJar", Jar::class.java) {
    dependsOn(dokkaJavadoc)
    archiveClassifier.value("javadoc")
    from(dokkaJavadoc.outputDirectory)
}
val sourcesJar = tasks.create("sourcesJar", Jar::class.java) {
    archiveClassifier.value("sources")
    from(sourceSets.main.get().allSource)
}
tasks.create("assembleDist", Zip::class.java) {
    archiveBaseName.value(project.description)
    from("${project.buildDir}/libs") {
        include("${project.name}-${project.version}.jar")
    }
    from("${project.buildDir}/resources/main") {
        include("*.yml")
        include("resources/**/*")
        include("tiers/*.yml")
        into("MythicDrops")
    }
}

tasks.findByName("assemble")?.dependsOn("shadowJar")
tasks.findByName("build")?.dependsOn("assembleDist")

tasks.withType<ShadowJar> {
    archiveClassifier.set(null as? String?) // stupid overloading
    mergeServiceFiles()
    relocate("com.github.zafarkhaja", "com.tealcube.minecraft.bukkit.mythicdrops.shade.jsemver")
    relocate("se.ranzdo.bukkit.methodcommand", "com.tealcube.minecraft.bukkit.mythicdrops.shade.methodcommand")
    relocate("mkremins", "com.tealcube.minecraft.bukkit.mythicdrops.shade.fanciful")
    relocate("org.apache.commons.lang3", "com.tealcube.minecraft.bukkit.mythicdrops.shade.apache.commons.lang3")
    relocate("org.apache.commons.text", "com.tealcube.minecraft.bukkit.mythicdrops.shade.apache.commons.text")
    relocate("org.bstats", "com.tealcube.minecraft.bukkit.mythicdrops.shade.bstats")
    relocate("okio", "com.tealcube.minecraft.bukkit.mythicdrops.shade.okio")
    relocate("com.squareup.moshi", "com.tealcube.minecraft.bukkit.mythicdrops.shade.moshi")
    relocate("net.amoebaman.util", "com.tealcube.minecraft.bukkit.mythicdrops.shade.amoebaman")
    relocate("javassist", "com.tealcube.minecraft.bukkit.mythicdrops.shade.javassist")
    relocate("co.aikar.commands", "com.tealcube.minecraft.bukkit.mythicdrops.shade.acf")
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            project.shadow.component(this@create)
            artifact(javadocJar)
            artifact(sourcesJar)
        }
    }
}

buildConfigKt {
    appName = "MythicDrops"
    version = getVersionName()
}

fun getVersionName(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}