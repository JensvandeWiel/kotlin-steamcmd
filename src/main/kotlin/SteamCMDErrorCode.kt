

/**
 * Used to handle SteamCMD error codes, with typesafe handling.
* */
enum class SteamCMDErrorCode(val code: String, val message: String) {
    NotEnoughDiskSpace("0x202", "Not enough disk space"),
    NotEnoughDiskSpace2("0x212", "Not enough disk space"),
    SteamConnectionIssue("0x212", "Connection issue with steam, you will need to wait for the steam servers to recover."),
    UnableToWriteToDisk("0x606", "SteamCMD is unable to write to the disk. Normally caused by permissions issues. This issue was discovered when a directory that was linked using symlink did not have the correct permissions to allow SteamCMD to write to it."),
    MissingUpdateFiles("0x626", "Missing update files"),
    CorruptedUpdateFiles("0x6A6", "Corrupted update files"),
    NoConnectionToContentServer("0x6", "No connection to content server"),
    UnknownError("", "Unknown error"),
}

fun fromHexCode(hexCode: String): SteamCMDErrorCode {
    return SteamCMDErrorCode.values().find { it.code.equals(hexCode, ignoreCase = true) }
        ?: SteamCMDErrorCode.UnknownError
}