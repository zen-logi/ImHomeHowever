plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.0.3"
    kotlin("plugin.serialization") version "2.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "net.bbldvw"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:3.0.3")
    implementation("io.ktor:ktor-server-netty-jvm:3.0.3")
    implementation("io.ktor:ktor-server-call-logging-jvm:3.0.3")
    implementation("io.ktor:ktor-server-status-pages-jvm:3.0.3")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.0.3")
    implementation("io.ktor:ktor-client-cio-jvm:3.0.3")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:3.0.3")
    implementation("io.ktor:ktor-client-serialization-jvm:3.0.3")
    implementation("io.insert-koin:koin-ktor:4.2.0-RC1")

    // AWS SDK
    implementation(platform("software.amazon.awssdk:bom:2.29.0"))
    implementation("software.amazon.awssdk:sqs")
    implementation("software.amazon.awssdk:lambda")
    implementation("software.amazon.awssdk:url-connection-client")

    implementation("net.dv8tion:JDA:6.3.0") {
        exclude(module = "opus-java")
    }
    implementation("org.jsoup:jsoup:1.17.2")

    // --- ログ出力 ---
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.insert-koin:koin-logger-slf4j:4.2.0-RC1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation("io.ktor:ktor-server-test-host-jvm:3.0.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.0")
}

kotlin {
    jvmToolchain(21)
}

ktlint {
    android.set(false)

    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("net.bbldvw.imhomehowever.ApplicationKt")
}
