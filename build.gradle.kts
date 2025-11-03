import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mockkVersion = "1.14.5"
val tokenValidationVersion = "5.0.35"
val logstashVersion = "8.1"
val unleashVersion = "4.4.1"
val springRetryVersion = "2.0.12"
val springMockkVersion = "4.0.2"
val springDocVersion = "2.8.12"
val testContainersVersion = "1.21.3"
val ehcacheVersion = "3.11.1"
val kodeverkVersion = "1.12.5"
val shedlockVersion = "6.10.0"

plugins {
    val kotlinVersion = "2.2.10"
    id("org.springframework.boot") version "3.5.5"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    idea
}

apply(plugin = "io.spring.dependency-management")

java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("javax.cache:cache-api")
    implementation("org.ehcache:ehcache:$ehcacheVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.zaxxer:HikariCP")
    implementation("org.postgresql:postgresql")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("ch.qos.logback:logback-classic")

    implementation("no.nav.klage:klage-kodeverk:$kodeverkVersion")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")

    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")
    implementation("no.nav.security:token-client-spring:$tokenValidationVersion")

    implementation("org.springframework.retry:spring-retry:$springRetryVersion")
    implementation("no.finn.unleash:unleash-client-java:$unleashVersion")

    implementation("net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
        exclude(group = "org.mockito")
    }
    testImplementation("org.springframework.kafka:spring-kafka-test")

    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")

}

idea {
    module {
        isDownloadJavadoc = true
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}
