@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val minecraft_version: String by project
val yarn_version: String by project
val loader_version: String by project
val meteor_version: String by project
val baritone_version: String by project
val kotlin_loader_version: String by project

plugins {
	kotlin("jvm") version "2.0.21"
	id("fabric-loom") version "1.7-SNAPSHOT"
	id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
	archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 21
java {
	toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
	withSourcesJar()
}

repositories {
	mavenCentral()
	mavenLocal()
	maven { url = uri("https://maven.meteordev.org/releases") }
	maven { url = uri("https://maven.meteordev.org/snapshots") }
	maven { url = uri("https://maven.seedfinding.com/") }
	maven { url = uri("https://maven-snapshots.seedfinding.com/") }
	maven { url = uri("https://jitpack.io") }
	maven { url = uri("https://maven.duti.dev/releases") }
}
configurations {
	// configuration that holds jars to include in the jar
	create("extraLibs")
}

configurations {
	named("implementation") {
		extendsFrom(getByName("extraLibs"))
	}
}

fun DependencyHandlerScope.extraLibs(dependencyNotation: String, configure: ModuleDependency.() -> Unit = {}) {
	add("extraLibs", dependencyNotation, configure)
}

dependencies {
	// This will make it work on most platforms. It automatically chooses the right dependencies at runtime.
	val cubiomesVersion = "dev.duti.acheong:cubiomes:1.22.3"
	listOf("linux64", "osx", "windows64").forEach { os ->
		extraLibs("$cubiomesVersion:$os") { isTransitive = false }
	}
	extraLibs(cubiomesVersion) { isTransitive = false }
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:$minecraft_version")
	mappings("net.fabricmc:yarn:$yarn_version:v2")
	modImplementation("net.fabricmc:fabric-loader:$loader_version")
	modImplementation("net.fabricmc:fabric-language-kotlin:$kotlin_loader_version")

	modImplementation("meteordevelopment:meteor-client:$meteor_version-SNAPSHOT")
	modCompileOnly("meteordevelopment:baritone:$baritone_version-SNAPSHOT")

	// seed .locate and ore sim
	val seedFindingDependencies = setOf(
		"com.seedfinding:mc_math:ffd2edcfcc0d18147549c88cc7d8ec6cf21b5b91",
		"com.seedfinding:mc_seed:1ead6fcefe7e8de4b3d60cd6c4e993f1e8f33409",
		"com.seedfinding:mc_core:d64d5f90be66300da41ef58f4f1736db2499784f",
		"com.seedfinding:mc_noise:7e3ba65e181796c4a2a1c8881d840b2254b92962",
		"com.seedfinding:mc_biome:41a42cb9019a552598f12089059538853e18ec78",
		"com.seedfinding:mc_terrain:b4246cbd5880c4f8745ccb90e1b102bde3448126",
		"com.seedfinding:mc_feature:919b7e513cc1e87e029a9cd703fc4e2dc8686229"
	)
	seedFindingDependencies.forEach { dependency ->
		extraLibs(dependency) { isTransitive = false }
	}
	// seedcracker api
	implementation("com.github.19MisterX98.SeedcrackerX:seedcrackerx-api:2.10.1") { isTransitive = false }
	// implementation (include('com.github.19MisterX98.SeedcrackerX:seedcrackerx-api:master-SNAPSHOT')) {transitive = false}
	ksp(project(":processor"))
}

loom {
    accessWidenerPath = file("src/main/resources/meteor-rejects.accesswidener")
}

tasks.processResources {
	inputs.property("version", project.version)
	inputs.property("minecraft_version", minecraft_version)
	inputs.property("loader_version", loader_version)
	filteringCharset = "UTF-8"


	filesMatching("fabric.mod.json") {
		expand(
			"version" to project.version,
			"mc_version" to minecraft_version,
			"gh_hash" to (System.getenv("GITHUB_SHA") ?: ""),
			"kotlin_loader_version" to kotlin_loader_version,
		)
	}
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${project.base.archivesName}" }
	}
	from(configurations["extraLibs"].map { if (it.isDirectory) it else zipTree(it) })
}

tasks.withType<Jar>() {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	if(JavaVersion.current().isJava9Compatible){
		options.release.set(targetJavaVersion)
	}
}
