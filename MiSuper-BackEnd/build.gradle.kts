plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
}

group = "com.misuper"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.misuper.backend.Application")
}

repositories {
    mavenCentral()
}

val ktorVersion = "3.4.2"
val exposedVersion = "1.0.0"
val serializationVersion = "1.8.0"
val postgresVersion = "42.7.5"
val logbackVersion = "1.5.16"
val bcryptVersion = "0.10.2"
val hoconVersion = "1.4.3"
val hikariVersion = "6.3.0"
val flywayVersion = "10.20.1"

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-rate-limit:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    implementation("at.favre.lib:bcrypt:$bcryptVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("com.typesafe:config:$hoconVersion")



    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation(kotlin("test"))
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    workingDir = project.rootDir
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.2.0")
        force("org.jetbrains.kotlin:kotlin-reflect:2.2.0")
        force("org.jetbrains.kotlin:kotlin-test:2.2.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-common:2.2.0")
    }
}
