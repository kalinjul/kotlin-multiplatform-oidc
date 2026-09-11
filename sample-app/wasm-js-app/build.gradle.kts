import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    jvm()
    // Explicitly apply the default KMP hierarchy so js and wasmJs get a shared 'webMain' source set.
    applyDefaultHierarchyTemplate()
    js {
        browser()
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "wasm-js-app"
        browser {
            commonWebpackConfig {
                outputFileName = "wasm-js-app.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    // Serve sources to debug inside browser
                    static(project.rootDir.path)
                    static(project.projectDir.path)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val webMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)

                implementation(projects.sampleApp.shared)
            }
        }
    }
}