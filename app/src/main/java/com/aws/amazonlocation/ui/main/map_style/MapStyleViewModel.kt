package com.aws.amazonlocation.ui.main.map_style

import android.content.Context
import androidx.lifecycle.ViewModel
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.MapStyleData
import com.aws.amazonlocation.data.response.MapStyleInnerData
import com.aws.amazonlocation.ui.main.explore.FilterOption
import com.aws.amazonlocation.utils.ATTRIBUTE_3D
import com.aws.amazonlocation.utils.ATTRIBUTE_DARK
import com.aws.amazonlocation.utils.ATTRIBUTE_LIGHT
import com.aws.amazonlocation.utils.ATTRIBUTE_SATELLITE
import com.aws.amazonlocation.utils.ATTRIBUTE_TRUCK
import com.aws.amazonlocation.utils.MapNames
import com.aws.amazonlocation.utils.MapStyles
import com.aws.amazonlocation.utils.TYPE_RASTER
import com.aws.amazonlocation.utils.TYPE_VECTOR
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapStyleViewModel @Inject constructor() : ViewModel() {

    var mStyleList = ArrayList<MapStyleData>()
    var mStyleListForFilter = ArrayList<MapStyleData>()
    var providerOptions = ArrayList<FilterOption>()
    var attributeOptions = ArrayList<FilterOption>()
    var typeOptions = ArrayList<FilterOption>()

    fun setMapListData(context: Context, isGrabMapEnable: Boolean = false) {
        val items = arrayListOf(
            MapStyleInnerData(
                context.getString(R.string.map_light),
                context.getString(R.string.map_esri),
                listOf(ATTRIBUTE_LIGHT),
                listOf(TYPE_VECTOR),
                false,
                R.drawable.light
            ),
            MapStyleInnerData(
                context.getString(R.string.map_streets),
                context.getString(R.string.map_esri),
                listOf(ATTRIBUTE_LIGHT),
                listOf(TYPE_VECTOR),
                false,
                R.drawable.streets
            ),
            MapStyleInnerData(
                context.getString(R.string.map_navigation),
                context.getString(R.string.map_esri),
                listOf(ATTRIBUTE_LIGHT),
                listOf(TYPE_VECTOR),
                false,
                R.drawable.navigation
            ),
            MapStyleInnerData(
                context.getString(R.string.map_dark_gray),
                context.getString(R.string.map_esri),
                listOf(ATTRIBUTE_DARK),
                listOf(TYPE_VECTOR),
                false,
                R.drawable.dark_gray
            ),
            MapStyleInnerData(
                context.getString(R.string.map_light_gray),
                context.getString(R.string.map_esri),
                listOf(ATTRIBUTE_LIGHT),
                listOf(TYPE_VECTOR),
                false,
                R.drawable.light_gray
            ),
            MapStyleInnerData(
                context.getString(R.string.map_imagery),
                context.getString(R.string.map_esri),
                listOf(ATTRIBUTE_SATELLITE),
                listOf(TYPE_RASTER),
                false,
                R.drawable.imagery
            ),
            MapStyleInnerData(
                context.resources.getString(R.string.map_explore),
                context.resources.getString(R.string.here),
                listOf(ATTRIBUTE_LIGHT),
                listOf(TYPE_VECTOR),
                image = R.mipmap.ic_here_explore,
                isSelected = false,
                mMapName = MapNames.HERE_EXPLORE,
                mMapStyleName = MapStyles.VECTOR_HERE_EXPLORE
            ),
            MapStyleInnerData(
                context.resources.getString(R.string.map_contrast),
                context.resources.getString(R.string.here),
                listOf(ATTRIBUTE_DARK, ATTRIBUTE_3D),
                listOf(TYPE_VECTOR),
                image = R.mipmap.ic_here_contrast,
                isSelected = false,
                mMapName = MapNames.HERE_CONTRAST,
                mMapStyleName = MapStyles.VECTOR_HERE_CONTRAST
            ),
            MapStyleInnerData(
                context.resources.getString(R.string.map_explore_truck),
                context.resources.getString(R.string.here),
                listOf(ATTRIBUTE_LIGHT, ATTRIBUTE_TRUCK),
                listOf(TYPE_VECTOR),
                image = R.mipmap.ic_here_explore_truck,
                isSelected = false,
                mMapName = MapNames.HERE_EXPLORE_TRUCK,
                mMapStyleName = MapStyles.VECTOR_HERE_EXPLORE_TRUCK
            ),
            MapStyleInnerData(
                context.resources.getString(R.string.map_hybrid),
                context.resources.getString(R.string.here),
                listOf(ATTRIBUTE_SATELLITE),
                listOf(TYPE_VECTOR, TYPE_RASTER),
                image = R.mipmap.ic_here_hybrid,
                isSelected = false,
                mMapName = MapNames.HERE_HYBRID,
                mMapStyleName = MapStyles.HYBRID_HERE_EXPLORE_SATELLITE
            ),
            MapStyleInnerData(
                context.resources.getString(R.string.map_raster),
                context.resources.getString(R.string.here),
                listOf(ATTRIBUTE_SATELLITE),
                listOf(TYPE_RASTER),
                image = R.mipmap.ic_here_imagery,
                isSelected = false,
                mMapName = MapNames.HERE_IMAGERY,
                mMapStyleName = MapStyles.RASTER_HERE_EXPLORE_SATELLITE
            )
        )
        if (isGrabMapEnable) {
            items.add(
                MapStyleInnerData(
                    context.resources.getString(R.string.map_grab_light),
                    context.resources.getString(R.string.grab),
                    listOf(ATTRIBUTE_LIGHT),
                    listOf(TYPE_VECTOR),
                    image = R.drawable.grab_light,
                    isSelected = false,
                    mMapName = MapNames.GRAB_LIGHT,
                    mMapStyleName = MapStyles.GRAB_LIGHT
                )
            )
            items.add(
                MapStyleInnerData(
                    context.resources.getString(R.string.map_grab_dark),
                    context.resources.getString(R.string.grab),
                    listOf(ATTRIBUTE_DARK),
                    listOf(TYPE_VECTOR),
                    image = R.drawable.grab_dark,
                    isSelected = false,
                    mMapName = MapNames.GRAB_DARK,
                    mMapStyleName = MapStyles.GRAB_DARK
                )
            )
        }
        items.add(
            MapStyleInnerData(
                context.resources.getString(R.string.map_standard_light),
                context.resources.getString(R.string.open_data),
                listOf(ATTRIBUTE_LIGHT),
                listOf(TYPE_VECTOR),
                image = R.drawable.standard_light,
                isSelected = false,
                mMapName = MapNames.OPEN_DATA_STANDARD_LIGHT,
                mMapStyleName = MapStyles.VECTOR_OPEN_DATA_STANDARD_LIGHT
            )
        )
        items.add(
            MapStyleInnerData(
                context.resources.getString(R.string.map_standard_dark),
                context.resources.getString(R.string.open_data),
                listOf(ATTRIBUTE_DARK),
                listOf(TYPE_VECTOR),
                image = R.drawable.standard_dark,
                isSelected = false,
                mMapName = MapNames.OPEN_DATA_STANDARD_DARK,
                mMapStyleName = MapStyles.VECTOR_OPEN_DATA_STANDARD_DARK
            )
        )
        items.add(
            MapStyleInnerData(
                context.resources.getString(R.string.map_visualization_light),
                context.resources.getString(R.string.open_data),
                listOf(ATTRIBUTE_LIGHT),
                listOf(TYPE_VECTOR),
                image = R.drawable.visualization_light,
                isSelected = false,
                mMapName = MapNames.OPEN_DATA_VISUALIZATION_LIGHT,
                mMapStyleName = MapStyles.VECTOR_OPEN_DATA_VISUALIZATION_LIGHT
            )
        )
        items.add(
            MapStyleInnerData(
                context.resources.getString(R.string.map_visualization_dark),
                context.resources.getString(R.string.open_data),
                listOf(ATTRIBUTE_DARK),
                listOf(TYPE_VECTOR),
                image = R.drawable.visualization_dark,
                isSelected = false,
                mMapName = MapNames.OPEN_DATA_VISUALIZATION_DARK,
                mMapStyleName = MapStyles.VECTOR_OPEN_DATA_VISUALIZATION_DARK
            )
        )
        mStyleList.clear()

        mStyleList = items.groupBy { it.provider }
            .map { (providerName, providerItems) ->
                MapStyleData(
                    styleNameDisplay = providerName,
                    isSelected = false, // Set isSelected as per your requirement
                    mapInnerData = providerItems
                )
            } as ArrayList<MapStyleData>

        mStyleListForFilter.clear()
        mStyleListForFilter.addAll(mStyleList)
        providerOptions = items.map { it.provider }
            .distinct()
            .map { FilterOption(it) } as ArrayList<FilterOption>

        attributeOptions = items.flatMap { it.attributes }
            .distinct()
            .map { FilterOption(it) } as ArrayList<FilterOption>

        typeOptions = items.flatMap { it.types }
            .distinct()
            .map { FilterOption(it) } as ArrayList<FilterOption>
    }

    fun filterAndSortItems(
        context: Context,
        searchQuery: String? = null,
        providerNames: List<String>? = null,
        attributes: List<String>? = null,
        types: List<String>? = null
    ): List<MapStyleData> {
        val providerOrder = listOf(
            context.resources.getString(R.string.map_esri),
            context.resources.getString(R.string.here),
            context.resources.getString(R.string.grab),
            context.resources.getString(R.string.open_data)
        )

        // Convert the providers to a sequence for more efficient processing
        return mStyleListForFilter.asSequence()
            .filter { providerNames?.contains(it.styleNameDisplay) ?: true }
            .mapNotNull { provider ->
                val filteredItems = provider.mapInnerData?.asSequence()?.filter { item ->
                    val matchesSearchQuery = searchQuery?.let { sq ->
                        var attributeDataContains = false
                        var typeDataContains = false
                        item.attributes.forEach {
                            if (!attributeDataContains) {
                                attributeDataContains = it.contains(sq, ignoreCase = true)
                            }
                        }
                        item.types.forEach {
                            if (!typeDataContains) {
                                typeDataContains = it.contains(sq, ignoreCase = true)
                            }
                        }
                        item.mapName?.contains(sq, ignoreCase = true) == true ||
                            item.provider.contains(sq, ignoreCase = true) || attributeDataContains || typeDataContains
                    } ?: true

                    val hasRequiredAttributes = attributes?.let { attrs ->
                        item.attributes.intersect(attrs).isNotEmpty()
                    } ?: true

                    val hasRequiredTypes = types?.let { ts ->
                        item.types.intersect(ts).isNotEmpty()
                    } ?: true

                    matchesSearchQuery && hasRequiredAttributes && hasRequiredTypes
                }?.toList()

                if (filteredItems?.isEmpty() == true) {
                    null
                } else {
                    provider.copy(mapInnerData = filteredItems)
                }
            }
            .sortedBy { providerOrder.indexOf(it.styleNameDisplay) }
            .toList() // Convert the result back to a list
    }
}
