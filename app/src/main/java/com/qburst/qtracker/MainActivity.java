package com.qburst.qtracker;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable timer;
    private SharedPreferences myPref;
    private EditText authKey;
    private TextView inOut;
    private TextView inTime;
    private TextView burnedTime;
    private TextView clockedTime;
    private TextView breakDuration;
    private Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPref = this.getSharedPreferences("myPref", MODE_PRIVATE);
        boolean addedKey = myPref.getBoolean("addedKey", false);
        initViews(addedKey);
        handler = new Handler();
        timer = new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        };
    }

    private void initViews(boolean b) {
        if (b) {
            setContentView(R.layout.activity_main);
            inOut = findViewById(R.id.inOut);
            inTime = findViewById(R.id.inTimeValue);
            breakDuration = findViewById(R.id.breakDurationValue);
            clockedTime = findViewById(R.id.clockedTimeValue);
            burnedTime = findViewById(R.id.burnedTimeValue);
            Button reEnterKey = findViewById(R.id.button);
            reEnterKey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myPref.edit().putBoolean("addedKey", false).apply();
                    initViews(false);
                }
            });
        }
        else {
            setContentView(R.layout.enter_auth_key);
            authKey = findViewById(R.id.key);
            Button submit = findViewById(R.id.submit);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String key = authKey.getText().toString();
                    if(key.equals("")) {
                        Toast.makeText(MainActivity.this,
                                "The key can't be empty",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        SharedPreferences.Editor editor = myPref.edit();
                        editor.putString("key", key);
                        editor.putBoolean("addedKey", true);
                        editor.apply();
                        initViews(true);
                    }
                }
            });
        }
    }

    private void startTimer() {
        handler.postDelayed(timer,20000);
    }

    private void stopTimer() {
        handler.removeCallbacks(timer);
    }

    @Override
    protected void onResume() {
        refresh();
        startTimer();
        super.onResume();
    }

    @Override
    protected void onPause() {
        stopTimer();
        super.onPause();
    }
    private String changeToTime(Float time) {
        int hour = (int) Math.floor(time);
        int minute = (int) Math.floor((time * 60) % 60);
        String totalTime = String.valueOf(hour) + ":" + String.valueOf(minute);
        return totalTime;
    }
    private void refresh() {
        calendar = Calendar.getInstance(TimeZone.getDefault());
        String key = myPref.getString("key","");
        WebService service = ApiClient.getClient().create(WebService.class);
        Call<AttendanceResponse> call = service.getData(String.valueOf(calendar.get(Calendar.MONTH) + 1),
                String.valueOf(calendar.get(Calendar.YEAR)), key);
        call.enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> res) {
                if (res.isSuccessful()) {
                    if(res.body() != null) {
                    List<AttendanceResponse.PayLoad.DailyData> dailyDataList =
                            res.body().getPayload().getMonthlyData();
                        int k = dailyDataList.size() - 1;
                        if(k > -1) {
                            AttendanceResponse.PayLoad.DailyData data = dailyDataList.get(k);
                            int m = data.getDailyLog().size() - 1;
                            if(m > -1) {
                                AttendanceResponse.PayLoad.DailyLog log = data.getDailyLog().get(m);
                                if(log.getInOut() == 0) {
                                    inOut.setText("IN");
                                }
                                else {
                                    inOut.setText("OUT");
                                }
                            }
                            inTime.setText(findStartTime(data.getFirstInTime()));
                            burnedTime.setText(String.valueOf(data.getHoursBurned()));
                            clockedTime.setText(String.valueOf(data.getHoursClocked()));
                            breakDuration.setText(String.valueOf(data.getBreakDuration()));
                        }
                        else {
                            setDash();
                        }
                    }
                    else {
                        setDash();
                    }
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                setDash();
            }
        });
    }

    private String findStartTime(String firstInTime) {
        String[] a = firstInTime.split("T");
        String[] a2 = a[1].split(":");
        int hour = Integer.parseInt(a2[0]) + 5;
        int minute = Integer.parseInt(a2[0]) + 30;
        if(minute >= 60) {
            hour += 1;
            minute -= 60;
        }
        return String.valueOf(hour) + ":" + String.valueOf(minute);
    }

    private void setDash() {
        if(burnedTime.getText() == null) {
            burnedTime.setText("--");
        }
        if(clockedTime.getText() == null) {
            clockedTime.setText("--");
        }
        if(breakDuration.getText() == null) {
            breakDuration.setText("--");
        }
    }
}