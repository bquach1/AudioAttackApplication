package com.example.toasttest

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.toasttest.ui.theme.ToastTestTheme
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : ComponentActivity() {

        private var backgroundThread: MyThread? = null
        private var audioThread: MyThread? = null
        private var threadRunning = false

        private enum class ThreadState {
            NOT_INIT, RUNNING, STOPPED
        }

        private var threadState = ThreadState.NOT_INIT

        private var audioRecord: AudioRecord? = null
        private var isRecording = false
        private val bufferSize = AudioRecord.getMinBufferSize(
            44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )

        private var audioFile: File? = null
        private var audioFilePath: String? = null
        private var fileOutputStream: FileOutputStream? = null
        override fun onCreate(savedInstanceState: Bundle?) {

            ActivityCompat.requestPermissions(
            this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                ),
                0
            )

            backgroundThread?.notifyThread()
            super.onCreate(savedInstanceState)

            setContent {
                ComposeThread()
            }
        }

        @Composable
        fun ComposeThread() {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttonTextState = remember { mutableStateOf("Start Thread") }
                val recordingState = remember { mutableStateOf(false) }

                ToastTestTheme {
                    Button(onClick = {
                        toggleThread()
                        if (threadState == ThreadState.RUNNING) {
                            buttonTextState.value = "Stop Thread"
                        } else {
                            buttonTextState.value = "Start Thread"
                        }
                    }) {
                        Text(buttonTextState.value)
                    }
                    Button(onClick = {
                        toggleThread()
                        if (recordingState.value) {
                            stopRecording()
                        } else {
                            startRecording()
                        }
                        recordingState.value = !recordingState.value
                    }) {
                        if (recordingState.value) {
                            Text("Stop Recording")
                        } else {
                            Text("Start Recording")
                        }
                    }

                    Button(
                        onClick = {
                            playRecordedAudio()
                        },
                    ) {
                        Text("Play Recorded Audio")
                    }

                    Button(
                        onClick = {
                            playAttackingNoise()
                        },
                    ) {
                        Text("Play Attacking Noise 1")
                    }

                    Button (
                        onClick = {
                            stopAttackingNoise()
                        },
                    ) {
                        Text("Stop Attacking Noise 1")
                    }

                    if (buttonTextState.value == "Stop Thread") {
                        Text("Thread is running")
                    } else {
                        Text("Thread is not running")
                    }
                }
            }
        }


        private fun startRecording() {
            val audioRecordingThread = Thread {
                try {
                    audioFile = File(
                        this.getExternalFilesDir(null),
                        "audio_record_${System.currentTimeMillis()}.pcm"
                    )
                    audioFilePath = audioFile?.absolutePath

                    fileOutputStream = FileOutputStream(audioFile)

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        return@Thread
                    }

                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                        44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
                    )

                    val buffer = ByteArray(bufferSize)
                    audioRecord?.startRecording()
                    threadState = ThreadState.RUNNING
                    isRecording = true

                    while (isRecording) {
                        val bytesRead = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                        fileOutputStream?.write(buffer, 0, bytesRead)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    audioRecord?.stop()
                    audioRecord?.release()
                    fileOutputStream?.close()
                    threadState == ThreadState.STOPPED
                }
            }
            audioRecordingThread.start()
        }

        private fun stopRecording() {
            isRecording = false
        }

        private fun toggleThread() {
            if (backgroundThread == null) {
                // Start the thread
                threadRunning = true
                backgroundThread = MyThread(this)
                backgroundThread?.start()
                threadState = ThreadState.RUNNING
            } else {
                // Stop the thread
                threadRunning = false
                backgroundThread?.stopThread()
                backgroundThread = null
                threadState = ThreadState.STOPPED
            }
        }

        private var playedAudioTrack: AudioTrack? = null

    private fun playAttackingNoise() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            44100,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize,
            AudioTrack.MODE_STREAM
        )

        val buffer = ByteArray(minBufferSize)

        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.mono_short_swap)

            audioTrack.play()

            val playbackThread = Thread {
                try {
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        audioTrack.write(buffer, 0, bytesRead)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    inputStream.close()
                    audioTrack.stop()
                    audioTrack.release()
                }
            }

            playbackThread.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun stopAttackingNoise() {
        playedAudioTrack?.stop()
        playedAudioTrack?.release()
    }

    private fun playRecordedAudio() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            44100,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize,
            AudioTrack.MODE_STREAM
        )

        val buffer = ByteArray(minBufferSize)

        val audioFile = File(audioFilePath)

        println(audioFilePath)

        if (audioFile.exists()) {
            val fileInputStream = FileInputStream(audioFile)
            audioTrack.play()

            while (fileInputStream.read(buffer) != -1) {
                audioTrack.write(buffer, 0, buffer.size)
            }

            fileInputStream.close()
            audioTrack.stop()
            audioTrack.release()
        }
    }

        override fun onDestroy() {
            super.onDestroy()
            backgroundThread?.interrupt()
        }
    }

class MyThread(private val context: Context) : Thread() {
    private var isRunning = true
    private val lock = Object()
    private val handler = Handler(Looper.getMainLooper())

    override fun run() {
        while (isRunning) {
            synchronized(lock) {
                try {
                    lock.wait()
                    handler.post {
                        Toast.makeText(context, "Thread run Hello", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: InterruptedException) {
                    println(e)
                }
            }
        }
    }

    fun stopThread() {
        isRunning = false
        synchronized(lock) {
            lock.notify()
        }
    }

    fun notifyThread() {
        synchronized(lock) {
            lock.notify()
        }
    }
}
