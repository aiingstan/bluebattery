package com.yfguo.bluebattery

import android.app.Activity
import android.app.Instrumentation
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import java.util.*

private val UUID_BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
private const val SCAN_PERIOD = 10000L
private const val REQUEST_ENABLE_BT = 1
private val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    private val mHandler = Handler()

    private val mBluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private lateinit var mScanner: BluetoothLeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "created")
        ensureBluetooth()
        scanLeDevices()
    }

    private fun ensureBluetooth() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        mBluetoothAdapter?.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != Activity.RESULT_OK) {
                Log.d(TAG, "user forbids bluetooth")
                return
            }
            Log.d(TAG, "bluetooth enabled")
        }
    }

    private fun getScanFilter(): ScanFilter {
        val filterBuilder = ScanFilter.Builder()
        filterBuilder.setServiceUuid(ParcelUuid(UUID_BATTERY_SERVICE))
        return filterBuilder.build()
    }

    private fun scanLeDevices(stop: Boolean = true) {
        Log.d(TAG, "call scan le device")
        if (mBluetoothAdapter != null) {
            val scanner = mBluetoothAdapter!!.bluetoothLeScanner
            when(stop) {
                true -> {
                    mHandler.postDelayed({
                        scanner.stopScan(scanCallback)
                    }, SCAN_PERIOD)
                    val settings = ScanSettings.Builder()
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build()
                    scanner.startScan(listOf(getScanFilter()), settings, scanCallback)
                }
                else -> {
                    scanner.stopScan(scanCallback)
                }
            }

        }
    }

    private val scanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d(TAG, "scan callback type: ${callbackType}")
            super.onScanResult(callbackType, result)
        }
    }

}
