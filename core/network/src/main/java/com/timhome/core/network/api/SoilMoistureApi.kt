package com.timhome.core.network.api

import com.timhome.core.network.api.response.SoilMoistureStatusResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface SoilMoistureApi {
    @GET
    suspend fun getData(
        @Url url: String,
    ): Response<SoilMoistureStatusResponse>

    @GET
    suspend fun waterPot(
        @Url url: String,
    ): Response<ResponseBody>
}
