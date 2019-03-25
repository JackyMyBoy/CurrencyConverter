package edu.ktu.projektas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

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
    private int curPosition1;
    private int curPosition2;
    private LineChart chart;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.mainactivitydesign);
        setContentView(R.layout.updateddesign);
        if (!isConnected(MainActivity.this))
            buildDialog(MainActivity.this).show();
        else {
            Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
            //setContentView(R.layout.mainactivitydesign);
        }

        currencyRateDisplay = (TextView) findViewById(R.id.exchange_rate);
        result = (TextView) findViewById(R.id.result);
        valueIn = (EditText) findViewById(R.id.input_number);
        valueIn.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies_array, R.layout.support_simple_spinner_dropdown_item);
        spinnerIn = (Spinner) findViewById(R.id.spinnerIn);
        spinnerOut = (Spinner) findViewById(R.id.spinnerOut);
        spinnerIn.setAdapter(adapter);
        spinnerOut.setAdapter(adapter);

        button = (ImageButton) findViewById(R.id.convert);
        reverse = (ImageButton) findViewById(R.id.reverse);
        button.setOnClickListener(exchangeCurrency);

        //reverse.setOnClickListener(reverseCurrency);

        spinnerIn.setOnItemSelectedListener(getFirstValue);
        spinnerOut.setOnItemSelectedListener(getSecondValue);

        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curPosition1 = spinnerIn.getSelectedItemPosition();
                curPosition2 = spinnerOut.getSelectedItemPosition();
                spinnerIn.setSelection(curPosition2);
                spinnerOut.setSelection(curPosition1);
                String dummyCur1 = cur1;
                String dummyCur2 = cur2;
                cur1 = dummyCur2;
                cur2 = dummyCur1;
            }
        });
        chart = (LineChart) findViewById(R.id.chart);
        chart.setNoDataText("");


        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        System.out.println(currentDate);
    }

    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else return false;
        } else
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

    AdapterView.OnItemSelectedListener getFirstValue = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectIn = parent.getItemAtPosition(position).toString();
            //curPosition1=position;
            cur1 = Character.toString(selectIn.charAt(0)) + Character.toString(selectIn.charAt(1)) + Character.toString(selectIn.charAt(2));
            System.out.println(cur1);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    AdapterView.OnItemSelectedListener getSecondValue = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectOut = parent.getItemAtPosition(position).toString();
            //curPosition2=position;
            cur2 = Character.toString(selectOut.charAt(0)) + Character.toString(selectOut.charAt(1)) + Character.toString(selectOut.charAt(2));
            System.out.println(cur2);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    View.OnClickListener exchangeCurrency = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new CalculateCurrency().execute();
            new GetChartData().execute();
        }
    };

    class DateAndValues {
        public String date;
        public String value;

        public DateAndValues(String date, String value) {
            this.date = date;
            this.value = value;
        }
    }

    public void openCalculator(View view) {
        Intent startCalculator = new Intent(this, Calculator.class);
        startActivity(startCalculator);
    }

    private class GetChartData extends AsyncTask<URL, Void, String> {
        ArrayList<DateAndValues> dateValues = new ArrayList<>();
        String[] dates = new String[10];
        @Override
        protected String doInBackground(URL... urls) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            System.out.println(currentDate);
            String url = "https://free.currencyconverterapi.com/api/v6/convert?apiKey=64395b619a9dbe7217b4&q=" + cur1 + "_" + cur2 + "&compact=ultra&date=2019-03-20&endDate=2019-03-25";
            try {
                InputStream is = new URL(url).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                StringBuilder sb = new StringBuilder();
                int cp;
                while ((cp = rd.read()) != -1) {
                    sb.append((char) cp);
                }
                String jsonText = sb.toString();
                System.out.println(jsonText);
                JSONObject json = new JSONObject(jsonText);
                String dateValuesString = json.getString(cur1 + "_" + cur2);
                System.out.println(dateValuesString);
                JSONObject valuesJson = new JSONObject(dateValuesString);
                Iterator<String> iterator = valuesJson.keys();
                int count=0;
                while (iterator.hasNext()) {
                    String raktas = iterator.next();
                    dateValues.add(new DateAndValues(raktas,valuesJson.getString(raktas)));
                    dates[count++]=raktas;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String value) {
            List<Entry> entries = new ArrayList<>();
            for(int i=0;i<dateValues.size();i++){
                entries.add(new Entry((float)i,Float.parseFloat(dateValues.get(i).value)));
            }
            LineDataSet dataSet = new LineDataSet(entries,"Currency values");
            LineData lineData = new LineData(dataSet);
            lineData.setValueTextColor(Color.WHITE);
            chart.setData(lineData);
            IAxisValueFormatter formatter = new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return dates[(int)value];
                }
            };
            XAxis xAxis = chart.getXAxis();
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(formatter);
            YAxis right = chart.getAxisRight();
            right.setDrawLabels(false);
            chart.setPinchZoom(true);
            chart.getAxisLeft().setTextColor(Color.WHITE);
            chart.getXAxis().setTextColor(Color.WHITE);
            chart.getLegend().setTextColor(Color.WHITE);
            chart.getAxisLeft().setTextSize(8f);
            chart.getXAxis().setTextSize(8f);
            chart.getDescription().setEnabled(false);
            chart.invalidate();
        }
    }

    private class CalculateCurrency extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String value = null;
            String url = "https://free.currencyconverterapi.com/api/v6/convert?q=" + cur1 + "_" + cur2 + "&compact=ultra&apiKey=64395b619a9dbe7217b4";
            try {
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
                value = json.getString(cur1 + "_" + cur2);
                //System.out.println(value);
                is.close();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Turn on internet access", Toast.LENGTH_SHORT).show();
            }
            return value;
        }

        @Override
        protected void onPostExecute(String value) {
            String number;
            final String myStr = valueIn.getText().toString();
            if (!myStr.isEmpty()) {
                number = valueIn.getText().toString();
                //System.out.println(number);
                double converted = Double.parseDouble(number);
                double val = Double.parseDouble(value);
                if (val == 0) {
                    val = 1;
                }
                converted = converted * val;
                currencyRateDisplay.setText("1 " + cur1 + " = " + val + " " + cur2);
                result.setText(String.valueOf(String.format("%.2f", converted)) + " " + cur2);
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.noinput),
                        Toast.LENGTH_LONG).show();
                currencyRateDisplay.setText("Exchange rate");
                result.setText("Result");
            }

        }
    }
}
