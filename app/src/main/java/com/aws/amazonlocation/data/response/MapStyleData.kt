package com.aws.amazonlocation.data.response

data class MapStyleData(
    var styleNameDisplay: String? = null,
    var isSelected: Boolean = false,
    var mapInnerData: List<MapStyleInnerData>? = null,
    var isDisable: Boolean = false
)
