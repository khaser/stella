plugins {
    java
    antlr
}

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.9.2")
}

tasks.test {
    useJUnitPlatform()
}
