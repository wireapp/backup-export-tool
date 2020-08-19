import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.properties.saveToFile
import java.util.Properties

plugins {
    java
    kotlin("jvm") version "1.4.0"
    application
    id("net.nemerosa.versioning") version "2.8.2"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "com.wire.backups"
version = (versioning.info.tag ?: versioning.info.lastTag) +
        if (versioning.info.dirty) "-dirty" else ""

val mClass = "com.wire.backups.exports.Service"

repositories {
    jcenter()

    // lithium
    maven {
        url = uri("https://packagecloud.io/dkovacevic/lithium/maven2")
    }

    // transitive dependency for the lithium
    maven {
        url = uri("https://packagecloud.io/dkovacevic/cryptobox4j/maven2")
    }
}


dependencies {
    // ------- Java dependencies -------
    implementation("com.wire.bots", "lithium", "2.36.2") {
        // we're replacing it with newer version as the one included in Lithium has problems with JRE 11
        exclude("com.google.protobuf", "protobuf-java")
    }
    implementation("com.google.protobuf", "protobuf-java", "3.12.4")

    val atlassianVersion = "0.12.1"
    implementation("com.atlassian.commonmark", "commonmark", atlassianVersion)
    implementation("com.atlassian.commonmark", "commonmark-ext-autolink", atlassianVersion)

    val htmlToPdfVersion = "1.0.2"
    implementation("com.openhtmltopdf", "openhtmltopdf-core", htmlToPdfVersion)
    implementation("com.openhtmltopdf", "openhtmltopdf-pdfbox", htmlToPdfVersion)
    implementation("com.openhtmltopdf", "openhtmltopdf-svg-support", htmlToPdfVersion)

    implementation("com.github.spullara.mustache.java", "compiler", "0.9.5")

    // ------- Common dependencies -------
    implementation("net.lingala.zip4j", "zip4j", "2.6.1")

    // ------- Kotlin dependencies -------
    implementation(kotlin("stdlib-jdk8"))
    implementation("pw.forst.tools", "katlib", "1.0.0")

    // libsodium for decryption
    implementation("com.goterl.lazycode", "lazysodium-java", "4.3.0") {
        // otherwise the application won't start, the problem is combination of Dropwizard and sl4j 2.0
        exclude("org.slf4j", "slf4j-api")
    }
    implementation("net.java.dev.jna", "jna", "5.6.0")
    // logging
    implementation("io.github.microutils", "kotlin-logging", "1.7.9")
    // database
    val exposedVersion = "0.26.1"
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-java-time", exposedVersion)
    implementation("org.xerial", "sqlite-jdbc", "3.32.3")
    // jackson for kotlin
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.11.1")

    // testing
    val junitVersion = "5.6.2"
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))

    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
}

application {
    mainClassName = mClass
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<Test> {
        systemProperties["jna.library.path"] = "${projectDir}/libs"
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    shadowJar {
        mergeServiceFiles()
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to mClass
                )
            )
        }
        // because there's some conflict (LICENSE already exists) during the unzipping process
        // by excluding it from the shadow jar we try to fix problem on Oracle JVM 8
        exclude("LICENSE")
        // standard Dropwizard excludes
        exclude("META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.SF")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("backup-export.jar")
    }

    test {
        useJUnitPlatform()
    }

    classes {
        dependsOn("createVersionFile")
    }

    register("createVersionFile") {
        dependsOn(processResources)
        doLast {
            Properties().apply {
                setProperty("version", project.version.toString())
                saveToFile(File("$buildDir/resources/main/version.properties"))
            }
        }
    }

    register("resolveDependencies") {
        doLast {
            project.allprojects.forEach { subProject ->
                with(subProject) {
                    buildscript.configurations.forEach { if (it.isCanBeResolved) it.resolve() }
                    configurations.compileClasspath.get().resolve()
                    configurations.testCompileClasspath.get().resolve()
                }
            }
        }
    }
}
