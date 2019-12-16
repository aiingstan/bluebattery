package com.yfguo.bluebattery

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

private val UUID_BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
private const val SCAN_PERIOD = 10000L
private const val REQUEST_ENABLE_BT = 1
private const val MY_PERMISSION_ACCESS_COURSE_LOCATION = 1
private val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    private val mHandler = Handler()
    private lateinit var mBondedDevices: Set<BluetoothDevice>

    private val mBluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

//    private lateinit var mScanner: BluetoothLeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "created")
        Log.d(TAG, "checking location permission")
        ensureLocationService()
        Log.d(TAG, "finish checking location permission")
        ensureBluetooth()

        mBondedDevices = mBluetoothAdapter!!.bondedDevices

        scanLeDevices()
    }

    private fun ensureLocationService() {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSION_ACCESS_COURSE_LOCATION
            )
        } else {
            Log.d(TAG, "location permission is already granted")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_ACCESS_COURSE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "user allowed location permission")
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun ensureBluetooth() {
        Log.d(TAG, "the ble adapter is ${mBluetoothAdapter}")
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

    private fun scanLeDevices(enabled: Boolean = true) {
        Log.d(TAG, "call scan le device")
        if (mBluetoothAdapter != null) {
            val scanner = mBluetoothAdapter!!.bluetoothLeScanner
            Log.d(TAG, "scanner[$scanner] is enabled: $enabled")
            when(enabled) {
                true -> {
                    mHandler.postDelayed({
                        scanner.stopScan(scanCallback)
                    }, SCAN_PERIOD)
                    scanner.startScan(scanCallback)
//                    val settings = ScanSettings.Builder()
//                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                        .build()
//                    scanner.startScan(emptyList(), settings, scanCallback)
                }
                else -> {
                    scanner.stopScan(scanCallback)
                }
            }

        }
    }

    private val scanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d(TAG, "scan callback ($callbackType) with result: ${result!!.device}")
            if (result !== null) {
                val device = result.device
                val deviceName = device.name
                val deviceAddress = device.address
                Log.d(TAG, "$deviceName: $deviceAddress")
            }
            super.onScanResult(callbackType, result)
        }
    }

}
