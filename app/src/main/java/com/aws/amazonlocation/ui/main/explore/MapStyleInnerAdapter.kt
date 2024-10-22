package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.MapStyleInnerData
import com.aws.amazonlocation.databinding.ItemMapNameBinding
import com.aws.amazonlocation.utils.DESCRIPTION_TAG_ESRI
import com.aws.amazonlocation.utils.DESCRIPTION_TAG_GRAB
import com.aws.amazonlocation.utils.DESCRIPTION_TAG_HERE
import com.aws.amazonlocation.utils.DESCRIPTION_TAG_OPEN
import com.aws.amazonlocation.utils.GRAB
import com.aws.amazonlocation.utils.HERE
import com.aws.amazonlocation.utils.OPEN

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class MapStyleInnerAdapter(
    private val mMapStyleData: List<MapStyleInnerData>,
    var mapInterface: MapInterface
) :
    RecyclerView.Adapter<MapStyleInnerAdapter.SearchPlaceVH>() {

    inner class SearchPlaceVH(private val binding: ItemMapNameBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MapStyleInnerData) {
            binding.apply {
                tvStyleName.text = data.mapName
                ivChecked.setImageResource(data.image)
                if (data.isSelected) {
                    cardMapImage.strokeWidth = 2
                    tvStyleName.setTextColor(ContextCompat.getColor(tvStyleName.context, R.color.color_primary_green))
                } else {
                    cardMapImage.strokeWidth = 0
                    tvStyleName.setTextColor(ContextCompat.getColor(tvStyleName.context, R.color.color_medium_black))
                }
                cardMapImage.contentDescription = getDescription(data, adapterPosition)
                cardMapImage.setOnClickListener {
                    mapInterface.mapClick(position = adapterPosition)
                }
                tvStyleName.setOnClickListener {
                    mapInterface.mapClick(position = adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceVH {
        return SearchPlaceVH(
            ItemMapNameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchPlaceVH, position: Int) {
        holder.bind(mMapStyleData[position])
    }

    private fun getDescription(innerStyle: MapStyleInnerData, position: Int): String {
        return if (innerStyle.mMapStyleName?.contains(HERE) == true) {
            DESCRIPTION_TAG_HERE + position
        } else if (innerStyle.mMapStyleName?.contains(OPEN) == true) {
            DESCRIPTION_TAG_OPEN + position
        }  else if (innerStyle.mMapStyleName?.contains(GRAB) == true) {
            DESCRIPTION_TAG_GRAB + position
        } else {
            DESCRIPTION_TAG_ESRI + position
        }
    }

    override fun getItemCount() = mMapStyleData.size

    interface MapInterface {
        fun mapClick(position: Int)
    }
}
