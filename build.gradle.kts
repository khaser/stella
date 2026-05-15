plugins {
    java
    antlr
    application
}

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.1")
    implementation("org.antlr:antlr4-runtime:4.13.1")
}

application {
    mainClass.set("Main")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
}

tasks.compileJava {
    dependsOn(tasks.generateGrammarSource)
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java", "build/generated-src/antlr/main")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
