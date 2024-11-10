package com.example.printease;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.printease.databinding.ActivityHistoryBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private ActivityHistoryBinding binding;
    private OrderHistoryAdapter adapter;
    private Handler refreshHandler;
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize RecyclerView with updated order history
        List<OrderHistory> orderHistories = loadAndUpdateOrderHistory();
        adapter = new OrderHistoryAdapter(orderHistories);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);

        // Back button functionality
        binding.btnBack.setOnClickListener(v -> finish());

        // Setup periodic refresh
        setupRefresh();
    }

    private void setupRefresh() {
        refreshHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Reload and update the order history
                List<OrderHistory> updatedOrders = loadAndUpdateOrderHistory();
                adapter.updateData(updatedOrders);

                // Schedule next refresh
                refreshHandler.postDelayed(this, 30000); // Refresh every 30 seconds
            }
        };

        // Start the refresh cycle
        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the data when activity becomes visible
        List<OrderHistory> updatedOrders = loadAndUpdateOrderHistory();
        adapter.updateData(updatedOrders);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the refresh cycle when the activity is destroyed
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    // Method to load order history from SharedPreferences and update statuses
    private List<OrderHistory> loadAndUpdateOrderHistory() {
        SharedPreferences prefs = getSharedPreferences("OrderHistory", MODE_PRIVATE);
        String json = prefs.getString("orders", "");

        if (json.isEmpty()) {
            return new ArrayList<>(); // Return empty list if no data
        }

        Type type = new TypeToken<List<OrderHistory>>() {}.getType();
        List<OrderHistory> orderHistories = new Gson().fromJson(json, type);

        // Check each order's estimated time and update status if needed
        boolean isUpdated = false;
        for (OrderHistory order : orderHistories) {
            if (order.getEstimatedTime() != null && hasTimePassed(order.getEstimatedTime())) {
                order.setStatus("Pesanan Sudah Siap");
                order.setEstimatedTime("");  // Clear the estimated time if time has passed
                isUpdated = true;  // Mark that updates were made
            }
        }

        // Save updated order history back to SharedPreferences if changes were made
        if (isUpdated) {
            saveOrderHistory(orderHistories);
        }

        return orderHistories;
    }

    // Helper method to check if the estimated time has passed
    private boolean hasTimePassed(String estimatedTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date estimatedDate = sdf.parse(estimatedTime);
            return estimatedDate != null && estimatedDate.before(new Date());
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false if there was an error parsing
        }
    }

    // Method to save updated order history back to SharedPreferences
    private void saveOrderHistory(List<OrderHistory> orderHistories) {
        SharedPreferences prefs = getSharedPreferences("OrderHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(orderHistories);
        editor.putString("orders", json);
        editor.apply();  // Apply changes asynchronously
    }
}