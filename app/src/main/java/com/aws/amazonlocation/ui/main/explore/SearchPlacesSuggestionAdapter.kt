package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.databinding.ItemSearchDirectionsBinding
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
    private var isForDirections: Boolean,
    var mSearchPlaceSuggestionInterface: SearchPlaceSuggestionInterface,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PLACE = 0
        private const val TYPE_DIRECTIONS = 1
    }

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

    inner class SearchDirectionsVH(private val binding: ItemSearchDirectionsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SearchSuggestionData) {
            binding.apply {
                tvPlaceName.text = data.text
                binding.apply {
                    if (data.isPlaceIndexForPosition) {
                        tvPlaceName.text = data.text
                    } else {
                        if (data.amazonLocationAddress?.label.isNullOrEmpty()) {
                            tvPlaceName.text = data.text
                            tvDescription.hide()
                        } else {
                            tvDescription.show()
                        }
                        data.amazonLocationAddress?.label?.split(",")?.let { parts ->
                            tvPlaceName.text = parts.getOrNull(0) ?: data.text
                            tvDescription.text = parts.drop(1).joinToString(",").trim()
                        }
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
    }

    override fun getItemViewType(position: Int): Int {
        return if (isForDirections) TYPE_DIRECTIONS else TYPE_PLACE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PLACE -> SearchPlaceVH(
                ItemSearchPlacesSuggestionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_DIRECTIONS -> SearchDirectionsVH(
                ItemSearchDirectionsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = mSearchPlaceList[position]
        when (holder) {
            is SearchPlaceVH -> holder.bind(data)
            is SearchDirectionsVH -> holder.bind(data)
        }
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
