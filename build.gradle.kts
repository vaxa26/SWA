@file:Suppress("MissingPackageDeclaration", "SpellCheckingInspection", "GrazieInspection")

/*
* Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

//  Aufrufe
//  1) Microservice uebersetzen und starten
//        .\gradlew bootRun [--args='--debug']
//        .\gradlew compileJava
//        .\gradlew compileTestJava
//
//  2) Microservice als selbstausfuehrendes JAR oder Docker-Image erstellen und ausfuehren
//        .\gradlew bootJar
//        java -jar build/libs/....jar
//        .\gradlew bootBuildImage [-Pbuildpack=azul-zulu|-Pbuildpack=bellsoft]
//
//  3) Tests und Codeanalyse
//        .\gradlew test jacocoTestReport [-Dtest=rest-get] [--rerun-tasks]
//        .\gradlew jacocoTestCoverageVerification
//        .\gradlew allureServe
//              EINMALIG>>   .\gradlew downloadAllure
//        .\gradlew checkstyleMain checkstyleTest spotbugsMain spotbugsTest spotlessApply modernizer
//        .\gradlew sonar
//        .\gradlew buildHealth
//        .\gradlew reason --id com.fasterxml.jackson.core:jackson-annotations:...
//
//  4) Sicherheitsueberpruefung durch OWASP Dependency Check und Snyk
//        .\gradlew dependencyCheckAnalyze --info
//        .\gradlew snyk-test
//
//  5) "Dependencies Updates"
//        .\gradlew versions
//        .\gradlew dependencyUpdates
//        .\gradlew checkNewVersions
//
//  6) API-Dokumentation erstellen
//        .\gradlew javadoc
//
//  7) Projekthandbuch erstellen
//        .\gradlew asciidoctor asciidoctorPdf
//
//  8) Projektreport erstellen
//        .\gradlew projectReport
//        .\gradlew dependencyInsight --dependency jakarta.persistence-api
//        .\gradlew dependencies
//        .\gradlew dependencies --configuration runtimeClasspath
//        .\gradlew buildEnvironment
//        .\gradlew htmlDependencyReport
//
//  9) Report ueber die Lizenzen der eingesetzten Fremdsoftware
//        .\gradlew generateLicenseReport
//
//  10) Daemon stoppen
//        .\gradlew --stop
//
//  11) Verfuegbare Tasks auflisten
//        .\gradlew tasks
//
//  12) "Dependency Verification"
//        .\gradlew --write-verification-metadata pgp,sha256 --export-keys
//
//  13) Native Compilation mit Spring AOT (= Ahead Of Time) in einer Eingabeaufforderung
//        "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\VC\Auxiliary\Build\vcvars64.bat"
//        .\gradlew nativeCompile
//        .\build\native\nativeCompile\kunde.exe --spring.profiles.active=dev --logging.file.name=.\build\log\application.log
//        .\build\native\nativeCompile\kunde.exe --spring.datasource.url=jdbc:h2:mem:testdb --spring.datasource.username=sa --spring.datasource.password="" --logging.file.name=.\build\log\application.log
//
//  14) Initialisierung des Gradle Wrappers in der richtigen Version
//      dazu ist ggf. eine Internetverbindung erforderlich
//        gradle wrapper --gradle-version=8.8-rc-2 --distribution-type=bin

// https://github.com/gradle/kotlin-dsl/tree/master/samples
// https://docs.gradle.org/current/userguide/kotlin_dsl.html
// https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
// https://guides.gradle.org/migrating-build-logic-from-groovy-to-kotlin

import java.nio.file.Paths
import net.ltgt.gradle.errorprone.errorprone

val javaLanguageVersion = project.properties["javaLanguageVersion"] as String? ?: JavaVersion.VERSION_22.majorVersion
val javaVersion = project.properties["javaVersion"] ?: libs.versions.javaVersion.get()

// alternativ:   project.findProperty("...")
val imagePath = project.properties["imagePath"] ?: "juergenzimmermann"
val enablePreview = if (project.properties["enablePreview"] == "false" || project.properties["enablePreview"] == "FALSE") null else "--enable-preview"
val tracePinnedThreads = project.properties["tracePinnedThreads"] == "true" || project.properties["tracePinnedThreads"] == "TRUE"
val alternativeBuildpack = project.properties["buildpack"]

val mapStructVerbose = project.properties["mapStructVerbose"] == "true" || project.properties["mapStructVerbose"] == "TRUE"
val useTracing = project.properties["tracing"] != "false" && project.properties["tracing"] != "FALSE"
val useDevTools = project.properties["devTools"] != "false" && project.properties["devTools"] != "FALSE"
val activeProfiles = if (project.properties["https"] != "false" && project.properties["https"] != "FALSE") "dev" else "dev,http"

plugins {
    java
    idea
    checkstyle
    jacoco
    `project-report`

    // https://plugins.gradle.org

    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#reacting-to-other-plugins.java
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#packaging-executable
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image
    id("org.springframework.boot") version libs.versions.springBootPlugin.get()

    // Spring AOT: Kommentar entfernen
    //id("org.graalvm.buildtools.native") version libs.versions.graalvm.get()

    // https://github.com/tbroyer/gradle-errorprone-plugin
    // https://errorprone.info/docs/installation
    id("net.ltgt.errorprone") version libs.versions.errorpronePlugin.get()

    // https://cyclonedx.org
    // https://github.com/CycloneDX/cyclonedx-gradle-plugin
    // https://github.com/spring-projects/spring-boot/pull/39799
    id("org.cyclonedx.bom") version libs.versions.cyclonedx.get()

    // https://spotbugs.readthedocs.io/en/latest/gradle.html
    id("com.github.spotbugs") version libs.versions.spotbugsPlugin.get()

    // https://github.com/diffplug/spotless
    id("com.diffplug.spotless") version libs.versions.spotless.get()

    // https://github.com/andygoossens/gradle-modernizer-plugin
    id("com.github.andygoossens.modernizer") version libs.versions.modernizerPlugin.get()

    // https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner-for-gradle
    id("org.sonarqube") version libs.versions.sonarqube.get()

    // https://github.com/radarsh/gradle-test-logger-plugin
    id("com.adarshr.test-logger") version libs.versions.testLogger.get()

    // https://github.com/allure-framework/allure-gradle
    // https://docs.qameta.io/allure/#_gradle_2
    // TODO "The Project.getConvention() method has been deprecated."
    // TODO https://youtrack.jetbrains.com/issue/IDEA-320266 Project.getConvention()
    //  io.qameta.allure.gradle.adapter.AllureAdapterBasePlugin
    //  io.qameta.allure.gradle.adapter.AllureAdapterExtension
    //  io.qameta.allure.gradle.adapter.AllureAdapterPlugin
    //  io.qameta.allure.gradle.report.AllureReportBasePlugin
    //  io.qameta.allure.gradle.report.AllureReportExtension
    //  io.qameta.allure.gradle.download.AllureDownloadPlugin
    id("io.qameta.allure") version libs.versions.allurePlugin.get()

    // https://github.com/boxheed/gradle-sweeney-plugin
    id("com.fizzpod.sweeney") version libs.versions.sweeney.get()

    // https://github.com/jeremylong/dependency-check-gradle
    id("org.owasp.dependencycheck") version libs.versions.owaspDependencycheck.get()

    // https://github.com/snyk/gradle-plugin
    id("io.snyk.gradle.plugin.snykplugin") version libs.versions.snyk.get()

    // https://github.com/asciidoctor/asciidoctor-gradle-plugin
    id("org.asciidoctor.jvm.convert") version libs.versions.asciidoctor.get()
    id("org.asciidoctor.jvm.pdf") version libs.versions.asciidoctor.get()
    // Leanpub als Alternative zu PDF: https://github.com/asciidoctor/asciidoctor-leanpub-converter

    // https://github.com/nwillc/vplugin
    // Aufruf: gradle versions
    id("com.github.nwillc.vplugin") version libs.versions.nwillcVPlugin.get()

    // https://github.com/ben-manes/gradle-versions-plugin
    // Aufruf: gradle dependencyUpdates
    id("com.github.ben-manes.versions") version libs.versions.benManesVersions.get()

    // https://github.com/markelliot/gradle-versions
    // Aufruf: gradle checkNewVersions
    id("com.markelliot.versions") version libs.versions.markelliotVersions.get()

    // https://github.com/jk1/Gradle-License-Report
    id("com.github.jk1.dependency-license-report") version libs.versions.licenseReport.get()

    // https://github.com/gradle-dependency-analyze/gradle-dependency-analyze
    // https://github.com/jaredsburrows/gradle-license-plugin
    // https://github.com/hierynomus/license-gradle-plugin
}

defaultTasks = mutableListOf("compileTestJava")
group = "com.acme"
version = "2024.04.0"
val imageTag = project.properties["imageTag"] ?: project.version.toString()

sweeney {
    enforce(mapOf("type" to "gradle", "expect" to "[8.8,8.8]"))
    // https://www.java.com/releases
    // https://devcenter.heroku.com/articles/java-support#specifying-a-java-version
    enforce(mapOf("type" to "jdk", "expect" to "[${javaVersion},${javaVersion}]"))
    validate()
}

// https://docs.gradle.org/current/userguide/java_plugin.html#sec:java-extension
// https://docs.gradle.org/current/dsl/org.gradle.api.plugins.JavaPluginExtension.html
java {
    // https://docs.gradle.org/current/userguide/toolchains.html : gradle -q javaToolchains
    // GraalVM unterstuetzt nicht toolchain: deshalb auskommentieren sowie Kommentare bei sourceCompatibility und targetCompatibility entfernen
    toolchain {
        // einschl. sourceCompatibility und targetCompatibility
        languageVersion = JavaLanguageVersion.of(javaLanguageVersion)
    }
    // sourceCompatibility = JavaVersion.VERSION_22
    // targetCompatibility = sourceCompatibility
}

repositories {
    mavenCentral()

    // https://github.com/spring-projects/spring-framework/wiki/Spring-repository-FAQ
    // https://github.com/spring-projects/spring-framework/wiki/Release-Process
    maven("https://repo.spring.io/milestone")

    // Snapshots von Spring Framework, Spring Boot, Spring Data, Spring Security, Spring for GraphQL, ...
    //maven("https://repo.spring.io/snapshot") { mavenContent { snapshotsOnly() } }

    // Snapshots von Lombok: https://projectlombok.org/download-edge
    if (libs.versions.lombok.get() == "edge-SNAPSHOT") {
        maven("https://projectlombok.org/edge-releases") { mavenContent { snapshotsOnly() } }
    }

    // Snapshots von Hibernate
    //maven("https://oss.sonatype.org/content/repositories/snapshots") { mavenContent { snapshotsOnly() } }

    // Snapshots von springdoc-openapi
    if (libs.versions.springdocOpenapi.get().endsWith("-SNAPSHOT")) {
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots") { mavenContent { snapshotsOnly() } }
    }

    // Snapshots von JaCoCo
    if (libs.versions.jacoco.get().endsWith("-SNAPSHOT")) {
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            mavenContent { snapshotsOnly() }
            // https://docs.gradle.org/current/userguide/jacoco_plugin.html#sec:jacoco_dependency_management
            content { onlyForConfigurations("jacocoAgent", "jacocoAnt") }
        }
    }
}

// aktuelle Snapshots laden
//configurations.all {
//    resolutionStrategy { cacheChangingModulesFor(0, "seconds") }
//}

//configurations.all {
//    resolutionStrategy {
//        force(
//            "org.asciidoctor:asciidoctorj-pdf:${libs.versions.asciidoctorjPdf.get()}",
//            "org.jruby:jruby-complete:${libs.versions.jruby.get()}"
//        )
//    }
//}

// https://github.com/gradle/gradle/issues/27035#issuecomment-1814589243
// https://github.com/google/guava/releases/tag/v32.1.0
configurations.checkstyle {
    resolutionStrategy.capabilitiesResolution.withCapability("com.google.collections:google-collections") {
        select("com.google.guava:guava:0")
    }
}

/* ktlint-disable comment-spacing */
@Suppress("CommentSpacing")
// https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_separation
dependencies {
    implementation(platform("org.slf4j:slf4j-bom:${libs.versions.slf4j.get()}"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:${libs.versions.log4j2.get()}"))
    //implementation(platform("io.zipkin.reporter2:zipkin-reporter-bom:${libs.versions.zipkinReporter.get()}"))
    //implementation(platform("io.zipkin.brave:brave-bom:${libs.versions.brave.get()}"))
    //implementation(platform("io.micrometer:micrometer-tracing-bom:${libs.versions.micrometerTracing.get()}"))
    implementation(platform("io.prometheus:prometheus-metrics-bom:${libs.versions.prometheusMetrics.get()}"))
    //implementation(platform("io.prometheus:simpleclient_bom:${libs.versions.prometheusMetrics.get()}"))
    //implementation(platform("io.micrometer:micrometer-bom:${libs.versions.micrometer.get()}"))
    //implementation(platform("com.fasterxml.jackson:jackson-bom:${libs.versions.jackson.get()}"))
    //implementation(platform("io.netty:netty-bom:${libs.versions.netty.get()}"))
    //implementation(platform("io.projectreactor:reactor-bom:${libs.versions.reactorBom.get()}"))
    implementation(platform("org.springframework:spring-framework-bom:${libs.versions.springFramework.get()}"))
    implementation(platform("com.oracle.database.jdbc:ojdbc-bom:${libs.versions.oracleDatabase.get()}"))
    //implementation(platform("org.springframework.data:spring-data-bom:${libs.versions.springData.get()}"))
    //implementation(platform("org.springframework.security:spring-security-bom:${libs.versions.springSecurity.get()}"))
    //implementation(platform("ch.qos.logback:logback-parent:${libs.versions.logback.get()}"))

    testImplementation(platform("org.assertj:assertj-bom:${libs.versions.assertj.get()}"))
    testImplementation(platform("org.mockito:mockito-bom:${libs.versions.mockito.get()}"))
    testImplementation(platform("org.junit:junit-bom:${libs.versions.junitJupiter.get()}"))
    testImplementation(platform("io.qameta.allure:allure-bom:${libs.versions.allureBom.get()}"))

    implementation(platform("org.springframework.boot:spring-boot-starter-parent:${libs.versions.springBoot.get()}")) // NOSONAR
    // spring-boot-starter-parent als "Parent POM"
    implementation(platform("org.springdoc:springdoc-openapi:${libs.versions.springdocOpenapi.get()}"))

    // https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#tooling-modelgen
    // https://docs.jboss.org/hibernate/orm/current/introduction/html_single/Hibernate_Introduction.html#generator
    // build\generated\sources\annotationProcessor\java\main\com.acme.kunde\entity\Kunde_.java
    annotationProcessor("org.hibernate.orm:hibernate-processor:${libs.versions.hibernateProcessor.get()}")
    //annotationProcessor("org.hibernate:hibernate-jpamodelgen:${libs.versions.hibernateJpamodelgen.get()}")
    annotationProcessor("org.projectlombok:lombok:${libs.versions.lombok.get()}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${libs.versions.mapstruct.get()}")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${libs.versions.lombokMapstructBinding.get()}")

    // "Starters" enthalten sinnvolle Abhaengigkeiten, die man i.a. benoetigt
    // spring-boot-starter-web verwendet spring-boot-starter, spring-boot-starter-tomcat, spring-boot-starter-json
    // spring-boot-starter verwendet spring-boot-starter-logging mit Logback
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.apache.tomcat.embed", module = "tomcat-embed-websocket")
    }
    // HttpGraphQlClient benoetigt WebClient mit Project Reactor
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    //implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")
    runtimeOnly("com.h2database:h2")

    // Flyway unterstuetzt nur Oracle 21 in der lizenzpflichtigen Version: https://documentation.red-gate.com/fd/oracle-184127602.html
    // org.flywaydb.core.internal.database.DatabaseTypeRegister.getDatabaseTypeForConnection()
    implementation("org.flywaydb:flyway-core")
    // https://documentation.red-gate.com/flyway/learn-more-about-flyway/system-requirements/supported-databases-for-flyway
    // https://documentation.red-gate.com/fd/postgresql-184127604.html
    // https://github.com/flyway/flyway/blob/main/flyway-core/src/main/java/org/flywaydb/core/internal/database/DatabaseTypeRegister.java#L99
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.flywaydb:flyway-database-oracle")
    // https://flywaydb.org/documentation/database/mysql#java-usage
    runtimeOnly("org.flywaydb:flyway-mysql")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("com.c4-soft.springaddons:spring-addons-starter-oidc:${libs.versions.springAddonsStarterOidc.get()}")
    // Passwort-Verschluesselung mit bcrypt oder Argon2:
    implementation("org.springframework.security:spring-security-crypto")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Tracing durch Micrometer und Visualisierung durch Zipkin
    if (useTracing) {
        println("")
        println("Tracing mit   Z i p k i n   aktiviert")
        println("")
        implementation("io.micrometer:micrometer-tracing-bridge-brave")
        implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    } else {
        println("")
        println("Tracing mit   Z i p k i n   d e a k t i v i e r t")
        println("")
    }

    // Metriken durch Micrometer und Visualisierung durch Prometheus/Grafana
    implementation("io.micrometer:micrometer-registry-prometheus")

    // https://docs.spring.io/spring-framework/reference/6.1/integration/checkpoint-restore.html
    // https://www.azul.com/blog/superfast-application-startup-java-on-crac
    // https://piotrminkowski.com/2023/09/05/speed-up-java-startup-on-kubernetes-with-crac
    // https://github.com/CRaC/example-spring-boot
    // https://github.com/sdeleuze/spring-boot-crac-demo
    //implementation("org.crac:crac:${libs.versions.crac.get()}")

    compileOnly("org.projectlombok:lombok")
    implementation("org.mapstruct:mapstruct:${libs.versions.mapstruct.get()}")

    // https://springdoc.org/v2/#swagger-ui-configuration
    // https://github.com/springdoc/springdoc-openapi
    // https://github.com/springdoc/springdoc-openapi-demos/wiki/springdoc-openapi-2.x-migration-guide
    // https://www.baeldung.com/spring-rest-openapi-documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    runtimeOnly("org.fusesource.jansi:jansi:${libs.versions.jansi.get()}")
    // Passwort-Verschluesselung mit Argon2:
    runtimeOnly("org.bouncycastle:bcpkix-jdk18on:${libs.versions.bouncycastle.get()}") // Argon2

    compileOnly("com.github.spotbugs:spotbugs-annotations:${libs.versions.spotbugs.get()}")
    testCompileOnly("com.github.spotbugs:spotbugs-annotations:${libs.versions.spotbugs.get()}")
    testImplementation("org.gaul:modernizer-maven-annotations:${libs.versions.modernizer.get()}")

    // https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-devtools
    if (useDevTools) {
        developmentOnly("org.springframework.boot:spring-boot-devtools:${libs.versions.springBoot.get()}")
    }
    // https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.docker-compose
    if (project.properties["dockerCompose"] == "true") {
        println("spring-boot-docker-compose aktiviert")
        developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    }

    // einschl. JUnit und Mockito
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.hamcrest", module = "hamcrest")
        exclude(group = "org.skyscreamer", module = "jsonassert")
        exclude(group = "org.xmlunit", module = "xmlunit-core")
        exclude(group = "org.awaitility", module ="awaitility")
    }
    testImplementation("org.junit.platform:junit-platform-suite-api:${libs.versions.junitPlatformSuite.get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:${libs.versions.junitPlatformSuite.get()}")
    //testImplementation("org.springframework.security:spring-security-test")
    // mock() fuer record
    testImplementation("org.mockito:mockito-inline:${libs.versions.mockitoInline.get()}")

    // https://github.com/tbroyer/gradle-errorprone-plugin
    // https://docs.gradle.org/8.4-rc-1/release-notes.html#easier-to-create-role-focused-configurations
    errorprone("com.google.errorprone:error_prone_core:${libs.versions.errorprone.get()}")

    constraints {
        //compileOnly("org.projectlombok:lombok:${libs.versions.lombok.get()}")

        implementation("org.jetbrains:annotations:${libs.versions.annotations.get()}")
        //implementation("org.apache.tomcat.embed:tomcat-embed-core:${libs.versions.tomcat.get()}")
        //implementation("org.apache.tomcat.embed:tomcat-embed-el:${libs.versions.tomcat.get()}")
        //implementation("com.graphql-java:java-dataloader:${libs.versions.graphqlJavaDataloader.get()}")
        implementation("com.graphql-java:graphql-java:${libs.versions.graphqlJava.get()}")
        implementation("jakarta.validation:jakarta.validation-api:${libs.versions.jakartaValidation.get()}")
        //implementation("org.hibernate.validator:hibernate-validator:${libs.versions.hibernateValidator.get()}")

        //implementation("org.springframework.hateoas:spring-hateoas:${libs.versions.springHateoas.get()}")
        //implementation("org.springframework.graphql:spring-graphql:${libs.versions.springGraphQL.get()}")

        //runtimeOnly("org.postgresql:postgresql:${libs.versions.postgresql.get()}")
        runtimeOnly("com.mysql:mysql-connector-j:${libs.versions.mysql.get()}")
        //runtimeOnly("com.h2database:h2:${libs.versions.h2.get()}")
        implementation("jakarta.persistence:jakarta.persistence-api:${libs.versions.jakartaPersistence.get()}")
        //implementation("com.zaxxer:HikariCP:${libs.versions.hikaricp.get()}") // NOSONAR
        implementation("org.hibernate.orm:hibernate-core:${libs.versions.hibernate.get()}")

        implementation("org.flywaydb:flyway-core:${libs.versions.flyway.get()}")
        runtimeOnly("org.flywaydb:flyway-database-postgresql:${libs.versions.flyway.get()}")
        runtimeOnly("org.flywaydb:flyway-database-oracle:${libs.versions.flyway.get()}")
        runtimeOnly("org.flywaydb:flyway-mysql:${libs.versions.flyway.get()}")

        //implementation("org.eclipse.angus:jakarta.mail:${libs.versions.angusMail.get()}")
        //implementation("org.yaml:snakeyaml:${libs.versions.snakeyaml.get()}")

        allureCommandline("io.qameta.allure:allure-commandline:${libs.versions.allureCommandline.get()}")
    }
}
/* ktlint-enable comment-spacing */

