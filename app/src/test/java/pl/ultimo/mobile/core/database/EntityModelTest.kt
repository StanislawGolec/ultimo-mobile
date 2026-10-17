package pl.ultimo.mobile.core.database

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import pl.ultimo.mobile.core.database.entity.PlayerProfileEntity
import pl.ultimo.mobile.core.datastore.UserPreferences

class EntityModelTest {

    @Test
    fun playerProfileEntity_winRatePercent_computesAccurately() {
        val emptyProfile = PlayerProfileEntity(totalMatches = 0, totalWins = 0)
        assertThat(emptyProfile.winRatePercent).isEqualTo(0.0)

        val activeProfile = PlayerProfileEntity(totalMatches = 20, totalWins = 15)
        assertThat(activeProfile.winRatePercent).isEqualTo(75.0)

        val perfectProfile = PlayerProfileEntity(totalMatches = 5, totalWins = 5)
        assertThat(perfectProfile.winRatePercent).isEqualTo(100.0)
    }

    @Test
    fun userPreferences_effectiveWebSocketUrl_switchesDynamically() {
        val standardPrefs = UserPreferences(
            serverUrl = "ws://backend.ultimo.pl:8080/ws",
            useChaosProxy = false,
            chaosProxyUrl = "ws://proxy.ultimo.pl:8090/ws"
        )
        assertThat(standardPrefs.effectiveWebSocketUrl).isEqualTo("ws://backend.ultimo.pl:8080/ws")

        val chaosPrefs = standardPrefs.copy(useChaosProxy = true)
        assertThat(chaosPrefs.effectiveWebSocketUrl).isEqualTo("ws://proxy.ultimo.pl:8090/ws")
    }
}
