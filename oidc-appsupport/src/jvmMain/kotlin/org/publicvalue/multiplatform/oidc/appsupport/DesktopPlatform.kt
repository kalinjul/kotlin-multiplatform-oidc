package org.publicvalue.multiplatform.oidc.appsupport

internal enum class DesktopPlatform {
    Windows,
    Linux,
    MacOS,
    Unknown;

    companion object {
        val Current by lazy {
            val osName = System.getProperty("os.name").orEmpty().lowercase()
            when {
                osName.startsWith("win") -> Windows
                osName.startsWith("mac") -> MacOS
                osName.startsWith("linux") -> Linux
                else -> Unknown
            }
        }
    }
}
