package com.example.printease;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderHistory {
    private String orderNumber;
    private String name;
    private String fileName;
    private String pages;
    private String printType;
    private String size;
    private String copies;
    private String payment;
    private String price;
    private String status;
    private String estimatedTime;
    private boolean isCompleted;

    // Constructor
    public OrderHistory(String orderNumber, String name, String fileName, String pages,
                        String printType, String size, String copies, String payment,
                        String price, String status, String estimatedTime) {
        this.orderNumber = orderNumber;
        this.name = name;
        this.fileName = fileName;
        this.pages = pages;
        this.printType = printType;
        this.size = size;
        this.copies = copies;
        this.payment = payment;
        this.price = price;
        this.status = status;
        this.estimatedTime = estimatedTime;
    }

    // Getters
    public String getOrderNumber() { return orderNumber; }
    public String getName() { return name; }
    public String getFileName() { return fileName; }
    public String getPages() { return pages; }
    public String getPrintType() { return printType; }
    public String getSize() { return size; }
    public String getCopies() { return copies; }
    public String getPayment() { return payment; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }
    public String getEstimatedTime() { return estimatedTime; }
    public boolean isCompleted() {
        return isCompleted;
    }

    // Setters
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public void setName(String name) { this.name = name; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setPages(String pages) { this.pages = pages; }
    public void setPrintType(String printType) { this.printType = printType; }
    public void setSize(String size) { this.size = size; }
    public void setCopies(String copies) { this.copies = copies; }
    public void setPayment(String payment) { this.payment = payment; }
    public void setPrice(String price) { this.price = price; }
    public void setStatus(String status) { this.status = status; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    // Method to check if the estimated time has passed
    public boolean isOrderReady() {
        if (estimatedTime == null || estimatedTime.isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date estimatedDate = sdf.parse(estimatedTime);
            return estimatedDate != null && estimatedDate.before(new Date());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}