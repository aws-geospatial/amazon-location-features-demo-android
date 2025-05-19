package com.aws.amazonlocation.ui.main.mapStyle

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentMapStyleBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.explore.MapLanguageAdapter
import com.aws.amazonlocation.ui.main.explore.PoliticalAdapter
import com.aws.amazonlocation.utils.ATTRIBUTE_DARK
import com.aws.amazonlocation.utils.ATTRIBUTE_LIGHT
import com.aws.amazonlocation.utils.DELAY_300
import com.aws.amazonlocation.utils.KEY_COLOR_SCHEMES
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_POLITICAL_VIEW
import com.aws.amazonlocation.utils.KEY_SELECTED_MAP_LANGUAGE
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideKeyboard
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.isInternetAvailable
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.textChanges
import kotlin.math.ceil
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MapStyleFragment : BaseFragment() {
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var mBinding: FragmentMapStyleBinding
    private val mViewModel: MapStyleViewModel by viewModels()
    private var isTablet = false
    private var isLargeTablet = false
    private var columnCount = 2
    private var mMapStyleAdapter: SettingMapStyleAdapter? = null
    private var mPoliticalAdapter: PoliticalAdapter? = null
    private var mMapLanguageAdapter: MapLanguageAdapter? = null

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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        if ((activity is MainActivity)) {
            isTablet = (activity as MainActivity).isTablet
        }
        isLargeTablet = requireContext().resources.getBoolean(R.bool.is_large_tablet)
        if (isTablet) {
            setColumnCount()
        }
        setMapStyleAdapter()
        backPress()
        clickListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setMapStyleAdapter() {
        mBinding.apply {
            mViewModel.setMapListData(rvMapStyle.context)
            mViewModel.setPoliticalListData(rvPoliticalView.context)
            mViewModel.setMapLanguageData(rvMapLanguage.context)
            val mapStyleName =
                mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_standard))
                    ?: getString(R.string.map_standard)
            val colorScheme =
                mPreferenceManager.getValue(KEY_COLOR_SCHEMES, ATTRIBUTE_LIGHT) ?: ATTRIBUTE_LIGHT
            toggleMode.check(if (colorScheme == ATTRIBUTE_LIGHT) R.id.btn_light else R.id.btn_dark)
            mViewModel.mStyleList.forEach {
                it.isSelected = true
                it.mapInnerData?.forEach { mapStyleInnerData ->
                    if (mapStyleInnerData.mapName.equals(mapStyleName)) {
                        if (mapStyleInnerData.mapName == getString(R.string.map_satellite) ||
                            mapStyleInnerData.mapName ==
                            getString(
                                    R.string.map_hybrid
                                )
                        ) {
                            disableToggle()
                        } else {
                            enableToggle()
                        }
                        if (mapStyleInnerData.mapName == getString(R.string.map_satellite)) {
                            disablePoliticalView()
                        } else {
                            enablePoliticalView()
                        }
                        mapStyleInnerData.isSelected = true
                    }
                }
            }
            layoutNoDataFoundPolitical.tvNoMatchingFound.text =
                getString(R.string.label_style_search_error_title)
            layoutNoDataFoundPolitical.tvMakeSureSpelledCorrect.text =
                getString(R.string.label_style_search_error_des)
            rvMapStyle.layoutManager = LinearLayoutManager(requireContext())
            mMapStyleAdapter =
                SettingMapStyleAdapter(
                    columnCount,
                    mViewModel.mStyleList,
                    object : SettingMapStyleAdapter.MapInterface {
                        override fun mapStyleClick(
                            position: Int,
                            innerPosition: Int
                        ) {
                            if (checkInternetConnection()) {
                                if (position != -1 && innerPosition != -1) {
                                    val selectedInnerData =
                                        mViewModel.mStyleList[position]
                                            .mapInnerData
                                            ?.get(
                                                innerPosition
                                            )?.mapName
                                    for (data in mViewModel.mStyleList) {
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
                                    selectedInnerData?.let { it1 ->
                                        changeStyle(
                                            it1
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            rvMapStyle.adapter = mMapStyleAdapter

            val languageCode = getLanguageCode(requireContext())
            val isRtl =
                languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW || languageCode == LANGUAGE_CODE_HEBREW_1
            val selectedCountry = mPreferenceManager.getValue(KEY_POLITICAL_VIEW, "") ?: ""
            selectedCountry.takeIf { it.isNotEmpty() }?.let { country ->
                mViewModel.mPoliticalSearchData.find { it.countryName == country }?.let {
                    it.isSelected = true
                    tvPoliticalDescription.apply {
                        text =
                            buildString {
                                append(it.countryName)
                                append(". ")
                                append(it.description)
                            }
                        setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_primary_green
                            )
                        )
                    }
                }
            }
            if (selectedCountry.isEmpty()) {
                mViewModel.mPoliticalData[0].isSelected = true
            }
            rvPoliticalView.layoutManager = LinearLayoutManager(requireContext())
            mPoliticalAdapter =
                PoliticalAdapter(
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

                            val selectedItem = mViewModel.mPoliticalData.find { it.isSelected }
                            if (selectedItem != null && selectedItem.countryName != getString(
                                    R.string.label_no_political_view
                                )
                            ) {
                                mPreferenceManager.setValue(
                                    KEY_POLITICAL_VIEW,
                                    selectedItem.countryName
                                )
                                tvPoliticalDescription.apply {
                                    text =
                                        buildString {
                                            append(selectedItem.countryName)
                                            append(". ")
                                            append(selectedItem.description)
                                        }
                                    setTextColor(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.color_primary_green
                                        )
                                    )
                                }
                            } else {
                                mPreferenceManager.setValue(KEY_POLITICAL_VIEW, "")
                                tvPoliticalDescription.apply {
                                    text =
                                        getString(
                                            R.string.label_map_representation_for_different_countries
                                        )
                                    setTextColor(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.color_hint_text
                                        )
                                    )
                                }
                            }
                            appCompatTextView2.text = getString(R.string.label_map_style)
                            showViews(rvMapStyle, cardColorScheme, clPoliticalView, clMapLanguage)
                            hideViews(clSearchPolitical, cardMapLanguage)
                        }
                    }
                )
            rvPoliticalView.adapter = mPoliticalAdapter

            val selectedMapLanguage = mPreferenceManager.getValue(KEY_SELECTED_MAP_LANGUAGE, "") ?: ""
            selectedMapLanguage.takeIf { it.isNotEmpty() }?.let { language ->
                mViewModel.mMapLanguageData.find { it.value == language }?.let {
                    it.isSelected = true
                    tvMapLanguageDescription.apply {
                        text = buildString {
                            append(it.label)
                            append(context.getString(R.string.label_is_selected))
                        }
                        setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.color_primary_green)
                        )
                    }
                }
            }
            if (selectedMapLanguage.isEmpty()) {
                mViewModel.mMapLanguageData[0].isSelected = true
            }
            rvMapLanguage.layoutManager = LinearLayoutManager(requireContext())
            mMapLanguageAdapter = MapLanguageAdapter(
                mViewModel.mMapLanguageData,
                isRtl,
                object : MapLanguageAdapter.MapLanguageInterface {
                    override fun languageClick(position: Int) {
                        if (mViewModel.mMapLanguageData[position].isSelected) return
                        mViewModel.mMapLanguageData.forEach {
                            it.isSelected = false
                        }
                        mViewModel.mMapLanguageData[position].isSelected = true
                        mMapLanguageAdapter?.notifyDataSetChanged()
                        val selectedItem = mViewModel.mMapLanguageData.find { it.isSelected }
                        if (selectedItem != null && selectedItem.label != getString(
                                R.string.label_no_map_language
                            )
                        ) {
                            mPreferenceManager.setValue(
                                KEY_SELECTED_MAP_LANGUAGE,
                                selectedItem.value
                            )
                            tvMapLanguageDescription.apply {
                                text = buildString {
                                    append(selectedItem.label)
                                    append(context.getString(R.string.label_is_selected))
                                }
                                setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.color_primary_green
                                    )
                                )
                            }
                        } else {
                            mPreferenceManager.setValue(KEY_SELECTED_MAP_LANGUAGE, "")
                            tvMapLanguageDescription.apply {
                                text = getString(R.string.label_change_map_language)
                                setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.color_hint_text
                                    )
                                )
                            }
                        }
                        appCompatTextView2.text = getString(R.string.label_map_style)
                        showViews(rvMapStyle, cardColorScheme, clPoliticalView, clMapLanguage)
                        hideViews(clSearchPolitical, cardMapLanguage)
                    }
                }
            )
            rvMapLanguage.adapter = mMapLanguageAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeStyle(mapStyleName: String) {
        mViewModel.mStyleList.forEach {
            it.mapInnerData?.forEach { innerData ->
                innerData.isSelected = false
            }
        }
        for (data in mViewModel.mStyleList) {
            data.mapInnerData.let {
                if (it != null) {
                    for (innerData in it) {
                        if (innerData.mapName.equals(mapStyleName)) {
                            innerData.isSelected = true
                            innerData.mapName?.let { it1 ->
                                mPreferenceManager.setValue(
                                    KEY_MAP_STYLE_NAME,
                                    it1
                                )
                            }
                        }
                    }
                }
            }
        }
        if (mapStyleName == getString(R.string.map_satellite) || mapStyleName == getString(
                R.string.map_hybrid
            )
        ) {
            disableToggle()
        } else {
            enableToggle()
        }
        if (mapStyleName == getString(R.string.map_satellite)) {
            disablePoliticalView()
            mPreferenceManager.setValue(KEY_POLITICAL_VIEW, "")
            mBinding.tvPoliticalDescription.apply {
                text =
                    getString(R.string.label_map_representation_for_different_countries)
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_hint_text
                    )
                )
            }
            mViewModel.mPoliticalSearchData.forEach {
                it.isSelected = false
            }
            mViewModel.mPoliticalData.forEach {
                it.isSelected = false
            }
            hideKeyboard(requireActivity(), mBinding.etSearchCountry)
            mViewModel.mPoliticalData[0].isSelected = true
            mPoliticalAdapter?.notifyDataSetChanged()
        } else {
            enablePoliticalView()
        }
        mMapStyleAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        setColumnCount()
        layoutManager.spanCount = columnCount
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

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    @SuppressLint("NotifyDataSetChanged")
    private fun clickListener() {
        mBinding.apply {
            ivMapStyleBack.setOnClickListener {
                if (clSearchPolitical.visibility == View.VISIBLE || cardMapLanguage.visibility == View.VISIBLE) {
                    etSearchCountry.setText("")
                    appCompatTextView2.text = getString(R.string.label_map_style)
                    showViews(rvMapStyle, cardColorScheme, clPoliticalView, clMapLanguage)
                    hideViews(clSearchPolitical, cardMapLanguage)
                    val selectedCountry = mPreferenceManager.getValue(KEY_POLITICAL_VIEW, "") ?: ""
                    clearSelectionAndSetOriginalData(selectedCountry)
                } else {
                    findNavController().popBackStack()
                }
            }
            toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
                when (checkedId) {
                    R.id.btn_light -> {
                        if (isChecked) {
                            mPreferenceManager.setValue(KEY_COLOR_SCHEMES, ATTRIBUTE_LIGHT)
                        }
                    }

                    R.id.btn_dark -> {
                        if (isChecked) {
                            mPreferenceManager.setValue(KEY_COLOR_SCHEMES, ATTRIBUTE_DARK)
                        }
                    }
                }
            }
            clPoliticalView.setOnClickListener {
                val mapStyleName =
                    mPreferenceManager.getValue(
                        KEY_MAP_STYLE_NAME,
                        getString(R.string.map_standard)
                    )
                        ?: getString(R.string.map_standard)
                if (mapStyleName == getString(R.string.map_satellite)) return@setOnClickListener
                appCompatTextView2.text = getString(R.string.label_political_view)
                hideViews(rvMapStyle, cardColorScheme, clPoliticalView, clMapLanguage)
                showViews(clSearchPolitical)
            }
            clMapLanguage.setOnClickListener {
                appCompatTextView2.text = getString(R.string.label_map_language)
                hideViews(rvMapStyle, cardColorScheme, clPoliticalView, clMapLanguage)
                showViews(cardMapLanguage)
            }

            etSearchCountry
                .textChanges()
                .debounce(DELAY_300)
                .onEach { text ->
                    tilSearch.isEndIconVisible = !text.isNullOrEmpty()
                    val result = mViewModel.searchPoliticalData(text.toString())
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
            tilSearch.isEndIconVisible = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }

    private fun checkInternetConnection(): Boolean =
        if (context?.isInternetAvailable() == true) {
            true
        } else {
            showError(getString(R.string.check_your_internet_connection_and_try_again))
            false
        }

    fun hideKeyBoard() {
        mBinding.etSearchCountry.clearFocus()
    }

    private fun enableToggle() {
        mBinding.toggleMode.isEnabled = true
        mBinding.toggleMode.alpha = 1.0f
    }

    private fun disableToggle() {
        mBinding.toggleMode.isEnabled = false
        mBinding.toggleMode.alpha = 0.5f
    }

    private fun enablePoliticalView() {
        mBinding.clPoliticalView.isClickable = true
        mBinding.clPoliticalView.alpha = 1.0f
    }

    private fun disablePoliticalView() {
        mBinding.clPoliticalView.isClickable = false
        mBinding.clPoliticalView.alpha = 0.5f
    }
}
