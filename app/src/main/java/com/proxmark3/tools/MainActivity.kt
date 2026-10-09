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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var checkBtn: Button
    private lateinit var usbManager: UsbManager

    private val ACTION_USB_PERMISSION = "com.proxmark3.tools.USB_PERMISSION"

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                if (granted) {
                    statusText.text = "USB Permission Granted!\nSiap komunikasi dengan PM3."
                } else {
                    statusText.text = "USB Permission DITOLAK."
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        checkBtn = findViewById(R.id.checkBtn)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        registerReceiver(permissionReceiver, IntentFilter(ACTION_USB_PERMISSION))

        checkBtn.setOnClickListener {
            checkUsbDevices()
        }
    }

    private fun checkUsbDevices() {
        val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (drivers.isEmpty()) {
            statusText.text = "Tidak ada USB Serial device terdeteksi."
            return
        }

        val driver = drivers[0]
        val device: UsbDevice = driver.device

        statusText.text = "Terdeteksi USB:\nNama: ${device.deviceName}\nVID: ${device.vendorId} | PID: ${device.productId}"

        if (usbManager.hasPermission(device)) {
            statusText.append("\n\nPermission sudah ada. Siap connect.")
            connectToDevice(driver)
        } else {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            val intent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), flags)
            usbManager.requestPermission(device, intent)
        }
    }

    private fun connectToDevice(driver: com.hoho.android.usbserial.driver.UsbSerialDriver) {
        try {
            val connection = usbManager.openDevice(driver.device) ?: run {
                statusText.append("\nGagal buka koneksi USB.")
                return
            }
            val port: UsbSerialPort = driver.ports[0]
            port.open(connection)
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            statusText.append("\n\nBerhasil connect ke PM3 di 115200 baud!")
        } catch (e: Exception) {
            statusText.append("\n\nError connect: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(permissionReceiver)
    }
}
