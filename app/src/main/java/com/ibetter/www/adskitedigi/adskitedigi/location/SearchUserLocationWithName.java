package com.ibetter.www.adskitedigi.adskitedigi.location;

import android.app.IntentService;
import android.content.Intent;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class SearchUserLocationWithName extends IntentService {

    private String intentAction;

    public SearchUserLocationWithName() {
        super("SearchUserLocationWithName");
    }

    protected void onHandleIntent(Intent intent) {
        System.out.println("this searchuserlocation by name");
        intentAction = intent.getStringExtra("intentAction");
        String address = intent.getStringExtra("address");
        try {

            String URL = "https://maps.googleapis.com/maps/api/geocode/json?address="+address+ "&key="+ getString(R.string.google_map_key);

            com.squareup.okhttp.OkHttpClient httpclient = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(URL)
                    .build();

            Response response =httpclient.newCall(request).execute();
            String result = response.body().string();
            processResult(result);

        } catch(IOException e)
        {
            sendResponse(false, 0, 0, "No internet connection","","");
            //Toast.makeText(SearchUserLocationWithName.this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
            sendResponse(false, 0, 0, "Unable to find location","","");
        }
    }

    private void processResult(String result) throws Exception {

        System.out.println("place search result:"+result);

        JSONObject jsonObject = new JSONObject(result);
        String status = jsonObject.getString("status");
        if (status.equals("OK")) {
            JSONArray resultsArray = jsonObject.getJSONArray("results");
            JSONObject resultObject = resultsArray.getJSONObject(0);
            JSONArray addressArray = resultObject.getJSONArray("address_components");

            String streetName=getStreetName(addressArray);
            String cityName=getCityName(addressArray);
            System.out.println("city Name"+cityName);
            System.out.println("streetName" + streetName);

            String formatted_address = resultObject.getString("formatted_address");

            JSONObject geometry = resultObject.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            double lat = (location.getDouble("lat"));
            double lng = (location.getDouble("lng"));
            System.out.println("flag" );
            System.out.println("lattitude"+lat);
            System.out.println("longitude"+lng);

            ///get street name and city name
            sendResponse(true,lat, lng,formatted_address,cityName,streetName);

        }else if(status.equals("OVER_QUERY_LIMIT"))
        {
            String errorMsg=jsonObject.getString("error_message");
            sendResponse(false, 0, 0, errorMsg,"","");
        }
        else {
            sendResponse(false, 0, 0, "No Address found","","");
        }
    }
    private String getCityName(JSONArray addressArray)  throws Exception
    {

        String cityName="";
        for(int i=0;i<addressArray.length();i++) {

            JSONObject addresssObject = addressArray.getJSONObject(i);
            JSONArray localityArray= addresssObject.getJSONArray("types");
            for(int j=0;j<localityArray.length();j++)

            {
                JSONObject loaclityArrayObject = addressArray.getJSONObject(j);

                if(localityArray.toString().contains("locality"))
                {
                    cityName = addresssObject.getString("long_name");
                    break;
                }
            }


        }

        return cityName;
    }

    private String getStreetName(JSONArray addressArray)  throws Exception
    {
        System.out.println("place search result:"+"inside get street name");
        String streetName="N/A";
        for(int i=0;i<addressArray.length();i++) {

            JSONObject addresssObject = addressArray.getJSONObject(i);
            JSONArray localityArray= addresssObject.getJSONArray("types");

            System.out.println("place search result"+"street name locality array string conversion:"+localityArray.toString());

            for(int j=0;j<localityArray.length();j++)
            {
               // JSONObject loaclityArrayObject = addressArray.getJSONObject(j);
                System.out.println("place search result:"+"inside locality array for loop");

                try {

                    System.out.println("place search result:"+"inside comparing locality array string :"+localityArray.getString(i));
                   if(localityArray.toString().contains("sublocality"))
                    {
                        System.out.println("place search result:"+"csublocality condition is true");
                        String shortName=addresssObject.getString("short_name");
                        System.out.println("place search result:"+"short name:"+shortName);
                        if(shortName==null)
                        {
                            String longName=addresssObject.getString("long_name");
                            System.out.println("place search result:"+"short name is null:, long name"+longName);
                            if(longName!=null)
                            {
                                return longName;

                            }
                        }else
                        {
                           return shortName;

                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                      return "N/A";
                }
                /*if(localityArray.toString().contains("sublocality"))
                {
                    streetName = addresssObject.getString("long_name");
                    break;
                }*/

            }


        }

        System.out.println("place search result:"+"no street name found:");
        return streetName;
    }
    private void sendResponse(boolean flag, double lat, double lang, String address, String cityName, String streetName) {
        Intent intent = new Intent(intentAction);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("flag", flag);
        intent.putExtra("lat", lat);
        intent.putExtra("lng",lang);
        System.out.println("flag" + flag);
        System.out.println("lattitude" + lat);
        System.out.println("longitude"+lang);
        intent.putExtra("cityName", cityName);
        intent.putExtra("streetName", streetName);

        if(flag) {
            intent.putExtra("address", address);
        }else {
            intent.putExtra("reason", address);
        }
        sendBroadcast(intent);

    }
}
