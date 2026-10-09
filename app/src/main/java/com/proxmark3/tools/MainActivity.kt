package com.proxmark3.tools

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("proxmark3-tools")
        }
    }

    external fun stringFromJNI(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus: TextView = findViewById(R.id.tvStatus)
        val btnTest: Button = findViewById(R.id.btnTest)

        // Ambil teks dari C++ JNI
        tvStatus.text = stringFromJNI()

        btnTest.setOnClickListener {
            checkConnectedDevices(tvStatus)
        }
    }

    private fun checkConnectedDevices(tvStatus: TextView) {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        if (deviceList.isEmpty()) {
            tvStatus.text = "Tidak ada perangkat USB terdeteksi via OTG."
            return
        }
        for (device in deviceList.values) {
            tvStatus.text = "Terdeteksi USB:\nNama: ${device.deviceName}\nVID: ${device.vendorId} | PID: ${device.productId}"
            break
        }
    }
}
