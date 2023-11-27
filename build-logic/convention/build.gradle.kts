import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "org.publicvalue.multiplatform.mobilecapture.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17 // hardcode to default android studio embedded jdk version JavaVersion.toVersion(libs.versions.jvmTarget.get())
    targetCompatibility = JavaVersion.VERSION_17 // hardcode to default android studio embedded jdk version  JavaVersion.toVersion(libs.versions.jvmTarget.get())
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString() // hardcode to default android studio embedded jdk version libs.versions.jvmTarget.get()
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.nexusPublish.gradlePlugin)
    compileOnly(libs.multiplatform.swiftpackage.gradlePlugin)

    // https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "org.publicvalue.convention.android.library"
            implementationClass = "org.publicvalue.convention.AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "org.publicvalue.convention.android.application"
            implementationClass = "org.publicvalue.convention.AndroidApplicationConventionPlugin"
        }
        register("kotlinMultiplatform") {
            id = "org.publicvalue.convention.kotlin.multiplatform"
            implementationClass = "org.publicvalue.convention.KotlinMultiplatformConventionPlugin"
        }
        register("kotlinMultiplatformMobile") {
            id = "org.publicvalue.convention.kotlin.multiplatform.mobile"
            implementationClass = "org.publicvalue.convention.KotlinMultiplatformMobileConventionPlugin"
        }

        register("composeMultiplatform") {
            id = "org.publicvalue.convention.compose.multiplatform"
            implementationClass = "org.publicvalue.convention.ComposeMultiplatformConventionPlugin"
        }
        register("kotlinAndroid") {
            id = "org.publicvalue.convention.kotlin.android"
            implementationClass = "org.publicvalue.convention.KotlinAndroidConventionPlugin"
        }
        register("centralPublish") {
            id = "org.publicvalue.convention.centralPublish"
            implementationClass = "org.publicvalue.convention.MavenCentralPublishConventionPlugin"
        }
        register("multiplatformSwiftPackage") {
            id = "org.publicvalue.convention.multiplatformSwiftPackage"
            implementationClass = "org.publicvalue.convention.MultiplatformSwiftPackageConventionPlugin"
        }
    }
}