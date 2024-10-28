package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.PoliticalData
import com.aws.amazonlocation.databinding.ItemSearchCountryBinding

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class PoliticalAdapter(
    private val politicalData: ArrayList<PoliticalData>,
    private val isRtl: Boolean,
    var placeInterface: PoliticalInterface,
) :
    RecyclerView.Adapter<PoliticalAdapter.SearchPoliticalVH>() {

    inner class SearchPoliticalVH(private val binding: ItemSearchCountryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PoliticalData) {
            binding.apply {
                tvCountryName.text = data.countryName
                tvDescription.text = data.description
                tvCountry.text = data.countryCode
                clMain.setOnClickListener {
                    placeInterface.countryClick(adapterPosition)
                }
                if (data.isSelected) {
                    setRadioButtonIcon(rbCountry, R.drawable.icon_checkmark)
                } else {
                    setRadioButtonIcon(rbCountry, R.drawable.ic_radio_button_unchecked)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPoliticalVH {
        return SearchPoliticalVH(
            ItemSearchCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: SearchPoliticalVH, position: Int) {
        holder.bind(politicalData[position])
    }

    override fun getItemCount() = politicalData.size

    interface PoliticalInterface {
        fun countryClick(position: Int)
    }
    private fun setRadioButtonIcon(
        rb: AppCompatRadioButton,
        iconCheckmark: Int
    ) {
        rb.isChecked = true
        if (isRtl) {
            rb.setCompoundDrawablesWithIntrinsicBounds(
                iconCheckmark,
                0,
                0,
                0
            )
        } else {
            rb.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                iconCheckmark,
                0
            )
        }
    }

}
