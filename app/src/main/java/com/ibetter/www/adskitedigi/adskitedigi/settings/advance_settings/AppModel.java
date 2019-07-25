package com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings;

import android.graphics.drawable.Drawable;

public class AppModel
{
    private String name,packageName;
    private Drawable appIcon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
}
