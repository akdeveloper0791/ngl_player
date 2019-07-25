package com.ibetter.www.adskitedigi.adskitedigi.nearby;

import android.support.annotation.NonNull;

/**
 * Created by vineethkumar0791 on 28/03/18.
 */

public class Endpoint {
    @NonNull
    private final String id;
    @NonNull
    private final String name;

    public Endpoint(@NonNull String id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;

    }
}
