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
    private static final int DATABASE_VERSION = 1;

    // Table Expenses
    private static final String TABLE_EXPENSES = "expenses";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_PAYMENT_METHOD = "payment_method";
    private static final String COLUMN_USERNAME = "username";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        onCreate(db);
    }

    // Thêm chi tiêu
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

    // Lấy tất cả chi tiêu của user
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

    // Lấy một chi tiêu theo ID
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

    // Cập nhật chi tiêu
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

    // Xóa chi tiêu
    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    // Tính tổng chi tiêu theo tháng
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

    // Lấy chi tiêu theo danh mục
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
}