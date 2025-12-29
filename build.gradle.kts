plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.project.rainmind"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// mysql
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")
	runtimeOnly("com.mysql:mysql-connector-j")
	implementation("org.flywaydb:flyway-core")
	implementation("org.springframework.security:spring-security-crypto")
	// redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// test
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")

	// swagger io
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

	// jwt
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	// test
	testImplementation("org.testcontainers:testcontainers")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

springBoot {
	mainClass.set("com.project.rainmind.DemoApplicationKt")
}
