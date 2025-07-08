plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-rc1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

group = "io.github.lumine1909"
version = "2.0.0"
description = "A experimental season plugin for paper 1.21.7"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.7-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}


tasks {
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveFileName.set("SeasonsReload-${version}-MC-1.21.7.jar")
        minimize()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to "1.21"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
