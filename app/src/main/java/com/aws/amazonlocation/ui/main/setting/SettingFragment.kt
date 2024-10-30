package com.aws.amazonlocation.ui.main.setting

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.databinding.FragmentSettingBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.language.LanguageFragment
import com.aws.amazonlocation.ui.main.map_style.MapStyleFragment
import com.aws.amazonlocation.ui.main.region.RegionFragment
import com.aws.amazonlocation.ui.main.route_option.RouteOptionFragment
import com.aws.amazonlocation.ui.main.unit_system.UnitSystemFragment
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.DisconnectAWSInterface
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_SELECTED_REGION
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_BR_PT
import com.aws.amazonlocation.utils.LANGUAGE_CODE_CH_CN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_CH_TW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ENGLISH
import com.aws.amazonlocation.utils.LANGUAGE_CODE_FRENCH
import com.aws.amazonlocation.utils.LANGUAGE_CODE_GERMAN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HINDI
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ITALIAN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_JAPANESE
import com.aws.amazonlocation.utils.LANGUAGE_CODE_KOREAN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_SPANISH
import com.aws.amazonlocation.utils.SignOutInterface
import com.aws.amazonlocation.utils.disconnectFromAWSDialog
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.regionDisplayName
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.signOutDialog
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SettingFragment : BaseFragment(), SignOutInterface {

    private lateinit var aWSCloudInformationFragment: AWSCloudInformationFragment
    private lateinit var mBinding: FragmentSettingBinding
    private var mAuthStatus: String? = null
    private var isTablet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSettingBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val width = resources.getDimensionPixelSize(R.dimen.screen_size)
        mBinding.clSettings?.layoutParams?.width = width
        mBinding.clSettings?.requestLayout()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if ((activity is MainActivity)) {
            isTablet = (activity as MainActivity).isTablet
        }
        init()
        getUnitSystem()
        getMapStyle()
        getRegion()
        clickListener()
        if (isTablet) {
            addReplaceFragment(
                R.id.frame_container,
                UnitSystemFragment(),
                addFragment = true,
                addToBackStack = false
            )
            mBinding.apply {
                clUnitSystem.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                ivUnitSystem.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
            }
            (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.UNITS)
        }
    }

    private fun init() {
        mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        mBinding.apply {
            if (!mAuthStatus.isNullOrEmpty()) {
                when (mAuthStatus) {
                    AuthEnum.SIGNED_IN.name -> {
                        ivDisconnect.setImageDrawable(ContextCompat.getDrawable(ivDisconnect.context, R.drawable.icon_log_out))
                        tvDisconnect.text = getText(R.string.label_sign_out)
                        clDisconnect.show()
                        clRegion.hide()
                    }
                    AuthEnum.AWS_CONNECTED.name -> {
                        ivDisconnect.setImageDrawable(ContextCompat.getDrawable(ivDisconnect.context, R.drawable.ic_plug))
                        tvDisconnect.text = getString(R.string.label_disconnect)
                        clDisconnect.show()
                        clRegion.hide()
                    }
                    else -> {
                        clDisconnect.hide()
                        clRegion.show()
                    }
                }
            } else {
                clDisconnect.hide()
            }
            setSelectedLanguage(getLanguageCode())
        }
    }

    private fun setSelectedLanguage(languageCode: String?) {
        mBinding.apply {
            when (languageCode) {
                LANGUAGE_CODE_GERMAN -> {
                    tvLanguageName.text = getString(R.string.label_deutsch)
                }
                LANGUAGE_CODE_SPANISH -> {
                    tvLanguageName.text = getString(R.string.label_espa_ol)
                }
                LANGUAGE_CODE_ENGLISH -> {
                    tvLanguageName.text = getString(R.string.label_english)
                }
                LANGUAGE_CODE_FRENCH -> {
                    tvLanguageName.text = getString(R.string.label_fran_ais)
                }
                LANGUAGE_CODE_ITALIAN -> {
                    tvLanguageName.text = getString(R.string.label_italiano)
                }
                LANGUAGE_CODE_BR_PT -> {
                    tvLanguageName.text = getString(R.string.label_portugu_s_brasileiro)
                }
                LANGUAGE_CODE_CH_CN -> {
                    tvLanguageName.text = getString(R.string.label_simplified_chinese)
                }
                LANGUAGE_CODE_CH_TW -> {
                    tvLanguageName.text = getString(R.string.label_traditional_chinese)
                }
                LANGUAGE_CODE_JAPANESE -> {
                    tvLanguageName.text = getString(R.string.label_japanese)
                }
                LANGUAGE_CODE_KOREAN -> {
                    tvLanguageName.text = getString(R.string.label_korean)
                }
                LANGUAGE_CODE_ARABIC -> {
                    tvLanguageName.text = getString(R.string.label_arabic)
                }
                LANGUAGE_CODE_HEBREW, LANGUAGE_CODE_HEBREW_1 -> {
                    tvLanguageName.text = getString(R.string.label_hebrew)
                }
                LANGUAGE_CODE_HINDI -> {
                    tvLanguageName.text = getString(R.string.label_hindi)
                }
                else -> {
                    tvLanguageName.text = getString(R.string.label_english)
                }
            }
        }
    }
    private fun getUnitSystem() {
        val unitSystem =
            mPreferenceManager.getValue(KEY_UNIT_SYSTEM, resources.getString(R.string.automatic))
        mBinding.tvUnitSystemName.text = unitSystem
    }

    private fun getMapStyle() {
        val mapStyleNameDisplay =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_standard))
                ?: getString(R.string.map_standard)
        mBinding.tvMapStyleName.text = mapStyleNameDisplay
    }
    private fun getRegion() {
        val selectedRegion = mPreferenceManager.getValue(KEY_SELECTED_REGION, regionDisplayName[0])
        if (selectedRegion != null) {
            when (selectedRegion) {
                regionDisplayName[0] -> {
                    mBinding.tvRegionName.text = selectedRegion
                }
                else -> {
                    mBinding.tvRegionName.text = selectedRegion.split("(")[0].trim()
                }
            }
        }
    }

    private fun clickListener() {
        mBinding.apply {
            clUnitSystem.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        UnitSystemFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clUnitSystem.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivUnitSystem.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.unit_system_fragment)
                }
                (activity as MainActivity).exitScreen()
                (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.UNITS)
            }
            clMapStyle.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        MapStyleFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clMapStyle.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivMapStyle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.map_style_fragment)
                }
                (activity as MainActivity).exitScreen()
                (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.MAP_STYLE)
            }
            clRouteOption.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        RouteOptionFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clRouteOption.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivRouteOption.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.route_option_fragment)
                }
                (activity as MainActivity).exitScreen()
                (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.DEFAULT_ROUTE_OPTIONS)
            }
            clAwsCloudformation.setOnClickListener {
                if (isTablet) {
                    aWSCloudInformationFragment = AWSCloudInformationFragment()
                    addReplaceFragment(
                        R.id.frame_container,
                        aWSCloudInformationFragment,
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clAwsCloudformation.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivAwsCloudFormation.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.aws_cloud_information_fragment)
                }
                (activity as MainActivity).exitScreen()
                (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.CONNECT_YOUR_AWS_ACCOUNT)
                val propertiesAws = listOf(
                    Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
                )
                (activity as MainActivity).analyticsUtils?.recordEvent(EventType.AWS_ACCOUNT_CONNECTION_STARTED, propertiesAws)
            }
            clLanguage.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        LanguageFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clLanguage.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivLanguage.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.language_fragment)
                }
                (activity as MainActivity).exitScreen()
                (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.LANGUAGES)
            }
            clRegion.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        RegionFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clRegion.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivRegion.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.region_fragment)
                }
            }
            clDisconnect.setOnClickListener {
                if (tvDisconnect.text.toString().trim() == getText(R.string.label_sign_out)) {
                    requireContext().signOutDialog(this@SettingFragment)
                } else {
                    requireContext().disconnectFromAWSDialog(
                        object : DisconnectAWSInterface {
                            override fun disconnectAWS(dialog: DialogInterface) {
                                lifecycleScope.launch {
                                    mPreferenceManager.setDefaultConfig()
                                }
                                if (isTablet) {
                                    if (this@SettingFragment::aWSCloudInformationFragment.isInitialized) {
                                        if (aWSCloudInformationFragment.isVisible) {
                                            aWSCloudInformationFragment.refresh()
                                        }
                                    }
                                }
                                (activity as MainActivity).initClient()
                                init()
                                dialog.dismiss()
                            }

                            override fun logoutAndDisconnectAWS(dialog: DialogInterface) {
                            }
                        },
                        false
                    )
                }
            }
        }
    }

    fun hideKeyBoard() {
        if (isTablet) {
            if (this::aWSCloudInformationFragment.isInitialized) {
                if (aWSCloudInformationFragment.isVisible) {
                    aWSCloudInformationFragment.hideKeyBoard()
                }
            }
        }
    }

    fun refreshAfterSignOut() {
        if (isTablet) {
            if (this::aWSCloudInformationFragment.isInitialized) {
                if (aWSCloudInformationFragment.isVisible) {
                    aWSCloudInformationFragment.refreshAfterSignOut()
                }
            }
        } else {
            val propertiesAws = listOf(
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
            )
            (activity as MainActivity).analyticsUtils?.recordEvent(EventType.SIGN_OUT_SUCCESSFUL, propertiesAws)
            showError(getString(R.string.sign_out_successfully))
        }
        init()
    }

    fun refreshAfterConnection() {
        init()
    }

    fun refreshAfterSignIn() {
        init()
        if (this::aWSCloudInformationFragment.isInitialized) {
            if (aWSCloudInformationFragment.isVisible) {
                aWSCloudInformationFragment.refresh()
            }
        }
    }

    private fun FragmentSettingBinding.setDefaultSelection() {
        clUnitSystem.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        ivUnitSystem.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_img_tint
            )
        )
        clAwsCloudformation.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        ivAwsCloudFormation.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_img_tint
            )
        )
        clRouteOption.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        ivRouteOption.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_img_tint
            )
        )
        clMapStyle.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        ivMapStyle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_img_tint))
        clLanguage.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        ivLanguage.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_img_tint))
        clRegion.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        ivRegion.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_img_tint))
    }

    override fun logout(dialog: DialogInterface, isDisconnectFromAWSRequired: Boolean) {
        (activity as MainActivity).openSignOut()
    }
}
