package pl.ultimo.mobile.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ultimo_user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val USE_CHAOS_PROXY = booleanPreferencesKey("use_chaos_proxy")
        val CHAOS_PROXY_URL = stringPreferencesKey("chaos_proxy_url")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val AUTO_DECLARE_ULTIMO = booleanPreferencesKey("auto_declare_ultimo")
        val SAVED_PLAYER_HANDLE = stringPreferencesKey("saved_player_handle")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                serverUrl = preferences[PreferencesKeys.SERVER_URL] ?: UserPreferences.DEFAULT_SERVER_URL,
                useChaosProxy = preferences[PreferencesKeys.USE_CHAOS_PROXY] ?: false,
                chaosProxyUrl = preferences[PreferencesKeys.CHAOS_PROXY_URL] ?: UserPreferences.DEFAULT_CHAOS_PROXY_URL,
                soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true,
                autoDeclareUltimo = preferences[PreferencesKeys.AUTO_DECLARE_ULTIMO] ?: false,
                savedPlayerHandle = preferences[PreferencesKeys.SAVED_PLAYER_HANDLE] ?: "Player_1"
            )
        }

    suspend fun updateServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = url
        }
    }

    suspend fun updateChaosProxySettings(useChaosProxy: Boolean, proxyUrl: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_CHAOS_PROXY] = useChaosProxy
            if (proxyUrl != null) {
                preferences[PreferencesKeys.CHAOS_PROXY_URL] = proxyUrl
            }
        }
    }

    suspend fun updateAudioSettings(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = soundEnabled
            preferences[PreferencesKeys.HAPTICS_ENABLED] = hapticsEnabled
        }
    }

    suspend fun updateSavedPlayerHandle(handle: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_PLAYER_HANDLE] = handle
        }
    }

    suspend fun updateAutoDeclareUltimo(autoDeclare: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_DECLARE_ULTIMO] = autoDeclare
        }
    }
}
