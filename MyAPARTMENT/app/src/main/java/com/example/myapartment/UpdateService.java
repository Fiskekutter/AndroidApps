package com.example.myapartment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

public class UpdateService extends Service {

    private boolean started = false;
    public boolean dbStarted = false;

    private int channelId = 1;

    private List<Stock> stocks;
    Stock stock;
    StockRoom stockRoom;
    StockDAO DAO;

    String NotificationTimestamp;
    NotificationCompat.Builder NotificationBuilder;
    NotificationManagerCompat notificationManager;

    String NotificationChannelName = "Stock Channel";
    String NotificationChannelDescrip = "Stockapp";

    int WaitTwoMinutes = 1000*60*2;

    String NotificationContentTitle = "Last checked stock prices at";

    private final IBinder serviceBinder = new ServiceBinder();
    RequestQueue queue;

    Context context;

    //Class that allows binding to service
    public class ServiceBinder extends Binder {
        UpdateService getService(){
            return UpdateService.this;
        }
    }


    //Reads Stocks from DB
    public List<Stock> getStocks(){
        stocks = DAO.readAll();
        return stocks;
    }

    public void deleteStock(Stock s) {
        DAO.deleteStock(s);
    }

    //Refreshes the stocks in the database to the newest value from API
    public void refreshStocks(){
        stocks = DAO.readAll();
        for(int i = 0; i < stocks.size(); i++) {
            stock = stocks.get(i);
            apiRequestUpdate(stock.getSymbol());
        }
    }

    //Returns a stock with a specific ID from DB
    public Stock getStockById(int id) {
        return DAO.readStockById(id);
    }

    //Updates a stock in the DB: But not from API
    public void updateStock(Stock s){
        DAO.updateStock(s);
    }

    //Initiates the Database/room
    private void initStockRoom(String roomName){ //Not best practice to use mainThreadQueries but creates a better flow in the app
        stockRoom = Room.databaseBuilder(getApplicationContext(), StockRoom.class, roomName).allowMainThreadQueries().build();
        DAO = stockRoom.stockDAO();
        Log.d(SharedVariables.ServiceTag, "Initiating DB");
        if(DAO.readAll().isEmpty()) {
            insertTenStocks();
        }
        broadcastTaskResult(SharedVariables.BroadcastDBMessage);
    }

    private void insertTenStocks(){     //Prepopulates 10 stocks in database
        String[] Syms = {"FB", "TSLA", "AAPL", "ATVI", "AMZN", "EBAY", "NVDA", "INTC", "HAS", "E"};
        int[] Price = {112, 113, 114, 115, 116, 117, 118, 119, 120, 121};
        String NumOfStocks = "1";

        for(int i = 0; i < Syms.length; i++){
            apiRequestInsert(Syms[i], Price[i], NumOfStocks);
        }
    }

    private void createNotificationChannel(){ //Sets notifcation channel: Required in API26 and above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Inspired by android guide for notifications
            CharSequence name = NotificationChannelName;
            String description = NotificationChannelDescrip;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(""+channelId, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            try{
                notificationManager.createNotificationChannel(channel);
            } catch(NullPointerException e){
                e.printStackTrace();
                Log.d(SharedVariables.ServiceTag, "Notification Nullpointer exception");
            }
        }
    }

    public UpdateService(){
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(SharedVariables.ServiceTag, "Service Created");
        context = this;
        //initStockRoom(SharedVariables.DatabaseName);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(SharedVariables.ServiceTag, "Service Started");

        createNotificationChannel();
        CreateNotification();



        if(!started) {
            CreateDB();
            started = true;
            ApiTask(WaitTwoMinutes);
        } else{
            Log.d(SharedVariables.ServiceTag, "Service already started");
        }

        queue = Volley.newRequestQueue(this);
        //queue.addRequestFinishedListener(RequestListener());

        return START_STICKY;
    }


