package com.mythcon.savr.ngelih.Databases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mythcon.savr.ngelih.Model.Order;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SAVR on 26/02/2018.
 * Digunakan untuk menyimpan Order di dalam database local dengan SQLite Database
 * Selanjutnya di PUSH ke Firebase Database Pada Class Cart
 */

public class Database extends SQLiteAssetHelper {
    private static final String DB_Name = "NgelihDB.db";
    private static final int DB_VER = 1;
    public Database(Context context) {
        super(context, DB_Name,null, DB_VER);
    }

    public List<Order> getCarts(){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String[] sqlSelect = {"ProductName","ProductId","Quantity","Price","Discount"};
        String SqlTable = "OrderDetail";

        queryBuilder.setTables(SqlTable);
        Cursor c = queryBuilder.query(db,sqlSelect,null,null,null,null,null);

        final List<Order> result = new ArrayList<>();
        if (c.moveToFirst()){
            do {
                result.add(new Order(c.getString(c.getColumnIndex("ProductId"))
                        ,c.getString(c.getColumnIndex("ProductName"))
                        ,c.getString(c.getColumnIndex("Quantity"))
                        ,c.getString(c.getColumnIndex("Price"))
                        ,c.getString(c.getColumnIndex("Discount")))
                );
            }while (c.moveToNext());
        }
        return result;
    }

    public void addCart (Order order){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO OrderDetail(ProductId,ProductName,Quantity,Price,Discount) VALUES ('%s','%s','%s','%s','%s');",
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount());
        db.execSQL(query);
    }

    public void cleanCart (){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail");
        db.execSQL(query);
    }

    public void addFavorites(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO Favorites(FoodId) VALUES ('%s');",foodId);
        db.execSQL(query);
    }

    public void removeFavorites(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM Favorites WHERE FoodId='%s';",foodId);
        db.execSQL(query);
    }

    public boolean isFavorites(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorites WHERE FoodId='%s';",foodId);
        Cursor cursor = db.rawQuery(query,null);
        if (cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
}
