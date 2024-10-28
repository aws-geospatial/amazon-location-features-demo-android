package com.aws.amazonlocation.data.response


data class PoliticalData(
    val countryName: String,
    val description: String,
    val countryCode: String,
    var isSelected: Boolean = false
)