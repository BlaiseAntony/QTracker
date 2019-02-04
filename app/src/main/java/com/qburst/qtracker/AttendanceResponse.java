package com.qburst.qtracker;

import com.google.gson.annotations.SerializedName;

import java.util.List;

class AttendanceResponse {
    private int status;
    private PayLoad payload;

    public int getStatus() {
        return status;
    }

    PayLoad getPayload() {
        return payload;
    }

    class PayLoad {
        private String region;
        private List<DailyData> monthlyData;

        List<DailyData> getMonthlyData() {
            return monthlyData;
        }

        class DailyData {
            @SerializedName("hours_burned")
            private Float hoursBurned;
            @SerializedName("hours_clocked")
            private Float hoursClocked;
            @SerializedName("breakDuration")
            private Float breakDuration;
            @SerializedName("daily_logs")
            private List<DailyLog> dailyLog;
            @SerializedName("first_in_time")
            private String firstInTime;

            Float getHoursBurned() {
                return hoursBurned;
            }

            Float getHoursClocked() {
                return hoursClocked;
            }

            Float getBreakDuration() {
                return breakDuration;
            }

            public String getFirstInTime() {
                return firstInTime;
            }

            List<DailyLog> getDailyLog() {
                return dailyLog;
            }
        }

        class DailyLog {
            @SerializedName("in_out")
            private int inOut;

            int getInOut() {
                return inOut;
            }
        }
    }
}
