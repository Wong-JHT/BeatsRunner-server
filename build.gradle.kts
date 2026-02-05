plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    application
}

group = "com.beatrunner"
version = "1.0.0"

application {
    mainClass.set("com.beatrunner.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    val ktorVersion = "2.3.12"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    
    // Ktor Client (for DeepSeek API)
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    
    // Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Database - Exposed ORM
    val exposedVersion = "0.47.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    
    // PostgreSQL Driver
    implementation("org.postgresql:postgresql:42.7.3")
    
    // Connection Pooling
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Redis Client
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")
    
    // Password Hashing
    implementation("org.mindrot:jbcrypt:0.4")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.3")
    
    // DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    // Environment Variables
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    
    // Testing
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.10")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
