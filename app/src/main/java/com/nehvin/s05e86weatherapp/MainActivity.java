package com.nehvin.s05e86weatherapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    EditText cityName = null;
    TextView weatherReport = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = (EditText)findViewById(R.id.cityName);
        weatherReport = (TextView)findViewById(R.id.weatherReport);
    }

    public void getWeatherReport(View view)
    {
        String cName = null;
        try {
            cName = URLEncoder.encode(cityName.getText().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Please enter a City Name ", Toast.LENGTH_LONG).show();
        }
        if(cName != null && cName.length()>0) {
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(cityName.getWindowToken(), 0);

            DownloadWeatherByCity weatherContent = new DownloadWeatherByCity();
            try {
                weatherContent.execute("http://api.openweathermap.org/data/2.5/weather?q=" + cName + "&appid=72f99145220bac2613bffc41ebee0df1").get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        else
        {
            weatherReport.setText("");
            Toast.makeText(getApplicationContext(), "Please enter a City Name ", Toast.LENGTH_LONG).show();
        }
    }

    class DownloadWeatherByCity extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            HttpURLConnection connection = null;
            InputStream in = null;
            InputStreamReader reader = null;
            String result = "";
            try {
                url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                in = connection.getInputStream();
                reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1)
                {
                    char current = (char)data;
                    result += current;
                    data=reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Unable to get Content";
            } catch (IOException e) {
                e.printStackTrace();
                return "Unable to get Content";
            }
            finally {
                try {
                    if(in != null)
                        in.close();
                    if(reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            StringBuffer wRep = new StringBuffer();
            try {
                JSONObject jsonObject = new JSONObject(s);

                String weatherInfo = jsonObject.getString("weather");
                Log.i(" weather ", weatherInfo);
                JSONArray jsonArrayWeather = new JSONArray(weatherInfo);
                for (int i=0; i < jsonArrayWeather.length(); i++)
                {
                    JSONObject jsonPart = jsonArrayWeather.getJSONObject(i);
                    wRep.append(jsonPart.getString("main"));
                    wRep.append(" : ");
                    wRep.append(jsonPart.getString("description"));
                    wRep.append("\n");
                }
                JSONObject main = jsonObject.getJSONObject("main");
                wRep.append("Temp : ").append(Double.parseDouble(main.getString("temp"))-273.15).append("\n");
                wRep.append("Temp Minimum : ").append(Double.parseDouble(main.getString("temp_min"))-273.15).append("\n");
                wRep.append("Temp Maximum : ").append(Double.parseDouble(main.getString("temp_max"))-273.15).append("\n");
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Unable to get city data", Toast.LENGTH_LONG).show();
            }
            if (wRep.toString() != "") {
                weatherReport.setText(wRep.toString());
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Unable to get city data", Toast.LENGTH_LONG).show();
            }
        }
    }
}