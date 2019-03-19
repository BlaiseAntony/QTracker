package com.qburst.qtracker;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WebService {
    @GET("attendance-tracker/user/monthly-status")
    Call<AttendanceResponse> getData(@Query("month")String month,
                                     @Query("year")String query,
                                     @Header("Authorization")String key);
    @POST("users/login")
    Call<LoginResponse> login(@Body RequestBody requestBody);
}
