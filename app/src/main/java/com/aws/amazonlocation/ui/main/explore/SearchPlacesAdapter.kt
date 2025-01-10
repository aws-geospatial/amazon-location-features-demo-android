package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.databinding.ItemSearchDirectionsBinding
import com.aws.amazonlocation.databinding.ItemSearchPlacesBinding
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.getRegion
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SearchPlacesAdapter(
    private val mSearchPlaceList: ArrayList<SearchSuggestionData>,
    private val preferenceManager: PreferenceManager?,
    private var isForDirections: Boolean,
    var mSearchPlaceInterface: SearchPlaceInterface,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PLACE = 0
        private const val TYPE_DIRECTIONS = 1
    }

    inner class SearchPlaceVH(private val binding: ItemSearchPlacesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SearchSuggestionData) {
            binding.apply {
                ivSearchLocation.setImageResource(R.drawable.ic_map_pin)

                if (data.isPlaceIndexForPosition) {
                    tvPlaceName.text = data.text
                    groupDistance.hide()
                    tvRegion.hide()
                } else {
                    tvPlaceName.text = data.amazonLocationAddress?.label?.split(",")?.toTypedArray()?.get(0) ?: data.amazonLocationAddress?.label
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

                tvRegion.text =
                    getRegion(data.amazonLocationAddress?.region?.name, data.amazonLocationAddress?.subRegion?.name, data.amazonLocationAddress?.country?.name)

                binding.clMain.setOnClickListener {
                    mSearchPlaceInterface.placeClick(adapterPosition)
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
                        mSearchPlaceInterface.placeClick(adapterPosition)
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
                ItemSearchPlacesBinding.inflate(
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

    interface SearchPlaceInterface {
        fun placeClick(position: Int)
    }
}
