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
        val items = arrayListOf(
            MapStyleInnerData(
                context.getString(R.string.map_standard),
                false,
                R.drawable.light
            ),
            MapStyleInnerData(
                context.getString(R.string.map_monochrome),
                false,
                R.drawable.streets
            ),
            MapStyleInnerData(
                context.getString(R.string.map_hybrid),
                false,
                R.drawable.navigation
            ),
            MapStyleInnerData(
                context.getString(R.string.map_satellite),
                false,
                R.drawable.dark_gray
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
                description = "Argentina's view on the Southern Patagonian Ice Field and Tierra Del Fuego, including the Falkland Islands, South Georgia, and South Sandwich Islands",
                countryCode = context.getString(R.string.flag_arg),
            ),
            PoliticalData(
                countryName = "EGY",
                description = "Egypt's view on Bir Tawil",
                countryCode = context.getString(R.string.flag_egy),
            ),
            PoliticalData(
                countryName = "IND",
                description = "India's view on Gilgit-Baltistan",
                countryCode = context.getString(R.string.flag_ind),
            ),
            PoliticalData(
                countryName = "KEN",
                description = "Kenya's view on the Ilemi Triangle",
                countryCode = context.getString(R.string.flag_ken),
            ),
            PoliticalData(
                countryName = "MAR",
                description = "Morocco's view on Western Sahara",
                countryCode = context.getString(R.string.flag_mar),
            ),
            PoliticalData(
                countryName = "PAK",
                description = "Pakistan's view on Jammu and Kashmir and the Junagadh Area",
                countryCode = context.getString(R.string.flag_pak),
            ),
            PoliticalData(
                countryName = "RUS",
                description = "Russia's view on Crimea",
                countryCode = context.getString(R.string.flag_rus),
            ),
            PoliticalData(
                countryName = "SDN",
                description = "Sudan's view on the Halaib Triangle",
                countryCode = context.getString(R.string.flag_sdn),
            ),
            PoliticalData(
                countryName = "SRB",
                description = "Serbia's view on Kosovo, Vukovar, and Sarengrad Islands",
                countryCode = context.getString(R.string.flag_srb),
            ),
            PoliticalData(
                countryName = "SUR",
                description = "Suriname's view on the Courantyne Headwaters and Lawa Headwaters",
                countryCode = context.getString(R.string.flag_sur),
            ),
            PoliticalData(
                countryName = "SYR",
                description = "Syria's view on the Golan Heights",
                countryCode = context.getString(R.string.flag_syr),
            ),
            PoliticalData(
                countryName = "TUR",
                description = "Turkey's view on Cyprus and Northern Cyprus",
                countryCode = context.getString(R.string.flag_tur),
            ),
            PoliticalData(
                countryName = "TZA",
                description = "Tanzania's view on Lake Malawi",
                countryCode = context.getString(R.string.flag_tza),
            ),
            PoliticalData(
                countryName = "URY",
                description = "Uruguay's view on Rincon de Artigas",
                countryCode = context.getString(R.string.flag_ury),
            ),
            PoliticalData(
                countryName = "VNM",
                description = "Vietnam's view on the Paracel Islands and Spratly Islands",
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
