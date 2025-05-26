/**
 * Represents the status of SteamCMD operations.
 * Used as a sealed class hierarchy for type-safe status handling.
 */
sealed class Status

/**
 * Status for updating SteamCMD, includes progress percentage.
 */
data class SteamCMDUpdating(val progress: Float) : Status()

/**
 * Status for installing SteamCMD.
 */
data class SteamCMDInstalling(val installing: Boolean = true) : Status()

/**
 * Status for preparing to run SteamCMD command.
 */
data class Preparing(val preparing: Boolean = true) : Status()

/**
 * Status for downloading an app, includes progress percentage.
 */
data class Downloading(val progress: Float) : Status()

/**
 * Status for validating an app, includes progress percentage.
 */
data class Validating(val progress: Float) : Status()

/**
 * Status for a successfully installed app, includes appId.
 */
data class Installed(val appId: Int) : Status()

/**
 * Status for a failed app installation, includes appId and error code.
 */
data class Failed(val appId: Int, val error: SteamCMDErrorCode) : Status()

/**
 * Status for a generic error, includes exit code.
 */
data class Error(val exitCode: Int) : Status()

