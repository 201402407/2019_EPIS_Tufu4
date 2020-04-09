package com.gaze.rkdus.a2019_epis_tufu4.utils

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable
import java.util.concurrent.TimeUnit


object Prop {

    private const val serverUrl: String = ""

    val client = OkHttpClient.Builder().apply {
        readTimeout(5, TimeUnit.SECONDS)
        writeTimeout(5, TimeUnit.SECONDS)
        connectTimeout(5, TimeUnit.SECONDS)

    }
//    var gson = GsonBuilder()
//            .setLenient()
//            .create()

    // Retrofit 객체 생성
    val retrofit: Retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(serverUrl)
            .client(client.build())
            .build()


    data class EntrpsData(val entrpsCode: Int,  val entrpsName: String, val entrpsRepresentName : String, val entrpsDetailAddr : String,
                          val entrpsTelNo: String, val entrpsAddr: String, var entrpsMemberId: String?) : Serializable
    data class EntrpsJsonData(var ADDR: String, var DETAIL_ADDR: String, var RPRSNTV_NM: String, var ENTRPS_NM: String, var ENTRPS_TELNO: String)

    data class AllEntrpsJsonData(var data : ArrayList<EntrpsJsonData>)
    data class ResultData(val resultCode : String)
}

//private int hospital_key;
//private String ceo_name;
//private String hospital_name;
//private String phone;
//private String address1;
//private String address2;
//private int signup_app;
//private int reservation_count;
//private int review_count;
//private String review_total;
