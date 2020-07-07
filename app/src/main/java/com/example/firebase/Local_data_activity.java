package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;

import com.github.mikephil.charting.components.XAxis;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;

import java.util.Locale;
import java.util.StringTokenizer;

import static com.example.firebase.Home_activity.SHARDED_PREFS;
import static com.example.firebase.Home_activity.Stored_bounded_device;
import static com.example.firebase.Loginactivity.BLE_DB;

import static com.example.firebase.Loginactivity.mDatareff_User;
import static java.lang.String.valueOf;


@SuppressWarnings("ALL")
public class Local_data_activity extends AppCompatActivity {
    private final static String TAG = Local_data_activity.class.getSimpleName();//Debugging tag
    private String Filter_stage = "Start";
    private Interface_adapter mInterface_adapter;//The class that displays the information to the interface
    private Button btn_See_graph,btn_Clear_Graph,btn_filter,btn_mass_store,btn_home, btn_Delete_Local,btn_data_science;
    private Spinner Spinner_filter_year,Spinner_filter_month,Spinner_filter_day,Spinner_filter_hour,Spinner_filter_minute,Spinner_data_type, Spinner_data_1, Spinner_data_2;
    private LineChart local_linechart;
    private String   Data_Spinner_value_1 = "Counter",Data_Spinner_value_2 = "Counter", Spinner_device_value= "Board",Board = "Board",Phone = "Phone",Sensor_Board = "Sensor_Board",User_data,Data_type_string = "Live";//Strings
    private String Year_Spinner_value = "2019", Month_Spinner_value = "Nov", Day_Spinner_value = "22", Hour_Spinner_value = "30",Minute_Spinner_value = "30";
    private Global_Settings global_settings;
    private ProgressDialog progressDialog_filtered;
    private Calendar Start_filter_String;
    private Calendar Stop_filter_String;
    private Calendar database_cal;
    private Member member;//Used for sending data to the database
    public int Userdata_number_of_data = 0;
    private String Start_date = "";
    private int starting_seconds =0;
    private int begining_seconds = 0;
    private int ending_seconds = 0;
    private long data_stored = 0;
    private String  sDay_of_week = "", sMonth = "", sDay = "", sTime_of_day = "", sGMT = "", sYear = "";//Spliting the date up
    private String sHour, sMinute, sSecond;//Time data

    private int Day_1 = 0, Day_2 = 0, Day_3 =0;
    private CardView btn_day_one, btn_day_two, btn_day_three;

    private int Day_to_view = 1;//The first day by default
    private boolean D1_visible = false;
    private boolean D2_visible = false;
    private boolean D3_visible = false;

    private boolean Filter_ran = false;
    private boolean Graph_process = false;// filter
    //Data science tests
    private Dialog dialog_ds_select_test;
    private Dialog dialog_ds_inlet_select;
    private Dialog dialog_ds_outlet_select;

    private String ds_selected_inlet = "";//The selected inlet
    private String ds_selected_outlet ="";//The selected outlet

