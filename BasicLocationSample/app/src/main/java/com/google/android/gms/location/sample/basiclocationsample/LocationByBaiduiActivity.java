package com.google.android.gms.location.sample.basiclocationsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class LocationByBaiduiActivity extends Activity {

    private static final String TAG = "LocationByBaiduAct";
    private static final int MSG_LOCATION = 0;
    private static final String LOCATION_POSITION = "position";
    private static final String LOCATION_CITY = "city";

    private LocationManager locationManager;
    private String locationProvider;

    private TextView tv_baidu_lat;
    private TextView tv_baidu_lng;
    private static TextView tv_baidu_position;
    private static TextView tv_baidu_city;
    //Define and init handle message class object
    private MyLocationHandler myLocationHandler = new MyLocationHandler();

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, LocationByBaiduiActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_by_baidu);

        //Init Widget
        tv_baidu_lat = (TextView) findViewById(R.id.latitude_baidu_text);
        tv_baidu_lng = (TextView) findViewById(R.id.longitude_baidu_text);
        tv_baidu_position = (TextView) findViewById(R.id.position_baidu_text);
        tv_baidu_city = (TextView) findViewById(R.id.province_baidu_text);

        //Init LocationManager and get last known location, then display location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationProvider = getLocationProvider();
        Location lastLocation = getLatestLocation(locationProvider);
        displayLocation(lastLocation);

    }


    @Override
    protected void onResume() {
        super.onResume();

        //Register LocationListener with latest location provider
        locationProvider = getLocationProvider();
        if (locationProvider != null) {
            locationManager.requestLocationUpdates(locationProvider, 5*1000, 1, locationListener);
        }else {
            Toast.makeText(this, "location provider is null.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        //Unregister LocationListener
        locationManager.removeUpdates(locationListener);
    }

    private String getLocationProvider(){
        List<String> providers = locationManager.getProviders(true);

        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            return  LocationManager.GPS_PROVIDER;
        }else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }else {
            return null;
        }
    }

    private Location getLatestLocation(String provider) {
        if (provider != null){
            return locationManager.getLastKnownLocation(provider);
        }else {
            return null;
        }
    }

    private void displayLocation(Location currentLocation) {
        if (currentLocation != null){
            tv_baidu_lat.setText(String.valueOf(currentLocation.getLatitude()));
            tv_baidu_lng.setText(String.valueOf(currentLocation.getLongitude()));

            displayReversedLocation(currentLocation);
        }else {
            Log.d(TAG, "displayLocation(): currentLocation is null.");
            Toast.makeText(this, "displayLocation():currentLocation is null.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayReversedLocation(final Location currentLocation) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Assemble Baidu Map API geography interface url string
                StringBuilder urlStrBuilder = new StringBuilder();
                urlStrBuilder.append("http://api.map.baidu.com/geocoder?output=json&location=")
                        .append(currentLocation.getLatitude()).append(",").append(currentLocation.getLongitude())
                        .append("&key=fu82xOIQMdkHnjwUt1XOG8d0");

                //Define local params
                HttpURLConnection httpURLConnection = null;
                BufferedReader br = null;

                try{
                    //Transform url string to url object
                    URL baiduiMapsApiUrl = new URL(urlStrBuilder.toString());
                    //Open HttpURLConnection and set it's properties
                    httpURLConnection = (HttpURLConnection) baiduiMapsApiUrl.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(8 * 1000);
                    httpURLConnection.setReadTimeout(8 * 1000);
                    httpURLConnection.setRequestProperty("Accept-Language", "zh-CN");

                    //Get response from server by HttpURLConnection inputStream
                    br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
                    StringBuilder responseBuilder = new StringBuilder();
                    String strLine;
                    while((strLine = br.readLine()) != null){
                        responseBuilder.append(strLine);
                    }

                    //Parse returned json data
                    String response = responseBuilder.toString();
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("status").equals("OK")){
                        JSONObject rstJsonObject = jsonObject.getJSONObject("result");
                        if (rstJsonObject != null){
                            JSONObject addrComponentJsonObj = rstJsonObject.getJSONObject("addressComponent");

                            String position = rstJsonObject.getString("formatted_address");
                            String city = addrComponentJsonObj.getString("city");

                            //Store response to Message object, then sends Message to MainThread
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString(LOCATION_POSITION, position);
                            bundle.putString(LOCATION_CITY, city);
                            msg.what = MSG_LOCATION;
                            msg.setData(bundle);
                            myLocationHandler.sendMessage(msg);

                        }else{
                            Toast.makeText(LocationByBaiduiActivity.this, "result is null.", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(LocationByBaiduiActivity.this, "Response error.", Toast.LENGTH_SHORT).show();
                    }


                }catch(Exception e){
                    e.printStackTrace();
                }finally {
                    if (httpURLConnection != null){
                        httpURLConnection.disconnect();
                    }

                    if (br != null){
                        try {
                            br.close();
                        } catch (IOException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private static class MyLocationHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCATION:
                    Bundle rstBundle = msg.getData();
                    tv_baidu_position.setText(rstBundle.getString(LOCATION_POSITION));
                    tv_baidu_city.setText(rstBundle.getString(LOCATION_CITY));
                    break;
                default:
                    break;
            }
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            displayLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}