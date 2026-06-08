import feature_tests.feature_tests_ping
import kotlin.test.Test
import kotlin.test.assertEquals

class SmokeTest {
    @Test
    fun ping_round_trip() {
        assertEquals(42, feature_tests_ping())
    }
}
