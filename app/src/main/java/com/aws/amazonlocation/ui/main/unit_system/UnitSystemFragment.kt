package com.aws.amazonlocation.ui.main.unit_system

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentUnitSystemBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.Units

class UnitSystemFragment : BaseFragment() {

    private lateinit var mBinding: FragmentUnitSystemBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentUnitSystemBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        backPress()
        clickListener()
    }

    private fun clickListener() {
        val unitSystem =
            mPreferenceManager.getValue(KEY_UNIT_SYSTEM, resources.getString(R.string.automatic))
        mBinding.apply {
            changeDataProvider(unitSystem)

            ivRouteUnitSystem.setOnClickListener {
                findNavController().popBackStack()
            }

            llAutomatic.setOnClickListener {
                mPreferenceManager.setValue(KEY_UNIT_SYSTEM, resources.getString(R.string.automatic))
                changeDataProvider(resources.getString(R.string.automatic))
                val properties = listOf(
                    Pair(AnalyticsAttribute.TYPE, resources.getString(R.string.automatic))
                )
                (activity as MainActivity).analyticsUtils?.recordEvent(EventType.MAP_UNIT_CHANGE, properties)
            }

            llMetric.setOnClickListener {
                changeDataProvider(resources.getString(R.string.metric))
                mPreferenceManager.setValue(KEY_UNIT_SYSTEM, resources.getString(R.string.metric))
                val properties = listOf(
                    Pair(AnalyticsAttribute.TYPE, resources.getString(R.string.metric))
                )
                (activity as MainActivity).analyticsUtils?.recordEvent(EventType.MAP_UNIT_CHANGE, properties)
            }

            llImperial.setOnClickListener {
                changeDataProvider(resources.getString(R.string.imperial))
                mPreferenceManager.setValue(KEY_UNIT_SYSTEM, resources.getString(R.string.imperial))
                val properties = listOf(
                    Pair(AnalyticsAttribute.TYPE, resources.getString(R.string.imperial))
                )
                (activity as MainActivity).analyticsUtils?.recordEvent(EventType.MAP_UNIT_CHANGE, properties)
            }
        }
    }

    private fun changeDataProvider(dataProvider: String?) {
        mBinding.apply {
            when (dataProvider) {
                resources.getString(R.string.metric) -> {
                    ivAutomatic.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked
                        )
                    )
                    ivMetric.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.icon_checkmark
                        )
                    )
                    ivImperial.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked
                        )
                    )
                }
                resources.getString(R.string.imperial) -> {
                    ivAutomatic.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked
                        )
                    )
                    ivMetric.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked
                        )
                    )
                    ivImperial.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.icon_checkmark
                        )
                    )
                }
                else -> {
                    setAutomaticLabel()
                    ivAutomatic.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.icon_checkmark
                        )
                    )
                    ivMetric.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked
                        )
                    )
                    ivImperial.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked
                        )
                    )
                }
            }
        }
    }

    private fun FragmentUnitSystemBinding.setAutomaticLabel() {
        val isMetric = Units.isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
        if (isMetric) {
            tvAutomaticSubLabel.text = getString(R.string.label_automatic_sub_title_km)
        } else {
            tvAutomaticSubLabel.text = getString(R.string.label_automatic_sub_title_mile)
        }
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