tasks.named<JavaCompile>("compileJava") {
    // https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.JavaCompile.html
    // https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.CompileOptions.html
    // https://dzone.com/articles/gradle-goodness-enabling-preview-features-for-java
    with(options) {
        isDeprecation = true
        with(compilerArgs) {
            if (enablePreview != null) {
                add(enablePreview)
            }

            // javac --help-lint
            add("-Xlint:all,-serial,-processing,-preview")

            // https://github.com/tbroyer/gradle-errorprone-plugin#jdk-16-support
            add("--add-opens")
            add("--add-exports")

            // https://mapstruct.org/documentation/stable/reference/html/#configuration-options
            if (mapStructVerbose) {
                add("-Amapstruct.verbose=true")
            }
            //add("-Amapstruct.unmappedTargetPolicy=ERROR")
            //add("-Amapstruct.unmappedSourcePolicy=ERROR")
        }

        // https://uber.github.io/AutoDispose/error-prone
        // https://errorprone.info/docs/flags
        // https://stackoverflow.com/questions/56975581/how-to-setup-error-prone-with-gradle-getting-various-errors
        errorprone.errorproneArgs.add("-Xep:MissingSummary:OFF")

        // ohne sourceCompatiblity und targetCompatibility:
        //release = javaLanguageVersion
    }

    // https://blog.gradle.org/incremental-compiler-avoidance#about-annotation-processors
}

