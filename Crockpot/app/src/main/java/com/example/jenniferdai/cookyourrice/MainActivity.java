package com.example.jenniferdai.cookyourrice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Future;


public class MainActivity extends Activity {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String uri = "http://54.215.11.207:38001"; // wtf is this
    Button onoff;
    Button keepWarm;
    TextView displayData;
    TextView cookerStatus;
    protected Map<String, String> uuidToKey;
    protected Map<String, String> keyToUuid;
    public static final int smapDelay = 5000;
    public static final int refreshPeriod = 5000;
    private Timer timer = null;
    private TimerTask timerTask;
    private String tempValue;
    public boolean runFlag = true;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 10000;
    private String deviceMac = "D9:DD:4C:5A:94:D1";
    BluetoothGatt mGatt = null;
    private boolean mScanning = false;
    private Handler mHandler = new Handler();
    UUID characteristicUUID = UUID.fromString("00004005-0000-1000-8000-00805f9b34fb");
    private String status = "off";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onoff = ((Button) findViewById(R.id.onoff));
        keepWarm = ((Button) findViewById(R.id.warm));
        displayData = ((TextView) findViewById(R.id.temp));
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("firstStart", 1); // value to store
        editor.commit();
        int firstStart = settings.getInt("firstStart", 1);
        Log.d("firstStart", Integer.toString(firstStart));
        if(firstStart == 1) {
            //display your Message here
            editor.putInt("firstStart", 0);
            // Log.d("hello", "helo");
            editor.commit();
            initMaps();
            sendUpdate();
        }

        // Initializes Bluetooth Adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //toggleRiceCooker(onoff);
        scanLeDevice(!mScanning);
    }

    private static String inputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        Log.d("reuslt", result);
        inputStream.close();
        return result;

    }
    private void initMaps() {
        uuidToKey = new HashMap<String, String>();
        uuidToKey.put("6f6b3306-ef7f-11e4-bc83-0001c0158419", "temperature");
        keyToUuid = new HashMap<String, String>();
        for(Map.Entry<String, String> entry : uuidToKey.entrySet()){
            keyToUuid.put(entry.getValue(), entry.getKey());
        }

    }

    protected void updateTemperatureText() {
        displayData.setText(tempValue+"");
        //displayData.setAllCaps(true);
    }

    public void sendUpdate() {
        rescheduleTimer();
    }

    private void rescheduleTimer() {
        rescheduleTimer(smapDelay);
    }

    private void rescheduleTimer(int delay) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.querySmapView(null);
            }
        };
        timer.scheduleAtFixedRate(timerTask, delay, refreshPeriod);
    }

    protected boolean updatePref(String value) {
        tempValue = value;
        return true;
    }

    private class SmapQueryAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String uuid;
        public static final String QUERY_LINE = "select data before now where uuid = '6f6b3306-ef7f-11e4-bc83-0001c0158419'";
        public SmapQueryAsyncTask(String uuid) {
            super();
            this.uuid = uuid;
        }
        @Override
        protected Boolean doInBackground(String...urls) {
            for (String url : urls) {
                final String uri = url;
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (runFlag) {
                            try {
                                Log.d("runflag", Boolean.toString(runFlag));
                                DefaultHttpClient httpclient = new DefaultHttpClient();
                                //HttpPost httpPostReq = new HttpPost(uri);
                                URI address = new URI("http", null, "54.215.11.207", 8079, "/api/tags/uuid/6f6b3306-ef7f-11e4-bc83-0001c0158419", "", "");
                                //HttpGet httpGetReq = new HttpGet(address);
                                HttpPost httpPostReq = new HttpPost("http://shell.storm.pm:8079/api/query");
                                StringEntity se = new StringEntity(QUERY_LINE);
                                httpPostReq.setEntity(se);
                                HttpResponse httpResponse = httpclient.execute(httpPostReq);
                                Log.d("httpResponse", httpResponse.toString());
                                InputStream inputStream = httpResponse.getEntity().getContent();
                                final String response = inputStreamToString(inputStream);
                                Log.d("httpPost", response);
                                JSONObject jsonResponse = new JSONObject(response.substring(1, response.length() - 1));
                                JSONArray readings = ((JSONArray) jsonResponse.getJSONArray("Readings")).getJSONArray(0);
                                String retUuid = jsonResponse.getString("uuid");
                                Log.d("retUuid", retUuid);
                                String value = readings.getString(1);
                                Log.d("value", value);
                                MainActivity.this.updatePref(value);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.updateTemperatureText();
                                    }
                                });

                            } catch (Exception e) {
                                Log.d("httpPost", "failed");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Please Check Connection", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                e.printStackTrace();
                            }
                        }
                    }
                });
                t.run();
            }
            return true;
        }
        // onPostExecute displays the results of the AsyncTask.
        protected void onPostExecute(String result) {
        }
    }

    public void querySmapView(View v) {
        for(String uuid : uuidToKey.keySet()) {
            querySmap(uuid);
        }
    }

    private void querySmap(String uuid) {
        new SmapQueryAsyncTask(uuid).execute("http://shell.storm.pm:8079/api/query");
    }

    String ricecookerUUID = "6f6b3306-ef7f-11e4-bc83-0001c0158419";
    // Stops scanning after 10 seconds.
    UUID serviceUUID = UUID.fromString("00003003-0000-1000-8000-00805f9b34fb");

    public void toggleRiceCooker(View view) {
//        if (onoff.getText().equals("Sending..."))
//            return;
//        BluetoothGattCharacteristic writeChar = mGatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
//        if (status.equals("off"))
//            writeChar.setValue(new byte[]{0x01});
//        else
//            writeChar.setValue(new byte[]{0x00});
//        onoff.setText("Sending...");
//        mGatt.writeCharacteristic(writeChar);
    }

    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //System.out.println("devie address " + device.getAddress());
            // System.out.println("uuids " + device.getUuids());
            if (device.getAddress().equals(deviceMac)) {
                // System.out.println("found our firestorm");
                mGatt = device.connectGatt(getApplicationContext(), true, gattCallback);
                // System.out.println(mGatt);
            }
        }
    };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            System.out.println("discovered services!");
            // runOnUiThread(() {
//                    connecting.setVisibility(View.GONE);
//                    send.setVisibility(View.VISIBLE);
            // });
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int stat) {
            super.onCharacteristicWrite(gatt, characteristic, stat);
            System.out.println("onCharWrite: " + stat + ", " + characteristic.getStringValue(0));
            if (characteristic.getUuid().equals(characteristicUUID)) {
                if (characteristic.getValue()[0] == 0x00) {
                    status = "off";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onoff.setText("Start");
                            //cookerStatus.setText("Off");
                        }
                    });
                } else if (characteristic.getValue()[0] == 0x01) {
                    status = "on";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onoff.setText("Stop");
                            //cookerStatus.setText("On");
                        }
                    });
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices();
            } else {
                System.out.println("gatt connection: " + status);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        start.setVisibility(View.GONE);
//                        connecting.setVisibility(View.VISIBLE);
//                        send.setVisibility(View.GONE);
                    }
                });
            }
        }
    };

}