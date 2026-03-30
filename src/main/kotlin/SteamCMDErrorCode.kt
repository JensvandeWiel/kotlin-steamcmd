/**
 * Used to handle SteamCMD error codes, with typesafe handling.
 * */
enum class SteamCMDErrorCode(val code: String, val message: String) {
    HldsTransientIssue(
        "0x10E",
        "Can affect HLDS based servers. Running again often fixes the issue."
    ),
    GenericState2("0x2", "Unknown SteamCMD update state error"),
    NoConnectionToContentServer("0x6", "No connection to content server"),
    NotEnoughDiskSpace("0x202", "Not enough disk space"),
    UnknownState206("0x206", "Unknown SteamCMD update state error"),
    NotEnoughDiskSpaceQuota("0x212", "Not enough disk space"),
    SteamConnectionIssue(
        "0x402",
        "Connection issue with steam, you will need to wait for the steam servers to recover."
    ),
    UnknownState602("0x602", "Unknown SteamCMD update state error"),
    UnableToWriteToDisk(
        "0x606",
        "SteamCMD is unable to write to the disk. Normally caused by permissions issues. This issue was discovered when a directory that was linked using symlink did not have the correct permissions to allow SteamCMD to write to it."
    ),
    MissingUpdateFiles("0x626", "Missing update files"),
    CorruptedUpdateFiles("0x6A6", "Corrupted update files"),
    UnknownError("", "Unknown error"),
}

fun fromHexCode(hexCode: String): SteamCMDErrorCode {
    val normalizedCode = hexCode.trim().lowercase()
    return SteamCMDErrorCode.entries.find { it.code.lowercase() == normalizedCode }
        ?: SteamCMDErrorCode.UnknownError
}