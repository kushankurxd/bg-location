package com.example.bglocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends Activity {
    private static final int REQUEST_PERMISSIONS = 100;
    boolean boolean_permission;
    boolean isServiceRunning;

    // x-Views
    TextView latitude, longitude;
    Button button;

    SharedPreferences mPref;
    SharedPreferences.Editor medit;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // activity_mail
        button = (Button) findViewById(R.id.button);
        latitude = (TextView) findViewById(R.id.textView5);
        longitude = (TextView) findViewById(R.id.textView7);

        // Shared pref
        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        medit = mPref.edit();


        // Checking if service is already running
        if (mPref.getString("service", "").matches("service")) {
            // it is running
            isServiceRunning = true;
            button.setText("Stop Service");
            button.setBackground(getDrawable(R.drawable.button_shape_dark));
        } else {
            isServiceRunning = false;
            button.setText("Start Service");
            button.setBackground(getDrawable(R.drawable.button_shape));
        }

        // When button is clicked
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boolean_permission) {

                    if (!isServiceRunning) {
                        // Saving state of service in prefrence
                        Toast.makeText(getApplicationContext(), "Starting background service", Toast.LENGTH_SHORT).show();
                        medit.putString("service", "service").commit();

                        // Starting the service
                        Intent intent = new Intent(getApplicationContext(), BgLocationService.class);
                        startService(intent);

                        // Changing button text
                        button.setText("Stop Service");
                        button.setBackground(getDrawable(R.drawable.button_shape_dark));

                        isServiceRunning = true;

                    } else {
                        // Saving state of service in prefrence
                        Toast.makeText(getApplicationContext(), "Stopping background service", Toast.LENGTH_SHORT).show();
                        medit.putString("service", "").commit();

                        // Stopping the service
                        Intent intent = new Intent(getApplicationContext(), BgLocationService.class);
                        boolean isStoped = stopService(intent);

                        // Changing button text
                        if (isStoped) {
                            button.setText("Start Service");
                            button.setBackground(getDrawable(R.drawable.button_shape));

                            isServiceRunning = false;
                        }else {
                            // Do something when it is not stopped
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enable the permissions", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Call permission manager to request user permission.
        fn_permission();
    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION

                        },
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;

                } else {
                    Toast.makeText(getApplicationContext(), "Permission not granted", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Receiving data from service
            double lat = Double.valueOf(intent.getStringExtra("latutide"));
            double lng = Double.valueOf(intent.getStringExtra("longitude"));

            // Updating the ui
            latitude.setText(lat + "");
            longitude.setText(lng + "");

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // Register the receiver on resume
        registerReceiver(broadcastReceiver, new IntentFilter(BgLocationService.str_receiver));

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the receiver on pause
        unregisterReceiver(broadcastReceiver);
    }


}