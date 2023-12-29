plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.627")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    // deps below are to avoid warnings in using Java 9+ SDK
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.glassfish.jaxb:jaxb-core:3.0.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:3.0.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("petuch03.backups3.AppKt")
}

tasks.named<Jar>("jar") {
    dependsOn("test")

    manifest {
        attributes["Main-Class"] = "petuch03.backups3.AppKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    archiveFileName.set("s3-backup-tool.jar")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}