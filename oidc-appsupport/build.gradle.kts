import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.exportKdoc
import org.publicvalue.convention.config.configureWasm
import java.nio.file.Files
import java.util.stream.Collectors.toList
import kotlin.io.path.name

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.centralPublish")
    id("org.publicvalue.convention.multiplatformSwiftPackage")
}

description = "Kotlin Multiplatform OIDC appsupport library for Android/iOS"


multiplatformSwiftPackage {
    packageName("OpenIdConnectClient")
    zipFileName("OpenIdConnectClient")
}


// workaround for https://forums.developer.apple.com/forums/thread/748177, remove once apple fixed it
val fixTask = tasks.create("fixFrameworkPlist") {
    val fixTask = this
    group = "multiplatform-swift-package"
    afterEvaluate {
        val createFrameworkTask = tasks.named("createXCFramework").get()
        val deps = createFrameworkTask.taskDependencies.getDependencies(createFrameworkTask)
        fixTask.dependsOn(deps)
    }
    doFirst {
        val createFrameworkTask = tasks.named("createXCFramework").get()
        val deps = createFrameworkTask.taskDependencies.getDependencies(createFrameworkTask)
        val outDirs = deps.flatMap {
            it.outputs.files.files
        }
        val plists = outDirs.flatMap {
            if (it.exists() && it.isDirectory) {
                Files.walk(it.toPath())
                    .filter {
                        it.name == "Info.plist"
                    }
                    .collect(toList())
            } else {
                listOf()
            }
        }

        plists.forEach {
            logger.warn("Apply XCode 15.3(+) workaround to plist file: $it")
            exec {
                commandLine("/usr/libexec/PlistBuddy", "-c", "Set MinimumOSVersion 100.0", it.toFile().absolutePath)
            }
        }
    }

}


afterEvaluate {
    tasks.named("createXCFramework").dependsOn(tasks.named(fixTask.name))
}

kotlin {
    configureIosTargets(baseName = "OpenIdConnectClient")
    configureWasm(baseName = "OpenIdConnectClient")
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

    exportKdoc()
    targets.withType<KotlinNativeTarget> {
        binaries.withType<Framework> {
            export(projects.oidcCore)
            export(projects.oidcTokenstore)

//            freeCompilerArgs += listOf("-Xoverride-konan-properties=minVersion.ios=15.0;minVersionSinceXcode15.ios=15.0")
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.appsupport"
}