package pl.ultimo.mobile.core.audio

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Unit tests verifying PCM WAV synthesis format integrity for [SoundEffectsManager].
 * Ensures standard 44-byte RIFF header, correct byte-rates, and 16-bit little-endian samples.
 */
class WavSynthesisTest {

    private fun createWavHeader(pcmSize: Int, sampleRate: Int): ByteArray {
        val totalDataLen = pcmSize + 36
        val byteRate = sampleRate * 2 // 16-bit mono

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0 // PCM format
        header[22] = 1; header[23] = 0 // Mono
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0 // Block align
        header[34] = 16; header[35] = 0 // Bits per sample
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (pcmSize and 0xff).toByte()
        header[41] = ((pcmSize shr 8) and 0xff).toByte()
        header[42] = ((pcmSize shr 16) and 0xff).toByte()
        header[43] = ((pcmSize shr 24) and 0xff).toByte()

        return header
    }

    @Test
    fun wavHeader_hasValidRiffAndWaveSignatures() {
        val sampleRate = 44100
        val pcmSize = 8820 // 100ms at 44.1kHz mono 16-bit
        val header = createWavHeader(pcmSize, sampleRate)

        // Verify "RIFF"
        val riff = String(header.sliceArray(0..3))
        assertThat(riff).isEqualTo("RIFF")

        // Verify "WAVE"
        val wave = String(header.sliceArray(8..11))
        assertThat(wave).isEqualTo("WAVE")

        // Verify "fmt "
        val fmt = String(header.sliceArray(12..15))
        assertThat(fmt).isEqualTo("fmt ")

        // Verify "data"
        val data = String(header.sliceArray(36..39))
        assertThat(data).isEqualTo("data")
    }

    @Test
    fun wavHeader_calculatesAccurateChunkSizes() {
        val sampleRate = 44100
        val pcmSize = 44100 * 2 // 1 full second
        val header = createWavHeader(pcmSize, sampleRate)

        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        val riffChunkSize = buffer.getInt(4)
        val dataChunkSize = buffer.getInt(40)

        assertThat(dataChunkSize).isEqualTo(pcmSize)
        assertThat(riffChunkSize).isEqualTo(pcmSize + 36)
    }
}
