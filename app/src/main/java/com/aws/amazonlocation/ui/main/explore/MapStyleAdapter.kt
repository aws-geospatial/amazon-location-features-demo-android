package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.data.response.MapStyleData
import com.aws.amazonlocation.databinding.ItemMapStyleBinding

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class MapStyleAdapter(
    private val mMapStyleData: ArrayList<MapStyleData>,
    var mapInterface: MapInterface
) :
    RecyclerView.Adapter<MapStyleAdapter.SearchPlaceVH>() {

    inner class SearchPlaceVH(private val binding: ItemMapStyleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MapStyleData) {
            binding.apply {
                if (data.isDisable) {
                    rvMapName.isClickable = false
                    rvMapName.alpha = 0.3f
                } else {
                    rvMapName.isClickable = true
                    rvMapName.alpha = 1f
                }
                data.mapInnerData?.let {
                    rvMapName.apply {
                        this.layoutManager = GridLayoutManager(this.context, 2)
                        val mMapStyleAdapter = MapStyleInnerAdapter(
                            it,
                            object : MapStyleInnerAdapter.MapInterface {
                                override fun mapClick(position: Int) {
                                    if (!data.isDisable) {
                                        mapInterface.mapStyleClick(
                                            position = adapterPosition,
                                            position
                                        )
                                    }
                                }
                            }
                        )
                        this.adapter = mMapStyleAdapter
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceVH {
        return SearchPlaceVH(
            ItemMapStyleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchPlaceVH, position: Int) {
        holder.bind(mMapStyleData[position])
    }

    override fun getItemCount() = mMapStyleData.size

    interface MapInterface {
        fun mapStyleClick(position: Int, innerPosition: Int)
    }
}
