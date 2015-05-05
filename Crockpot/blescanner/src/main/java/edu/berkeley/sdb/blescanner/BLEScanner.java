package edu.berkeley.sdb.blescanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;

/**
 * Author: Siyuan He <siyuanhe@berkeley.edu> 2/16/2015
 */
public class BLEScanner {

    private BluetoothAdapter mBluetoothAdapter;
    private HashMap<String, Integer> bleMaps;
    private boolean mScanning =false;
    private Handler mHandler;
    private Context mContext;
//    private BluetoothLeScanner mScanner;

    private static final long SCAN_PERIOD = 1000;

    public BLEScanner(Context _context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("Bluetooth", "Initialization Failed. No bluetooth device on this machine.");
            throw new NullPointerException("No bluetooth device");
        }
        mHandler = new Handler();
        mContext = _context;
        bleMaps = new HashMap<String, Integer>();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mScanner = mBluetoothAdapter.getBluetoothLeScanner();
//        }
    }

    public boolean enable() {
        if (mBluetoothAdapter == null) {
            Log.e("Bluetooth", "Initialization Failed. No bluetooth device on this machine.");
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivity(turnOn);
            Log.d("Bluetooth", "Bluetooth enabled.");
        } else {
            Log.d("Bluetooth", "Bluetooth already enabled.");
        }
        return true;
    }

    public void disable() {
        mBluetoothAdapter.disable();
    }

    public void getBLEDeviceMap(final BluetoothScannerCallBack callback) {
        scanLeDevice(enable());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onMapReturned(new HashMap<String, Integer>(bleMaps));
            }
        }, SCAN_PERIOD);
    }

    public interface BluetoothScannerCallBack {
        public void onMapReturned(HashMap<String, Integer> map);
    }

    public void scanLeDevice(final boolean _enable) {
        if (_enable) {
            if (!mScanning) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mScanning) {
                            mScanning = false;
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        }
                    }
                }, SCAN_PERIOD);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    ScanSettings settings = new ScanSettings.Builder()
//                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                            .build();
//                    List<ScanFilter> filters = new ArrayList<ScanFilter>();
//                    mScanner.startScan(filters, settings, mLeScanCallback21);
//                } else {
                    mScanning = true;
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
//                }
            }
        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                mScanner.stopScan(mLeScanCallback21);
//            }
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    bleMaps.put(device.getAddress(), Integer.valueOf(rssi));
                }
            });
        }
    };

//    // Device scan callback for API 21
//    private ScanCallback mLeScanCallback21 = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            bleMaps.put(result.getDevice().getAddress(), Integer.valueOf(result.getRssi()));
//        }
//
//        @Override
//        public void onScanFailed(int errorCode) {
//            Log.e("Bluetooth", "Scan Failed");
//        }
//    };
}

