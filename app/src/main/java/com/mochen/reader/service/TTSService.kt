package com.mochen.reader.service

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import com.mochen.reader.R
import com.mochen.reader.presentation.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class TTSService : Service(), TextToSpeech.OnInitListener {

    private val binder = TTSBinder()
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private var currentText = ""
    private var sentences = listOf<String>()
    private var currentSentenceIndex = 0
    private var speechRate = 1.0f
    private var pitch = 1.0f

    private val notificationId = 1001
    private val channelId = "tts_channel"

    inner class TTSBinder : Binder() {
        fun getService(): TTSService = this@TTSService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        tts = TextToSpeech(this, this)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.CHINESE)
            isInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                }

                override fun onDone(utteranceId: String?) {
                    currentSentenceIndex++
                    if (currentSentenceIndex < sentences.size) {
                        speakNext()
                    } else {
                        _isPlaying.value = false
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    }
                }

                override fun onError(utteranceId: String?) {
                    _isPlaying.value = false
                }
            })
        }
    }

    fun speak(text: String) {
        if (!isInitialized) return

        currentText = text
        sentences = splitIntoSentences(text)
        currentSentenceIndex = 0

        speakNext()
    }

    private fun speakNext() {
        if (currentSentenceIndex < sentences.size) {
            val sentence = sentences[currentSentenceIndex]
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)
            tts?.speak(sentence, TextToSpeech.QUEUE_ADD, null, "sentence_$currentSentenceIndex")
            _currentPosition.value = currentSentenceIndex

            showNotification("正在朗读: ${sentences[currentSentenceIndex].take(20)}...")
        }
    }

    fun pause() {
        tts?.stop()
        _isPlaying.value = false
    }

    fun resume() {
        if (currentSentenceIndex < sentences.size) {
            speakNext()
        }
    }

    fun stop() {
        tts?.stop()
        _isPlaying.value = false
        currentSentenceIndex = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(speechRate)
    }

    fun setPitch(pitchValue: Float) {
        pitch = pitchValue.coerceIn(0.5f, 2.0f)
        tts?.setPitch(pitch)
    }

    private fun splitIntoSentences(text: String): List<String> {
        return text.split(Regex("[。！？；\n]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "TTS 朗读",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "语音朗读通知"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(text: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("墨阅朗读")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "暂停", null)
            .addAction(android.R.drawable.ic_media_previous, "上一句", null)
            .addAction(android.R.drawable.ic_media_next, "下一句", null)
            .build()

        startForeground(notificationId, notification)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
