package com.aws.amazonlocation.ui.main.map_style

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetMapStickyBinding
import com.aws.amazonlocation.databinding.BottomSheetMapStyleBinding
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
import com.aws.amazonlocation.ui.main.explore.MapStyleAdapter
import com.aws.amazonlocation.ui.main.explore.PoliticalAdapter
import com.aws.amazonlocation.utils.ATTRIBUTE_DARK
import com.aws.amazonlocation.utils.ATTRIBUTE_LIGHT
import com.aws.amazonlocation.utils.BottomSheetHelper
import com.aws.amazonlocation.utils.DELAY_300
import com.aws.amazonlocation.utils.KEY_COLOR_SCHEMES
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_POLITICAL_VIEW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideKeyboard
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.invisible
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.textChanges
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
class MapStyleBottomSheetFragment(
    private val mViewModel: ExploreViewModel,
    private val mBaseActivity: BaseActivity? = null,
    private val bottomSheetHelper: BottomSheetHelper,
    private val mapInterface: MapInterface,
) : BottomSheetDialogFragment() {
    private var behaviour: BottomSheetBehavior<FrameLayout>?= null
    private lateinit var mBinding: BottomSheetMapStyleBinding
    private lateinit var buttonStickyBinding: BottomSheetMapStickyBinding

    private var mMapStyleAdapter: MapStyleAdapter? = null
    private var mPoliticalAdapter: PoliticalAdapter? = null
    @Inject
    lateinit var mPreferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.CustomBottomSheetDialogTheme,
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout
            parentLayout?.let { layout ->
                val colorScheme = mPreferenceManager.getValue(KEY_COLOR_SCHEMES, ATTRIBUTE_LIGHT) ?: ATTRIBUTE_LIGHT
                val logoResId =
                    when (colorScheme) {
                        ATTRIBUTE_LIGHT,
                        -> R.drawable.ic_amazon_logo_on_light

                        ATTRIBUTE_DARK,
                        -> R.drawable.ic_amazon_logo_on_dark

                        else -> R.drawable.ic_amazon_logo_on_light
                    }
                setImageIcon(logoResId)
                behaviour = BottomSheetBehavior.from(layout)
                behaviour?.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                mBinding.imgAmazonLogoMapStyle?.alpha = 1f
                                mBinding.ivAmazonInfoMapStyle?.alpha = 1f
                                bottomSheetHelper.directionSheetDraggable(false)
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                mBinding.imgAmazonLogoMapStyle?.alpha = 0f
                                mBinding.ivAmazonInfoMapStyle?.alpha = 0f
                                bottomSheetHelper.directionSheetDraggable(false)
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                                mBinding.imgAmazonLogoMapStyle?.alpha = 1f
                                mBinding.ivAmazonInfoMapStyle?.alpha = 1f
                                if (isMapStyleExpandedOrHalfExpand() && bottomSheetHelper.isDirectionSearchSheetVisible()) {
                                    bottomSheetHelper.collapseDirectionSearch()
                                }
                                bottomSheetHelper.hideSearchBottomSheet(true)
                                bottomSheetHelper.directionSheetDraggable(false)
                                activity?.hideKeyboard()
                            }
                            BottomSheetBehavior.STATE_HIDDEN -> {
                                mapStyleHide()
                            }
                            BottomSheetBehavior.STATE_SETTLING -> {
                            }
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }
                })
                behaviour?.isDraggable = mBaseActivity?.isTablet != true
                setupFullHeight(layout)
                if (mBaseActivity?.isTablet != true) {
                    val layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM
                    )
                    (dialog.findViewById<View>(android.R.id.content) as ViewGroup).addView(
                        buttonStickyBinding.root,
                        layoutParams
                    )
                }
            }
        }
        if (mBaseActivity?.isTablet == true) {
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            dialog.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            dialog.behavior.halfExpandedRatio = 0.58f
            dialog.behavior.isFitToContents = false
            dialog.setCanceledOnTouchOutside(false) // Prevent collapse on outside click
            dialog.behavior.expandedOffset =
                requireContext().resources.getDimension(R.dimen.dp_15).toInt()
            dialog.window?.setDimAmount(0f)
        }
        return dialog
    }

    private fun mapStyleHide() {
        bottomSheetHelper.directionSheetDraggable(true)
        if (mBaseActivity?.mTrackingUtils?.isTrackingSheetCollapsed() != null) {
            mBaseActivity.mTrackingUtils?.isTrackingSheetCollapsed()
                ?.let {
                    if (!bottomSheetHelper.isDirectionSearchSheetVisible() && !bottomSheetHelper.isDirectionSheetVisible()) {
                        if (!it && mBaseActivity.mGeofenceUtils?.isGeofenceSheetCollapsed() != null) {
                            mBaseActivity.mGeofenceUtils?.isGeofenceSheetCollapsed()
                                ?.let { it1 ->
                                    if (!it1) {
                                        if (mBaseActivity.mSimulationUtils?.isSimulationBottomSheetVisible() != true) {
                                            bottomSheetHelper.hideSearchBottomSheet(false)
                                        } else {
                                            mBaseActivity.mSimulationUtils?.setSimulationDraggable()
                                        }
                                    } else {
                                        mBaseActivity.bottomNavigationVisibility(true)
                                    }
                                }
                        } else {
                            mBaseActivity.bottomNavigationVisibility(true)
                        }
                    }
                }
        } else {
            if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() != true) {
                bottomSheetHelper.hideSearchBottomSheet(false)
            } else {
                mBaseActivity.mSimulationUtils?.setSimulationDraggable()
            }
        }
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        buttonStickyBinding = BottomSheetMapStickyBinding.inflate(LayoutInflater.from(requireContext()))
        mBinding = BottomSheetMapStyleBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroy() {
        behaviour = null
        super.onDestroy()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        mBinding.apply {
            val languageCode = getLanguageCode()
            val isRtl =
                languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW || languageCode == LANGUAGE_CODE_HEBREW_1
            val selectedCountry = mPreferenceManager.getValue(KEY_POLITICAL_VIEW, "") ?: ""
            selectedCountry.takeIf { it.isNotEmpty() }?.let { country ->
                mViewModel.mPoliticalSearchData.find { it.countryName == country }?.let {
                    it.isSelected = true
                    tvPoliticalDescription.apply {
                        text = "${it.countryName}. ${it.description}"
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }

                }
            }
            rvPoliticalView.layoutManager = LinearLayoutManager(requireContext())
            mPoliticalAdapter = PoliticalAdapter(
                mViewModel.mPoliticalData,
                isRtl,
                object : PoliticalAdapter.PoliticalInterface {
                    override fun countryClick(position: Int) {
                        if (mViewModel.mPoliticalData[position].isSelected) return
                        mViewModel.mPoliticalSearchData.forEach {
                            it.isSelected = false
                        }
                        mViewModel.mPoliticalData.forEach {
                            it.isSelected = false
                        }
                        hideKeyboard(requireActivity(), etSearchCountry)
                        mViewModel.mPoliticalData[position].isSelected = true
                        mPoliticalAdapter?.notifyDataSetChanged()
                        if (mBaseActivity?.isTablet != true) {
                            buttonStickyBinding.root.show()
                            clApply.invisible()
                        }  else clApply.show()
                    }
                },
            )
            rvPoliticalView.adapter = mPoliticalAdapter

            rvMapStyle.apply {
                mViewModel.setMapListData(context)
                val mapStyleName =
                    mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_standard))
                        ?: getString(R.string.map_standard)
                val colorScheme = mPreferenceManager.getValue(KEY_COLOR_SCHEMES, ATTRIBUTE_LIGHT) ?: ATTRIBUTE_LIGHT
                toggleMode.check(if(colorScheme == ATTRIBUTE_LIGHT) R.id.btn_light else R.id.btn_dark)
                mViewModel.mStyleList.forEach {
                    it.isSelected = true
                    it.mapInnerData?.forEach { mapStyleInnerData ->
                        if (mapStyleInnerData.mapName.equals(mapStyleName)) {
                            if (mapStyleInnerData.mapName == getString(R.string.map_satellite) || mapStyleInnerData.mapName == getString(R.string.map_hybrid)) {
                                disableToggle()
                            } else {
                                enableToggle()
                            }
                            mapStyleInnerData.isSelected = true
                        }
                    }
                }
                layoutNoDataFoundPolitical.tvNoMatchingFound.text =
                    getString(R.string.label_style_search_error_title)
                layoutNoDataFoundPolitical.tvMakeSureSpelledCorrect.text =
                    getString(R.string.label_style_search_error_des)
                this.layoutManager = LinearLayoutManager(requireContext())
                mMapStyleAdapter =
                    MapStyleAdapter(
                        mViewModel.mStyleList,
                        object : MapStyleAdapter.MapInterface {
                            override fun mapStyleClick(
                                position: Int,
                                innerPosition: Int,
                            ) {
                                if (position != -1 && innerPosition != -1) {
                                    mapInterface.mapStyleClick(position, innerPosition)
                                }
                            }
                        },
                    )
                this.adapter = mMapStyleAdapter
            }
            ivMapStyleClose.setOnClickListener {
                etSearchCountry.setText("")
                tvMapStyle.text = getString(R.string.label_map_style)
                showViews(rvMapStyle, cardColorScheme, clPoliticalView)
                hideViews(clSearchPolitical, ivBack, clApply)
                val selectedCountry = mPreferenceManager.getValue(KEY_POLITICAL_VIEW, "") ?: ""
                clearSelectionAndSetOriginalData(selectedCountry)
                if (mBaseActivity?.isTablet != true) buttonStickyBinding.root.hide()
                mapStyleShowList()
                if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true) {
                    mBaseActivity.mSimulationUtils?.setSimulationDraggable()
                }
                mapStyleHide()
                dismiss()
            }
            mBinding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
                when (checkedId) {
                    R.id.btn_light -> {
                        if (isChecked) {
                            mPreferenceManager.setValue(KEY_COLOR_SCHEMES, ATTRIBUTE_LIGHT)
                            val mapStyleNameDisplay =
                                mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_standard))
                                    ?: getString(R.string.map_standard)
                            mapInterface.mapColorScheme(ATTRIBUTE_LIGHT, mapStyleNameDisplay)

                        }
                    }
                    R.id.btn_dark -> {
                        if (isChecked) {
                            mPreferenceManager.setValue(KEY_COLOR_SCHEMES, ATTRIBUTE_DARK)
                            val mapStyleNameDisplay =
                                mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_standard))
                                    ?: getString(R.string.map_standard)
                            mapInterface.mapColorScheme(ATTRIBUTE_DARK, mapStyleNameDisplay)
                        }
                    }
                }
            }
            clPoliticalView.setOnClickListener {
                mViewModel.mPoliticalData.find { it.isSelected }?.let {
                    if (mBaseActivity?.isTablet != true) {
                        buttonStickyBinding.root.show()
                        clApply.invisible()
                    } else clApply.show()
                }
                tvMapStyle.text = getString(R.string.label_political_view)
                hideViews(rvMapStyle, cardColorScheme, clPoliticalView)
                showViews(clSearchPolitical, ivBack)
            }

            ivBack.setOnClickListener {
                etSearchCountry.setText("")
                tvMapStyle.text = getString(R.string.label_map_style)
                showViews(rvMapStyle, cardColorScheme, clPoliticalView)
                hideViews(clSearchPolitical, ivBack, clApply)
                val selectedCountry = mPreferenceManager.getValue(KEY_POLITICAL_VIEW, "") ?: ""
                clearSelectionAndSetOriginalData(selectedCountry)
                if (mBaseActivity?.isTablet != true) buttonStickyBinding.root.hide()
            }
            etSearchCountry
                .textChanges()
                .debounce(DELAY_300)
                .onEach { text ->
                    tilSearch.isEndIconVisible = !text.isNullOrEmpty()
                    val result =  mViewModel.searchPoliticalData(text.toString())
                    if (result.isEmpty()) {
                        layoutNoDataFoundPolitical.root.show()
                        mViewModel.mPoliticalData.clear()
                    } else {
                        layoutNoDataFoundPolitical.root.hide()
                        mViewModel.mPoliticalData.clear()
                        mViewModel.mPoliticalData.addAll(result)
                    }
                    activity?.runOnUiThread {
                        mPoliticalAdapter?.notifyDataSetChanged()
                    }
                }.launchIn(lifecycleScope)

            tilSearch.setEndIconOnClickListener {
                etSearchCountry.setText("")
            }
            layoutNoDataFoundPolitical.tvClearFilter.setOnClickListener {
                etSearchCountry.setText("")
            }
            buttonStickyBinding.tvClearSelection.setOnClickListener {
                clearSelection()
            }
            tvClearSelection.setOnClickListener {
                clearSelection()
            }
            buttonStickyBinding.btnApplyFilter.setOnClickListener {
                applyFilter()
            }

            btnApplyFilter.setOnClickListener {
                applyFilter()
            }
            tilSearch.isEndIconVisible = false
        }
    }

    private fun clearSelectionAndSetOriginalData(selectedCountry: String) {
        mViewModel.mPoliticalData.forEach {
            it.isSelected = false
        }
        mViewModel.mPoliticalSearchData.forEach {
            it.isSelected = false
        }
        if (selectedCountry.isNotEmpty()) {
            selectedCountry.takeIf { it.isNotEmpty() }?.let { country ->
                mViewModel.mPoliticalSearchData.find { it.countryName == country }?.let {
                    it.isSelected = true
                }
            }
        }
        mPoliticalAdapter?.notifyDataSetChanged()
    }

    private fun clearSelection() {
        mViewModel.mPoliticalData.forEach {
            it.isSelected = false
        }
        val selectedCountry = mPreferenceManager.getValue(KEY_POLITICAL_VIEW, "") ?: ""
        if (selectedCountry.isEmpty()) {
            if (mBaseActivity?.isTablet != true) {
                buttonStickyBinding.root.hide()
                mBinding.clApply.hide()
            } else mBinding.clApply.hide()
        }
        activity?.runOnUiThread {
            mPoliticalAdapter?.notifyDataSetChanged()
        }
    }

    private fun BottomSheetMapStyleBinding.applyFilter() {
        val selectedItem = mViewModel.mPoliticalData.find { it.isSelected }
        if (selectedItem != null) {
            mPreferenceManager.setValue(KEY_POLITICAL_VIEW, selectedItem.countryName)
            tvPoliticalDescription.apply {
                text = "${selectedItem.countryName}. ${selectedItem.description}"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
            }
        } else {
            mPreferenceManager.setValue(KEY_POLITICAL_VIEW, "")
            tvPoliticalDescription.apply {
                text = getString(R.string.label_map_representation_for_different_countries)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.color_hint_text))
            }
        }
        tvMapStyle.text = getString(R.string.label_map_style)
        showViews(rvMapStyle, cardColorScheme, clPoliticalView)
        hideViews(clSearchPolitical, ivBack, clApply)
        if (mBaseActivity?.isTablet != true) buttonStickyBinding.root.hide()
        val colorScheme =
            mPreferenceManager.getValue(KEY_COLOR_SCHEMES, ATTRIBUTE_LIGHT) ?: ATTRIBUTE_LIGHT
        val mapStyleNameDisplay =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_standard))
                ?: getString(R.string.map_standard)

        mapInterface.mapColorScheme(colorScheme, mapStyleNameDisplay)
    }

    private fun BottomSheetMapStyleBinding.mapStyleShowList() {
        rvMapStyle.show()
    }

    fun isMapStyleExpandedOrHalfExpand(): Boolean {
        if (behaviour == null) {
            return false
        }
        return behaviour!!.state == BottomSheetBehavior.STATE_HALF_EXPANDED || behaviour!!.state == BottomSheetBehavior.STATE_EXPANDED
    }
    fun expandMapStyleSheet() {
        if (behaviour == null) {
            return
        }
        behaviour?.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetHelper.hideSearchBottomSheet(true)
    }

    fun setImageIcon(logoResId: Int) {
        mBinding.imgAmazonLogoMapStyle?.setImageResource(logoResId)
    }

    fun hideMapStyleSheet() {
        if (behaviour == null) {
            return
        }
        behaviour?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyAdapter() {
        mMapStyleAdapter?.notifyDataSetChanged()
        val mapStyleName =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_standard))
                ?: getString(R.string.map_standard)
        if (mapStyleName == getString(R.string.map_satellite) || mapStyleName == getString(R.string.map_hybrid)) {
            disableToggle()
        } else {
            enableToggle()
        }
    }

    private fun enableToggle() {
        mBinding.toggleMode.isEnabled = true
        mBinding.toggleMode.alpha = 1.0f
    }

    private fun disableToggle() {
        mBinding.toggleMode.isEnabled = false
        mBinding.toggleMode.alpha = 0.5f
    }

    interface MapInterface {
        fun mapStyleClick(position: Int, innerPosition: Int)
        fun mapColorScheme(colorScheme: String, mapStyleName: String)
    }
}
