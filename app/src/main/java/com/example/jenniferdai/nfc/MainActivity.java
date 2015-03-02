package com.example.jenniferdai.nfc;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
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
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    // Gets default NfcAdapter for the device
    EditText mNote;
    NfcAdapter mNfcAdapter;
    // token that allows the foreign application to use your application's permissions to
    // execute a predefined piece of code.
    PendingIntent mNfcPendingIntent;
    private BluetoothAdapter mBluetoothAdapter;
    // declares the capabilities of its parent component — what an activity or service can do
    // and what types of broadcasts a receiver can handle. It opens the component to receiving
    // intents of the advertised type, while filtering out those that are not meaningful for
    // the component.
    IntentFilter[] mWriteTagFilters;
    IntentFilter[] mNdefExchangeFilters;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        setContentView(R.layout.activity_main);
        // get widgets that you want to interact with
        mNote = ((EditText) findViewById(R.id.note));
        mNote.setText("new text");

        // Handle all of our received NFC intents in this activity.
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Intent filters for reading a note from a tag or exchanging over p2p.
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        // NDEF exchange mode
        System.out.println("Got new intent");
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] msgs = getNdefMessages(intent);
            promptForContent(msgs[0]);
        }
    }

    private NdefMessage getNoteAsNdef() {
        byte[] textBytes = mNote.getText().toString().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                new byte[] {}, textBytes);
        return new NdefMessage(new NdefRecord[] {
                textRecord
        });
    }

    boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    toast("Tag is read-only.");
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    toast("Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.");
                    return false;
                }

                ndef.writeNdefMessage(message);
                toast("Wrote message to pre-formatted tag.");
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        toast("Formatted tag and wrote message");
                        return true;
                    } catch (IOException e) {
                        toast("Failed to format tag.");
                        return false;
                    }
                } else {
                    toast("Tag doesn't support NDEF.");
                    return false;
                }
            }
        } catch (Exception e) {
            toast("Failed to write tag");
        }

        return false;
    }

    private void setNoteBody(String body) {
        Editable text = mNote.getText();
        text.clear();
        text.append(body);
    }
    
    private void promptForContent(final NdefMessage msg) {
        new AlertDialog.Builder(this).setTitle("Replace current content?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String body = new String(msg.getRecords()[0].getPayload());
                        setNoteBody(body);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();
    }

    public void onResume() {
        System.out.println("HELLOOOOOO");
        super.onResume();
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            System.out.println("WOOHOOO");
            String message = "";

            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    System.out.println("MSG " + i + " IS " + msgs[i].toString());
                    for (NdefRecord r : msgs[i].getRecords()) {
                        // Ignore first three bytes, UTF-8 en
                        System.out.println("toString:" + r.toString());
                        System.out.println("asbytes:" + r.getPayload());
                        System.out.println("payload length:" + r.getPayload().length);
                        System.out.println("mime: " + r.toMimeType());
                        System.out.println("tnf: " + r.getTnf());
                        System.out.println("Mm: " + r.TNF_MIME_MEDIA);
                        int k = 0;
                        for (byte b : r.getPayload()) {
                            // Ignores the first three bytes
                            if( k >= 3) {
                                System.out.println("> " + b);
                                message += Character.toString((char) b);
                            }
                            k += 1;
                        }
                    }
                }
                mNote.setText("You have discovered a NFC Tag!\n" + message);
            }
        } else {
            System.out.println("FFS");
        }
        //process the msgs array
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        boolean mResumed = false;
//        mNfcAdapter.disableForegroundNdefPush(this);
//    }

    NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    Character.toString((char) i);
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {
                        record
                });
                msgs = new NdefMessage[] {
                        msg
                };
            }
        } else {
            //Log.d(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private boolean mScanning = false;
    private Handler mHandler = new Handler();

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    UUID serviceUUID = UUID.fromString("00001337-0000-1000-8000-00805f9b34fb");
    UUID characteristicUUID = UUID.fromString("00001338-0000-1000-8000-00805f9b34fb");
    private String deviceMac = "E2:B8:CB:31:54:C3";
    BluetoothGatt mGatt = null;

    public void writeChar(){
        if(mGatt != null) {
            BluetoothGattService s = mGatt.getService(serviceUUID);
            BluetoothGattCharacteristic c = s.getCharacteristic(characteristicUUID);
            c.setValue("testing");
            mGatt.writeCharacteristic(c);
        }
    }

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            System.out.println("discovered services!");
            writeChar();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            System.out.println("onCharWrite: "+status+", "+characteristic.getStringValue(0));
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(status == BluetoothGatt.GATT_SUCCESS){
                gatt.discoverServices();
            } else {
                System.out.println("gatt connection: "+status);
            }
        }
    };


    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if(device.getAddress().equals(deviceMac)){
                System.out.println("found our firestorm");
                mGatt = device.connectGatt(getApplicationContext(), true, gattCallback);
            }
        }
    };

    public void testBLE(View v){
        if(mGatt == null){
            scanLeDevice(!mScanning);
        } else {
            writeChar();
        }
    }

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
}

