package com.aws.amazonlocation.ui.main.routeOption

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.databinding.FragmentRouteOptionBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.utils.KEY_AVOID_DIRT_ROADS
import com.aws.amazonlocation.utils.KEY_AVOID_FERRIES
import com.aws.amazonlocation.utils.KEY_AVOID_TOLLS
import com.aws.amazonlocation.utils.KEY_AVOID_TUNNELS
import com.aws.amazonlocation.utils.KEY_AVOID_U_TURNS

class RouteOptionFragment : BaseFragment() {

    private lateinit var mBinding: FragmentRouteOptionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentRouteOptionBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        backPress()
        clickListener()
    }

    private fun clickListener() {
        mBinding.apply {
            switchAvoidTools.isChecked = mPreferenceManager.getValue(KEY_AVOID_TOLLS, false)
            switchAvoidFerries.isChecked = mPreferenceManager.getValue(KEY_AVOID_FERRIES, false)
            switchAvoidDirtRoads.isChecked = mPreferenceManager.getValue(
                KEY_AVOID_DIRT_ROADS,
                false
            )
            switchAvoidUTurns.isChecked = mPreferenceManager.getValue(KEY_AVOID_U_TURNS, false)
            switchAvoidTunnels.isChecked = mPreferenceManager.getValue(KEY_AVOID_TUNNELS, false)

            ivRouteOptionBack.setOnClickListener {
                findNavController().popBackStack()
            }

            switchAvoidTools.setOnCheckedChangeListener { _, isChecked ->
                mPreferenceManager.setValue(KEY_AVOID_TOLLS, isChecked)
            }

            switchAvoidFerries.setOnCheckedChangeListener { _, isChecked ->
                mPreferenceManager.setValue(KEY_AVOID_FERRIES, isChecked)
            }

            switchAvoidDirtRoads.setOnCheckedChangeListener { _, isChecked ->
                mPreferenceManager.setValue(KEY_AVOID_DIRT_ROADS, isChecked)
            }

            switchAvoidUTurns.setOnCheckedChangeListener { _, isChecked ->
                mPreferenceManager.setValue(KEY_AVOID_U_TURNS, isChecked)
            }

            switchAvoidTunnels.setOnCheckedChangeListener { _, isChecked ->
                mPreferenceManager.setValue(KEY_AVOID_TUNNELS, isChecked)
            }
        }
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
