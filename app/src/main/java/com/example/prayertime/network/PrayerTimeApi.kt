package com.example.prayertime.network

import com.example.prayertime.model.MonthlyPrayerTimesResponse
import com.example.prayertime.model.PrayerTimesResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PrayerTimeApiService {
    @GET("v1/timings/{date}")
    suspend fun getDailyTimings(
        @Path("date") date: String, // DD-MM-YYYY
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 3 // ISNA
    ): PrayerTimesResponse

    @GET("v1/calendar")
    suspend fun getMonthlyTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 3,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): MonthlyPrayerTimesResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://api.aladhan.com/"

    val apiService: PrayerTimeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PrayerTimeApiService::class.java)
    }
}
//
//lat: 36.6432167
//long: -4.6841312
