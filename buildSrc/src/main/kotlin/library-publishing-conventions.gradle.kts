plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "com.github.martinkoster"
val versionSuffix = if (isOnCIServer()) "ci" else "local"
version = "0.0.1-$versionSuffix"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://https://github.com/martinkoster/actionfx")
                inceptionYear.set("2020")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("martinkoster")
                        name.set("Martin Koster")
                    }
                }
                scm {
                    connection.set("scm:https://github.com/martinkoster/actionfx.git")
                    developerConnection.set("scm:git://github.com/martinkoster/actionfx.git")
                    url.set("https://https://github.com/martinkoster/actionfx")
                }
            }
        }
    }
}

signing {
    setRequired { !project.version.toString().endsWith("-SNAPSHOT") && !project.hasProperty("skipSigning") }
    if (project.hasProperty("signingKey")) {
        useInMemoryPgpKeys(properties["signingKey"].toString(), properties["signingPassword"].toString())
    } else {
        useGpgCmd()
    }
    sign(publishing.publications["mavenJava"])
}

fun isOnCIServer() = System.getenv("CI") == "true"
