package com.example.campusexpensemanager;

public class CategoryExpense {
    private String category;
    private double amount;
    private double percentage;
    private int color;

    public CategoryExpense(String category, double amount, double percentage, int color) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
        this.color = color;
    }

    // Getters
    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public int getColor() {
        return color;
    }

    // Setters
    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public void setColor(int color) {
        this.color = color;
    }
}