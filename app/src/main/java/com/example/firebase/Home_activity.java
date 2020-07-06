package com.example.firebase;
/*
Function where the main functions will run from
 */
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class Home_activity extends AppCompatActivity {

    private CardView btn_view_live_data,btn_local_data,btn_view_server_data,btn_logout_home,btn_settings;
    private TextView text_logged_in_as, text_bound_to;
    private View view;
    private String User_data;//Passed in from the login activity
    private FirebaseAuth mAuth;
    private Global_Settings global_settings;
    public static final String SHARDED_PREFS = "sharedPrefs";
    public static final String Stored_bounded_device = "null";
    private Dialog dialog_start_bind;
    private final static int REQUEST_ENABLE_BT=1;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    private boolean No_device_edition = true;//Trun no device edition / false not

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        view = this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.background);
        global_settings = Global_Settings.getInstance();//Get access to the settings
        text_logged_in_as = findViewById(R.id.text_logged_in_as);
        text_bound_to = findViewById(R.id.text_bound_to);
        btn_view_live_data =  findViewById(R.id.btn_view_live_data);
        btn_local_data = findViewById(R.id.btn_local_data);
        btn_view_server_data = findViewById(R.id.btn_view_server_data);
        btn_logout_home = findViewById(R.id.btn_logout_home);
        btn_settings = findViewById(R.id.btn_settings);//com.example.firebase.Settings
        btn_view_live_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View_Live_Data();
            }
        });
        btn_local_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View_Phone_Data();
            }
        });
        btn_view_server_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View_Server_Data();
            }
        });
        btn_logout_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logout();
            }
        });
        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings();
            }
        });
        mAuth = FirebaseAuth.getInstance();//Get an instance of the firebase
        //Bundle is used to identify the user's email address
        Bundle bundle = getIntent().getExtras();
        User_data = bundle.getString("User_data");

        if(User_data.equals("admin"))//Disable firebase functionality
        {
            btn_view_server_data.setVisibility(View.GONE);
        }
        text_logged_in_as.setText("Signed in as: " + User_data);

        display_UI();
        checkPermission();

        if(No_device_edition == true) {
            Utils.toast(getApplicationContext(),"No ble device edition");
            SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);
            String bound_device = sharedPreferences.getString(Stored_bounded_device, "Null");
            String device_to_bind_to = "D3:68:62:99:90:C3";
            global_settings.Set_Bound_Device(device_to_bind_to);

            if (global_settings.Get_Bound_device().equals("Null")) {
                //Bind device
                Go_to_bind_device();
            } else {
                text_bound_to.setTextSize(8);
                text_bound_to.setText("Bound to" + global_settings.Get_Bound_device() + "No device edition");
            }
        }else {//False
            SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);
            String bound_device = sharedPreferences.getString(Stored_bounded_device, "Null");
            global_settings.Set_Bound_Device(bound_device);

            if (global_settings.Get_Bound_device().equals("Null")) {
                //Bind device
                Go_to_bind_device();
            } else {
                text_bound_to.setText("Bound to" + global_settings.Get_Bound_device());
            }
        }

    }
    private void Go_to_bind_device()
    {
        dialog_start_bind = new Dialog(this);
        //Show the dialog
        dialog_start_bind.show();
        //Set the content view
        dialog_start_bind.setContentView(R.layout.dialog_start_bind);

        Button btn_start_yes = dialog_start_bind.findViewById(R.id.btn_go_to_bind_yes);
        Button btn_start_no  = dialog_start_bind.findViewById(R.id.btn_go_to_bind_no);
        dialog_start_bind.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btn_start_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Got to bind device
                Utils.toast(getApplicationContext(),"Please bind a device");
                Intent myIntent = new Intent(Home_activity.this, Bind_device_activity.class);
                Bundle bundle = new Bundle();
                bundle.putString("User_data",User_data);//Pass through the user's email address to the main activity for display
                myIntent.putExtras(bundle);
                Home_activity.this.startActivity(myIntent);
                finish();
                dialog_start_bind.dismiss();

            }
        });
        btn_start_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.toast(getApplicationContext(),"No device bound");
            }
        });
    }
    private void display_UI(){
        setTitle("Home" + "Logged in as: " + User_data);//Change the title to Scanner as the device is no longer being read
    }
    private void View_Live_Data(){
        Intent myIntent = new Intent(Home_activity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data", User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        Home_activity.this.startActivity(myIntent);//Run the main activity}
        finish();
    }
    private void Settings()
    {
        Intent myIntent = new Intent(Home_activity.this, Settings_activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data", User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        Home_activity.this.startActivity(myIntent);//Run the main activity}
        finish();
    }
    private void View_Phone_Data(){
        Utils.toast(getApplicationContext(), "Login successful");//Pass information to the display
        Intent myIntent = new Intent(Home_activity.this, Local_data_activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data", User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        Home_activity.this.startActivity(myIntent);//Run the main activity}
        finish();
    }
    private void View_Server_Data(){
        Intent myIntent = new Intent(Home_activity.this, Server_activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data", User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        Home_activity.this.startActivity(myIntent);
        finish();
    }
    private void Logout(){
        mAuth.signOut();
        Intent myIntent = new Intent(Home_activity.this, Loginactivity.class);
        Home_activity.this.startActivity(myIntent);
        finish();
    }
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }
            if(!mBluetoothAdapter.isEnabled())//Turn on bluetooth
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        } else {
            checkPermission();
        }
    }
}

