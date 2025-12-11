package com.example.campusexpensemanager;

import java.io.Serializable;

public class Budget implements Serializable {
    private int id;
    private String category;
    private double budgetAmount;
    private String month;
    private String year;
    private String username;

    // Constructor rỗng
    public Budget() {
    }

    // Constructor đầy đủ
    public Budget(int id, String category, double budgetAmount, String month, String year, String username) {
        this.id = id;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.month = month;
        this.year = year;
        this.username = username;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public String getMonth() {
        return month;
    }

    public String getYear() {
        return year;
    }

    public String getUsername() {
        return username;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}