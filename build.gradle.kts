import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.71"
    id("java-library")

    maven
    id("maven-publish")
}

group = "dev.drzepka.smarthome"
version = "1.3.0"

java.sourceCompatibility = JavaVersion.VERSION_1_8
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()

    maven {
        setupSmartHomeRepo("https://gitlab.com/api/v4/projects/21177602/packages/maven", false)
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("dev.drzepka.smarthome:common:1.0.+")

    implementation("com.fasterxml.jackson.core:jackson-core:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.12")
    implementation("com.intelligt.modbus:jlibmodbus:1.2.9.7")

    testImplementation("org.junit.platform:junit-platform-launcher:1.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "dev.drzepka.smarthome.logger.PVStatsDataLogger"
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