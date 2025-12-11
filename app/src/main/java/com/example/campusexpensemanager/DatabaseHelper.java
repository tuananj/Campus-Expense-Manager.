package com.example.campusexpensemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CampusExpense.db";
    private static final int DATABASE_VERSION = 2; // Tăng từ 1 lên 2

    // ==================== EXPENSES TABLE ====================
    private static final String TABLE_EXPENSES = "expenses";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_PAYMENT_METHOD = "payment_method";
    private static final String COLUMN_USERNAME = "username";

    // ==================== BUDGETS TABLE ====================
    private static final String TABLE_BUDGETS = "budgets";
    private static final String COLUMN_BUDGET_ID = "id";
    private static final String COLUMN_BUDGET_CATEGORY = "category";
    private static final String COLUMN_BUDGET_AMOUNT = "budget_amount";
    private static final String COLUMN_BUDGET_MONTH = "month";
    private static final String COLUMN_BUDGET_YEAR = "year";
    private static final String COLUMN_BUDGET_USERNAME = "username";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Expenses
        String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_AMOUNT + " REAL,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_PAYMENT_METHOD + " TEXT,"
                + COLUMN_USERNAME + " TEXT"
                + ")";
        db.execSQL(CREATE_EXPENSES_TABLE);

        // Tạo bảng Budgets
        String CREATE_BUDGETS_TABLE = "CREATE TABLE " + TABLE_BUDGETS + "("
                + COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BUDGET_CATEGORY + " TEXT,"
                + COLUMN_BUDGET_AMOUNT + " REAL,"
                + COLUMN_BUDGET_MONTH + " TEXT,"
                + COLUMN_BUDGET_YEAR + " TEXT,"
                + COLUMN_BUDGET_USERNAME + " TEXT"
                + ")";
        db.execSQL(CREATE_BUDGETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        onCreate(db);
    }

    // ==================== EXPENSE METHODS ====================

    public long addExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, expense.getAmount());
        values.put(COLUMN_CATEGORY, expense.getCategory());
        values.put(COLUMN_DESCRIPTION, expense.getDescription());
        values.put(COLUMN_DATE, expense.getDate());
        values.put(COLUMN_PAYMENT_METHOD, expense.getPaymentMethod());
        values.put(COLUMN_USERNAME, expense.getUsername());

        long id = db.insert(TABLE_EXPENSES, null, values);
        db.close();
        return id;
    }

    public List<Expense> getAllExpenses(String username) {
        List<Expense> expenseList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EXPENSES +
                " WHERE " + COLUMN_USERNAME + " = ? ORDER BY " + COLUMN_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{username});

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(cursor.getInt(0));
                expense.setAmount(cursor.getDouble(1));
                expense.setCategory(cursor.getString(2));
                expense.setDescription(cursor.getString(3));
                expense.setDate(cursor.getString(4));
                expense.setPaymentMethod(cursor.getString(5));
                expense.setUsername(cursor.getString(6));
                expenseList.add(expense);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return expenseList;
    }

    public Expense getExpense(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSES, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Expense expense = null;
        if (cursor != null && cursor.moveToFirst()) {
            expense = new Expense();
            expense.setId(cursor.getInt(0));
            expense.setAmount(cursor.getDouble(1));
            expense.setCategory(cursor.getString(2));
            expense.setDescription(cursor.getString(3));
            expense.setDate(cursor.getString(4));
            expense.setPaymentMethod(cursor.getString(5));
            expense.setUsername(cursor.getString(6));
            cursor.close();
        }

        db.close();
        return expense;
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, expense.getAmount());
        values.put(COLUMN_CATEGORY, expense.getCategory());
        values.put(COLUMN_DESCRIPTION, expense.getDescription());
        values.put(COLUMN_DATE, expense.getDate());
        values.put(COLUMN_PAYMENT_METHOD, expense.getPaymentMethod());

        int result = db.update(TABLE_EXPENSES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(expense.getId())});
        db.close();
        return result;
    }

    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public double getTotalExpenseByMonth(String username, String month, String year) {
        double total = 0;
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSES +
                " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_DATE + " LIKE ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{username, "%/" + month + "/" + year});

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return total;
    }

    public List<Expense> getExpensesByCategory(String username, String category) {
        List<Expense> expenseList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EXPENSES +
                " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_CATEGORY + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{username, category});

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(cursor.getInt(0));
                expense.setAmount(cursor.getDouble(1));
                expense.setCategory(cursor.getString(2));
                expense.setDescription(cursor.getString(3));
                expense.setDate(cursor.getString(4));
                expense.setPaymentMethod(cursor.getString(5));
                expense.setUsername(cursor.getString(6));
                expenseList.add(expense);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return expenseList;
    }

    // ==================== BUDGET METHODS ====================

    public long addBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BUDGET_CATEGORY, budget.getCategory());
        values.put(COLUMN_BUDGET_AMOUNT, budget.getBudgetAmount());
        values.put(COLUMN_BUDGET_MONTH, budget.getMonth());
        values.put(COLUMN_BUDGET_YEAR, budget.getYear());
        values.put(COLUMN_BUDGET_USERNAME, budget.getUsername());

        long id = db.insert(TABLE_BUDGETS, null, values);
        db.close();
        return id;
    }

    public List<Budget> getAllBudgets(String username) {
        List<Budget> budgetList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BUDGETS +
                " WHERE " + COLUMN_BUDGET_USERNAME + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{username});

        if (cursor.moveToFirst()) {
            do {
                Budget budget = new Budget();
                budget.setId(cursor.getInt(0));
                budget.setCategory(cursor.getString(1));
                budget.setBudgetAmount(cursor.getDouble(2));
                budget.setMonth(cursor.getString(3));
                budget.setYear(cursor.getString(4));
                budget.setUsername(cursor.getString(5));
                budgetList.add(budget);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return budgetList;
    }

    public Budget getBudgetByCategoryAndMonth(String username, String category, String month, String year) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_BUDGETS +
                " WHERE " + COLUMN_BUDGET_USERNAME + " = ? AND " +
                COLUMN_BUDGET_CATEGORY + " = ? AND " +
                COLUMN_BUDGET_MONTH + " = ? AND " +
                COLUMN_BUDGET_YEAR + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username, category, month, year});

        Budget budget = null;
        if (cursor != null && cursor.moveToFirst()) {
            budget = new Budget();
            budget.setId(cursor.getInt(0));
            budget.setCategory(cursor.getString(1));
            budget.setBudgetAmount(cursor.getDouble(2));
            budget.setMonth(cursor.getString(3));
            budget.setYear(cursor.getString(4));
            budget.setUsername(cursor.getString(5));
            cursor.close();
        }

        db.close();
        return budget;
    }

    public int updateBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BUDGET_CATEGORY, budget.getCategory());
        values.put(COLUMN_BUDGET_AMOUNT, budget.getBudgetAmount());
        values.put(COLUMN_BUDGET_MONTH, budget.getMonth());
        values.put(COLUMN_BUDGET_YEAR, budget.getYear());

        int result = db.update(TABLE_BUDGETS, values, COLUMN_BUDGET_ID + " = ?",
                new String[]{String.valueOf(budget.getId())});
        db.close();
        return result;
    }

    public void deleteBudget(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BUDGETS, COLUMN_BUDGET_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public double getTotalExpenseByCategoryAndMonth(String username, String category, String month, String year) {
        double total = 0;
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSES +
                " WHERE " + COLUMN_USERNAME + " = ? AND " +
                COLUMN_CATEGORY + " = ? AND " +
                COLUMN_DATE + " LIKE ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{username, category, "%/" + month + "/" + year});

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return total;
    }
}