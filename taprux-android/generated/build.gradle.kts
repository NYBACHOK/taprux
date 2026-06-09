plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    `java-library`
}

group = "com.ghuba.taprux.core"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {}

tasks.withType<Jar> {
    manifest {
        attributes["Implementation-Title"] = "com.ghuba.taprux.core"
        attributes["Implementation-Version"] = "1.0.0"
    }
}
