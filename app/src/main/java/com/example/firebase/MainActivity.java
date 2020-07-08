package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.tbruyelle.rxpermissions2.RxPermissions;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.example.firebase.Scanner_BTLE.isLocationEnabled;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static java.lang.String.*;
//Imports
import static com.example.firebase.Loginactivity.BLE_DB;

public class MainActivity extends AppCompatActivity {
    //Global variables
    //public static DatabaseReference mDatareff_phone, mDatareff_board, mDatareff_Sensor_board;//Firebase
    //public static DatabaseHelper BLE_DB;//DatabaseHelper Bluetooth_databases for phone and the boards
    //public static Calibration calibration;//Used to convert the integer value to a temperature value
    public static final int REQUEST_ENABLE_BT = 1;//Used for sending a Utils intent to enable the bluetooth if it is turned off

    //Scanning
    public static Scanner_BTLE mBtLescanner;//Allows the usage of the scanning functionalities
    public static HashMap<String, BTLE_Device> mBTDevicesHashMap;//List of the address of the devices
    public static HashMap<String, BTLE_Device> mBTDevicesHashMap_phone;
    public static ArrayList<BTLE_Device> mBTDevicesArrayList;//List of the devices
    public static BluetoothManager mBluetoothManager;//The manager for the bluetooth API's
    //Private variables
    private final static String TAG = MainActivity.class.getSimpleName();//Debugging tag

    private BroadcastReceiver_BTState mBTStateUpdateReceiver;//Allows for the usage of the broadcast receiver to see the bluetooth state
    private Interface_adapter mInterface_adapter;//The class that displays the information to the interface
    private Button btn_scan,btn_Delete_All,btn_See_graph,btn_home,btn_Connect;//Buttons and text boxes //btn_view_data
    private Switch differential_switch;
    private Calendar calendar;
    private int last_rssi;
    private LineChart main_linechart;
    private boolean scanning = false;
    private boolean delta;
    private byte[] last_scanRecord = {};//Create the array
    private String Data_Spinner_value = "Counter",Data_Spinner_value_2 = "T1", address,time, User_data;//"EF:B9:5F:F0:86:AD";

