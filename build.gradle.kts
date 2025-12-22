import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"
    application
}

group = "net.illunis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // for Konbini
    maven {
        url = uri("https://jitpack.io")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    implementation("com.varabyte.kotter:kotter-jvm:1.2.1")
    implementation("cc.ekblad.konbini:konbini:0.1.2")

    testImplementation("com.varabyte.kotterx:kotter-test-support-jvm:1.2.1")
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    applicationDefaultJvmArgs = listOf(
        // JDK24 started reporting warnings for libraries that use restricted native methods, at least one which Kotter
        // uses indirectly (via jline/jansi). It looks like this:
        //
        // WARNING: A restricted method in java.lang.System has been called
        // WARNING: java.lang.System::loadLibrary has been called by ...
        // WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
        // WARNING: Restricted methods will be blocked in a future release unless native access is enabled
        //
        // The best solution we have for now is to disable the warning by explicitly enabling access.
        // See also: https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/lang/doc-files/RestrictedMethods.html
        // And also: https://github.com/jline/jline3/issues/1067
        "--enable-native-access=ALL-UNNAMED",
    )
    // The following assumes a top-level `main.kt` file in your project; adjust as needed otherwise
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}