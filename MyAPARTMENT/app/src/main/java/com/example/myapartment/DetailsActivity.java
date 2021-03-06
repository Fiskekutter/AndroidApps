package com.example.myapartment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class DetailsActivity extends AppCompatActivity {

    Stock DetailStock;
    Boolean CalledFromOverview = true;

    UpdateService BindDetailService;
    boolean DetailBound = false;

    @Override
    public void onSaveInstanceState(Bundle outState) { //This function is called right before re-orientation
        super.onSaveInstanceState(outState);           //Temporarily saves an instance of the stock info
        outState.putSerializable(SharedVariables.StockInfo, DetailStock);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) { //This function is called after re-orientation
        super.onRestoreInstanceState(savedInstanceState);              //Restores the data from saved instance
        if (savedInstanceState != null && savedInstanceState.containsKey(SharedVariables.StockInfo)) {
                DetailStock = (Stock) savedInstanceState.getSerializable(SharedVariables.StockInfo);
                CalledFromOverview = false;
                SetValuesOnDetails();
            }
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) { //This function is called when activity starts
        super.onCreate(savedInstanceState);              //And on re-orientation
        setContentView(R.layout.activity_details);

        Intent dataFromOverview = getIntent();
        if(CalledFromOverview) {
            DetailStock = (Stock) dataFromOverview.getSerializableExtra(SharedVariables.MESSAGE);
        }

        SetValuesOnDetails();

    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SharedVariables.BroadcastAction_Detail);
        LocalBroadcastManager.getInstance(this).registerReceiver(OnServiceResult, intentFilter);

        Intent intent = new Intent(this, UpdateService.class);
        bindService(intent, DetailConnection, Context.BIND_AUTO_CREATE);
        Log.d(SharedVariables.DetailTag, "bind and register");
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(OnServiceResult);

        unbindService(DetailConnection);
        DetailBound = false;
        Log.d(SharedVariables.DetailTag, "Unbind and Unregister");
    }

    private ServiceConnection DetailConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UpdateService.ServiceBinder binder = (UpdateService.ServiceBinder) service;
            BindDetailService = binder.getService();
            DetailBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DetailBound = false;
        }
    };

    private BroadcastReceiver OnServiceResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {//Receive broadcasts when service has updated data
            String result = intent.getStringExtra(SharedVariables.BroadcastResult);
            if (result == null){
                result = "ServiceERROR";
            }
            Log.d(SharedVariables.OverviewTag, "Broadcast Recieved from service"+result);
            //Toast.makeText(getApplicationContext(),"Got result from service "+result, Toast.LENGTH_SHORT).show(); For debugging

            DetailStock = BindDetailService.getStockById(DetailStock.getSid());

            TextView DetailStockLatestValue = findViewById(R.id.DetailStockLatestValue);
            TextView DetailStockLatestTimestamp = findViewById(R.id.DetailStockTimestamp);

            DetailStockLatestValue.setText(String.valueOf(DetailStock.getLatestValue()));
            DetailStockLatestTimestamp.setText(DetailStock.getLatestTimestamp());
        }
    };

    public void SetValuesOnDetails(){ //This function sets the stock values on details activity/screen
        TextView DetailStockName = findViewById(R.id.DetailStockName); //Find widgets in activity
        TextView DetailStockPrice = findViewById(R.id.DetailStockPrice);
        TextView DetailStockNum = findViewById(R.id.DetailStockNum);
        TextView DetailStockSector = findViewById(R.id.DetailStockSector);
        TextView DetailStockLatestValue = findViewById(R.id.DetailStockLatestValue);
        TextView DetailStockLatestTimestamp = findViewById(R.id.DetailStockTimestamp);

        DetailStockName.setText(DetailStock.getCompanyName()); //Set widgets according to our data
        DetailStockPrice.setText(String.valueOf(DetailStock.getPrice()));
        DetailStockNum.setText(DetailStock.getNumber());
        DetailStockSector.setText(DetailStock.getSector());
        DetailStockLatestValue.setText(String.valueOf(DetailStock.getLatestValue()));
        DetailStockLatestTimestamp.setText(DetailStock.getLatestTimestamp());

    }

    public void PressEdit(View v){ //This function is called on click by Edit button
        Intent EditPressed = new Intent(this, EditActivity.class);
        EditPressed.putExtra(SharedVariables.MESSAGE, DetailStock); //Data to edit activity
        startActivityForResult(EditPressed, SharedVariables.overview_Activity_Request_Code);
    }

    public void PressBack(View v){ //This function is called on click by Back button
        setResult(RESULT_CANCELED);
        finish();
    }

    public void pressDelete(View v) //This function is called on click by Back button
    {
        BindDetailService.deleteStock(DetailStock);
        setResult(SharedVariables.RESULT_DELETED);
        Log.d(SharedVariables.DetailTag, "DeletePressed");
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // This function looks for a result from a finished activity with a specific request code
        if(requestCode == SharedVariables.overview_Activity_Request_Code)
        {
            if(resultCode == RESULT_OK) {
                Log.d(SharedVariables.DetailTag, "RESULT_OK");
                DetailStock = (Stock) data.getSerializableExtra(SharedVariables.MESSAGE);

                TextView DetailStockPrice = findViewById(R.id.DetailStockPrice);
                TextView DetailStockNum = findViewById(R.id.DetailStockNum);

                DetailStockNum.setText(DetailStock.getNumber());
                DetailStockPrice.setText(String.valueOf(DetailStock.getPrice()));
            }
        }
    }
}
