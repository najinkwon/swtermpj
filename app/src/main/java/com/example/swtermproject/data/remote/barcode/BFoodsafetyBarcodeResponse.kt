package com.example.swtermproject.data.remote.barcode

import com.google.gson.annotations.SerializedName

data class BFoodsafetyBarcodeResponse(
    @SerializedName("C005")
    val body: BFoodsafetyBarcodeBody?
)

data class BFoodsafetyBarcodeBody(
    @SerializedName("total_count")
    val totalCount: String?,

    @SerializedName("row")
    val rows: List<BFoodsafetyBarcodeRow>?,

    @SerializedName("RESULT")
    val result: BFoodsafetyBarcodeResult?
)

data class BFoodsafetyBarcodeResult(
    @SerializedName("CODE")
    val code: String?,

    @SerializedName("MSG")
    val message: String?
)

data class BFoodsafetyBarcodeRow(
    @SerializedName("BAR_CD")
    val barcode: String?,

    @SerializedName("PRDLST_NM")
    val productName: String?,

    @SerializedName("BSSH_NM")
    val companyName: String?,

    @SerializedName("PRDLST_DCNM")
    val foodTypeName: String?
)