tasks.named<JavaCompile>("compileTestJava") {
    // sourceCompatibility = javaLanguageVersion
    with(options) {
        isDeprecation = true
        with(compilerArgs) {
            if (enablePreview != null) {
                add(enablePreview)
            }
            // javac --help-lint
            add("-Xlint:all,-serial,-processing,-preview")
        }
        errorprone.errorproneArgs.add("-Xep:VariableNameSameAsType:OFF")
    }
}

tasks.named("bootJar", org.springframework.boot.gradle.tasks.bundling.BootJar::class.java) {
    // in src/main/resources/
    exclude("private-key.pem", "certificate.crt", ".reloadtrigger")

    doLast {
        // CDS = Class Data Sharing seit Spring Boot 3.3.0
        // https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.3.0-M3-Release-Notes#cds-support
        // https://docs.spring.io/spring-framework/reference/integration/cds.html
        println(
            """
            |
            |Aufruf der ausfuehrbaren JAR-Datei mit CDS (= Class Data Sharing):
            |java -Djarmode=tools -jar build/libs/${project.name}-${project.version}.jar extract
            |java -Djarmode=tools --enable-preview -jar ${project.name}-${project.version}/${project.name}-${project.version}.jar `
            |     --spring.profiles.active=dev `
            |     --spring.ssl.bundle.pem.microservice.keystore.private-key=./src/main/resources/private-key.pem `
            |     --spring.ssl.bundle.pem.microservice.keystore.certificate=./src/main/resources/certificate.crt `
            |     --spring.ssl.bundle.pem.microservice.truststore.certificate=./src/main/resources/certificate.crt [--debug]
            |
            |Aufruf der ausfuehrbaren JAR-Datei ohne CDS:
            |java --enable-preview -jar build/libs/${project.name}-${project.version}.jar `
            |     --spring.profiles.active=dev `
            |     --spring.ssl.bundle.pem.microservice.keystore.private-key=./src/main/resources/private-key.pem `
            |     --spring.ssl.bundle.pem.microservice.keystore.certificate=./src/main/resources/certificate.crt `
            |     --spring.ssl.bundle.pem.microservice.truststore.certificate=./src/main/resources/certificate.crt [--debug]
            """.trimMargin("|")
        )
    }
}

