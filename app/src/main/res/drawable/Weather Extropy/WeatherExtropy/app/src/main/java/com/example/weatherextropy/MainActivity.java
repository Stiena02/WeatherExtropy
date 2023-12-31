package com.example.weatherextropy;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};


    //initialising the variables
    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, TemperatureTV, ConditionTV;
    private TextInputEditText cityEdt;
    private ImageView backIV;
    private ImageView IconIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private final int PERMISSION_CODE = 1;
    String cityName;
    Button btn;
    int hum;
    int temp;


    //executes as soon as the app is opened
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);




//accessing the variables made in the layout with their functionality in main activity

        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        TemperatureTV = findViewById(R.id.idTVTemperature);
        ConditionTV = findViewById(R.id.idTVCondition);
        RecyclerView weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        IconIV = findViewById(R.id.idIVIcon);
        ImageView searchIV = findViewById(R.id.idIVSearch);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        btn=findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(MainActivity.this,"Voice Synthesis Activated",Toast.LENGTH_SHORT).show();
            }
        });



//permission to access current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);

        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(), location.getLatitude());
//storing the current location
        getWeatherInfo(cityName);
//functionality of search icon
        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override                 //search box functionality
            public void onClick(View v) {
                String city = Objects.requireNonNull(cityEdt.getText()).toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                } else {
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });




    }



// checks whether the user has granted permission to access current location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }


//for address lookup
    private String getCityName(double longitude, double latitude) {
        StringBuilder cityName = new StringBuilder();
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            if (addresses.size() > 0)
            { Address address = addresses.get(0);
                cityName.append(address.getLocality()).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName.toString();
    }

//passes city name in the api then data is fetched accordingly
        public void getWeatherInfo (String cityName){
            String url = "https://api.weatherapi.com/v1/forecast.json?key=e230fba106594928844121050230301&q=" +cityName+ "&days=1&aqi=yes&alerts=yes";
            cityNameTV.setText(cityName);  //request generated and sent to api
            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override    // executed when api responds
                public void onResponse(JSONObject response) {
                    loadingPB.setVisibility(View.GONE);
                    homeRL.setVisibility(View.VISIBLE);
                    weatherRVModalArrayList.clear();

                try {
                    //fetching data like temperature and humidity and storing them in global variables
                        String temperature = response.getJSONObject("current").getString("temp_c");
                        TemperatureTV.setText(temperature + "°C");

                    int isDay = response.getJSONObject("current").getInt("is_day");
                        String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                        String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                        Picasso.get().load("http:".concat(conditionIcon)).into(IconIV);
                        ConditionTV.setText(condition);
                    if (isDay == 1) {
                        //morning
                        Picasso.get().load("https://i.pinimg.com/originals/e6/16/c8/e616c8f3191d03c891a473afe727dba6.png").into(backIV);
                    } else {
                            //night
                            Picasso.get().load("https://www.wallpaperwolf.com/wallpapers/iphone-wallpapers/hd/download/night-sky-0189.png").into(backIV);
                       }

                        JSONObject forcastObj = response.getJSONObject("forecast");
                        JSONObject forcast0 = forcastObj.getJSONArray("forecastday").getJSONObject(0);
                        JSONArray hourArray = forcast0.getJSONArray("hour");

                        for (int i = 0; i < hourArray.length(); i++) {
                            JSONObject hourObj = hourArray.getJSONObject(i);
                            String time = hourObj.getString("time");
                            String temper = hourObj.getString("temp_c");
                            String img = hourObj.getJSONObject("condition").getString("icon");
                            String wind = hourObj.getString("wind_kph");
                            weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind));
                        }
                        weatherRVAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener(){ //executed when the data is not fetched from api
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Please Enter Valid City Name", Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(jsonObjectRequest);

        }


    }