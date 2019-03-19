package com.qburst.qtracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class ShowNotification extends Service implements DataRecievedListener {

    private Handler handler;
    private Runnable timer;
    private String burned;
    private String clocked;
    private String breakDur;
    private boolean IsLeaveTime;
    private SharedPreferences myPref;

    private void refresh() {
        MainApi api = new MainApi(this, this);
        api.getData();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myPref = this.getSharedPreferences("myPref", MODE_PRIVATE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel("101",
                    "QTracker", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setSound(null,null);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(false);
            NotificationChannel notificationChannel1 = new NotificationChannel("102",
                    "Leave Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel1.enableLights(true);
            notificationChannel1.setLightColor(Color.RED);
            notificationChannel1.enableVibration(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.createNotificationChannel(notificationChannel1);
        }
    }

    private void startTimer() {
        handler.postDelayed(timer,20000);
    }

    private void stopTimer() {
        handler.removeCallbacks(timer);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler(Looper.getMainLooper());
        timer = new Runnable() {
            @Override
            public void run() {
                refresh();
                startTimer();
            }
        };
        refresh();
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopTimer();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onResponseReceived(String name, String log, boolean conflict,
                                   int inOrOut, String firstIn, String burned, String clocked,
                                   String breakDur, boolean IsLeaveTime, String out) {
        this.IsLeaveTime = IsLeaveTime;
        this.burned = burned;
        this.clocked = clocked;
        this.breakDur = breakDur;
        if (IsLeaveTime) {
            createLeaveNotification(burned, clocked, breakDur);
        } else {
            createPermNotification("InTime:"+firstIn, burned, clocked, breakDur);
        }
    }

    @Override
    public void onFailure() {
        if (IsLeaveTime) {
            createLeaveNotification(burned, clocked, breakDur);
        } else {
            createPermNotification("Can't refresh data", burned, clocked, breakDur);
        }
    }

    private void createLeaveNotification(String burned, String clocked, String breakDur) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "102");
        PendingIntent resultIntent = PendingIntent.getActivity(this, 101,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentTitle("You can leave now")
                .setContentText("Burned:" + burned + " :: Clocked:" + clocked + " :: Break:" + breakDur)
                .setOnlyAlertOnce(true)
                .setColor(getResources().getColor(R.color.green))
                .setContentIntent(resultIntent)
                .setSmallIcon(R.mipmap.ic_stat_onesignal_default);
        Notification notification = mBuilder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(102, notification);
        stopForeground(true);
        notificationManager.cancel(101);
        myPref.edit().putBoolean("notificationShown", true).commit();
        onDestroy();
    }

    private void createPermNotification(String firstIn, String burned, String clocked,
                                        String breakDur) {
        if(myPref.getBoolean("disableNotification", false)) {
            stopForeground(true);
            onDestroy();
        } else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "101");
            PendingIntent resultIntent = PendingIntent.getActivity(this, 101,
                    new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentTitle(firstIn)
                    .setContentText("Burned:" + burned + " :: Clocked:" + clocked + " :: Break:" + breakDur)
                    .setSmallIcon(R.mipmap.ic_stat_onesignal_default)
                    .setOnlyAlertOnce(true)
                    .setColor(getResources().getColor(R.color.red))
                    .setSound(null)
                    .setContentIntent(resultIntent);
            Notification notification = mBuilder.build();
            startForeground(101, notification);
        }
    }
}