// Spring AOT: Kommentar entfernen
//tasks.named("processAot", org.springframework.boot.gradle.tasks.aot.ProcessAot::class.java) {
//    if (enablePreview != null) {
//        jvmArguments = if (jvmArguments.get().isEmpty()) {
//            listOf(enablePreview)
//        } else {
//            val args = jvmArguments.get().toMutableList()
//            args.add(enablePreview)
//            args
//        }
//    }
//}
//tasks.named<JavaCompile>("compileAotJava") {
//    with(options) {
//        with(compilerArgs) {
//            add("--add-opens")
//            add("--add-exports")
//            add("-Amapstruct.defaultComponentModel=spring")
//            if (enablePreview != null) {
//                add(enablePreview)
//            }
//        }
//        errorprone.errorproneArgs.add("-Xep:MissingSummary:OFF")
//    }
//}
//tasks.named("nativeCompile", org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask::class.java) {
//    if (enablePreview != null) {
//        options.get().asCompileOptions().jvmArgs.add("--enable-preview")
//    }
//}

// https://github.com/paketo-buildpacks/spring-boot
tasks.named("bootBuildImage", org.springframework.boot.gradle.tasks.bundling.BootBuildImage::class.java) {
    // statt "created xx years ago": https://medium.com/buildpacks/time-travel-with-pack-e0efd8bf05db
    createdDate = "now"

    // default:   imageName = "docker.io/${project.name}:${project.version}"
    imageName = "$imagePath/${project.name}:$imageTag"

    @Suppress("StringLiteralDuplication")
    environment = mapOf(
        // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image.examples.builder-configuration
        // https://github.com/paketo-buildpacks/bellsoft-liberica#configuration
        // https://github.com/paketo-buildpacks/bellsoft-liberica/blob/main/buildpack.toml: Umgebungsvariable und Ubuntu Jammy
        // https://releases.ubuntu.com: Jammy = 22.04
        // https://github.com/paketo-buildpacks/bellsoft-liberica/releases
        "BP_JVM_VERSION" to javaLanguageVersion, // default: 17
        // BPL = Build Packs Launch
        "BPL_JVM_THREAD_COUNT" to "20", // default: 250 (reactive: 50)
        // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image.examples.runtime-jvm-configuration
        "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
        "BPE_APPEND_JAVA_TOOL_OPTIONS" to enablePreview,
        // https://github.com/paketo-buildpacks/spring-boot#configuration
        // https://github.com/paketo-buildpacks/spring-boot/blob/main/buildpack.toml
        //"BP_SPRING_CLOUD_BINDINGS_DISABLED" to "true",
        //"BPL_SPRING_CLOUD_BINDINGS_DISABLED" to "true",
        //"BPL_SPRING_CLOUD_BINDINGS_ENABLED" to "false", // deprecated
        // https://paketo.io/docs/howto/configuration/#enabling-debug-logging
        //"BP_LOG_LEVEL" to "DEBUG",

        // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#reacting-to-other-plugins.nbt
        // https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html
        // https://github.com/spring-projects/spring-framework/blob/main/framework-docs/src/docs/asciidoc/core/core-aot.adoc
        //"BP_NATIVE_IMAGE" to "true" und paketobuildpacks/builder:tiny als Builder fuer "Native Image"
        //"BP_BOOT_NATIVE_IMAGE_BUILD_ARGUMENTS" to "-H:+ReportExceptionStackTraces",

        // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image.examples.publish
    )

    // https://paketo.io/docs/howto/java/#use-an-alternative-jvm
    when (alternativeBuildpack) {
        "azul-zulu" -> {
            // Azul Zulu: JRE 8, 11, 17 (default, siehe buildpack.toml: BP_JVM_VERSION), 21, 22
            // https://github.com/paketo-buildpacks/azul-zulu/releases
            buildpacks = listOf(
                "gcr.io/paketo-buildpacks/azul-zulu",
                //"paketobuildpacks/azul-zulu",
                "paketo-buildpacks/java",
            )
            imageName = "${imageName.get()}-azul"
            println("")
            println("Buildpacks: JVM durch   A z u l   Z u l u")
            println("")
        }
        "adoptium" -> {
            // Eclipse Temurin: JRE 8, 11, 17 (default, siehe buildpack.toml: BP_JVM_VERSION), 21, 22
            // https://github.com/paketo-buildpacks/adoptium/releases
            buildpacks = listOf(
                //"paketo-buildpacks/ca-certificates",
                "gcr.io/paketo-buildpacks/adoptium",
                "paketo-buildpacks/java",
            )
            imageName = "${imageName.get()}-eclipse"
            println("")
            println("Buildpacks: JVM durch   E c l i p s e   T e m u r i n")
            println("")
        }
        "sap-machine" -> {
            // SapMachine: JRE 11, 17 (default, siehe buildpack.toml: BP_JVM_VERSION), 21, 22
            // https://github.com/paketo-buildpacks/sap-machine/releases
            buildpacks = listOf(
                "gcr.io/paketo-buildpacks/sap-machine",
                "paketo-buildpacks/java",
            )
            imageName = "${imageName.get()}-sapmachine"
            println("")
            println("Buildpacks: JVM durch   S a p M a c h i n e")
            println("")
        }
        else -> {
            // Bellsoft Liberica: JRE 8, 11, 17 (default, siehe buildpack.toml: BP_JVM_VERSION), 21, 22
            // https://github.com/paketo-buildpacks/bellsoft-liberica/releases
            imageName = "${imageName.get()}-bellsoft"
            println("")
            println("Buildpacks: JVM durch   B e l l s o f t   L i b e r i c a   (default)")
            println("")

            // *kein* JRE, nur JDK:
            // Amazon Coretto     https://github.com/paketo-buildpacks/amazon-corretto/releases
            // Oracle             https://github.com/paketo-buildpacks/oracle/releases
            // Microsoft OpenJDK  https://github.com/paketo-buildpacks/microsoft-openjdk/releases
            // Alibaba Dragonwell https://github.com/paketo-buildpacks/alibaba-dragonwell/releases
        }
    }

    // Podman statt Docker
    // docker {
    //    host = "unix:///run/user/1000/podman/podman.sock"
    //    isBindHostToBuilder = true
    // }
}

