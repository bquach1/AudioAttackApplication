package com.example.toasttest

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter

    class MainActivity : ComponentActivity() {

        private var backgroundThread: MyThread? = null
        private var threadRunning = false

        private var audioRecord: AudioRecord? = null
        private var isRecording = false
        private val bufferSize = AudioRecord.getMinBufferSize(
            44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )

        private var audioFile: File? = null
        private var audioFilePath: String? = null
        private var fileOutputStream: FileOutputStream? = null

        // Helper function to show a toast message
        private fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        override fun onCreate(savedInstanceState: Bundle?) {

            ActivityCompat.requestPermissions(
            this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                0
            )

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
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
                val context = LocalContext.current
                val buttonTextState = remember { mutableStateOf("Start Thread") }
                val recordingState = remember { mutableStateOf(false) }

                ToastTestTheme {
                    Button(onClick = {
                        toggleThread()
                        buttonTextState.value = buttonTextState.value
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
                        Environment.getExternalStorageDirectory().absolutePath,
                        "audio_record.pcm"
                    )
                    audioFilePath = audioFile?.absolutePath
                    fileOutputStream = FileOutputStream(audioFile)

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        return@Thread
                    }

                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
                    )

                    val buffer = ByteArray(bufferSize)
                    audioRecord?.startRecording()
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
            } else {
                // Stop the thread
                threadRunning = false
                backgroundThread?.stopThread()
                backgroundThread = null
            }
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

        private fun showToastAndNotifyThread(context: Context) {
            if (backgroundThread != null) {
                // Notify the waiting thread
                backgroundThread?.notifyThread()

                // Show a toast message
                Handler(Looper.getMainLooper()).post {
                    showToast(context, "Hello")
                }
            } else {
                // Thread not running, show a message
                showToast(context, "Thread is not running")
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

