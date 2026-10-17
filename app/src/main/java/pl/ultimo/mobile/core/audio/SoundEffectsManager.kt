package pl.ultimo.mobile.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository
import pl.ultimo.mobile.feature.game.model.SoundType
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin

/**
 * Low-latency audio manager backed by Android [SoundPool].
 *
 * Configured with [AudioAttributes.USAGE_GAME] and [AudioAttributes.CONTENT_TYPE_SONIFICATION]
 * for sub-10ms response times on device. Automatically synthesizes clean tonal audio streams
 * on startup, ensuring zero dependency on external bundled audio asset files.
 */
@Singleton
class SoundEffectsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(audioAttributes)
        .build()

    private val soundMap = mutableMapOf<SoundType, Int>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var isSfxEnabled = true
    private var volume = 0.8f

    init {
        // Observe preferences for volume and SFX toggle
        coroutineScope.launch {
            preferencesRepository.userPreferences.collect { prefs ->
                isSfxEnabled = prefs.isSfxEnabled
                volume = prefs.sfxVolume
            }
        }

        // Initialize and synthesize audio clips in background
        coroutineScope.launch {
            initializeSynthesizedSounds()
        }
    }

    /**
     * Plays the specified sound effect if SFX is enabled.
     */
    fun playSound(type: SoundType) {
        if (!isSfxEnabled) return

        val soundId = soundMap[type] ?: return
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    /**
     * Releases SoundPool native resources upon application termination.
     */
    fun release() {
        soundPool.release()
    }

    private fun initializeSynthesizedSounds() {
        try {
            val cacheDir = context.cacheDir

            // 1. CARD_PLAY: Short percussive click/snap (800Hz quick decay, 50ms)
            val playFile = File(cacheDir, "sfx_card_play.wav")
            generateWav(playFile, durationMs = 50, frequencies = floatArrayOf(880f, 440f), isPercussive = true)
            soundMap[SoundType.CARD_PLAY] = soundPool.load(playFile.absolutePath, 1)

            // 2. CARD_DRAW: Swift swoosh sound (ascending pitch, 80ms)
            val drawFile = File(cacheDir, "sfx_card_draw.wav")
            generateWav(drawFile, durationMs = 80, frequencies = floatArrayOf(300f, 600f), isPercussive = false)
            soundMap[SoundType.CARD_DRAW] = soundPool.load(drawFile.absolutePath, 1)

            // 3. MY_TURN: Elegant high chime (1046Hz C6 + 1318Hz E6, 180ms)
            val turnFile = File(cacheDir, "sfx_my_turn.wav")
            generateWav(turnFile, durationMs = 180, frequencies = floatArrayOf(1046.5f, 1318.5f), isPercussive = true)
            soundMap[SoundType.MY_TURN] = soundPool.load(turnFile.absolutePath, 1)

            // 4. ULTIMO_SHOUT: Bold fanfare brass chord (523Hz C5 + 659Hz E5 + 784Hz G5, 400ms)
            val ultimoFile = File(cacheDir, "sfx_ultimo.wav")
            generateWav(ultimoFile, durationMs = 400, frequencies = floatArrayOf(523.25f, 659.25f, 783.99f), isPercussive = false)
            soundMap[SoundType.ULTIMO_SHOUT] = soundPool.load(ultimoFile.absolutePath, 1)

            // 5. VICTORY: Major arpeggio fanfare (523Hz -> 659Hz -> 784Hz -> 1046Hz, 500ms)
            val victoryFile = File(cacheDir, "sfx_victory.wav")
            generateWav(victoryFile, durationMs = 500, frequencies = floatArrayOf(523.25f, 783.99f, 1046.5f), isPercussive = false)
            soundMap[SoundType.VICTORY] = soundPool.load(victoryFile.absolutePath, 1)

            // 6. DEFEAT: Minor descending chime (440Hz -> 392Hz -> 349Hz, 500ms)
            val defeatFile = File(cacheDir, "sfx_defeat.wav")
            generateWav(defeatFile, durationMs = 500, frequencies = floatArrayOf(440f, 349.23f), isPercussive = false)
            soundMap[SoundType.DEFEAT] = soundPool.load(defeatFile.absolutePath, 1)

            // 7. ERROR: Low warning buzz (140Hz square-like wave, 120ms)
            val errorFile = File(cacheDir, "sfx_error.wav")
            generateWav(errorFile, durationMs = 120, frequencies = floatArrayOf(140f, 160f), isPercussive = true)
            soundMap[SoundType.ERROR] = soundPool.load(errorFile.absolutePath, 1)

        } catch (e: Exception) {
            // Audio synthesis fallback: non-fatal if file I/O is restricted
        }
    }

    /**
     * Synthesizes an uncompressed 16-bit mono 44.1kHz PCM WAV file.
     */
    private fun generateWav(
        outputFile: File,
        durationMs: Int,
        frequencies: FloatArray,
        isPercussive: Boolean
    ) {
        val sampleRate = 44100
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcmData = ByteArray(totalSamples * 2) // 16-bit mono = 2 bytes per sample

        val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / totalSamples

            // Envelope: Percussive decay vs smooth fade-out
            val envelope = if (isPercussive) {
                (1.0 - progress) * (1.0 - progress)
            } else {
                sin(progress * Math.PI) // bell-curve envelope
            }

            // Sum sine waves
            var sampleVal = 0.0
            for (freq in frequencies) {
                sampleVal += sin(2.0 * Math.PI * freq * t)
            }
            sampleVal /= frequencies.size

            val sampleInt16 = (sampleVal * envelope * Short.MAX_VALUE * 0.85).toInt().toShort()
            buffer.putShort(sampleInt16)
        }

        // Write WAV Header (44 bytes) + PCM data
        FileOutputStream(outputFile).use { fos ->
            writeWavHeader(fos, totalSamples * 2, sampleRate)
            fos.write(pcmData)
        }
    }

    private fun writeWavHeader(fos: FileOutputStream, pcmSize: Int, sampleRate: Int) {
        val totalDataLen = pcmSize + 36
        val byteRate = sampleRate * 2 // 16-bit mono = 2 bytes/sample

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // subchunk1size (16 for PCM)
        header[20] = 1; header[21] = 0 // AudioFormat (1 = PCM)
        header[22] = 1; header[23] = 0 // NumChannels (1 = Mono)
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0 // BlockAlign (16-bit mono = 2 bytes)
        header[34] = 16; header[35] = 0 // BitsPerSample
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (pcmSize and 0xff).toByte()
        header[41] = ((pcmSize shr 8) and 0xff).toByte()
        header[42] = ((pcmSize shr 16) and 0xff).toByte()
        header[43] = ((pcmSize shr 24) and 0xff).toByte()

        fos.write(header)
    }
}
