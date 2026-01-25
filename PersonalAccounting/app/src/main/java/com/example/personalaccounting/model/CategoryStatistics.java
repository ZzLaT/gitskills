package com.example.personalaccounting.model;

public class CategoryStatistics {
    private String categoryName;
    private double amount;
    private int count;
    private double percentage;

    public CategoryStatistics(String categoryName, double amount, int count, double percentage) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.count = count;
        this.percentage = percentage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
