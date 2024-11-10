package com.example.printease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import android.widget.Toast;

public class OrderSummaryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        String orderSummary = getIntent().getStringExtra("orderSummary");

        // Validasi jika orderSummary null
        if (orderSummary == null) {
            Toast.makeText(this, "Data pesanan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String[] details = orderSummary.split("\n");

        // Validasi panjang array details untuk menghindari ArrayIndexOutOfBoundsException
        if (details.length < 9) {
            Toast.makeText(this, "Data pesanan tidak lengkap", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupOrderDetail(R.id.nameLayout, "Nama", details[1].substring(6));
        setupOrderDetail(R.id.fileLayout, "File", details[2].substring(6));
        setupOrderDetail(R.id.pageLayout, "Halaman", details[3].substring(9));
        setupOrderDetail(R.id.printTypeLayout, "Jenis Cetakan", details[4].substring(15));
        setupOrderDetail(R.id.sizeLayout, "Ukuran", details[5].substring(8));
        setupOrderDetail(R.id.copiesLayout, "Jumlah", details[6].substring(8));
        setupOrderDetail(R.id.paymentLayout, "Pembayaran", details[7].substring(11));

        // Menampilkan harga total dengan label
        TextView totalPrice = findViewById(R.id.totalPrice);
        totalPrice.setText("" + details[8]);

        MaterialButton editButton = findViewById(R.id.editButton);
        MaterialButton confirmButton = findViewById(R.id.confirmButton);

        // Tombol edit untuk kembali ke halaman sebelumnya
        editButton.setOnClickListener(v -> finish());

        // Tombol konfirmasi untuk membuka OrderSuccessActivity
        confirmButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSummaryActivity.this, OrderSuccessActivity.class);
            intent.putExtra("orderSummary", orderSummary);
            startActivity(intent);
            finish();
        });
    }

    private void setupOrderDetail(int layoutId, String label, String value) {
        View layout = findViewById(layoutId);
        TextView labelText = layout.findViewById(R.id.labelText);
        TextView valueText = layout.findViewById(R.id.valueText);

        labelText.setText(label);
        valueText.setText(value);
    }
}
