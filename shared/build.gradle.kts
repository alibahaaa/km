import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    val xcframeworkName = "shared"
    val xcf = XCFramework(xcframeworkName)
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = xcframeworkName
            binaryOption("bundleId", "ir.baha.${xcframeworkName}")
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val iosMain by creating {
            dependencies {
                // Add iOS-specific dependencies here
            }
        }
        val iosTest by creating {
            dependencies {
                // Add iOS-specific test dependencies here
            }
        }
    }
}

android {
    namespace = "ir.baha.km"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["kotlin"])

            groupId = "ir.baha"
            artifactId = "km"
            version = "0.1.0"
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://github.com/alibahaaa/km")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}

tasks.register("buildXCFramework") {
    dependsOn("linkReleaseFrameworkIosArm64", "linkReleaseFrameworkIosX64")

    doLast {
        val arm64Framework = buildDir.resolve("bin/iosArm64/releaseFramework/shared.framework")
        val x64Framework = buildDir.resolve("bin/iosX64/releaseFramework/shared.framework")
        val outputDir = buildDir.resolve("XCFrameworks")
        val xcFramework = outputDir.resolve("shared.xcframework")

        exec {
            commandLine = listOf(
                "xcodebuild",
                "-create-xcframework",
                "-framework", arm64Framework.absolutePath,
                "-framework", x64Framework.absolutePath,
                "-output", xcFramework.absolutePath
            )
        }
    }
}