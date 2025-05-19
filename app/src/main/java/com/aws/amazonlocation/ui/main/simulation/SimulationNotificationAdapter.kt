package com.aws.amazonlocation.ui.main.simulation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.databinding.RvItemNotificationBinding
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show
import java.util.Locale

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SimulationNotificationAdapter(
    private val notificationData: ArrayList<NotificationData>,
    private val defaultLocale: Locale?,
    var notificationInterface: NotificationInterface
) :
    RecyclerView.Adapter<SimulationNotificationAdapter.SearchPlaceVH>() {

    inner class SearchPlaceVH(private val binding: RvItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NotificationData) {
            binding.apply {
                if (defaultLocale?.language == LANGUAGE_CODE_ARABIC) {
                    tvBusName.textDirection = View.TEXT_DIRECTION_RTL
                }
                tvBusName.text = data.name
                switchBusNotification.isChecked = data.isSelected
                if (adapterPosition == notificationData.size - 1) {
                    viewNotification.hide()
                } else {
                    viewNotification.show()
                }
                switchBusNotification.setOnCheckedChangeListener { _, isChecked ->
                    notificationInterface.click(adapterPosition, isChecked)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceVH {
        return SearchPlaceVH(
            RvItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchPlaceVH, position: Int) {
        holder.bind(notificationData[position])
    }

    override fun getItemCount() = notificationData.size

    interface NotificationInterface {
        fun click(position: Int, isSelected: Boolean)
    }
}
