plugins {
    java
    id("com.gradleup.shadow") version "9.3.2"
    id("io.github.intisy.github-gradle") version "1.8.2.1"
}

group = "dev.j3fftw"
description = "LiteXpansion is a Slimefun addon inspired by Industrial Craft 2."

apply(from = "https://raw.githubusercontent.com/Slimefun5/gradle/main/slimefun-addon.gradle")

dependencies {
    githubImplementation("Slimefun5:SlimefunMetrics:v1.0.0")
    implementation("org.bstats:bstats-bukkit:2.2.1")
}

tasks {
    shadowJar {
        relocate("org.bstats", "litexpansion.libs.bstats")
        relocate("dev.j3fftw.extrautils", "dev.j3fftw.litexpansion.extrautils")
    }
}
