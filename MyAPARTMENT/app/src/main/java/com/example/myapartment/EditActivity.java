package com.example.myapartment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;


public class EditActivity extends AppCompatActivity {

    Context context;                          //Used for warning message
    String Warning_Message;                   //Used for warning message
    String SymNotExist;
    int durationOfToast = Toast.LENGTH_SHORT; //Length of warning message
    UpdateService BindEditService;
    boolean EditBound = false;
    boolean CalledFromDetail = false;
    Stock StockFromDetails;
    RequestQueue queue;
    EditText EditStockSymbol;
    EditText EditStockPrice;
    EditText EditStockNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        SetValuesOnEdit();
        context = this;
        Warning_Message = getString(R.string.Warning);
        SymNotExist = "Symbol does not exist";
        queue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, UpdateService.class);
        bindService(intent, EditConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(EditConnection);
        EditBound = false;
    }

    private ServiceConnection EditConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UpdateService.ServiceBinder binder = (UpdateService.ServiceBinder) service;
            BindEditService = binder.getService();
            EditBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            EditBound = false;
        }
    };

    public void PressCancel(View v){ //This function is called on click by Cancel button
        setResult(RESULT_CANCELED);
        finish();
    }
    public void PressSave(View v) { //This function is called on click by Save button
        EditStockSymbol = findViewById(R.id.StockNameView); //Find widgets in activity
        EditStockPrice = findViewById(R.id.PriceOfStockView);
        EditStockNumber = findViewById(R.id.NumOfStockView);

        if (!EditStockSymbol.getText().toString().isEmpty() //Checks if any of edit fields are empty
                && !EditStockPrice.getText().toString().isEmpty()
                && !EditStockNumber.getText().toString().isEmpty()){

            if (CalledFromDetail) { //Check if EditActivity is started by detail
                Intent PassStockInfo = new Intent(this, DetailsActivity.class);
                StockFromDetails.setPrice(Double.parseDouble(EditStockPrice.getText().toString()));
                StockFromDetails.setNumber(EditStockNumber.getText().toString());
                BindEditService.updateStock(StockFromDetails);
                PassStockInfo.putExtra(SharedVariables.MESSAGE, StockFromDetails);
                setResult(RESULT_OK, PassStockInfo);
                finish();
            } else{ //If EditActivity is started by overview
                CheckSymExist(EditStockSymbol.getText().toString().toUpperCase());
            }
        } else { //Sends warning if edit fields are empty
            Toast.makeText(context, Warning_Message, durationOfToast).show();
        }

    }
    public void SetValuesOnEdit(){ //This function sets values in activity
        Intent dataFromDetails = getIntent();

        EditStockSymbol = findViewById(R.id.StockNameView); //Find widgets in activity
        EditStockPrice = findViewById(R.id.PriceOfStockView);
        EditStockNumber = findViewById(R.id.NumOfStockView);

        StockFromDetails = (Stock) dataFromDetails.getSerializableExtra(SharedVariables.MESSAGE); //Gets data from details activity

        try {
            EditStockSymbol.setText(StockFromDetails.getSymbol()); //Sets texts in edit fields according to data from details activity
            EditStockSymbol.setEnabled(false);
            EditStockPrice.setText(String.valueOf(StockFromDetails.getPrice()));
            EditStockNumber.setText(StockFromDetails.getNumber());
            CalledFromDetail = true;
        }catch (NullPointerException n){
            n.printStackTrace();
        }

    }

    public void CheckSymExist(final String symbol) {
        String SingleURL = "https://api.iextrading.com/1.0/stock/"+symbol+"/quote";
        Log.d(SharedVariables.ServiceTag, ""+symbol);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, SingleURL, (JSONObject) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                    if (response.length() > 0) {
                        AddStock();
                    }
                    Log.d(SharedVariables.EditTag, ""+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(SharedVariables.EditTag, "Request error");
                Toast.makeText(context, SymNotExist, Toast.LENGTH_SHORT).show();//Symbol does not exist
            }
        });

        queue.add(jsonObjectRequest);
    }

    private void AddStock() {
        BindEditService.apiRequestInsert(
                EditStockSymbol.getText().toString().toUpperCase(),
                Double.parseDouble(EditStockPrice.getText().toString()),
                EditStockNumber.getText().toString()
        );
        setResult(RESULT_OK);
        finish();
    }
}

