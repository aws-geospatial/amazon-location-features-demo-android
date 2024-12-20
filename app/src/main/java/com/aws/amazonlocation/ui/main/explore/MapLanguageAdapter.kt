package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.LanguageData
import com.aws.amazonlocation.databinding.ItemMapLanguageBinding

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class MapLanguageAdapter(
    private val languageData: ArrayList<LanguageData>,
    private val isRtl: Boolean,
    var placeInterface: MapLanguageInterface,
) :
    RecyclerView.Adapter<MapLanguageAdapter.SearchPoliticalVH>() {

    inner class SearchPoliticalVH(private val binding: ItemMapLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: LanguageData) {
            binding.apply {
                tvLanguage.text = data.label
                clMain.setOnClickListener {
                    placeInterface.languageClick(adapterPosition)
                }
                if (data.isSelected) {
                    setRadioButtonIcon(rbLanguage, R.drawable.icon_checkmark)
                } else {
                    setRadioButtonIcon(rbLanguage, R.drawable.ic_radio_button_unchecked)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPoliticalVH {
        return SearchPoliticalVH(
            ItemMapLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: SearchPoliticalVH, position: Int) {
        holder.bind(languageData[position])
    }

    override fun getItemCount() = languageData.size

    interface MapLanguageInterface {
        fun languageClick(position: Int)
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
