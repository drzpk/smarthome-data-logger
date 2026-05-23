import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    id("java-library")
    id("maven-publish")
}

group = "dev.drzepka.smarthome"
version = "1.5.2"

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        setupSmartHomeRepo("https://gitlab.com/api/v4/projects/21177602/packages/maven", false)
    }

    maven("https://maven.google.com")
}

dependencies {
    implementation(libs.kotlin.reflect)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.jackson)

    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.httpclient)
    implementation(libs.jlibmodbus)
    implementation(libs.diozero.core)
    implementation(libs.logback.classic)
    implementation(libs.koin.core)

    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

allOpen {
    annotation("dev.drzepka.smarthome.logger.core.util.Mockable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "dev.drzepka.smarthome.logger.DataLogger"
        attributes["Implementation-Version"] = project.version.toString()
        attributes["Implementation-Title"] = "Smart Home data logger"
    }

    archiveBaseName.set("data-logger")
    archiveVersion.set(project.version.toString())

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            setupSmartHomeRepo("https://gitlab.com/api/v4/projects/21270113/packages/maven", true)
        }
    }
}

fun MavenArtifactRepository.setupSmartHomeRepo(repoUrl: String, publishing: Boolean) {
    setUrl(repoUrl)
    credentials(HttpHeaderCredentials::class) {
        val ciToken = System.getenv("CI_JOB_TOKEN")
        val privateToken = findProperty("gitLabPrivateToken") as String? // from ~/.gradle/gradle.properties

        when {
            ciToken != null -> {
                name = "Job-Token"
                value = ciToken
            }
            privateToken != null -> {
                name = "Private-Token"
                value = privateToken
            }
            else -> {
                val suffix = if (publishing) "publishing will fail" else "Smart Home dependencies cannot be downloaded"
                logger.warn("Neither job nor private token were defined, $suffix")
            }
        }
    }
    authentication {
        create<HttpHeaderAuthentication>("header")
    }
}
