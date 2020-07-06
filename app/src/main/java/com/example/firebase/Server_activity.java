package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.StringTokenizer;

import static com.example.firebase.Home_activity.SHARDED_PREFS;
import static com.example.firebase.Home_activity.Stored_bounded_device;
import static com.example.firebase.Loginactivity.BLE_DB;
import static com.example.firebase.Loginactivity.mDatareff_User;
public class Server_activity extends AppCompatActivity {
    private final static String TAG = Server_activity.class.getSimpleName();//Debugging tag
    private CardView btn_pull_data, btn_home, btn_delete_server_data;
    private String User_data;
    private Global_Settings global_settings;
    private String t1_string,t2_string,t3_string,t4_string,pd_string,time_string;
    private Dialog dialog_confirm_pull_server_data;
    public int Userdata_number_of_data = 0;
    private long data_pulled_from_firebase = 0;
    private boolean fire_base_skip = false;
    private int t1_second=0, t2_second =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        Bundle bundle = getIntent().getExtras();
        User_data = bundle.getString("User_data");

        global_settings = Global_Settings.getInstance();
        //Buttons
        btn_pull_data = findViewById(R.id.btn_pull_server_data);
        btn_home =  findViewById(R.id.btn_home_activity);
        btn_delete_server_data = findViewById(R.id.btn_delete_server_data);

        mDatareff_User = FirebaseDatabase.getInstance().getReference().child(global_settings.Get_Bound_device());
        setTitle("Server Data");//Change the title to Scanner as the device is no longer being read

        mDatareff_User.addValueEventListener(new ValueEventListener(){//Count the ID values for the phone database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Userdata_number_of_data =(int)(dataSnapshot.getChildrenCount());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){}
        });
        BLE_DB.deleteAll_Filtered();//Clear filter data

        //Edit text

