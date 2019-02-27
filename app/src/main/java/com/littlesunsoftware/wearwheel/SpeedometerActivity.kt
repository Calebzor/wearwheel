package com.littlesunsoftware.wearwheel

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast

import com.littlesunsoftware.wearwheel.events.EventHub
import com.littlesunsoftware.wearwheel.events.EventType

import java.util.Date
import java.util.UUID

class SpeedometerActivity : WearableActivity() {

    private var mDeviceAddress: String? = null
    private var mBluetoothManager: BluetoothManager? = null
    private var mGatt: BluetoothGatt? = null
    private val mKingsongData = KingsongData()
    private var mPrimaryView: PrimaryView? = null
    private var startTime: Long? = Date().time
    private var lastUpdateAmbient: Long? = Date().time
    private val minAmbientUpdateMS: Long = 1000

    private val mGattCallback = object : BluetoothGattCallback() {
        var reconnected = false

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Logger.Log("Connected to GATT client. Attempting to start service discovery");
                gatt.discoverServices()
                mPrimaryView!!.hideSpinner()
                startTime = Date().time
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Logger.Log("Disconnected from GATT client");
                mPrimaryView!!.showSpinner()
                reconnected = true
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Toast.makeText(this@SpeedometerActivity, "No GATT", Toast.LENGTH_SHORT).show()
                return
            }
            Logger.Log("Services discovered");

            val characteristic = gatt.getService(KINGSONG_SERVICE)
                    .getCharacteristic(KINGSONG_CHARACTERISTIC)
            gatt.setCharacteristicNotification(characteristic, true)

            val descriptor = characteristic.getDescriptor(KINGSONG_DESCRIPTOR)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            readKingsongCharacteristic(characteristic)
        }

        private fun readKingsongCharacteristic(characteristic: BluetoothGattCharacteristic) {
            if (KINGSONG_CHARACTERISTIC == characteristic.uuid) {
                val data = characteristic.value
                mKingsongData.decodeKingSong(data)
                runOnUiThread { updateDisplay() }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            Logger.Log("Kingsong data changed", LogDetail.verbose);
            readKingsongCharacteristic(characteristic)
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (KINGSONG_DESCRIPTOR == descriptor.uuid) {
                Logger.Log("Descriptor written", LogDetail.verbose);
                if (!reconnected) requestNameData()
            }
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
        }
    }


    private var initialized = false

    override fun onDestroy() {
        super.onDestroy()
        mGatt!!.close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.speedometer)
        setAmbientEnabled()

        mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val intent = intent
        mDeviceAddress = intent.getStringExtra("deviceAddress")

        val isMockDevice = mDeviceAddress!!.compareTo("999") == 0

        if (!isMockDevice)
            connectWheel(mDeviceAddress!!)

        mPrimaryView = findViewById<View>(R.id.speedometer) as PrimaryView

        if (isMockDevice)
            mPrimaryView!!.hideSpinner()

        mPrimaryView!!.setOnApplyWindowInsetsListener { v, insets ->
            ChinSize = insets.systemWindowInsetBottom.toFloat()

            v.onApplyWindowInsets(insets)

            insets
        }
    }

    private fun connectWheel(deviceAddress: String) {

        val bluetoothAdapter = mBluetoothManager!!.adapter
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        mGatt = device.connectGatt(this, true, mGattCallback)
    }

    fun playHorn() {
        val data = ByteArray(20)
        data[0] = (-86).toByte()
        data[1] = 85.toByte()
        data[16] = (-120).toByte()
        data[17] = 20.toByte()
        data[18] = 90.toByte()
        data[19] = 90.toByte()
        val c = mGatt!!.getService(KINGSONG_SERVICE).getCharacteristic(KINGSONG_CHARACTERISTIC)
        c.value = data
        c.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        mGatt!!.writeCharacteristic(c)
    }

    fun requestNameData() {
        val data = ByteArray(20)
        data[0] = (-86).toByte()
        data[1] = 85.toByte()
        data[16] = (-101).toByte()
        data[17] = 20.toByte()
        data[18] = 90.toByte()
        data[19] = 90.toByte()
        val c = mGatt!!.getService(KINGSONG_SERVICE).getCharacteristic(KINGSONG_CHARACTERISTIC)
        c.value = data
        c.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        mGatt!!.writeCharacteristic(c)
    }

    fun requestSerialData() {
        val data = ByteArray(20)
        data[0] = (-86).toByte()
        data[1] = 85.toByte()
        data[16] = 99.toByte()
        data[17] = 20.toByte()
        data[18] = 90.toByte()
        data[19] = 90.toByte()
        val c = mGatt!!.getService(KINGSONG_SERVICE).getCharacteristic(KINGSONG_CHARACTERISTIC)
        c.value = data
        c.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        mGatt!!.writeCharacteristic(c)
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)

        val b = Bundle()
        b.putBoolean("ambient", true)
        EventHub.instance.post(EventType.AmbientDisplay, b)

        updateDisplay()
        lastUpdateAmbient = Date().time
    }

    override fun onUpdateAmbient() {
        super.onUpdateAmbient()

        updateDisplay()
    }

    override fun onExitAmbient() {
        updateDisplay()

        val b = Bundle()
        b.putBoolean("ambient", false)
        EventHub.instance.post(EventType.AmbientDisplay, b)

        super.onExitAmbient()
    }

    fun speedAlert(view: View) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 10, 200, 10)
        vibrator.vibrate(pattern, -1)
    }

    private fun getMaxSpeedKMh(name: String): Float {
        Logger.Log( "name is: $name", LogDetail.verbose)

        var nameParts = name.split("-")
        val model = (if (nameParts.count() > 1) nameParts[1] else "unknown").toLowerCase()

        return when(model) {
            "14b" -> 25f
            "14s" -> 31f
            "16b" -> 31f
            "16s" -> 35f
            "18s" -> 48f
            else -> 42f
        }
    }

    private fun updateDisplay() {
        val now = Date()
        val nowTime = now.time

        if (isAmbient) {
            if (nowTime - lastUpdateAmbient!! < minAmbientUpdateMS)
                return

            lastUpdateAmbient = nowTime
        }

        if (!initialized) {
            val maxSpeedUnits = Units().getRate(getMaxSpeedKMh(mKingsongData.name))
            mPrimaryView!!.setMaxSpeed(maxSpeedUnits)
            initialized = true
        }
        //        if(mKingsongData.getSpeed()>=34) {
        //            speedAlert(null);
        //        }

        mPrimaryView!!.setVoltage(mKingsongData.voltage)
        mPrimaryView!!.setSpeed(mKingsongData.speed)
        mPrimaryView!!.setCurrent(mKingsongData.current)
        mPrimaryView!!.setBatteryLevel(mKingsongData.battery)
        mPrimaryView!!.setTemp(mKingsongData.temperature)
        mPrimaryView!!.setClock(now)
        mPrimaryView!!.setDuration(now.time - startTime!!)
        mPrimaryView!!.setDistance(mKingsongData.distance)

        //mPrimaryView!!.refresh()
    }

    companion object {

        var ChinSize: Float = 0.toFloat()

        private val TAG = "SpeedometerActivity"

        private val KINGSONG_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        private val KINGSONG_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        private val KINGSONG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        internal fun dipToPx(dip: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, Resources.getSystem().displayMetrics)
        }
    }
}