    private int data_display_max;
    private Global_Settings global_settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {//Creation method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//Link the interface output file
        global_settings = Global_Settings.getInstance();//Get access to the settings = Global_Settings.getInstance();//Get access to the settings
        //Determine whether there is bluetooth on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");//Send the message to the toast service
            finish();
        }
        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());//Declare the receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBtLescanner = new Scanner_BTLE(this, 1000, -150);//Start the scanner btle. Run in the main activity with a duration of 7.5s and a signal strength in db -75
        }
        mInterface_adapter = new Interface_adapter(this);
        //Create the instances of the devices
        mBTDevicesHashMap = new HashMap<>();//Board
        mBTDevicesHashMap_phone = new HashMap<>();//Phone
        mBTDevicesArrayList = new ArrayList<>();//Devices
        mBTDevicesHashMap.clear();//Board
        mBTDevicesHashMap_phone.clear();//Phone
        mBTDevicesArrayList.clear();//Device list

        calendar = Calendar.getInstance();//Get access to the calendar

        //Setup databases
        //BLE_DB = new DatabaseHelper(this);

        data_display_max = 0;//Set 0 values to be displayed to the graph
        //Linking the buttons to their layout Id's
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_Delete_All = (Button)findViewById(R.id.btn_Delete_All);
        btn_See_graph = (Button)findViewById(R.id.btn_See_graph);
        btn_home = (Button)findViewById(R.id.btn_home);
        btn_Connect = (Button)findViewById(R.id.btn_Connect);
        differential_switch = (Switch)findViewById(R.id.differential_switch);
        Spinner data_spinner_1 = findViewById(R.id.spinner_select_data_1);//For the device sending data
        Spinner data_spinner_2 = findViewById(R.id.spinner_select_data_2);//Used to display the second data

        //Setup the adapters and link them to the layout and the layout list
        ArrayAdapter<CharSequence> adapter_data_1 = ArrayAdapter.createFromResource(this,R.array.Data_selection,R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter_data_2 = ArrayAdapter.createFromResource(this,R.array.Data_selection_2,R.layout.support_simple_spinner_dropdown_item);
        adapter_data_1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_data_2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        data_spinner_1.setAdapter(adapter_data_1);
        data_spinner_2.setAdapter(adapter_data_2);

        main_linechart = (LineChart)findViewById(R.id.main_data_line_chart);

        data_spinner_1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//Used for seeing the data of the spinner so the correct data  can be displayed to the graph
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Data_Spinner_value = text;
                Log.d(TAG, "Data spinner is:" +  Data_Spinner_value);
                //graph_output();
                graph_output_multiple();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });
        data_spinner_2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Data_Spinner_value_2 = text;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                logout();
            }
        });
        btn_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connect();
            }
        });
        btn_Delete_All.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Delete_all_data();
            }
        });
        btn_See_graph.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View view) {
            //graph_output();
            graph_output_multiple();
            }});
        differential_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                delta = differential_switch.isChecked();
            }
        });


        //Request location
    }
    private void logout() {//Logs out the user of the database and goes to the login screen.
        Clear_screen(); Close();
        Intent myIntent = new Intent(MainActivity.this, Home_activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data",User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        MainActivity.this.startActivity(myIntent);
        finish();
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
            mBtLescanner.Type(global_settings.Get_Bound_device());
            mBtLescanner.start();//Start scanning
            scanning = true;
            //Log.d(TAG, "Onclick: Starting scan");
            //btn_scan.setText("Stop Scan");
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
    public void addDevice_Sensor_board(BluetoothDevice device, int new_rssi, byte[] scanRecord) {//Called via the scanner class given a device has been found
                address = device.getAddress();//Get the mac address of the device
                //Log.d(TAG, "addDevice: selected device is board");
                if (!mBTDevicesHashMap.containsKey(address) && global_settings.Get_Bound_device().equals(address)) {//Check if the device is not already listed and allowed
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
            Interface_output(device, scanRecord);//Pass the scan record through to the output function
        } else {
            boolean adchange = false;
            if (new_rssi != last_rssi) {//Update if not the same
                mBTDevicesHashMap.get(address).setRssi(new_rssi);//Change the devices listed signal strength
                last_rssi = new_rssi;
            }
            if(scanRecord[7]!= last_scanRecord[7]) {//Means the counter has changed////////////////////////////////////////////////////////////////change back to != for change in the counter
                mBTDevicesHashMap.get(address).setScanRecord(scanRecord);//Change the devices listed signal strength
                last_scanRecord = scanRecord;
                adchange = true;//Only update database if scan record is changed
            }//Counter value changed
            if (adchange == true) {//See if data has been updated.
                Interface_output(device, scanRecord);//Pass the scan record through to the output function
                //Add data to the database
                data_changes(last_scanRecord);
                adchange = false;
            }
        }
    }
    private void data_changes(byte[] last_scanRecord) {//Used to update the data when the scan record has changed
        Date date = new Date();//Get the time
        Calendar.getInstance();
        calendar.setTime(date);
        time = calendar.getTime().toString();
        String Pdiff_Data_string = "";

        int Counter = (last_scanRecord[7]);
        //Need to add check tolerances
        float T1 = (((last_scanRecord[8]) & 0xFF) << 8 | (last_scanRecord[9] & 0xFF));
        float T2 = (((last_scanRecord[10]) & 0xFF) << 8 | (last_scanRecord[11] & 0xFF));
        float T3 = (((last_scanRecord[12]) & 0xFF) << 8 | (last_scanRecord[13] & 0xFF));
        float T4 = (((last_scanRecord[14]) & 0xFF) << 8 | (last_scanRecord[15] & 0xFF));
        int P_pos_neg = last_scanRecord[16];//Determines if it is positive or negative
        float Pdiff = (((last_scanRecord[17]) & 0xFF) << 8 ) | ((last_scanRecord[18] & 0xFF));
        //Used to debug the values of temperature and pressure in the logcat
        //Log.d(TAG, "T1 is: " +T1);Log.d(TAG, "T2 is: " +T2);Log.d(TAG, "T3 is: " +T3);//Log.d(TAG, "P1 is: " +P1);Log.d(TAG, "P1 is: " +P2);
        //Set the string values to be pass
        String T1_Data_string =    Float.toString(T1/100);
        String T2_Data_string =    Float.toString(T2/100);
        String T3_Data_string =    Float.toString(T3/100);
        String T4_Data_string =    Float.toString(T4/100);
        if(P_pos_neg == 0){
            Pdiff_Data_string = Float.toString(-Pdiff/100);//Value is negative
        }
        else {
            Pdiff_Data_string = Float.toString(Pdiff / 100);
        }

        boolean result = BLE_DB.addData_Sensor_Board(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
        if (result == true) {Log.d(TAG, "data_changes: Added data correctly");}
        if (result == false) {Log.d(TAG, "data_changes: did not add data correctly");}

    }
    private void Connect(){//Function to connect to the GATT server
        //Code in scanner btle?
        Utils.toast(getApplicationContext(),"Connecting to Board");
        BTLE_Device device = mBTDevicesHashMap.get(global_settings.Get_Bound_device());

        if (device == null) return;
        Intent myIntent = new Intent(MainActivity.this, DeviceControlActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Device_Name", device.getName());//Pass through the device name
        bundle.putString("Device_Address", device.getAddress());
        bundle.putString("User_data", User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        if (mBtLescanner.ismScanning()) {
            stopScan();//Stop scanning
        }
        //startActivity(intent);
        MainActivity.this.startActivity(myIntent);//Run the device control activity}
        finish();
    }
    public void Close(){//Use to close the scan data
        /*
        if (!mBtLescanner.ismScanning()) {//Check if the device is not scanning then clear the data on the screen
            mBTDevicesArrayList.clear();//Clear out the current list of devices
            mBTDevicesHashMap.clear();//Clear the hashmap of devices
            Log.d(TAG, "Connection closed");
            btn_scan.setText("Scan");
            Clear_screen();//Clear interface
        */
        if (mBtLescanner.ismScanning()) {//Cannot close as the device is scanning therefore output data to the user
            Utils.toast(getApplicationContext(), "Closing Scan");
        }
        //Check if the device is not scanning then clear the data on the screen
        mBTDevicesArrayList.clear();//Clear out the current list of devices
        mBTDevicesHashMap.clear();//Clear the hashmap of devices
        Log.d(TAG, "Connection closed");
        btn_scan.setText("Scan");
        stopScan();
        Clear_screen();//Clear interface
    }
    public void Clear_screen() {
        setTitle("Scanner");//Change the title to Scanner as the device is no longer being read
        //Clear the data
        mInterface_adapter.Clear();
    }
    public void Interface_output(BluetoothDevice device, byte[] scanRecord){
        int _rssi = 0;

        setTitle("Device address" + address );//Set the title to the address of the board
        _rssi = mBTDevicesHashMap.get(device.getAddress()).getRssi();

        mInterface_adapter.Display(device,scanRecord, _rssi, global_settings.Get_Bound_device());
        //graph_output();
        graph_output_multiple();
    }
    private void Delete_all_data() {
        Utils.toast(getApplicationContext(), "Deleting All data");
        BLE_DB.deleteAll_Sensor_Board();//Delete the sensor board data
    }
    /*private void graph_output_multiple() {//Outputs the data to the graph depending upon which spinner selection has been made

        float x =0;
        float x2 = 0;
        float x3 = 0;
        float td1 = 0;
        float td2 = 0;

        float yaxis_delta = 0;
        float max_value = 0;
        float min_value = 0;

        ArrayList<Entry> yAxes = new ArrayList<>();


        ArrayList<Entry> board_values = new ArrayList<>();
        ArrayList<Entry> board_yAXES = new ArrayList<>();

        ArrayList<Entry> board_values_2 = new ArrayList<>();
        ArrayList<Entry> board_yAXES_2 = new ArrayList<>();

        ArrayList<Entry> board_values_delta = new ArrayList<>();
        ArrayList<Entry> board_yAXES_delta  = new ArrayList<>();
        XAxis xAxis = main_linechart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        Cursor data;

        data = BLE_DB.showData_Sensor_Board();//Show the sensor board database

        if (data.getCount() <= 60){
            data_display_max = data.getCount();
            data.moveToFirst();//Display live values
        }
        else if(data.getCount() > 60) {

            //Display only 100 last data values
            data.moveToLast();
            for (int i = 0; i < 60; i++) {
                data.moveToPrevious();
            }
            //data.move((data.getCount())-100);//Set the starting value to be 100th last value
            data_display_max = 60;
        }

        for (int i = 0; i < data_display_max; i++) {
            //Counter
            if (Data_Spinner_value.equals("T1")) {
                float yaxis = Float.parseFloat(valueOf(data.getString(1)));//Gets the t1 value and sets it on the y axis
                x = x + 1;//Move to next Id
                board_yAXES.add(new Entry(yaxis, i));
                board_values.add(new Entry(yaxis , (int)x));
                yAxes.add(new Entry((int)yaxis,(int)x));
                if(yaxis <=0){
                    if(yaxis< min_value) {
                        min_value = yaxis;
                    }
                }
                if(yaxis >=0){
                    if(yaxis> max_value) {
                        max_value = yaxis;
                    }
                }
            } else if (Data_Spinner_value.equals("T2")) {
                float yaxis = Float.parseFloat(valueOf(data.getString(2)));//Gets the t2 value and sets it on the y axis
                x = x + 1;//Move to next Id
                board_yAXES.add(new Entry(yaxis, i));
                board_values.add(new Entry(yaxis , (int)x));
                yAxes.add(new Entry((int)yaxis,(int)x));
                if(yaxis <=0){
                    if(yaxis< min_value) {
                        min_value = yaxis;
                    }
                }
                if(yaxis >=0){
                    if(yaxis> max_value) {
                        max_value = yaxis;
                    }
                }
            } else if (Data_Spinner_value.equals("T3")) {
                float yaxis = Float.parseFloat(valueOf(data.getString(4)));//Gets the t3 value and sets it on the y axis
                x = x + 1;//Move to next Id
                board_yAXES.add(new Entry(yaxis, i));
                board_values.add(new Entry(yaxis , (int)x));
                yAxes.add(new Entry((int)yaxis,(int)x));
                if(yaxis <=0){
                    if(yaxis< min_value) {
                        min_value = yaxis;
                    }
                }
                if(yaxis >=0){
                    if(yaxis> max_value) {
                        max_value = yaxis;
                    }
                }
            } else if (Data_Spinner_value.equals("T4")) {
                float yaxis = Float.parseFloat(valueOf(data.getString(5)));//Gets the t4 value and sets it on the y axis
                x = x + 1;//Move to next Id
                board_yAXES.add(new Entry(yaxis, i));
                board_values.add(new Entry(yaxis , (int)x));
                yAxes.add(new Entry((int)yaxis,(int)x));
                if(yaxis <=0){
                    if(yaxis< min_value) {
                        min_value = yaxis;
                    }
                }
                if(yaxis >=0){
                    if(yaxis> max_value) {
                        max_value = yaxis;
                    }
                }
            } else if (Data_Spinner_value.equals("Pd")) {
                float yaxis = Float.parseFloat(valueOf(data.getString(6)));//Gets the pd value and sets it on the y axis
                x = x + 1;//Move to next Id
                board_yAXES.add(new Entry(yaxis, i));
                board_values.add(new Entry(yaxis , (int)x));
                if(yaxis <=0){
                    if(yaxis< min_value) {
                        min_value = yaxis;
                    }
                }
                if(yaxis >=0){
                    if(yaxis> max_value) {
                        max_value = yaxis;
                    }
                }
            }
            if (Data_Spinner_value_2.equals("T1")) {
                float yaxis = Float.parseFloat(valueOf(data.getString(1)));//Gets the t1 value and sets it on the y axis
                x2 = x2 + 1;//Move to next Id
                board_yAXES_2.add(new Entry(yaxis, i));
                board_values.add(new Entry(yaxis , (int)x));
                yAxes.add(new Entry((int)yaxis,(int)x));
                if(yaxis <=0){
                    if(yaxis< min_value) {
                        min_value = yaxis;
                    }
                }
                if(yaxis >=0){
                    if(yaxis> max_value) {
                        max_value = yaxis;
                    }
                }
            } else if (Data_Spinner_value_2.equals("T2")) {
                float yaxis = Float.parseFloat(valueOf(data.getString(2)));//Gets the t2 value and sets it on the y axis
                x2 = x2 + 1;//Move to next Id
                board_yAXES_2.add(new Entry(yaxis, i));
                board_values.add(new Entry(yaxis , (int)x));
                yAxes.add(new Entry((int)yaxis,(int)x));
                if(yaxis <=0){
                    if(yaxis< min_value) {
                        min_value = yaxis;
                    }
                }
                if(yaxis >=0){
                    if(yaxis> max_value) {
                        max_value = yaxis;
                    }
                }
            } else if (Data_Spinner_value_2.equals("T3")) {
                float yaxis = Float.parseFloat(valueOf(data.getString(4)));//Gets the t3 value and sets it on the y axis
                x2 = x2 + 1;//Move to next Id
                board_yAXES_2.add(new Entry(yaxis, i));
                board_values.add(new Entry(yaxis , (int)x));
                yAxes.add(new Entry((int)yaxis,(int)x));
                if(yaxis <=0){
                    if(yaxis< min_value) {
                        min_value = yaxis;
                    }
                }
                if(yaxis >=0){
                    if(yaxis> max_value) {
                        max_value = yaxis;
                    }
                }
            } else if (Data_Spinner_value_2.equals("T4")) {
                float yaxis = Float.parseFloat(valueOf(data.getString(5)));//Gets the t4 value and sets it on the y axis
                x2 = x2 + 1;//Move to next Id
                board_yAXES_2.add(new Entry(yaxis, i));
                board_values.add(new Entry(yaxis , (int)x));
                yAxes.add(new Entry((int)yaxis,(int)x));
                if(yaxis <=0){
                    if(yaxis< min_value) {
                        min_value = yaxis;
                    }
                }
                if(yaxis >=0){
                    if(yaxis> max_value) {
                        max_value = yaxis;
                    }
                }
            } else if (Data_Spinner_value_2.equals("Pd")) {
                float yaxis = Float.parseFloat(valueOf(data.getString(6)));//Gets the pd value and sets it on the y axis
                x2 = x2+ 1;//Move to next Id
                board_yAXES_2.add(new Entry(yaxis, i));
                board_values.add(new Entry(yaxis , (int)x));
                yAxes.add(new Entry((int)yaxis,(int)x));
                if(yaxis <=0){
                    if(yaxis< min_value) {
                        min_value = yaxis;
                    }
                }
                if(yaxis >=0){
                    if(yaxis> max_value) {
                        max_value = yaxis;
                    }
                }
            }
            if (delta) {
                if (((Data_Spinner_value.equals("T1")) && (Data_Spinner_value_2.equals("T2"))) || ((Data_Spinner_value.equals("2")) && (Data_Spinner_value_2.equals("T1")))) {
                    td1 = Float.parseFloat(valueOf(data.getString(1)));//Gets the counter value and sets it on the y axis
                    td2 = Float.parseFloat(valueOf(data.getString(2)));//Gets the counter value and sets it on the y axis;}
                } else if (((Data_Spinner_value.equals("T1")) && (Data_Spinner_value_2.equals("T3"))) || ((Data_Spinner_value.equals("T3")) && (Data_Spinner_value_2.equals("T1")))) {
                    td1 = Float.parseFloat(valueOf(data.getString(1)));//Gets the counter value and sets it on the y axis
                    td2 = Float.parseFloat(valueOf(data.getString(4)));//Gets the counter value and sets it on the y axis;}
                } else if (((Data_Spinner_value.equals("T1")) && (Data_Spinner_value_2.equals("T4"))) || ((Data_Spinner_value.equals("T4")) && (Data_Spinner_value_2.equals("T4")))) {
                    td1 = Float.parseFloat(valueOf(data.getString(1)));//Gets the counter value and sets it on the y axis
                    td2 = Float.parseFloat(valueOf(data.getString(5)));//Gets the counter value and sets it on the y axis;}
                } else if (((Data_Spinner_value.equals("T2")) && (Data_Spinner_value_2.equals("T3"))) || ((Data_Spinner_value.equals("T3")) && (Data_Spinner_value_2.equals("T2")))) {
                    td1 = Float.parseFloat(valueOf(data.getString(2)));//Gets the counter value and sets it on the y axis
                    td2 = Float.parseFloat(valueOf(data.getString(4)));//Gets the counter value and sets it on the y axis;}
                } else if (((Data_Spinner_value.equals("T3")) && (Data_Spinner_value_2.equals("T4"))) || ((Data_Spinner_value.equals("T4")) && (Data_Spinner_value_2.equals("T3")))) {
                    td1 = Float.parseFloat(valueOf(data.getString(4)));//Gets the counter value and sets it on the y axis
                    td2 = Float.parseFloat(valueOf(data.getString(5)));//Gets the counter value and sets it on the y axis;}
                }

                if (td1 - td2 >= 0) {
                    yaxis_delta = td1 - td2;
                } else {
                    yaxis_delta = td2 - td1;
                }
                x3 = x3 + 1;//Move to next Id
                board_yAXES_delta.add(new Entry(yaxis_delta, i));

                board_values_delta.add(new Entry(yaxis_delta, (int)x));
                if (yaxis_delta <= 0) {
                    if (yaxis_delta < min_value) {
                        min_value = yaxis_delta;
                    }
                }
                if (yaxis_delta >= 0) {
                    if (yaxis_delta > max_value) {
                        max_value = yaxis_delta;
                    }
                }
            }
            data.moveToNext();
        }

        main_linechart.setVisibleXRangeMinimum(min_value * 1.2f);
        main_linechart.setVisibleXRangeMaximum(max_value * 1.2f);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();


        ArrayList<String> xAxes = new ArrayList<>();
        for(int i = 0; i < 60; i++)
        {
            xAxes.add(String.valueOf(i));
        }
        String[] xaxes = new String[xAxes.size()];

        for(int i= 0;i<xAxes.size();i++)
        {
            xaxes[i] = xAxes.get(i).toString();
        }

        ArrayList<String> xAxisLabel = new ArrayList<>();

        LineDataSet lineDataSet = new LineDataSet(yAxes,"V1");
        dataSets.add(lineDataSet);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setColor(Color.BLUE);
        main_linechart.setData(new LineData(xaxes,dataSets));

        main_linechart.setTouchEnabled(true);
        main_linechart.setDragEnabled(false);
        main_linechart.invalidate();//Refresh the graph
    }
    */
    private void graph_output_multiple() {
        main_linechart.invalidate();
        main_linechart.clear();

        float x3 = 0;
        float td1 = 0;
        float td2 = 0;
        float yaxis_delta = 0;
        float max_value = 0;
        float min_value = 0;

        ArrayList<String> xAxes = new ArrayList<>();
        ArrayList<Entry> yAxes_1 = new ArrayList<>();
        ArrayList<Entry> yAxes_2 = new ArrayList<>();
        ArrayList<Entry> yAxes_d = new ArrayList<>();
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

        Cursor data = null;
        data = BLE_DB.showData_Sensor_Board();//Show the sensor board database

        if (data.getCount() <= 60) {
            data_display_max = data.getCount();
            data.moveToFirst();//Display live values
        } else if (data.getCount() > 60) {

            //Display only 100 last data values
            data.moveToLast();
            for (int i = 0; i < 60; i++) {
                data.moveToPrevious();
            }
            //data.move((data.getCount())-100);//Set the starting value to be 100th last value
            data_display_max = 60;
        }

        //Xaxis
        //Add to the array list

        //for(int i = beginning_seconds; i <= ending_seconds; i++)
        for (int i = 0; i <= data_display_max; i++) {
            xAxes.add(String.valueOf(i));
        }

        //Yaxis
        float x1 = 0;
        float x2 = 0;
        float xd = 0;
        for (int i = 0; i < data_display_max; i++) {
            //Counter
            if(!delta) {
                if (Data_Spinner_value.equals("T1")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(1)));//Gets the counter value and sets it on the y axis
                    x1 = x1 + 1;
                    yAxes_1.add(new Entry(yaxis, (int) x1));
                } else if (Data_Spinner_value.equals("T2")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(2)));//Gets the counter value and sets it on the y axis
                    x1 = x1 + 1;
                    yAxes_1.add(new Entry(yaxis, (int) x1));
                } else if (Data_Spinner_value.equals("T3")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(4)));//Gets the counter value and sets it on the y axis
                    x1 = x1 + 1;
                    yAxes_1.add(new Entry(yaxis, (int) x1));
                } else if (Data_Spinner_value.equals("T4")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(5)));//Gets the counter value and sets it on the y axis
                    x1 = x1 + 1;
                    yAxes_1.add(new Entry(yaxis, (int) x1));
                } else if (Data_Spinner_value.equals("Pd")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(6)));//Gets the counter value and sets it on the y axis
                    x1 = x1 + 1;
                    yAxes_1.add(new Entry(yaxis, (int) x1));
                }

                if (Data_Spinner_value_2.equals("T1")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(1)));//Gets the counter value and sets it on the y axis
                    x2 = x2 + 1;
                    yAxes_2.add(new Entry(yaxis, (int) x2));
                } else if (Data_Spinner_value_2.equals("T2")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(2)));//Gets the counter value and sets it on the y axis
                    x2 = x2 + 1;
                    yAxes_2.add(new Entry(yaxis, (int) x2));
                } else if (Data_Spinner_value_2.equals("T3")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(4)));//Gets the counter value and sets it on the y axis
                    x2 = x2 + 1;
                    yAxes_2.add(new Entry(yaxis, (int) x2));
                } else if (Data_Spinner_value_2.equals("T4")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(5)));//Gets the counter value and sets it on the y axis
                    x2 = x2 + 1;
                    yAxes_2.add(new Entry(yaxis, (int) x2));
                } else if (Data_Spinner_value_2.equals("Pd")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(6)));//Gets the counter value and sets it on the y axis
                    x2 = x2 + 1;
                    yAxes_2.add(new Entry(yaxis, (int) x2));
                }
            }
            else if (delta) {
                if (((Data_Spinner_value.equals("T1")) && (Data_Spinner_value_2.equals("T2"))) || ((Data_Spinner_value.equals("2")) && (Data_Spinner_value_2.equals("T1")))) {
                    td1 = Float.parseFloat(valueOf(data.getString(1)));//Gets the counter value and sets it on the y axis
                    td2 = Float.parseFloat(valueOf(data.getString(2)));//Gets the counter value and sets it on the y axis;}
                } else if (((Data_Spinner_value.equals("T1")) && (Data_Spinner_value_2.equals("T3"))) || ((Data_Spinner_value.equals("T3")) && (Data_Spinner_value_2.equals("T1")))) {
                    td1 = Float.parseFloat(valueOf(data.getString(1)));//Gets the counter value and sets it on the y axis
                    td2 = Float.parseFloat(valueOf(data.getString(4)));//Gets the counter value and sets it on the y axis;}
                } else if (((Data_Spinner_value.equals("T1")) && (Data_Spinner_value_2.equals("T4"))) || ((Data_Spinner_value.equals("T4")) && (Data_Spinner_value_2.equals("T4")))) {
                    td1 = Float.parseFloat(valueOf(data.getString(1)));//Gets the counter value and sets it on the y axis
                    td2 = Float.parseFloat(valueOf(data.getString(5)));//Gets the counter value and sets it on the y axis;}
                } else if (((Data_Spinner_value.equals("T2")) && (Data_Spinner_value_2.equals("T3"))) || ((Data_Spinner_value.equals("T3")) && (Data_Spinner_value_2.equals("T2")))) {
                    td1 = Float.parseFloat(valueOf(data.getString(2)));//Gets the counter value and sets it on the y axis
                    td2 = Float.parseFloat(valueOf(data.getString(4)));//Gets the counter value and sets it on the y axis;}
                } else if (((Data_Spinner_value.equals("T3")) && (Data_Spinner_value_2.equals("T4"))) || ((Data_Spinner_value.equals("T4")) && (Data_Spinner_value_2.equals("T3")))) {
                    td1 = Float.parseFloat(valueOf(data.getString(4)));//Gets the counter value and sets it on the y axis
                    td2 = Float.parseFloat(valueOf(data.getString(5)));//Gets the counter value and sets it on the y axis;}
                }

                if (td1 - td2 >= 0) {
                    yaxis_delta = td1 - td2;
                } else {
                    yaxis_delta = td2 - td1;
                }
                xd = xd + 1;
                yAxes_d.add(new Entry(yaxis_delta, (int) xd));
                if (yaxis_delta <= 0) {
                    if (yaxis_delta < min_value) {
                        min_value = yaxis_delta;
                    }
                }
                if (yaxis_delta >= 0) {
                    if (yaxis_delta > max_value) {
                        max_value = yaxis_delta;
                    }
                }
            }
            data.moveToNext();
        }

        String[] xaxes = new String[xAxes.size()];
        //int size = 0;
        //size = ending_seconds - beginning_seconds;
        //String[] xaxes = new String[size];
        for (int i = 0; i < xAxes.size(); i++) {
            xaxes[i] = xAxes.get(i).toString();
        }

        ArrayList<String> xAxisLabel = new ArrayList<>();

        if (!delta) {
            LineDataSet lineDataSet1 = new LineDataSet(yAxes_1, Data_Spinner_value);
            lineDataSet1.setDrawCircles(true);
            lineDataSet1.setColor(Color.RED);
            lineDataSets.add(lineDataSet1);

            LineDataSet lineDataSet2 = new LineDataSet(yAxes_2, Data_Spinner_value_2);
            lineDataSet2.setDrawCircles(true);
            lineDataSet2.setColor(Color.BLUE);
            lineDataSets.add(lineDataSet2);
        } else if (delta) {

            LineDataSet lineDataSetd = new LineDataSet(yAxes_d, "Delta");
            //lineDataSetd.setDrawValues(true);
            lineDataSetd.setDrawCircles(true);
            lineDataSetd.setColor(Color.GREEN);
            lineDataSets.add(lineDataSetd);
            main_linechart.getAxisLeft().setStartAtZero(false);
            main_linechart.getAxisRight().setStartAtZero(false);
        }

        //main_linechart.setVisibleXRangeMinimum(min_value * 1.2f);
        //main_linechart.setVisibleXRangeMaximum(max_value * 1.2f);

        main_linechart.setData(new LineData(xaxes,lineDataSets));

        XAxis xAxis = main_linechart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        main_linechart.setTouchEnabled(true);
        main_linechart.setDragEnabled(true);
        main_linechart.invalidate();//Refresh the graph
    }
}