// TODO https://github.com/spring-projects/spring-boot/issues/40074
tasks.named("resolveMainClassName", org.springframework.boot.gradle.plugin.ResolveMainClassName::class.java) {
    configuredMainClassName = "${project.group}.${project.name}.Application"
}

tasks.named("bootRun", org.springframework.boot.gradle.tasks.run.BootRun::class.java) {
    if (enablePreview != null) {
        jvmArgs(enablePreview)
    }

    // "System Properties", z.B. fuer Spring Properties oder fuer logback
    // https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties
    systemProperty("spring.profiles.active", activeProfiles)
    systemProperty("logging.file.name", "./build/log/application.log")
    // $env:TEMP\tomcat-docbase.* -> src\main\webapp (urspruengl. fuer WAR)
    // Document Base = Context Root, siehe https://tomcat.apache.org/tomcat-10.1-doc/config/context.html
    // $env:TEMP\hsperfdata_<USERNAME>\<PID> Java HotSpot Performance data log: bei jedem Start der JVM neu angelegt.
    // https://support.oracle.com/knowledge/Middleware/2325910_1.html
    // https://blog.mygraphql.com/zh/notes/java/diagnostic/hsperfdata/hsperfdata
    systemProperty("server.tomcat.basedir", "build/tomcat")
    systemProperty("keycloak.client-secret", project.properties["keycloak.client-secret"]!!)
    systemProperty("keycloak.issuer", project.properties["keycloak.issuer"]!!)
    //systemProperty("app.keycloak.host", project.properties["keycloak.host"]!!)

    if (tracePinnedThreads) {
        systemProperty("tracePinnedThreads", "full")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        includeTags = when (project.properties["test"]) {
            "all" -> setOf("integration", "unit")
            "integration" -> setOf("integration")
            "rest" -> setOf("rest")
            "rest-get" -> setOf("rest-get")
            "rest-write" -> setOf("rest-write")
            "graphql" -> setOf("graphql")
            "query" -> setOf("query")
            "mutation" -> setOf("mutation")
            "unit" -> setOf("unit")
            "service-read" -> setOf("service-read")
            "service-write" -> setOf("service-write")
            else -> setOf("integration", "unit")
        }
    }

    systemProperty("spring.profiles.active", activeProfiles)
    systemProperty("junit.platform.output.capture.stdout", true)
    systemProperty("junit.platform.output.capture.stderr", true)

    val logLevelTest = project.properties["logLevelTest"] ?: "INFO"
    // systemProperty("logging.level.com.acme", logLevelTest)
    systemProperty("logging.level.org.hibernate.SQL", logLevelTest)
    systemProperty("logging.level.org.hibernate.orm.jdbc.bind", logLevelTest)
    systemProperty("logging.level.org.flywaydb.core.internal.sqlscript.DefaultSqlScriptExecutor", logLevelTest)
    systemProperty("logging.level.org.springframework.web.reactive.function.client.ExchangeFunctions", logLevelTest)
    systemProperty("logging.level.org.springframework.web.service.invoker.PathVariableArgumentResolver", logLevelTest)
    systemProperty("logging.level.org.springframework.web.service.invoker.RequestHeaderArgumentResolver", logLevelTest)
    systemProperty("logging.level.org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor", logLevelTest)
    systemProperty("logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping", logLevelTest)

    // $env:TEMP\tomcat-docbase.* -> src\main\webapp (urspruengl. fuer WAR)
    // Document Base = Context Root, siehe https://tomcat.apache.org/tomcat-10.1-doc/config/context.html
    // $env:TEMP\hsperfdata_<USERNAME>\<PID> Java HotSpot Performance data log: bei jedem Start der JVM neu angelegt.
    // https://support.oracle.com/knowledge/Middleware/2325910_1.html
    // https://blog.mygraphql.com/zh/notes/java/diagnostic/hsperfdata/hsperfdata
    systemProperty("server.tomcat.basedir", "build/tomcat")
    systemProperty("keycloak.client-secret", project.properties["keycloak.client-secret"]!!)
    systemProperty("keycloak.issuer", project.properties["keycloak.issuer"]!!)
    //systemProperty("app.keycloak.host", project.properties["keycloak.host"]!!)

    if (enablePreview != null) {
        jvmArgs(enablePreview)
    }
    if (tracePinnedThreads) {
        systemProperty("tracePinnedThreads", "full")
    }

    if (project.properties["showTestStandardStreams"] == "true" || project.properties["showTestStandardStreams"] == "TRUE") {
        testLogging.showStandardStreams = true
    }

    extensions.configure(JacocoTaskExtension::class) {
        excludes = listOf("**/entity/*_.class", "**/dev/*.class")
    }

    // https://docs.gradle.org/current/userguide/java_testing.html#sec:debugging_java_tests
    // https://www.jetbrains.com/help/idea/run-debug-configuration-junit.html
    // https://docs.gradle.org/current/userguide/java_testing.html#sec:debugging_java_tests
    // debug = true

    // finalizedBy("jacocoTestReport")
}

