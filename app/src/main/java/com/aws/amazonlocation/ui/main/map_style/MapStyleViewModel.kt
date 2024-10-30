package com.aws.amazonlocation.ui.main.map_style

import android.content.Context
import androidx.lifecycle.ViewModel
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.MapStyleData
import com.aws.amazonlocation.data.response.MapStyleInnerData
import com.aws.amazonlocation.data.response.PoliticalData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapStyleViewModel @Inject constructor() : ViewModel() {

    var mStyleList = ArrayList<MapStyleData>()
    var mPoliticalData = ArrayList<PoliticalData>()
    var mPoliticalSearchData = ArrayList<PoliticalData>()

    fun setMapListData(context: Context) {
        val items =
            arrayListOf(
                MapStyleInnerData(
                    context.getString(R.string.map_standard),
                    false,
                    R.drawable.standard_light,
                ),
                MapStyleInnerData(
                    context.getString(R.string.map_monochrome),
                    false,
                    R.drawable.monochrome,
                ),
                MapStyleInnerData(
                    context.getString(R.string.map_hybrid),
                    false,
                    R.drawable.hybrid,
                ),
                MapStyleInnerData(
                    context.getString(R.string.map_satellite),
                    false,
                    R.drawable.satellite,
                ),
            )
        mStyleList.clear()

        mStyleList =
            arrayListOf(
                MapStyleData(
                    styleNameDisplay = "",
                    isSelected = false,
                    mapInnerData = items,
                ),
            )
    }

    fun setPoliticalListData(context: Context) {
        val item = arrayListOf(
            PoliticalData(
                countryName = context.getString(R.string.label_arg),
                description = context.getString(R.string.description_arg),
                countryCode = context.getString(R.string.flag_arg),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_egy),
                description = context.getString(R.string.description_egy),
                countryCode = context.getString(R.string.flag_egy),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_ind),
                description = context.getString(R.string.description_ind),
                countryCode = context.getString(R.string.flag_ind),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_ken),
                description = context.getString(R.string.description_ken),
                countryCode = context.getString(R.string.flag_ken),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_mar),
                description = context.getString(R.string.description_mar),
                countryCode = context.getString(R.string.flag_mar),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_rus),
                description = context.getString(R.string.description_rus),
                countryCode = context.getString(R.string.flag_rus),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_sdn),
                description = context.getString(R.string.description_sdn),
                countryCode = context.getString(R.string.flag_sdn),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_srb),
                description = context.getString(R.string.description_srb),
                countryCode = context.getString(R.string.flag_srb),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_sur),
                description = context.getString(R.string.description_sur),
                countryCode = context.getString(R.string.flag_sur),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_syr),
                description = context.getString(R.string.description_syr),
                countryCode = context.getString(R.string.flag_syr),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_tur),
                description = context.getString(R.string.description_tur),
                countryCode = context.getString(R.string.flag_tur),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_tza),
                description = context.getString(R.string.description_tza),
                countryCode = context.getString(R.string.flag_tza),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_ury),
                description = context.getString(R.string.description_ury),
                countryCode = context.getString(R.string.flag_ury),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_vnm),
                description = context.getString(R.string.description_vnm),
                countryCode = context.getString(R.string.flag_vnm),
            )
        )
        mPoliticalData.addAll(item)

        mPoliticalSearchData.addAll(item)
    }

    fun searchPoliticalData(query: String): ArrayList<PoliticalData> {
        return ArrayList(mPoliticalSearchData.filter {
            it.countryName.contains(query, ignoreCase = true)
        })
    }
}
