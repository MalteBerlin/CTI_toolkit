package com.example.firebase;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


import static com.example.firebase.Home_activity.SHARDED_PREFS;
import static com.example.firebase.Home_activity.Stored_bounded_device;
import static com.example.firebase.Loginactivity.BLE_DB;
import static java.lang.String.valueOf;

public class Bind_device_activity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final int REQUEST_ENABLE_BT = 1;//Used for sending a Utils intent to enable the bluetooth if it is turned off

    //Scanning
    public static Bind_device_scanner mBtLescanner;//Allows the usage of the scanning functionalities
    public static HashMap<String, BTLE_Device> mBTDevicesHashMap;//List of the address of the devices
    public static HashMap<String, BTLE_Device> mBTDevicesHashMap_phone;
    public static ArrayList<BTLE_Device> mBTDevicesArrayList;//List of the devices
    public static BluetoothManager mBluetoothManager;//The manager for the bluetooth API's
    //Private variables
    private final static String TAG = Bind_device_activity.class.getSimpleName();//Debugging tag

    private BroadcastReceiver_BTState mBTStateUpdateReceiver;//Allows for the usage of the broadcast receiver to see the bluetooth state
    private Button btn_scan,btn_home,btn_unbind;//Buttons and text boxes //btn_view_data
    private int last_rssi;
    private boolean scanning = false;
    private byte[] last_scanRecord = {};//Create the array
    private String address,time, User_data;
    private Bind_device_list_adapter adapter;
    private Dialog dialog_bind_device;
    private Global_Settings global_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {//Creation method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_device);//Link the interface output file
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        global_settings = Global_Settings.getInstance();//Get access to the settings

        //Determine whether there is bluetooth on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");//Send the message to the toast service
            finish();
        }
        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());//Declare the receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBtLescanner = new Bind_device_scanner(this, 1000, -150);//Start the scanner btle. Run in the main activity with a duration of 7.5s and a signal strength in db -75
        }
        //Create the instances of the devices
        mBTDevicesHashMap = new HashMap<>();//Board
        mBTDevicesHashMap_phone = new HashMap<>();//Phone
        mBTDevicesArrayList = new ArrayList<>();//Devices
        mBTDevicesHashMap.clear();//Board
        mBTDevicesHashMap_phone.clear();//Phone
        mBTDevicesArrayList.clear();//Device list

        adapter = new Bind_device_list_adapter(this,R.layout.btle_device_list_item,mBTDevicesArrayList);

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);


        //Linking the buttons to their layout Id's
        btn_scan = (Button) findViewById(R.id.btn_bind_scan);
        btn_unbind = (Button) findViewById(R.id.btn_unbind);
        btn_home = (Button)findViewById(R.id.btn_bind_home);

        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);

        //Check if the bluetooth manager has been created
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d(TAG, "No bluetooth manager therefore error");//No bluetooth manager thus error
            }
        }
        //Bundle is used to identify the user's email address
        Bundle bundle = getIntent().getExtras();
        User_data = bundle.getString("User_data");
        //The onclick listeners and actions found
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Home();
            }
        });
        btn_unbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnBind();
            }
        });

    }

    protected void onStart() {//Called on start
        super.onStart();
        //registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }
    public void onDestroy() {//Call when destroying the object
        super.onDestroy();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//Used to pass the information from a change by the bluetooth receiver
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Utils.toast(getApplicationContext(), "Bluetooth turned on");
            } else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "Bluetooth not enabled");
            }
        }
    }
    public void startScan() {//Start the bluetooth scan given the button has been pressed
        Utils.toast(getApplicationContext(), "Scan Button Pressed");
        //Log.d(TAG, "Scan button pressed");
        if ((!mBtLescanner.ismScanning())& scanning == false) {
            btn_scan.setText("Stop Scan");//Set the button text
            mBTDevicesArrayList.clear();//Clear out the current list of devices
            mBTDevicesHashMap.clear();//Clear the hashmap of devices
            mBTDevicesHashMap_phone.clear();
            mBtLescanner.Type("Test");
            mBtLescanner.start();//Start scanning
            scanning = true;

        }
        else if(scanning == true) {//Scan in progress
            scanning = false;
            btn_scan.setText("Scan");
            stopScan();
            Utils.toast(getApplicationContext(), "Stopping scan");
        }
    }
    public void stopScan() {//Function to stop scanning bluetooth le devices
        mBtLescanner.stop();
    }
    public void add_Bind_Device(BluetoothDevice device, int new_rssi, byte[] scanRecord) {//Called via the scanner class given a device has been found//Filter here
        address = device.getAddress();//Get the mac address of the device
        //Log.d(TAG, "addDevice: selected device is board");
        if (!mBTDevicesHashMap.containsKey(address)) //Check if the device is not already listed and allowed
        {
            if((scanRecord[0] == 2 ) && (scanRecord[1] == 1 ) && (scanRecord[2] == 4 ) && (scanRecord[3] == 26 ) &&(scanRecord[4] == -1 ) &&(scanRecord[5] == -121 ) &&(scanRecord[6] == 4 )) {
                {
                    BTLE_Device btle_device = new BTLE_Device(device);//Add the device to the list
                    //Add the device to the list and set the internal member variables to what has been passed in
                    if (new_rssi != last_rssi) {//Update if not the same
                        btle_device.setRssi(new_rssi);
                        last_rssi = new_rssi;
                    }
                    if (scanRecord != last_scanRecord) {
                        btle_device.setScanRecord(scanRecord);
                        last_scanRecord = scanRecord;
                    }
                    mBTDevicesHashMap.put(address, btle_device);
                    mBTDevicesArrayList.add(btle_device);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
    private void Home() {//Logs out the user of the database and goes to the login screen.

        Intent myIntent = new Intent(Bind_device_activity.this, Home_activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data",User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        Bind_device_activity.this.startActivity(myIntent);
        finish();
    }
    private void UnBind()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);//Share preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Stored_bounded_device, "Null");
        editor.commit();//Save it
        Utils.toast(getApplicationContext(),"Unbound device");
    }
    @Override
    public void onClick(View v) {

    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Handle click events
        BTLE_Device selected_device;
        selected_device = mBTDevicesArrayList.get(position);
        Confirm_bind_device(selected_device.getAddress());
    }
    private void Confirm_bind_device(final String device_address)
    {
        dialog_bind_device = new Dialog(this);
        //Show the dialog
        dialog_bind_device.show();
        //Set the content view
        dialog_bind_device.setContentView(R.layout.dialog_bind_device);

        Button btn_start_yes = dialog_bind_device.findViewById(R.id.btn_bind_device_yes);
        Button btn_start_no  = dialog_bind_device.findViewById(R.id.btn_bind_device_no);
        dialog_bind_device.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btn_start_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Bind the device

                SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);//Share preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Stored_bounded_device, device_address.toString());
                editor.commit();//Save it
                global_settings.Set_Bound_Device(device_address);
                dialog_bind_device.dismiss();//Turn oof the yes / no dialog
                Utils.toast(getApplicationContext(),"Bound to: " + device_address);
                Home();
            }
        });
        btn_start_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_bind_device.dismiss();//Turn oof the yes / no dialog
            }
        });
    }
}




