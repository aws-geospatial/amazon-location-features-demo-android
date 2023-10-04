package com.aws.amazonlocation.ui.main.map_style // ktlint-disable package-name

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetMapStyleBinding
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
import com.aws.amazonlocation.ui.main.explore.MapStyleAdapter
import com.aws.amazonlocation.ui.main.explore.SortingAdapter
import com.aws.amazonlocation.utils.DELAY_300
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideSoftKeyboard
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.isGrabMapEnable
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.textChanges
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
class MapStyleBottomSheetFragment(
    private val mViewModel: ExploreViewModel,
    private val mBaseActivity: BaseActivity? = null,
    private val mapInterface: MapStyleAdapter.MapInterface
) : BottomSheetDialogFragment() {

    private lateinit var mBinding: BottomSheetMapStyleBinding
    private var mMapStyleAdapter: MapStyleAdapter? = null
    private var mProviderAdapter: SortingAdapter? = null
    private var mAttributeAdapter: SortingAdapter? = null
    private var mTypeAdapter: SortingAdapter? = null
    private var isFilterApplied: Boolean = false
    private var isNoDataFoundVisible: Boolean = false

    @Inject
    lateinit var mPreferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.CustomBottomSheetDialogTheme
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { layout ->
                val behaviour = BottomSheetBehavior.from(layout)
                behaviour.isDraggable = false
                setupFullHeight(layout)
            }
        }
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = BottomSheetMapStyleBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        mBinding.apply {
            rvMapStyle.apply {
                mViewModel.setMapListData(context, isGrabMapEnable(mPreferenceManager))
                val mapName =
                    mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                        ?: getString(R.string.map_esri)
                val mapStyleName =
                    mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_light))
                        ?: getString(R.string.map_light)
                mViewModel.mStyleList.forEach {
                    if (it.styleNameDisplay.equals(mapName)) {
                        it.isSelected = true
                        it.mapInnerData?.forEach { mapStyleInnerData ->
                            if (mapStyleInnerData.mapName.equals(mapStyleName)) {
                                mapStyleInnerData.isSelected = true
                            }
                        }
                    } else {
                        it.isSelected = false
                    }
                }
                layoutNoDataFound.tvNoMatchingFound.text = getString(R.string.label_style_search_error_title)
                layoutNoDataFound.tvMakeSureSpelledCorrect.text = getString(R.string.label_style_search_error_des)
                if (!isGrabMapEnable(mPreferenceManager)) {
                    cardGrabMap.hide()
                }
                setMapTileSelection(mapName)
                this.layoutManager = LinearLayoutManager(requireContext())
                mMapStyleAdapter =
                    MapStyleAdapter(
                        mViewModel.mStyleList,
                        object : MapStyleAdapter.MapInterface {
                            override fun mapStyleClick(position: Int, innerPosition: Int) {
                                if (position != -1 && innerPosition != -1) {
                                    mapInterface.mapStyleClick(position, innerPosition)
                                }
                            }
                        }
                    )
                this.adapter = mMapStyleAdapter

                if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true) {
                    cardGrabMap.isClickable = false
                    cardGrabMap.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_img_tint))
                } else {
                    cardGrabMap.isClickable = true
                    cardGrabMap.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
                mViewModel.mStyleList.forEachIndexed { index, mapStyleData ->
                    if (mapStyleData.styleNameDisplay.equals(getString(R.string.grab))) {
                        mapStyleData.isDisable = mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true
                        mMapStyleAdapter?.notifyItemChanged(index)
                    }
                }
            }
            rvProvider.layoutManager = LinearLayoutManager(requireContext())
            mProviderAdapter =
                SortingAdapter(
                    mViewModel.providerOptions,
                    object : SortingAdapter.MapInterface {
                        override fun mapClick(position: Int, isSelected: Boolean) {
                            if (position != -1) {
                                mViewModel.providerOptions[position].isSelected = isSelected
                            }
                        }
                    }
                )
            rvProvider.adapter = mProviderAdapter

            rvAttribute.layoutManager = LinearLayoutManager(requireContext())
            mAttributeAdapter =
                SortingAdapter(
                    mViewModel.attributeOptions,
                    object : SortingAdapter.MapInterface {
                        override fun mapClick(position: Int, isSelected: Boolean) {
                            if (position != -1) {
                                mViewModel.attributeOptions[position].isSelected = isSelected
                            }
                        }
                    }
                )
            rvAttribute.adapter = mAttributeAdapter

            rvType.layoutManager = LinearLayoutManager(requireContext())
            mTypeAdapter =
                SortingAdapter(
                    mViewModel.typeOptions,
                    object : SortingAdapter.MapInterface {
                        override fun mapClick(position: Int, isSelected: Boolean) {
                            if (position != -1) {
                                mViewModel.typeOptions[position].isSelected = isSelected
                            }
                        }
                    }
                )
            rvType.adapter = mTypeAdapter
            ivMapStyleClose.setOnClickListener {
                mapStyleShowList()
                if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true) {
                    mBaseActivity.mSimulationUtils?.setSimulationDraggable()
                }
                dismiss()
            }

            etSearchMap.textChanges().debounce(DELAY_300).onEach { text ->
                mapStyleShowList()
                searchText(text)
            }.launchIn(lifecycleScope)
            val params = cardSearchFilter.layoutParams
            tilSearch.setEndIconOnClickListener {
                isNoDataFoundVisible = false
                mapStyleShowList()
                setDefaultMapStyleList()
                etSearchMap.setText("")
                tilSearch.clearFocus()
                etSearchMap.clearFocus()
                tilSearch.isEndIconVisible = false
                activity?.hideSoftKeyboard(etSearchMap)
                val providerNames = arrayListOf<String>()
                val attributeNames = arrayListOf<String>()
                val typeNames = arrayListOf<String>()
                mViewModel.providerOptions.filter { it.isSelected }
                    .forEach { data -> providerNames.add(data.name) }
                mViewModel.attributeOptions.filter { it.isSelected }
                    .forEach { data -> attributeNames.add(data.name) }
                mViewModel.typeOptions.filter { it.isSelected }
                    .forEach { data -> typeNames.add(data.name) }
                val filterList = mViewModel.filterAndSortItems(
                    requireContext(),
                    null,
                    providerNames.ifEmpty { null },
                    attributeNames.ifEmpty { null },
                    typeNames.ifEmpty { null }
                )
                if (filterList.isNotEmpty()) {
                    mViewModel.mStyleList.clear()
                    mViewModel.mStyleList.addAll(filterList)
                    checkSimulationDisableForGrab()
                    activity?.runOnUiThread {
                        mMapStyleAdapter?.notifyDataSetChanged()
                    }
                    mapStyleShowList()
                    layoutNoDataFound.root.hide()
                    tvClearFilter.hide()
                    isNoDataFoundVisible = false
                } else {
                    isNoDataFoundVisible = true
                    mViewModel.mStyleList.clear()
                    activity?.runOnUiThread {
                        mMapStyleAdapter?.notifyDataSetChanged()
                    }
                    mapStyleShowList()
                    showNoDataFoundFilter()
                }
            }
            tilSearch.isEndIconVisible = false
            ivSearch.setOnClickListener {
                openSearch(params)
            }

            tvClearFilter.setOnClickListener {
                clearSortingSelection()
                notifySortingAdapter()
                setFilterNotSelected()
                searchText(etSearchMap.text.toString().trim())
            }
            tvClearSelection.setOnClickListener {
                clearSortingSelection()
                notifySortingAdapter()
                imgFilterSelected.hide()
                isNoDataFoundVisible = false
                isFilterApplied = false
                layoutNoDataFound.root.hide()
                tvClearFilter.hide()
                mViewModel.mStyleList.clear()
                mViewModel.mStyleList.addAll(mViewModel.mStyleListForFilter)
                checkSimulationDisableForGrab()
                mMapStyleAdapter?.notifyDataSetChanged()
            }
            btnApplyFilter.setOnClickListener {
                val providerNames = arrayListOf<String>()
                val attributeNames = arrayListOf<String>()
                val typeNames = arrayListOf<String>()
                mViewModel.providerOptions.filter { it.isSelected }
                    .forEach { data -> providerNames.add(data.name) }
                mViewModel.attributeOptions.filter { it.isSelected }
                    .forEach { data -> attributeNames.add(data.name) }
                mViewModel.typeOptions.filter { it.isSelected }
                    .forEach { data -> typeNames.add(data.name) }
                val filterList = mViewModel.filterAndSortItems(
                    requireContext(),
                    etSearchMap.text.toString().trim().ifEmpty { null },
                    providerNames.ifEmpty { null },
                    attributeNames.ifEmpty { null },
                    typeNames.ifEmpty { null }
                )
                if (providerNames.isNotEmpty() || attributeNames.isNotEmpty() || typeNames.isNotEmpty()) {
                    imgFilterSelected.show()
                    imgFilter.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_primary_green
                        )
                    )
                    isFilterApplied = true
                    if (providerNames.isNotEmpty()) {
                        providerNames.forEach { name ->
                            mViewModel.providerOptions.filter { it.name == name }
                                .forEach { data -> data.isApplyFilter = true }
                        }
                    }
                    if (attributeNames.isNotEmpty()) {
                        attributeNames.forEach { name ->
                            mViewModel.attributeOptions.filter { it.name == name }
                                .forEach { data -> data.isApplyFilter = true }
                        }
                    }
                    if (typeNames.isNotEmpty()) {
                        typeNames.forEach { name ->
                            mViewModel.typeOptions.filter { it.name == name }
                                .forEach { data -> data.isApplyFilter = true }
                        }
                    }
                    mViewModel.providerOptions.filter { !it.isSelected }
                        .forEach { data -> data.isApplyFilter = false }
                    mViewModel.attributeOptions.filter { !it.isSelected }
                        .forEach { data -> data.isApplyFilter = false }
                    mViewModel.typeOptions.filter { !it.isSelected }
                        .forEach { data -> data.isApplyFilter = false }
                } else {
                    setFilterNotSelected()
                }
                if (filterList.isNotEmpty()) {
                    mViewModel.mStyleList.clear()
                    mViewModel.mStyleList.addAll(filterList)
                    checkSimulationDisableForGrab()
                    activity?.runOnUiThread {
                        mMapStyleAdapter?.notifyDataSetChanged()
                    }
                    mapStyleShowList()
                    isNoDataFoundVisible = false
                } else {
                    isNoDataFoundVisible = true
                    mViewModel.mStyleList.clear()
                    activity?.runOnUiThread {
                        mMapStyleAdapter?.notifyDataSetChanged()
                    }
                    mapStyleShowList()
                    showNoDataFoundFilter()
                }
            }
            imgFilter.setOnClickListener {
                if (nsvFilter.isVisible) {
                    mViewModel.providerOptions.forEach {
                        if (it.isSelected) {
                            if (!it.isApplyFilter) {
                                it.isSelected = false
                            }
                        } else {
                            if (it.isApplyFilter) {
                                it.isSelected = true
                            }
                        }
                    }
                    mViewModel.attributeOptions.forEach {
                        if (it.isSelected) {
                            if (!it.isApplyFilter) {
                                it.isSelected = false
                            }
                        } else {
                            if (it.isApplyFilter) {
                                it.isSelected = true
                            }
                        }
                    }
                    mViewModel.typeOptions.forEach {
                        if (it.isSelected) {
                            if (!it.isApplyFilter) {
                                it.isSelected = false
                            }
                        } else {
                            if (it.isApplyFilter) {
                                it.isSelected = true
                            }
                        }
                    }
                    notifySortingAdapter()
                    mapStyleShowList()
                    if (isNoDataFoundVisible) {
                        layoutNoDataFound.root.show()
                        if (isFilterApplied) {
                            tvClearFilter.show()
                        } else {
                            tvClearFilter.hide()
                        }
                    }
                    collapseSearchMap(params)
                } else {
                    activity?.hideSoftKeyboard(etSearchMap)
                    mapStyleShowSorting()
                    openSearch(params)
                    imgFilter.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_primary_green
                        )
                    )
                }
            }
            clSearchMapStyle.setOnClickListener {
                if (etSearchMap.text.isNullOrEmpty() && tilSearch.isVisible && !nsvFilter.isVisible) {
                    collapseSearchMap(params)
                }
            }
            cardEsri.setOnClickListener {
                val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                if (mapName != getString(R.string.esri)) {
                    mapInterface.mapStyleClick(0, 0)
                }
            }
            cardHere.setOnClickListener {
                val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                if (mapName != getString(R.string.here)) {
                    mapInterface.mapStyleClick(1, 0)
                }
            }
            cardOpenData.setOnClickListener {
                val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                if (mapName != getString(R.string.open_data)) {
                    mapInterface.mapStyleClick(3, 0)
                }
            }
            cardGrabMap.setOnClickListener {
                if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true) {
                    return@setOnClickListener
                }
                val mapName =
                    mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                if (mapName != getString(R.string.grab)) {
                    mapInterface.mapStyleClick(2, 0)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifySortingAdapter() {
        mTypeAdapter?.notifyDataSetChanged()
        mAttributeAdapter?.notifyDataSetChanged()
        mProviderAdapter?.notifyDataSetChanged()
    }

    private fun BottomSheetMapStyleBinding.showNoDataFoundFilter() {
        layoutNoDataFound.root.show()
        if (isFilterApplied) {
            tvClearFilter.show()
        } else {
            tvClearFilter.hide()
        }
        rvMapStyle.hide()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun BottomSheetMapStyleBinding.searchText(text: CharSequence?) {
        tilSearch.isEndIconVisible = !text.isNullOrEmpty()
        val providerNames = arrayListOf<String>()
        val attributeNames = arrayListOf<String>()
        val typeNames = arrayListOf<String>()
        mViewModel.providerOptions.filter { it.isSelected }
            .forEach { data -> providerNames.add(data.name) }
        mViewModel.attributeOptions.filter { it.isSelected }
            .forEach { data -> attributeNames.add(data.name) }
        mViewModel.typeOptions.filter { it.isSelected }
            .forEach { data -> typeNames.add(data.name) }
        val filterList = mViewModel.filterAndSortItems(
            requireContext(),
            text.toString().ifEmpty { null },
            providerNames.ifEmpty { null },
            attributeNames.ifEmpty { null },
            typeNames.ifEmpty { null }
        )
        if (filterList.isNotEmpty()) {
            mViewModel.mStyleList.clear()
            mViewModel.mStyleList.addAll(filterList)
            checkSimulationDisableForGrab()
            activity?.runOnUiThread {
                mMapStyleAdapter?.notifyDataSetChanged()
            }
            rvMapStyle.show()
            layoutNoDataFound.root.hide()
            tvClearFilter.hide()
            isNoDataFoundVisible = false
        } else {
            mViewModel.mStyleList.clear()
            activity?.runOnUiThread {
                mMapStyleAdapter?.notifyDataSetChanged()
            }
            isNoDataFoundVisible = true
            showNoDataFoundFilter()
        }
    }

    private fun BottomSheetMapStyleBinding.setFilterNotSelected() {
        isFilterApplied = false
        imgFilterSelected.hide()
        imgFilter.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_img_tint
            )
        )
    }

    private fun clearSortingSelection() {
        mViewModel.providerOptions.forEachIndexed { index, _ ->
            mViewModel.providerOptions[index].isSelected = false
            mViewModel.providerOptions[index].isApplyFilter = false
        }
        mViewModel.attributeOptions.forEachIndexed { index, _ ->
            mViewModel.attributeOptions[index].isSelected = false
            mViewModel.attributeOptions[index].isApplyFilter = false
        }
        mViewModel.typeOptions.forEachIndexed { index, _ ->
            mViewModel.typeOptions[index].isSelected = false
            mViewModel.typeOptions[index].isApplyFilter = false
        }
    }

    private fun checkSimulationDisableForGrab() {
        mViewModel.mStyleList.forEachIndexed { _, mapStyleData ->
            if (mapStyleData.styleNameDisplay.equals(getString(R.string.grab))) {
                mapStyleData.isDisable =
                    mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true
            }
        }
    }

    private fun BottomSheetMapStyleBinding.openSearch(params: ViewGroup.LayoutParams?) {
        viewLine.hide()
        tilSearch.show()
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        cardSearchFilter.layoutParams = params
        etSearchMap.clearFocus()
        groupFilterButton.hide()
        scrollMapStyle.isFillViewport = true
    }

    private fun BottomSheetMapStyleBinding.collapseSearchMap(params: ViewGroup.LayoutParams?) {
        if (!isFilterApplied) {
            imgFilter.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_img_tint
                )
            )
        }
        activity?.runOnUiThread {
            scrollMapStyle.isFillViewport = false
            params?.width = ViewGroup.LayoutParams.WRAP_CONTENT
            cardSearchFilter.layoutParams = params
        }
        tilSearch.hide()
        viewLine.show()
        if (!isGrabMapEnable(mPreferenceManager)) {
            cardGrabMap.hide()
            cardEsri.show()
            cardHere.show()
        } else {
            groupFilterButton.show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun BottomSheetMapStyleBinding.setDefaultMapStyleList() {
        mViewModel.mStyleList.clear()
        mViewModel.mStyleList.addAll(mViewModel.mStyleListForFilter)
        checkSimulationDisableForGrab()
        activity?.runOnUiThread {
            etSearchMap.setText("")
            mMapStyleAdapter?.notifyDataSetChanged()
        }
    }

    private fun BottomSheetMapStyleBinding.mapStyleShowSorting() {
        showViews(
            nsvFilter,
            viewDivider,
            tvClearSelection,
            btnApplyFilter
        )
        layoutNoDataFound.root.hide()
        tvClearFilter.hide()
        rvMapStyle.hide()
    }

    private fun BottomSheetMapStyleBinding.mapStyleShowList() {
        hideViews(
            nsvFilter,
            viewDivider,
            tvClearSelection,
            btnApplyFilter
        )
        rvMapStyle.show()
    }

    fun refreshMapTile(mapName: String) {
        mBinding.apply {
            setMapTileSelection(mapName)
        }
    }
    private fun BottomSheetMapStyleBinding.setMapTileSelection(
        mapName: String
    ) {
        val colorToSet = ContextCompat.getColor(requireContext(), R.color.color_primary_green)

        val selectedCard: MaterialCardView = when (mapName) {
            resources.getString(R.string.esri) -> {
                cardEsri
            }
            resources.getString(R.string.here) -> {
                cardHere
            }
            resources.getString(R.string.grab) -> {
                cardGrabMap
            }
            resources.getString(R.string.open_data) -> {
                cardOpenData
            }
            else -> cardEsri
        }

        val cardList = listOf(cardEsri, cardHere, cardGrabMap, cardOpenData)

        cardList.forEach { card ->
            card.strokeColor = if (card == selectedCard) colorToSet
            else ContextCompat.getColor(requireContext(), R.color.white)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyAdapter() {
        mMapStyleAdapter?.notifyDataSetChanged()
    }
}
