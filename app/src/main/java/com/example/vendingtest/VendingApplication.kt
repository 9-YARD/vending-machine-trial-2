package com.example.vendingtest

import android.app.Application
import com.ys.libvending.YsVendingIF

class VendingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val mapInit = mutableMapOf<String, Any>()
        mapInit["PkgName"] = "com.example.vendingtest"
        mapInit["DeviceSeriPath1"] = "/dev/ttyS2"
        YsVendingIF.getInstance().initialize(applicationContext, mapInit)
    }
}