// https://docs.qameta.io/allure/#_gradle_2
allure {
    // version = libs.versions.allure.get()
    adapter {
        frameworks {
            junit5 {
                adapterVersion = libs.versions.allureJunit.get()
                autoconfigureListeners = true
                enabled = true
            }
        }
        autoconfigure = true
        aspectjWeaver = false
        aspectjVersion = libs.versions.aspectjweaver.get()
    }

    // https://github.com/allure-framework/allure-gradle#customizing-allure-commandline-download
    //commandline {
    //  group = "io.qameta.allure"
    //  module = "allure-commandline"
    //  extension = "zip"
    //}
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

// https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
// https://guides.gradle.org/migrating-build-logic-from-groovy-to-kotlin/#configuring-tasks
tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required = true
        html.required = true
    }

    classDirectories.setFrom(classDirectories.files.map {
        fileTree(it).matching {
            exclude(listOf("**/entity/*_.class", "**/dev/*.class"))
        }
    })

    // https://docs.gradle.org/current/userguide/jacoco_plugin.html
    // https://github.com/gradle/gradle/pull/12626
    dependsOn(tasks.test)
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            limit { minimum = BigDecimal("0.7") }
        }
    }
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    isIgnoreFailures = false
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required = true
        html.required = true
    }
}

spotbugs {
    // https://github.com/spotbugs/spotbugs/releases
    toolVersion = libs.versions.spotbugs.get()
}
tasks.named("spotbugsMain", com.github.spotbugs.snom.SpotBugsTask::class.java) {
    reportLevel = com.github.spotbugs.snom.Confidence.LOW
    reports.create("html") { required = true }
    // val excludePath = File("config/spotbugs/exclude.xml")
    val excludePath = Paths.get("config", "spotbugs", "exclude.xml")
    excludeFilter = file(excludePath)
}

