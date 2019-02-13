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
            @SerializedName("break_duration")
            private Float breakDuration;
            @SerializedName("daily_logs")
            private List<DailyLog> dailyLog;
            @SerializedName("first_in_time")
            private String firstInTime;
            @SerializedName("has_improper_logs")
            private boolean Conflict;

            Float getHoursBurned() {
                return hoursBurned;
            }

            Float getHoursClocked() {
                return hoursClocked;
            }

            Float getBreakDuration() {
                return breakDuration;
            }

            String getFirstInTime() {
                return firstInTime;
            }

            List<DailyLog> getDailyLog() {
                return dailyLog;
            }

            public boolean isConflict() {
                return Conflict;
            }
        }

        class DailyLog {
            @SerializedName("in_out")
            private int inOut;
            @SerializedName("card_swipe_time")
            private String  time;
            @SerializedName("employee_name")
            private String name;

            int getInOut() {
                return inOut;
            }

            String getTime() {
                return time;
            }

            public String getName() {
                return name;
            }
        }
    }
}
