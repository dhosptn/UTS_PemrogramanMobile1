package com.example.printease;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.printease.databinding.ItemOrderHistoryBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {
    private List<OrderHistory> orderHistories;
    private DatabaseReference ordersRef;

    public OrderHistoryAdapter(List<OrderHistory> orderHistories) {
        this.orderHistories = orderHistories;
        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("orders");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderHistoryBinding binding = ItemOrderHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderHistory order = orderHistories.get(position);

        // Set order details to the views
        holder.binding.tvOrderNumber.setText("Order #" + order.getOrderNumber());
        holder.binding.tvName.setText("Nama: " + order.getName());
        holder.binding.tvFileName.setText("File: " + order.getFileName());
        holder.binding.tvPages.setText("Halaman: " + order.getPages());
        holder.binding.tvPrintType.setText("Jenis: " + order.getPrintType());
        holder.binding.tvSize.setText("Ukuran: " + order.getSize());
        holder.binding.tvCopies.setText("Jumlah: " + order.getCopies());
        holder.binding.tvPayment.setText("Pembayaran: " + order.getPayment());
        holder.binding.tvPrice.setText(order.getPrice());

        // Check and update estimated time and status
        updateEstimatedTime(holder, order);
        holder.binding.tvStatus.setText("Status: " + order.getStatus());

        // Save/Update order in Firebase
        saveOrderToFirebase(order);

        // Handle button click to update order status
        holder.binding.btnSelesai.setOnClickListener(v -> {
            if (order.getStatus().equals("Dalam Proses")) {
                order.setStatus("Selesai");
                order.setCompleted(true);

                // Update Firebase when status changes
                updateOrderStatusInFirebase(order);

                // Update UI
                holder.binding.btnSelesai.setBackgroundColor(holder.itemView.getContext()
                        .getResources().getColor(android.R.color.holo_green_light));
                holder.binding.btnSelesai.setText("Pesanan Selesai");
                notifyItemChanged(position);
            }
        });

        // Change button color if already completed
        if (order.isCompleted()) {
            holder.binding.btnSelesai.setBackgroundColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_green_light));
            holder.binding.btnSelesai.setText("Pesanan Selesai");
            holder.binding.btnSelesai.setEnabled(false); // Disable button to prevent further clicks
        }
    }

    @Override
    public int getItemCount() {
        return orderHistories.size();
    }

    public void updateData(List<OrderHistory> newOrders) {
        this.orderHistories = newOrders;
        notifyDataSetChanged();
    }

    // Method to update estimated time and status
    private void updateEstimatedTime(ViewHolder holder, OrderHistory order) {
        String estimatedTime = order.getEstimatedTime();
        if (estimatedTime != null && !estimatedTime.isEmpty()) {
            if (hasTimePassed(estimatedTime)) {
                order.setStatus("Pesanan Sudah Siap");
                holder.binding.tvEstimatedTime.setText("");
            } else {
                holder.binding.tvEstimatedTime.setText(estimatedTime);
            }
        } else {
            holder.binding.tvEstimatedTime.setText("");
        }
    }

    // Helper method to check if the estimated time has passed
    private boolean hasTimePassed(String estimatedTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date estimatedDate = sdf.parse(estimatedTime);
            return estimatedDate != null && estimatedDate.before(new Date());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Save order to Firebase
    private void saveOrderToFirebase(OrderHistory order) {
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("orderNumber", order.getOrderNumber());
        orderMap.put("name", order.getName());
        orderMap.put("fileName", order.getFileName());
        orderMap.put("pages", order.getPages());
        orderMap.put("printType", order.getPrintType());
        orderMap.put("size", order.getSize());
        orderMap.put("copies", order.getCopies());
        orderMap.put("payment", order.getPayment());
        orderMap.put("price", order.getPrice());
        orderMap.put("status", order.getStatus());
        orderMap.put("estimatedTime", order.getEstimatedTime());
        orderMap.put("completed", order.isCompleted());
        orderMap.put("timestamp", ServerValue.TIMESTAMP);

        ordersRef.child(order.getOrderNumber()).setValue(orderMap);
    }

    // Update order status in Firebase
    private void updateOrderStatusInFirebase(OrderHistory order) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", order.getStatus());
        updates.put("completed", order.isCompleted());

        ordersRef.child(order.getOrderNumber()).updateChildren(updates);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemOrderHistoryBinding binding;

        ViewHolder(ItemOrderHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}