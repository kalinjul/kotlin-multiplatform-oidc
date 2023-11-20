package org.publicvalue.multiplatform.oauth

data class ApplicationInfo(
    val packageName: String,
    val debugBuild: Boolean,
    val versionName: String,
    val versionCode: Int,
)

