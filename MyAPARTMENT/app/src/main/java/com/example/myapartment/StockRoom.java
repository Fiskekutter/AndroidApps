package com.example.myapartment;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Stock.class}, version = 1)
public abstract class StockRoom extends RoomDatabase {
    public abstract StockDAO stockDAO();
}
