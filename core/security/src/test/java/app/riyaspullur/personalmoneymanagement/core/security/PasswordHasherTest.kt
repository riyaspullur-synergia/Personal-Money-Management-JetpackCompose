package app.riyaspullur.personalmoneymanagement.core.security

import android.util.Base64
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * android.util.Base64 has no real implementation on the plain JVM unit-test classpath, so its
 * static methods are shimmed here to delegate to java.util.Base64 (equivalent encoding).
 */
class PasswordHasherTest {

    @Before
    fun setup() {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any<ByteArray>(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg<ByteArray>())
        }
        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getDecoder().decode(firstArg<String>())
        }
    }

    @After
    fun tearDown() {
        unmockkStatic(Base64::class)
    }

    @Test
    fun `hashed password can be verified with the original password`() {
        val hash = PasswordHasher.hashPassword("mySecret123")
        assertTrue(PasswordHasher.verifyPassword("mySecret123", hash))
    }

    @Test
    fun `verification fails for a wrong password`() {
        val hash = PasswordHasher.hashPassword("mySecret123")
        assertFalse(PasswordHasher.verifyPassword("wrongPassword", hash))
    }

    @Test
    fun `same password produces a different hash each time due to random salt`() {
        val hash1 = PasswordHasher.hashPassword("mySecret123")
        val hash2 = PasswordHasher.hashPassword("mySecret123")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `both salted hashes still verify correctly`() {
        val hash1 = PasswordHasher.hashPassword("mySecret123")
        val hash2 = PasswordHasher.hashPassword("mySecret123")
        assertTrue(PasswordHasher.verifyPassword("mySecret123", hash1))
        assertTrue(PasswordHasher.verifyPassword("mySecret123", hash2))
    }

    @Test
    fun `hash format is salt colon hash`() {
        val hash = PasswordHasher.hashPassword("mySecret123")
        assertEquals(2, hash.split(":").size)
    }

    @Test
    fun `verification fails for malformed stored hash`() {
        assertFalse(PasswordHasher.verifyPassword("mySecret123", "not-a-valid-hash"))
    }

    @Test
    fun `verification fails for empty stored hash`() {
        assertFalse(PasswordHasher.verifyPassword("mySecret123", ""))
    }
}
