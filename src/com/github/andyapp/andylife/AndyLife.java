package com.github.andyapp.andylife;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class AndyLife extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_andy_life);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_andy_life, menu);
        return true;
    }
}
