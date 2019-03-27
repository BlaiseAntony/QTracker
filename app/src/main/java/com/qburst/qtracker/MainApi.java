package com.qburst.qtracker;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

class MainApi {

    private Context context;
    private DataRecievedListener listener;
    private LoginListener loginListener;
    private int currentMinute;
    private int currentHour;
    private int inHour;
    private int inMinute;
    private int breakMinute;
    private int breakHour;
    private WebService service;

    MainApi (Context context, DataRecievedListener listener, LoginListener loginListener) {
        this.context = context;
        this.listener = listener;
        this.loginListener = loginListener;
        service = ApiClient.getClient().create(WebService.class);
    }

    MainApi(Context context, DataRecievedListener listener) {
        this.context = context;
        this.listener = listener;
        service = ApiClient.getClient().create(WebService.class);
    }
    void getData() {
        SharedPreferences myPref = context.getSharedPreferences("myPref", MODE_PRIVATE);
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        String key = myPref.getString("key", "");
        if(calendar.get(Calendar.DAY_OF_MONTH) != myPref.getInt("month",0)) {
            myPref.edit().putBoolean("notificationShown", false).commit();
            myPref.edit().putInt("month", calendar.get(Calendar.DAY_OF_MONTH)).commit();
        }
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinute = calendar.get(Calendar.MINUTE);
        Call<AttendanceResponse> call = service.getData(String.valueOf(calendar.get(Calendar.MONTH) + 1),
                String.valueOf(calendar.get(Calendar.YEAR)), key);
        call.enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> res) {
                if (res.isSuccessful()) {
                    if (res.body() != null) {
                        AttendanceResponse.PayLoad.DailyLog log;
                        AttendanceResponse.PayLoad.DailyData data;
                        List<AttendanceResponse.PayLoad.DailyData> dailyDataList =
                                res.body().getPayload().getMonthlyData();
                        int k = dailyDataList.size() - 1;
                        if (k > -1) {
                            data = dailyDataList.get(k);
                            int m = data.getDailyLog().size() - 1;
                            if (m > -1) {
                                log = data.getDailyLog().get(m);
                            } else {
                                log = null;
                            }
                        } else {
                            data = null;
                            log = null;
                        }
                        if (data != null  && log != null) {
                            String a = stringToTime(log.getTime(), false);
                            String b = stringToTime(data.getFirstInTime(), true);
                            String c = String.valueOf(changeToTime(data.getHoursBurned())) + " Hrs";
                            String d = String.valueOf(changeToTime(data.getHoursClocked())) + " Hrs";
                            String e = String.valueOf(minuteToHour(data.getBreakDuration())) + " Hrs";
                            int breakMin = breakHour*60 + breakMinute;
                            int additionalTime;
                            if(breakMin > 60) {
                                additionalTime = breakMin - 60;
                            } else {
                                additionalTime = 0;
                            }
                            int expectedOut = 540 + additionalTime;
                            String f = convertToTime(expectedOut);
                            int clocked = (currentHour - inHour)*60 + currentMinute - inMinute;
                            int burned = (currentHour - inHour - breakHour)*60 + currentMinute - inMinute - breakMinute;
                            String name[] = log.getName().split(" ");
                            listener.onResponseReceived(name[0], a,
                                    data.isConflict(), log.getInOut(), b, c, d, e,
                                    clocked > 540 && burned > 480 && log.getInOut() == 0, f);
                        } else {
                            listener.onFailure();
                        }
                    } else {
                        listener.onFailure();
                    }
                } else  {
                    listener.onFailure();
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                listener.onFailure();
            }
        });
    }

    private String convertToTime(int requiredInTime) {
        int expectedOutHour = inHour + requiredInTime/60;
        int expectedOutMin = inMinute + requiredInTime%60;
        if (expectedOutMin > 59) {
            expectedOutHour += 1;
            expectedOutMin -= 60;
        }
        return changeToNormalClock(expectedOutHour, expectedOutMin);
    }

    private String changeToNormalClock(int hour, int minute) {
        String amOrpm;
        if (hour > 12) {
            hour-=12;
            amOrpm = "PM";
        } else  {
            amOrpm ="AM";
        }
        return intToString(hour) + ":" + intToString(minute) + " " + amOrpm;
    }

    private String changeToTime(Float time) {
        int hour = (int) Math.floor(time);
        int minute = (int) Math.floor((time * 60) % 60);
        return intToString(hour) + ":" + intToString(minute);
    }
    private String minuteToHour(Float time) {
        int hour = (int) (time/60);
        int minute = (int) (time%60);
        breakHour = hour;
        breakMinute = minute;
        return intToString(hour) + ":" + intToString(minute);
    }
    private String stringToTime(String firstInTime, Boolean isFirstInTime) {
        String[] a = firstInTime.split("T");
        String[] a2 = a[1].split(":");
        int hour = Integer.parseInt(a2[0]) + 5;
        int minute = Integer.parseInt(a2[1]) + 30;
        if(minute >= 60) {
            hour += 1;
            minute -= 60;
        }
        if(isFirstInTime) {
            inHour = hour;
            inMinute = minute;
        }
        return changeToNormalClock(hour, minute);
    }

    private String intToString(int num) {
        return String.format(Locale.getDefault(),"%02d", num);
    }

    private static String generateRandom(String aToZ) {
        Random rand=new Random();
        StringBuilder res=new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randIndex=rand.nextInt(aToZ.length());
            res.append(aToZ.charAt(randIndex));
        }
        return res.toString();
    }

    public void Login(String userT, String passwordT) {
        RequestBody requestBody = new FormBody.Builder()
                .add("clientId",generateRandom("abcdefghijklmnopqrstuvwxyz0123456789"))
                .add("userId",userT).add("password",passwordT).build();
        Call<LoginResponse> call = service.login(requestBody);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response.isSuccessful()) {
                    if(response.body() != null) {
                        LoginResponse resp = response.body();
                        if(resp.getStatus() == 0) {
                            try {
                                loginListener.onSuccess(resp.getPayload().getAccessToken().getToken());
                            } catch (NullPointerException e) {
                                loginListener.onFailure1();
                            }
                        } else {
                            loginListener.onFailure1();
                        }
                    } else {
                        loginListener.onFailure1();
                    }
                } else {
                    loginListener.onFailure1();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loginListener.onFailure1();
            }
        });
    }
}
