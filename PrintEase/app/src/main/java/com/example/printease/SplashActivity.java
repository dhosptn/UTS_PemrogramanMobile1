// SplashActivity.java
package com.example.printease;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Menampilkan Splash Screen selama 3 detik sebelum menuju ke MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Setelah 5 detik, pindah ke MainActivity
                Intent intent = new Intent(SplashActivity.this, LandingActivity.class);
                startActivity(intent);
                finish();  // Menutup SplashActivity setelah berpindah ke MainActivity
            }
        }, 2000);  // 2000ms =  detik
    }
}
