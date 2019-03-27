package com.qburst.qtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.PopupMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements DataRecievedListener,
        LoginListener {

    private Handler handler;
    private Runnable timer;
    private SharedPreferences myPref;
    private EditText user;
    private TextView inOut;
    private TextView inTime;
    private TextView burnedTime;
    private TextView clockedTime;
    private TextView breakDuration;
    private TextView salutation;
    private TextView conflict;
    private TextView inoutStatus;
    private TextView leaveMessage;
    private TextView outTime;
    private TextView password;
    private MainApi api;
    private SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPref = this.getSharedPreferences("myPref", MODE_PRIVATE);
        boolean addedKey = myPref.getBoolean("addedKey", false);
        api = new MainApi(this, this, this);
        initViews(addedKey);
        handler = new Handler();
        timer = new Runnable() {
            @Override
            public void run() {
                refresh();
                startTimer();
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
            salutation = findViewById(R.id.salutation);
            conflict = findViewById(R.id.conflict);
            inoutStatus = findViewById(R.id.inoutStatus);
            leaveMessage = findViewById(R.id.leaveMessage);
            outTime = findViewById(R.id.outTimeValue);
            swipe = findViewById(R.id.swipe);
            swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refresh();
                }
            });
            refresh();
        }
        else {
            setContentView(R.layout.enter_auth_key);
            user = findViewById(R.id.user);
            password = findViewById(R.id.password);
            Button submit = findViewById(R.id.login);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userT = user.getText().toString();
                    String passwordT = password.getText().toString();
                    if(userT.equals("") || passwordT.equals("")) {
                        popUp("The username or password can't be empty");
                    }
                    else {
                        api.Login(userT,passwordT);
                    }
                }
            });
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    private void popUp(String str) {
        Snackbar.make(findViewById(android.R.id.content), str, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
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

    private void startService() {
        if(myPref.getBoolean("addedKey", false)) {
            Intent intent = new Intent(this, ShowNotification.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

    private void refresh() {
        if(myPref.getBoolean("addedKey", false)) {
            swipe.setRefreshing(true);
            api.getData();
        }
    }

    private void checkForOutTime(boolean IsLeaveTime, boolean conflic) {
        if(IsLeaveTime) {
            if (conflic) {
                leaveMessage.setVisibility(View.VISIBLE);
                leaveMessage.setText("You have conflicts");
            } else {
                leaveMessage.setVisibility(View.VISIBLE);
                leaveMessage.setText("You can leave now");
            }
        } else {
            leaveMessage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResponseReceived(String name, String log, boolean conflic, int inOrOut, String firstIn,
                                   String burned, String clocked, String breakDur, boolean IsLeaveTime,
                                   String outTim) {
        stopTimer();
        salutation.setText("Hi " + name + ",");
        inOut.setText(log);
        if (inOrOut == 0) {
            inoutStatus.setText("IN");
            inoutStatus.setTextColor(Color.GREEN);
        } else {
            inoutStatus.setText("OUT");
            inoutStatus.setTextColor(Color.RED);
        }
        inTime.setText(firstIn);
        burnedTime.setText(burned);
        clockedTime.setText(clocked);
        breakDuration.setText(breakDur);
        if (conflic) {
            conflict.setVisibility(View.VISIBLE);
            outTime.setText(outTim+"*");
        } else {
            outTime.setText(outTim);
        }
        swipe.setRefreshing(false);
        checkForOutTime(IsLeaveTime, conflic);
        if (!myPref.getBoolean("notificationShown", false) &&
                !myPref.getBoolean("disableNotification", false)) {
            startService();
        }
        startTimer();
    }

    @Override
    public void onFailure() {
        swipe.setRefreshing(false);
        inOut.setText("--");
        inTime.setText("--");
        burnedTime.setText("--");
        clockedTime.setText("--");
        breakDuration.setText("--");
        outTime.setText("--");
        inoutStatus.setText("");
        leaveMessage.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSuccess(String accessToken) {
        SharedPreferences.Editor editor = myPref.edit();
        editor.putString("key", accessToken);
        editor.putBoolean("addedKey", true);
        editor.commit();
        initViews(true);
    }

    @Override
    public void onFailure1() {
        popUp("Login failed please try after sometime");
    }

    public void createOptionsMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.options_menu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.settings:
                        openSettings();
                        return true;
                    case R.id.refresh:
                        refresh();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }
}