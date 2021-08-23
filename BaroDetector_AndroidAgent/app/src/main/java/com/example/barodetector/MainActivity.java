package com.example.barodetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

//google maps
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

//samsung accessory
import com.samsung.android.sdk.accessory.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Google map
    GoogleMap googleMap;
    SupportMapFragment mapFragment;
    Marker marker;
    LocationBroadcastReceiver locationBroadcastReceiver;
    private static boolean userEntered;

    //GNS
    private static final int SATELLITE_THRESHOLD = 6; // 2 이하일경우 실내, 6 이상일경우 실외
    private static int satelliteNum;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

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
    private GnssStatus.Callback mStatusCallback = new GnssStatus.Callback() {
        @Override
        public void onStarted() {
            super.onStarted();
        }

        @Override
        public void onStopped() {
            super.onStopped();
        }

        @Override
        public void onFirstFix(int ttffMillis) {
            super.onFirstFix(ttffMillis);
        }

        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            if(getUserState()){
                satelliteNum = status.getSatelliteCount();
                if(satelliteNum <= SATELLITE_THRESHOLD){
                    Log.d("ML_OK_GNSS_NOT_OK","ML_OK_GNSS_NOT_OK");
                    return;
                }else{
                    Log.d("ML_OK_GNSS_OK","User go out of building");
                    setUserState(false);
                    userStatus.setText("STATUS: OUT OF BUILDING");
                    userStatus.setTextColor(Color.GREEN);
                }
            }

        }
    };

    //connect with Tizen
    static Context context;
    private ConsumerService mConsumerService = null;
    private boolean mIsBound = false;
    private boolean collecting = true;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mConsumerService = ((ConsumerService.LocalBinder) service).getService();
            connectionStatus.setText("Connected");
            connectionStatus.setTextColor(Color.GREEN);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mConsumerService = null;
            mIsBound = false;
            connectionStatus.setText("Disconnected");
            connectionStatus.setTextColor(Color.RED);
        }
    };

    //baro data
    public static ArrayList<Double> baroData;
    Timer baroTimer = new Timer();
    TimerTask baroService= new TimerTask() {
        @Override
        public void run() {
            startBaroService();
            int idx = baroData.size()/2;
            for(int i=0;i<idx;i++){
                baroData.remove(i);
            }
        }
    };
    private BaroBroadcastReceiver baroBroadcastReceiver;

    //기타 상호작용
    private static boolean sendButtonClicked;
    private static TextView userStatus;
    private static TextView connectionStatus;
    static final String TAG = "BaroDetector";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //application view
        setContentView(R.layout.activity_main);
        userStatus = (TextView) findViewById(R.id.status);
        userStatus.setTextColor(Color.GREEN);
        connectionStatus = (TextView) findViewById(R.id.textView);

        //pairing
        context = getApplicationContext();
        mIsBound = bindService(new Intent(MainActivity.this, ConsumerService.class), mConnection, Context.BIND_AUTO_CREATE);
        sendButtonClicked = false;

        //barometer data
        baroData = new ArrayList();
        baroTimer.schedule(baroService,0,300000);
        baroBroadcastReceiver = new BaroBroadcastReceiver();

        //google map
        locationBroadcastReceiver = new LocationBroadcastReceiver();
        userEntered = false;

        //GNSS
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        satelliteNum = 0;

        //permission for location
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //request permission
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                //request location
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,mLocationListener);
                mLocationManager.registerGnssStatusCallback(mStatusCallback);
                startLocationService();
            }
        } else {
            //start service
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,mLocationListener);
            mLocationManager.registerGnssStatusCallback(mStatusCallback);
            startLocationService();
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);
        mapFragment.getMapAsync(this);


    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(locationBroadcastReceiver);
    }

    @Override
    protected void onDestroy(){
        // Clean up connections
        if (mIsBound == true && mConsumerService != null) {
            connectionStatus.setText("Disconnected");
            connectionStatus.setTextColor(Color.RED);
            mConsumerService.clearToast();
            baroData.clear();
        }
        // Un-bind service
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        sendButtonClicked = false;
        baroTimer.cancel();
        super.onDestroy();
    }

    // Interaction
    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.buttonFindPeerAgent: {
                if (mIsBound == true && mConsumerService != null) {
                    mConsumerService.findPeers();
                    sendButtonClicked = false;
                }
                break;
            }
            case R.id.buttonSend: {
                if (mIsBound == true && sendButtonClicked == false && mConsumerService != null) {
                    if (collecting) {
                        mConsumerService.sendData("Start collecting!");
                        sendButtonClicked = true;
                        collecting = false;
                    } else {
                        sendButtonClicked = false;
                        collecting = true;
                    }
                }
                break;
            }

            default: { }
        }
    }

    public static void updateButtonState(boolean enable) {
        sendButtonClicked = enable;
    }

    // barometer data
    public static void saveBaroData(String data){
        double baroVal = Double.parseDouble(data);
        baroData.add(baroVal);
    }

    public void startBaroService(){
        IntentFilter filter = new IntentFilter("Baro");
        registerReceiver(baroBroadcastReceiver,filter);
        Intent intent = new Intent(MainActivity.this, BaroService.class);
        startService(intent);
    }

    public class BaroBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context con, Intent in){
            if(in.getAction().equals("Baro")){
                //바로미터 결과를 받은 후에 어떻게 할지 적는 부분
            }
        }
    }

    public static void setUserState(boolean state){
        userEntered = state;
    }

    public static boolean getUserState(){
        return userEntered;
    }

    // Location service
    void startLocationService(){
        IntentFilter filter = new IntentFilter("ACT_LOC");
        registerReceiver(locationBroadcastReceiver, filter);
        Intent intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(this.googleMap == null){
            this.googleMap = googleMap;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationService();
                }else{
                    Toast.makeText(this, "Please give permission", Toast.LENGTH_LONG).show();
                }
        }
    }

    public class LocationBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("ACT_LOC")){
                double lat = intent.getDoubleExtra("latitude",0f);
                double log = intent.getDoubleExtra("longitude",0f);
                if(googleMap != null){
                    LatLng latLng = new LatLng(lat,log);
                    if (marker != null){
                        marker.setPosition(latLng);
                    }else{
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        marker = googleMap.addMarker(markerOptions);
                    }
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
                }
            }
        }
    }

    //GNSS
    public static int getSatelliteNum(){
        return satelliteNum;
    }
}