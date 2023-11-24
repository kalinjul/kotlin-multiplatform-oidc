package org.publicvalue.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.compose.internal.utils.getLocalProperty
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MavenCentralPublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("maven-publish")
                apply("signing")
            }
//
            extensions.configure<KotlinMultiplatformExtension> {
                if (pluginManager.hasPlugin("com.android.library")) {
                    androidTarget {
                        publishLibraryVariants("release")
                    }
                }
            }

            extensions.configure<PublishingExtension> {
                publications.withType<MavenPublication> {
                    pom {
                        name.set(project.name)
                        description.set(project.description)
                        url.set("https://github.com/kalinjul/kotlin-multiplatform-oidc")
                        licenses {
                            license {
                                name.set("Apache-2.0 License")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                            developers {
                                developer {
                                    id.set("kalinjul")
                                    name.set("Julian Kalinowski")
                                    email.set("julakali@gmail.com")
                                }
                                developer {
                                    id.set("ch4rl3x")
                                    name.set("Alexander Karkossa")
                                    email.set("alexander.karkossa@googlemail.com")
                                }
                                developer {
                                    id.set("capjan")
                                    name.set("Jan Ruhländer")
                                    email.set("jan.ruhlaender@gmail.com")
                                }
                            }
                            scm {
                                connection.set("scm:git:github.com/kalinjul/kotlin-multiplatform-oidc.git")
                                developerConnection.set("scm:git:ssh://github.com/kalinjul/kotlin-multiplatform-oidc.git")
                                url.set("https://github.com/kalinjul/kotlin-multiplatform-oidc/tree/main")
                            }
                        }
                    }
                }
            }

            extensions.configure<SigningExtension> {
                useInMemoryPgpKeys(
                    getLocalProperty("SIGNING_KEY_ID") ?: System.getenv("SIGNING_KEY_ID"),
                    getLocalProperty("SIGNING_KEY") ?: System.getenv("SIGNING_KEY"),
                    getLocalProperty("SIGNING_KEY_PASSWORD") ?: System.getenv("SIGNING_KEY_PASSWORD"),
                )
                val publishing = extensions.getByType<PublishingExtension>()
                sign(publishing.publications)
            }
        }
    }
}