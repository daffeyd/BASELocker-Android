package com.example.BASELocker

import com.example.httpreq.RequestModel
import com.example.httpreq.ResponseModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiInterface {
    @POST("P_return")
    fun sendReq(@Body requestModel: RequestModel) : Call<ResponseModel>
}
interface ApiRecomendationInterface {
    @POST("P_recommendation")
    fun sendReq(@Body requestModel: RequestModel) : Call<ResponseModel>
}