modernizer {
    toolVersion = libs.versions.modernizer.get()
    includeTestClasses = true
}

// https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner-for-gradle/#analyzing
// https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/languages/java
// https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/test-coverage/java-test-coverage/#gradle-project
sonarqube {
    properties {
        property("sonar.organization", "Softwarearchitektur und Microservices")
        property("sonar.projectDescription", "Beispiel fuer Softwarearchitektur")
        property("sonar.projectVersion", "2024.04.0")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.token", project.properties["sonarToken"]!!)
        property("sonar.scm.disabled", "true")
        property("sonar.exclusions", ".allure/**/*,.gradle/**/*,.idea/**/*,build/**/*,config/**/*,extras/**/*,gradle/**/*,src/test/java/**/*,target/*,tmp/**/*")
        property("sonar.java.source", javaLanguageVersion)
        property("sonar.java.enablePreview", "true")
        property("sonar.gradle.skipCompile", "true")

        println("")
        println("sonar.junit.reportPaths=${properties["sonar.junit.reportPaths"]}")
        println("sonar.coverage.jacoco.xmlReportPaths=${properties["sonar.coverage.jacoco.xmlReportPaths"]}")
        println("sonar.jacoco.reportPath=${properties["sonar.jacoco.reportPath"]}")
        println("")
    }
}

