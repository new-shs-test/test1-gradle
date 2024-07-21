import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.4"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	id("org.owasp.dependencycheck") version "9.0.9"
}

group = "com.isel"
version = "0.0.1"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	//JDBI
	implementation("org.jdbi:jdbi3-core:3.37.1")
	implementation("org.jdbi:jdbi3-kotlin:3.37.1")
	implementation("org.jdbi:jdbi3-postgres:3.37.1")
	implementation("org.postgresql:postgresql:42.5.4")

	// To use Kotlin specific date and time functions
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

	// To get password encode
	implementation("org.springframework.security:spring-security-core:6.0.2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	// To use WebTestClient on tests
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

dependencyCheck {
	nvd.apiKey = "3fd85911-12f9-474d-8081-a52d69747465"
}

subprojects{
	apply(plugin = "org.owasp.dependencycheck")
}
