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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;



import java.util.ArrayList;
import java.util.List;


public class OverviewActivity extends AppCompatActivity {

    public ListViewAdapter adapter;
    public ListView listOfStocks;

    List<Stock> StocksFromDB;
    Stock stockObj;
    final ArrayList<Stock> listOfMyStocks = new ArrayList<>();

    Context context;
    int ListPosition = 0;

    UpdateService BindOverviewService;
    boolean OverviewBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) { //Is the first function called when activity starts
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        adapter = new ListViewAdapter(this, listOfMyStocks);
        listOfStocks = findViewById(R.id.stocks_listView);
        listOfStocks.setAdapter(adapter);

        listOfStocks.setOnItemClickListener(mMessageClickedHandler);

        context = this;

        Intent ServiceIntent = new Intent(this, UpdateService.class);
        startService(ServiceIntent);

    }

    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView parent, View v, int position, long id){
            ListPosition = position;
            stockObj = StocksFromDB.get(position);
            Intent ItemClicked = new Intent(context, DetailsActivity.class);
            ItemClicked.putExtra(SharedVariables.MESSAGE, stockObj);
            startActivityForResult(ItemClicked, SharedVariables.detail_Activity_Request_Code);

        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SharedVariables.BroadcastAction);

        LocalBroadcastManager.getInstance(this).registerReceiver(OnServiceResult, intentFilter);

        Intent intent = new Intent(this, UpdateService.class);
        bindService(intent, OverviewServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(SharedVariables.OverviewTag, "bind and register");

        setValuesOnOverview();
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(OnServiceResult);
        unbindService(OverviewServiceConnection);
        OverviewBound = false;
        Log.d(SharedVariables.OverviewTag, "Unbind and Unregister");
    }

    private ServiceConnection OverviewServiceConnection = new ServiceConnection() { //Binder for service
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UpdateService.ServiceBinder binder = (UpdateService.ServiceBinder) service;
            BindOverviewService = binder.getService();
            OverviewBound = true;

            setValuesOnOverview();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            OverviewBound = false;
        }
    };

    public void PressPlus(View v){ //This function is called on click by Plus button
        Intent ButtonPressed = new Intent(this, EditActivity.class);
        startActivityForResult(ButtonPressed, SharedVariables.overview_Activity_Request_Code);
    }
    public void PressRefresh(View v){//This function is called on click by Refresh button
        Log.d(SharedVariables.OverviewTag, "Refresh");
        BindOverviewService.refreshStocks(); //Refreshes stock
    }

    public void setValuesOnOverview() { //This function sets value on overview activity/screen
        try {
            UpdateListView();
        }catch (NullPointerException n){
            n.printStackTrace();
            Log.d(SharedVariables.OverviewTag, "NullpointerException: SetValues");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent){
            if(requestCode == SharedVariables.overview_Activity_Request_Code) {// This function looks for a result from a finished activity with a specific request code
                if (resultCode == RESULT_OK){
                    Log.d(SharedVariables.OverviewTag, "Refresh After RESULT_OK");
                    BindOverviewService.refreshStocks();
                }
                if(resultCode == SharedVariables.RESULT_DELETED){
                    Log.d(SharedVariables.OverviewTag, "RESULT_DELETED");
                    UpdateListView();
                }
                if(resultCode == RESULT_CANCELED){
                    UpdateListView();
                }
            }
            if(requestCode == SharedVariables.detail_Activity_Request_Code)
                if(resultCode == RESULT_OK){
                }

    }

    private BroadcastReceiver OnServiceResult = new BroadcastReceiver() { //Receive broadcasts when service has updated data
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(SharedVariables.BroadcastResult);
            if (result == null){
                result = "ServiceERROR";
            }
            Log.d(SharedVariables.OverviewTag, "Broadcast Recieved from service"+result);
            //Toast.makeText(context,"Got result from service "+result, Toast.LENGTH_SHORT).show(); //For debugging

            UpdateListView(); //Update listview when a broadcast happens
        }
    };

    private void UpdateListView() {
        listOfMyStocks.clear();
        StocksFromDB = BindOverviewService.getStocks();
        listOfMyStocks.addAll(StocksFromDB);
        adapter.notifyDataSetChanged();
    }


}
