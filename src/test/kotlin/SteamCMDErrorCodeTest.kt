import kotlin.test.Test
import kotlin.test.assertEquals

class SteamCMDErrorCodeTest {
    @Test
    fun `fromHexCode resolves known code ignoring case`() {
        val code = fromHexCode("0x6a6")

        assertEquals(SteamCMDErrorCode.CorruptedUpdateFiles, code)
    }

    @Test
    fun `fromHexCode resolves documented reference codes`() {
        assertEquals(SteamCMDErrorCode.HldsTransientIssue, fromHexCode("0x10E"))
        assertEquals(SteamCMDErrorCode.NotEnoughDiskSpace, fromHexCode("0x202"))
        assertEquals(SteamCMDErrorCode.UnknownState206, fromHexCode("0x206"))
        assertEquals(SteamCMDErrorCode.NotEnoughDiskSpaceQuota, fromHexCode("0x212"))
        assertEquals(SteamCMDErrorCode.SteamConnectionIssue, fromHexCode("0x402"))
        assertEquals(SteamCMDErrorCode.UnknownState602, fromHexCode("0x602"))
        assertEquals(SteamCMDErrorCode.UnableToWriteToDisk, fromHexCode("0x606"))
        assertEquals(SteamCMDErrorCode.MissingUpdateFiles, fromHexCode("0x626"))
        assertEquals(SteamCMDErrorCode.CorruptedUpdateFiles, fromHexCode("0x6A6"))
        assertEquals(SteamCMDErrorCode.GenericState2, fromHexCode("0x2"))
        assertEquals(SteamCMDErrorCode.NoConnectionToContentServer, fromHexCode("0x6"))
    }

    @Test
    fun `fromHexCode trims whitespace`() {
        val code = fromHexCode("  0x402  ")

        assertEquals(SteamCMDErrorCode.SteamConnectionIssue, code)
    }

    @Test
    fun `fromHexCode resolves unknown code to UnknownError`() {
        val code = fromHexCode("0xDEAD")

        assertEquals(SteamCMDErrorCode.UnknownError, code)
    }
}


