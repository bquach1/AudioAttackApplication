package com.example.toasttest

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class MainActivity : ComponentActivity() {

    private var backgroundThread: MyThread? = null
    private var threadRunning = false

    // Helper function to show a toast message
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        backgroundThread?.notifyThread()
        super.onCreate(savedInstanceState)

        setContent {
            ComposeThread()
        }
    }

    // Jetpack compose for UI
    @Composable
    fun ComposeThread() {
        ToastTestTheme {
            val context = LocalContext.current
            val buttonTextState = remember { mutableStateOf("Start Thread") }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Start Thread
                Button(
                    onClick = {
                        toggleThread()
                        Log.d("MyButton", "Button clicked. Thread running: $threadRunning")
                        if (threadRunning) {
                            buttonTextState.value = "Stop Thread"
                        } else {
                            buttonTextState.value = "Start Thread"
                        }
                    },
                ) {
                    Text(buttonTextState.value)
                }

                // Send Hello Message
                Button(
                    onClick = {
                        if (threadRunning) {
                            showToastAndNotifyThread(context)
                        } else {
                            showToast(context, "Thread is not running")
                        }
                    },
                ) {
                    Text("Hello")
                }

                if (buttonTextState.value == "Stop Thread") {
                    Text("Thread is running")
                } else {
                    Text("Thread is not running")
                }
            }
        }
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
