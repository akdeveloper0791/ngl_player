package com.ibetter.www.adskitedigi.adskitedigi.location;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ibetter.www.adskitedigi.adskitedigi.R;

public class SearchLocation extends FragmentActivity implements OnMapReadyCallback,View.OnClickListener {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap googleMap;
    private Context context;
    private final static int LOCATION_PERMISSION_REQUEST = 1;
    private final static int REQUEST_CHECK_SETTINGS =  2;
    private ProgressDialog busyDialog;
    private Location location;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_location);
        context = this;
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_layout);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }

    public void onMapReady(GoogleMap map)
    {
        googleMap = map;
        //map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //get last known location
        getAndSetLastKnownLocation();
    }

    private void getAndSetLastKnownLocation()
    {
       Task task =  fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
              if(location!=null)
              {
                  foundLocation(location);

              }else
              {

                  Toast.makeText(context, "Unable to get location, please click refresh", Toast.LENGTH_SHORT).show();
              }
            }
        });
    }

    private void refreshLocation()
    {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
             startRefreshLocation();
        }else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST);
        }
    }

    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResult)
    {
      if(grantResult[0]==PackageManager.PERMISSION_GRANTED)
      {
          startRefreshLocation();
      }else
      {
          Toast.makeText(context,"Please approve permission",Toast.LENGTH_LONG).show();
      }
    }

    private void startRefreshLocation()
    {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.

                displayBusyDialog("Retrieving location please wait...");
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);

            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(SearchLocation.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }else
                {
                    Toast.makeText(context,"Unable to get the location"+e.getMessage(),Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
       if(resultCode==RESULT_OK)
       {
           switch (requestCode)
           {
               case REQUEST_CHECK_SETTINGS:
                   startRefreshLocation();
                   break;
           }
       }
    }

    private LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            dismissBusyDialog();
            boolean isLocationFound=false;
            stopLocationUpdates();
            if (locationResult != null) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    if (location != null) {
                        foundLocation(location);
                        isLocationFound=true;
                        break;
                    }
                }
            }else
            {
                Log.d("Licence","Inside location call back locationResult is null");
            }
            if(!isLocationFound)
            {
                Toast.makeText(context,"Unable to get the location",Toast.LENGTH_SHORT).show();
            }
        };
    };

    public void onDestroy()
    {
       super.onDestroy();
       stopLocationUpdates();
    }

    private void stopLocationUpdates()
    {
        if(fusedLocationProviderClient!=null)
        {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    private void displayBusyDialog(String msg)
    {
        busyDialog = new ProgressDialog(context);
        busyDialog.setMessage(msg);
        busyDialog.setCanceledOnTouchOutside(false);
        busyDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
              stopLocationUpdates();
            }
        });
        busyDialog.show();
    }

    private void dismissBusyDialog()
    {
        if(busyDialog!=null && busyDialog.isShowing())
        {
            busyDialog.dismiss();
        }
    }

    private void foundLocation(Location location)
    {
        this.location = location;
        googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())));

        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.refresh_location:
                refreshLocation();
                break;
            case R.id.submit_button:
                getAddressFromLocation();
                break;
        }
    }

    private void getAddressFromLocation()
    {
        if(location!=null) {
            IntentFilter intentFilter = new IntentFilter(GetAddressReceiver.ACTION);
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

            registerReceiver(new GetAddressReceiver(), intentFilter);

            displayBusyDialog("Retrieving address");

            Intent intent = new Intent(context, GetAddressFromLocation.class);
            intent.putExtra("intent_action", GetAddressReceiver.ACTION);
            intent.putExtra("location", location);
            startService(intent);
        }else
        {
            Toast.makeText(context,"No location found",Toast.LENGTH_SHORT).show();
        }


    }

    private class GetAddressReceiver extends BroadcastReceiver
    {
        private final static String ACTION="com.ibetter.www.adskitedigi.adskitedigi.location.SearchLocation.GetAddressReceiver";
        public void onReceive(Context context,Intent intent)
        {
            unregisterReceiver(this);
            dismissBusyDialog();
            //Toast.makeText(context,intent.getStringExtra("status"),Toast.LENGTH_SHORT).show();
            Intent returnIntent = getIntent();
            returnIntent.putExtra("location",location);

           if(intent.getBooleanExtra("flag",false))
           {
               returnIntent.putExtra("address",intent.getStringExtra("status"));
           }

           if(getParent()!=null)
           {
               getParent().setResult(RESULT_OK,returnIntent);

           }else
           {

               SearchLocation.this.setResult(RESULT_OK,returnIntent);
           }


           finish();
        }
    }
}

