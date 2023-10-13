package com.example.toasttest

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
    private fun showToast(context: android.content.Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        backgroundThread?.notifyThread()
        super.onCreate(savedInstanceState)

        setContent {
            ComposeThread()
        }
    }

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
            backgroundThread = MyThread()
            backgroundThread?.start()
        } else {
            // Stop the thread
            threadRunning = false
            backgroundThread?.stopThread()
            backgroundThread = null
        }
    }

    private fun showToastAndNotifyThread(context: android.content.Context) {
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

class MyThread : Thread() {
    private var isRunning = true
    private val lock = Object()

    override fun run() {
        while (isRunning) {
            synchronized(lock) {
                try {
                    lock.wait()
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

//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.widget.Toast
//import androidx.activity.compose.setContent
//import androidx.compose.material.Button
//import androidx.compose.material.Text
//import androidx.activity.ComponentActivity
//import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import com.example.toasttest.ui.theme.ToastTestTheme
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.runtime.*
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.text.input.ImeAction
//import com.example.toasttest.MyThread
//
//class MainActivity : AppCompatActivity() {
//    private var backgroundThread: MyThread? = null
//    private var threadRunning = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MyComposeApp()
//        }
//    }
//
//    @Composable
//    fun MyComposeApp() {
//        var messageText by remember { mutableStateOf("Hello There") }
//
//        ToastTestTheme {
//            Column(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Start Thread
//                Button(
//                    onClick = {
//                        toggleThread()
//                    },
//                ) {
//                    Text(if (threadRunning) "Stop Thread" else "Start Thread")
//                }
//
//                Button(
//                    onClick = {
//                        if (threadRunning) {
//                            showToastAndNotifyThread()
//                        } else {
//                            Toast.makeText(applicationContext, "Thread is not running", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                ) {
//                    Text("Show Toast")
//                }
//                BasicTextField(
//                    value = messageText,
//                    onValueChange = { messageText = it },
//                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
//                    keyboardActions = KeyboardActions(onDone = {
//                        showToastAndNotifyThread()
//                    }),
//                    modifier = Modifier.padding(16.dp))
//            }
//        }
//    }
//
//    private fun toggleThread() {
//        if (backgroundThread == null) {
//            // Start the thread
//            threadRunning = true
//            backgroundThread = MyThread()
//            backgroundThread?.start()
//        } else {
//            // Stop the thread
//            threadRunning = false
//            backgroundThread?.stopThread()
//            backgroundThread = null
//        }
//    }
//
//    private fun showToastAndNotifyThread() {
//        if (backgroundThread != null) {
//            // Notify the waiting thread
//            backgroundThread?.notifyThread()
//
//            // Show a toast message
//            Handler(Looper.getMainLooper()).post {
//                Toast.makeText(applicationContext, "Hello", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            // Thread not running, show a message
//            Toast.makeText(applicationContext, "Thread is not running", Toast.LENGTH_SHORT).show()
//        }
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        backgroundThread?.interrupt()
//    }
//}
