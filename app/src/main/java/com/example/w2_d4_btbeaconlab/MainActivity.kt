package com.example.w2_d4_btbeaconlab

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val data = ArrayList<ItemsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)







        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        button.setOnClickListener {
            if(hasPermissions()) {
                startScan()


            } else {
                hasPermissions()
            }
        }


    }

    private var mScanResults: HashMap<String, ScanResult>? = null
    companion object {
        const val SCAN_PERIOD: Long = 3000
    }
    private fun startScan() {
        data.clear()
        Log.d("DBG", "startScan")
        Toast.makeText(applicationContext,"startScan()",Toast.LENGTH_SHORT).show()

        mScanResults = HashMap()

        var mScanCallback = BtleScanCallback()
        var mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        val filter: List<ScanFilter>? = null
// Stops scanning after a pre-defined scan period.
        var mHandler = Handler()
        mHandler!!.postDelayed({mBluetoothLeScanner.stopScan(mScanCallback)}, SCAN_PERIOD)
        Toast.makeText(applicationContext,"stopScan()",Toast.LENGTH_SHORT).show()
        var mScanning = true
        mBluetoothLeScanner!!.startScan(filter, settings, mScanCallback)
    }

    /*private fun stopScan() {
        Log.d("DBG", "stopScan")
        var mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner

    }*/

    private inner class BtleScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d("DBG", "onScanResult ${result.device}")

            addScanResult(result)
        }
        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)

            }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.d("DBG", "BLE Scan Failed with code $errorCode")
        }
        private fun addScanResult(result: ScanResult) {
            val device = result.device
            val deviceName = device.name ?: "N/A"
            val deviceAddress = device.address
            mScanResults!![deviceAddress] = result
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        data.add(ItemsViewModel(deviceName,
                            result.device.address,result.rssi.toString(),result.isConnectable))
                val adapter = CustomAdapter(data.distinctBy { it.deviceAddress })
                        recyclerview.adapter = adapter

                    Log.d("DBG", "result.device.name ${deviceName}")
                    Log.d("DBG", "Device address: $deviceAddress (${result.isConnectable})")
                    Log.d("DBG","data.distinctBy: ${data.distinctBy { it.deviceAddress }}" )


            }
        }
    }

    private fun hasPermissions(): Boolean {
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            Log.d("DBG", "No Bluetooth LE capability")
            Toast.makeText(applicationContext,"No Bluetooth LE capability please open bluetooth :)",Toast.LENGTH_SHORT).show()

            return false
        } else if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "No fine location access please open location :)")
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1);
            return true // assuming that the user grants permission
        }
        return true
    }
}
