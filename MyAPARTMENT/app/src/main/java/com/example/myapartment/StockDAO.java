package com.example.myapartment;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface StockDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStocks(Stock... stocks);

    @Update
    void updateStock(Stock stock);

    @Delete
    void deleteStock(Stock stock);

    @Query("SELECT * FROM Stock")
    List<Stock> readAll();

    @Query("SELECT * FROM Stock WHERE symbol = :s")
    List<Stock> readStocksBySymbol(String s);

    @Query("SELECT * FROM Stock WHERE sid = :id")
    Stock readStockById(int id);

    @Query("UPDATE Stock SET latest_Value = :lV, latest_timestamp = :lT WHERE symbol = :s ")
    void updateStockbySym(double lV, String lT, String s);

}
