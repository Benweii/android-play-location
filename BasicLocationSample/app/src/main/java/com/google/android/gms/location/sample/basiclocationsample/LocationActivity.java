package com.google.android.gms.location.sample.basiclocationsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class LocationActivity extends Activity {

    private static final String TAG = "LocationActivity";
    private LocationManager locationManager;
    private Location mLastLocation;

    private TextView tv_latitude;
    private TextView tv_longitude;

    private String provider;

    public static void actionStart(Context context){
        Intent intent = new Intent(context, LocationActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        tv_latitude = (TextView) findViewById(R.id.latitude_text);
        tv_longitude = (TextView) findViewById(R.id.longitude_text);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLastLocation = getLatestLocation();
        if (mLastLocation != null){
            showLocation(mLastLocation);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume()");
        if (locationManager != null){
            locationManager.requestLocationUpdates(provider, 5*1000, 1, locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause()");
        if (locationManager != null){
            locationManager.removeUpdates(locationListener);
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

    private Location getLatestLocation(){
        Location location = null;
        List<String> providers = locationManager.getProviders(true);

        if (providers.contains(LocationManager.GPS_PROVIDER)){
            provider = LocationManager.GPS_PROVIDER;
            Log.d(TAG, "GPS");
        }else if (providers.contains(LocationManager.NETWORK_PROVIDER)){
            provider = LocationManager.NETWORK_PROVIDER;
            Log.d(TAG, "NETWORK");
        }else{
            Toast.makeText(this, "No location provider to use.", Toast.LENGTH_LONG).show();
        }

        if (provider != null){

            location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, "return latest location." + location);
        }else{
            Log.d(TAG, "provider is null.");
        }

        return location;
    }

    private void showLocation(Location location){
        Log.d(TAG, "showLocation()");
        tv_latitude.setText(location.getLatitude() + "");
        tv_longitude.setText(location.getLongitude() + "");
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
            showLocation(location);
        }
    };
}