// https://github.com/jeremylong/DependencyCheck/blob/master/src/site/markdown/dependency-check-gradle/configuration.md
// https://github.com/jeremylong/DependencyCheck/blob/main/pom.xml#L144
// cd C:\Z\caches\modules-2\files-2.1\com.h2database\h2\2.1.214\...
// java -jar h2-2.1.214.jar
//  Generic H2 (Embedded)
//  JDBC URL:       jdbc:h2:tcp://localhost/C:/Zimmermann/dependency-check-data/odc
//  Benutzername:   dcuser
//  Passwort:       DC-Pass1337!
//  Tabelle:        VULNERABILITY
dependencyCheck {
    // https://github.com/dependency-check/dependency-check-gradle/blob/main/src/main/groovy/org/owasp/dependencycheck/gradle/extension/NvdExtension.groovy
    // NVD = National Vulnerability Database
    // NIST = National Institute of Standards and Technology
    // https://nvd.nist.gov/developers/request-an-api-key
    nvd(
        closureOf<org.owasp.dependencycheck.gradle.extension.NvdExtension> {
            apiKey = (project.properties["nvdApiKey"] as String?)  ?: ""
            // default: 3500 Millisekunden Wartezeit zwischen den Aufrufen an das NVD API bei einem API-Key, sonst 8000
            //delay = 5000
            // default: max. 10 wiederholte Requests fuer einen Aufruf an das NVD API
            //nvdMaxRetryCount = 20
            // https://services.nvd.nist.gov/rest/json/cves/2.0
        }
    )

    data(
        closureOf<org.owasp.dependencycheck.gradle.extension.DataExtension> {
            directory = "C:/Zimmermann/dependency-check-data"
            // https://github.com/jeremylong/DependencyCheck/blob/main/core/src/main/java/org/owasp/dependencycheck/data/nvdcve/DatabaseManager.java#L158
            // username = "dcuser"
            // password = "DC-Pass1337!"
        },
    )

    suppressionFile = "$projectDir/config/dependencycheck/suppression.xml"
    scanConfigurations = listOf("runtimeClasspath")
    analyzedTypes = listOf("jar")

    analyzers(
        closureOf<org.owasp.dependencycheck.gradle.extension.AnalyzerExtension> {
            // nicht benutzte Analyzer
            archiveEnabled = false
            assemblyEnabled = false
            autoconfEnabled = false
            bundleAuditEnabled = false
            cmakeEnabled = false
            cocoapodsEnabled = false
            composerEnabled = false
            cpanEnabled = false
            dartEnabled = false
            golangDepEnabled = false
            golangModEnabled = false
            msbuildEnabled = false
            nugetconfEnabled = false
            nuspecEnabled = false
            pyDistributionEnabled = false
            pyPackageEnabled = false
            rubygemsEnabled = false
            swiftEnabled = false
            swiftPackageResolvedEnabled = false
            nodePackage(closureOf<org.owasp.dependencycheck.gradle.extension.NodePackageExtension> { enabled = false })
            nodeAudit(closureOf<org.owasp.dependencycheck.gradle.extension.NodeAuditExtension> { enabled = false })
            retirejs(closureOf<org.owasp.dependencycheck.gradle.extension.RetireJSExtension> { enabled = false })
        },
    )

    // format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.HTML.toString()
    // format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.ALL.toString()
}

snyk {
    setArguments("--configuration-matching=implementation|runtimeOnly")
    setSeverity("low")
    setApi("40df2078-e1a3-4f28-b913-e2babbe427fd")
}

tasks.named<Javadoc>("javadoc") {
    options {
        showFromPackage()
        // outputLevel = org.gradle.external.javadoc.JavadocOutputLevel.VERBOSE

        if (this is CoreJavadocOptions) {
            // Keine bzw. nur elementare Warnings anzeigen wegen Lombok
            // https://stackoverflow.com/questions/52205209/configure-gradle-build-to-suppress-javadoc-console-warnings
            addStringOption("Xdoclint:none", "-quiet")
            // https://stackoverflow.com/questions/59485464/javadoc-and-enable-preview
            addBooleanOption("-enable-preview", true)
            addStringOption("-release", javaLanguageVersion)
        }

        if (this is StandardJavadocDocletOptions) {
            author(true)
            bottom("Copyright &#169; 2016 - present J&uuml;rgen Zimmermann, Hochschule Karlsruhe. All rights reserved.")
        }
    }
}

tasks.named("asciidoctor", org.asciidoctor.gradle.jvm.AsciidoctorTask::class) {
    asciidoctorj {
        setVersion(libs.versions.asciidoctorj.get())
        setJrubyVersion(libs.versions.jruby.get())
        // requires("asciidoctor-diagram")

        modules {
            diagram.use()
            diagram.setVersion(libs.versions.asciidoctorjDiagram.get())
        }
    }

    val docPath = Paths.get("extras", "doc")
    setBaseDir(file(docPath))
    setSourceDir(file(docPath))
    logDocuments = true

    doLast {
        val outputPath = Paths.get(layout.buildDirectory.asFile.get().absolutePath, "docs", "asciidoc")
        val outputFile = Paths.get(outputPath.toFile().absolutePath, "projekthandbuch.html")
        println("Das Projekthandbuch ist in $outputFile")
    }
}

tasks.named("asciidoctorPdf", org.asciidoctor.gradle.jvm.pdf.AsciidoctorPdfTask::class) {
    asciidoctorj {
        setVersion(libs.versions.asciidoctorj.get())
        setJrubyVersion(libs.versions.jruby.get())

        modules {
            diagram.setVersion(libs.versions.asciidoctorjDiagram.get())
            diagram.use()
            pdf.setVersion(libs.versions.asciidoctorjPdf.get())
        }
    }

    val docPath = Paths.get("extras", "doc")
    setBaseDir(file(docPath))
    setSourceDir(file(docPath))
    attributes(mapOf("pdf-page-size" to "A4"))
    logDocuments = true

    doLast {
        val outputPath = Paths.get(layout.buildDirectory.asFile.get().absolutePath, "docs", "asciidocPdf")
        val outputFile = Paths.get(outputPath.toString(), "projekthandbuch.pdf")
        println("Das Projekthandbuch ist in $outputFile")
    }
}

licenseReport {
    configurations = arrayOf("runtimeClasspath")
}

tasks.named("dependencyUpdates", com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class) {
    checkConstraints = true
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
        // https://stackoverflow.com/questions/59950657/querydsl-annotation-processor-and-gradle-plugin
        sourceDirs.add(file("generated/"))
        generatedSourceDirs.add(file("generated/"))
    }
}
