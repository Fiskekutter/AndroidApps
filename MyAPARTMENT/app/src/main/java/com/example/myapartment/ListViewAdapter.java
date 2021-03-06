package com.example.myapartment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    public Context context;
    public ArrayList<Stock> stocks;
    public Stock stock;

    public ListViewAdapter(Context c, ArrayList<Stock> stockList){
        this.context = c;
        this.stocks = stockList;
    }

    @Override
    public int getCount(){
        if(stocks != null){
            return stocks.size();
        } else{
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (stocks != null) {
            return stocks.get(position);
        } else {
            return null;
        }
    }


    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.stock_list_text_view,null);
        }

        stock = stocks.get(position);
        if(stock != null){

            TextView StockName = convertView.findViewById(R.id.listStockName);
            StockName.setText(stock.getCompanyName());

            TextView StockPrice = convertView.findViewById(R.id.listStockPrice);
            StockPrice.setText(String.valueOf(stock.getLatestValue()));

            if(stock.getPrice() != 0) {
                stock.setValueChange((stock.getLatestValue() / stock.getPrice())*100 - 100);
            } else{
                stock.setValueChange(0.0);
            }
            TextView StockChange = convertView.findViewById(R.id.listStockChange);
            StockChange.setText(String.format("%.2f" , stock.getValueChange())+"%");
            if(stock.getValueChange() > 0){
                StockChange.setTextColor(Color.GREEN);
            } else if (stock.getValueChange() < 0){
                StockChange.setTextColor(Color.RED);
            }
        }
        return convertView;
    }

}
