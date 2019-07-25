package com.ibetter.www.adskitedigi.adskitedigi.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.widget.ListView;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by vineeth_ibetter on 12/30/17.
 */

public class SettingsModel
{
    private Context settingsContext;
    private ListView settingsListView;
    protected SettingsAdaptor settingsAdaptor;

    public SettingsModel(Context settingsContext, ListView settingsListView) {
        this.settingsContext = settingsContext;
        this.settingsListView = settingsListView;
    }

    public Context getSettingsContext() {
        return settingsContext;
    }


    protected void  setValuesListView()
    {
        TypedArray icons;
        String[] settings;
        settings = settingsContext.getResources().getStringArray(R.array.settings);
        icons=settingsContext.getResources().obtainTypedArray(R.array.settings_icons);

        settingsAdaptor=new SettingsAdaptor(settingsContext,R.layout.global_settings_support_view,new ArrayList<String>(Arrays.asList(settings)),icons);
        settingsListView.setAdapter(settingsAdaptor);
    }

}
