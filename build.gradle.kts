import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("plugin.allopen") version "1.4.20"
    id("java-library")

    maven
    id("maven-publish")
}

group = "dev.drzepka.smarthome"
version = "1.5.2"

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
    implementation("dev.drzepka.smarthome:common:1.1.+")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.20")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("io.ktor:ktor-client-core:1.4.3")
    implementation("io.ktor:ktor-client-apache:1.4.3")
    implementation("io.ktor:ktor-client-jackson:1.4.3")

    //implementation("com.fasterxml.jackson.core:jackson-core:2.11.0")
    //implementation("com.fasterxml.jackson.core:jackson-databind:2.11.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.12")
    implementation("com.intelligt.modbus:jlibmodbus:1.2.9.7")
    implementation("com.diozero:diozero-core:1.3.2")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    testImplementation("org.junit.platform:junit-platform-launcher:1.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.mockito:mockito-core:3.9.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.9.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

allOpen {
    annotation("dev.drzepka.smarthome.common.util.Mockable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict -opt-in=kotlin.RequiresOptIn")
        jvmTarget = "1.8"
    }
}

tasks.withType<Jar> {
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