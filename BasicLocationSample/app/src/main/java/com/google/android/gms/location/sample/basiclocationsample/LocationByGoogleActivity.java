package com.google.android.gms.location.sample.basiclocationsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class LocationByGoogleActivity extends Activity {

    private static final String TAG = "LocationByGoogleActivity";
    private static final int SHOW_LOCATION = 0;
    private static final String ADDR_LOCALITY = "locality";
    private static final String ADDR_FORMATTED = "formatted_address";

    private LocationManager locationManager;
    private Location mLastLocation;

    private TextView tv_latitude;
    private TextView tv_longitude;
    private TextView tv_position;
    private TextView tv_province;

    private String provider;

    public static void actionStart(Context context){
        Intent intent = new Intent(context, LocationByGoogleActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_by_google);

        tv_latitude = (TextView) findViewById(R.id.latitude_text);
        tv_longitude = (TextView) findViewById(R.id.longitude_text);
        tv_position = (TextView) findViewById(R.id.position_text);
        tv_province = (TextView) findViewById(R.id.province_text);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = getLocationProvider();
        mLastLocation = getLatestLocation(provider);

        Toast.makeText(this, "mLastLocation=" + mLastLocation, Toast.LENGTH_SHORT).show();
        showLocation(mLastLocation);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume()");
        provider = getLocationProvider();
        if (provider != null && locationManager != null){
            locationManager.requestLocationUpdates(provider, 5*1000, 1, locationListener);
            Log.d(TAG, "set LocationListener successed.");
            Toast.makeText(this, "Set LocationListener successed.provider=" + provider, Toast.LENGTH_LONG).show();
        }else{
            Log.d(TAG, "set LocationListener failed. provider=" + provider + " & LocationManager=" + locationManager);
            Toast.makeText(this, "Set LocationListener failed.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause()");
        if (locationManager != null){
            locationManager.removeUpdates(locationListener);
            Toast.makeText(this, "cancel LocationListener successed.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private  String getLocationProvider(){
        List<String> providers = locationManager.getProviders(true);

        if (providers.contains(LocationManager.GPS_PROVIDER)){
            provider = LocationManager.GPS_PROVIDER;
            Log.d(TAG, "GPS");
        }else if (providers.contains(LocationManager.NETWORK_PROVIDER)){
            provider = LocationManager.NETWORK_PROVIDER;
            Log.d(TAG, "NETWORK");
        }else{
            provider = null;
            Toast.makeText(this, "No location provider to use.", Toast.LENGTH_LONG).show();
        }
        return provider;
    }

    private Location getLatestLocation(String provider){
        Location location = null;

        if (provider != null){
            location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, "return latest location." + location);
        }else{
            Log.d(TAG, "provider is null.");
        }

        return location;
    }

    private void showLocation(final Location location){
        Log.d(TAG, "showLocation()");

        if(location == null){
            tv_latitude.setText("null");
            tv_longitude.setText("null");
        }else{
            tv_latitude.setText(location.getLatitude() + "");
            tv_longitude.setText(location.getLongitude() + "");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Assemble reverse geography encoding's interface address.

                    Looper.prepare();
                    StringBuilder urlStr = new StringBuilder();
                    urlStr.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
                    urlStr.append(location.getLatitude()).append(",");
                    urlStr.append(location.getLongitude()).append("&sensor=false");

                    HttpURLConnection urlConnection = null;
                    BufferedReader br = null;

                    try{
                        //define a bundle object use for save data, that parse from json object
                        Bundle addrBundle = new Bundle();
                        URL url = new URL(urlStr.toString());
//                        Toast.makeText(LocationByGoogleActivity.this, url.toString(), Toast.LENGTH_LONG).show();
                        Log.d(TAG, url.toString());
                        urlConnection = (HttpURLConnection)url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setConnectTimeout(8 * 1000);
                        urlConnection.setReadTimeout(8 * 1000);
                        urlConnection.setRequestProperty("Accept-Language", "zh-CN");

                        StringBuilder response = new StringBuilder();
                        String line;
                        br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        //Read result from server.
                        int i = 0;
                        while((line = br.readLine()) != null){
                            response.append(line);
                            i++;
                        }
                        Log.d(TAG, "i=" + i);
                        String result = response.toString();
                        Log.d(TAG, "Result=" + result);

                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray rstArray = jsonObject.getJSONArray("results");
                        Log.d(TAG, "rstArrat length=" + rstArray.length());
                        if (rstArray.length() > 0){
                            JSONObject subJsonObject = rstArray.getJSONObject(0);
                            Log.d(TAG, "subJsonObject=" + subJsonObject);

                            //Get accurate location
                            //Get address components
                            JSONArray addressComponents = subJsonObject.getJSONArray("address_components");
                            int addCompsLength = addressComponents.length();
                            Log.d(TAG, "addressComponents length=" + addCompsLength);
                            //Get province
                            for (int j = 0; j < addCompsLength; j++ ){
                                JSONObject addrInfoObj = addressComponents.getJSONObject(j);
                                JSONArray addrTypes = addrInfoObj.getJSONArray("types");
                                if (addrTypes.length() > 0 && addrTypes.opt(0).equals("locality")){
                                    addrBundle.putString(ADDR_LOCALITY, addrInfoObj.getString("short_name"));
                                    break;
                                }
                            }
                            //Get formatted address
                            addrBundle.putString(ADDR_FORMATTED, subJsonObject.getString("formatted_address"));

                            //Send message to mainThread.
                            Message message = new Message();
                            message.what =SHOW_LOCATION;
//                            message.obj = address;
                            message.setData(addrBundle);
                            handler.sendMessage(message);
                        }else{
                            Log.d(TAG, "have no json object.");
                        }


                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        if (urlConnection != null){
                            urlConnection.disconnect();
                        }

                        if (br != null){
                            try{
                                br.close();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }).start();
        }


    }

    private LocationListener locationListener = new LocationListener(){
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(LocationByGoogleActivity.this, "onLocationChanged()", Toast.LENGTH_LONG).show();
            showLocation(location);
        }
    };


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SHOW_LOCATION:
                    Bundle addrBundle = msg.getData();
                    if (addrBundle != null){
                        Toast.makeText(MyApplication.getContext(), "currentPosition=" +
                                addrBundle.getString(ADDR_FORMATTED, "null"), Toast.LENGTH_SHORT).show();
                        tv_position.setText(addrBundle.getString(ADDR_FORMATTED, "null"));
                        tv_province.setText(addrBundle.getString(ADDR_LOCALITY, "null"));
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
