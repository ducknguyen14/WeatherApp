package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // Notes: Debug Tools
    boolean debug = false;

    // Notes: XML Elements
    Button get_button;
    EditText city_editText;
    TextView cityName_textView;
    TextView result_textView;
    ConstraintLayout constraintLayout;

    // Notes: Variables
    private RequestQueue requestQueue;


    // Notes: Format - https://api.openweathermap.org/data/2.5/weather?q=San%20Francisco + APIKey
    String baseURL = "https://api.openweathermap.org/data/2.5/weather?q=";
    final String API = BuildConfig.apikey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Notes: XML Elements
        get_button = (Button) findViewById(R.id.get_button);
        city_editText = (EditText) findViewById(R.id.city_editText);
        cityName_textView = (TextView) findViewById(R.id.cityName_textView);
        result_textView = (TextView) findViewById(R.id.result_textView);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);

        // Notes: Variables
        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        // Notes: Button Listener
        get_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Notes: Closes the keyboard when button is clicked
                hideKeyboard(v);

                String city_string = city_editText.getText().toString();
                String city_final_string;

                // Notes: Need to format the city name correctly if there is any white space before
                //  sending request
                if(city_string.contains(" "))
                {
                    Log.d("DEBUG", "White Space?: YES");
                    city_final_string = city_string.replaceAll("\\s", "%20");
                }
                else
                {
                    city_final_string = city_string;
                }


                // Notes: Reset city input field element
                city_editText.setText("");

                // Notes: Set the city name textview
                cityName_textView.setText(city_string);

                // Notes: Checking if city input is empty
                if (TextUtils.isEmpty(city_string))
                {
                    city_editText.setError("Missing a city");
                }
                else
                {
                    // Notes: Creating the URL Request
                    String url = baseURL + city_final_string + API;

                    if(debug == true)
                    {
                        Log.d("DEBUG", "URL: " + url);
                    }

                    sendAPIRequest(url);
                }
            }
        });


    }


    private void changeBackground(String weather)
    {
        switch (weather)
        {
            case "Clear":
                constraintLayout.setBackgroundResource(R.drawable.clear);
                break;
            case "Clouds":
                constraintLayout.setBackgroundResource(R.drawable.clouds);
                break;
            case "Rain":
                constraintLayout.setBackgroundResource(R.drawable.rain);
                break;
            case "Snow":
                constraintLayout.setBackgroundResource(R.drawable.snow);
                break;
            case "Haze":
                constraintLayout.setBackgroundResource(R.drawable.haze);

        }
    }


    private void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
    }


    private double kelvinToFahrenheit(double kelvin)
    {
        double fahrenheit = ((kelvin - 273.15) * (1.8)) + 32;
        return fahrenheit;
    }


    private void sendAPIRequest(String url)
    {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            // Notes: Weather
                            JSONArray jsonArray = response.getJSONArray("weather");
                            JSONObject weather_JSON = jsonArray.getJSONObject((0));
                            String weather_string = weather_JSON.getString("main");

                            // Notes: Temperature
                            JSONObject jsonObject = response.getJSONObject("main");
                            double temperature_kelvin = jsonObject.getDouble("temp");

                            if(debug == true)
                            {
                                Log.d("DEBUG", "Weather: " + weather_string);
                                Log.d("DEBUG", "Temperature_Kelvin: " + temperature_kelvin);
                            }

                            // Notes: Conversion of temperature
                            double temperature_fahrenheit = kelvinToFahrenheit(temperature_kelvin);

                            // Notes: Set the results in the result_textView and change the background
                            String result = String.format("Weather: %s\nTemperature: %.2f", weather_string, temperature_fahrenheit);
                            changeBackground(weather_string);
                            result_textView.setText(result);

                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if(debug == true)
                        {
                            Log.d("DEBUG", "onErrorResponse: ");
                        }

                        error.printStackTrace();
                    }
                });


        requestQueue.add(request);
    }


}