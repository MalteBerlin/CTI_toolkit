package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Splash_screen_activity extends AppCompatActivity {


    //Variables for the splash screen
    Animation topAnim, bottomAnim;
    ImageView image;
    TextView text_view_name;

    private static int SPLASH_SCREEN = 3000;//5 Seconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        //Animations
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        image = findViewById(R.id.imageView);
        text_view_name = findViewById(R.id.text_view_name);

        image.setAnimation(topAnim);
        text_view_name.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Login();
            }
        },SPLASH_SCREEN);
    }
    private void Login()
    {
        Intent myIntent = new Intent(Splash_screen_activity.this, Loginactivity.class);

        Pair[] pairs = new Pair[2];
        pairs[0] = new Pair<View,String>(image,"logo_image");
        pairs[1] = new Pair<View,String>(text_view_name,"image_text");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Splash_screen_activity.this, pairs);

        Splash_screen_activity.this.startActivity(myIntent,options.toBundle());
        finish();
    }
}
