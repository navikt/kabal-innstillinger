import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mockkVersion = "1.13.1"
val tokenValidationVersion = "2.1.4"
val logstashVersion = "7.2"
val springSleuthVersion = "3.1.4"
val unleashVersion = "4.4.1"
val problemSpringWebStartVersion = "0.27.0"
val springRetryVersion = "1.3.3"
val springMockkVersion = "3.1.1"
val springDocVersion = "1.6.11"
val testContainersVersion = "1.17.3"

plugins {
    id("org.springframework.boot") version "2.7.4"
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.spring") version "1.7.10"
    kotlin("plugin.jpa") version "1.7.10"
    idea
}

apply(plugin = "io.spring.dependency-management")

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
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
    implementation("org.ehcache:ehcache")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.flywaydb:flyway-core")
    implementation("com.zaxxer:HikariCP")
    implementation("org.postgresql:postgresql")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("ch.qos.logback:logback-classic")

    implementation("com.github.navikt:klage-kodeverk:1.1.1")

    implementation("org.springframework.cloud:spring-cloud-starter-sleuth:$springSleuthVersion")
    implementation("org.springdoc:springdoc-openapi-ui:$springDocVersion")

    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")
    implementation("no.nav.security:token-client-spring:$tokenValidationVersion")

    implementation("org.springframework.retry:spring-retry:$springRetryVersion")
    implementation("no.finn.unleash:unleash-client-java:$unleashVersion")
    implementation("org.zalando:problem-spring-web-starter:$problemSpringWebStartVersion")

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
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
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
