package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.databinding.ItemNavigationRouteListBinding
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.Units.convertToLowerUnit
import com.aws.amazonlocation.utils.Units.getMetricsNew
import com.aws.amazonlocation.utils.Units.isMetric
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class NavigationAdapter(
    private val mNavigationList: ArrayList<NavigationData>,
    private val preferenceManager: PreferenceManager
) :
    RecyclerView.Adapter<NavigationAdapter.NavigationVH>() {
    private var isRoundDotted = false
    inner class NavigationVH(val binding: ItemNavigationRouteListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NavigationData) {
            binding.apply {
                tvNavigationAddress.text = data.getAddress()

                data.distance?.let { distance ->
                    tvNavigationDistance.text = preferenceManager.getValue(KEY_UNIT_SYSTEM, "").let { unitSystem ->
                        val isMetric = isMetric(unitSystem)
                        getMetricsNew(
                            tvNavigationAddress.context,
                            convertToLowerUnit(distance, isMetric),
                            isMetric
                        )
                    }
                }
            }
        }
    }

    fun setIsRounded(isRound: Boolean) {
        isRoundDotted = isRound
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavigationVH {
        return NavigationVH(
            ItemNavigationRouteListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NavigationVH, position: Int) {
        holder.setIsRecyclable(false)
        holder.bind(mNavigationList[position])
        if (position == 0) {
            if (isRoundDotted) {
                holder.binding.viewTop.hide()
                holder.binding.viewTopDotted.hide()
                holder.binding.viewBottomDotted.show()
            } else {
                holder.binding.viewTop.hide()
                holder.binding.viewTopDotted.hide()
                holder.binding.viewBottom.show()
            }
        } else if (position == mNavigationList.size - 1) {
            if (isRoundDotted) {
                holder.binding.viewTopDotted.show()
                holder.binding.viewBottom.hide()
                holder.binding.viewBottomDotted.hide()
            } else {
                holder.binding.viewTop.show()
                holder.binding.viewBottom.hide()
                holder.binding.viewBottomDotted.hide()
            }
            holder.binding.ivNavigationIcon.setImageResource(R.drawable.ic_destination_small)
        } else {
            if (isRoundDotted) {
                holder.binding.viewTop.hide()
                holder.binding.viewBottom.hide()
                holder.binding.viewTopDotted.show()
                holder.binding.viewBottomDotted.show()
            } else {
                holder.binding.viewTop.show()
                holder.binding.viewBottom.show()
                holder.binding.viewTopDotted.hide()
                holder.binding.viewBottomDotted.hide()
            }
        }
    }

    override fun getItemCount() = mNavigationList.size
}
