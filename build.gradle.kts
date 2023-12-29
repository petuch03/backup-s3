plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0" // For creating an executable JAR
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.268")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("org.apache.commons:commons-compress:1.21") // For compression

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
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
    archiveFileName.set("app.jar")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}