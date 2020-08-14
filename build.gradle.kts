plugins {
    java
    `java-library`
    application
    distribution
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

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
    implementation("pw.forst.wire:backups:3.2.1")
    implementation("com.wire.bots:lithium:2.36.2")
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha5")
    implementation("com.github.spullara.mustache.java:compiler:0.9.5")
    implementation("com.atlassian.commonmark:commonmark:0.12.1")
    implementation("com.atlassian.commonmark:commonmark-ext-autolink:0.12.1")
    implementation("org.jsoup:jsoup:1.8.3")
    implementation("com.openhtmltopdf:openhtmltopdf-core:1.0.2")
    implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.2")
    implementation("com.openhtmltopdf:openhtmltopdf-svg-support:1.0.2")
    implementation("net.lingala.zip4j:zip4j:2.6.1")
    testImplementation("junit:junit:4.11")
}

group = "com.wire.backups"
version = "0.3.0"

val mClass = "com.wire.backups.exports.Service"

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
        exclude("LICENCE", "licence", "LICENSE", "license")
        exclude("META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.SF")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("backup-export.jar")
    }

    register<Jar>("fatJar") {
        manifest {
            attributes["Main-Class"] = mClass
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("backup-export-fat.jar")
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        from(sourceSets.main.get().output)
    }
}
