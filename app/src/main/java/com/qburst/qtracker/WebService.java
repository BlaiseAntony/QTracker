package com.qburst.qtracker;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface WebService {
    @GET("monthly-status")
    Call<AttendanceResponse> getData(@Query("month")String month,
                                     @Query("year")String query,
                                     @Header("Authorization")String key);
}
