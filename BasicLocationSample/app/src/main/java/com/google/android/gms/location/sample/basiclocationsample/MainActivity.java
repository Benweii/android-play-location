/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.basiclocationsample;

import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

//import android.support.v7.app.ActionBarActivity;

/**
 * Location sample.
 *
 * Demonstrates use of the Location API to retrieve the last known location for a device.
 * This sample uses Google Play services (GoogleApiClient) but does not need to authenticate a user.
 * See https://github.com/googlesamples/android-google-accounts/tree/master/QuickStart if you are
 * also using APIs that need authentication.
 */
public class MainActivity extends Activity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    protected static final String TAG = "basic-location-sample";
    private static  final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected TextView mLatitudeText;
    protected TextView mLongitudeText;


    private LocationManager locationManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));

//        if (checkPlayServices()){
//            buildGoogleApiClient();
//        }


    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    @Override
    protected void onStart() {
        super.onStart();
//        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
    }


    private boolean checkPlayServices(){
        int rstCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (rstCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(rstCode)){
                GooglePlayServicesUtil.getErrorDialog(rstCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else{
                Toast.makeText(this, "Not support Play Services.", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Not support Play Services");
                finish();
            }
            return false;
        }
        Toast.makeText(this, "support Play Services.", Toast.LENGTH_LONG).show();
        Log.d(TAG, "support Play Services");
        return true;
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if (mGoogleApiClient == null){
            Toast.makeText(this, "mGoogleApiClient is null.", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "mGoogleApiClient not null.", Toast.LENGTH_LONG).show();
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        } else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    /**
     * Get last location by LocationManager.
     */
    private Location getLatestLocation(){

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null){
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }

    public void bt_location_by_google(View view){
        LocationByGoogleActivity.actionStart(this);
    }

    public void bt_location_by_baidu(View view) {
        LocationByBaiduiActivity.actionStart(this);
    }
}