    private boolean inlet_outlet_test_selected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_data);
        btn_See_graph = (Button)findViewById(R.id.btn_See_graph);
        btn_Clear_Graph = (Button)findViewById(R.id.btn_Clear_Graph);
        btn_Delete_Local = (Button)findViewById(R.id.btn_Delete_Local);
        btn_filter = (Button)findViewById(R.id.btn_filter);
        btn_mass_store = (Button)findViewById(R.id.btn_mass_store);
        btn_home = (Button)findViewById(R.id.btn_home);
        btn_data_science = (Button)findViewById(R.id.btn_data_science);
        //Days


        btn_day_one = findViewById(R.id.btn_day_one);
        btn_day_two = findViewById(R.id.btn_day_two);
        btn_day_three = findViewById(R.id.btn_day_three);

        btn_day_one.setVisibility(View.INVISIBLE);
        btn_day_two.setVisibility(View.INVISIBLE);
        btn_day_three.setVisibility(View.INVISIBLE);

        local_linechart = (LineChart)findViewById(R.id.local_data_line_chart);//Linked from the local data activity

        //Spinners
        Spinner_filter_year = findViewById(R.id.spinner_filter_year);
        Spinner_filter_month = findViewById(R.id.spinner_filter_month);
        Spinner_filter_day = findViewById(R.id.spinner_filter_day);
        Spinner_filter_hour = findViewById(R.id.spinner_filter_hour);
        Spinner_filter_minute = findViewById(R.id.spinner_filter_minute);
        Spinner_data_type = findViewById(R.id.spinner_data_type);
        global_settings = Global_Settings.getInstance();//Get access to the settings
        //btn_view_data = (Button)findViewById(R.id.btn_view_data);//Used to view data to a dialog box
        Spinner_data_1 = findViewById(R.id.spinner_select_data_1);//For the device sending data
        Spinner_data_2 = findViewById(R.id.spinner_select_data_2);//For the device sending data
        //Setup the adapters and link them to the layout and the layout list
        ArrayAdapter<CharSequence> adapter_data_1 = ArrayAdapter.createFromResource(this,R.array.Data_selection,R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter_data_2 = ArrayAdapter.createFromResource(this,R.array.Data_selection,R.layout.support_simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapter_filter_year = ArrayAdapter.createFromResource(this,R.array.Year_Selection,R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter_filter_month = ArrayAdapter.createFromResource(this,R.array.Month_Selection,R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter_data_filter_day = ArrayAdapter.createFromResource(this,R.array.Day_Selection,R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter_mode_filter_hour = ArrayAdapter.createFromResource(this,R.array.Hour_Selection,R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter_mode_filter_minute = ArrayAdapter.createFromResource(this,R.array.Minute_Selection,R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter_mode_device_type = ArrayAdapter.createFromResource(this,R.array.Data_type,R.layout.support_simple_spinner_dropdown_item);

        adapter_data_1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_data_2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_filter_year.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_filter_month.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_data_filter_day.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_mode_filter_hour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_mode_filter_minute.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_mode_device_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner_data_1.setAdapter(adapter_data_1);
        Spinner_data_2.setAdapter(adapter_data_2);
        Spinner_filter_year.setAdapter(adapter_filter_year);
        Spinner_filter_month.setAdapter(adapter_filter_month);
        Spinner_filter_day.setAdapter(adapter_data_filter_day);
        Spinner_filter_hour.setAdapter(adapter_mode_filter_hour);
        Spinner_filter_minute.setAdapter(adapter_mode_filter_minute);
        Spinner_data_type.setAdapter(adapter_mode_device_type);

        Spinner_data_1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//Used for seeing the data of the spinner so the correct data  can be displayed to the graph
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Data_Spinner_value_1 = text;
                Log.d(TAG, "Data spinner is:" +  Data_Spinner_value_1);
                New_Graph();//Graph the data
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });
        Spinner_data_2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//Used for seeing the data of the spinner so the correct data  can be displayed to the graph
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Data_Spinner_value_2 = text;
                Log.d(TAG, "Data spinner is:" +  Data_Spinner_value_2);
                New_Graph();//Graph the data
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });
        Spinner_filter_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//Get the current mode of the device to be used for other functions
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Year_Spinner_value = text;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}//Not used
        });
        Spinner_filter_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//Used for seeing the data of the spinner so the correct data  can be displayed to the graph
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Month_Spinner_value = text;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });
        Spinner_filter_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//Get the current mode of the device to be used for other functions
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Day_Spinner_value = text;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}//Not used
        });
        Spinner_filter_hour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//Used for seeing the data of the spinner so the correct data  can be displayed to the graph
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Hour_Spinner_value = text;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });
        Spinner_filter_minute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//Get the current mode of the device to be used for other functions
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Minute_Spinner_value = text;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}//Not used
        });
        Spinner_data_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//Get the current mode of the device to be used for other functions
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Data_type_string = text;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}//Not used
        });
        //Check if the bluetooth manager has been created

        //Bundle is used to identify the user's email address
        Bundle bundle = getIntent().getExtras();
        User_data = bundle.getString("User_data");
        //The onclick listeners and actions found

        Start_filter_String = Calendar.getInstance();
        Stop_filter_String = Calendar.getInstance();
        database_cal = Calendar.getInstance();

        member = new Member();//Used to send data to firebase server

        mDatareff_User = FirebaseDatabase.getInstance().getReference().child(global_settings.Get_Bound_device());//User Data
        setTitle("Local Data");//Change the title to Scanner as the device is no longer being read
        mDatareff_User.addValueEventListener(new ValueEventListener(){//Count the ID values for the phone database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Userdata_number_of_data =(int)(dataSnapshot.getChildrenCount());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){}
        });

        btn_filter.setText("Filter Start Date");
        btn_See_graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                New_Graph();
            }
        });
        btn_Clear_Graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLE_DB.deleteAll_Filtered();
                local_linechart.invalidate();
                local_linechart.clear();
            }
        });
        btn_Delete_Local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Delete_local();
            }
        });
        btn_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Filter();
            }
        });
        btn_mass_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mass_store_to_firebase();
            }
        });
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Home();
            }
        });

        btn_day_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Day_to_view = 1;
                New_Graph();
            }
        });
        btn_day_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Day_to_view = 2;
                New_Graph();
            }
        });
        btn_day_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Day_to_view = 3;
                New_Graph();
            }
        });
        btn_data_science.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Process_data_science();
            }
        });
        if(User_data.equals("admin"))//Disable firebase functionality
        {
            Utils.toast(getApplicationContext(),"Testing invisible");
            btn_mass_store.setVisibility(View.GONE);
        }
    }


    private int abs(int value)
    {
        if(value <0)
        {
            return -value;
        }
        else
        {
            return value;
        }
    }
    private void Delete_local(){
        BLE_DB.deleteAll_Filtered();//Delete filtered data
        BLE_DB.deleteAll_Filtered_Day_1();
        BLE_DB.deleteAll_Filtered_Day_2();
        BLE_DB.deleteAll_Filtered_Day_3();
        BLE_DB.deleteAll_Downloaded();
        Log.d(TAG, "Deleted local");
        Utils.toast(getApplicationContext(),"Deleted Local");
    }

    private void Filter() {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String parse_string_start;
        String parse_string_stop;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

        switch (Filter_stage) {
            case "Start":
                BLE_DB.deleteAll_Filtered();
                try {
                    parse_string_start = "Mon"+ " "
                        +Month_Spinner_value+" "
                        +Day_Spinner_value  +" "
                        +Hour_Spinner_value +":"
                        +Minute_Spinner_value+":"
                        +"00"+" "+"GMT"+" "
                        +Year_Spinner_value;
                    Start_filter_String.setTime(sdf.parse(parse_string_start));//From the text views
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Filter_stage = "Stop";
                btn_filter.setText("Filter Stop Date");
                break;
            case "Stop":
                try {
                    parse_string_stop = "Mon"+ " "
                            +Month_Spinner_value+" "
                            +Day_Spinner_value  +" "
                            +Hour_Spinner_value +":"
                            +Minute_Spinner_value+":"
                            +"00"+" "+"GMT"+" "
                            +Year_Spinner_value;
                    Stop_filter_String.setTime(sdf.parse(parse_string_stop));//From the text views
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Show_Dialog();
                btn_filter.setText("Filter Start Date");
                Filter_stage = "Start";
                new Handler().postDelayed(new Runnable() {//Using this to allow the code to break out and show the dialog box then run the filter
                    @Override
                    public void run() {
                        Run_Filter();
                    }
                },500);
                break;
        }
    }
    private void Show_Dialog()
    {
        //Filter from results
        //Show the dialog
        progressDialog_filtered = new ProgressDialog(Local_data_activity.this);
        progressDialog_filtered.show();
        //Set the content view

        progressDialog_filtered.setContentView(R.layout.processing_filtered_data_dialog);

        progressDialog_filtered.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressDialog_filtered.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    private void Run_Filter()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        D1_visible = false;
        D2_visible = false;
        D3_visible = false;

        BLE_DB.deleteAll_Filtered_Day_1();
        BLE_DB.deleteAll_Filtered_Day_2();
        BLE_DB.deleteAll_Filtered_Day_3();
        BLE_DB.deleteAll_Filtered();
        BLE_DB.deleteAll_Filtered_Log();
        BLE_DB.deleteAll_Filtered_Live();

        btn_day_one.setVisibility(View.INVISIBLE);
        btn_day_two.setVisibility(View.INVISIBLE);
        btn_day_three.setVisibility(View.INVISIBLE);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Cursor data;
        boolean Start_time_acquired = false;
        boolean Date_set = false;
        String T1_Data_string = "";
        String T2_Data_string = "";
        String T3_Data_string = "";
        String T4_Data_string = "";
        String Pdiff_Data_string = "";
        String time = "";
        String Second_part = "";
        String Third_part = "";
        String iDay = "";
        data = BLE_DB.showData_Sensor_Board();//Sensor board filter
        if(Data_type_string.equals("Live")) {
            data = BLE_DB.showData_Sensor_Board();
        }
        else if(Data_type_string.equals("Log")) {
            data = BLE_DB.showData_Log();
        }
        else if(Data_type_string.equals("Downloaded")) {
            data = BLE_DB.showData_Downloaded();
        }

        if(data.getCount() != 0) {
            //Cursor window fix and get data
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                CursorWindow cw = new CursorWindow("test", 67108864);//16777216 broke,536870912
                AbstractWindowedCursor ac = (AbstractWindowedCursor) data;
                ac.setWindow(cw);
                ac.moveToFirst();//0

                if (ac.getCount() == 0)//No data selected
                {
                    Utils.toast(getApplicationContext(), "No data selected");
                }
                for (int i = 0; i < ac.getCount(); i++) {

                    try {
                        database_cal.setTime(sdf.parse(ac.getString(7)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (Start_filter_String.compareTo(database_cal) == 1) {
                        Log.d(TAG, "Filter: Data is not after start time");
                    }
                    if (Stop_filter_String.compareTo(database_cal) == 1) {
                        Log.d(TAG, "Filter: Data is not before stop time");
                    }
                    if (Start_filter_String.compareTo(database_cal) == -1) {
                        Log.d(TAG, "Filter: Data is after start time");
                    }
                    if (Stop_filter_String.compareTo(database_cal) == -1) {
                        Log.d(TAG, "Filter: Data is before stop time");
                    }
                    if ((Start_filter_String.compareTo(database_cal) == -1) & (Stop_filter_String.compareTo(database_cal) == 1)) {//Check that the start and stop time criteria are met
                        //Add device to the list

                        if (Data_type_string.equals("Live")) {//Live data
                            T1_Data_string = ac.getString(1);
                            T2_Data_string = ac.getString(2);
                            T3_Data_string = ac.getString(4);
                            T4_Data_string = ac.getString(5);
                            Pdiff_Data_string = ac.getString(6);
                            time = ac.getString(7);
                            if (Start_time_acquired == false) {
                                Start_date = time;
                                Start_time_acquired = true;
                                StringTokenizer tokens = new StringTokenizer(Start_date, " ");
                                sDay_of_week = tokens.nextToken();//Day of week
                                sMonth = tokens.nextToken();//Month
                                sDay = tokens.nextToken();//Day
                                sTime_of_day = tokens.nextToken();//Time of day
                                sGMT = tokens.nextToken();//GMT
                                sYear = tokens.nextToken();//Year
                                StringTokenizer timetokens = new StringTokenizer(sTime_of_day, ":");//This substring is split with a :
                                sHour = timetokens.nextToken();//Hour
                                sMinute = timetokens.nextToken();//Minute
                                sSecond = timetokens.nextToken();//Second
                                starting_seconds = (Integer.parseInt(sHour) * 3600) + (Integer.parseInt(sMinute) * 60) + (Integer.parseInt(sSecond));

                                if (Date_set == false) {
                                    Day_1 = Integer.parseInt(sDay);
                                    if (Day_1 == 28 && Integer.parseInt(sMonth) == 2)//28 check Feb check
                                    {
                                        if (Integer.parseInt(sYear) % 100 == 0 && (Integer.parseInt(sYear) % 400 == 0))//Leap year
                                        {
                                            Day_2 = Day_1 + 1;
                                            Day_3 = Day_2 + 1;
                                            Date_set = true;
                                        }
                                        //Not a leap year
                                        Day_2 = 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    } else if (Day_1 == 29) {
                                        Day_2 = Day_1 + 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    } else if (Day_1 == 30)//Check 31 days
                                    {
                                        if ((Integer.parseInt(sMonth) == 1) || (Integer.parseInt(sMonth) == 3) || (Integer.parseInt(sMonth) == 5)
                                                || (Integer.parseInt(sMonth) == 7) || (Integer.parseInt(sMonth) == 8) || (Integer.parseInt(sMonth) == 10)
                                                || (Integer.parseInt(sMonth) == 12)) {
                                            Day_2 = 1;
                                            Day_3 = Day_2 + 1;
                                            Date_set = true;
                                        } else//No a 31 month
                                        {
                                            Day_2 = Day_1 + 1;
                                            Day_3 = Day_2 + 1;
                                            Date_set = true;
                                        }
                                    } else {
                                        Day_2 = Day_1 + 1;
                                        Day_3 = Day_2 + 1;
                                    }
                                }//Set date 1 ,2 and 3]

                                StringTokenizer sting_token = new StringTokenizer(time, " ");
                                String lDay_of_week = sting_token.nextToken();//Day of week
                                String lMonth = sting_token.nextToken();//Month
                                String lDay = sting_token.nextToken();//Day
                                String lTime_of_day = sting_token.nextToken();//Time of day
                                String lGMT = sting_token.nextToken();//GMT
                                String lYear = sting_token.nextToken();//Year
                                StringTokenizer string_time_token = new StringTokenizer(lTime_of_day, ":");//This substring is split with a :
                                String lHour = string_time_token.nextToken();//Hour
                                String lMinute = string_time_token.nextToken();//Minute
                                String lSecond = string_time_token.nextToken();//Second
                                iDay = lDay;

                            }
                        } else if (Data_type_string.equals("Log") || ((Data_type_string.equals("Downloaded")))) { //Log data
                            T1_Data_string = ac.getString(1);
                            T2_Data_string = ac.getString(2);
                            T3_Data_string = ac.getString(4);
                            T4_Data_string = ac.getString(5);
                            Pdiff_Data_string = ac.getString(6);
                            time = ac.getString(7);
                            //time = "2020-05-20 12:51:19 GMT
                            if (Start_time_acquired == false) {
                                Start_date = time;
                                Start_time_acquired = true;
                                StringTokenizer tokens = new StringTokenizer(Start_date, "-");
                                sYear = tokens.nextToken();//Year
                                sMonth = tokens.nextToken();//Month
                                sDay = tokens.nextToken();//Day
                                StringTokenizer tokens_next = new StringTokenizer(sDay, " ");
                                sDay = tokens_next.nextToken();//Day
                                Second_part = tokens_next.nextToken();
                                Third_part = tokens_next.nextToken();
                                StringTokenizer tokens_time = new StringTokenizer(Second_part, ":");
                                sHour = tokens_time.nextToken();//Hour
                                sMinute = tokens_time.nextToken();//Minute
                                sSecond = tokens_time.nextToken();//Second
                                StringTokenizer tokens_zone = new StringTokenizer(Third_part, " ");
                                sGMT = tokens_zone.nextToken();//GMT
                                starting_seconds = (Integer.parseInt(sHour) * 3600) + (Integer.parseInt(sMinute) * 60) + (Integer.parseInt(sSecond));

                                if (Date_set == false) {
                                    Day_1 = Integer.parseInt(sDay);
                                    if (Day_1 == 28 && Integer.parseInt(sMonth) == 2)//28 check Feb check
                                    {
                                        if (Integer.parseInt(sYear) % 100 == 0 && (Integer.parseInt(sYear) % 400 == 0))//Leap year
                                        {
                                            Day_2 = Day_1 + 1;
                                            Day_3 = Day_2 + 1;
                                            Date_set = true;
                                        }
                                        //Not a leap year
                                        Day_2 = 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    } else if (Day_1 == 29) {
                                        Day_2 = Day_1 + 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    } else if (Day_1 == 30)//Check 31 days
                                    {
                                        if ((Integer.parseInt(sMonth) == 1) || (Integer.parseInt(sMonth) == 3) || (Integer.parseInt(sMonth) == 5)
                                                || (Integer.parseInt(sMonth) == 7) || (Integer.parseInt(sMonth) == 8) || (Integer.parseInt(sMonth) == 10)
                                                || (Integer.parseInt(sMonth) == 12)) {
                                            Day_2 = 1;
                                            Day_3 = Day_2 + 1;
                                            Date_set = true;
                                        } else//No a 31 month
                                        {
                                            Day_2 = Day_1 + 1;
                                            Day_3 = Day_2 + 1;
                                            Date_set = true;
                                        }
                                    } else {
                                        Day_2 = Day_1 + 1;
                                        Day_3 = Day_2 + 1;
                                    }
                                }//Set date 1 ,2 and 3

                            }//Set the start date

                            StringTokenizer tokens = new StringTokenizer(time, "-");
                            String iYear = tokens.nextToken();//Year
                            String iMonth = tokens.nextToken();//Month
                            iDay = tokens.nextToken();//Day
                            StringTokenizer tokens_next = new StringTokenizer(iDay, " ");
                            iDay = tokens_next.nextToken();//Day
                            /*
                            String iSecond_part = tokens_next.nextToken();
                            String iThird_part = tokens_next.nextToken();

                            StringTokenizer tokens_time = new StringTokenizer(iSecond_part, ":");
                            String isHour = tokens_time.nextToken();//Hour
                            String isMinute = tokens_time.nextToken();//Minute
                            String isSecond = tokens_time.nextToken();//Second
                            StringTokenizer tokens_zone = new StringTokenizer(iThird_part, " ");
                            String iGMT = tokens_zone.nextToken();//GMT

                             */
                        }
    /*
                        else if ((Data_type_string.equals("Downloaded"))) {
                            T1_Data_string = ac.getString(1);
                            T2_Data_string = ac.getString(2);
                            T3_Data_string = ac.getString(4);
                            T4_Data_string = ac.getString(5);
                            Pdiff_Data_string = ac.getString(6);
                            time = ac.getString(7);
                            //time = "2020-05-20 12:51:19 GMT
                            //time = "Thu May 21 18:06:46 GMT +01:00 2020
                            if (Start_time_acquired == false) {
                                Start_date = time;
                                Start_time_acquired = true;
                                StringTokenizer tokens = new StringTokenizer(Start_date, "-");
                                String st_year = tokens.nextToken();//Year
                                String st_month = tokens.nextToken();//Month
                                String st_day = tokens.nextToken();//Day
                                StringTokenizer tokens_next = new StringTokenizer(st_day, " ");
                                st_day = tokens_next.nextToken();//Day
                                Second_part = tokens_next.nextToken();
                                Third_part = tokens_next.nextToken();
                                StringTokenizer tokens_time = new StringTokenizer(Second_part, ":");
                                String st_Hour = tokens_time.nextToken();//Hour
                                String st_Minute = tokens_time.nextToken();//Minute
                                String st_Second = tokens_time.nextToken();//Second
                                StringTokenizer tokens_zone = new StringTokenizer(Third_part, " ");
                                String st_time_zone = tokens_zone.nextToken();//GMT
                                starting_seconds = (Integer.parseInt(st_Hour) * 3600) + (Integer.parseInt(st_Minute) * 60) + (Integer.parseInt(st_Second));

                                if (Date_set == false) {
                                    Day_1 = Integer.parseInt(st_day);
                                    if (Day_1 == 28 && Integer.parseInt(st_month) == 2)//28 check Feb check
                                    {
                                        if (Integer.parseInt(st_year) % 100 == 0 && (Integer.parseInt(st_year) % 400 == 0))//Leap year
                                        {
                                            Day_2 = Day_1 + 1;
                                            Day_3 = Day_2 + 1;
                                            Date_set = true;
                                        }
                                        //Not a leap year
                                        Day_2 = 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    } else if (Day_1 == 29) {
                                        Day_2 = Day_1 + 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    } else if (Day_1 == 30)//Check 31 days
                                    {
                                        if ((Integer.parseInt(st_month) == 1) || (Integer.parseInt(st_month) == 3) || (Integer.parseInt(st_month) == 5)
                                                || (Integer.parseInt(st_month) == 7) || (Integer.parseInt(st_month) == 8) || (Integer.parseInt(st_month) == 10)
                                                || (Integer.parseInt(st_month) == 12)) {
                                            Day_2 = 1;
                                            Day_3 = Day_2 + 1;
                                            Date_set = true;
                                        } else//No a 31 month
                                        {
                                            Day_2 = Day_1 + 1;
                                            Day_3 = Day_2 + 1;
                                            Date_set = true;
                                        }
                                    }
                                    else
                                    {
                                        Day_2 = Day_1 + 1;
                                        Day_3 = Day_2 + 1;
                                    }
                                }//Set date 1 ,2 and 3

                            }//Set the start date

                            if(time.contains("-"))//time = "2020-05-20 12:51:19 GMT
                            {
                                StringTokenizer tokens = new StringTokenizer(time, "-");
                                String d_year = tokens.nextToken();//Year
                                String d_month = tokens.nextToken();//Month
                                String d_day = tokens.nextToken();//Day
                                StringTokenizer tokens_next = new StringTokenizer(d_day, " ");
                                d_day = tokens_next.nextToken();//Day
                                Second_part = tokens_next.nextToken();
                                Third_part = tokens_next.nextToken();
                                StringTokenizer tokens_time = new StringTokenizer(Second_part, ":");
                                String d_hour = tokens_time.nextToken();//Hour
                                String d_minute = tokens_time.nextToken();//Minute
                                String d_second = tokens_time.nextToken();//Second
                                StringTokenizer tokens_zone = new StringTokenizer(Third_part, " ");
                                String d_time_zone = tokens_zone.nextToken();//GMT

                                iDay = d_day;
                            }
                            else {//time = "Thu May 21 18:06:46 GMT +01:00 2020
                                StringTokenizer tokens_next = new StringTokenizer(time, " ");
                                String d_day_of_week = tokens_next.nextToken();//Thus
                                String d_month = tokens_next.nextToken();//May
                                String d_date = tokens_next.nextToken();//21 (day)
                                String d_time = tokens_next.nextToken();//18:06:46
                                String d_time_zone_offset = tokens_next.nextToken();//GMT+01:00
                                String d_year = tokens_next.nextToken();//2020

                                StringTokenizer tokens_time = new StringTokenizer(d_time, ":");
                                String dHour = tokens_time.nextToken();//Hour
                                String dMinute = tokens_time.nextToken();//Minute
                                String dSecond = tokens_time.nextToken();//Second

                                iDay = d_date;
                            }

                            if(Data_type_string.equals("Live"))
                            {
                                boolean result = BLE_DB.addData_Filtered_Live(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                                if (result == true) {
                                    Log.d(TAG, "data_changes: Added data correctly");
                                }
                                if (result == false) {
                                    Log.d(TAG, "data_changes: did not add data correctly");
                                }//false
                            }
                            else if(Data_type_string.equals("Log"))
                            {
                                boolean result = BLE_DB.addData_Filtered_Log(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                                if (result == true) {
                                    Log.d(TAG, "data_changes: Added data correctly");
                                }
                                if (result == false) {
                                    Log.d(TAG, "data_changes: did not add data correctly");
                                }//false
                            }
                            else if(Data_type_string.equals("Downloaded"))
                            {
                                boolean result = BLE_DB.addData_Filtered_Downloaded(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                                if (result == true) {
                                    Log.d(TAG, "data_changes: Added data correctly");
                                }
                                if (result == false) {
                                    Log.d(TAG, "data_changes: did not add data correctly");
                                }//false
                            }


                            if (Integer.parseInt(iDay) == Day_1)
                            {
                                boolean result_d1 = BLE_DB.addData_Filtered_Day_1(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                                if (result_d1 == true) {
                                    Log.d(TAG, "data_changes: Added data correctly");
                                }
                                if (result_d1 == false) {
                                    Log.d(TAG, "data_changes: did not add data correctly");
                                }//false
                                if(D1_visible == false) {
                                    btn_day_one.setVisibility(View.VISIBLE);
                                    D1_visible = true;
                                }

                            }
                            else if (Integer.parseInt(iDay) == Day_2)
                            {
                                boolean result_d2 = BLE_DB.addData_Filtered_Day_2(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                                if (result_d2 == true) {
                                    Log.d(TAG, "data_changes: Added data correctly");
                                }
                                if (result_d2 == false) {
                                    Log.d(TAG, "data_changes: did not add data correctly");
                                }//false
                                if(D2_visible == false) {
                                    btn_day_two.setVisibility(View.VISIBLE);
                                    D2_visible = true;
                                }
                            }
                            else if (Integer.parseInt(iDay) == Day_3)
                            {
                                boolean result_d3 = BLE_DB.addData_Filtered_Day_3(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                                if (result_d3 == true) {
                                    Log.d(TAG, "data_changes: Added data correctly");
                                }
                                if (result_d3 == false) {
                                    Log.d(TAG, "data_changes: did not add data correctly");
                                }//false
                                if(D3_visible == false) {
                                    btn_day_three.setVisibility(View.VISIBLE);
                                    D3_visible = true;
                                }
                            }
                            ac.moveToNext();
                        }
                        */
                        if (Data_type_string.equals("Live")) {
                            boolean result = BLE_DB.addData_Filtered_Live(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                            if (result == true) {
                                Log.d(TAG, "data_changes: Added data correctly");
                            }
                            if (result == false) {
                                Log.d(TAG, "data_changes: did not add data correctly");
                            }//false
                        } else if (Data_type_string.equals("Log")) {
                            boolean result = BLE_DB.addData_Filtered_Log(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                            if (result == true) {
                                Log.d(TAG, "data_changes: Added data correctly");
                            }
                            if (result == false) {
                                Log.d(TAG, "data_changes: did not add data correctly");
                            }//false
                        } else if (Data_type_string.equals("Downloaded")) {
                            boolean result = BLE_DB.addData_Filtered_Downloaded(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                            if (result == true) {
                                Log.d(TAG, "data_changes: Added data correctly");
                            }
                            if (result == false) {
                                Log.d(TAG, "data_changes: did not add data correctly");
                            }//false
                        }


                        if (Integer.parseInt(iDay) == Day_1) {
                            boolean result_d1 = BLE_DB.addData_Filtered_Day_1(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                            if (result_d1 == true) {
                                Log.d(TAG, "data_changes: Added data correctly");
                            }
                            if (result_d1 == false) {
                                Log.d(TAG, "data_changes: did not add data correctly");
                            }//false
                            if (D1_visible == false) {
                                btn_day_one.setVisibility(View.VISIBLE);
                                D1_visible = true;
                            }

                        } else if (Integer.parseInt(iDay) == Day_2) {
                            boolean result_d2 = BLE_DB.addData_Filtered_Day_2(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                            if (result_d2 == true) {
                                Log.d(TAG, "data_changes: Added data correctly");
                            }
                            if (result_d2 == false) {
                                Log.d(TAG, "data_changes: did not add data correctly");
                            }//false
                            if (D2_visible == false) {
                                btn_day_two.setVisibility(View.VISIBLE);
                                D2_visible = true;
                            }
                        } else if (Integer.parseInt(iDay) == Day_3) {
                            boolean result_d3 = BLE_DB.addData_Filtered_Day_3(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                            if (result_d3 == true) {
                                Log.d(TAG, "data_changes: Added data correctly");
                            }
                            if (result_d3 == false) {
                                Log.d(TAG, "data_changes: did not add data correctly");
                            }//false
                            if (D3_visible == false) {
                                btn_day_three.setVisibility(View.VISIBLE);
                                D3_visible = true;
                            }
                        }
                        ac.moveToNext();
                    }
                }
            }
        }
        if(data.getCount() == 0)
        {
            Utils.toast(getApplicationContext(),"No data");
        }
        //Done
        progressDialog_filtered.dismiss();//Turn off the progress dialog
        progressDialog_filtered.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);//Enable touching of screen again
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);//Enable touching of screen again
        //Graph();//See the data
        Filter_ran = true;
        Graph_process = false;
        New_Graph();//See if it will change the x axis format
    }
    private void Mass_store_to_firebase() {
        Cursor data;
        data = BLE_DB.showData_Filtered_Log();
        data.moveToFirst();
        data_stored = 0;
        if (data.getCount() == 0) {
            Utils.toast(getApplicationContext(), "No data selected");
        }
        if (data.getCount() != 0) {
                //Cursor window fix and get data
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                CursorWindow cw = new CursorWindow("mass_store", 536870912);//16777216 broke
                AbstractWindowedCursor ac = (AbstractWindowedCursor) data;
                ac.setWindow(cw);
                ac.moveToFirst();//0

                if (ac.getCount() == 0)//No data selected
                {
                    Utils.toast(getApplicationContext(), "No data selected");
                }
                if(ac.getCount()< 100)//One day of data
                {
                    for (int i = 0; i < ac.getCount(); i++) {//Write once per second
                        member.setT1_Data_string(ac.getString(1));//T1
                        member.setT2_Data_string(ac.getString(2));//T2
                        member.setT3_Data_string(ac.getString(4));//T3
                        member.setT4_Data_string(ac.getString(5));//T4
                        member.setPdiff_Data_string(ac.getString(6));//P1 data
                        member.setTime_string(ac.getString(7));

                        Userdata_number_of_data = Userdata_number_of_data + 1;
                        mDatareff_User.child(valueOf(Userdata_number_of_data)).setValue(member);//Increment max id count

                        //mDatareff_User.child(valueOf(i)).setValue(member);
                        //ac.moveToNext();
                        ac.moveToNext();//Move to next time
                    }
                    ac.close();
                }
                else {
                    for (int i = 0; i < ac.getCount(); i += 60) {//Write every 60 entries
                        member.setT1_Data_string(ac.getString(1));//T1
                        member.setT2_Data_string(ac.getString(2));//T2
                        member.setT3_Data_string(ac.getString(4));//T3
                        member.setT4_Data_string(ac.getString(5));//T4
                        member.setPdiff_Data_string(ac.getString(6));//P1 data
                        member.setTime_string(ac.getString(7));

                        Userdata_number_of_data = Userdata_number_of_data + 1;
                        mDatareff_User.child(valueOf(Userdata_number_of_data)).setValue(member);//Increment max id count

                        //mDatareff_User.child(valueOf(i)).setValue(member);
                        //ac.moveToNext();
                        ac.moveToPosition(i);
                    }
                    ac.close();
                }
            }
            Log.d(TAG, "Stored mass data");//Log the event
            Utils.toast(getApplicationContext(), "Stored mass data");//Inform the user
        }
    }

    private void Home(){
        Intent myIntent = new Intent(Local_data_activity.this, Home_activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data",User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        Local_data_activity.this.startActivity(myIntent);
        finish();
    }

    private void New_Graph()
    {
        local_linechart.invalidate();
        local_linechart.clear();
        int x_records = 0;
        int y_records = 0;
        ArrayList<String> xAxes = new ArrayList<>();
        ArrayList<Entry> yAxes_1 = new ArrayList<>();
        ArrayList<Entry> yAxes_2 = new ArrayList<>();
        ArrayList<Entry> yAxes_3 = new ArrayList<>();//Fault line
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

        Cursor data = null;
        //data = BLE_DB.showData_Filtered_Day_1();//Initialise
        if(Day_to_view == 1)
        {
            data = BLE_DB.showData_Filtered_Day_1();
        }
        else if(Day_to_view == 2)
        {
            data = BLE_DB.showData_Filtered_Day_2();
        }
        else if(Day_to_view == 3)
        {
            data = BLE_DB.showData_Filtered_Day_3();
        }
        if((data.getCount() > 0) && (Filter_ran == true))//Null check also check that the filter has been run at least once
        {
            data.moveToFirst();
            //Xaxis
            //Add to the array list
            String aHour = "00", aMinute = "00", aSecond = "00";
            int atotal_hours = 0;
            int atotal_minutes = 0;
            int atotal_seconds = 0;
            int acurrent_seconds = 0;
            //for(int i = begining_seconds; i <= ending_seconds; i++)
            for (int i = 0; i <= 86400; i++) {
                acurrent_seconds = i;
                x_records = x_records + 1;
                if (acurrent_seconds > 60) {
                    atotal_hours = acurrent_seconds / 3600;//Devide to lower number to 24 hours

                    atotal_minutes = (acurrent_seconds / 60) - (60 * atotal_hours);

                    atotal_seconds = acurrent_seconds - (atotal_hours * 3600) - (atotal_minutes * 60);
                    aHour = Integer.toString(atotal_hours);
                    aMinute = Integer.toString(atotal_minutes);
                    aSecond = Integer.toString(atotal_seconds);
                } else {
                    aSecond = Integer.toString(acurrent_seconds);
                }

                xAxes.add(aHour + ":" + aMinute + ":" + aSecond);
            }

            //Yaxis
            int tcurrent_seconds = 0;
            if (Day_to_view == 1) {
                tcurrent_seconds = starting_seconds;
            } else {
                tcurrent_seconds = 0;//start at 0
            }
            for (int i = 0; i < data.getCount(); i++) {
                //Counter
                String tHour = "00", tMinute = "00", tSecond = "00";
                int ttotal_hours = 0;
                int ttotal_minutes = 0;
                int ttotal_seconds = 0;
                int data_count = data.getCount();
                //tcurrent_seconds = starting_seconds;

                //starting_seconds = starting_seconds + 1;
                if (tcurrent_seconds >= 86400) {
                    tcurrent_seconds = 0;//Reset
                }
                tcurrent_seconds = tcurrent_seconds + 1;

                y_records = y_records + 1;

                if (tcurrent_seconds > 60) {
                    ttotal_hours = tcurrent_seconds / 3600;//Devide to lower number
                    ttotal_minutes = (tcurrent_seconds - (ttotal_hours * 3600)) / 60;
                    ttotal_seconds = tcurrent_seconds - (ttotal_hours * 3600) - (ttotal_minutes * 60);
                    tHour = Integer.toString(ttotal_hours);
                    tMinute = Integer.toString(ttotal_minutes);
                    tSecond = Integer.toString(ttotal_seconds);
                } else {
                    tSecond = Integer.toString(tcurrent_seconds);
                }
                int this_seconds = 0;
                this_seconds = ((Integer.parseInt(tHour)) * 3600) + ((Integer.parseInt(tMinute)) * 60) + Integer.parseInt(tSecond);
                //Spinner 1
                if (Data_Spinner_value_1.equals("T1")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(1)));//Gets the counter value and sets it on the y axis
                    yAxes_1.add(new Entry((int) yaxis, this_seconds));
                } else if (Data_Spinner_value_1.equals("T2")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(2)));//Gets the counter value and sets it on the y axis
                    yAxes_1.add(new Entry((int) yaxis, this_seconds));
                } else if (Data_Spinner_value_1.equals("T3")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(4)));//Gets the counter value and sets it on the y axis
                    yAxes_1.add(new Entry((int) yaxis, this_seconds));
                } else if (Data_Spinner_value_1.equals("T4")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(5)));//Gets the counter value and sets it on the y axis
                    yAxes_1.add(new Entry((int) yaxis, this_seconds));
                } else if (Data_Spinner_value_1.equals("Pd")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(6)));//Gets the counter value and sets it on the y axis
                    yAxes_1.add(new Entry((int) yaxis, this_seconds));
                }
                //Spinner 2
                if (Data_Spinner_value_2.equals("T1")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(1)));//Gets the counter value and sets it on the y axis
                    yAxes_2.add(new Entry((int) yaxis, this_seconds));
                } else if (Data_Spinner_value_2.equals("T2")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(2)));//Gets the counter value and sets it on the y axis
                    yAxes_2.add(new Entry((int) yaxis, this_seconds));
                } else if (Data_Spinner_value_2.equals("T3")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(4)));//Gets the counter value and sets it on the y axis
                    yAxes_2.add(new Entry((int) yaxis, this_seconds));
                } else if (Data_Spinner_value_2.equals("T4")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(5)));//Gets the counter value and sets it on the y axis
                    yAxes_2.add(new Entry((int) yaxis, this_seconds));
                } else if (Data_Spinner_value_2.equals("Pd")) {
                    float yaxis = Float.parseFloat(valueOf(data.getString(6)));//Gets the counter value and sets it on the y axis
                    yAxes_2.add(new Entry((int) yaxis, this_seconds));
                }
                data.moveToNext();
            }
            //Loop to create line
            for(int i = 0; i< 7000; i +=100)//Create a fault line will need to pass in the correct seconds
            {
                yAxes_3.add(new Entry((int) i, 45000));
            }

            String[] xaxes = new String[xAxes.size()];

            for (int i = 0; i < xAxes.size(); i++) {
                xaxes[i] = xAxes.get(i).toString();
            }

            ArrayList<String> xAxisLabel = new ArrayList<>();

            LineDataSet lineDataSet1 = new LineDataSet(yAxes_1, Data_Spinner_value_1);//Second componenet is the label
            LineDataSet lineDataSet2 = new LineDataSet(yAxes_2, Data_Spinner_value_2);
            if(Graph_process == true) {//True for datascience
                if (ds_selected_inlet.equals(Data_Spinner_value_1)) {
                    lineDataSet1 = new LineDataSet(yAxes_1, "Inlet");//Second componenet is the laebel
                }
                if (ds_selected_outlet.equals(Data_Spinner_value_2)) {
                    lineDataSet2 = new LineDataSet(yAxes_2, "Outlet");//Second componenet is the laebel
                }

                //Striaght line on the point of error
                LineDataSet lineDataSet3 = new LineDataSet(yAxes_3, "Fault");
                lineDataSet3.setDrawCircles(false);
                lineDataSet3.setColor(Color.BLACK);
                lineDataSets.add(lineDataSet3);
            }
            lineDataSet1.setDrawCircles(false);
            lineDataSet1.setColor(Color.RED);
            lineDataSets.add(lineDataSet1);
            lineDataSet2.setDrawCircles(false);
            lineDataSet2.setColor(Color.BLUE);
            lineDataSets.add(lineDataSet2);

            int month = 0;
            int year = 0;
            if(Data_type_string.equals("Live"))
            {
                local_linechart.setDescription("Live data");
            }
            else {
                if (Day_to_view == 1) {
                    month = Integer.parseInt(sMonth);
                    year = Integer.parseInt(sYear);
                    local_linechart.setDescription(Day_1 + "/" + month + "/" + year);
                } else if (Day_to_view == 2) {
                    month = Integer.parseInt(sMonth);
                    year = Integer.parseInt(sYear);
                    if (Day_1 + 1 != Day_2) {
                        month = Integer.parseInt(sMonth) + 1;
                        if (month == 13) {
                            month = 1;
                            year = Integer.parseInt(sYear) + 1;
                        }
                    }
                    local_linechart.setDescription(Day_2 + "/" + month + "/" + year);

                } else if (Day_to_view == 3) {
                    month = Integer.parseInt(sMonth);
                    year = Integer.parseInt(sYear);
                    if (Day_3 + 1 != Day_2) {
                        month = Integer.parseInt(sMonth) + 1;
                        if (month == 13) {
                            month = 1;
                            year = Integer.parseInt(sYear) + 1;
                        }
                    }
                    local_linechart.setDescription(Day_3 + "/" + month + "/" + year);

                }
            }

            local_linechart.setData(new LineData(xaxes, lineDataSets));
            //local_linechart.setVisibleXRange(min,max);


            local_linechart.setTouchEnabled(true);
            local_linechart.setDragEnabled(true);
            local_linechart.invalidate();//Refresh the graph
        }
        else
        {
            Utils.toast(getApplicationContext(),"No data");
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private void Process_data_science() {
        int temp_diff = 1000;//10°C
        D1_visible = false;
        D2_visible = false;
        D3_visible = false;
        //Use data science database
        BLE_DB.deleteAll_Data_Science();

        btn_day_one.setVisibility(View.INVISIBLE);
        btn_day_two.setVisibility(View.INVISIBLE);
        btn_day_three.setVisibility(View.INVISIBLE);
        //Select the test
        //Setup the dialogs
        dialog_ds_select_test = new Dialog(this);

        //Show the dialog
        dialog_ds_select_test.show();
        //Set the content view
        dialog_ds_select_test.setContentView(R.layout.dialog_ds_select_process);//Go to the select process
        //Test select buttons
        Button btn_let_test = dialog_ds_select_test.findViewById(R.id.btn_ds_let_test);
        Button btn_no_test  = dialog_ds_select_test.findViewById(R.id.btn_ds_no_test);


        //dialog_ds_select_test.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btn_let_test.setOnClickListener(new View.OnClickListener() {//Show next dialog
            @Override
            public void onClick(View v) {
                inlet_outlet_test_selected = true;
                dialog_ds_select_test.dismiss();//Turn off the yes / no dialog

            }
        });
        btn_no_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inlet_outlet_test_selected = false;
                dialog_ds_select_test.dismiss();//Turn off the yes / no dialog
            }
        });
        if(inlet_outlet_test_selected == true) {
            inlet_Select();//Select the inlet
            dialog_ds_select_test.dismiss();//Turn off the yes / no dialog
        }
    }

    private void inlet_Select()
    {
        inlet_outlet_test_selected = false;//Reset the testing
        dialog_ds_inlet_select = new Dialog(this);
        dialog_ds_inlet_select.show();

        dialog_ds_inlet_select.setContentView(R.layout.dialog_ds_inlet_select);//Go to the select process
        //Inlet buttons
        Button btn_ds_inlet_t1 = dialog_ds_inlet_select.findViewById(R.id.btn_inlet_t1);//t1
        Button btn_ds_inlet_t2 = dialog_ds_inlet_select.findViewById(R.id.btn_inlet_t2);//t2
        Button btn_ds_inlet_t3 = dialog_ds_inlet_select.findViewById(R.id.btn_inlet_t3);//t3
        Button btn_ds_inlet_t4 = dialog_ds_inlet_select.findViewById(R.id.btn_inlet_t4);//t4
        //dialog_ds_inlet_select.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        btn_ds_inlet_t1.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v) {ds_selected_inlet = "T1"; outlet_select();}});
        btn_ds_inlet_t2.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v) {ds_selected_inlet = "T2"; outlet_select();}});
        btn_ds_inlet_t3.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v) {ds_selected_inlet = "T3"; outlet_select();}});
        btn_ds_inlet_t4.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v) {ds_selected_inlet = "T4"; outlet_select();}});


    }

    private void outlet_select()
    {
        dialog_ds_inlet_select.dismiss();
        dialog_ds_outlet_select = new Dialog(this);
        dialog_ds_outlet_select.show();
        dialog_ds_outlet_select.setContentView(R.layout.dialog_ds_outlet_select);//Go to the select process


        //Outlet buttons need to remove previously selected button
        Button btn_ds_outlet_t1 = dialog_ds_outlet_select.findViewById(R.id.btn_outlet_t1);//t1
        Button btn_ds_outlet_t2 = dialog_ds_outlet_select.findViewById(R.id.btn_outlet_t2);//t2
        Button btn_ds_outlet_t3 = dialog_ds_outlet_select.findViewById(R.id.btn_outlet_t3);//t3
        Button btn_ds_outlet_t4 = dialog_ds_outlet_select.findViewById(R.id.btn_outlet_t4);//t4
        //Hide selected inlet
        if(ds_selected_inlet.equals(("T1")))
        {
            btn_ds_outlet_t1.setVisibility(View.INVISIBLE);
        }
        else if(ds_selected_inlet.equals(("T2")))
        {
            btn_ds_outlet_t2.setVisibility(View.INVISIBLE);
        }
        else if(ds_selected_inlet.equals(("T3")))
        {
            btn_ds_outlet_t3.setVisibility(View.INVISIBLE);
        }
        else if(ds_selected_inlet.equals(("T4")))
        {
            btn_ds_outlet_t4.setVisibility(View.INVISIBLE);
        }


        //Call back buttons
        btn_ds_outlet_t1.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v) {ds_selected_outlet = "T1"; Run_inlet_outlet_data_science();}});
        btn_ds_outlet_t2.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v) {ds_selected_outlet = "T2"; Run_inlet_outlet_data_science();}});
        btn_ds_outlet_t3.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v) {ds_selected_outlet = "T3"; Run_inlet_outlet_data_science();}});
        btn_ds_outlet_t4.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v) {ds_selected_outlet = "T4"; Run_inlet_outlet_data_science();}});

    }
    private void Run_inlet_outlet_data_science()
    {
        dialog_ds_outlet_select.dismiss();
        //Log an output string
        Utils.toast(getApplicationContext(),"Running data science on: " + ds_selected_inlet +" and : " + ds_selected_outlet);
        //loop through data
        //compare tin to tout if not greater than 30°C draw a line at the first point in time

        BLE_DB.deleteAll_Filtered_Day_1();
        BLE_DB.deleteAll_Filtered_Day_2();
        BLE_DB.deleteAll_Filtered_Day_3();
        BLE_DB.deleteAll_Filtered();
        BLE_DB.deleteAll_Filtered_Log();
        BLE_DB.deleteAll_Filtered_Live();

        //Processing loop
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Cursor data;
        boolean Start_time_acquired = false;
        boolean Date_set = false;
        String T1_Data_string = "";
        String T2_Data_string = "";
        String T3_Data_string = "";
        String T4_Data_string = "";
        String Pdiff_Data_string = "";
        String time = "";
        String Second_part = "";
        String Third_part = "";
        String iDay = "";
        data = BLE_DB.showData_Sensor_Board();//Sensor board filter
        if(Data_type_string.equals("Live")) {
            data = BLE_DB.showData_Sensor_Board();
        }
        else if(Data_type_string.equals("Log")) {
            data = BLE_DB.showData_Log();
        }
        else if(Data_type_string.equals("Downloaded")) {
            data = BLE_DB.showData_Downloaded();
        }

        if(data.getCount() != 0) {
            //Cursor window fix and get data
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                CursorWindow cw = new CursorWindow("test", 67108864);//16777216 broke,536870912
                AbstractWindowedCursor ac = (AbstractWindowedCursor) data;
                ac.setWindow(cw);
                ac.moveToFirst();//0

                if (ac.getCount() == 0)//No data selected
                {
                    Utils.toast(getApplicationContext(), "No data selected");
                }
                for (int i = 0; i < ac.getCount(); i++) {

                    if (Data_type_string.equals("Live")) {//Live data
                        T1_Data_string = ac.getString(1);
                        T2_Data_string = ac.getString(2);
                        T3_Data_string = ac.getString(4);
                        T4_Data_string = ac.getString(5);
                        Pdiff_Data_string = ac.getString(6);
                        time = ac.getString(7);
                        if (Start_time_acquired == false) {
                            Start_date = time;
                            Start_time_acquired = true;
                            StringTokenizer tokens = new StringTokenizer(Start_date, " ");
                            sDay_of_week = tokens.nextToken();//Day of week
                            sMonth = tokens.nextToken();//Month
                            sDay = tokens.nextToken();//Day
                            sTime_of_day = tokens.nextToken();//Time of day
                            sGMT = tokens.nextToken();//GMT
                            sYear = tokens.nextToken();//Year
                            StringTokenizer timetokens = new StringTokenizer(sTime_of_day, ":");//This substring is split with a :
                            sHour = timetokens.nextToken();//Hour
                            sMinute = timetokens.nextToken();//Minute
                            sSecond = timetokens.nextToken();//Second
                            starting_seconds = (Integer.parseInt(sHour) * 3600) + (Integer.parseInt(sMinute) * 60) + (Integer.parseInt(sSecond));

                            if (Date_set == false) {
                                Day_1 = Integer.parseInt(sDay);
                                if (Day_1 == 28 && Integer.parseInt(sMonth) == 2)//28 check Feb check
                                {
                                    if (Integer.parseInt(sYear) % 100 == 0 && (Integer.parseInt(sYear) % 400 == 0))//Leap year
                                    {
                                        Day_2 = Day_1 + 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    }
                                    //Not a leap year
                                    Day_2 = 1;
                                    Day_3 = Day_2 + 1;
                                    Date_set = true;
                                } else if (Day_1 == 29) {
                                    Day_2 = Day_1 + 1;
                                    Day_3 = Day_2 + 1;
                                    Date_set = true;
                                } else if (Day_1 == 30)//Check 31 days
                                {
                                    if ((Integer.parseInt(sMonth) == 1) || (Integer.parseInt(sMonth) == 3) || (Integer.parseInt(sMonth) == 5)
                                            || (Integer.parseInt(sMonth) == 7) || (Integer.parseInt(sMonth) == 8) || (Integer.parseInt(sMonth) == 10)
                                            || (Integer.parseInt(sMonth) == 12)) {
                                        Day_2 = 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    } else//No a 31 month
                                    {
                                        Day_2 = Day_1 + 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    }
                                } else {
                                    Day_2 = Day_1 + 1;
                                    Day_3 = Day_2 + 1;
                                }
                            }//Set date 1 ,2 and 3]

                            StringTokenizer sting_token = new StringTokenizer(time, " ");
                            String lDay_of_week = sting_token.nextToken();//Day of week
                            String lMonth = sting_token.nextToken();//Month
                            String lDay = sting_token.nextToken();//Day
                            String lTime_of_day = sting_token.nextToken();//Time of day
                            String lGMT = sting_token.nextToken();//GMT
                            String lYear = sting_token.nextToken();//Year
                            StringTokenizer string_time_token = new StringTokenizer(lTime_of_day, ":");//This substring is split with a :
                            String lHour = string_time_token.nextToken();//Hour
                            String lMinute = string_time_token.nextToken();//Minute
                            String lSecond = string_time_token.nextToken();//Second
                            iDay = lDay;

                        }
                    } else if (Data_type_string.equals("Log") || ((Data_type_string.equals("Downloaded")))) { //Log data
                        T1_Data_string = ac.getString(1);
                        T2_Data_string = ac.getString(2);
                        T3_Data_string = ac.getString(4);
                        T4_Data_string = ac.getString(5);
                        Pdiff_Data_string = ac.getString(6);
                        time = ac.getString(7);
                        //time = "2020-05-20 12:51:19 GMT
                        if (Start_time_acquired == false) {
                            Start_date = time;
                            Start_time_acquired = true;
                            StringTokenizer tokens = new StringTokenizer(Start_date, "-");
                            sYear = tokens.nextToken();//Year
                            sMonth = tokens.nextToken();//Month
                            sDay = tokens.nextToken();//Day
                            StringTokenizer tokens_next = new StringTokenizer(sDay, " ");
                            sDay = tokens_next.nextToken();//Day
                            Second_part = tokens_next.nextToken();
                            Third_part = tokens_next.nextToken();
                            StringTokenizer tokens_time = new StringTokenizer(Second_part, ":");
                            sHour = tokens_time.nextToken();//Hour
                            sMinute = tokens_time.nextToken();//Minute
                            sSecond = tokens_time.nextToken();//Second
                            StringTokenizer tokens_zone = new StringTokenizer(Third_part, " ");
                            sGMT = tokens_zone.nextToken();//GMT
                            starting_seconds = (Integer.parseInt(sHour) * 3600) + (Integer.parseInt(sMinute) * 60) + (Integer.parseInt(sSecond));

                            if (Date_set == false) {
                                Day_1 = Integer.parseInt(sDay);
                                if (Day_1 == 28 && Integer.parseInt(sMonth) == 2)//28 check Feb check
                                {
                                    if (Integer.parseInt(sYear) % 100 == 0 && (Integer.parseInt(sYear) % 400 == 0))//Leap year
                                    {
                                        Day_2 = Day_1 + 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    }
                                    //Not a leap year
                                    Day_2 = 1;
                                    Day_3 = Day_2 + 1;
                                    Date_set = true;
                                } else if (Day_1 == 29) {
                                    Day_2 = Day_1 + 1;
                                    Day_3 = Day_2 + 1;
                                    Date_set = true;
                                } else if (Day_1 == 30)//Check 31 days
                                {
                                    if ((Integer.parseInt(sMonth) == 1) || (Integer.parseInt(sMonth) == 3) || (Integer.parseInt(sMonth) == 5)
                                            || (Integer.parseInt(sMonth) == 7) || (Integer.parseInt(sMonth) == 8) || (Integer.parseInt(sMonth) == 10)
                                            || (Integer.parseInt(sMonth) == 12)) {
                                        Day_2 = 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    } else//No a 31 month
                                    {
                                        Day_2 = Day_1 + 1;
                                        Day_3 = Day_2 + 1;
                                        Date_set = true;
                                    }
                                } else {
                                    Day_2 = Day_1 + 1;
                                    Day_3 = Day_2 + 1;
                                }
                            }//Set date 1 ,2 and 3

                        }//Set the start date

                        StringTokenizer tokens = new StringTokenizer(time, "-");
                        String iYear = tokens.nextToken();//Year
                        String iMonth = tokens.nextToken();//Month
                        iDay = tokens.nextToken();//Day
                        StringTokenizer tokens_next = new StringTokenizer(iDay, " ");
                        iDay = tokens_next.nextToken();//Day
                        /*
                        String iSecond_part = tokens_next.nextToken();
                        String iThird_part = tokens_next.nextToken();

                        StringTokenizer tokens_time = new StringTokenizer(iSecond_part, ":");
                        String isHour = tokens_time.nextToken();//Hour
                        String isMinute = tokens_time.nextToken();//Minute
                        String isSecond = tokens_time.nextToken();//Second
                        StringTokenizer tokens_zone = new StringTokenizer(iThird_part, " ");
                        String iGMT = tokens_zone.nextToken();//GMT

                         */
                    }

                    if (Data_type_string.equals("Live")) {
                        boolean result = BLE_DB.addData_Filtered_Live(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                        if (result == true) {
                            Log.d(TAG, "data_changes: Added data correctly");
                        }
                        if (result == false) {
                            Log.d(TAG, "data_changes: did not add data correctly");
                        }//false
                    } else if (Data_type_string.equals("Log")) {
                        boolean result = BLE_DB.addData_Filtered_Log(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                        if (result == true) {
                            Log.d(TAG, "data_changes: Added data correctly");
                        }
                        if (result == false) {
                            Log.d(TAG, "data_changes: did not add data correctly");
                        }//false
                    } else if (Data_type_string.equals("Downloaded")) {
                        boolean result = BLE_DB.addData_Filtered_Downloaded(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                        if (result == true) {
                            Log.d(TAG, "data_changes: Added data correctly");
                        }
                        if (result == false) {
                            Log.d(TAG, "data_changes: did not add data correctly");
                        }//false
                    }


                    if (Integer.parseInt(iDay) == Day_1) {
                        boolean result_d1 = BLE_DB.addData_Filtered_Day_1(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                        if (result_d1 == true) {
                            Log.d(TAG, "data_changes: Added data correctly");
                        }
                        if (result_d1 == false) {
                            Log.d(TAG, "data_changes: did not add data correctly");
                        }//false
                        if (D1_visible == false) {
                            btn_day_one.setVisibility(View.VISIBLE);
                            D1_visible = true;
                        }

                    } else if (Integer.parseInt(iDay) == Day_2) {
                        boolean result_d2 = BLE_DB.addData_Filtered_Day_2(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                        if (result_d2 == true) {
                            Log.d(TAG, "data_changes: Added data correctly");
                        }
                        if (result_d2 == false) {
                            Log.d(TAG, "data_changes: did not add data correctly");
                        }//false
                        if (D2_visible == false) {
                            btn_day_two.setVisibility(View.VISIBLE);
                            D2_visible = true;
                        }
                    } else if (Integer.parseInt(iDay) == Day_3) {
                        boolean result_d3 = BLE_DB.addData_Filtered_Day_3(T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Pdiff_Data_string, time);
                        if (result_d3 == true) {
                            Log.d(TAG, "data_changes: Added data correctly");
                        }
                        if (result_d3 == false) {
                            Log.d(TAG, "data_changes: did not add data correctly");
                        }//false
                        if (D3_visible == false) {
                            btn_day_three.setVisibility(View.VISIBLE);
                            D3_visible = true;
                        }
                    }
                    ac.moveToNext();

                }
            }
        }
        if(data.getCount() == 0)
        {
            Utils.toast(getApplicationContext(),"No data");
        }
        Filter_ran = true;//Need this line to graph the data
        //Done
        Graph_process = true;
        New_Graph();//See if it will change the x axis format
    }
}

