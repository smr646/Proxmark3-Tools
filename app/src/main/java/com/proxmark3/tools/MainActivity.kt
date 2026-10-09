package com.proxmark3.tools

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var outputText: TextView
    private lateinit var inputCmd: EditText
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val connectBtn = Button(this).apply { text = "CONNECT TO PM3" }
        val sendBtn = Button(this).apply { text = "SEND" }
        val clearBtn = Button(this).apply { text = "CLEAR" }

        outputText = TextView(this).apply {
            text = "Not connected.\nTap CONNECT first.\n"
            textSize = 12f
            setPadding(16, 16, 16, 16)
        }

        val scrollView = ScrollView(this).apply { addView(outputText) }

        inputCmd = EditText(this).apply { hint = "Ketik command (contoh: hw version)" }

        layout.addView(connectBtn)
        layout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        layout.addView(inputCmd)
        layout.addView(sendBtn)
        layout.addView(clearBtn)

        setContentView(layout)

        connectBtn.setOnClickListener { connectToPM3() }
        sendBtn.setOnClickListener { sendCommand() }
        clearBtn.setOnClickListener { outputText.text = "" }
    }

    private fun connectToPM3() {
        thread {
            try {
                socket?.close()
                val s = Socket()
                s.connect(InetSocketAddress("127.0.0.1", 18888), 5000)
                socket = s
                writer = PrintWriter(s.getOutputStream(), true)
                reader = BufferedReader(InputStreamReader(s.getInputStream()))

                runOnUiThread { outputText.append("Connected to PM3 bridge!\n") }

                var line: String?
                while (reader?.readLine().also { line = it } != null) {
                    val l = line ?: break
                    runOnUiThread { outputText.append("$l\n") }
                }
            } catch (e: Exception) {
                runOnUiThread { outputText.append("Connection error: ${e.message}\n") }
            }
        }
    }

    private fun sendCommand() {
        val cmd = inputCmd.text.toString().trim()
        if (cmd.isEmpty()) return
        thread {
            try {
                writer?.println(cmd)
                writer?.flush()
                runOnUiThread {
                    outputText.append("> $cmd\n")
                    inputCmd.setText("")
                }
            } catch (e: Exception) {
                runOnUiThread { outputText.append("Send error: ${e.message}\n") }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { socket?.close() } catch (_: Exception) {}
    }
}
