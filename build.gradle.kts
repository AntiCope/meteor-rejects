plugins {
    alias(libs.plugins.fabric.loom)
}

base {
    archivesName = properties["archives_base_name"] as String
    version = "${libs.versions.mod.version.get()}-${libs.versions.minecraft.get()}"
    group = properties["maven_group"] as String
}

repositories {
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
    maven { 
        name = "seedfinding-maven"
        url = uri("https://maven.seedfinding.com/") 
    }
    maven {
        name = "seedfinding-maven-snapshots"
        url = uri("https://maven-snapshots.seedfinding.com/") 
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "cubiomes-maven"
        url = uri("https://maven.duti.dev/releases") 
    }
    mavenCentral()
}

val extraLibs: Configuration by configurations.creating
val modInclude: Configuration by configurations.creating

configurations {
    implementation.configure {
        extendsFrom(modInclude)
        extendsFrom(extraLibs) 
    }
    include.configure {
        extendsFrom(modInclude)
        extendsFrom(extraLibs)
    }
}

dependencies {    
    // Fabric
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)

    val fapiVersion = libs.versions.fabric.api.get()
    modInclude(fabricApi.module("fabric-resource-loader-v1", fapiVersion))

    //Cubiomes (Different platforms. Automatically chooses the right one.)
    extraLibs(libs.cubiomes) { isTransitive = false }
    extraLibs(variantOf(libs.cubiomes) { classifier("linux64") }) { isTransitive = false }
    extraLibs(variantOf(libs.cubiomes) { classifier("osx") }) { isTransitive = false }
    extraLibs(variantOf(libs.cubiomes) { classifier("windows64") }) { isTransitive = false }

    // Meteor
    implementation(libs.meteor.client)

    //Baritone
    compileOnly(libs.baritone)

    // ExploitPreventer
    compileOnly(libs.exploit.preventer.api)

    // SeedFinding
    extraLibs(libs.mc.math) { isTransitive = false }
    extraLibs(libs.mc.seed) { isTransitive = false }
    extraLibs(libs.mc.core) { isTransitive = false }
    extraLibs(libs.mc.noise) { isTransitive = false }
    extraLibs(libs.mc.biome) { isTransitive = false }
    extraLibs(libs.mc.terrain) { isTransitive = false }
    extraLibs(libs.mc.feature) { isTransitive = false }

    // SeedCrackerX
    implementation(libs.seedcrackerx.api) { isTransitive = false }
    include(libs.seedcrackerx.api)

}

fun toMinecraftCompat(version: String): String {
    val match = Regex("""^(\d{2})\.([1-9]\d*)(?:\.([1-9]\d*))?$""")
        .matchEntire(version)
        ?: error("Invalid Minecraft version format: $version. Expected YY.D or YY.D.H")

    val (year, drop, _) = match.destructured
    return "~$year.$drop"
}

tasks {
    processResources {

        val ghHash = providers.gradleProperty("gh_hash").getOrElse("dev")

        val propertyMap = mapOf(
            "version" to project.version,
            "minecraft_version" to toMinecraftCompat(libs.versions.minecraft.get()),
            "jdk_version" to libs.versions.jdk.get(),
            "gh_hash" to ghHash
        )

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        val licenseSuffix = project.base.archivesName.get()
        from("LICENSE") {
            rename { "${it}_${licenseSuffix}" }
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get().toInt()))
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 25
    }
}

