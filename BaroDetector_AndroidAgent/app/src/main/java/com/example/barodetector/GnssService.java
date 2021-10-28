package com.example.barodetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements  LocationListener{

    private LocationManager mLocationmanager;
    private LocationListener mLocationListener;
    private GnssStatus.Callback mGnssStatusCallback;
    private static int satelliteNum;

    private Button btnIndoor;
    private Button btnOutdoor;
    private Button btnstart;
    private Button btnstop;
    TextView sat;
    TextView currentLoc;

    StringBuilder data;
    String state;
    Long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVal();
        initLayout();

        mLocationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);


        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //request location
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            mLocationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this); //1초마다 위치 정보 요구
            mGnssStatusCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {
                    super.onSatelliteStatusChanged(status);
                    satelliteNum = status.getSatelliteCount();
                    data.append("\n" + (System.currentTimeMillis()-startTime)/1000 + "," + satelliteNum + "," + state);
                    Log.v("Satellite","Satellite count: "+ status.getSatelliteCount());
                    sat.setText("sat_num: " + satelliteNum);
                }
            };
            mLocationmanager.registerGnssStatusCallback(mGnssStatusCallback);
        }
    }

    private void initVal(){
        satelliteNum = 0;
        state = "";
        data = new StringBuilder();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status,
                                Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    private void initLayout(){
        btnIndoor = findViewById(R.id.indoor);
        btnOutdoor = findViewById(R.id.outdoor);
        btnstart = findViewById(R.id.start);
        btnstop = findViewById(R.id.stop);
        sat = findViewById(R.id.sat_num);
        sat.setText("Num");
        currentLoc = findViewById(R.id.loc);
        currentLoc.setText("loc");
    }

    public void indoor_state(View view){
        state = "indoor";
        currentLoc.setText(state);
        Log.v("State indoor","User state: "+state);
    }

    public void outdoor_state(View view){
        state = "outdoor";
        currentLoc.setText(state);
        Log.v("State outdoor","User state: "+state);
        //Log.e("State outdoor", "outdoor_state: " + state);
    }
}
