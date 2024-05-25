plugins {
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"
	id("com.google.cloud.tools.jib") version "3.4.2"
	kotlin("plugin.spring") version "1.9.24"
	kotlin("jvm") version "1.9.24"
}

group = "example"
version = "1.0.0"

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.apache.kafka:kafka-clients:3.7.0")
	implementation("org.apache.kafka:kafka-streams:3.7.0")
	implementation ("io.micrometer:micrometer-registry-prometheus:1.12.5")

	// https://github.com/awslabs/aws-sdk-kotlin/issues/765
	implementation("aws.sdk.kotlin:s3:1.2.15") {
		constraints {
			implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14") {
				because("okhttp3 ~v4 does not support Request builder (kotlin reflect)")
			}
		}
	}
	configurations.all {
		// https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.ResolutionStrategy.html
		resolutionStrategy.eachDependency {
			if (requested.group == "com.squareup.okhttp3" && requested.name == "okhttp") {
				useVersion("5.0.0-alpha.14")
				because("okhttp3 ~v4 does not support Request builder (kotlin reflect) on AWS SDK")
			}
		}
	}

	implementation("org.slf4j:slf4j-api:2.0.12")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.apache.kafka:kafka-streams-test-utils:3.7.0")

	compileOnly("org.projectlombok:lombok:1.18.30")
	annotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

kotlin {
	jvmToolchain(17)
}




