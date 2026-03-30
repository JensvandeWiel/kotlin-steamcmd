import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class InstallerTest {
    @Test
    fun `resolveInstallEntryPath keeps files inside install dir`() {
        val installPath = Files.createTempDirectory("steamcmd-install")
        try {
            val installer = Installer(installPath)

            val resolved = installer.resolveInstallEntryPath("steamcmd.exe")

            assertTrue(resolved.startsWith(installPath.toAbsolutePath().normalize()))
        } finally {
            installPath.toFile().deleteRecursively()
        }
    }

    @Test
    fun `resolveInstallEntryPath blocks zip slip entries`() {
        val installPath = Files.createTempDirectory("steamcmd-install")
        try {
            val installer = Installer(installPath)

            assertFailsWith<SecurityException> {
                installer.resolveInstallEntryPath("..\\..\\Windows\\System32\\bad.exe")
            }
        } finally {
            installPath.toFile().deleteRecursively()
        }
    }
}


