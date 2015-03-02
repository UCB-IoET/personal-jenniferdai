package com.example.jenniferdai.nfc;

/**
 * Created by jenniferdai on 3/1/15.
 */

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;

import java.util.UUID;

/**
 * Activity for scanning and displaying available BLE devices.
 */
public class DeviceScanActivity extends ListActivity {

    private BluetoothAdapter mBluetoothAdapter;

}