package com.aws.amazonlocation.ui.main.mapStyle

import android.content.Context
import androidx.lifecycle.ViewModel
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.LanguageData
import com.aws.amazonlocation.data.response.MapStyleData
import com.aws.amazonlocation.data.response.MapStyleInnerData
import com.aws.amazonlocation.data.response.PoliticalData
import com.aws.amazonlocation.utils.getLanguageData
import com.aws.amazonlocation.utils.getPoliticalData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapStyleViewModel @Inject constructor() : ViewModel() {

    var mStyleList = ArrayList<MapStyleData>()
    var mPoliticalData = ArrayList<PoliticalData>()
    var mPoliticalSearchData = ArrayList<PoliticalData>()
    var mMapLanguageData = ArrayList<LanguageData>()

    fun setMapListData(context: Context) {
        val items =
            arrayListOf(
                MapStyleInnerData(
                    context.getString(R.string.map_standard),
                    false,
                    R.drawable.standard_light
                ),
                MapStyleInnerData(
                    context.getString(R.string.map_monochrome),
                    false,
                    R.drawable.monochrome
                ),
                MapStyleInnerData(
                    context.getString(R.string.map_hybrid),
                    false,
                    R.drawable.hybrid
                ),
                MapStyleInnerData(
                    context.getString(R.string.map_satellite),
                    false,
                    R.drawable.satellite
                )
            )
        mStyleList.clear()

        mStyleList =
            arrayListOf(
                MapStyleData(
                    styleNameDisplay = "",
                    isSelected = false,
                    mapInnerData = items
                )
            )
    }

    fun setPoliticalListData(context: Context) {
        val item = getPoliticalData(context)

        mPoliticalData.addAll(item)

        mPoliticalSearchData.addAll(item)
    }

    fun searchPoliticalData(query: String): ArrayList<PoliticalData> {
        return ArrayList(
            mPoliticalSearchData.filter {
                it.countryName.contains(query, ignoreCase = true)
            }
        )
    }

    fun setMapLanguageData(context: Context) {
        val item = getLanguageData(context)

        mMapLanguageData.clear()

        mMapLanguageData.addAll(item)
    }
}
