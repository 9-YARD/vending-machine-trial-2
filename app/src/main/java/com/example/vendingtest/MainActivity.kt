package com.example.vendingtest

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ys.libvending.YsResultListener
import com.ys.libvending.YsVendingIF
import com.ys.libvending.YsVendingListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvAuthStatus: TextView
    private lateinit var btnSlot36: Button
    private lateinit var btnSlot31: Button
    private lateinit var tvResult: TextView
    private lateinit var tvLog: TextView
    private lateinit var scrollLog: ScrollView

    private val myVendingListener = MyVendingListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAuthStatus = findViewById(R.id.tv_auth_status)
        btnSlot36    = findViewById(R.id.btn_slot_36)
        btnSlot31    = findViewById(R.id.btn_slot_31)
        tvResult     = findViewById(R.id.tv_result)
        tvLog        = findViewById(R.id.tv_log)
        scrollLog    = findViewById(R.id.scroll_log)

        // Check auth and update indicator
        val authorized = YsVendingIF.getInstance().isAuthSuccess()
        if (authorized) {
            tvAuthStatus.text = "Authorized"
            tvAuthStatus.setTextColor(Color.parseColor("#2E7D32"))   // dark green
        } else {
            tvAuthStatus.text = "Not Authorized"
            tvAuthStatus.setTextColor(Color.parseColor("#C62828"))   // dark red
        }

        // Register live-event listener
        YsVendingIF.getInstance().registerListener(myVendingListener)

        btnSlot36.setOnClickListener { dispense(36) }
        btnSlot31.setOnClickListener { dispense(31) }
    }

    private fun dispense(slot: Int) {
        val mapShip = mutableMapOf<Any, Any>()
        mapShip["MsgType"]   = YsAidlConstant.MsgType.MSG_TYPE_SHIP
        mapShip["SlotNo"]    = slot
        mapShip["DropCheck"] = true
        mapShip["TransID"]   = System.currentTimeMillis().toString()
        mapShip["PayMethod"] = "255"
        mapShip["PayAmount"] = "1.0"
        mapShip["Order"]     = System.currentTimeMillis().toString()

        YsVendingIF.getInstance().ship(mapShip, object : YsResultListener() {
            override fun OnResult(map: MutableMap<Any?, Any?>?) {
                val status  = map?.get(YsResultKey.DRIVE_RESULT_STATUS)
                val errCode = map?.get(YsResultKey.DRIVE_RESULT_ERRCODE)
                val errMsg  = map?.get(YsResultKey.DRIVE_RESULT_ERRMSG)
                val slotNo  = map?.get(YsResultKey.DRIVE_RESULT_SLOTNO)
                val order   = map?.get(YsResultKey.DRIVE_RESULT_ORDER)

                val resultText =
                    "Status:  $status\n" +
                    "ErrCode: $errCode\n" +
                    "ErrMsg:  $errMsg\n" +
                    "SlotNo:  $slotNo\n" +
                    "Order:   $order"

                runOnUiThread {
                    tvResult.text = resultText
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        YsVendingIF.getInstance().unregisterListener(myVendingListener)
    }

    inner class MyVendingListener : YsVendingListener() {
        override fun OnResult(map: MutableMap<Any?, Any?>?) {
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            runOnUiThread {
                tvLog.append("[$timestamp] ${map.toString()}\n")
                // Auto-scroll to bottom
                scrollLog.post { scrollLog.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
    }
}
