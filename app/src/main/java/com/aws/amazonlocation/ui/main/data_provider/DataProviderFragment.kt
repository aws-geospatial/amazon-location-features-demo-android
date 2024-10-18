package com.aws.amazonlocation.ui.main.data_provider

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.databinding.FragmentDataProviderBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_GRAB_DONT_ASK
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_NEAREST_REGION
import com.aws.amazonlocation.utils.KEY_OPEN_DATA_DONT_ASK
import com.aws.amazonlocation.utils.KEY_SELECTED_REGION
import com.aws.amazonlocation.utils.MapStyleRestartInterface
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.enableOpenData
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.isGrabMapEnable
import com.aws.amazonlocation.utils.isRunningTest
import com.aws.amazonlocation.utils.regionDisplayName
import com.aws.amazonlocation.utils.restartAppMapStyleDialog
import com.aws.amazonlocation.utils.restartApplication
import com.aws.amazonlocation.utils.show
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DataProviderFragment : BaseFragment() {

    private lateinit var mBinding: FragmentDataProviderBinding
    private var isGrabMapEnable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDataProviderBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isGrabMapEnable = isGrabMapEnable(mPreferenceManager)
        backPress()
        clickListener()
    }

    private fun clickListener() {
        val dataProvider =
            mPreferenceManager.getValue(KEY_MAP_NAME, resources.getString(R.string.esri))
        mBinding.apply {
            if (isGrabMapEnable) {
                llGrab.show()
            } else {
                llGrab.hide()
            }
        }
        mBinding.apply {
            when (dataProvider) {
                resources.getString(R.string.esri) -> {
                    changeDataProvider(isEsri = true)
                }
                resources.getString(R.string.here) -> {
                    changeDataProvider(isHere = true)
                }
                resources.getString(R.string.grab) -> {
                    changeDataProvider(isGrab = true)
                }
                resources.getString(R.string.open_data) -> {
                    changeDataProvider(isOpenData = true)
                }
            }

            ivRouteDataProvider.setOnClickListener {
                findNavController().popBackStack()
            }

            llEsri.setOnClickListener {
                showRestartDialog(isEsri = true)
            }

            llHere.setOnClickListener {
                showRestartDialog(isHere = true)
            }

            llGrab.setOnClickListener {
                showRestartDialog(isGrab = true)
            }

            llOpenData.setOnClickListener {
                showRestartDialog(isOpenData = true)
            }
        }
    }

    private fun changeDataProviderEsri() {
        val properties = listOf(
            Pair(AnalyticsAttribute.PROVIDER, resources.getString(R.string.esri)),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsUtils?.recordEvent(
            EventType.MAP_PROVIDER_CHANGE,
            properties
        )
        changeDataProvider(isEsri = true)
        val mapStyle = mPreferenceManager.getValue(
            KEY_MAP_STYLE_NAME,
            resources.getString(R.string.map_light)
        )

        if (mapStyle != getString(R.string.map_light) ||
            mapStyle != getString(R.string.map_streets) ||
            mapStyle != getString(R.string.map_navigation) ||
            mapStyle != getString(R.string.map_dark_gray) ||
            mapStyle != getString(R.string.map_light_gray) ||
            mapStyle != getString(R.string.map_imagery)
        ) {
            mPreferenceManager.setValue(
                KEY_MAP_STYLE_NAME,
                resources.getString(R.string.map_light)
            )
        }
        mPreferenceManager.setValue(KEY_MAP_NAME, resources.getString(R.string.esri))
    }

    private fun changeDataProviderHere() {
        val properties = listOf(
            Pair(AnalyticsAttribute.PROVIDER, resources.getString(R.string.here)),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsUtils?.recordEvent(
            EventType.MAP_PROVIDER_CHANGE,
            properties
        )
        changeDataProvider(isHere = true)
        val mapStyle = mPreferenceManager.getValue(
            KEY_MAP_STYLE_NAME,
            resources.getString(R.string.map_light)
        )
        if (mapStyle != getString(R.string.map_contrast) ||
            mapStyle != getString(R.string.map_explore) ||
            mapStyle != getString(R.string.map_explore_truck) ||
            /*mapStyle != getString(R.string.map_hybrid) ||*/
            mapStyle != getString(R.string.map_raster)
        ) {
            mPreferenceManager.setValue(
                KEY_MAP_STYLE_NAME,
                resources.getString(R.string.map_explore)
            )
        }
        mPreferenceManager.setValue(KEY_MAP_NAME, resources.getString(R.string.here))
    }

    private fun changeDataProviderOpenData() {
        val properties = listOf(
            Pair(AnalyticsAttribute.PROVIDER, resources.getString(R.string.open_data)),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsUtils?.recordEvent(
            EventType.MAP_PROVIDER_CHANGE,
            properties
        )
        changeDataProvider(isOpenData = true)
        val mapStyle = mPreferenceManager.getValue(
            KEY_MAP_STYLE_NAME,
            resources.getString(R.string.map_light)
        )
        if (mapStyle != getString(R.string.map_standard_light) ||
            mapStyle != getString(R.string.map_standard_dark) ||
            mapStyle != getString(R.string.map_visualization_light) ||
            mapStyle != getString(R.string.map_visualization_dark)
        ) {
            mPreferenceManager.setValue(
                KEY_MAP_STYLE_NAME,
                resources.getString(R.string.map_standard_light)
            )
        }
        mPreferenceManager.setValue(KEY_MAP_NAME, resources.getString(R.string.open_data))
    }

    private fun showRestartDialog(isEsri: Boolean = false, isGrab: Boolean = false, isHere: Boolean = false, isOpenData: Boolean = false) {
        val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
        val defaultIdentityPoolId: String = Units.getDefaultIdentityPoolId(
            mPreferenceManager.getValue(
                KEY_SELECTED_REGION,
                regionDisplayName[0]
            ),
            mPreferenceManager.getValue(KEY_NEAREST_REGION, "")
        )
        var isRestartNeeded =
            if (defaultIdentityPoolId == BuildConfig.DEFAULT_IDENTITY_POOL_ID_AP) {
                false
            } else {
                if (mapName == getString(R.string.esri) || mapName == getString(R.string.here) || mapName == getString(R.string.open_data)) {
                    isGrab
                } else {
                    !isGrab
                }
            }
        val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        if (mAuthStatus == AuthEnum.SIGNED_IN.name || mAuthStatus == AuthEnum.AWS_CONNECTED.name) {
            isRestartNeeded = false
        }
        if (isGrab) {
            val shouldShowGrabDialog = !mPreferenceManager.getValue(KEY_GRAB_DONT_ASK, false)
            if (shouldShowGrabDialog) {
                activity?.restartAppMapStyleDialog(object : MapStyleRestartInterface {
                    override fun onOkClick(dialog: DialogInterface, dontAskAgain: Boolean) {
                        mPreferenceManager.setValue(KEY_GRAB_DONT_ASK, dontAskAgain)
                        changeDataProviderGrab()
                        if (isRestartNeeded) {
                            mPreferenceManager.setValue(KEY_SELECTED_REGION, regionDisplayName[2])
                            mAWSLocationHelper.locationCredentialsProvider?.clear()
                            lifecycleScope.launch {
                                if (!isRunningTest) {
                                    delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                                    activity?.restartApplication()
                                }
                            }
                        }
                    }

                    override fun onLearnMoreClick(dialog: DialogInterface) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(BuildConfig.GRAB_LEARN_MORE)
                            )
                        )
                    }
                })
            } else {
                changeDataProviderGrab()
                if (isRestartNeeded) {
                    mPreferenceManager.setValue(KEY_SELECTED_REGION, regionDisplayName[2])
                    mAWSLocationHelper.locationCredentialsProvider?.clear()
                    lifecycleScope.launch {
                        if (!isRunningTest) {
                            delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                            activity?.restartApplication()
                        }
                    }
                }
            }
        } else {
            if (isEsri) {
                changeDataProviderEsri()
            }
            if (isHere) {
                changeDataProviderHere()
            }
            if (isOpenData && mapName != getString(R.string.open_data)) {
                val shouldShowOpenDataDialog = !mPreferenceManager.getValue(KEY_OPEN_DATA_DONT_ASK, false)
                if (shouldShowOpenDataDialog) {
                    activity?.enableOpenData(object : MapStyleRestartInterface {
                        override fun onOkClick(dialog: DialogInterface, dontAskAgain: Boolean) {
                            changeDataProviderOpenData()
                            mPreferenceManager.setValue(KEY_OPEN_DATA_DONT_ASK, dontAskAgain)
                        }

                        override fun onLearnMoreClick(dialog: DialogInterface) {}
                    })
                } else {
                    changeDataProviderOpenData()
                }
            }
        }
    }
    private fun changeDataProviderGrab() {
        val properties = listOf(
            Pair(AnalyticsAttribute.PROVIDER, resources.getString(R.string.grab)),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsUtils?.recordEvent(
            EventType.MAP_PROVIDER_CHANGE,
            properties
        )
        changeDataProvider(isGrab = true)
        val mapStyle = mPreferenceManager.getValue(
            KEY_MAP_STYLE_NAME,
            resources.getString(R.string.map_light)
        )
        if (mapStyle != getString(R.string.map_grab_light) ||
            mapStyle != getString(R.string.map_grab_dark)
        ) {
            mPreferenceManager.setValue(
                KEY_MAP_STYLE_NAME,
                resources.getString(R.string.map_grab_light)
            )
        }
        mPreferenceManager.setValue(KEY_MAP_NAME, resources.getString(R.string.grab))
    }
    private fun changeDataProvider(isEsri: Boolean = false, isGrab: Boolean = false, isHere: Boolean = false, isOpenData: Boolean = false) {
        mBinding.apply {
            ivEsri.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (isEsri) R.drawable.icon_checkmark else R.drawable.ic_radio_button_unchecked
                )
            )
            ivHere.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (isHere) R.drawable.icon_checkmark else R.drawable.ic_radio_button_unchecked
                )
            )
            ivGrab.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (isGrab) R.drawable.icon_checkmark else R.drawable.ic_radio_button_unchecked
                )
            )
            ivOpenData.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (isOpenData) R.drawable.icon_checkmark else R.drawable.ic_radio_button_unchecked
                )
            )
        }
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
