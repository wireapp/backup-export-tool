import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.properties.saveToFile
import java.util.Properties

plugins {
    java
    kotlin("jvm") version "1.4.31"
    application
    id("net.nemerosa.versioning") version "2.14.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.wire.backups"
version = (versioning.info.tag ?: versioning.info.lastTag) +
        if (versioning.info.dirty) "-dirty" else ""

val mClass = "com.wire.backups.exports.Service"

repositories {
    jcenter()

    // lithium
    maven {
        url = uri("https://packagecloud.io/dkovacevic/helium/maven2")
    }

    maven {
        url = uri("https://packagecloud.io/dkovacevic/xenon/maven2")
    }

    // transitive dependency for the lithium
    maven {
        url = uri("https://packagecloud.io/dkovacevic/cryptobox4j/maven2")
    }
}


dependencies {
    // ------- Java dependencies -------
    implementation("com.wire", "helium", "1.0-SNAPSHOT")
    implementation("org.glassfish.jersey.inject", "jersey-hk2", "2.32")
    implementation("org.glassfish.jersey.media", "jersey-media-json-jackson", "2.32")
    implementation("javax.activation", "activation", "1.1.1")

    implementation("info.picocli", "picocli", "4.6.1")

    val atlassianVersion = "0.17.1"
    implementation("org.commonmark", "commonmark", atlassianVersion)
    implementation("org.commonmark", "commonmark-ext-autolink", atlassianVersion)

    val htmlToPdfVersion = "1.0.6"
    implementation("com.openhtmltopdf", "openhtmltopdf-core", htmlToPdfVersion)
    implementation("com.openhtmltopdf", "openhtmltopdf-pdfbox", htmlToPdfVersion)
    implementation("com.openhtmltopdf", "openhtmltopdf-svg-support", htmlToPdfVersion)

    implementation("com.github.spullara.mustache.java", "compiler", "0.9.7")

    // ------- Common dependencies -------
    implementation("net.lingala.zip4j", "zip4j", "2.6.1")

    // ------- Kotlin dependencies -------
    implementation("pw.forst.tools", "katlib", "1.2.1")

    // libsodium for decryption
    implementation("com.goterl.lazycode", "lazysodium-java", "4.3.4") {
        // otherwise the application won't start, the problem is combination of Dropwizard and sl4j 2.0
        exclude("org.slf4j", "slf4j-api")
    }
    implementation("net.java.dev.jna", "jna", "5.7.0")
    // logging
    implementation("io.github.microutils", "kotlin-logging", "2.0.6")
    // database
    val exposedVersion = "0.29.1"
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-java-time", exposedVersion)
    implementation("org.xerial", "sqlite-jdbc", "3.34.0")
    // jackson for kotlin
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.11.1")
    // correct reflect lib until jackson fixes theirs
    implementation("org.jetbrains.kotlin", "kotlin-reflect", "1.4.31")

    // testing
    val junitVersion = "5.7.1"
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))

    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
}

application {
    mainClass.set(mClass)
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
            attributes(mapOf("Main-Class" to mClass))
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
