plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

group = "io.github.lumine1909"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}