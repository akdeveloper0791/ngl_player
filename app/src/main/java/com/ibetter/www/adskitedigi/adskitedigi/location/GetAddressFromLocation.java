package com.ibetter.www.adskitedigi.adskitedigi.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;

public class GetAddressFromLocation extends IntentService {

    private String intentAction;

    public GetAddressFromLocation()
    {
        super(GetAddressFromLocation.class.getName());
    }

    public void onHandleIntent(Intent intent)
    {
        //get Location
        Location location = (Location)intent.getParcelableExtra("location");
        intentAction=intent.getStringExtra("intent_action");
        List<Address> addresses = null;

        try {
            Geocoder geocoder = new Geocoder(this);
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            sendResponse(false,"Service not available");
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.

            sendResponse(false,"Invalid lat and lng");
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            sendResponse(false,"Unable to get the address");
        } else {
            Address address = addresses.get(0);
            StringBuilder addressFragments = new StringBuilder();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.append(address.getAddressLine(i));
            }

            sendResponse(true,addressFragments.toString());
        }
    }

    private void sendResponse(boolean flag,String status)
    {
        Intent intent = new Intent(intentAction);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("flag",flag);
        intent.putExtra("status",status);
        sendBroadcast(intent);
    }
}
