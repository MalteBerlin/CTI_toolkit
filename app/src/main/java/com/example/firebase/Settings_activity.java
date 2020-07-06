package com.example.firebase;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import static com.example.firebase.Loginactivity.BLE_DB;

public class Settings_activity extends AppCompatActivity {

    private String User_data;//Passed in from the login activity
    private CardView Card_view_home;
    private View view;
    private Switch switch_advanced_mode;
    private Button btn_degrees_c;
    private Button btn_degrees_f;
    private Button btn_delete_local_data;
    private CardView Card_view_bind_device;
    private Dialog Confirm_delete_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        view = this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.background);

        switch_advanced_mode = findViewById(R.id.switch_advanced_mode);//Switching of advanced mode
        btn_degrees_c = findViewById(R.id.btn_degrees_c);//Degrees c
        btn_degrees_f = findViewById(R.id.btn_degrees_f);//Degrees f
        btn_delete_local_data = findViewById(R.id.btn_delete_local_data);
        Card_view_home = findViewById(R.id.btn_settings_home);//Home button
        Card_view_bind_device = findViewById(R.id.btn_bind_device);
        final Global_Settings global_settings = Global_Settings.getInstance();//Get access to the settings


        Bundle bundle = getIntent().getExtras();
        User_data = bundle.getString("User_data");
        if(User_data.equals("admin"))//Disable firebase functionality
        {
            //btn_view_server_data.setVisibility(View.GONE);
        }
        switch_advanced_mode.setChecked(global_settings.Get_Advanced_settings());
        if(global_settings.Get_temp_unit()== false) {
            btn_degrees_c.setBackgroundResource(R.drawable.green_button);
            btn_degrees_f.setBackgroundResource(R.drawable.red_button);
        }
        else if(global_settings.Get_temp_unit() == true)
        {
            btn_degrees_c.setBackgroundResource(R.drawable.red_button);
            btn_degrees_f.setBackgroundResource(R.drawable.green_button);
        }


        switch_advanced_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                global_settings.Set_Advanced_settings(isChecked);// do something, the isChecked will be
            }
        });
        btn_delete_local_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Confirm_delete_all();//Confirm delete all local data
            }
        });
        btn_degrees_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                global_settings.Set_Temp_Unit(false);//Degrees c
                btn_degrees_c.setBackgroundResource(R.drawable.green_button);
                btn_degrees_f.setBackgroundResource(R.drawable.red_button);
            }
        });
        btn_degrees_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                global_settings.Set_Temp_Unit(true);//F
                btn_degrees_c.setBackgroundResource(R.drawable.red_button);
                btn_degrees_f.setBackgroundResource(R.drawable.green_button);
            }
        });
        Card_view_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Home();
            }
        });
        Card_view_bind_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bind_device();
            }
        });
    }
    private void Confirm_delete_all()
    {
        Confirm_delete_data = new Dialog(this);
        //Show the dialog
        Confirm_delete_data.show();
        //Set the content view
        Confirm_delete_data.setContentView(R.layout.dialog_delete_all_data);

        Button btn_start_yes = Confirm_delete_data.findViewById(R.id.btn_delete_phone_data_yes);
        Button btn_start_no  = Confirm_delete_data.findViewById(R.id.btn_delete_phone_data_no);
        Confirm_delete_data.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btn_start_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Delete_all_local_data();
                Confirm_delete_data.dismiss();//Turn oof the yes / no dialog
            }
        });
        btn_start_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Confirm_delete_data.dismiss();//Turn oof the yes / no dialog
            }
        });

    }
    private void Delete_all_local_data()
    {
        BLE_DB.delete_all_tables();
        Utils.toast(getApplicationContext(),"Deleted all local data");
    }
    private void Bind_device()
    {
        Intent myIntent = new Intent(Settings_activity.this, Bind_device_activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data",User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        Settings_activity.this.startActivity(myIntent);
        finish();
    }
    private void Home() {//Logs out the user of the database and goes to the login screen.

        Intent myIntent = new Intent(Settings_activity.this, Home_activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("User_data",User_data);//Pass through the user's email address to the main activity for display
        myIntent.putExtras(bundle);
        Settings_activity.this.startActivity(myIntent);
        finish();
    }
}
