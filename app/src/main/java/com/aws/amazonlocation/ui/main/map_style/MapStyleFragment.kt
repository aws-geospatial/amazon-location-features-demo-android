package com.aws.amazonlocation.ui.main.map_style // ktlint-disable package-name

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentMapStyleBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.explore.SortingAdapter
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.DELAY_300
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.KEY_GRAB_DONT_ASK
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_NEAREST_REGION
import com.aws.amazonlocation.utils.KEY_OPEN_DATA_DONT_ASK
import com.aws.amazonlocation.utils.KEY_SELECTED_REGION
import com.aws.amazonlocation.utils.MapNames
import com.aws.amazonlocation.utils.MapStyleRestartInterface
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.enableOpenData
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideSoftKeyboard
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.isGrabMapEnable
import com.aws.amazonlocation.utils.isInternetAvailable
import com.aws.amazonlocation.utils.isRunningTest
import com.aws.amazonlocation.utils.regionDisplayName
import com.aws.amazonlocation.utils.restartAppMapStyleDialog
import com.aws.amazonlocation.utils.restartApplication
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.textChanges
import com.google.android.material.card.MaterialCardView
import kotlin.math.ceil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MapStyleFragment : BaseFragment() {

    private lateinit var mLayoutManagerEsri: GridLayoutManager
    private lateinit var mLayoutManagerHere: GridLayoutManager
    private lateinit var mLayoutManagerGrab: GridLayoutManager
    private lateinit var mBinding: FragmentMapStyleBinding
    private val mViewModel: MapStyleViewModel by viewModels()
    private var mAdapter: EsriMapStyleAdapter? = null
    private var mHereAdapter: EsriMapStyleAdapter? = null
    private var mGrabAdapter: EsriMapStyleAdapter? = null
    private var isTablet = false
    private var isLargeTablet = false
    private var columnCount = 3
    private var isGrabMapEnable = false
    private var mMapStyleAdapter: SettingMapStyleAdapter? = null
    private var mProviderAdapter: SortingAdapter? = null
    private var mAttributeAdapter: SortingAdapter? = null
    private var mTypeAdapter: SortingAdapter? = null
    private var isFilterApplied: Boolean = false
    private var isNoDataFoundVisible: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMapStyleBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if ((activity is MainActivity)) {
            isTablet = (activity as MainActivity).isTablet
        }
        isGrabMapEnable = isGrabMapEnable(mPreferenceManager)
        isLargeTablet = requireContext().resources.getBoolean(R.bool.is_large_tablet)
        if (isTablet) {
            setColumnCount()
        }
        mBinding.apply {
            mViewModel.setMapListData(requireContext(), isGrabMapEnable)
        }
        setMapStyleAdapter()
        backPress()
        clickListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setMapStyleAdapter() {
        mBinding.apply {
            mViewModel.setMapListData(rvMapStyle.context, isGrabMapEnable(mPreferenceManager))
            val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
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
            rvMapStyle.layoutManager = LinearLayoutManager(requireContext())
            mMapStyleAdapter =
                SettingMapStyleAdapter(
                    columnCount,
                    mViewModel.mStyleList,
                    object : SettingMapStyleAdapter.MapInterface {
                        override fun mapStyleClick(position: Int, innerPosition: Int) {
                            if (checkInternetConnection()) {
                                if (position != -1 && innerPosition != -1) {
                                    val selectedProvider =
                                        mViewModel.mStyleList[position].styleNameDisplay
                                    val selectedInnerData =
                                        mViewModel.mStyleList[position].mapInnerData?.get(
                                            innerPosition
                                        )?.mapName
                                    for (data in mViewModel.mStyleListForFilter) {
                                        if (data.styleNameDisplay.equals(selectedProvider)) {
                                            data.mapInnerData.let {
                                                if (it != null) {
                                                    for (innerData in it) {
                                                        if (innerData.mapName.equals(
                                                                selectedInnerData
                                                            )
                                                        ) {
                                                            if (innerData.isSelected) return
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    selectedProvider?.let {
                                        selectedInnerData?.let { it1 ->
                                            mapStyleChange(
                                                false,
                                                it,
                                                it1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            rvMapStyle.adapter = mMapStyleAdapter

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
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun mapStyleChange(
        isMapClick: Boolean,
        selectedProvider: String,
        selectedInnerData: String
    ) {
        val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
        val defaultIdentityPoolId: String = Units.getDefaultIdentityPoolId(
            mPreferenceManager.getValue(
                KEY_SELECTED_REGION,
                regionDisplayName[0]
            ),
            mPreferenceManager.getValue(KEY_NEAREST_REGION, "")
        )
        val isRestartNeeded =
            if (defaultIdentityPoolId == BuildConfig.DEFAULT_IDENTITY_POOL_ID_AP) {
                false
            } else {
                if (mapName == getString(R.string.esri) || mapName == getString(R.string.here)) {
                    selectedProvider == getString(R.string.grab)
                } else {
                    selectedProvider != getString(R.string.grab)
                }
            }
        if (selectedProvider == getString(R.string.grab) && mapName != getString(R.string.grab)) {
            val shouldShowGrabDialog = !mPreferenceManager.getValue(KEY_GRAB_DONT_ASK, false)
            if (shouldShowGrabDialog) {
                activity?.restartAppMapStyleDialog(object : MapStyleRestartInterface {
                    override fun onOkClick(dialog: DialogInterface, dontAskAgain: Boolean) {
                        mPreferenceManager.setValue(KEY_GRAB_DONT_ASK, dontAskAgain)
                        changeMapStyle(isMapClick, selectedProvider, selectedInnerData)
                        if (isRestartNeeded) {
                            mPreferenceManager.setValue(KEY_SELECTED_REGION, regionDisplayName[2])
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
                changeMapStyle(isMapClick, selectedProvider, selectedInnerData)
                if (isRestartNeeded) {
                    mPreferenceManager.setValue(KEY_SELECTED_REGION, regionDisplayName[2])
                    lifecycleScope.launch {
                        if (!isRunningTest) {
                            delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                            activity?.restartApplication()
                        }
                    }
                }
            }
        } else if (selectedProvider == getString(R.string.open_data) && mapName != getString(R.string.open_data)) {
            val shouldShowOpenDataDialog = !mPreferenceManager.getValue(KEY_OPEN_DATA_DONT_ASK, false)
            if (shouldShowOpenDataDialog) {
                activity?.enableOpenData(object : MapStyleRestartInterface {
                    override fun onOkClick(dialog: DialogInterface, dontAskAgain: Boolean) {
                        changeMapStyle(isMapClick, selectedProvider, selectedInnerData)
                        mPreferenceManager.setValue(KEY_OPEN_DATA_DONT_ASK, dontAskAgain)
                    }

                    override fun onLearnMoreClick(dialog: DialogInterface) {}
                })
            } else {
                changeMapStyle(isMapClick, selectedProvider, selectedInnerData)
            }
        } else {
            changeMapStyle(isMapClick, selectedProvider, selectedInnerData)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeMapStyle(
        isMapClick: Boolean,
        selectedProvider: String,
        selectedInnerData: String
    ) {
        if (isMapClick) {
            repeat(mViewModel.mStyleList.size) {
                mViewModel.mStyleList[it].isSelected = false
            }
            repeat(mViewModel.mStyleListForFilter.size) {
                mViewModel.mStyleListForFilter[it].isSelected = false
            }
            changeStyle(selectedProvider, selectedInnerData)
            for (data in mViewModel.mStyleListForFilter) {
                if (data.styleNameDisplay.equals(selectedProvider)) {
                    data.isSelected = !data.isSelected
                }
            }
            mMapStyleAdapter?.notifyDataSetChanged()
        } else {
            changeStyle(selectedProvider, selectedInnerData)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeStyle(
        selectedProvider: String,
        selectedInnerData: String
    ) {
        mBinding.apply {
            setMapTileSelection(selectedProvider)
        }
        mViewModel.mStyleList.forEach {
            it.mapInnerData?.forEach { innerData ->
                innerData.isSelected = false
            }
        }
        mViewModel.mStyleListForFilter.forEach {
            it.mapInnerData?.forEach { innerData ->
                innerData.isSelected = false
            }
        }
        for (data in mViewModel.mStyleListForFilter) {
            if (data.styleNameDisplay.equals(selectedProvider)) {
                data.mapInnerData.let {
                    if (it != null) {
                        for (innerData in it) {
                            if (innerData.mapName.equals(selectedInnerData)) {
                                innerData.isSelected = true
                                var selectedId = ""
                                if (data.styleNameDisplay != getString(R.string.grab)) {
                                    when (innerData.mapName) {
                                        getString(R.string.map_light) -> {
                                            selectedId = MapNames.ESRI_LIGHT
                                        }
                                        getString(R.string.map_streets) -> {
                                            selectedId = MapNames.ESRI_STREET_MAP
                                        }
                                        getString(R.string.map_navigation) -> {
                                            selectedId = MapNames.ESRI_NAVIGATION
                                        }
                                        getString(R.string.map_dark_gray) -> {
                                            selectedId = MapNames.ESRI_DARK_GRAY_CANVAS
                                        }
                                        getString(R.string.map_light_gray) -> {
                                            selectedId = MapNames.ESRI_LIGHT_GRAY_CANVAS
                                        }
                                        getString(R.string.map_imagery) -> {
                                            selectedId = MapNames.ESRI_IMAGERY
                                        }
                                        resources.getString(R.string.map_contrast) -> {
                                            selectedId = MapNames.HERE_CONTRAST
                                        }
                                        resources.getString(R.string.map_explore) -> {
                                            selectedId = MapNames.HERE_EXPLORE
                                        }
                                        resources.getString(R.string.map_explore_truck) -> {
                                            selectedId = MapNames.HERE_EXPLORE_TRUCK
                                        }
                                        resources.getString(R.string.map_hybrid) -> {
                                            selectedId = MapNames.HERE_HYBRID
                                        }
                                        resources.getString(R.string.map_raster) -> {
                                            selectedId = MapNames.HERE_IMAGERY
                                        }
                                        resources.getString(R.string.map_standard_light) -> {
                                            selectedId = MapNames.OPEN_DATA_STANDARD_LIGHT
                                        }
                                        resources.getString(R.string.map_standard_dark) -> {
                                            selectedId = MapNames.OPEN_DATA_STANDARD_DARK
                                        }
                                        resources.getString(R.string.map_visualization_light) -> {
                                            selectedId = MapNames.OPEN_DATA_VISUALIZATION_LIGHT
                                        }
                                        resources.getString(R.string.map_visualization_dark) -> {
                                            selectedId = MapNames.OPEN_DATA_VISUALIZATION_DARK
                                        }
                                    }
                                } else {
                                    when (innerData.mapName) {
                                        resources.getString(R.string.map_grab_light) -> {
                                            selectedId = MapNames.GRAB_LIGHT
                                        }
                                        resources.getString(R.string.map_grab_dark) -> {
                                            selectedId = MapNames.GRAB_DARK
                                        }
                                    }
                                }
                                innerData.mapName?.let { it1 ->
                                    mPreferenceManager.setValue(
                                        KEY_MAP_STYLE_NAME,
                                        it1
                                    )
                                }
                                data.styleNameDisplay?.let { mapName ->
                                    val properties = listOf(
                                        Pair(AnalyticsAttribute.PROVIDER, mapName),
                                        Pair(AnalyticsAttribute.ID, selectedId),
                                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
                                    )
                                    (activity as MainActivity).analyticsHelper?.recordEvent(
                                        EventType.MAP_STYLE_CHANGE,
                                        properties
                                    )
                                    mPreferenceManager.setValue(
                                        KEY_MAP_NAME,
                                        mapName
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        mMapStyleAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        setColumnCount()
        mLayoutManagerHere.spanCount = columnCount
        mLayoutManagerEsri.spanCount = columnCount
        mHereAdapter?.notifyDataSetChanged()
        mAdapter?.notifyDataSetChanged()
        if (isGrabMapEnable) {
            mLayoutManagerGrab.spanCount = columnCount
            mGrabAdapter?.notifyDataSetChanged()
        }
    }

    private fun setColumnCount() {
        columnCount = calculateColumnCount()
    }

    private fun calculateColumnCount(): Int {
        val imageWidth = resources.getDimensionPixelSize(R.dimen.dp_104)
        val imageMargin = resources.getDimensionPixelSize(R.dimen.dp_48)
        val width = resources.getDimensionPixelSize(R.dimen.screen_size)
        val calculatedColumn: Double =
            ((requireContext().resources.displayMetrics.widthPixels).toDouble() - width) / (imageWidth + imageMargin)
        return ceil(calculatedColumn).toInt()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentMapStyleBinding.setDefaultMapStyleList() {
        mViewModel.mStyleList.clear()
        mViewModel.mStyleList.addAll(mViewModel.mStyleListForFilter)
        activity?.runOnUiThread {
            etSearchMap.setText("")
            mMapStyleAdapter?.notifyDataSetChanged()
        }
    }

    private fun FragmentMapStyleBinding.mapStyleShowSorting() {
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

    private fun FragmentMapStyleBinding.mapStyleShowList() {
        hideViews(
            nsvFilter,
            viewDivider,
            tvClearSelection,
            btnApplyFilter
        )
        rvMapStyle.show()
    }

    private fun FragmentMapStyleBinding.setMapTileSelection(
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
    private fun notifySortingAdapter() {
        mTypeAdapter?.notifyDataSetChanged()
        mAttributeAdapter?.notifyDataSetChanged()
        mProviderAdapter?.notifyDataSetChanged()
    }

    private fun FragmentMapStyleBinding.openSearch(params: ViewGroup.LayoutParams?) {
        viewLine.hide()
        tilSearch.show()
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        cardSearchFilter.layoutParams = params
        etSearchMap.clearFocus()
        groupFilterButton.hide()
        scrollMapStyle.isFillViewport = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clickListener() {
        mBinding.ivMapStyleBack.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.apply {
            @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
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
            clMapStyleMain.setOnClickListener {
                if (etSearchMap.text.isNullOrEmpty() && tilSearch.isVisible && !nsvFilter.isVisible) {
                    collapseSearchMap(params)
                }
            }
            cardEsri.setOnClickListener {
                val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                if (mapName != getString(R.string.esri)) {
                    mapStyleChange(
                        false,
                        getString(R.string.esri),
                        getString(R.string.map_light)
                    )
                }
            }
            cardHere.setOnClickListener {
                val mapName =
                    mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                if (mapName != getString(R.string.here)) {
                    mapStyleChange(
                        false,
                        getString(R.string.here),
                        getString(R.string.map_explore)
                    )
                }
            }
            cardOpenData.setOnClickListener {
                val mapName =
                    mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                if (mapName != getString(R.string.open_data)) {
                    mapStyleChange(
                        false,
                        getString(R.string.open_data),
                        getString(R.string.map_standard_light)
                    )
                }
            }
            cardGrabMap.setOnClickListener {
                val mapName =
                    mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                if (mapName != getString(R.string.grab)) {
                    mapStyleChange(
                        false,
                        getString(R.string.grab),
                        getString(R.string.map_grab_light)
                    )
                }
            }
        }
    }

    private fun FragmentMapStyleBinding.showNoDataFoundFilter() {
        layoutNoDataFound.root.show()
        if (isFilterApplied) {
            tvClearFilter.show()
        } else {
            tvClearFilter.hide()
        }
        rvMapStyle.hide()
    }

    private fun FragmentMapStyleBinding.searchText(text: CharSequence?) {
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

    private fun FragmentMapStyleBinding.setFilterNotSelected() {
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

    private fun FragmentMapStyleBinding.collapseSearchMap(params: ViewGroup.LayoutParams?) {
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

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }

    private fun checkInternetConnection(): Boolean {
        return if (context?.isInternetAvailable() == true) {
            true
        } else {
            showError(getString(R.string.check_your_internet_connection_and_try_again))
            false
        }
    }
    fun hideKeyBoard() {
        mBinding.etSearchMap.clearFocus()
    }
}
