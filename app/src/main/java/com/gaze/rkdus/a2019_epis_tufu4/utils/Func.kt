package com.gaze.rkdus.a2019_epis_tufu4.utils

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// 1. RETROFIT(모든 회사 정보 얻기)
interface GetAllEntrpsInfoService {
    @GET("/entrps/")
    fun resultRepos() : Single<ArrayList<Prop.EntrpsData>>
}

interface AddEntrpsInfoService {
    @POST("/entrps/addEntrpsJson")
    fun resultRepos(@Body params: Prop.AllEntrpsJsonData) : Single<Prop.ResultData>
}


