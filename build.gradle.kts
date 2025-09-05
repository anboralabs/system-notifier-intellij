import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.3.1")
    }
}

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.7.2"
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(properties("javaVersion").get().toInt())
}

repositories {
    mavenCentral()
    // Rococoa artifacts are hosted on Cyberduck's S3 Maven repo
    maven {
        url = uri("https://s3.eu-west-1.amazonaws.com/repo.maven.cyberduck.io/releases")
    }
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure IntelliJ Platform Gradle Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(properties("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(properties("platformPlugins").map { it.split(',') })

        pluginVerifier()
        testFramework(TestFrameworkType.Platform)
    }
    implementation("io.sentry:sentry:6.15.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        version = properties("pluginVersion")
        description = file("src/main/html/description.html").inputStream().readBytes().toString(Charsets.UTF_8)
        changeNotes = file("src/main/html/change-notes.html").inputStream().readBytes().toString(Charsets.UTF_8)

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    buildSearchableOptions = false
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    verbose()

    // Alternatively put your config in a separate file
    // configuration("config.pro")

    // Use the jar task output as an input jar. This will automatically add the necessary task dependency.
    injars(tasks.named("jar"))
    outjars("build/${rootProject.name}-obfuscated.jar")

    val javaHome = System.getProperty("java.home")

    //Dependencies
    if(!properties("skipProguard").get().toBoolean()) {
        File("$javaHome/jmods/").listFiles()!!.forEach { libraryjars(it.absolutePath) }
    }
    libraryjars(configurations.compileClasspath.get())

    //Processing-configuration
    target(properties("javaVersion").get())
    printmapping("build/obfuscation-mapping.txt")

    dontshrink()
    overloadaggressively()
    repackageclasses("co.anbora.labs.system.notifier")
    renamesourcefileattribute("SourceFile")
    adaptresourcefilecontents("**.xml")

    keepdirectories("demo,fileTemplates,implicitCode,inspectionDescriptions,messages,images")
    keepattributes("InnerClasses,Signature,EnclosingMethod,SourceFile,LineNumberTable,*Annotation*")

    dontwarn("kotlin.jvm.internal.SourceDebugExtension")

    keep("""class co.anbora.labs.system.notifier.impl.ObjectiveCRuntime {
       <fields>;
    }""")

    keep("""class co.anbora.labs.system.notifier.SystemNotifierFlavor {
       <fields>;
    }""")

    keep("""class ** implements co.anbora.labs.system.notifier.SystemNotifier {
       <fields>;
    }""")
}

val inputPath = "build/libs/${rootProject.name}-$version.jar" //Where <PluginName> is the actual name of your plugin
val outputPath = "build/${rootProject.name}-obfuscated.jar"

// Ensure signing is not silently skipped when publishing
val hasSigningCreds = listOf("CERTIFICATE_CHAIN", "PRIVATE_KEY", "PRIVATE_KEY_PASSWORD")
    .all { environment(it).isPresent }

tasks {

    // Fail early if trying to publish without signing credentials
    named("publishPlugin") {
        dependsOn("signPlugin")
        doFirst {
            if (!hasSigningCreds) {
                throw GradleException(
                    "Missing signing credentials. Set CERTIFICATE_CHAIN, PRIVATE_KEY, and PRIVATE_KEY_PASSWORD in the environment to sign before publishing."
                )
            }
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    prepareSandbox {
        if(!properties("skipProguard").get().toBoolean()) {
            dependsOn("proguard")
            doFirst {
                with(File(inputPath)) {
                    if (exists()) {
                        val proguarded = File(outputPath)
                        if (proguarded.exists()) {
                            delete() //delete original jar
                            proguarded.renameTo(this)
                            println("Plugin archive successfully obfuscated and optimized.")
                        } else println("ProGuarded file doesn't exist.")
                    } else println("Original file doesn't exist. $inputPath")
                }
            }
        }
    }
}