        //Button listeners
        btn_pull_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Confirm_Pull_Data();
            }
        });
        btn_home.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Home();
            }
        });
        btn_delete_server_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Delete_data();
            }
        });
    }
    private void Home(){
        Intent myIntent = new Intent(Server_activity.this, Home_activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data",User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        Server_activity.this.startActivity(myIntent);
        finish();
    }
    private void Confirm_Pull_Data()
    {
        dialog_confirm_pull_server_data = new Dialog(this);
        //Show the dialog
        dialog_confirm_pull_server_data.show();
        //Set the content view
        dialog_confirm_pull_server_data.setContentView(R.layout.dialog_pull_server_data);

        Button btn_start_yes = dialog_confirm_pull_server_data.findViewById(R.id.btn_pull_server_data_yes);
        Button btn_start_no  = dialog_confirm_pull_server_data.findViewById(R.id.btn_pull_server_data_no);
        dialog_confirm_pull_server_data.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btn_start_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_confirm_pull_server_data.dismiss();
                Pulldata();
            }
        });
        btn_start_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_confirm_pull_server_data.dismiss();
            }
        });
    }
    private void Pulldata()//Get data from the server and display it
    {
        BLE_DB.deleteAll_Downloaded();
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Getting data from the server");
        pd.setIndeterminate(true);
        pd.setCancelable(false);


        pd.show();
        Thread mThread = new Thread() {
            @Override
            public void run() {

            mDatareff_User = FirebaseDatabase.getInstance().getReference().child(global_settings.Get_Bound_device());//Get the reference of the device
            String device = global_settings.Get_Bound_device();
            mDatareff_User.addListenerForSingleValueEvent(new ValueEventListener() {
                boolean t1_got= false,t2_got= false;
                String t1Second="", t2Second="";
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long data_added = 0;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        long count = dataSnapshot.getChildrenCount();
                        if(count == 0)
                        {
                            //pd.dismiss();
                        }
                        t1_string = data.child("t1_Data_string").getValue().toString();
                        t2_string = data.child("t2_Data_string").getValue().toString();
                        t3_string = data.child("t3_Data_string").getValue().toString();
                        t4_string = data.child("t4_Data_string").getValue().toString();
                        pd_string = data.child("pdiff_Data_string").getValue().toString();
                        time_string = data.child("time_string").getValue().toString();


                        //check time data

                        if((t2_got == false) &&(t1_got == true))
                        {
                            StringTokenizer tokens = new StringTokenizer(time_string, "-");
                            String iYear = tokens.nextToken();//Year
                            String iMonth = tokens.nextToken();//Month
                            String iDay = tokens.nextToken();//Day
                            StringTokenizer tokens_next = new StringTokenizer(iDay, " ");
                            iDay = tokens_next.nextToken();//Day

                            String iSecond_part = tokens_next.nextToken();
                            String iThird_part = tokens_next.nextToken();

                            StringTokenizer tokens_time = new StringTokenizer(iSecond_part, ":");
                            String isHour = tokens_time.nextToken();//Hour
                            String isMinute = tokens_time.nextToken();//Minute
                            t2Second = tokens_time.nextToken();//Second
                            StringTokenizer tokens_zone = new StringTokenizer(iThird_part, " ");
                            String iGMT = tokens_zone.nextToken();//GMT
                            t2_got = true;
                        }
                        if(t1_got == false)
                        {
                            StringTokenizer tokens = new StringTokenizer(time_string, "-");
                            String iYear = tokens.nextToken();//Year
                            String iMonth = tokens.nextToken();//Month
                            String iDay = tokens.nextToken();//Day
                            StringTokenizer tokens_next = new StringTokenizer(iDay, " ");
                            iDay = tokens_next.nextToken();//Day

                            String iSecond_part = tokens_next.nextToken();
                            String iThird_part = tokens_next.nextToken();

                            StringTokenizer tokens_time = new StringTokenizer(iSecond_part, ":");
                            String isHour = tokens_time.nextToken();//Hour
                            String isMinute = tokens_time.nextToken();//Minute
                            t1Second = tokens_time.nextToken();//Second
                            StringTokenizer tokens_zone = new StringTokenizer(iThird_part, " ");
                            String iGMT = tokens_zone.nextToken();//GMT
                            t1_got = true;
                        }
                        if(t1_got == true && t2_got == true) {
                            if (((Integer.parseInt(t1Second) + 1) == Integer.parseInt(t2Second))
                                    || (Integer.parseInt(t1Second) == 60 && Integer.parseInt(t2Second) == 0))//Check if 1 second different
                            {

                                fire_base_skip = false;
                            } else {
                                fire_base_skip = true;
                            }
                        }


                        if(fire_base_skip = true)//Write every 60 seconds
                        {
                            for(int i =0;i<60;i++) {//Add 100 entries per entry to fill out graph
                                boolean result = BLE_DB.addData_Downloaded(t1_string, t2_string, t3_string, t4_string, pd_string, time_string);//Add data
                                if (result == true) {
                                    Log.d(TAG, "data_changes: Added data correctly");
                                }
                                if (result == false) {
                                    Log.d(TAG, "data_changes: did not add data correctly");
                                }//false
                                data_added = data_added + 1;
                                data_pulled_from_firebase++;
                                if (data_added == count*60) {
                                    pd.dismiss();
                                }
                            }
                        }
                        else//1 entry per second
                        {
                            boolean result = BLE_DB.addData_Downloaded(t1_string, t2_string, t3_string, t4_string, pd_string, time_string);//Add data
                            if (result == true) {
                                Log.d(TAG, "data_changes: Added data correctly");
                            }
                            if (result == false) {
                                Log.d(TAG, "data_changes: did not add data correctly");
                            }//false
                            data_added = data_added + 1;
                            data_pulled_from_firebase++;
                            if (data_added == count) {
                                pd.dismiss();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //pd.dismiss();
                }
            });
            }
        };
        mThread.start();


    }
    private void Delete_data()
    {
        for(int id = 0; id <= Userdata_number_of_data; id ++) {
            mDatareff_User.getDatabase().getReference().child(Integer.toString(id));
            mDatareff_User.removeValue();
        }
    }

}
