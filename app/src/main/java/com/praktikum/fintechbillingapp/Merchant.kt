package com.praktikum.fintechbillingapp
import com.google.gson.annotations.SerializedName
data class Merchant(
    @SerializedName("merchant_id")
    val merchantId: String,
    @SerializedName("merchant_name")
    val merchantName: String,
    @SerializedName("city_location")
    val cityLocation: String,
    @SerializedName("is_verified")
    val isVerified: Boolean = false
)