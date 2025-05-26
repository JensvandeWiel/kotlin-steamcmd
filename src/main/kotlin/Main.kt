import kotlin.io.path.Path

suspend fun main(args: Array<String>) {
    val installer = Installer()

    val result = installer.install()
    if (result.isSuccess) {
        println("SteamCMD installed successfully.")
    } else {
        println("Failed to install SteamCMD: ${result.exceptionOrNull()?.message}")
    }

    if (installer.isInstalled()) {
        println("SteamCMD is installed at: ${installer.cmdPath.toAbsolutePath()}")
    } else {
        println("SteamCMD is not installed.")
    }

    if (Path("./server").toFile().exists()) {
        println("Server directory already exists.")
    } else {
        println("Creating server directory at: ${Path("./server").toAbsolutePath()}")
        Path("./server").toFile().mkdirs()
    }


    val steamCmd = SteamCMD(installer.installPath)
    try {
        val flow = steamCmd.runAsFlow(listOf("force_install_dir C:\\Users\\jens\\MyProjects\\kotlin-steamcmd\\server", "login anonymous", "app_update 740 validate"))
        flow.collect { status ->
            when (status) {
                is SteamCMDUpdating -> println("Updating SteamCMD: ${status.progress}%")
                is SteamCMDInstalling -> println("Installing SteamCMD Update...")
                is Preparing -> println("Preparing to run SteamCMD...")
                is Downloading -> println("Downloading: ${status.progress}%")
                is Validating -> println("Validating: ${status.progress}%")
                is Installed -> println("App ${status.appId} installed successfully.")
                is Failed -> println("Failed to install app ${status.appId}: ${status.error}")
                is Error -> println("SteamCMD exited with error code: ${status.exitCode}")
            }
        }
    } catch (e: Exception) {
        println("Error running SteamCMD: ${e.message}")
    }


}