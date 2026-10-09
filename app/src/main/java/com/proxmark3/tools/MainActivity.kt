package com.proxmark3.tools

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialProber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            gravity = android.view.Gravity.CENTER
        }

        val statusText = TextView(this).apply {
            text = "Proxmark3 Tools\nTap button to check USB"
            textSize = 16f
            gravity = android.view.Gravity.CENTER
        }

        val checkBtn = Button(this).apply {
            text = "CHECK USB DEVICES"
        }

        layout.addView(statusText)
        layout.addView(checkBtn)
        setContentView(layout)

        checkBtn.setOnClickListener {
            try {
                val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
                val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
                if (drivers.isEmpty()) {
                    statusText.text = "Tidak ada USB device terdeteksi."
                } else {
                    val d = drivers[0].device
                    statusText.text = "Terdeteksi:\n${d.deviceName}\nVID: ${d.vendorId}\nPID: ${d.productId}"
                }
            } catch (e: Exception) {
                statusText.text = "Error: ${e.message}"
            }
        }
    }
}
