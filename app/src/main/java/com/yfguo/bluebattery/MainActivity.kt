package com.yfguo.bluebattery

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import java.util.*

private val UUID_BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanLeDevices()
    }

    private fun scanLeDevices() {
        if (bluetoothAdapter != null) {
            val scanner = bluetoothAdapter!!.bluetoothLeScanner
            val filterBuilder = ScanFilter.Builder()
            filterBuilder.setServiceUuid(ParcelUuid(UUID_BATTERY_SERVICE))
            val filter = filterBuilder.build()
            scanner.startScan(listOf(filter), scanCallback)
        }
    }

    private val scanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
        }
    }

}
