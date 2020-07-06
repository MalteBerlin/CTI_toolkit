package com.example.firebase;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.Spinner;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import static com.example.firebase.Loginactivity.BLE_DB;
import static com.example.firebase.R.integer.CMD_BLE_START;//10
import static com.example.firebase.R.integer.CMD_BLE_STOP;//20
import static com.example.firebase.R.integer.CMD_BLE_HELP;//30
import static com.example.firebase.R.integer.CMD_BLE_UART_LOG;//50
import static com.example.firebase.R.integer.CMD_BLE_READ_CSV;//60
import static com.example.firebase.R.integer.CMD_BLE_START_LOGGING;//70
import static com.example.firebase.R.integer.CMD_BLE_DELETE_LOG_DATA_CSV;//80
import static com.example.firebase.R.integer.CMD_BLE_DELETE_LOG_EVENT_CSV;//90
import static com.example.firebase.R.integer.CMD_BLE_GATT_LOG;//100


/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private boolean Gatt_services_found = false;
    private TextView progress_bar_output_text;
    private Spinner Gatt_Command_Spinner;
    private Button btn_download_files,btn_start_logging,btn_live,btn_command;
    private String mDeviceName,mDeviceAddress;
    private String User_data;
    private String Gatt_command_Spinner_value = "Help";
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private Calendar calendar;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private String Switch_case ="Disconnect";
    private String Time_stamp_string;
    public long total_entries_to_read = 0;
    public long total_lines_read = 0;
    public long total_lines_to_read =0;
    private ProgressDialog progressDialog;
    private Dialog start_dialog;
    private boolean Notification_status = false;
    private boolean No_file_bool  = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        Global_Settings global_settings = Global_Settings.getInstance();//Get access to the settings


        calendar = Calendar.getInstance();//Get access to the calendar
        //Get the extras passed in
        Bundle bundle = getIntent().getExtras();
        mDeviceName = bundle.getString("Device_Name");
        mDeviceAddress = bundle.getString("Device_Address");
        User_data = bundle.getString("User_data");
        Gatt_services_found =false;
        // Sets up UI references.


        //btn_view_files,btn_download_files,btn_start_logging,btn_Delete_file,btn_live,btn_command;
        btn_download_files = findViewById(R.id.btn_download_files);
        btn_start_logging = findViewById(R.id.btn_start_logging);
        btn_live = (Button) findViewById(R.id.btn_live);

        btn_command = (Button) findViewById(R.id.btn_command);
        Gatt_Command_Spinner = findViewById(R.id.spinner_gatt_command_spinner);//For the device sending data


        btn_download_files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Gatt_services_found == true){
                Download_File();}}
        });
        btn_start_logging.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v) {
            Confirm_start();
            }});
        btn_live.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                Live_data();
            }});
        btn_command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {if(Gatt_services_found = true) {
                    Process_Command();
                }}});
        //Spinner Setup
        ArrayAdapter<CharSequence> adapter_gatt_command = ArrayAdapter.createFromResource(this,R.array.Gatt_Command,R.layout.support_simple_spinner_dropdown_item);
        adapter_gatt_command.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        BLE_DB.deleteAll_Raw();//Clear raw data
        total_entries_to_read = 0;


        Gatt_Command_Spinner.setAdapter(adapter_gatt_command);
        Gatt_Command_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Gatt_command_Spinner_value = text;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Intent gattServiceIntent = new Intent(DeviceControlActivity.this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        if(global_settings.Get_Advanced_settings()!=true)//Disable firebase functionality
        {
            Gatt_Command_Spinner.setVisibility(View.GONE);
            btn_command.setVisibility(View.GONE);
        }

    }
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));//Use this!!
            }
        }
    };
    private void Turn_off_notifications(){
        if(Notification_status == true) {
            if (mGattCharacteristics != null) {
                final BluetoothGattCharacteristic characteristic =
                        mGattCharacteristics.get(2).get(1);
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService.turnoff_Notifications(
                                mNotifyCharacteristic);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.readCharacteristic(characteristic);
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {//Changed | to & run if correct characterisitic is selected
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLeService.turnoff_Notifications(characteristic);

                }
            }
            Notification_status = false;
        }
    }
    //Button functions
    private void Download_File(){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            BLE_DB.deleteAll_Log();//Clear log data
            total_entries_to_read = 0;
            read_gatt_data();
            //Reset commands
            int Command_1 = 0;
            Command_1 = getApplicationContext().getResources().getInteger(CMD_BLE_READ_CSV);//60
            BLE_DB.deleteAll_Raw();//Clear raw data
            final int Command_to_run_1 = Command_1;
            mBluetoothLeService.writeCharacteristic(getApplicationContext().getResources().getInteger(CMD_BLE_START));//Write command
            if (Command_to_run_1 != 0) {
                mBluetoothLeService.writeCharacteristic(Command_1);
            }//Run this command
            mBluetoothLeService.writeCharacteristic(getApplicationContext().getResources().getInteger(CMD_BLE_STOP));//Stop command
    }
    private void Confirm_start()
    {
        start_dialog = new Dialog(this);
        //Show the dialog
        start_dialog.show();
        //Set the content view
        start_dialog.setContentView(R.layout.dialog_confirm_start);

        Button btn_start_yes = start_dialog.findViewById(R.id.btn_start_yes);
        Button btn_start_no  = start_dialog.findViewById(R.id.btn_start_no);
        start_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btn_start_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Start_logging();
                start_dialog.dismiss();//Turn oof the yes / no dialog
            }
        });
        btn_start_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_dialog.dismiss();//Turn oof the yes / no dialog
            }
        });
    }
    private void Start_logging(){




        int year=0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute =0;
        int second = 0;
        Date date = new Date();//Get the time
        Calendar.getInstance();
        calendar.setTime(date);
        Time_stamp_string = calendar.getTime().toString();

        StringTokenizer tokens = new StringTokenizer(Time_stamp_string, " ");
        String sDay_of_week = tokens.nextToken();//Day of week
        String sMonth = tokens.nextToken();//Month
        String sDay = tokens.nextToken();//Day
        String sTime_of_day = tokens.nextToken();//Time of day
        String sGMT = tokens.nextToken();//GMT
        String sYear = tokens.nextToken();//Year

        StringTokenizer timetokens = new StringTokenizer(sTime_of_day, ":");//This substring is split with a :
        String sHour = timetokens.nextToken();//Hour
        String sMinute = timetokens.nextToken();//Minute
        String sSecond = timetokens.nextToken();//Second

        year   = Convert_Year(sYear);
        month  = Convert_Month(sMonth);
        day    = Convert_Day(sDay);
        hour   = Convert_Hour(sHour);
        minute = Convert_Minute(sMinute);
        second = Convert_Second(sSecond);

        int Command_1 = getApplicationContext().getResources().getInteger(CMD_BLE_START_LOGGING);//70;
        int Command_2 = year;
        int Command_3 = month;
        int Command_4 = day;
        int Command_5 = hour;
        int Command_6 = minute;
        int Command_7 = second;

        final int Command_to_run_1 = Command_1;
        final int Command_to_run_2 = Command_2;
        final int Command_to_run_3 = Command_3;
        final int Command_to_run_4 = Command_4;
        final int Command_to_run_5 = Command_5;
        final int Command_to_run_6 = Command_6;
        final int Command_to_run_7 = Command_7;

        mBluetoothLeService.writeCharacteristic(getApplicationContext().getResources().getInteger(CMD_BLE_START));//Write command
        //mBluetoothLeService.writeCharacteristic(Command_1);//Run this command
        if(Command_to_run_1 != 0){mBluetoothLeService.writeCharacteristic(Command_1);}//Run this command
        if(Command_to_run_2 != 0){mBluetoothLeService.writeCharacteristic(Command_2);}//Run this command
        if(Command_to_run_3 != 0){mBluetoothLeService.writeCharacteristic(Command_3);}//Run this command
        if(Command_to_run_4 != 0){mBluetoothLeService.writeCharacteristic(Command_4);}//Run this command
        if(Command_to_run_5 != 0){mBluetoothLeService.writeCharacteristic(Command_5);}//Run this command
        if(Command_to_run_6 != 0){mBluetoothLeService.writeCharacteristic(Command_6);}//Run this command
        if(Command_to_run_7 != 0){mBluetoothLeService.writeCharacteristic(Command_7);}//Run this command

        mBluetoothLeService.writeCharacteristic(getApplicationContext().getResources().getInteger(CMD_BLE_STOP));//Stop command
    }


    private void Live_data(){
        //Ensure you are disconnected
        Turn_off_notifications();
        mBluetoothLeService.disconnect();//Might only run if connected
        Switch_case = "Connect";
        Intent myIntent = new Intent(DeviceControlActivity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data", User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        DeviceControlActivity.this.startActivity(myIntent);//Run the main activity}
        finish();
    }
    private void clearUI() {
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
    protected void Process_Command(){
        if(Gatt_command_Spinner_value.equals("Get_log"))//Set notification
        {
            BLE_DB.deleteAll_Log();//Clear log data
            total_entries_to_read = 0;
            read_gatt_data();
        }

        int Command_length = 1;//Default is 1
        //Reset commands
        int Command_1 = 0;
        int Command_2 = 0;
        int Command_3 = 0;
        int Command_4 = 0;
        int Command_5 = 0;
        int Command_6 = 0;
        int Command_7 = 0;


        if(Gatt_command_Spinner_value.equals("Help")){
            Command_1 = 30;
            //value = getApplicationContext().getResources().getInteger(CMD_BLE_HELP);//30
        }else if(Gatt_command_Spinner_value.equals("UART_Debugging")){
            Command_1 = 50;
            //value = getApplicationContext().getResources().getInteger(CMD_BLE_UART_LOG);//50
        }else if(Gatt_command_Spinner_value.equals("Start_logging")){
            Command_length = 7;
            long unix_time =0;
            int year=0;
            int month = 0;
            int day = 0;
            int hour = 0;
            int minute =0;
            int second = 0;
            Date date = new Date();//Get the time
            Calendar.getInstance();
            calendar.setTime(date);
            Time_stamp_string = calendar.getTime().toString();

            StringTokenizer tokens = new StringTokenizer(Time_stamp_string, " ");
            String sDay_of_week = tokens.nextToken();//Day of week
            String sMonth = tokens.nextToken();//Month
            String sDay = tokens.nextToken();//Day
            String sTime_of_day = tokens.nextToken();//Time of day
            String sGMT = tokens.nextToken();//GMT
            String sYear = tokens.nextToken();//Year

            StringTokenizer timetokens = new StringTokenizer(sTime_of_day, ":");//This substring is split with a :
            String sHour = timetokens.nextToken();//Hour
            String sMinute = timetokens.nextToken();//Minute
            String sSecond = timetokens.nextToken();//Second
            //Need to convert all data to integers for transmission

            //Convert functions
            year   = Convert_Year(sYear);
            month  = Convert_Month(sMonth);
            day    = Convert_Day(sDay);
            hour   = Convert_Hour(sHour);
            minute = Convert_Minute(sMinute);
            second = Convert_Second(sSecond);

            Command_1 = getApplicationContext().getResources().getInteger(CMD_BLE_START_LOGGING);//70;
            Command_2 = year;
            Command_3 = month;
            Command_4 = day;
            Command_5 = hour;
            Command_6 = minute;
            Command_7 = second;

        }else if(Gatt_command_Spinner_value.equals("Get_log")){
            Command_1 = getApplicationContext().getResources().getInteger(CMD_BLE_READ_CSV);//60
            BLE_DB.deleteAll_Raw();//Clear raw data
        }else if(Gatt_command_Spinner_value.equals("Gatt_Log")){
            Command_1 = getApplicationContext().getResources().getInteger(CMD_BLE_GATT_LOG);//Gatt log command
        }else if(Gatt_command_Spinner_value.equals("Delete_event_log")){
            Command_1 = getApplicationContext().getResources().getInteger(CMD_BLE_DELETE_LOG_EVENT_CSV);//90
        }else if(Gatt_command_Spinner_value.equals("Delete_data_log")){
            Command_1 = getApplicationContext().getResources().getInteger(CMD_BLE_DELETE_LOG_DATA_CSV);//80
        }else{
            Command_1 = Integer.valueOf(0);//Write 0 data or a force error command
            //Error don't send data
        }
        final int Command_to_run_1 = Command_1;
        final int Command_to_run_2 = Command_2;
        final int Command_to_run_3 = Command_3;
        final int Command_to_run_4 = Command_4;
        final int Command_to_run_5 = Command_5;
        final int Command_to_run_6 = Command_6;
        final int Command_to_run_7 = Command_7;

        mBluetoothLeService.writeCharacteristic(getApplicationContext().getResources().getInteger(CMD_BLE_START));//Write command
        //mBluetoothLeService.writeCharacteristic(Command_1);//Run this command
        if(Command_to_run_1 != 0){mBluetoothLeService.writeCharacteristic(Command_1);}//Run this command
        if(Command_to_run_2 != 0){mBluetoothLeService.writeCharacteristic(Command_2);}//Run this command
        if(Command_to_run_3 != 0){mBluetoothLeService.writeCharacteristic(Command_3);}//Run this command
        if(Command_to_run_4 != 0){mBluetoothLeService.writeCharacteristic(Command_4);}//Run this command
        if(Command_to_run_5 != 0){mBluetoothLeService.writeCharacteristic(Command_5);}//Run this command
        if(Command_to_run_6 != 0){mBluetoothLeService.writeCharacteristic(Command_6);}//Run this command
        if(Command_to_run_7 != 0){mBluetoothLeService.writeCharacteristic(Command_7);}//Run this command

        mBluetoothLeService.writeCharacteristic(getApplicationContext().getResources().getInteger(CMD_BLE_STOP));//Stop command

    }
    //Read the data from the gatt notification
    private void read_gatt_data()//Read the notifications
    {

        mBluetoothLeService.Connection_type(1);//High power connection
        if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic =
                    mGattCharacteristics.get(2).get(1);
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService.setCharacteristicNotification(
                            mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }
                mBluetoothLeService.readCharacteristic(characteristic);
            }
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {//Changed | to & run if correct characterisitic is selected
                mNotifyCharacteristic = characteristic;
                mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                Notification_status = true;
            }
        }
    }
    private void Process_raw_log()//Take raw SQL data turn it into split variables
    {
        Cursor data;

        data = BLE_DB.showData_Raw();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            CursorWindow cw = new CursorWindow("test", 536870912);//16777216 broke
            AbstractWindowedCursor ac = (AbstractWindowedCursor) data;
            ac.setWindow(cw);

            ac.moveToFirst();
            String data_to_process;
            Date date1, date2, date3, date4, date5;
            int D1_unix_time = 0, D2_unix_time = 0, D3_unix_time = 0, D4_unix_time = 0, D5_unix_time = 0;
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("BST"));//British summer time

            if (ac.getCount() == 0) {
                Utils.toast(getApplicationContext(), "No data selected");
            }
            //for(int i = 0; i < data.getCount(); i++) {
            for (int i = 0; i < ac.getCount(); i ++) {//Store every 2 seconds
                //for(int i = 0; i <max_data; i++){
                if(i>ac.getCount()-10)//Ignore last 10 seconds as device is out 3 seconds in every 48 hours
                {
                    Utils.toast(getApplicationContext(),"ending processing");//Handle string one by one
                }
                else {
                    data_to_process = ac.getString(1);//This should be the raw data

                    StringTokenizer Data_pre_process_string = new StringTokenizer(data_to_process, "\r\n");
                    String Data_1_string = Data_pre_process_string.nextToken();//Data 1
                    String Data_2_string = Data_pre_process_string.nextToken();//Data 2
                    String Data_3_string = Data_pre_process_string.nextToken();//Data 3
                    String Data_4_string = Data_pre_process_string.nextToken();//Data 4
                    String Data_5_string = Data_pre_process_string.nextToken();//Data 5

                    StringTokenizer D1_string_split = new StringTokenizer(Data_1_string, ",");
                    String D1_Time = D1_string_split.nextToken();
                    String D1_T1 = D1_string_split.nextToken();
                    String D1_T2 = D1_string_split.nextToken();
                    String D1_T3 = D1_string_split.nextToken();
                    String D1_T4 = D1_string_split.nextToken();
                    String D1_Pd = D1_string_split.nextToken();

                    StringTokenizer D2_string_split = new StringTokenizer(Data_2_string, ",");
                    String D2_Time = D2_string_split.nextToken();
                    String D2_T1 = D2_string_split.nextToken();
                    String D2_T2 = D2_string_split.nextToken();
                    String D2_T3 = D2_string_split.nextToken();
                    String D2_T4 = D2_string_split.nextToken();
                    String D2_Pd = D2_string_split.nextToken();

                    StringTokenizer D3_string_split = new StringTokenizer(Data_3_string, ",");
                    String D3_Time = D3_string_split.nextToken();
                    String D3_T1 = D3_string_split.nextToken();
                    String D3_T2 = D3_string_split.nextToken();
                    String D3_T3 = D3_string_split.nextToken();
                    String D3_T4 = D3_string_split.nextToken();
                    String D3_Pd = D3_string_split.nextToken();

                    StringTokenizer D4_string_split = new StringTokenizer(Data_4_string, ",");
                    String D4_Time = D4_string_split.nextToken();
                    String D4_T1 = D4_string_split.nextToken();
                    String D4_T2 = D4_string_split.nextToken();
                    String D4_T3 = D4_string_split.nextToken();
                    String D4_T4 = D4_string_split.nextToken();
                    String D4_Pd = D4_string_split.nextToken();

                    StringTokenizer D5_string_split = new StringTokenizer(Data_5_string, ",");
                    String D5_Time = D5_string_split.nextToken();
                    String D5_T1 = D5_string_split.nextToken();
                    String D5_T2 = D5_string_split.nextToken();
                    String D5_T3 = D5_string_split.nextToken();
                    String D5_T4 = D5_string_split.nextToken();
                    String D5_Pd = D5_string_split.nextToken();


                    D1_unix_time = Integer.parseInt(D1_Time);
                    D2_unix_time = D1_unix_time + 1;
                    D3_unix_time = D1_unix_time + 2;
                    D4_unix_time = D1_unix_time + 3;
                    D5_unix_time = D1_unix_time + 4;

                    date1 = new java.util.Date(D1_unix_time * 1000L);
                    date2 = new java.util.Date(D2_unix_time * 1000L);
                    date3 = new java.util.Date(D3_unix_time * 1000L);
                    date4 = new java.util.Date(D4_unix_time * 1000L);
                    date5 = new java.util.Date(D5_unix_time * 1000L);
                    String D1_f_time = sdf.format(date1);
                    String D2_f_time = sdf.format(date2);
                    String D3_f_time = sdf.format(date3);
                    String D4_f_time = sdf.format(date4);
                    String D5_f_time = sdf.format(date5);

                    BLE_DB.addData_Log(D1_T1, D1_T2, D1_T3, D1_T4, D1_Pd, D1_f_time);//D1 add
                    BLE_DB.addData_Log(D2_T1, D2_T2, D2_T3, D2_T4, D2_Pd, D2_f_time);//D2 add
                    BLE_DB.addData_Log(D3_T1, D3_T2, D3_T3, D3_T4, D3_Pd, D3_f_time);//D3 add
                    BLE_DB.addData_Log(D4_T1, D4_T2, D4_T3, D4_T4, D4_Pd, D4_f_time);//D4 add
                    BLE_DB.addData_Log(D5_T1, D5_T2, D5_T3, D5_T4, D5_Pd, D5_f_time);//D5 add

                }
                ac.moveToNext();
            }
            //Processed
            ac.close();
        }
        //Data processed
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }


    private void displayData(String data) {//Keep this minimal to speed up data transferance
        if (data != null) {
            //Store new value here
            if(data.contains("End of file"))//End of file check
            {
                if(No_file_bool == false) {
                    Process_raw_log();
                    //Do nothing end of file notify user
                    Utils.toast(getApplicationContext(), "File read End of file");
                    Log.d("TAG", "End of file");

                    mBluetoothLeService.Connection_type(0);//Balanced power connection
                    progressDialog.dismiss();//Turn off the progress dialog
                    progressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);//Enable touching of screen again
                    this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);//Enable touching of screen again
                }
                else if(No_file_bool = true)
                {
                    No_file_bool = false;
                }

            }
            else if(data.contains("Start of file"))//Start code
            {
                total_lines_read =0;
                total_lines_to_read =0;
                No_file_bool = false;
                StringTokenizer Entries = new StringTokenizer(data, ",");
                String Number_of_entries = Entries.nextToken();


                total_entries_to_read = Integer.parseInt(Number_of_entries);
                progressDialog = new ProgressDialog(DeviceControlActivity.this);
                //Show the dialog
                progressDialog.show();
                //Set the content view
                progressDialog.setContentView(R.layout.progress_dialog);

                progress_bar_output_text = progressDialog.findViewById(R.id.progress_bar_output);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                float hours = total_entries_to_read /3600;
                progress_bar_output_text.setText(Float.toString(hours));
                this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            else if(data.contains("No file"))//Start code
            {
                No_file_bool = true;
                Utils.toast(getApplicationContext(),"No file");
            }
            else if(data.contains("1"))//Not 0 check
                if (data.length() > 10)//Actual data
                {
                    BLE_DB.addData_Raw(data);
                }
        }
    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }

            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
            Gatt_services_found = true;
        }
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    private int Convert_Year(String year){return Integer.valueOf(year);}
    private int Convert_Month(String month){
        int return_value =0;
        if(month.equals("Jan")){
            return_value = 1;
        }else if(month.equals("Feb")){
            return_value =2;
        }else if(month.equals("Mar")){
            return_value =3;
        }else if(month.equals("Apr")){
            return_value =4;
        }else if(month.equals("May")){
            return_value =5;
        }else if(month.equals("Jun")){
            return_value =6;
        }else if(month.equals("Jul")){
            return_value =7;
        }else if(month.equals("Aug")){
            return_value =8;
        }else if(month.equals("Sep")){
            return_value =9;
        }else if(month.equals("Oct")){
            return_value =10;
        }else if(month.equals("Nov")){
            return_value =11;
        }else if(month.equals("Dec")){
            return_value =12;
        }else{
            return_value =0;//Error
        }
        return return_value;
    }
    private int Convert_Day(String day){return Integer.valueOf(day);}
    private int Convert_Hour(String hour){return Integer.valueOf(hour);}
    private int Convert_Minute(String minute){return Integer.valueOf(minute);}
    private int Convert_Second(String second){return Integer.valueOf(second);}

}
