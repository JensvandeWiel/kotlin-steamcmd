import com.pty4j.PtyProcessBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.nio.file.Path


/**
 * Represents a class to run SteamCMD commands and parse their output.
 * Mainly used to install and update games.
 * */
class SteamCMD(steamCmdPath: Path) {

    private val steamCMDUpdatingRegex = Regex(""".*\[\s*(\d+)%\]\s*(Downloading update|Download Complete).*""")
    private val statusRegex = Regex(""".*Update state \([^)]+\)\s+([a-zA-Z ]+), progress: (\d+\.\d+).*""")
    private val successRegex = Regex(""".*Success! App '(\d+)' fully installed\..*""")
    private val failureRegex = Regex(""".*Error! App '(\d+)' state is (0x[0-9a-fA-F]+) after update job\..*""")
    private val installer: Installer = Installer(steamCmdPath)

    fun runAsFlow(commands: List<String>): Flow<Status> = flow {
        if (!installer.isInstalled()) {
            throw IOException("SteamCMD is not installed at ${installer.installPath}")
        }

        val cmdList = mutableListOf<String>()
        cmdList.add(installer.cmdPath.toString())
        for (cmd in commands) cmdList.add("+$cmd")
        cmdList.add("+quit")

        val process = PtyProcessBuilder().setCommand(cmdList.toTypedArray()).start()
        val reader = process.inputStream.bufferedReader()
        try {
            while (true) {
                val line = reader.readLine() ?: break
                parseStatusLine(line)?.let { status -> emit(status) }
            }
        } finally {
            reader.close()
        }

        val exitCode = process.waitFor()
        // Exit code 0 means success, 7 is fine as well.
        if (exitCode != 0 && exitCode != 7) emit(Error(exitCode))

        process.destroy()
    }

    private fun parseStatusLine(line: String): Status? {
        val line = line.trim()
        return when {
            steamCMDUpdatingRegex.matches(line) -> {
                val matchResult = steamCMDUpdatingRegex.find(line)!!
                val progress = matchResult.groupValues[1].toFloat()
                return if (matchResult.groupValues[2] == "Downloading update" || matchResult.groupValues[2] == "Download Complete") {
                    SteamCMDUpdating(progress)
                } else {
                    return null
                }
            }
            line.contains("[----] Installing update...") -> SteamCMDInstalling()
            line.contains("[----] Update complete, launching...") -> Preparing()
            line.contains("-- type 'quit' to exit --") -> Preparing()
            statusRegex.matches(line) -> {
                val matchResult = statusRegex.find(line)!!
                val statusType = matchResult.groupValues[1]
                val progress = matchResult.groupValues[2].toFloat()
                return when (statusType) {
                    "downloading" -> Downloading(progress)
                    "verifying update" -> Validating(progress)
                    "verifying install" -> Validating(progress)
                    else -> null
                }
            }

            successRegex.matches(line) -> {
                val matchResult = successRegex.find(line)!!
                val appId = matchResult.groupValues[1].toInt()
                return Installed(appId)
            }

            failureRegex.matches(line) -> {
                val matchResult = failureRegex.find(line)!!
                val appId = matchResult.groupValues[1].toInt()
                val hexCode = matchResult.groupValues[2]
                val errorCode = fromHexCode(hexCode)
                return Failed(appId, errorCode)
            }

            else -> null
        }
    }
}
