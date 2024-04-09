import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.compose.internal.utils.getLocalProperty
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.publicvalue.convention.config.configureIosTargets

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.centralPublish")
    id("org.publicvalue.convention.multiplatformSwiftPackage")
}

description = "Kotlin Multiplatform OIDC appsupport library for Android/iOS"

//publishing {
//    publications {
//        create<MavenPublication>("releaseGithubPackages") {
//            groupId = "io.github.kalinjul.kotlin.multiplatform"
//            artifactId = "oidc-appsupport"
//            version = "0.9.2"
////            artifact("$buildDir/outputs/aar/oidc-appsupport-release.aar")
//
//        }
//    }
//
//    repositories {
//        maven {
//            name = "GithubPackages"
//            url = uri("https://maven.pkg.github.com/egeniq/kotlin-multiplatform-oidc")
//            credentials {
//                username = System.getenv("GITHUB_USER")
//                password = System.getenv("GITHUB_OIDC_TOKEN")
//            }
//        }
//    }
//
//    extensions.configure<KotlinMultiplatformExtension> {
//        if (pluginManager.hasPlugin("com.android.library")) {
//            androidTarget {
//                publishLibraryVariants("release")
//            }
//        }
//    }
//
//    extensions.configure<SigningExtension> {
//        useInMemoryPgpKeys(
//        )
//        val publishing = extensions.getByType<PublishingExtension>()
//        sign(publishing.publications)
//    }
//
//}

multiplatformSwiftPackage {
    packageName("OpenIdConnectClient")
    zipFileName("OpenIdConnectClient")
}

kotlin {
    configureIosTargets(baseName = "OpenIdConnectClient")
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.oidcCore)
                api(projects.oidcTokenstore)
            }
        }

        val iosMain by getting {
            dependencies {
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.browser)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
            }
        }

        val commonTest by getting {
            dependencies {
            }
        }
    }

    targets.withType<KotlinNativeTarget> {
        compilations["main"].compilerOptions.options.freeCompilerArgs.add("-Xexport-kdoc")

        binaries.withType<Framework> {
            export(projects.oidcCore)
            export(projects.oidcTokenstore)
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.appsupport"
}