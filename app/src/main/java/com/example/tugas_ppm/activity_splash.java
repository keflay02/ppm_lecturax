package com.example.tugas_ppm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class activity_splash extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000; // 2 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logoImage);
        Animation moveUp = AnimationUtils.loadAnimation(this, R.anim.logo_move_up);
        logo.startAnimation(moveUp);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(activity_splash.this, activity_login.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME);
    }
}
