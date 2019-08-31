package com.ibetter.www.adskitedigi.adskitedigi.location;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

public class SearchLocationBgService extends Service {

    private Context context;
    public final static String STOP_SERVICE_INTENT_ACTION = "com.ibetter.www.adskitedigi.adskitedigi.location.SearchLocationBgService.StopServiceReceiver";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final static int REQUEST_CHECK_SETTINGS =  2;

    public SearchLocationBgService()
    {
        super();
    }

    public void onCreate()
    {
        super.onCreate();
        context = SearchLocationBgService.this;
        startFrontEndNotification();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        registerStopServiceReceiver();
    }


    private void startFrontEndNotification()
    {
            NotificationModelConstants.displayFrontNotification(this,"Location updates",
                    NotificationModelConstants.LOCATION_SERVICE_ID,"Location updates");
    }


    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public int onStartCommand(Intent intent,int flags,int startId)
    {
        Log.d("Location","Inside SearchLocationBgService on startcommand");
        //check for settings
        checkSettingsAndPermissions();

        return START_STICKY;
    }

    public void onDestroy()
    {
        stopLocationUpdates();
        stopForeground(true);
    }

    private class StopServiceReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            unregisterReceiver(this);
        }
    }

    private void registerStopServiceReceiver()
    {
        IntentFilter intentFilter = new IntentFilter(STOP_SERVICE_INTENT_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(new StopServiceReceiver(),intentFilter);
    }

    public static void stopServiceReceiver(Context context)
    {
      Intent intent = new Intent(STOP_SERVICE_INTENT_ACTION);
      intent.addCategory(Intent.CATEGORY_DEFAULT);
      context.sendBroadcast(intent);
    }

    public static void startLocationService(Context context)
    {
        ContextCompat.startForegroundService(context,new Intent(context,SearchLocationBgService.class));
    }

    public static void restartService(Context context)
    {
        stopServiceReceiver(context);
        startLocationService(context);

    }

    private void checkSettingsAndPermissions()
    {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            startRefreshLocation();
        }else
        {
            Toast.makeText(context,"Cannot check for location updates, please check for permissions and approve",Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }


    private void startRefreshLocation()
    {
            final LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(1*60*1000);
            locationRequest.setFastestInterval(1*60*1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);

                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                       Toast.makeText(context,"Unable to start location updates, please check the settings",
                               Toast.LENGTH_SHORT).show();

                    }else
                    {
                        Toast.makeText(context,"Unable to start location updates,"+e.getMessage(),Toast.LENGTH_LONG).show();

                    }

                    stopSelf();
                }
            });
    }



        private LocationCallback locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        // ...
                        if (location != null) {
                            foundLocation(location);

                            break;
                        }
                    }
                }

            };
        };

    private void stopLocationUpdates()
    {
         if(fusedLocationProviderClient!=null)
         {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
         }
    }

    private void foundLocation(Location location)
    {
        if(User.isPlayerRegistered(context))
        {
            LatLng storedLocation = User.getDeviceLocation(context);
            if(canUpload(location,storedLocation))
            {
                User.updateLocation(this,location);
            }
        }
      Log.d("Location","Found location "+location.getLatitude()+","+location.getLongitude());
    }

    private boolean canUpload(Location newLocation,LatLng storedLocation)
    {
        if(storedLocation!=null)
        {
            Location endPoint=new Location("locationA");
            endPoint.setLatitude(storedLocation.latitude);
            endPoint.setLongitude(storedLocation.longitude);

            Log.d("Location","stored lat,lng "+storedLocation.latitude+","+storedLocation.longitude);

            double distance=newLocation.distanceTo(endPoint);
            Log.d("Location","total distance in meters "+distance);
            long km= ((long)distance/1000);
            return km>=1;
        }else
        {
            return true;
        }
    }

}
