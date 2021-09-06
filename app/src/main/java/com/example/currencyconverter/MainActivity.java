package com.example.currencyconverter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity {

    Button convert;
    Button transferSpinner;
    EditText currencyToBeConverted;
    TextView currencyConverted;
    Spinner spinnerFrom,spinnerTo;
    String stringFrom,stringTo;
    final String currencyData[] = {"USD","AED","AFN","ALL","AMD","ANG","AOA","ARS","AUD","AWG","AZN","BAM","BBD","BDT","BGN","BHD","BIF","BMD","BND","BOB","BRL","BSD","BTN","BWP","BYN","BZD","CAD","CDF","CHF","CLP","CNY","COP","CRC","CUC","CUP","CVE","CZK","DJF","DKK","DOP","DZD","EGP","ERN","ETB","EUR","FJD","FKP","FOK","GBP","GEL","GGP","GHS","GIP","GMD","GNF","GTQ","GYD","HKD","HNL","HRK","HTG","HUF","IDR","ILS","IMP","INR","IQD","IRR","ISK","JMD","JOD","JPY","KES","KGS","KHR","KID","KMF","KRW","KWD","KYD","KZT","LAK","LBP","LKR","LRD","LSL","LYD","MAD","MDL","MGA","MKD","MMK","MNT","MOP","MRU","MUR","MVR","MWK","MXN","MYR","MZN","NAD","NGN","NIO","NOK","NPR","NZD","OMR","PAB","PEN","PGK","PHP","PKR","PLN","PYG","QAR","RON","RSD","RUB","RWF","SAR","SBD","SCR","SDG","SEK","SGD","SHP","SLL","SOS","SRD","SSP","STN","SYP","SZL","THB","TJS","TMT","TND","TOP","TRY","TTD","TVD","TWD","TZS","UAH","UGX","UYU","UZS","VES","VND","VUV","WST","XAF","XCD","XDR","XOF","XPF","YER","ZAR","ZMW"};
    static HashMap<String,Double> pair = new HashMap<>();
    static ArrayAdapter<String> myadapter;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        convert = findViewById(R.id.convert);
        transferSpinner = findViewById(R.id.transferSpinner);
        currencyToBeConverted = findViewById(R.id.currencyToBeConverted);
        currencyConverted = findViewById(R.id.currencyConverted );
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        myadapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,currencyData);
        spinnerFrom.setAdapter(myadapter);
        spinnerTo.setAdapter(myadapter);
        spinnerTo.setSelection(myadapter.getPosition("INR"),false);

        stringFrom = spinnerFrom.getSelectedItem().toString();
        stringTo = spinnerTo.getSelectedItem().toString();

        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Internet not available, Cross check your internet connectivity and try again")
                .setCancelable(false)
                .setTitle("No Internet Connection")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


        if (haveInternetConnection())
            pair = vollyAPICall();
        else {
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {

                if (parentView.getSelectedItemPosition() == spinnerFrom.getSelectedItemPosition())
                    spinnerFrom.setSelection(myadapter.getPosition(stringTo));
                else
                    stringTo = parentView.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getSelectedItemPosition() == spinnerTo.getSelectedItemPosition())
                    spinnerTo.setSelection(myadapter.getPosition(stringFrom));
                else
                    stringFrom = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

        });

        currencyToBeConverted.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currencyToBeConverted.setBackgroundResource(R.drawable.custom_edittext);
                convertCurrency();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }


    public HashMap<String, Double> vollyAPICall() {

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
         JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                "https://api.exchangerate-api.com/v4/latest/USD",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject rates = response.getJSONObject("rates");
                            Iterator<String> iter = rates.keys();

                            while (iter.hasNext()) {
                                String key = iter.next();
                                pair.put(key, rates.getDouble(key));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        error.printStackTrace();

                    }
                });

        requestQueue.add(jsonObjectRequest);
        return pair;
    }

    private boolean haveInternetConnection() {

        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void convertCurrency() {

        if(currencyToBeConverted.getText().toString().equals("")) {
            Toast.makeText(this, "Enter Amount To Be Convert", Toast.LENGTH_SHORT).show();
            currencyToBeConverted.setBackgroundResource(R.drawable.custom_error);
            currencyConverted.setText("Currency");
        }else{

            double currency = Double.parseDouble(currencyToBeConverted.getText().toString());
            double fromVal = pair.get(spinnerFrom.getSelectedItem().toString());
            double toVal = pair.get(spinnerTo.getSelectedItem().toString());

            double convertedCurrency = currency * toVal / fromVal;

            currencyConverted.setText(new DecimalFormat("##.##").format(convertedCurrency));
        }
    }



    public void transferSpinner(View view) {

        int tempPosition = spinnerFrom.getSelectedItemPosition();
        spinnerFrom.setSelection(spinnerTo.getSelectedItemPosition());
        spinnerTo.setSelection(tempPosition);

        stringFrom = spinnerFrom.getSelectedItem().toString();
        stringTo = spinnerTo.getSelectedItem().toString();
    }

    public void convert(View view) {
        convertCurrency();
    }
}