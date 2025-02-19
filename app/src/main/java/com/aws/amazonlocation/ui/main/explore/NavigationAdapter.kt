package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.databinding.ItemNavigationRouteListBinding
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.TURN_LEFT
import com.aws.amazonlocation.utils.TURN_RIGHT
import com.aws.amazonlocation.utils.TYPE_ARRIVE
import com.aws.amazonlocation.utils.TYPE_CONTINUE
import com.aws.amazonlocation.utils.TYPE_CONTINUE_HIGHWAY
import com.aws.amazonlocation.utils.TYPE_DEPART
import com.aws.amazonlocation.utils.TYPE_ENTER_HIGHWAY
import com.aws.amazonlocation.utils.TYPE_EXIT
import com.aws.amazonlocation.utils.TYPE_KEEP
import com.aws.amazonlocation.utils.TYPE_RAMP
import com.aws.amazonlocation.utils.TYPE_ROUNDABOUT_ENTER
import com.aws.amazonlocation.utils.TYPE_ROUNDABOUT_EXIT
import com.aws.amazonlocation.utils.TYPE_ROUNDABOUT_PASS
import com.aws.amazonlocation.utils.TYPE_SDK_UNKNOWN
import com.aws.amazonlocation.utils.TYPE_TURN
import com.aws.amazonlocation.utils.TYPE_U_TURN
import com.aws.amazonlocation.utils.Units.getMetricsNew
import com.aws.amazonlocation.utils.Units.isMetric
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.showViews

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class NavigationAdapter(
    private val mNavigationList: ArrayList<NavigationData>,
    private val preferenceManager: PreferenceManager,
) : RecyclerView.Adapter<NavigationAdapter.NavigationVH>() {
    inner class NavigationVH(
        val binding: ItemNavigationRouteListBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: NavigationData,
            isLastItem: Boolean,
        ) {
            binding.apply {
                tvNavigationAddress.text = data.getAddress()

                data.distance?.let { distance ->
                    tvNavigationDistance.text =
                        preferenceManager.getValue(KEY_UNIT_SYSTEM, "").let { unitSystem ->
                            val isMetric = isMetric(unitSystem)
                            getMetricsNew(
                                tvNavigationAddress.context,
                                distance,
                                isMetric,
                                true,
                            )
                        }
                }
                data.type?.let {
                    when (it) {
                        TYPE_TURN -> {
                            data.routeTurnStepDetails?.let { routeTurnStepDetails ->
                                if (routeTurnStepDetails.steeringDirection?.value.equals(TURN_LEFT, true,)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_left)
                                } else if (routeTurnStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true,)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_right)
                                }
                            }
                        }

                        TYPE_ARRIVE -> {
                            ivNavigationIcon.setImageResource(R.drawable.ic_arrive)
                        }

                        TYPE_CONTINUE -> {
                            ivNavigationIcon.setImageResource(R.drawable.ic_continue)
                        }

                        TYPE_CONTINUE_HIGHWAY -> {
                            ivNavigationIcon.setImageResource(R.drawable.ic_continue)
                        }

                        TYPE_DEPART -> {
                            ivNavigationIcon.setImageResource(R.drawable.ic_arrive)
                        }

                        TYPE_ENTER_HIGHWAY -> {
                            data.routeEnterHighwayStepDetails?.let { highwayStepDetails ->
                                if (highwayStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_ramp_left)
                                } else if (highwayStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_ramp_right)
                                }
                            }
                        }

                        TYPE_EXIT -> {
                            data.routeExitStepDetails?.let { exitStepDetails ->
                                if (exitStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_exit_left)
                                } else if (exitStepDetails.steeringDirection?.value.equals(
                                        TURN_RIGHT,
                                        true,
                                    )
                                ) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_exit_right)
                                }
                            }
                        }

                        TYPE_KEEP -> {
                            ivNavigationIcon.setImageResource(R.drawable.ic_continue)
                        }

                        TYPE_RAMP -> {
                            data.routeRampStepDetails?.let { rampStepDetails ->
                                if (rampStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_ramp_left)
                                } else if (rampStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_ramp_right)
                                }
                            }
                        }

                        TYPE_ROUNDABOUT_ENTER -> {
                            ivNavigationIcon.setImageResource(R.drawable.ic_roundabout_enter)
                        }

                        TYPE_ROUNDABOUT_EXIT -> {
                            data.routeRoundaboutExitStepDetails?.let { exitStepDetails ->
                                if (exitStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_roundabout_exit_left)
                                } else if (exitStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_roundabout_exit_right)
                                }
                            }
                        }

                        TYPE_ROUNDABOUT_PASS -> {
                            data.routeRoundaboutPassStepDetails?.let { passStepDetails ->
                                if (passStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_roundabout_pass_left)
                                } else if (passStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_roundabout_pass_right)
                                }
                            }
                        }

                        TYPE_U_TURN -> {
                            data.routeUTurnStepDetails?.let { uTurnStepDetails ->
                                if (uTurnStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_uturn_left)
                                } else if (uTurnStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                    ivNavigationIcon.setImageResource(R.drawable.ic_uturn_right)
                                }
                            }
                        }

                        TYPE_SDK_UNKNOWN -> {
                            ivNavigationIcon.setImageResource(R.drawable.ic_sdk_unkown)
                        }

                        else -> {
                            ivNavigationIcon.setImageResource(R.drawable.ic_continue)
                        }
                    }
                }
                if (isLastItem) {
                    if (data.getAddress().contains(TURN_LEFT, true)) {
                        tvDestination.text =
                            tvDestination.context.getString(R.string.label_destination_will_be_on_the_left)
                        showViews(ivDestination, tvDestination)
                    } else if (data.getAddress().contains(TURN_RIGHT, true)) {
                        tvDestination.text =
                            tvDestination.context.getString(R.string.label_destination_will_be_on_the_right)
                        showViews(ivDestination, tvDestination)
                    } else {
                        hideViews(ivDestination, tvDestination)
                    }
                } else {
                    hideViews(ivDestination, tvDestination)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): NavigationVH =
        NavigationVH(
            ItemNavigationRouteListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: NavigationVH,
        position: Int,
    ) {
        holder.setIsRecyclable(false)
        holder.bind(mNavigationList[position], position == mNavigationList.size - 1)
    }

    override fun getItemCount() = mNavigationList.size
}
