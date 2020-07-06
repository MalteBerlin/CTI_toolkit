package com.example.firebase;
/*
    This is the logging in activity
    It enables the application to log in securely to the firebase server through its authentication system
    The login activity has its own interface / layout

    There is also a register user feature that is commented out.

    This will then be added to a control activity once written
 */
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

public class Loginactivity extends AppCompatActivity{

        private String login_email_default = "admin";
        private String login_password_default = "admin";

        private View view;
        private FirebaseAuth mAuth;
        private Button /*btn_register,*/btn_login;

        public static DatabaseReference mDatareff_User;
        public static DatabaseHelper BLE_DB;//DatabaseHelper Bluetooth_databases for phone and the boards
        public static Calibration calibration;//Used to convert the integer value to a temperature value

        private com.google.android.material.textfield.TextInputLayout Email_input, Password_input;
        private TextView attempts_remaining,Offline_login,version_number_text,text_view_name;
        private final static String TAG = Loginactivity.class.getSimpleName();//Debugging tag
        private int Login_counter = 0;
        private final int Counter_max = 5;//Maximum number of attempts that can be made final so it can not be changed
        private int number_of_attempts_remaining;
        private String logged_in_user_string;
        private boolean Toggle_offine =false;
        @Override
        protected void onCreate(Bundle savedInstanceState) {//Constructor
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_login);
                view = this.getWindow().getDecorView();
                view.setBackgroundResource(R.color.background);

                ActionBar actionBar = getSupportActionBar();
                actionBar.hide();
                mAuth = FirebaseAuth.getInstance();//Get an instance of the firebase
                //Link up the layout items
                //btn_register = (Button)findViewById(R.id.btn_register);//Used to create a user
                text_view_name = findViewById(R.id.logo_text);
                attempts_remaining = (TextView)findViewById(R.id.attempts_remaining);
                version_number_text = (TextView)findViewById(R.id.version_number);
                Offline_login = (TextView)findViewById(R.id.Offline_login);
                btn_login = (Button)findViewById(R.id.btn_login);
                Email_input = findViewById(R.id.Email_input);//Edit texts
                Password_input = findViewById(R.id.Password_input);//Edit texts
                number_of_attempts_remaining = Counter_max;

                String version_number = getString(R.string.Version_Number_string);
                version_number_text.setText(version_number);
                //Global variables setup
                BLE_DB = new DatabaseHelper(this);
                calibration = new Calibration();//Used to perform the calibration
                FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);//For seeing debug information in the logcat
                //Offline login
                Offline_login.setTextColor(Color.RED);
                //Onclick listeners
                btn_login.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                if(Login_counter < Counter_max){
                        if(Toggle_offine == false) {
                                login(Email_input.getEditText().getText().toString().trim(), Password_input.getEditText().getText().toString().trim());//Attempt to login with the data from the interface
                        }
                        else
                        {
                                Offline_login(Email_input.getEditText().getText().toString().trim(), Password_input.getEditText().getText().toString().trim());//Attempt to login with the data from the interface
                        }
                }
                if(Login_counter >= Counter_max){//Stop the user logging in to the system if the maximum number of attempts have been reached
                Log.d(TAG, "Maximum number of attempts used");
                }
                }
                });
                }
        private boolean Input_check(String email, String password){//Used to check whether the inputs are null, if so prompt the user to input data into both fields
                //Could have some added input checking as well
                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                        Log.d(TAG, "Input_check: Email or password is empty");
                        return false;
                }
                else
                {
                        return true;
                }
        }
        private void Offline_login(String email, String password){
                if(Input_check(email,password)==true) {//Used to check input is not null
                        if((email.equals(login_email_default)& password.equals(login_password_default)))
                        {
                                logged_in_user_string = email;//Store the logged in user's email address to be displayed
                                Utils.toast(getApplicationContext(), "Login successful");//Pass information to the display
                                Intent myIntent = new Intent(Loginactivity.this, Home_activity.class);
                                //Bundle is used for passing data between activities
                                Bundle bundle = new Bundle();
                                bundle.putString("User_data", logged_in_user_string);//Pass through the user's email address to the main activity for display
                                Pair[] pairs = new Pair[1];
                                pairs[0] = new Pair<View,String>(text_view_name,"image_text");

                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Loginactivity.this, pairs);
                                myIntent.putExtras(bundle);
                                Loginactivity.this.startActivity(myIntent,options.toBundle());
                                finish();
                        }
                        else
                        {
                                // If sign in fails, display a message to the user.
                                Utils.toast(getApplicationContext(), "Incorrect Email or Password");//Needs to be formatted like this due to best practice
                                Login_counter = Login_counter + 1;//Increment login counter
                                number_of_attempts_remaining = number_of_attempts_remaining - 1;//Decrement the login counter
                                attempts_remaining.setText("Attempts remaining: " + number_of_attempts_remaining);//Update the user  interface
                        }
                }
        }
        private void login(String email, String password) {
                if(Input_check(email,password)==true) {//Used to check input is not null
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {//This is the
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) //Logged in successfully to firebase
                        {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();//Identify who is logged in
                                logged_in_user_string = mAuth.getCurrentUser().getEmail();//Store the logged in user's email address to be displayed
                                Utils.toast(getApplicationContext(), "Login successful");//Pass information to the display
                                Intent myIntent = new Intent(Loginactivity.this, Home_activity.class);

                                //Bundle is used for passing data between activities
                                Bundle bundle = new Bundle();
                                bundle.putString("User_data",logged_in_user_string);//Pass through the user's email address to the main activity for display
                                Pair[] pairs = new Pair[1];
                                pairs[0] = new Pair<View,String>(text_view_name,"image_text");

                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Loginactivity.this, pairs);
                                myIntent.putExtras(bundle);
                                Loginactivity.this.startActivity(myIntent,options.toBundle());
                                finish();

                        }
                        else
                        {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Log.d(TAG, "Login failed");
                                Utils.toast(getApplicationContext(), "Incorrect Email or Password");//Needs to be formatted like this due to best practice
                                Login_counter = Login_counter + 1;//Increment login counter
                                number_of_attempts_remaining = number_of_attempts_remaining - 1;//Decrement the login counter
                                attempts_remaining.setText("Attempts remaining: " + number_of_attempts_remaining);//Update the user  interface
                        }
                }
                });
                }else{//Operates like a catch statement
                        Log.d(TAG, "Incorrect Email or Password");
                        Utils.toast(getApplicationContext(),"Incorrect Email or Password");
                }
        }
        public void Offline_login_click(View view) {
                if(Toggle_offine == false) {
                        Offline_login.setTextColor(Color.GREEN);
                        Toggle_offine = true;
                }else
                {
                        Offline_login.setTextColor(Color.RED);
                        Toggle_offine = false;
                }
                Utils.toast(getApplicationContext(),"Offline button clicked");

        }


}
