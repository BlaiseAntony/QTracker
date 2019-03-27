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
    private String firstIn;
    private String out;
    private boolean conflict;

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
        handler.postDelayed(timer,100000);
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
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onResponseReceived(String name, String log, boolean conflict,
                                   int inOrOut, String firstIn, String burned, String clocked,
                                   String breakDur, boolean IsLeaveTime, String out) {
        stopTimer();
        this.IsLeaveTime = IsLeaveTime;
        this.burned = burned;
        this.clocked = clocked;
        this.breakDur = breakDur;
        this.firstIn = firstIn;
        this.out = out;
        this.conflict = conflict;
        if (IsLeaveTime) {
            createLeaveNotification(conflict, burned, clocked, breakDur);
        } else {
            createPermNotification(inOrOut, log,conflict, firstIn, out);
        }
        startTimer();
    }

    @Override
    public void onFailure() {
        if (IsLeaveTime) {
            createLeaveNotification(conflict, burned, clocked, breakDur);
        } else {
            createPermNotification(-1, "Can't refresh data",conflict, firstIn, out);
        }
    }

    private void createLeaveNotification(Boolean conflict, String burned, String clocked, String breakDur) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "102");
        PendingIntent resultIntent = PendingIntent.getActivity(this, 101,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        String  title;
        if (conflict) {
            title = "You have conflicts";
        } else {
            title = "You can leave now";
        }
        mBuilder.setContentTitle(title)
                .setContentText("Burned : " + burned + " | Clocked : " + clocked + " | Break : " + breakDur)
                .setOnlyAlertOnce(true)
                .setColor(getResources().getColor(R.color.green))
                .setContentIntent(resultIntent)
                .setSmallIcon(R.mipmap.ic_stat_onesignal_default);
        Notification notification = mBuilder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(102, notification);
        myPref.edit().putBoolean("notificationShown", true).commit();
        stopTimer();
        stopForeground(true);
        notificationManager.cancel(101);
        stopSelf();
    }

    private void createPermNotification(int inOrOut, String s, Boolean conflict, String firstIn, String out) {
        if(myPref.getBoolean("disableNotification", false)) {
            stopTimer();
            stopForeground(true);
            stopSelf();
        } else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "101");
            PendingIntent resultIntent = PendingIntent.getActivity(this, 101,
                    new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            String title;
            if (inOrOut == 0) {
                title = "IN";
            } else if (inOrOut == 1) {
                title = "OUT";
            } else {
                title = s;
            }
            String outTime;
            if(conflict) {
                outTime = out+"(may vary since you have conflicts)";
            } else {
                outTime = out;
            }
            mBuilder.setContentTitle(title)
                    .setContentText("Recent : " + s + " | Expected Out Time : " + outTime)
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
