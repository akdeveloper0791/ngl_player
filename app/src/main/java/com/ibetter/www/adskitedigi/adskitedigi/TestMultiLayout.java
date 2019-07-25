package com.ibetter.www.adskitedigi.adskitedigi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class TestMultiLayout extends Activity {

    RelativeLayout parentLayout;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_mulitple_layout);
        parentLayout = (RelativeLayout)findViewById(R.id.parent_layout);

        addImageView();
    }

    //add image view
    private void addImageView()
    {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        ImageView image = new ImageView(this);
        image.setLayoutParams(params);
        parentLayout.addView(image);
    }
}
