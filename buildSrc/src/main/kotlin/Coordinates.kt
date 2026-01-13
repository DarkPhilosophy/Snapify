import java.io.FileInputStream
import java.util.Properties

/**
* Configuration of project coordinates (App ID, version, etc.)
*/
object Coordinates {
    private val properties = Properties().apply {
        val versionFile = java.io.File("version.properties")
        if (versionFile.exists()) {
            FileInputStream(versionFile).use { load(it) }
        }
    }

    val APP_PACKAGE =
        properties.getProperty("app.package.name", "ro.snapify")
            ?: "ro.snapify"  // Full package name for namespace and applicationId

    val APP_VERSION_NAME = "${properties.getProperty("version.major", "1")}.${properties.getProperty("version.minor", "0")}.${properties.getProperty("version.patch", "0")}"
    val APP_VERSION_CODE =
        properties.getProperty("version.code", "1").toInt()

    val MIN_SDK =
        properties.getProperty("app.sdk.min", "24").toInt()
    val TARGET_SDK =
        properties.getProperty("app.sdk.target", "36").toInt()
    val COMPILE_SDK =
        properties.getProperty("app.sdk.compile", "36").toInt()
}
