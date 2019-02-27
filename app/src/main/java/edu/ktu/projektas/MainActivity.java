package edu.ktu.projektas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;


import com.github.mikephil.charting.charts.LineChart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;

import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity{

    private Spinner spinnerIn;
    private Spinner spinnerOut;
    private ImageButton button;
    private String selectIn;
    private String selectOut;
    private TextView currencyRateDisplay;
    private TextView result;
    private EditText valueIn;
    private String cur1;
    private String cur2;
    private ImageButton reverse;


    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.mainactivitydesign);
        setContentView(R.layout.updateddesign);
        if(!isConnected(MainActivity.this))
            buildDialog(MainActivity.this).show();
        else {
            Toast.makeText(MainActivity.this,"Welcome", Toast.LENGTH_SHORT).show();
            //setContentView(R.layout.mainactivitydesign);
        }

        currencyRateDisplay = (TextView) findViewById(R.id.exchange_rate);
        result = (TextView) findViewById(R.id.result);
        valueIn = (EditText) findViewById(R.id.input_number);
        valueIn.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.currencies_array,R.layout.support_simple_spinner_dropdown_item);
        spinnerIn=(Spinner) findViewById(R.id.spinnerIn);
        spinnerOut= (Spinner)findViewById(R.id.spinnerOut);
        spinnerIn.setAdapter(adapter);
        spinnerOut.setAdapter(adapter);

        button = (ImageButton)findViewById(R.id.convert);
        reverse = (ImageButton) findViewById(R.id.reverse);
        button.setOnClickListener(exchangeCurrency);

        //reverse.setOnClickListener(reverseCurrency);

        spinnerIn.setOnItemSelectedListener(getFirstValue);
        spinnerOut.setOnItemSelectedListener(getSecondValue);

        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerIn=(Spinner) findViewById(R.id.spinnerOut);
                spinnerOut= (Spinner)findViewById(R.id.spinnerIn);
            }
        });
        /*LineChart chart = (LineChart) findViewById(R.id.chart);

        int[] data = new int[20];
        Random rng = new Random(100);

        for(int i=0;i<data.length;i++){
            data[i]= rng.nextInt();
        }*/

    }

    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
        else return false;
        }else
        return false;
    }

    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("You need to have Mobile Data or wifi to access this. Press ok to Exit");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });

        return builder;
    }

    AdapterView.OnItemSelectedListener getFirstValue=new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectIn = parent.getItemAtPosition(position).toString();
            cur1 = Character.toString(selectIn.charAt(0))+Character.toString(selectIn.charAt(1))+Character.toString(selectIn.charAt(2));
            System.out.println(cur1);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    AdapterView.OnItemSelectedListener getSecondValue=new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectOut = parent.getItemAtPosition(position).toString();
            cur2 = Character.toString(selectOut.charAt(0))+Character.toString(selectOut.charAt(1))+Character.toString(selectOut.charAt(2));
            System.out.println(cur2);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    View.OnClickListener exchangeCurrency = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            new CalculateCurrency().execute();
        }
    };

    public void openCalculator(View view){
        Intent startCalculator = new Intent(this, Calculator.class);
        startActivity(startCalculator);
    }


    private class CalculateCurrency extends AsyncTask<URL,Void,String>{
        @Override
        protected String doInBackground(URL... urls){
            String value=null;
            String url = "https://free.currencyconverterapi.com/api/v6/convert?q="+cur1+"_"+cur2+"&compact=ultra&apiKey=64395b619a9dbe7217b4";
            try{
                InputStream is = new URL(url).openStream();
                System.out.println(url);
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                StringBuilder sb = new StringBuilder();
                int cp;
                while ((cp = rd.read()) != -1) {
                    sb.append((char) cp);
                }
                String jsonText = sb.toString();
                System.out.println(jsonText);
                JSONObject json = new JSONObject(jsonText);
                System.out.println(json.toString());
                value=json.getString(cur1+"_"+cur2);
                //System.out.println(value);
                is.close();
            }catch (JSONException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
                Toast.makeText(MainActivity.this,"Turn on internet access", Toast.LENGTH_SHORT).show();
            }
            return value;
        }

        @Override
        protected void onPostExecute(String value) {
            String number;
            final String myStr = valueIn.getText().toString();
            if(!myStr.isEmpty())
            {
                number = valueIn.getText().toString();
                //System.out.println(number);
                double converted = Double.parseDouble(number);
                double val = Double.parseDouble(value);
                if(val==0){
                    val=1;
                }
                converted = converted * val;
                currencyRateDisplay.setText("1 " + cur1 + " = " + val +" "+ cur2);
                result.setText(String.valueOf(String.format("%.2f",converted))+ " "+cur2);
            }else{
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.noinput),
                        Toast.LENGTH_LONG).show();
                currencyRateDisplay.setText("Exchange rate");
                result.setText("Result");
            }

        }
    }
}
