package com.proxmark3.tools

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialProber

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var usbManager: UsbManager
    private val ACTION_USB_PERMISSION = "com.proxmark3.tools.USB_PERMISSION"

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                if (granted) {
                    statusText.text = "Permission granted!\nScanning PM3..."
                    scanUsbDevices()
                } else {
                    statusText.text = "Permission ditolak."
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            gravity = android.view.Gravity.CENTER
        }

        statusText = TextView(this).apply {
            text = "Proxmark3 Tools\nTap button to detect PM3"
            textSize = 14f
            gravity = android.view.Gravity.CENTER
        }

        val checkBtn = Button(this).apply {
            text = "DETECT PM3"
        }

        layout.addView(statusText)
        layout.addView(checkBtn)
        setContentView(layout)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        registerReceiver(permissionReceiver, IntentFilter(ACTION_USB_PERMISSION))

        checkBtn.setOnClickListener {
            scanUsbDevices()
        }
    }

    private fun scanUsbDevices() {
        try {
            val deviceList = usbManager.deviceList
            if (deviceList.isEmpty()) {
                statusText.text = "Tidak ada USB device."
                return
            }

            val pm3 = deviceList.values.firstOrNull { it.vendorId == 0x9AC4 }

            if (pm3 == null) {
                statusText.text = "PM3 tidak terdeteksi.\nUSB devices: ${deviceList.size}"
                return
            }

            statusText.text = "PM3 Terdeteksi:\n" +
                    "Nama: ${pm3.deviceName}\n" +
                    "VID: ${pm3.vendorId}\n" +
                    "PID: ${pm3.productId}\n" +
                    "Permission: ${usbManager.hasPermission(pm3)}"

            if (!usbManager.hasPermission(pm3)) {
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_MUTABLE else 0
                val intent = PendingIntent.getBroadcast(
                    this, 0, Intent(ACTION_USB_PERMISSION), flags
                )
                usbManager.requestPermission(pm3, intent)
            }

        } catch (e: Exception) {
            statusText.text = "Error: ${e.message}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(permissionReceiver)
    }
}
