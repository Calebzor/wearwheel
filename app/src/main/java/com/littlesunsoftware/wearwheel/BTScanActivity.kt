package com.littlesunsoftware.wearwheel

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.wearable.view.WatchViewStub
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast

import java.util.ArrayList

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.lang.IllegalStateException

class BTScanActivity : Activity() {

    private lateinit var mScanResultView: ListView
    private lateinit var mSpinnerView: SpinnerView
    private lateinit var mResultListAdapter: ScanResultListAdapter

    private var scanning = false

    internal var wheelSelectListener: AdapterView.OnItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        val deviceAddress = mResultListAdapter.getItem(position)!!.device.address
        Logger.Log("WHEEL SELECTED: $deviceAddress")
        startSpeedometerActivity(deviceAddress)
        finish()
    }

    internal var scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Logger.Log( "onScanResult: $result", LogDetail.verbose)
            dismissSpinner()
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            Logger.Log( "onBatchScanResults: $results", LogDetail.verbose)
            mResultListAdapter.clear()
            for (s in results) {
                val dn = s.scanRecord!!.deviceName
                if (dn != null && dn.startsWith("KS"))
                    mResultListAdapter.add(s)
            }
            mResultListAdapter.notifyDataSetInvalidated()

            if (mResultListAdapter.count > 0)
                dismissSpinner()
        }

        override fun onScanFailed(errorCode: Int) {
            Logger.Log( "onScanFailed: $errorCode")
            showToast("Scan failed")
            dismissSpinner()
        }
    }

    private fun dismissSpinner() {
        mSpinnerView.apply {
            visibility = View.GONE
            stopSpinning()
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun startSpeedometerActivity(deviceAddress: String) {
        val intent = Intent(this@BTScanActivity, SpeedometerActivity::class.java)
        intent.putExtra("deviceAddress", deviceAddress)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btscan)
        val stub = findViewById<View>(R.id.watch_view_stub) as WatchViewStub

        mResultListAdapter = ScanResultListAdapter(this,
                R.layout.bluetooth_item,
                ArrayList())

        stub.setOnLayoutInflatedListener { stub ->
            mScanResultView = stub.findViewById<View>(R.id.lstScanResults) as ListView
            mScanResultView.adapter = mResultListAdapter
            mScanResultView.onItemClickListener = wheelSelectListener

            mSpinnerView = stub.findViewById<View>(R.id.spinnerView) as SpinnerView
            mSpinnerView.startSpinnning()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Logger.Log( "onRequestPermissionsResult")
        if (requestCode == 1) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Logger.Log("onResume, already having permission. starting scan")
            startScan()
        } else {
            Logger.Log( "onResume, requesting permissions")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.Log( "onPause, stopping scan if running")
        stopScan()
    }

    private fun startScan() {
        if (!scanning) {
            Logger.Log("Starting scan")

            val scanner = BluetoothLeScannerCompat.getScanner()
            val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setUseHardwareBatchingIfSupported(false)
                    .setReportDelay(1000)
                    .build()

            try {
                scanner.startScan(ArrayList(), settings, scanCallback)
                scanning = true
            }
            catch(ise: IllegalStateException) {
                Toast.makeText(applicationContext, "Bluetooth is not on!", Toast.LENGTH_LONG)
                startSpeedometerActivity("999")
            }
        }
    }

    private fun stopScan() {
        if (scanning) {
            Logger.Log( "Stopping scan")

            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(scanCallback)
            scanning = false
        }
    }
}
