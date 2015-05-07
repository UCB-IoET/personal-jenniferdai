package com.example.jenniferdai.cookyourrice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String uri = "http://54.215.11.207:38001";

    Button onoff;
    Button keepWarm;
    TextView cookerStatus;
    TextView connection;
    private BluetoothAdapter mBluetoothAdapter;
    protected Map<String, String> uuidToKey;
    protected Map<String, String> keyToUuid;
    public static final int smapDelay = 5000;
    public static final int refreshPeriod = 5000;
    private Timer timer = null;
    private TimerTask timerTask;
    private String tempValue;
    public boolean runFlag = true;
    TextView displayData;
    String notification = "lowtemp";
    // NotificationManager mNotificationManager;
    int notificationID = 1283910;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get widgets to interact with
        onoff = ((Button) findViewById(R.id.onoff));
        keepWarm = ((Button) findViewById(R.id.warm));
        cookerStatus = ((TextView) findViewById(R.id.status));
        connection = ((TextView) findViewById(R.id.connection));
//        setAlarm = ((Button) findViewById(R.id.button5));
//        help = ((Button) findViewById(R.id.button6));

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
        scanLeDevice(!mScanning);

        displayData = ((TextView) findViewById(R.id.displayData));
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("firstStart", 1); // value to store
        editor.commit();
        int firstStart = settings.getInt("firstStart", 1);
        Log.d("firstStart", Integer.toString(firstStart));
        if(firstStart == 1) {
            //display your Message here
            editor.putInt("firstStart", 0);
            editor.commit();
            initMaps();
            //sendUpdate();
        }
        Button bt = (Button) findViewById(R.id.ntf);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notification();
            }
        });
    }

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    UUID serviceUUID = UUID.fromString("00003003-0000-1000-8000-00805f9b34fb");
    UUID characteristicUUID = UUID.fromString("00004005-0000-1000-8000-00805f9b34fb");
    UUID temperatureCharacteristicUUID = UUID.fromString("00004006-0000-1000-8000-00805f9b34fb");
    UUID recipeCharacteristicUUID = UUID.fromString("00004009-0000-1000-8000-00805f9b34fb");
    UUID eventCharacteristicUUID = UUID.fromString("00004008-0000-1000-8000-00805f9b34fb");
    UUID keepWarmCharacteristicUUID = UUID.fromString("00004007-0000-1000-8000-00805f9b34fb");
    private String deviceMac = "D9:DD:4C:5A:94:D1";
    BluetoothGatt mGatt = null;
    private boolean mScanning = false;
    private Handler mHandler = new Handler();

    private String status = "off";

