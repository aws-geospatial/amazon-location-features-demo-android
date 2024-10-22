package com.aws.amazonlocation.data.response

data class MapStyleInnerData(
    var mapName: String? = null,
    val provider: String,
    val attributes: List<String>,
    val types: List<String>,
    var isSelected: Boolean = false,
    val image: Int,
    var mMapName: String? = null,
    var mMapStyleName: String? = null
)
