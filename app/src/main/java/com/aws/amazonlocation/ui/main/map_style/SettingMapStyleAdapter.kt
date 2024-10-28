package com.aws.amazonlocation.ui.main.map_style

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.data.response.MapStyleData
import com.aws.amazonlocation.databinding.ItemMapStyleBinding
import com.aws.amazonlocation.ui.main.explore.MapStyleInnerAdapter
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SettingMapStyleAdapter(
    var columnCount: Int,
    private val mMapStyleData: ArrayList<MapStyleData>,
    var mapInterface: MapInterface
) :
    RecyclerView.Adapter<SettingMapStyleAdapter.SearchPlaceVH>() {

    inner class SearchPlaceVH(private val binding: ItemMapStyleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MapStyleData) {
            binding.apply {
                data.mapInnerData?.let {
                    rvMapName.apply {
                        this.layoutManager = GridLayoutManager(this.context, columnCount)
                        val mMapStyleAdapter = MapStyleInnerAdapter(
                            it,
                            object : MapStyleInnerAdapter.MapInterface {
                                override fun mapClick(position: Int) {
                                    mapInterface.mapStyleClick(position = adapterPosition, position)
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