//    String url = "http://[fe80::1cef:49e7:2661:ffda]:7001/a.MOV";
//    HttpParams httpParameters = new BasicHttpParams();
//    HttpClient client = new DefaultHttpClient(httpParameters);
//    HttpGet httpGet = new HttpGet(url);
//    HttpResponse response = client.execute(httpGet);

    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
             System.out.println("devie address " + device.getAddress());
            // System.out.println("uuids " + device.getUuids());
            if (device.getAddress().equals(deviceMac)) {
                System.out.println("found our firestorm");
                mGatt = device.connectGatt(getApplicationContext(), true, gattCallback);
                System.out.println(mGatt);
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
            try {
                super.onServicesDiscovered(gatt, status);
                System.out.println("discovered services!");
                BluetoothGattCharacteristic temp = mGatt.getService(serviceUUID).getCharacteristic(temperatureCharacteristicUUID);
                for (BluetoothGattDescriptor descriptor : temp.getDescriptors()) {
                    //find descriptor UUID that matches Client Characteristic Configuration (0x2902)
                    // and then call setValue on that descriptor

                    descriptor.setValue( BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    mGatt.writeDescriptor(descriptor);
                }

                //BluetoothGattDescriptor descriptor = temp.getDescriptor(temperatureCharacteristicUUID);
                //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                //mGatt.writeDescriptor(descriptor);
                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         connection.setText("connected");
                     }
                 });
            }
            catch (Exception e) {
                Log.d("exception", e.toString());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic){
            if(characteristic.getUuid().equals(temperatureCharacteristicUUID)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cookerStatus.setText("Temp: "+characteristic.getValue().toString());
                    }
                });
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int stat) {
            super.onCharacteristicWrite(gatt, characteristic, stat);
            System.out.println("onCharWrite: "+stat+", "+characteristic.getStringValue(0));
            if(characteristic.getUuid().equals(characteristicUUID)){
                if(characteristic.getValue()[0] == 0x00){
                    status = "off";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onoff.setText("Start");
                            cookerStatus.setText("Off");
                        }
                    });
                } else if(characteristic.getValue()[0] == 0x01){
                    status = "on";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onoff.setText("Stop");
                            cookerStatus.setText("On");
                        }
                    });
                }
            } else if(characteristic.getUuid().equals(keepWarmCharacteristicUUID)){
                if(characteristic.getValue()[0] == 0x00){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            keepWarm.setText("Keep Warm");
                            //cookerStatus.setText("Off");
                        }
                    });
                } else if(characteristic.getValue()[0] == 0x01){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            keepWarm.setText("Warming");
                            //cookerStatus.setText("On");
                        }
                    });
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(status == BluetoothGatt.GATT_SUCCESS){
                gatt.discoverServices();
            } else {
                System.out.println("gatt connection: " + status);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connection.setText("disconnected");
                    }
                });
            }
        }

    };

    public void toggleRiceCooker(View view) {
        if(onoff.getText().equals("Sending..."))
            return;
        BluetoothGattCharacteristic writeChar = mGatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
        if(status.equals("off"))
            writeChar.setValue(new byte[]{0x01});
        else
            writeChar.setValue(new byte[]{0x00});
        onoff.setText("Sending...");
        mGatt.writeCharacteristic(writeChar);
    }

    public void keepWarm(View view) {
        if(keepWarm.getText().equals("Sending..."))
            return;
        BluetoothGattCharacteristic writeChar = mGatt.getService(serviceUUID).getCharacteristic(keepWarmCharacteristicUUID);
        if(status.equals("off"))
            writeChar.setValue(new byte[]{0x01});
        else
            writeChar.setValue(new byte[]{0x00});
        keepWarm.setText("Sending...");
        mGatt.writeCharacteristic(writeChar);
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
        String[] dataArray = {"Temperature: " + tempValue};
        String[] dataTypes = {"string"};
        String templateName = "singleStringTemplate.html";
        if (Integer.parseInt(tempValue) > 20) {
            dataArray[0] = "Temp has reached 20! Your food is done!";
            //bcastObj.deviceCast(dataArray, dataTypes, templateName);
        }
        else
            //bcastObj.deviceCast(dataArray, dataTypes, templateName);
            Log.d("tempValue", tempValue);
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
                                //Log.d("runflag", Boolean.toString(runFlag));
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
//                                String[] dataArray = {"Temperature: " + value};
//                                String[] dataTypes = {"string"};
//                                String templateName = "singleStringTemplate.html";
//                                bcastObj.deviceCast(dataArray, dataTypes, templateName);
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

    private void notification() {
        Intent intent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
        intent.putExtra("msg", "hello");
        Log.d("jel", "hello");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Log.d("jel2", "hello");

        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("jel3", "hello");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("FOOD DONE!")
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Welcome"))
                .setAutoCancel(true)
                .setContentText("Elapsed time: 1 minutes 3 seconds")
                .setContentIntent(pendingIntent);
        Log.d("jel4", "hello");

        mNotificationManager.notify(1, mBuilder.build());
        Log.d("jel4", "hello");

//        Log.d("printed", "notif");
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setContentTitle("My notification")
//                        .setContentText("Hello World!");
//    // Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this, MainActivity.class);
//
//    // The stack builder object will contain an artificial back stack for the
//    // started Activity.
//    // This ensures that navigating backward from the Activity leads out of
//    // your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//    // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(MainActivity.class);
//    // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//// mId allows you to update the notification later on.
//        mNotificationManager.notify(0, mBuilder.build());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}