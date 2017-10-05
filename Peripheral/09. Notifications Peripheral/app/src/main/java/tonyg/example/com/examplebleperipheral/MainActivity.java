package tonyg.example.com.examplebleperipheral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.AdvertiseCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import tonyg.example.com.examplebleperipheral.ble.BlePeripheral;
import tonyg.example.com.examplebleperipheral.ble.callbacks.BlePeripheralCallback;
import tonyg.example.com.examplebleperipheral.utilities.DataConverter;


/**
 * Create a Bluetooth Peripheral.  Android 5 required
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2015-12-21
 */
public class MainActivity extends AppCompatActivity {
    /** Constants **/
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    /** Bluetooth Stuff **/
    private BlePeripheral mBlePeripheral;


    /** UI Stuff **/
    private TextView mAdvertisingNameTV, mCharacteristicLogTV;
    private Switch mBluetoothOnSwitch,
            mAdvertisingSwitch,
            mCentralConnectedSwitch,
            mCharacteristicSubscribedSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // notify when bluetooth is turned on or off
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBleBroadcastReceiver, filter);


        loadUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        // stop advertising when the activity pauses
        mBlePeripheral.stopAdvertising();
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeBluetooth();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBleBroadcastReceiver);
    }


    /**
     * Load UI components
     */
    public void loadUI() {
        mAdvertisingNameTV = (TextView)findViewById(R.id.advertising_name);
        mCharacteristicLogTV = (TextView)findViewById(R.id.characteristic_log);
        mBluetoothOnSwitch = (Switch)findViewById(R.id.bluetooth_on);
        mAdvertisingSwitch = (Switch)findViewById(R.id.advertising);
        mCentralConnectedSwitch = (Switch)findViewById(R.id.central_connected);
        mCharacteristicSubscribedSwitch = (Switch)findViewById(R.id.characteristic_subscribed);

        mAdvertisingNameTV.setText(BlePeripheral.ADVERTISING_NAME);
    }



    /**
     * Initialize the Bluetooth Radio
     */
    public void initializeBluetooth() {
        // reset connection variables

        try {
            mBlePeripheral = new BlePeripheral(this, mBlePeripheralCallback);
        } catch (Exception e) {
            Toast.makeText(this, "Could not initialize bluetooth", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage());
            finish();
        }


        mBluetoothOnSwitch.setChecked(mBlePeripheral.getBluetoothAdapter().isEnabled());

        // should prompt user to open settings if Bluetooth is not enabled.
        if (!mBlePeripheral.getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            startAdvertising();
        }

    }

    /**
     * Start advertising Peripheral
     */
    public void startAdvertising() {
        Log.v(TAG, "starting advertising...");
        try {
            mBlePeripheral.startAdvertising();
        } catch (Exception e) {
            Log.e(TAG, "problem starting advertising");
        }
    }


    /**
     * Event trigger when BLE Advertising has stopped
     */
    public void onBleAdvertisingStarted() {
        mAdvertisingSwitch.setChecked(true);
    }

    /**
     * Advertising Failed to start
     */
    public void onBleAdvertisingFailed() {
        mAdvertisingSwitch.setChecked(false);
    }

    /**
     * Event trigger when BLE Advertising has stopped
     */
    public void onBleAdvertisingStopped() {
        mAdvertisingSwitch.setChecked(false);
    }

    /**
     * Event trigger when Central has connected
     *
     * @param bluetoothDevice
     */
    public void onBleCentralConnected(final BluetoothDevice bluetoothDevice) {
        mCentralConnectedSwitch.setChecked(true);
    }

    /**
     * Event trigger when Central has disconnected
     * @param bluetoothDevice
     */
    public void onBleCentralDisconnected(final BluetoothDevice bluetoothDevice) {
        mCentralConnectedSwitch.setChecked(false);
    }

    /**
     * Event trigger when Characteristic has been written to
     *
     * @param characteristic the Characterisic being written to
     * @param value the byte value being written
     */
    public void onBleCharacteristicWritten(final BluetoothGattCharacteristic characteristic, final byte[] value) {
        mCharacteristicLogTV.append("\n");
        mCharacteristicLogTV.append(DataConverter.bytesToHex(value) + " written to " + characteristic.getUuid().toString());

        // scroll to bottom of TextView
        final int scrollAmount = mCharacteristicLogTV.getLayout().getLineTop(mCharacteristicLogTV.getLineCount()) - mCharacteristicLogTV.getHeight();
        if (scrollAmount > 0) {
            mCharacteristicLogTV.scrollTo(0, scrollAmount);
        } else {
            mCharacteristicLogTV.scrollTo(0, 0);
        }
    }

    /**
     * Event trigger when characteristic has been subscribed to
     *
     * @param characteristic the Characteristic being subscribed to
     */
    public void onBleCharacteristicSubscribed(final BluetoothGattCharacteristic characteristic) {
        mCharacteristicSubscribedSwitch.setChecked(true);
    }

    /**
     * Event trigger when characteristic has been unsubscribed from
     *
     * @param characteristic The Characteristic being unsubscribed from
     */
    public void onBleCharacteristicUnsubscribed(final BluetoothGattCharacteristic characteristic) {
        mCharacteristicSubscribedSwitch.setChecked(false);
    }


    /**
     * When the Bluetooth radio turns on, initialize the Bluetooth connection
     */
    private final BroadcastReceiver mBleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.v(TAG, "Bluetooth turned off");
                        initializeBluetooth();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.v(TAG, "Bluetooth turned on");
                        startAdvertising();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };


    /**
     * Respond to changes to the Bluetooth Peripheral state
     */
    private final BlePeripheralCallback mBlePeripheralCallback = new BlePeripheralCallback() {

        public void onAdvertisingStarted() {
            Log.v(TAG, "Advertising started");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleAdvertisingStarted();
                }
            });
        }
        public void onAdvertisingFailed(int errorCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleAdvertisingFailed();
                }
            });
            switch (errorCode) {
                case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                    Log.e(TAG, "Failed to start advertising as the advertising is already started.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                    Log.e(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(TAG, "This feature is not supported on this platform.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                    Log.e(TAG, "Operation failed due to an internal error.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    Log.e(TAG, "Failed to start advertising because no advertising instance is available.");
                    break;
                default:
                    Log.e(TAG, "unknown problem");
            }
        }
        public void onAdvertisingStopped() {
            Log.v(TAG, "Advertising stopped");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleAdvertisingStopped();
                }
            });

        }
        public void onCentralConnected(final BluetoothDevice bluetoothDevice) {
            Log.v(TAG, "Central connected");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleCentralConnected(bluetoothDevice);
                }
            });

        }
        public void onCentralDisconnected(final BluetoothDevice bluetoothDevice) {
            Log.v(TAG, "Central disconnected");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleCentralDisconnected(bluetoothDevice);
                }
            });

        }
        public void onCharacteristicWritten(final BluetoothGattCharacteristic characteristic, final byte[] value) {
            Log.v(TAG, "Charactersitic written to");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleCharacteristicWritten(characteristic, value);
                }
            });

        }
        public void onCharacteristicSubscribedTo(final BluetoothGattCharacteristic characteristic) {
            Log.v(TAG, "Characteristic subscribed to");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleCharacteristicSubscribed(characteristic);
                }
            });

        }
        public void onCharacteristicUnsubscribedFrom(final BluetoothGattCharacteristic characteristic) {
            Log.v(TAG, "Characteristic unsubscribed from");


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleCharacteristicUnsubscribed(characteristic);
                }
            });

        }

    };

}
