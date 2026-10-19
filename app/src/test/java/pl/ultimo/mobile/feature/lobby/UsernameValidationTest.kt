package pl.ultimo.mobile.feature.lobby

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UsernameValidationTest {

    @Test
    fun validUsernames_alphanumeric_accepted() {
        assertThat(LobbyViewModel.isValidUsername("Player1")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("gamer_pro")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("Ace123")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("Bo")).isTrue()
    }

    @Test
    fun validUsernames_polishDiacritics_accepted() {
        assertThat(LobbyViewModel.isValidUsername("Stanisław")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("Michał")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("Żółw")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("Gęś")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("Łukasz")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("Święty")).isTrue()
    }

    @Test
    fun validUsernames_allowedSymbols_accepted() {
        assertThat(LobbyViewModel.isValidUsername("user-name")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("pro_player!")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("Alpha#1")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("[PRO]Sniper")).isTrue()
        assertThat(LobbyViewModel.isValidUsername("score=100")).isTrue()
    }

    @Test
    fun validUsernames_boundaryLengths() {
        assertThat(LobbyViewModel.isValidUsername("AB")).isTrue() // 2 chars
        assertThat(LobbyViewModel.isValidUsername("A".repeat(24))).isTrue() // 24 chars
    }

    @Test
    fun invalidUsernames_tooShort_rejected() {
        assertThat(LobbyViewModel.isValidUsername("")).isFalse()
        assertThat(LobbyViewModel.isValidUsername("A")).isFalse()
        assertThat(LobbyViewModel.isValidUsername("Ł")).isFalse()
    }

    @Test
    fun invalidUsernames_tooLong_rejected() {
        assertThat(LobbyViewModel.isValidUsername("A".repeat(25))).isFalse()
    }

    @Test
    fun invalidUsernames_spaces_rejected() {
        assertThat(LobbyViewModel.isValidUsername("John Doe")).isFalse()
        assertThat(LobbyViewModel.isValidUsername(" Player")).isFalse()
        assertThat(LobbyViewModel.isValidUsername("Player ")).isFalse()
    }

    @Test
    fun invalidUsernames_controlCharactersAndNewlines_rejected() {
        assertThat(LobbyViewModel.isValidUsername("Player\n1")).isFalse()
        assertThat(LobbyViewModel.isValidUsername("Player\t1")).isFalse()
        assertThat(LobbyViewModel.isValidUsername("Player\r1")).isFalse()
        assertThat(LobbyViewModel.isValidUsername("Player\u00001")).isFalse()
        assertThat(LobbyViewModel.isValidUsername("Player\u001F")).isFalse()
    }

    @Test
    fun invalidUsernames_unsupportedCharacters_rejected() {
        assertThat(LobbyViewModel.isValidUsername("Player👑")).isFalse()
        assertThat(LobbyViewModel.isValidUsername("🚀Rocket")).isFalse()
    }
}
