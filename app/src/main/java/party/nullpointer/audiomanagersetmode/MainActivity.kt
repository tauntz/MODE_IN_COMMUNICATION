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

    private val minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODING)
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private val audioFormat = AudioFormat.Builder()
        .setSampleRate(SAMPLE_RATE)
        .setEncoding(ENCODING)
        .setChannelMask(CHANNEL_CONFIG)
        .build()
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        volumeControlStream = AudioManager.STREAM_VOICE_CALL

        Thread {
            val audioData = resources.openRawResource(R.raw.example).use {
                it.readBytes().toShortArray()
            }

            audioManager.isSpeakerphoneOn = true

            // Adding a delay between turning on the speakerphone and playing audio will trigger the
            // below audioTrack to play the audio at full volume. Sometimes at least on a Pixel 3a
            // running Android 10. It does not happen on a Pixel 1 running Android 10 though.
            Thread.sleep(10000)

            val audioTrack = AudioTrack(
                audioAttributes, audioFormat, minBufferSize,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            audioTrack.play()
            audioTrack.write(audioData, 0, audioData.size)
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.isSpeakerphoneOn = false
    }

    private fun ByteArray.toShortArray() = ShortArray(size / 2) {
        ((this[it * 2].toInt() and 0xff) or ((this[(it * 2) + 1].toInt() shl 8) and 0xff00)).toShort()
    }
}
