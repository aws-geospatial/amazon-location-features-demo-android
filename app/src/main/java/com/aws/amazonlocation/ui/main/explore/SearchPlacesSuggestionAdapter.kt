package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.databinding.ItemSearchPlacesSuggestionBinding
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SearchPlacesSuggestionAdapter(
    private val mSearchPlaceList: ArrayList<SearchSuggestionData>,
    private val preferenceManager: PreferenceManager?,
    var mSearchPlaceSuggestionInterface: SearchPlaceSuggestionInterface,
) :
    RecyclerView.Adapter<SearchPlacesSuggestionAdapter.SearchPlaceVH>() {

    inner class SearchPlaceVH(private val binding: ItemSearchPlacesSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SearchSuggestionData) {
            binding.apply {
                val region = getRegion(data.amazonLocationAddress?.region?.name, data.amazonLocationAddress?.subRegion?.name, data.amazonLocationAddress?.country?.name)

                if (region.isEmpty()) {
                    tvRegion.text = tvRegion.context.resources.getString(R.string.search_nearby)
                } else {
                    tvRegion.text = region
                }

                if (data.isPlaceIndexForPosition) {
                    tvPlaceName.text = data.text
                    tvRegion.hide()
                } else {
                    tvPlaceName.text = data.text?.split(",")?.toTypedArray()?.get(0) ?: data.text
                }

                if (data.distance != null && preferenceManager != null) {
                    tvDistance.text = preferenceManager.getValue(KEY_UNIT_SYSTEM, "").let { unitSystem ->
                        val isMetric = Units.isMetric(unitSystem)
                        data.distance?.let {
                            Units.getMetricsNew(
                                tvDistance.context,
                                it,
                                isMetric,
                                false
                            )
                        }
                    }
                    groupDistance.show()
                } else {
                    groupDistance.hide()
                }

                when {
                    data.placeId.isNullOrEmpty() -> ivSearchLocation.setImageResource(R.drawable.icon_search)
                    !data.placeId.isNullOrEmpty() -> ivSearchLocation.setImageResource(R.drawable.ic_map_pin)
                }
                clMain.setOnClickListener {
                    mSearchPlaceSuggestionInterface.suggestedPlaceClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceVH {
        return SearchPlaceVH(
            ItemSearchPlacesSuggestionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(holder: SearchPlaceVH, position: Int) {
        holder.bind(mSearchPlaceList[position])
    }

    override fun getItemCount() = mSearchPlaceList.size

    private fun getRegion(region: String?, subRegion: String?, country: String?): String {
        var mRegion = ""
        mRegion += if (!region.isNullOrEmpty()) {
            "$region, $country"
        } else if (!subRegion.isNullOrEmpty()) {
            "$subRegion, $country"
        } else if (!country.isNullOrEmpty()) {
            country
        } else {
            ""
        }
        return mRegion
    }

    interface SearchPlaceSuggestionInterface {
        fun suggestedPlaceClick(position: Int)
    }
}
