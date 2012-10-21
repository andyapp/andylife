package com.github.andyapp.andylife;

import java.util.List;

import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.annotation.TargetApi;
import android.os.Bundle;

public class AndyLifePreferences extends PreferenceActivity {

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
	}

//    @Override
//    public void onBuildHeaders(List<Header> target) {
//        loadHeadersFromResource(R.xml.preferences_headers, target);
//    }	
//	
//	public static class PrefsFragment extends PreferenceFragment {
//
//	    @Override
//	    public void onCreate(Bundle savedInstanceState) {
//	        super.onCreate(savedInstanceState);
//
//	        // Load the preferences from an XML resource
//	        addPreferencesFromResource(R.xml.preferences);
//	    }
//	}	
	
}