    private void CreateNotification() { //Modified from android guide
        NotificationTimestamp = Calendar.getInstance().getTime().toString();
        NotificationBuilder = new NotificationCompat.Builder(this,""+channelId)
                .setSmallIcon(R.mipmap.stock_app)
                .setContentTitle(NotificationContentTitle)
                .setContentText(NotificationTimestamp)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(channelId, NotificationBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent){
        return serviceBinder;
    }

    private void ApiTask(final long waitTimeMillis) { //Updates stocks every 2 minutes
        AsyncTask<Object, Object, String> task = new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object... objects) {
                String s = "Service Done";
                Log.d(SharedVariables.ServiceTag, "Async doInBackground");
                try {
                    if(!dbStarted) {
                        Log.d(SharedVariables.ServiceTag, "AfterDBStarted");
                        stocks = DAO.readAll();
                        for(int i = 0; i < stocks.size(); i++) {
                            stock = stocks.get(i);
                            apiRequestUpdate(stock.getSymbol());
                        }
                        Log.d(SharedVariables.ServiceTag, stock.getCompanyName()+"Outside");
                    }
                    Thread.sleep(waitTimeMillis);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d(SharedVariables.ServiceTag, "Error in APIREQUEST");
                }
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.d(SharedVariables.ServiceTag, "Async PostExecute");
                broadcastTaskResult(s);
                broadcastTaskResultForDetail(s);
                UpdateNotification();


                if(started){
                    ApiTask(WaitTwoMinutes);
                }
            }
        };
        task.execute();
    }

    private void CreateDB() { //Creates/initializes database
        AsyncTask<Object, Object, String> task = new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object... objects) {
                String s = "Service Done";
                Log.d(SharedVariables.ServiceTag, "Async doInBackground DB");
                try {
                    initStockRoom(SharedVariables.DatabaseName);
                } catch(Exception e){
                    e.printStackTrace();
                }
                    return s;
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.d(SharedVariables.ServiceTag, "Async PostExecute DB");
                dbStarted = true;
            }
        };
        task.execute();
    }

    private void UpdateNotification() { //Updates notification
        NotificationTimestamp = Calendar.getInstance().getTime().toString();
        NotificationBuilder.setContentText(NotificationTimestamp);
        notificationManager.notify(channelId, NotificationBuilder.build());
        Log.d(SharedVariables.ServiceTag, "Notification updated");
    }

    private void broadcastTaskResult(String result) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SharedVariables.BroadcastAction);
        broadcastIntent.putExtra(SharedVariables.BroadcastResult, result);
        Log.d(SharedVariables.ServiceTag, "BROADCASTING:"+result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        UpdateNotification();
    }

    private void broadcastTaskResultForDetail(String result) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SharedVariables.BroadcastAction_Detail);
        broadcastIntent.putExtra(SharedVariables.BroadcastResult, result);
        Log.d(SharedVariables.ServiceTag, "BROADCASTING:"+result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        UpdateNotification();
    }

    public void apiRequestInsert(final String symbol, final double price, final String numOfStocks) {//Retrieves info about stocks and Inserts Stocks into db
        String SingleURL = "https://api.iextrading.com/1.0/stock/market/batch?symbols="+symbol+"&types=company,price";

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, SingleURL, (JSONObject) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(SharedVariables.ServiceTag, "" + response);

                    JSONObject responseJSONObject = response.getJSONObject(symbol);
                    JSONObject NextJson = responseJSONObject.getJSONObject("company");
                    Stock stock = new Stock(symbol,
                                            price,
                                            numOfStocks,
                                            NextJson.getString("companyName"),
                                            responseJSONObject.getDouble("price"),
                                            NextJson.getString("sector"),
                                            NextJson.getString("exchange")
                                            );
                    DAO.insertStocks(stock);
                    broadcastTaskResult("InsertStock");
                    Log.d(SharedVariables.ServiceTag, "");
                }catch (JSONException e){
                    e.printStackTrace();
                    Log.d(SharedVariables.ServiceTag, "APIERROR");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(SharedVariables.ServiceTag, "Request error");
            }
        });

        queue.add(jsonObjectRequest);
    }

    public void apiRequestUpdate(final String symbol) {
        String URLJson = "https://api.iextrading.com/1.0/stock/market/batch?symbols="+symbol+"&types=price";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLJson, (JSONObject) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject responseJSONObject = response.getJSONObject(symbol);
                    //stock.setLatestValue(responseJSONObject.getDouble("price"));
                    //stock.setLatestTimestamp(Calendar.getInstance().getTime().toString());
                    DAO.updateStockbySym(responseJSONObject.getDouble("price"),
                                        Calendar.getInstance().getTime().toString(),
                                        symbol);
                    broadcastTaskResult("Refreshed Stocks");
                }catch (JSONException e){
                    e.printStackTrace();
                    Log.d(SharedVariables.ServiceTag, "APIERROR");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(SharedVariables.ServiceTag, "Request error");
            }
        });

        queue.add(jsonObjectRequest);

    }

    @Override
    public void onDestroy(){
        Log.d(SharedVariables.ServiceTag, "Service Destroyed");
    }
}
