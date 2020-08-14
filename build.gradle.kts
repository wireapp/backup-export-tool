plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "com.wire.backups"
version = "0.3.0"

val mClass = "com.wire.backups.exports.Service"

repositories {
    mavenLocal()
    jcenter()

    // lithium
    maven {
        url = uri("https://packagecloud.io/dkovacevic/lithium/maven2")
    }

    // transitive dependency for the lithium
    maven {
        url = uri("https://packagecloud.io/dkovacevic/cryptobox4j/maven2")
    }

    // stuff
    maven {
        url = uri("https://dl.bintray.com/lukas-forst/jvm-packages")
    }
}


dependencies {
    implementation("pw.forst.wire:backups:3.2.1") {
        // otherwise the application won't start, the problem of Dropwizard
        exclude("org.slf4j", "slf4j-api")
    }

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
    implementation("net.lingala.zip4j", "zip4j", "2.6.1")

    val junitVersion = "5.6.2"
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
}

application {
    mainClassName = mClass
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
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
        exclude("LICENSE")
        // standard Dropwizard excludes
        exclude("META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.SF")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("backup-export.jar")
    }

    test {
        useJUnitPlatform()
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
