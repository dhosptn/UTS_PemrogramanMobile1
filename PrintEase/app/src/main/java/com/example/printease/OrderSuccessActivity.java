package com.example.printease;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.printease.databinding.ActivityOrderSuccessBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class OrderSuccessActivity extends AppCompatActivity {
    private ActivityOrderSuccessBinding binding;
    private String orderNumber;
    private Handler statusUpdateHandler;
    private Runnable statusUpdateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String orderSummary = getIntent().getStringExtra("orderSummary");
        String[] details = orderSummary.split("\n");

        orderNumber = generateOrderNumber();
        binding.tvOrderNumber.setText("Nomor Pesanan: #" + orderNumber);

        Calendar estimatedTime = Calendar.getInstance();
        estimatedTime.add(Calendar.HOUR, 1);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("id"));
        String estimatedTimeStr = "Estimasi Selesai: " + timeFormat.format(estimatedTime.getTime());
        binding.tvEstimatedTime.setText(estimatedTimeStr);

        // Create and save order history
        OrderHistory newOrder = new OrderHistory(
                orderNumber,
                details[1].substring(6),
                details[2].substring(6),
                details[3].substring(9),
                details[4].substring(15),
                details[5].substring(8),
                details[6].substring(8),
                details[7].substring(11),
                details[8],
                "Dalam Proses",
                estimatedTimeStr
        );

        saveOrderHistory(newOrder);

        // Setup order details display
        setupOrderDetail(R.id.nameLayout, "Nama", details[1].substring(6));
        setupOrderDetail(R.id.fileLayout, "File", details[2].substring(6));
        setupOrderDetail(R.id.pageLayout, "Halaman", details[3].substring(9));
        setupOrderDetail(R.id.printTypeLayout, "Jenis Cetakan", details[4].substring(15));
        setupOrderDetail(R.id.sizeLayout, "Ukuran", details[5].substring(8));
        setupOrderDetail(R.id.copiesLayout, "Jumlah", details[6].substring(8));
        setupOrderDetail(R.id.paymentLayout, "Pembayaran", details[7].substring(11));

        TextView totalPrice = findViewById(R.id.totalPrice);
        totalPrice.setText(details[8]);

        binding.btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Set up status update check
        setupStatusUpdate(estimatedTime);
    }

    private void setupStatusUpdate(Calendar estimatedTime) {
        statusUpdateHandler = new Handler();
        statusUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                Calendar currentTime = Calendar.getInstance();

                if (currentTime.after(estimatedTime)) {
                    // Update status to "Pesanan Sudah Siap"
                    updateOrderStatus("Pesanan Sudah Siap");

                    // Update the UI to show the new status
                    runOnUiThread(() -> {
                        TextView statusView = findViewById(R.id.tvStatus);
                        if (statusView != null) {
                            statusView.setText("Status: Pesanan Sudah Siap");
                        }
                    });

                    // Stop checking once the status is updated
                    statusUpdateHandler.removeCallbacks(this);
                    Log.d("StatusUpdate", "Order is ready - status updated");
                } else {
                    // Continue checking every minute until the estimated time is reached
                    statusUpdateHandler.postDelayed(this, 60000);
                    Log.d("StatusUpdate", "Order still in process - checking again in 1 minute");
                }
            }
        };

        // Start the status update checks
        statusUpdateHandler.post(statusUpdateRunnable);
    }

    private void updateOrderStatus(String newStatus) {
        SharedPreferences prefs = getSharedPreferences("OrderHistory", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("orders", "");

        if (!json.isEmpty()) {
            Type type = new TypeToken<List<OrderHistory>>() {}.getType();
            List<OrderHistory> orders = gson.fromJson(json, type);

            for (OrderHistory order : orders) {
                if (order.getOrderNumber().equals(orderNumber)) {
                    order.setStatus(newStatus);
                    Log.d("UpdateStatus", "Status updated for order: " + orderNumber);
                    break;
                }
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("orders", gson.toJson(orders));
            editor.apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusUpdateHandler != null && statusUpdateRunnable != null) {
            statusUpdateHandler.removeCallbacks(statusUpdateRunnable);
        }
    }

    private String generateOrderNumber() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    private void saveOrderHistory(OrderHistory newOrder) {
        SharedPreferences prefs = getSharedPreferences("OrderHistory", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("orders", "");
        List<OrderHistory> orders;
        if (json.isEmpty()) {
            orders = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<OrderHistory>>() {}.getType();
            orders = gson.fromJson(json, type);
        }
        orders.add(newOrder);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("orders", gson.toJson(orders));
        editor.apply();
    }

    private void setupOrderDetail(int layoutId, String label, String value) {
        View layout = findViewById(layoutId);
        TextView labelText = layout.findViewById(R.id.labelText);
        TextView valueText = layout.findViewById(R.id.valueText);
        labelText.setText(label);
        valueText.setText(value);
    }
}