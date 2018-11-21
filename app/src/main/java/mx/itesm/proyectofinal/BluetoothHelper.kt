package mx.itesm.proyectofinal

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import java.io.IOException
import java.io.InputStream
import java.util.*

class BluetoothHelper(activity: Activity) {
    private var mConnectThread: ConnectThread?
    var mConnectedThread: ConnectedThread?
    var mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    init {
        mConnectThread = null
        mConnectedThread = null

        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, 1)
        } else {
            startConnection()
        }
    }

    fun startConnection() {
        var mDevice: BluetoothDevice? = null
        val pairedDevices = mBluetoothAdapter.bondedDevices
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                mDevice = device
            }
        }

        mConnectThread = ConnectThread(mDevice!!)
        mConnectThread?.start()

        mConnectedThread = ConnectedThread(mConnectThread?.mmSocket!!)
        mConnectedThread?.start()
    }

    fun closeConnection() {
        mConnectThread?.cancel()
        mConnectedThread?.cancel()
    }

    inner class ConnectThread(device: BluetoothDevice) : Thread() {
        var mmSocket: BluetoothSocket?
        var mmDevice: BluetoothDevice?
        private val GENERIC_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")!!

        init {
            var tmp: BluetoothSocket? = null
            mmDevice = device
            try {
                tmp = device.createRfcommSocketToServiceRecord(GENERIC_UUID)
            } catch (e: IOException) {
            }
            mmSocket = tmp!!
        }

        override fun run() {
            mBluetoothAdapter.cancelDiscovery()
            try {
                mmSocket?.connect()
            } catch (connectException: IOException) {
                try {
                    mmSocket?.close()
                } catch (closeException: IOException) {
                }

                return
            }

        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
            }

        }
    }

    inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        val mmInStream: InputStream?

        init {
            var tmpIn: InputStream? = null
            try {
                tmpIn = mmSocket.inputStream
            } catch (e: IOException) {
            }

            mmInStream = tmpIn
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
            }

        }
    }
}