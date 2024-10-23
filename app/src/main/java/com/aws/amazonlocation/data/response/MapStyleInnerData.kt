package com.aws.amazonlocation.data.response

data class MapStyleInnerData(
    var mapName: String? = null,
    var isSelected: Boolean = false,
    val image: Int,
    var mMapName: String? = null,
    var mMapStyleName: String? = null
)
