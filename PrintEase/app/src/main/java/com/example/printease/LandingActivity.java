package com.example.printease;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Tambahkan ini
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {
    private static final String TAG = "LandingActivity"; // Tambahkan tag untuk logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        showWarningDialog();
        setupClickListeners();
    }

    private void setupClickListeners() {
        Button orderButton = findViewById(R.id.btnOrder);
        if (orderButton != null) {
            orderButton.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(LandingActivity.this, OrderActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting OrderActivity: " + e.getMessage());
                }
            });
        } else {
            Log.e(TAG, "Order button is null");
        }

        // Bottom Navigation
        findViewById(R.id.menuOrder).setOnClickListener(v -> {
            try {
                Intent intent = new Intent(LandingActivity.this, OrderActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting OrderActivity: " + e.getMessage());
            }
        });

        findViewById(R.id.menuHistory).setOnClickListener(v -> {
            try {
                Intent intent = new Intent(LandingActivity.this, HistoryActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting HistoryActivity: " + e.getMessage());
            }
        });
    }

    private void showWarningDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Peringatan")
                .setMessage("Kami tidak menerima layanan print yang bersifat data pribadi. Harap pastikan dokumen Anda tidak mengandung informasi sensitif.")
                .setPositiveButton("Saya Mengerti", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}