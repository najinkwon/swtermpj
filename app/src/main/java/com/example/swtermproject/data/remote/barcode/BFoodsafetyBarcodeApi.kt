package com.example.swtermproject.data.remote.barcode

import retrofit2.http.GET
import retrofit2.http.Path

interface BFoodsafetyBarcodeApi {
    @GET("{apiKey}/C005/json/1/5/BAR_CD={barcode}")
    suspend fun searchBarcode(
        @Path(value = "apiKey", encoded = true) apiKey: String,
        @Path("barcode") barcode: String
    ): BFoodsafetyBarcodeResponse
}
