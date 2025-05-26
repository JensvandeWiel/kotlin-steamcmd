import java.util.HexFormat

sealed class Status

/**
 * Represents the status of updating SteamCMD.
 * */
data class SteamCMDUpdating(val progress: Float) : Status()
/**
 * Represents the status of installing SteamCMD.
 * */
data class SteamCMDInstalling(val installing: Boolean = true) : Status()
/**
 * Represents the status of preparing to run SteamCMD Command (steamcmd starting up).
 * */
data class Preparing(val preparing: Boolean = true) : Status()
/**
 * Represents the status of downloading an app.
 * */
data class Downloading(val progress: Float) : Status()
/**
 * Represents the status of validating an app.
 * */
data class Validating(val progress: Float) : Status()
/**
 * Represents the status of an app that has been successfully installed.
 * */
data class Installed(val appId: Int) : Status()
/**
 * Represents the status of an app that failed to install. With an error code.
 * */
data class Failed(val appId: Int, val error: SteamCMDErrorCode) : Status()
/**
 * Represents an error that occurred while running SteamCMD.
 * Contains the exit code of the process.
 * */
data class Error(val exitCode: Int) : Status()