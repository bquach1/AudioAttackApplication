package com.example.toasttest

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis
import com.example.toasttest.ui.theme.ToastTestTheme
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

const val HELLO_MSG = 1
const val PING_PONG_MSG = 2
const val QUIT_MSG = 3

class MainActivity : ComponentActivity() {

    private var isThreadRunning = false

    // Helper function to show a toast message
    private fun showToast(context: android.content.Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var mainThreadHandler: Handler? = null

        // Counter
        var counter = 0
        // How many threads we will create
        var numberOfThreads = 1000

        // Measure time to create 1000 threads
        var time = measureTimeMillis {
            // Create new threads on for loop
            for (i in 0..numberOfThreads) {
                thread() {
                    counter++
                }
            }
        }

        println("Created $numberOfThreads threads in $time ms")

        // How many coroutines we will create
        var numberOfCoroutines = 1000

        // Measure time to create 1000 coroutines
        time = measureTimeMillis {
            // Create new coroutines on for loop
            for (i in 0..numberOfCoroutines) {
                GlobalScope.launch {
                    counter++
                }
            }
        }

        println("Created $numberOfCoroutines coroutines in $time ms")

        // How many iteration on for loop
        var forLoopCount = 100000

        time = measureTimeMillis {
            // Now we do samething in main thread
            for (i in 0..forLoopCount) {
                counter++
            }
        }

        Log.v("ToniWesterlund", "Do  $forLoopCount iteration in $time ms")

        // Create and start Thread
        val mySimpleThread = SimpleThread()
        mySimpleThread.start()

        // Create and start runnable (Thread)
        val mySimpleRunnable = SimpleRunnable()
        val myThread = Thread(mySimpleRunnable)
        myThread.start()

        mainThreadHandler = object : Handler(Looper.getMainLooper()) {

            override fun handleMessage(msg: Message) {
                if (HELLO_MSG == msg.what) {
                    Log.v("ToniWesterlund", "Hello World ${Thread.currentThread()}")
                }
            }
        }

        val myWorkerRunnable = WorkerRunnable(mainThreadHandler, context)
        val myWorkerThread = Thread(myWorkerRunnable)

        setContent {
            ToastTestTheme {
                val context = LocalContext.current
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Start Thread
                    Button(
                        onClick = {
                            myWorkerThread.start()
                        },
                    ) {
                        Text("Start Thread")
                    }

                    // Send Hello Message
                    Button(
                        onClick = {
                            val msg = Message()
                            msg.what = HELLO_MSG
                            msg.obj = "Hello"
                            myWorkerRunnable.workerRunnableHandler?.sendMessage(msg)
                            showToast(context, "Hello")
                        },
                    ) {
                        Text("Hello")
                    }

                    // Send Ping Pong Message
                    Button(
                        onClick = {
                            val msg = Message()
                            msg.what = PING_PONG_MSG
                            msg.obj = "PING PONG"
                            myWorkerRunnable.workerRunnableHandler?.sendMessage(msg)
                            showToast(context, "Ping Pong")
                        },
                    ) {
                        Text("Ping Pong")
                    }

                    // Send Quit Message
                    Button(
                        onClick = {
                            val msg = Message()
                            msg.what = QUIT_MSG
                            msg.obj = "Quit"
                            myWorkerRunnable.workerRunnableHandler?.sendMessage(msg)
                            showToast(context, "Quit")
                        },
                    ) {
                        Text("Quit")
                    }

                    // Display a message
                    Text("Hello There")
                }
            }
        }
    }

    class WorkerRunnable(val mainThreadHandler: Handler?, val context: Context) : Runnable {

        var workerRunnableHandler: Handler? = null

        override fun run() {

            // Loop start Point
            Looper.prepare()

            Log.v("Test", "Run Start")

            Toast.makeText(context, "Thread Run", Toast.LENGTH_SHORT).show()

            workerRunnableHandler = object : Handler(Looper.myLooper()!!) {
                override fun handleMessage(msg: Message) {

                    // Handle Messages
                    if (HELLO_MSG == msg.what) {
                        Log.v("ToastTest", "Hello World ${Thread.currentThread()} ")
                    } else if (PING_PONG_MSG == msg.what) {
                        Log.v("ToastTest", "PING")
                        val msgReply = Message()
                        msgReply.what = HELLO_MSG
                        msgReply.obj = "Hello"

                        // Reply Message back to main thread
                        mainThreadHandler!!.sendMessage(msgReply)

                    } else if (QUIT_MSG == msg.what) {
                        Log.v("ToniWesterlund", "QUIT_MSG")
                        Looper.myLooper()?.quit()
                    }

                }
            }
            // Loop End Point
            Looper.loop()
        }
    }

    // SimpleThread Class, Inherited from Thread class
    class SimpleThread : Thread() {

        public override fun run() {
            Log.v("ToniWesterlund", "Thread Run - ${Thread.currentThread()} has run")
        }
    }

    // SimpleRunnable, Implements Runnable interface
    class SimpleRunnable : Runnable {
        override fun run() {
            Log.v("ToniWesterlund", "Runnable Run - ${Thread.currentThread()} has run")
        }

    }
}