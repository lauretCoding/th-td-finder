plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.github.lauretcoding"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("junit:junit:4.13.2")
}

intellij {
    version.set("2023.3.6")
    type.set("PS")
    plugins.set(listOf(
        "com.jetbrains.php",
        "com.jetbrains.twig",
        "org.jetbrains.plugins.yaml"
    ))
    updateSinceUntilBuild.set(true)
}

kotlin {
    jvmToolchain(17)
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    test {
        useJUnitPlatform()
    }

    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set("251.*")
        changeNotes.set("""
            <div>✨ First stable version</div>
            <div>🧭 Navigate between &lt;th&gt; et &lt;td&gt; in Twig</div>
        """.trimIndent())
    }

    runIde {
        jvmArgs = listOf(
            "-Xmx2048m",
            "-XX:ReservedCodeCacheSize=512m"
        )
    }

    buildSearchableOptions {
        enabled = false
    }
}