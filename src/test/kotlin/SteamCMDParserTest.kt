import java.nio.file.Path
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SteamCMDParserTest {
    private val steamCmd = SteamCMD(Path.of("C:\\SteamCMD"))

    @Test
    fun `parseStatusLine maps extra update states`() {
        val reconfiguring = steamCmd.parseStatusLine("Update state (0x3) reconfiguring, progress: 0.00 (0 / 0)")
        val preallocating = steamCmd.parseStatusLine("Update state (0x11) preallocating, progress: 8.53 (983870089 / 11530459441)")
        val committing = steamCmd.parseStatusLine("Update state (0x101) committing, progress: 3.43 (395043827 / 11530459441)")

        assertIs<Reconfiguring>(reconfiguring)
        assertIs<Preallocating>(preallocating)
        assertIs<Committing>(committing)
        assertTrue(abs(reconfiguring.progress - 0.0f) < 0.0001f)
        assertTrue(abs(preallocating.progress - 8.53f) < 0.0001f)
        assertTrue(abs(committing.progress - 3.43f) < 0.0001f)
    }

    @Test
    fun `parseStatusLine maps validating and downloading regardless of casing`() {
        val validating = steamCmd.parseStatusLine("Update state (0x5) validating, progress: 13.48 (1554089956 / 11530459441)")
        val downloading = steamCmd.parseStatusLine("Update state (0x61) DOWNLOADING, progress: 1.11 (127644881 / 11530459441)")

        assertIs<Validating>(validating)
        assertIs<Downloading>(downloading)
        assertTrue(abs(validating.progress - 13.48f) < 0.0001f)
        assertTrue(abs(downloading.progress - 1.11f) < 0.0001f)
    }

    @Test
    fun `parseStatusLine maps failure line without app id`() {
        val failed = steamCmd.parseStatusLine("Error! State is 0x402 after update job.")

        assertIs<Failed>(failed)
        assertEquals(-1, failed.appId)
        assertEquals(SteamCMDErrorCode.SteamConnectionIssue, failed.error)
    }

    @Test
    fun `parseStatusLine tolerates double is typo in failure line`() {
        val failed = steamCmd.parseStatusLine("Error! App '90' state is is 0x2 after update job.")

        assertIs<Failed>(failed)
        assertEquals(90, failed.appId)
        assertEquals(SteamCMDErrorCode.GenericState2, failed.error)
    }
}

