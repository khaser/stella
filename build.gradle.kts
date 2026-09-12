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
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
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
            srcDirs("src/main/java", "test/main/java", "build/generated-src/antlr/main")
        }
    }
}

tasks.test {
    useJUnitPlatform()
    exclude("**/TestSuiteV2Test*")
}

tasks.register<Test>("testSuiteV2") {
    description = "Runs test_suite_v2: well-typed (no exception) and ill-typed (exception with matching error code)"
    group = "verification"
    useJUnitPlatform()
    filter {
        includeTestsMatching("TestSuiteV2Test")
    }
    dependsOn(tasks.testClasses)
}
