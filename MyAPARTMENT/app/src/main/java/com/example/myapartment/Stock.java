package com.example.myapartment;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;
import java.util.Calendar;

@Entity
public class Stock implements Serializable{
    @PrimaryKey(autoGenerate = true)
    private int sid;

    @ColumnInfo(name = "latest_timestamp")
    private String latestTimestamp;

    @ColumnInfo(name = "company_Name")
    private String companyName;

    @ColumnInfo(name = "sector")
    private String sector;

    @ColumnInfo(name = "symbol")
    private String symbol;

    @ColumnInfo(name = "primary_Exchange")
    private String primaryExchange;

    @ColumnInfo(name = "latest_Value")
    private double latestValue;

    @ColumnInfo(name = "price")
    private double price;

    @ColumnInfo(name = "number_Of_Stocks")
    private String number;

    @ColumnInfo(name = "value_change")
    private double valueChange;

    public Stock(){
        this("", 0, "", "", 0, "", "");
    }

    public Stock(String Symbol, double Price, String NumOfStock, String CompanyName, double LatestValue,
                 String Sector, String PrimaryExchange){

        symbol = Symbol;
        price = Price;
        number = NumOfStock;
        latestTimestamp = Calendar.getInstance().getTime().toString();
        companyName = CompanyName;
        latestValue = LatestValue;
        sector = Sector;
        primaryExchange = PrimaryExchange;
        valueChange = 0;

    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getLatestTimestamp() {
        return latestTimestamp;
    }

    public void setLatestTimestamp(String latestTimestamp) { this.latestTimestamp = latestTimestamp; }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrimaryExchange() {
        return primaryExchange;
    }

    public void setPrimaryExchange(String primaryExchange) { this.primaryExchange = primaryExchange; }

    public double getLatestValue() {
        return latestValue;
    }

    public void setLatestValue(double latestValue) {
        this.latestValue = latestValue;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public double getValueChange() {
        return valueChange;
    }

    public void setValueChange(Double valueChange) {
       this.valueChange = valueChange;
    }

}
