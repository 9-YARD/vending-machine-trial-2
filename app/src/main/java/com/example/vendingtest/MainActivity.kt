package com.example.vendingtest

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private lateinit var btnScanSlots: Button
    private lateinit var tvResult: TextView
    private lateinit var tvLog: TextView
    private lateinit var scrollLog: ScrollView

    private val foundSlots = mutableListOf<Int>()
    private var scanSlot = 0
    private var scanning = false

    private val myVendingListener = MyVendingListener()
    private val authHandler = Handler(Looper.getMainLooper())
    private var authPollCount = 0

    private val authPollRunnable = object : Runnable {
        override fun run() {
            if (YsVendingIF.getInstance().isAuthSuccess()) {
                tvAuthStatus.text = "Authorized"
                tvAuthStatus.setTextColor(Color.parseColor("#2E7D32"))
            } else if (authPollCount < 20) {
                authPollCount++
                authHandler.postDelayed(this, 500)
            }
            // else: give up after 10 s, leave as "Not Authorized"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAuthStatus  = findViewById(R.id.tv_auth_status)
        btnSlot36     = findViewById(R.id.btn_slot_36)
        btnSlot31     = findViewById(R.id.btn_slot_31)
        btnScanSlots  = findViewById(R.id.btn_scan_slots)
        tvResult      = findViewById(R.id.tv_result)
        tvLog         = findViewById(R.id.tv_log)
        scrollLog     = findViewById(R.id.scroll_log)

        // Auth check is async — service connects ~1 s after init.
        // Show "Checking…" and poll until auth confirmed or 10 s elapsed.
        tvAuthStatus.text = "Checking auth…"
        tvAuthStatus.setTextColor(Color.parseColor("#F57F17"))   // amber
        authPollCount = 0
        authHandler.postDelayed(authPollRunnable, 500)

        // Register live-event listener
        YsVendingIF.getInstance().registerListener(myVendingListener)

        btnSlot36.setOnClickListener { dispense(36) }
        btnSlot31.setOnClickListener { dispense(31) }
        btnScanSlots.setOnClickListener { startSlotScan() }
    }

    private fun startSlotScan() {
        if (scanning) return
        scanning = true
        foundSlots.clear()
        scanSlot = 0
        tvLog.text = ""
        btnScanSlots.isEnabled = false
        btnScanSlots.text = "Scanning…"
        runOnUiThread { tvResult.text = "Scanning slots 1–100…" }
        scanNextSlot()
    }

    private fun scanNextSlot() {
        scanSlot++
        if (scanSlot > 100) {
            // Done
            runOnUiThread {
                scanning = false
                btnScanSlots.isEnabled = true
                btnScanSlots.text = "Scan Slots 1–100"
                val summary = if (foundSlots.isEmpty()) "No working slots found."
                              else "Working slots: ${foundSlots.joinToString(", ")}"
                tvResult.text = summary
                tvLog.append("[SCAN DONE] $summary\n")
                scrollLog.post { scrollLog.fullScroll(ScrollView.FOCUS_DOWN) }
            }
            return
        }

        val slot = scanSlot
        val map = mutableMapOf<Any, Any>()
        map["MsgType"]   = YsAidlConstant.MsgType.MSG_TYPE_SHIP_TEST
        map["SlotNo"]    = slot
        map["DropCheck"] = false
        map["TransId"]   = System.currentTimeMillis().toString()

        YsVendingIF.getInstance().ship(map, object : YsResultListener() {
            override fun OnResult(result: MutableMap<Any?, Any?>?) {
                val errMsg  = result?.get(YsResultKey.DRIVE_RESULT_ERRMSG)?.toString() ?: ""
                val errCode = result?.get(YsResultKey.DRIVE_RESULT_ERRCODE)?.toString() ?: ""
                val slotExists = !errMsg.contains("slot", ignoreCase = true) &&
                                 !errMsg.contains("not found", ignoreCase = true) &&
                                 !errMsg.contains("no slot", ignoreCase = true) &&
                                 errCode != "S241" && errCode != "241"
                runOnUiThread {
                    tvResult.text = "Scanning slot $slot/100…"
                    if (slotExists) {
                        foundSlots.add(slot)
                        tvLog.append("Slot $slot: OK (ErrCode=$errCode ErrMsg=$errMsg)\n")
                        scrollLog.post { scrollLog.fullScroll(ScrollView.FOCUS_DOWN) }
                    }
                }
                // small delay between slots to avoid flooding the service
                authHandler.postDelayed({ scanNextSlot() }, 300)
            }
        })
    }

    private fun dispense(slot: Int) {
        val mapShip = mutableMapOf<Any, Any>()
        mapShip["MsgType"]   = YsAidlConstant.MsgType.MSG_TYPE_SHIP
        mapShip["SlotNo"]    = slot
        mapShip["DropCheck"] = true
        mapShip["TransId"]   = System.currentTimeMillis().toString()
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
        authHandler.removeCallbacks(authPollRunnable)
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
