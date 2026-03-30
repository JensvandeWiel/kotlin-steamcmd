import com.pty4j.PtyProcessBuilder
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.nio.file.Path

/**
 * Runs SteamCMD commands and parses their output for status updates.
 *
 * @constructor Accepts the path to the SteamCMD installation.
 */
class SteamCMD(steamCmdPath: Path) {

    private val steamCMDUpdatingRegex =
        Regex(""".*\[\s*(\d+)%\]\s*(Downloading update|Download Complete).*""", RegexOption.IGNORE_CASE)
    private val statusRegex =
        Regex(""".*Update state \([^)]+\)\s+([a-zA-Z ]+), progress: (\d+(?:\.\d+)?).*""", RegexOption.IGNORE_CASE)
    private val successRegex = Regex(""".*Success! App '(\d+)' fully installed\..*""")
    private val failureRegex =
        Regex(""".*Error!\s+App\s+'(\d+)'\s+state\s+is(?:\s+is)?\s+(0x[0-9a-fA-F]+)\s+after update job\..*""", RegexOption.IGNORE_CASE)
    private val failureNoAppRegex =
        Regex(""".*Error!\s+State\s+is\s+(0x[0-9a-fA-F]+)\s+after update job\..*""", RegexOption.IGNORE_CASE)
    private val installer: Installer = Installer(steamCmdPath)

    /**
     * Runs a list of SteamCMD commands as a coroutine flow, emitting status updates.
     * @param commands List of commands to run.
     * @return Flow emitting [Status] updates.
     * @throws IOException if SteamCMD is not installed.
     */
    fun runAsFlow(commands: List<String>): Flow<Status> = flow {
        if (!installer.isInstalled()) {
            throw IOException("SteamCMD is not installed at ${installer.installPath}")
        }

        val cmdList = mutableListOf<String>()
        cmdList.add(installer.cmdPath.toString())
        for (cmd in commands) cmdList.add("+$cmd")
        cmdList.add("+quit")

        val process = PtyProcessBuilder().setCommand(cmdList.toTypedArray()).start()
        try {
            process.inputStream.bufferedReader().use { reader ->
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val line = reader.readLine() ?: break
                    parseStatusLine(line)?.let { status -> emit(status) }
                }
            }
            val exitCode = process.waitFor()
            // Exit code 0 means success, 7 is fine as well.
            if (exitCode != 0 && exitCode != 7) emit(Error(exitCode))

        } finally {
            if (process.isAlive) {
                process.destroy()
                if (process.isAlive) {
                    process.destroyForcibly()
                }
            }
        }
    }

    /**
     * Parses a line of SteamCMD output and returns a [Status] if recognized.
     * @param line Output line from SteamCMD.
     * @return [Status] or null if not recognized.
     */
    internal fun parseStatusLine(line: String): Status? {
        val line = line.trim()
        return when {
            steamCMDUpdatingRegex.matches(line) -> {
                val matchResult = steamCMDUpdatingRegex.find(line)!!
                val progress = matchResult.groupValues[1].toFloat()
                return if (matchResult.groupValues[2].equals("Downloading update", ignoreCase = true) ||
                    matchResult.groupValues[2].equals("Download Complete", ignoreCase = true)
                ) {
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
                val statusType = matchResult.groupValues[1].trim().lowercase().replace(Regex("""\s+"""), " ")
                val progress = matchResult.groupValues[2].toFloat()
                return when (statusType) {
                    "downloading" -> Downloading(progress)
                    "verifying update" -> Validating(progress)
                    "verifying install" -> Validating(progress)
                    "validating" -> Validating(progress)
                    "reconfiguring" -> Reconfiguring(progress)
                    "preallocating" -> Preallocating(progress)
                    "committing" -> Committing(progress)
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

            failureNoAppRegex.matches(line) -> {
                val matchResult = failureNoAppRegex.find(line)!!
                val errorCode = fromHexCode(matchResult.groupValues[1])
                return Failed(-1, errorCode)
            }

            else -> null
        }
    }
}
