import java.io.File
import java.net.URI
import java.nio.file.Path

/**
 * Handles downloading and installing SteamCMD to a specified directory.
 *
 * @property installPath The path where SteamCMD will be installed.
 */
class Installer(
    val installPath: Path = Path.of("${System.getProperty("user.home")}\\AppData\\Local\\SteamCMD")
) {
    private val isWindows: Boolean
        get() = System.getProperty("os.name").lowercase().contains("win")

    private val downloadUrl: String
        get() = if (isWindows) {
            "https://steamcdn-a.akamaihd.net/client/installer/steamcmd.zip"
        } else {
            throw UnsupportedOperationException("Unsupported operating system")
        }

    private val commandName: String
        get() = if (isWindows) {
            "steamcmd.exe"
        } else {
            throw UnsupportedOperationException("Unsupported operating system")
        }

    val cmdPath: Path
        get() = installPath.resolve(commandName)

    /**
     * Checks if SteamCMD is already installed.
     * @return true if installed, false otherwise.
     */
    fun isInstalled(): Boolean {
        return cmdPath.toFile().exists()
    }

    /**
     * Installs SteamCMD by downloading and extracting it if not already installed.
     * @return Result indicating success or failure.
     */
    fun install(): Result<Boolean> {
        if (isInstalled()) {
            return Result.success(true)
        }

        val installDir = installPath.toFile()
        if (!installDir.exists()) {
            if (!installDir.mkdirs()) {
                return Result.failure(Exception("Failed to create install directory: ${installPath.toAbsolutePath()}"))
            }
        }

        val tempFile = File.createTempFile("steamcmd", ".zip")

        URI(downloadUrl).toURL().openStream().use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        try {
            val zipFile = java.util.zip.ZipFile(tempFile)
            zipFile.entries().asSequence().forEach { entry ->
                if (!entry.isDirectory) {
                    val outputFile = installPath.resolve(entry.name).toFile()
                    outputFile.parentFile.mkdirs()
                    zipFile.getInputStream(entry).use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            tempFile.delete()
        }

        return if (isInstalled()) {
            Result.success(true)
        } else {
            Result.failure(Exception("Installation failed, command not found at: ${cmdPath.toAbsolutePath()}"))
        }
    }
}

