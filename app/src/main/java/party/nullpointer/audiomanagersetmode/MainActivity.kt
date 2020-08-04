package party.nullpointer.audiomanagersetmode

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle

private const val SAMPLE_RATE = 16000
private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val audioData = resources.openRawResource(R.raw.example).use {
            it.readBytes().toShortArray()
        }

        val minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODING)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val audioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(ENCODING)
            .setChannelMask(CHANNEL_CONFIG)
            .build()

        val audioTrack = AudioTrack(
            audioAttributes, audioFormat, minBufferSize,
            AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        audioTrack.play()

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        Thread {
            audioTrack.write(audioData, 0, audioData.size)
        }.start()
    }

    private fun ByteArray.toShortArray(): ShortArray = ShortArray(size / 2) {
        ((this[it * 2].toInt() and 0xff) or ((this[(it * 2) + 1].toInt() shl 8) and 0xff00)).toShort()
    }
}
