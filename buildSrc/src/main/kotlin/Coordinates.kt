import java.io.File
import java.io.FileInputStream
import java.util.Properties

/**
 * Configuration of project coordinates (App ID, version, etc.)
 * Reads from version.properties dynamically to ensure up-to-date values.
 */
object Coordinates {
    private val properties: Properties
        get() {
            val props = Properties()
            val versionFile = File("version.properties")
            if (versionFile.exists()) {
                FileInputStream(versionFile).use { props.load(it) }
            }
            return props
        }

    val APP_PACKAGE: String
        get() = properties.getProperty("app.package.name", "ro.snapify")

    val APP_VERSION_NAME: String
        get() = "${properties.getProperty("version.major", "1")}.${properties.getProperty("version.minor", "0")}.${properties.getProperty("version.patch", "0")}"

    val APP_VERSION_CODE: Int
        get() = properties.getProperty("version.code", "1").toInt()

    val MIN_SDK: Int
        get() = properties.getProperty("app.sdk.min", "26").toInt()

    val TARGET_SDK: Int
        get() = properties.getProperty("app.sdk.target", "36").toInt()

    val COMPILE_SDK: Int
        get() = properties.getProperty("app.sdk.compile", "36").toInt()
}   
