package com.qburst.qtracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

public class Settings extends AppCompatActivity {

    private SharedPreferences myPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPref = this.getSharedPreferences("myPref", MODE_PRIVATE);
        setContentView(R.layout.activity_settings);
        Switch notificationSwitch = findViewById(R.id.notification_switch);
        if(myPref.getBoolean("disableNotification", false)) {
            notificationSwitch.setChecked(false);
        } else {
            notificationSwitch.setChecked(true);
        }
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    myPref.edit().putBoolean("disableNotification", false).commit();
                } else {
                    myPref.edit().putBoolean("disableNotification", true).commit();
                }
            }
        });
    }
}

