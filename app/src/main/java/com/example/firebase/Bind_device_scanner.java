package com.example.firebase;
/*
This is the scanner class that enables the application to identify bluetooth low energy devices
 */
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;


import androidx.annotation.RequiresApi;

import com.example.firebase.MainActivity;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Bind_device_scanner {//Constructor
    private final static String TAG2 = MainActivity.class.getSimpleName();//Debugging tag
    private Bind_device_activity ba;
    private BluetoothAdapter mbluetoothAdapter;
    private BluetoothLeScanner myScanner;
    private boolean mScanning;//Used to see if the device is scanning or not
    private Handler mHandler;//Used to handle tasks
    private long scanPeriod;//The duration to scan for
    private int signalStrength;//How much power is received
    private boolean scan_type;//Perma scan or not
    private long scan_duration = 86400;//Another scan duration
    private String device_type;
    private String Sensor_Board_ID = "D3:68:62:99:90:C3";//"EF:B9:5F:F0:86:AD";//Used to help identify a device

    private int connectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";


    //Sensor_Board_ID = "D2:6A:B6:64:41:B6";
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//Needed for the scanning functionality to be performed
    public Bind_device_scanner(Bind_device_activity binddeviceadctivity, long scanPeriod, int signalStrength) {//Constructor also says to run within the main activity
        //Constructor
        ba = binddeviceadctivity;
        mHandler = new Handler();
        //Pass through the parameters that have been sent to the constructor from the main function
        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        final BluetoothManager bluetoothManager = (BluetoothManager) ba.getSystemService(Context.BLUETOOTH_SERVICE);//Create a reference to the bluetooth service to the phone

        mbluetoothAdapter = bluetoothManager.getAdapter();//References the bluetooth module on the phone

        myScanner = mbluetoothAdapter.getBluetoothLeScanner();//run in constructor

        device_type = "Phone";//Set to default so the field is not void
    }
    public boolean ismScanning() {//Determines if the bluetooth device is scanning
        return mScanning;//A boolean that is changed when the device is set to scan or finishes scanning
    }
    public void start() {//Used prior to starting the scan to ensure that the bluetooth adapter is enabled

        if (!Utils.checkbluetooth(mbluetoothAdapter)) {
            Utils.requestUserBluetooth(ba);//Start the bluetooth from the main activity but passed through the utils function
            ba.stopScan();//Stops scanning
        } else {
            scanLeDevice(true);//If the adapter is enabled then scan for ble devices.
        }
    }
    public void Type(String type)//Used to pass in the device type from the main activity so it can be identified for the scanning callback
    {
        device_type = type;//Set the passed in type either board or phone
    }
    public void stop() {//Stops the device from scanning when called
        scanLeDevice(false);//Stop scanning the device
    }
    public void Scan_type_change(final boolean Scanning_type) {//Pass into the class the type of scan to perform perma scan or not
        scan_type = Scanning_type;
        Log.d(TAG2, "Scan_type_changed: " + scan_type);
    }
    private void scanLeDevice(final boolean enable) {//The function that finds the BTLE devices it requires scanning to be off and for an enable true to be passed in
        if ((enable) && (!mScanning)) {
            Utils.toast(ba.getApplication(), "Started ble scan");//Inform the user that the device has started a ble scan
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.toast(ba.getApplication(), "Stopping ble scan");//Inform the user when the scan has been stopped

                    mScanning = false;//Set scanning to be false
                    mbluetoothAdapter.stopLeScan(mleScanCallback);//Stop the call back
                    ba.stopScan();//Stop scanning

                    if (scan_type == true)//Set up the scanning duration based upon the slider for perma scan
                    {
                        scan_duration = 5000;//5 Seconds
                    } else if ((scan_type == false))//Use a default scanning period
                    {
                        scan_duration = 5000;//5 Seconds
                    }
                }
            }, scan_duration);
            mScanning = true;//State that the scanning is occuring

            mbluetoothAdapter.startLeScan(mleScanCallback);//Does run needs location permission enabled to work
        } else {
            mScanning = false;
            mbluetoothAdapter.stopLeScan(mleScanCallback);
        }
    }
    public static String ByteArrayToString(byte[] ba) {//Used to convert the scan recored to a printable string for debugging purposes
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");
        return hex.toString();
    }
    private BluetoothAdapter.LeScanCallback mleScanCallback = new BluetoothAdapter.LeScanCallback() {//Callback function is called from the LE scan if a device is found, this function handles it
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {// The device has been found and passed in the device, power and advert respectively.
            //Set the private member variables
            final int new_rssi = rssi;
            final byte[] new_scanRecord = scanRecord;
            mHandler.post(new Runnable() {//Once received the lescan can be run again
                @Override
                public void run() {//Add the device to the list of found devices depending upon the type of device, either a board of the phone
                    //Log.d(TAG2, "Type is:" + device_type);
                        ba.add_Bind_Device(device, new_rssi, new_scanRecord);
                }
            });
        }
    };

}
