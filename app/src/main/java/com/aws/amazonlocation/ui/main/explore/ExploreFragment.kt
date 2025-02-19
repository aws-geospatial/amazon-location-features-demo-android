package com.aws.amazonlocation.ui.main.explore

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.res.Configuration
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.LinearLayoutManager
import aws.sdk.kotlin.services.geoplaces.model.Address
import aws.sdk.kotlin.services.georoutes.model.CalculateRoutesResponse
import aws.sdk.kotlin.services.georoutes.model.Route
import aws.sdk.kotlin.services.georoutes.model.RouteLeg
import aws.sdk.kotlin.services.georoutes.model.RouteTravelMode
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.onError
import com.aws.amazonlocation.data.common.onLoading
import com.aws.amazonlocation.data.common.onSuccess
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.data.enum.GeofenceBottomSheetEnum
import com.aws.amazonlocation.data.enum.MarkerEnum
import com.aws.amazonlocation.data.enum.RedirectionType
import com.aws.amazonlocation.data.enum.SearchApiEnum
import com.aws.amazonlocation.data.response.CalculateDistanceResponse
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.NavigationResponse
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.databinding.BottomSheetDirectionBinding
import com.aws.amazonlocation.databinding.BottomSheetDirectionSearchBinding
import com.aws.amazonlocation.databinding.FragmentExploreBinding
import com.aws.amazonlocation.domain.`interface`.GeofenceInterface
import com.aws.amazonlocation.domain.`interface`.MarkerClickInterface
import com.aws.amazonlocation.domain.`interface`.SimulationInterface
import com.aws.amazonlocation.domain.`interface`.TrackingInterface
import com.aws.amazonlocation.domain.`interface`.UpdateRouteInterface
import com.aws.amazonlocation.domain.`interface`.UpdateTrackingInterface
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.geofence.GeofenceViewModel
import com.aws.amazonlocation.ui.main.map_style.MapStyleBottomSheetFragment
import com.aws.amazonlocation.ui.main.map_style.MapStyleChangeListener
import com.aws.amazonlocation.ui.main.simulation.SimulationViewModel
import com.aws.amazonlocation.ui.main.tracking.TrackingViewModel
import com.aws.amazonlocation.ui.main.web_view.WebViewActivity
import com.aws.amazonlocation.utils.ATTRIBUTE_DARK
import com.aws.amazonlocation.utils.ATTRIBUTE_LIGHT
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.CLICK_DEBOUNCE
import com.aws.amazonlocation.utils.CLICK_DEBOUNCE_ENABLE
import com.aws.amazonlocation.utils.CLICK_TIME_DIFFERENCE
import com.aws.amazonlocation.utils.DELAY_300
import com.aws.amazonlocation.utils.DELAY_500
import com.aws.amazonlocation.utils.DateFormat.HH_MM_AA
import com.aws.amazonlocation.utils.DateFormat.HH_MM
import com.aws.amazonlocation.utils.Debouncer
import com.aws.amazonlocation.utils.Distance.DISTANCE_FOR_DRIVE_TRUCK
import com.aws.amazonlocation.utils.Distance.DISTANCE_FOR_SCOOTER
import com.aws.amazonlocation.utils.Distance.DISTANCE_FOR_WALK
import com.aws.amazonlocation.utils.Durations
import com.aws.amazonlocation.utils.Durations.DELAY_FOR_BOTTOM_SHEET_LOAD
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.EventType.PLACE_SEARCH
import com.aws.amazonlocation.utils.EventType.ROUTE_OPTION_CHANGED
import com.aws.amazonlocation.utils.EventType.ROUTE_SEARCH
import com.aws.amazonlocation.utils.IS_LOCATION_TRACKING_ENABLE
import com.aws.amazonlocation.utils.KEY_AVOID_DIRT_ROADS
import com.aws.amazonlocation.utils.KEY_AVOID_FERRIES
import com.aws.amazonlocation.utils.KEY_AVOID_TOLLS
import com.aws.amazonlocation.utils.KEY_AVOID_TUNNELS
import com.aws.amazonlocation.utils.KEY_AVOID_U_TURNS
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_COLOR_SCHEMES
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_POLITICAL_VIEW
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.KEY_URL
import com.aws.amazonlocation.utils.KILOMETERS
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.MAP_STYLE_ATTRIBUTION
import com.aws.amazonlocation.utils.MILES
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.STRING_FORMAT
import com.aws.amazonlocation.utils.STRING_FORMAT_TIME
import com.aws.amazonlocation.utils.SignOutInterface
import com.aws.amazonlocation.utils.SimulationDialogInterface
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
import com.aws.amazonlocation.utils.TrackerCons
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.Units.getDeviceId
import com.aws.amazonlocation.utils.Units.getMetricsNew
import com.aws.amazonlocation.utils.Units.getTime
import com.aws.amazonlocation.utils.Units.isGPSEnabled
import com.aws.amazonlocation.utils.Units.isMetric
import com.aws.amazonlocation.utils.attributionPattern
import com.aws.amazonlocation.utils.checkLocationPermission
import com.aws.amazonlocation.utils.formatToDisplayTime
import com.aws.amazonlocation.utils.copyTextToClipboard
import com.aws.amazonlocation.utils.formatToDisplayDate
import com.aws.amazonlocation.utils.formatToISO8601
import com.aws.amazonlocation.utils.getKeyboardHeight
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.getRegion
import com.aws.amazonlocation.utils.getUserName
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideKeyboard
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.invisible
import com.aws.amazonlocation.utils.isInternetAvailable
import com.aws.amazonlocation.utils.isRunningTest2LiveLocation
import com.aws.amazonlocation.utils.isRunningTest3LiveLocation
import com.aws.amazonlocation.utils.isRunningTestLiveLocation
import com.aws.amazonlocation.utils.locationPermissionDialog
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showKeyboard
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.simulationExit
import com.aws.amazonlocation.utils.textChanges
import com.aws.amazonlocation.utils.validateLatLng
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mapbox.android.gestures.StandardScaleGestureDetector
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.geojson.Point
import org.maplibre.geojson.Point.fromLngLat

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class ExploreFragment :
    BaseFragment(),
    OnMapReadyCallback,
    SignOutInterface,
    MapLibreMap.OnMapClickListener,
    MapHelper.IsMapLoadedInterface,
    MapLibreMap.OnScaleListener,
    MapStyleChangeListener {
    private var mapStyleBottomSheetFragment: MapStyleBottomSheetFragment? = null
    private lateinit var mBinding: FragmentExploreBinding
    private var mMapLibreMap: MapLibreMap? = null
    private var mAdapter: SearchPlacesAdapter? = null
    private var mAdapterDirection: SearchPlacesAdapter? = null
    private var mPlaceList = ArrayList<SearchSuggestionData>()
    private var mNavigationList = ArrayList<NavigationData>()
    private var mSearchPlacesDirectionSuggestionAdapter: SearchPlacesSuggestionAdapter? = null
    private var mSearchPlacesSuggestionAdapter: SearchPlacesSuggestionAdapter? = null
    private var mNavigationAdapter: NavigationAdapter? = null
    val mViewModel: ExploreViewModel by viewModels()
    private val mGeofenceViewModel: GeofenceViewModel by viewModels()
    private val mTrackingViewModel: TrackingViewModel by viewModels()
    private val mSimulationViewModel: SimulationViewModel by viewModels()
    private var mRedirectionType: String? = null
    private var timeDepart: String? = null
    private var calendar: Calendar? = null
    private val debouncer = Debouncer(lifecycleScope)

    private var gpsActivityResult =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                checkAndEnableLocation()
            }
        }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val width = resources.getDimensionPixelSize(R.dimen.screen_size)
        mBinding.bottomSheetSearch.clSearchSheet.layoutParams.width = width
        mBinding.bottomSheetSearch.clSearchSheet.requestLayout()
        mBinding.bottomSheetDirection.clPersistentBottomSheetDirection.layoutParams.width = width
        mBinding.bottomSheetDirection.clPersistentBottomSheetDirection.requestLayout()
        mBinding.bottomSheetDirectionSearch.clDirectionSearchSheet.layoutParams.width = width
        mBinding.bottomSheetDirectionSearch.clDirectionSearchSheet.requestLayout()
        mBinding.bottomSheetNavigation.clNavigationParent.layoutParams.width = width
        mBinding.bottomSheetNavigation.clNavigationParent.requestLayout()
        mBinding.bottomSheetNavigationComplete.clPersistentBottomSheetNavigationComplete.layoutParams.width =
            width
        mBinding.bottomSheetNavigationComplete.clPersistentBottomSheetNavigationComplete.requestLayout()
        mBinding.bottomSheetTracking.clPersistentBottomSheet.layoutParams.width = width
        mBinding.bottomSheetTracking.clPersistentBottomSheet.requestLayout()
        mBinding.bottomSheetGeofenceList.clGeofenceListMain.layoutParams.width = width
        mBinding.bottomSheetGeofenceList.clGeofenceListMain.requestLayout()
        mBinding.bottomSheetAddGeofence.clPersistentBottomSheetAddGeofence.layoutParams.width =
            width
        mBinding.bottomSheetAddGeofence.clPersistentBottomSheetAddGeofence.requestLayout()
        mBinding.bottomSheetAttribution.clMain.layoutParams.width = width
        mBinding.bottomSheetAttribution.clMain.requestLayout()
        val widthTimeDialog = resources.getDimensionPixelSize(R.dimen.navigation_top_dialog_size)
        mBinding.cardNavigationTimeDialog.layoutParams.width = widthTimeDialog
        mBinding.cardNavigationTimeDialog.requestLayout()
        mBinding.cardSimulationPopup.layoutParams.width = widthTimeDialog
        mBinding.cardSimulationPopup.requestLayout()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // initialize MapLibre
        MapLibre.getInstance(requireContext())
        mBinding = FragmentExploreBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    private fun checkRtl() {
        if ((activity as MainActivity).isTablet) {
            val languageCode = getLanguageCode()
            val isRtl =
                languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW || languageCode == LANGUAGE_CODE_HEBREW_1
            if (isRtl) {
                mBinding.apply {
                    clMainExplorer.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    bottomSheetSearch.clSearchSheet.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    bottomSheetDirection.clPersistentBottomSheetDirection.layoutDirection =
                        View.LAYOUT_DIRECTION_RTL
                    bottomSheetDirectionSearch.clDirectionSearchSheet.layoutDirection =
                        View.LAYOUT_DIRECTION_RTL
                    bottomSheetNavigation.clNavigationParent.layoutDirection =
                        View.LAYOUT_DIRECTION_RTL
                    bottomSheetNavigationComplete.clPersistentBottomSheetNavigationComplete.layoutDirection =
                        View.LAYOUT_DIRECTION_RTL
                    bottomSheetAttribution.clMain.layoutDirection = View.LAYOUT_DIRECTION_RTL
                }
            }
        }
    }

    fun showKeyBoard() {
        mBaseActivity?.mGeofenceUtils?.let {
            if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                mBottomSheetHelper.expandDirectionSearchSheet(this@ExploreFragment)
                getKeyboardHeight(requireActivity()) { keyboardHeight ->
                    mBinding.bottomSheetDirectionSearch.viewKeyboardScroll.updateLayoutParams {
                        height = keyboardHeight
                    }
                    mBinding.bottomSheetDirectionSearch.viewKeyboardScroll.show()
                }
            } else if (it.geofenceBottomSheetVisibility()) {
                mBaseActivity?.mGeofenceUtils?.expandAddGeofenceBottomSheet()
            } else if (mapStyleBottomSheetFragment?.isMapStyleExpandedOrHalfExpand() == true) {
                mapStyleBottomSheetFragment?.expandMapStyleSheet()
            } else {
                if (mBottomSheetHelper.isSearchSheetOpen && !mBottomSheetHelper.isSearchBottomSheetExpandedOrHalfExpand()) {
                    mBottomSheetHelper.expandSearchBottomSheet()
                } else if (mBinding.bottomSheetSearch.edtSearchPlaces.hasFocus() && mBottomSheetHelper.isSearchBottomSheetHalfExpand()) {
                    mBottomSheetHelper.expandSearchBottomSheet()
                } else {
                }
            }
        }
    }

    fun hideKeyBoard() {
        mBaseActivity?.mGeofenceUtils?.let {
            if (it.isAddGeofenceBottomSheetVisible()) {
                mBaseActivity?.mGeofenceUtils?.collapseAddGeofenceBottomSheet()
            } else if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                mBinding.apply {
                    bottomSheetDirectionSearch.apply {
                        if (!cardListRoutesOption.isVisible) {
                            viewKeyboardScroll.hide()
                        }
                        edtSearchDest.clearFocus()
                        edtSearchDirection.clearFocus()
                    }
                }
            } else if (mBottomSheetHelper.isSearchPlaceSheetVisible()) {
                mBinding.bottomSheetSearch.edtSearchPlaces.clearFocus()
                if (mPlaceList.isNotEmpty()) {
                    mBottomSheetHelper.halfExpandBottomSheet()
                } else {
                    mBottomSheetHelper.collapseSearchBottomSheet()
                }
            } else {
                if (mBottomSheetHelper.isSearchSheetOpen) {
                    mBottomSheetHelper.isSearchSheetOpen = false
                    mBottomSheetHelper.halfExpandBottomSheet()
                } else if (mBottomSheetHelper.isSearchBottomSheetExpandedOrHalfExpand()) {
                    mBottomSheetHelper.hideSearchBottomSheet(false)
                } else {
                }
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            checkRtl()
            mBottomSheetHelper.setSearchBottomSheet(
                activity,
                mBinding.bottomSheetSearch,
                mBaseActivity,
                this@ExploreFragment,
            )
            getUpdatedAvoidOptionValue()
            mBinding.bottomSheetDirectionSearch.switchAvoidTools.isChecked = mViewModel.mIsAvoidTolls
            mBinding.bottomSheetDirectionSearch.switchAvoidFerries.isChecked = mViewModel.mIsAvoidFerries
            mBinding.bottomSheetDirectionSearch.switchAvoidDirtRoads.isChecked = mViewModel.mIsAvoidDirtRoads
            mBinding.bottomSheetDirectionSearch.switchAvoidUTurns.isChecked = mViewModel.mIsAvoidUTurns
            mBinding.bottomSheetDirectionSearch.switchAvoidTunnels.isChecked = mViewModel.mIsAvoidTunnels
            mBinding.bottomSheetDirectionSearch.apply { checkedSwitchCount() }
            mBottomSheetHelper.setNavigationBottomSheet(mBinding.bottomSheetNavigation)
            mBottomSheetHelper.setNavigationCompleteBottomSheet(
                mBinding.bottomSheetNavigationComplete.clPersistentBottomSheetNavigationComplete,
            )
            mBottomSheetHelper.setDirectionBottomSheet(mBinding.bottomSheetDirection.clPersistentBottomSheetDirection)
            mBottomSheetHelper.setAttributeBottomSheet(mBinding.bottomSheetAttribution)
            mBottomSheetHelper.setDirectionSearchBottomSheet(
                mBinding.bottomSheetDirectionSearch,
                this@ExploreFragment,
                mBaseActivity,
            )
            mBaseActivity?.mGeofenceUtils?.initGeofenceView(
                activity,
                mBinding.bottomSheetGeofenceList,
                mBinding.bottomSheetAddGeofence,
                mGeofenceInterface,
            )

            mBaseActivity?.mTrackingUtils?.initTrackingView(
                activity,
                mBinding.bottomSheetTracking,
                mTrackingInterface,
            )
            if (Units.checkInternetConnection(requireContext())) {
                if (!mLocationProvider.checkClientInitialize()) {
                    val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
                    if (mAuthStatus == AuthEnum.SIGNED_IN.name) {
                        if (mLocationProvider.isAuthTokenExpired()) {
                            (activity as MainActivity).refreshToken()
                        } else {
                            async { (activity as MainActivity).initMobileClient() }.await()
                            (activity as MainActivity).getTokenAndAttachPolicy()
                        }
                    } else {
                        async { (activity as MainActivity).initMobileClient() }.await()
                    }
                }
            }
            setMap(savedInstanceState)
            initSimulationView()
            setUserProfile()
            if ((activity as MainActivity).isAppNotFirstOpened()) {
                checkPermission()
            }
            setNavigationAdapter()
            setMapStyleBottomSheet()
            setSearchPlaceDirectionAdapter()
            setSearchPlaceDirectionSuggestionAdapter()
            setSearchPlaceAdapter()
            setSearchPlaceSuggestionAdapter()
            initObserver()
            initGeofenceObserver()
            clickListener()

            val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerDefaultNetworkCallback(
                object :
                    ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        if (mMapHelper.mSymbolManager == null) {
                            activity?.runOnUiThread {
                                mBinding.groupMapLoad.show()
                                mBinding.mapView.getMapAsync(this@ExploreFragment)
                            }
                        }
                    }

                    override fun onLost(network: Network) {
                    }
                },
            )
            (activity as MainActivity).showBottomBar()
        }
    }

    fun initSimulationView() {
        mBaseActivity?.mSimulationUtils?.initSimulationView(
            activity,
            mBinding.bottomSheetTrackSimulation,
            mSimulationInterface,
        )
    }

    private fun initGeofenceObserver() {
        lifecycleScope.launch {
            withStarted { }
            mBinding.apply {
                mGeofenceViewModel.mGetGeofenceList.collect { handleResult ->
                    bottomSheetGeofenceList.apply {
                        handleResult
                            .onLoading {
                                rvGeofence.hide()
                                clSearchLoaderGeofenceList.root.show()
                            }.onSuccess {
                                val propertiesAws =
                                    listOf(
                                        Pair(
                                            AnalyticsAttribute.TRIGGERED_BY,
                                            AnalyticsAttributeValue.GEOFENCES,
                                        ),
                                    )
                                (activity as MainActivity).analyticsUtils?.recordEvent(
                                    EventType.GET_GEOFENCES_LIST_SUCCESSFUL,
                                    propertiesAws,
                                )
                                clSearchLoaderGeofenceList.root.hide()
                                rvGeofence.show()
                                lifecycleScope.launch(Dispatchers.Main) {
                                    mBaseActivity?.mGeofenceUtils?.manageGeofenceListUI(it)
                                }
                            }.onError {
                                val propertiesAws =
                                    listOf(
                                        Pair(
                                            AnalyticsAttribute.TRIGGERED_BY,
                                            AnalyticsAttributeValue.GEOFENCES,
                                        ),
                                    )
                                (activity as MainActivity).analyticsUtils?.recordEvent(
                                    EventType.GET_GEOFENCES_LIST_FAILED,
                                    propertiesAws,
                                )
                                clSearchLoaderGeofenceList.root.hide()
                                rvGeofence.hide()
                            }
                    }
                }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mTrackingViewModel.mGetGeofenceList.collect { handleResult ->
                handleResult
                    .onLoading {
                        mBinding.bottomSheetGeofenceList.clSearchLoaderGeofenceList.root
                            .show()
                    }.onSuccess {
                        lifecycleScope.launch(Dispatchers.Main) {
                            mBaseActivity?.mTrackingUtils?.manageGeofenceListUI(it)
                        }
                    }.onError {
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mSimulationViewModel.mGetGeofenceList.collect { handleResult ->
                handleResult
                    .onLoading {
                    }.onSuccess {
                        lifecycleScope.launch(Dispatchers.Main) {
                            mBaseActivity?.mSimulationUtils?.manageGeofenceListUI(it)
                        }
                    }.onError {
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mBinding.apply {
                mTrackingViewModel.mGetLocationHistoryList.collect { handleResult ->
                    handleResult
                        .onLoading {
                            bottomSheetTracking.clSearchLoaderSheetTracking.root.show()
                        }.onSuccess {
                            lifecycleScope.launch(Dispatchers.Main) {
                                mBaseActivity?.mTrackingUtils?.locationHistoryListUI(it)
                            }
                        }.onError {
                            bottomSheetTracking.clSearchLoaderSheetTracking.root.hide()
                        }
                }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mTrackingViewModel.mGetLocationHistoryTodayList.collect { handleResult ->
                handleResult
                    .onLoading {}
                    .onSuccess {
                        lifecycleScope.launch(Dispatchers.Main) {
                            mBaseActivity?.mTrackingUtils?.locationHistoryTodayListUI(it)
                        }
                    }.onError {
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mTrackingViewModel.mGetUpdateDevicePosition.collect { handleResult ->
                handleResult
                    .onLoading {}
                    .onSuccess {
                        lifecycleScope.launch(Dispatchers.Main) {
                            mBaseActivity?.mTrackingUtils?.getTodayData()
                        }
                    }.onError {
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mBinding.apply {
                mTrackingViewModel.mDeleteLocationHistoryList.collect { handleResult ->
                    handleResult
                        .onLoading {
                        }.onSuccess {
                            lifecycleScope.launch(Dispatchers.Main) {
                                mBaseActivity?.mTrackingUtils?.deleteTrackingData()
                            }
                        }.onError {
                        }
                }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mGeofenceViewModel.mAddGeofence.collect { handleResult ->
                handleResult
                    .onLoading {
                    }.onSuccess {
                        val propertiesAws =
                            listOf(
                                Pair(
                                    AnalyticsAttribute.TRIGGERED_BY,
                                    AnalyticsAttributeValue.GEOFENCES,
                                ),
                            )
                        (activity as MainActivity).analyticsUtils?.recordEvent(
                            EventType.GEOFENCE_CREATION_SUCCESSFUL,
                            propertiesAws,
                        )
                        lifecycleScope.launch(Dispatchers.Main) {
                            mBaseActivity?.mGeofenceUtils?.mangeAddGeofenceUI(requireActivity())
                            mBaseActivity?.bottomNavigationVisibility(true)
                            showViews(mBinding.cardGeofenceMap, mBinding.cardMap)
                            activity?.hideKeyboard()
                        }
                    }.onError {
                        val propertiesAws =
                            listOf(
                                Pair(
                                    AnalyticsAttribute.TRIGGERED_BY,
                                    AnalyticsAttributeValue.GEOFENCES,
                                ),
                            )
                        (activity as MainActivity).analyticsUtils?.recordEvent(
                            EventType.GEOFENCE_CREATION_FAILED,
                            propertiesAws,
                        )
                        if (it.messageResource
                                .toString()
                                .contains(resources.getString(R.string.unable_to_execute_request))
                        ) {
                            showError(resources.getString(R.string.check_your_internet_connection_and_try_again))
                        }
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mGeofenceViewModel.mDeleteGeofence.collect { handleResult ->
                handleResult
                    .onLoading {
                    }.onSuccess {
                        val propertiesAws =
                            listOf(
                                Pair(
                                    AnalyticsAttribute.TRIGGERED_BY,
                                    AnalyticsAttributeValue.GEOFENCES,
                                ),
                            )
                        (activity as MainActivity).analyticsUtils?.recordEvent(
                            EventType.GEOFENCE_DELETION_SUCCESSFUL,
                            propertiesAws,
                        )
                        lifecycleScope.launch(Dispatchers.Main) {
                            mGeofenceInterface.hideShowBottomNavigationBar(
                                false,
                                GeofenceBottomSheetEnum.NONE,
                            )
                            it.position?.let { position ->
                                activity?.runOnUiThread {
                                    mBaseActivity?.mGeofenceUtils?.notifyGeofenceList(
                                        position,
                                        requireActivity(),
                                    )
                                }
                            }
                        }
                    }.onError {
                        val propertiesAws =
                            listOf(
                                Pair(
                                    AnalyticsAttribute.TRIGGERED_BY,
                                    AnalyticsAttributeValue.GEOFENCES,
                                ),
                            )
                        (activity as MainActivity).analyticsUtils?.recordEvent(
                            EventType.GEOFENCE_DELETION_FAILED,
                            propertiesAws,
                        )
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mGeofenceViewModel.mGeofenceSearchForSuggestionsResultList.collect { handleResult ->
                handleResult
                    .onLoading {
                    }.onSuccess {
                        val mText =
                            mBinding.bottomSheetAddGeofence.edtAddGeofenceSearch.text
                                .toString()
                                .replace(", ", ",")
                        if (!it.text.isNullOrEmpty() && it.text == mText) {
                            mBaseActivity?.mGeofenceUtils?.updateGeofenceSearchSuggestionList(it.data)
                        }
                        activity?.hideKeyboard()
                    }.onError {
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mGeofenceViewModel.mGeofenceSearchLocationList.collect { handleResult ->
                handleResult
                    .onLoading {
                    }.onSuccess {
                        val mText =
                            mBinding.bottomSheetAddGeofence.edtAddGeofenceSearch.text
                                .toString()
                        if (!it.text.isNullOrEmpty() && it.text == mText) {
                            mBaseActivity?.mGeofenceUtils?.updateGeofenceSearchPlaceList(it.data)
                        }
                        activity?.hideKeyboard()
                    }.onError {
                    }
            }
        }
    }

    private val mGeofenceInterface =
        object : GeofenceInterface {
            override fun addGeofence(
                geofenceId: String,
                collectionName: String,
                radius: Double?,
                latLng: LatLng?,
            ) {
                activity?.hideKeyboard()
                mGeofenceViewModel.addGeofence(geofenceId, collectionName, radius, latLng)
            }

            override fun getGeofenceList(collectionName: String) {
                mGeofenceViewModel.getGeofenceList(collectionName)
            }

            override fun deleteGeofence(
                position: Int,
                data: ListGeofenceResponseEntry,
            ) {
                mGeofenceViewModel.deleteGeofence(position, data)
            }

            override fun geofenceSearchPlaceIndexForText(searchText: String) {
                mGeofenceViewModel.geofenceSearchPlaceIndexForText(searchText, mViewModel.mLatLng)
            }

            override fun hideShowBottomNavigationBar(
                isHide: Boolean,
                type: GeofenceBottomSheetEnum,
            ) {
                lifecycleScope.launch {
                    mBinding.apply {
                        if (!isHide) {
                            showViews(cardGeofenceMap, cardMap)
                        } else {
                            hideViews(cardGeofenceMap, cardMap)
                        }
                    }
                    mBaseActivity?.bottomNavigationVisibility(!isHide)
                    delay(100)
                    when (type) {
                        GeofenceBottomSheetEnum.EMPTY_GEOFENCE_BOTTOM_SHEET -> {
                            mBaseActivity?.mGeofenceUtils?.emptyGeofenceBottomSheetAddBtn()
                        }

                        GeofenceBottomSheetEnum.ADD_GEOFENCE_BOTTOM_SHEET -> {
                            mBaseActivity?.mGeofenceUtils?.addGeofenceCloseBtn(requireActivity())
                        }

                        else -> {}
                    }
                }
            }

            override fun openAddGeofenceBottomSheet(point: LatLng) {
                mViewModel.getAddressLineFromLatLng(point.longitude, point.latitude)
            }
        }

    private val mSimulationInterface =
        object : SimulationInterface {
            override fun getGeofenceList() {
                mSimulationViewModel.callAllSimulation()
            }

            override fun evaluateGeofence(
                collectionName: String,
                position1: List<Double>?,
            ) {
                val identityId = mLocationProvider.getIdentityId()
                identityId?.let {
                    mSimulationViewModel.evaluateGeofence(
                        collectionName,
                        position1,
                        getDeviceId(requireContext()),
                        it,
                    )
                }
            }
        }

    private val mTrackingInterface =
        object : TrackingInterface {
            override fun updateBatch(latLng: LatLng) {
                latLng.let {
                    val positionData = arrayListOf<Double>()
                    positionData.add(it.longitude)
                    positionData.add(it.latitude)
                    mTrackingViewModel.batchUpdateDevicePosition(
                        TrackerCons.TRACKER_COLLECTION,
                        positionData,
                        getDeviceId(requireContext()),
                    )
                }
            }

            override fun updateBatch() {
                activity?.hideKeyboard()
                mMapHelper.setTrackingUpdateRoute(mTrackingUpDate)
            }

            override fun removeUpdateBatch() {
                mMapHelper.removeTrackingLocationListener()
            }

            override fun getLocationHistory(
                startDate: Date,
                endDate: Date,
            ) {
                mTrackingViewModel.getLocationHistory(
                    TrackerCons.TRACKER_COLLECTION,
                    getDeviceId(requireContext()),
                    startDate,
                    endDate,
                )
            }

            override fun getTodayLocationHistory(
                startDate: Date,
                endDate: Date,
            ) {
                mTrackingViewModel.getLocationHistoryToday(
                    TrackerCons.TRACKER_COLLECTION,
                    getDeviceId(requireContext()),
                    startDate,
                    endDate,
                )
            }

            override fun getGeofenceList(collectionName: String) {
                mBinding.bottomSheetTracking.layoutNoDataFound.root
                    .hide()
                mTrackingViewModel.getGeofenceList(collectionName)
            }

            override fun getCheckPermission() {
                mViewModel.mIsTrackingLocationClicked = true
                checkLocationPermission(false)
            }

            override fun getDeleteTrackingData() {
                mTrackingViewModel.deleteLocationHistory(
                    TrackerCons.TRACKER_COLLECTION,
                    getDeviceId(requireContext()),
                )
            }
        }

    private fun setNavigationAdapter() {
        mBinding.bottomSheetNavigation.apply {
            mNavigationAdapter = NavigationAdapter(mNavigationList, mPreferenceManager)
            rvNavigationList.layoutManager = LinearLayoutManager(requireContext())
            rvNavigationList.adapter = mNavigationAdapter
        }
    }

    private fun setMapStyleBottomSheet() {
        mViewModel.setMapListData(requireContext())
        mViewModel.setPoliticalListData(requireContext())
        mViewModel.setMapLanguageData(requireContext())
        mapStyleBottomSheetFragment =
            MapStyleBottomSheetFragment(
                mViewModel,
                mBaseActivity,
                mBottomSheetHelper,
                object : MapStyleBottomSheetFragment.MapInterface {
                    override fun infoIconClick() {
                        mViewModel.isFromMapStyle = true
                        hideMapStyleSheet()
                        setAttributionDataAndExpandSheet()
                    }

                    override fun mapStyleClick(
                        position: Int,
                        innerPosition: Int,
                    ) {
                        if (checkInternetConnection() && position != -1 && innerPosition != -1) {
                            val selectedInnerData =
                                mViewModel.mStyleList[position]
                                    .mapInnerData
                                    ?.get(innerPosition)
                                    ?.mapName
                            for (data in mViewModel.mStyleList) {
                                data.mapInnerData.let {
                                    if (it != null) {
                                        for (innerData in it) {
                                            if (innerData.mapName.equals(
                                                    selectedInnerData,
                                                )
                                            ) {
                                                if (innerData.isSelected) return
                                            }
                                        }
                                    }
                                }
                            }
                            selectedInnerData?.let { it3 ->
                                mapStyleChange(
                                    it3,
                                )
                            }
                        }
                    }

                    override fun mapColorScheme(
                        colorScheme: String,
                        mapStyleName: String,
                    ) {
                        clearAllMapData()
                        mMapHelper.updateStyle(mapStyleName, colorScheme)
                    }

                    override fun updateMapLanguage() {
                        mMapLibreMap?.style?.let { mMapHelper.setStyleLanguage(it)}
                    }
                },
            )
    }

    fun setUserProfile() {
        mBinding.bottomSheetSearch.apply {
            val userName = getUserName(mBaseActivity?.getUserInfo())
            if (userName.isNullOrEmpty()) {
                tvUserProfile.hide()
                ivUserProfile.show()
                cardUserProfile.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white,
                    ),
                )
            } else {
                ivUserProfile.hide()
                tvUserProfile.show()
                tvUserProfile.text = userName
                cardUserProfile.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellow,
                    ),
                )
            }
        }
    }

    private val mRouteUpDate =
        object : UpdateRouteInterface {
            override fun updateRoute(
                latLng: Location,
                bearing: Float?,
            ) {
                if (mBottomSheetHelper.isNavigationSheetVisible() && mViewModel.isLocationUpdatedNeeded) {
                    if (mViewModel.mNavigationResponse != null) {
                        mViewModel.mDestinationLatLng?.latitude?.let { latitude ->
                            mViewModel.mDestinationLatLng?.longitude?.let { longitude ->
                                val destinationLocation = Location("destination")
                                destinationLocation.latitude = latitude
                                destinationLocation.longitude = longitude
                                val distance = destinationLocation.distanceTo(latLng)
                                val selectedRouteMinDistance =
                                    when (mViewModel.mTravelMode) {
                                        RouteTravelMode.Car.value -> DISTANCE_FOR_DRIVE_TRUCK
                                        RouteTravelMode.Pedestrian.value -> DISTANCE_FOR_WALK
                                        RouteTravelMode.Truck.value -> DISTANCE_FOR_DRIVE_TRUCK
                                        RouteTravelMode.Scooter.value -> DISTANCE_FOR_SCOOTER
                                        else -> DISTANCE_FOR_DRIVE_TRUCK
                                    }
                                if (distance < selectedRouteMinDistance) {
                                    mBottomSheetHelper.hideNavigationSheet()
                                    mBottomSheetHelper.expandNavigationCompleteSheet()
                                    mBinding.bottomSheetNavigationComplete.tvNavigationCompleteAddress.text =
                                        mViewModel.mSearchDirectionDestinationData
                                            ?.amazonLocationAddress
                                            ?.label
                                            ?.split(
                                                ",",
                                            )?.toTypedArray()
                                            ?.get(0)
                                            ?: mViewModel.mSearchDirectionDestinationData?.amazonLocationAddress?.label

                                    mBinding.bottomSheetNavigationComplete.sheetNavigationCompleteTvDirectionStreet.text =
                                        getRegion(
                                            mViewModel.mSearchDirectionDestinationData
                                                ?.amazonLocationAddress
                                                ?.region
                                                ?.name,
                                            mViewModel.mSearchDirectionDestinationData
                                                ?.amazonLocationAddress
                                                ?.subRegion
                                                ?.name,
                                            mViewModel.mSearchDirectionDestinationData
                                                ?.amazonLocationAddress
                                                ?.country
                                                ?.name,
                                        )

                                    mBinding.cardNavigationTimeDialog.hide()
                                    mMapHelper.removeLine()
                                    mMapHelper.removeLocationListener()
                                    mMapLibreMap?.removeOnScaleListener(this@ExploreFragment)
                                } else {
                                    bearing?.let { mMapHelper.bearingCamera(it, latLng) }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        mViewModel.updateCalculateDistanceFromMode(
                                            latLng.latitude,
                                            latLng.longitude,
                                            mViewModel.mDestinationLatLng?.latitude,
                                            mViewModel.mDestinationLatLng?.longitude,
                                            mViewModel.getAvoidanceOptions(),
                                            mViewModel.mSelectedDepartOption,
                                            time = timeDepart,
                                            mViewModel.mTravelMode,
                                        )
                                        val isMetric =
                                            isMetric(
                                                mPreferenceManager.getValue(
                                                    KEY_UNIT_SYSTEM,
                                                    "",
                                                ),
                                            )
                                        val properties =
                                            listOf(
                                                Pair(AnalyticsAttribute.TRAVEL_MODE, mViewModel.mTravelMode),
                                                Pair(
                                                    AnalyticsAttribute.DISTANCE_UNIT,
                                                    if (isMetric) KILOMETERS else MILES,
                                                ),
                                                Pair(
                                                    AnalyticsAttribute.TRIGGERED_BY,
                                                    AnalyticsAttributeValue.ROUTE_MODULE,
                                                ),
                                                Pair(
                                                    AnalyticsAttribute.AVOID_FERRIES,
                                                    mViewModel.mIsAvoidFerries.toString(),
                                                ),
                                                Pair(
                                                    AnalyticsAttribute.AVOID_TOLLS,
                                                    mViewModel.mIsAvoidTolls.toString(),
                                                ),
                                                Pair(
                                                    AnalyticsAttribute.AVOID_DIRT_ROADS,
                                                    mViewModel.mIsAvoidDirtRoads.toString(),
                                                ),
                                                Pair(
                                                    AnalyticsAttribute.AVOID_U_TURNS,
                                                    mViewModel.mIsAvoidUTurns.toString(),
                                                ),
                                                Pair(
                                                    AnalyticsAttribute.AVOID_TUNNELS,
                                                    mViewModel.mIsAvoidTunnels.toString(),
                                                ),
                                            )
                                        activity?.let {
                                            (it as MainActivity).analyticsUtils?.recordEvent(
                                                ROUTE_SEARCH,
                                                properties,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    private val mTrackingUpDate =
        object : UpdateTrackingInterface {
            override fun updateRoute(
                latLng: Location,
                bearing: Float?,
            ) {
                latLng.let {
                    val positionData = arrayListOf<Double>()
                    positionData.add(it.longitude)
                    positionData.add(it.latitude)
                    mTrackingViewModel.batchUpdateDevicePosition(
                        TrackerCons.TRACKER_COLLECTION,
                        positionData,
                        getDeviceId(requireContext()),
                    )
                }
            }
        }

    fun refreshAfterSignOut() {
        val propertiesAws =
            listOf(
                Pair(
                    AnalyticsAttribute.TRIGGERED_BY,
                    AnalyticsAttributeValue.EXPLORER,
                ),
            )
        (activity as MainActivity).analyticsUtils?.recordEvent(
            EventType.SIGN_OUT_SUCCESSFUL,
            propertiesAws,
        )
        setUserProfile()
        showError(getString(R.string.sign_out_successfully))
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        mBinding.bottomSheetNavigation.apply {
            lifecycleScope.launch {
                withStarted { }
                mViewModel.mNavigationData.collect { handleResult ->
                    handleResult
                        .onLoading {
                            clSearchLoaderNavigation.root.show()
                            hideViews(tvDepartName, tvArrivalTime, tvDepartAddress,tvNavigationDot, tvDestinationName, tvDestinationAddress, rvNavigationList)
                        }.onSuccess {
                            clSearchLoaderNavigation.root.hide()
                            showViews(tvArrivalTime, rvNavigationList)
                            mBinding.bottomSheetNavigation.apply {
                                tvArrivalTime.text = it.time ?: ""
                                if (mViewModel.mSearchDirectionOriginData == null && mViewModel.mSearchDirectionDestinationData?.isDestination == true)  {
                                    tvDepartAddress.hide()
                                    showViews(tvDestinationName, tvDestinationAddress, tvDepartName)
                                    tvDepartName.text = getString(R.string.label_my_location)
                                    mViewModel.mSearchDirectionDestinationData?.amazonLocationAddress?.label?.split(",")?.let { parts ->
                                        showViews(tvDestinationName, tvDestinationAddress)
                                        tvDestinationName.text = parts.getOrNull(0) ?: ""
                                        tvDestinationAddress.text = parts.drop(1).joinToString(",").trim()
                                    }
                                } else if (mViewModel.mSearchDirectionDestinationData == null && mViewModel.mSearchDirectionOriginData?.isDestination == false) {
                                    tvDestinationAddress.hide()
                                    showViews(tvDepartName, tvDepartAddress, tvDestinationName)
                                    tvDestinationName.text = getString(R.string.label_my_location)
                                    mViewModel.mSearchDirectionOriginData?.amazonLocationAddress?.label?.split(",")?.let { parts ->
                                        tvDepartName.text = parts.getOrNull(0) ?: ""
                                        tvDepartAddress.text = parts.drop(1).joinToString(",").trim()
                                    }
                                } else {
                                    showViews(tvDepartName, tvDepartAddress, tvDestinationName, tvDestinationAddress)
                                    mViewModel.mSearchDirectionOriginData?.amazonLocationAddress?.label?.split(",")?.let { parts ->
                                        tvDepartName.text = parts.getOrNull(0) ?: ""
                                        tvDepartAddress.text =
                                            parts.drop(1).joinToString(",").trim()
                                    }
                                    mViewModel.mSearchDirectionDestinationData?.amazonLocationAddress?.label?.split(",")?.let { parts ->
                                        tvDestinationName.text = parts.getOrNull(0) ?: ""
                                        tvDestinationAddress.text =
                                            parts.drop(1).joinToString(",").trim()
                                    }
                                }
                                tvNavigationDistance.text =
                                    it.distance
                                        ?.let { it1 ->
                                            getMetricsNew(
                                                requireContext(),
                                                it1,
                                                isMetric(
                                                    mPreferenceManager.getValue(
                                                        KEY_UNIT_SYSTEM,
                                                        "",
                                                    ),
                                                ),
                                                true,
                                            )
                                        }
                                tvNavigationTime.text = it.duration
                                tvNavigationDot.show()
                            }
                            if (it.navigationList.isNotEmpty()) {
                                val index = if (it.navigationList.size > 1) 1 else 0
                                it.navigationList[0].distance?.let { distance ->
                                    setNavigationTimeDialog(
                                        distance,
                                        it.navigationList[index].getAddress(),
                                        it.navigationList[index].type.orEmpty(),
                                        it.navigationList[index]
                                    )
                                }
                            }
                            mNavigationList.clear()
                            mNavigationList.addAll(it.navigationList)
                            mNavigationAdapter?.notifyDataSetChanged()
                            if (mViewModel.isLocationUpdatedNeeded) {
                                mMapHelper.setUpdateRoute(mRouteUpDate)
                                mMapLibreMap?.addOnScaleListener(this@ExploreFragment)
                            }
                        }.onError { it ->
                            mViewModel.isLocationUpdatedNeeded = false
                            clSearchLoaderNavigation.root.hide()
                            rvNavigationList.show()
                            it.messageResource?.let {
                                showError(it.toString())
                            }
                        }
                }
            }
        }
        lifecycleScope.launch {
            withStarted { }
            mBinding.apply {
                mViewModel.mSearchLocationList.collect { handleResult ->
                    handleResult
                        .onLoading {
                            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                bottomSheetSearch.apply {
                                    clSearchLoaderSearchSheet.root.show()
                                    rvSearchPlacesSuggestion.hide()
                                }
                            } else {
                                bottomSheetDirectionSearch.apply {
                                    clSearchLoaderDirectionSearch.root.show()
                                    rvSearchPlacesSuggestionDirection.hide()
                                    rvSearchPlacesDirection.hide()
                                }
                            }
                        }.onSuccess {
                            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                addPlaceDataInList(it, SearchApiEnum.SEARCH_PLACE_INDEX_TEXT)
                                mAdapter?.notifyDataSetChanged()
                            } else {
                                addPlaceDirectionDataInList(
                                    it,
                                    SearchApiEnum.SEARCH_PLACE_INDEX_TEXT,
                                )
                                mAdapterDirection?.notifyDataSetChanged()
                            }
                        }.onError {
                            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                mBinding.bottomSheetSearch.apply {
                                    clSearchLoaderSearchSheet.root.hide()
                                    rvSearchPlacesSuggestion.hide()
                                }
                            } else {
                                bottomSheetDirectionSearch.apply {
                                    clDriveLoader.hide()
                                    clWalkLoader.hide()
                                    clTruckLoader.hide()
                                    clScooterLoader.hide()
                                    clMapOptionRoute.show()
                                }
                            }
                            if (it.messageResource.toString() ==
                                resources.getString(R.string.check_your_internet_connection_and_try_again)
                            ) {
                                mBinding.apply {
                                    if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                        hideDirectionSearchData()
                                    } else if (mBottomSheetHelper.isSearchPlaceSheetVisible()) {
                                        hideSearchSheetData()
                                    }
                                }
                            }
                        }
                }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mBinding.apply {
                mViewModel.searchForSuggestionsResultList.collect { handleResult ->
                    handleResult
                        .onLoading {
                            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                bottomSheetSearch.apply {
                                    clSearchLoaderSearchSheet.root.show()
                                    rvSearchPlaces.hide()
                                    rvSearchPlacesSuggestion.hide()
                                }
                            } else {
                                bottomSheetDirectionSearch.apply {
                                    layoutNoDataFound.root.hide()
                                    rvSearchPlacesSuggestionDirection.hide()
                                    rvSearchPlacesDirection.hide()
                                    clSearchLoaderDirectionSearch.root.show()
                                }
                            }
                        }.onSuccess {
                            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                addPlaceDataInList(it, SearchApiEnum.SEARCH_PLACE_SUGGESTION)
                                mSearchPlacesSuggestionAdapter?.notifyDataSetChanged()
                            } else {
                                addPlaceDirectionDataInList(
                                    it,
                                    SearchApiEnum.SEARCH_PLACE_SUGGESTION,
                                )
                                mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
                            }
                        }.onError {
                            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                bottomSheetSearch.apply {
                                    clSearchLoaderSearchSheet.root.hide()
                                    rvSearchPlaces.hide()
                                    rvSearchPlacesSuggestion.hide()
                                }
                            } else {
                                bottomSheetDirectionSearch.apply {
                                    clSearchLoaderDirectionSearch.root.hide()
                                }
                            }
                            if (it.messageResource.toString() ==
                                resources.getString(R.string.check_your_internet_connection_and_try_again)
                            ) {
                                mBinding.apply {
                                    if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                        hideDirectionSearchData()
                                    } else if (!mBottomSheetHelper.isSearchPlaceSheetVisible()) {
                                        hideSearchSheetData()
                                    }
                                }
                            }
                        }
                }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mViewModel.mCalculateDistance.collect { handleResult ->
                handleResult
                    .onLoading {
                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            mBinding.apply {
                                bottomSheetDirection.apply {
                                    groupDistanceLoad.show()
                                    groupDistance.invisible()
                                    btnDirection.alpha = 0.5F
                                    btnDirection.isEnabled = false
                                }
                            }
                        } else {
                            mBinding.bottomSheetDirectionSearch.clSearchLoaderDirectionSearch.root
                                .hide()
                        }
                    }.onSuccess {
                        mBinding.apply {
                            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                bottomSheetDirection.apply {
                                    groupDistanceLoad.hide()
                                    btnDirection.alpha = 1.0F
                                    btnDirection.isEnabled = true
                                    groupDistance.show()
                                    tvDirectionError.hide()
                                    tvDirectionError2.hide()
                                    ivInfo.hide()
                                    btnDirection.setCardBackgroundColor(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.color_primary_green,
                                        ),
                                    )
                                    if (activity?.checkLocationPermission() == true) {
                                        if (!isGPSEnabled(requireContext())) {
                                            mBinding.bottomSheetDirection.tvDirectionError2.text =
                                                getString(R.string.label_location_permission_denied)
                                            locationErrorDirection()
                                        }
                                    } else {
                                        mBinding.bottomSheetDirection.tvDirectionError2.text =
                                            getString(R.string.label_location_permission_denied)
                                        locationErrorDirection()
                                    }
                                }
                            } else {
                                when (it.name.toString()) {
                                    RouteTravelMode.Pedestrian.value -> {
                                        mBinding.bottomSheetDirectionSearch.clWalkLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clWalk.show()
                                    }

                                    RouteTravelMode.Car.value -> {
                                        mBinding.bottomSheetDirectionSearch.clDriveLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clDrive.show()
                                    }

                                    RouteTravelMode.Truck.value -> {
                                        mBinding.bottomSheetDirectionSearch.clTruckLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clTruck.show()
                                    }

                                    RouteTravelMode.Scooter.value -> {
                                        mBinding.bottomSheetDirectionSearch.clScooterLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clScooter.show()
                                    }
                                }
                                mBinding.bottomSheetDirectionSearch.clMapOptionRoute.show()
                            }
                        }
                        when (it.name) {
                            RouteTravelMode.Car.value -> {
                                mViewModel.mCarData = it.calculateRouteResult
                                mViewModel.mCarCalculateDistanceResponse = it
                                setCarRouteData(it)
                            }

                            RouteTravelMode.Pedestrian.value -> {
                                mViewModel.mWalkingData = it.calculateRouteResult
                                mViewModel.mWalkCalculateDistanceResponse = it
                                setWalkingRouteData(it)
                            }

                            RouteTravelMode.Truck.value -> {
                                mViewModel.mTruckData = it.calculateRouteResult
                                mViewModel.mTruckCalculateDistanceResponse = it
                                setTruckRouteData(it)
                            }

                            RouteTravelMode.Scooter.value -> {
                                mViewModel.mScooterData = it.calculateRouteResult
                                mViewModel.mScooterCalculateDistanceResponse = it
                                setScooterRouteData(it)
                            }
                        }
                    }.onError {
                        mBinding.apply {
                            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                bottomSheetDirection.apply {
                                    groupDistanceLoad.hide()
                                    btnDirection.isEnabled = true
                                    btnDirection.alpha = 1.0F
                                    groupDistance.show()
                                    tvDirectionTime.hide()
                                    tvDirectionDot.invisible()
                                    tvDirectionTime.text = ""
                                    tvDirectionDistance.text = ""
                                    mViewModel.mCarData = null
                                }
                                checkErrorDirectionDistance()
                                mViewModel.isCalculateDriveApiError = true
                            } else {
                                bottomSheetDirectionSearch.apply {
                                    when (it.messageResource.toString()) {
                                        RouteTravelMode.Pedestrian.value -> {
                                            setGoButtonState(
                                                edtSearchDirection.text.toString(),
                                                cardWalkGo,
                                                clWalk,
                                            )
                                            cardWalkGo.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireContext(),
                                                    R.color.btn_go_disable,
                                                ),
                                            )
                                            showCalculateRouteAPIError(RouteTravelMode.Pedestrian.value)
                                            mViewModel.mWalkingData = null
                                            mViewModel.mWalkCalculateDistanceResponse = null
                                            mViewModel.isCalculateWalkApiError = true
                                            clWalkLoader.hide()
                                            clWalk.show()
                                            tvWalkLeaveTime.hide()
                                        }

                                        RouteTravelMode.Car.value -> {
                                            setGoButtonState(
                                                edtSearchDirection.text.toString(),
                                                cardDriveGo,
                                                clDrive,
                                            )
                                            cardDriveGo.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireContext(),
                                                    R.color.btn_go_disable,
                                                ),
                                            )
                                            showCalculateRouteAPIError(RouteTravelMode.Car.value)
                                            mViewModel.isCalculateDriveApiError = true
                                            mViewModel.mCarData = null
                                            mViewModel.mCarCalculateDistanceResponse = null
                                            clDriveLoader.hide()
                                            clDrive.show()
                                            tvDriveLeaveTime.hide()
                                        }

                                        RouteTravelMode.Truck.value -> {
                                            setGoButtonState(
                                                edtSearchDirection.text.toString(),
                                                cardTruckGo,
                                                clTruck,
                                            )
                                            cardTruckGo.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireContext(),
                                                    R.color.btn_go_disable,
                                                ),
                                            )
                                            showCalculateRouteAPIError(RouteTravelMode.Truck.value)
                                            mViewModel.mTruckData = null
                                            mViewModel.mTruckCalculateDistanceResponse = null
                                            mViewModel.isCalculateTruckApiError = true
                                            clTruckLoader.hide()
                                            clTruck.show()
                                            tvTruckLeaveTime.hide()
                                        }

                                        RouteTravelMode.Scooter.value -> {
                                            setGoButtonState(
                                                edtSearchDirection.text.toString(),
                                                cardScooterGo,
                                                clScooter,
                                            )
                                            cardScooterGo.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireContext(),
                                                    R.color.btn_go_disable,
                                                ),
                                            )
                                            showCalculateRouteAPIError(RouteTravelMode.Scooter.value)
                                            mViewModel.mScooterData = null
                                            mViewModel.mScooterCalculateDistanceResponse = null
                                            mViewModel.isCalculateScooterApiError = true
                                            clScooterLoader.hide()
                                            clScooter.show()
                                            tvScooterLeaveTime.hide()
                                        }
                                    }
                                }
                                mBinding.bottomSheetDirectionSearch.clMapOptionRoute.show()
                                checkAllApiCallFailed()
                            }
                        }
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mViewModel.mUpdateCalculateDistance.collect { handleResult ->
                handleResult
                    .onLoading {}
                    .onSuccess { data ->
                        var isWalk = false
                        when (data.name) {
                            RouteTravelMode.Car.value -> {
                                isWalk = false
                                mViewModel.mCarData = data.calculateRouteResult
                            }

                            RouteTravelMode.Pedestrian.value -> {
                                isWalk = true
                                mViewModel.mWalkingData = data.calculateRouteResult
                            }

                            RouteTravelMode.Truck.value -> {
                                isWalk = false
                                mViewModel.mTruckData = data.calculateRouteResult
                            }

                            RouteTravelMode.Scooter.value -> {
                                isWalk = false
                                mViewModel.mScooterData = data.calculateRouteResult
                            }
                        }

                        activity?.runOnUiThread {
                            data.calculateRouteResult?.routes?.get(0)?.legs?.let { legs ->
                                drawPolyLineOnMap(
                                    legs,
                                    true,
                                    isWalk,
                                    isLocationIcon = mBottomSheetHelper.isNavigationSheetVisible(),
                                )
                                val mNavigationListModel = ArrayList<NavigationData>()
                                mNavigationListModel.clear()
                                mViewModel.mNavigationResponse = NavigationResponse()
                                mViewModel.mNavigationResponse?.duration =
                                    data.calculateRouteResult?.routes?.get(0)?.summary
                                        ?.duration
                                        ?.let { getTime(requireContext(), it) }
                                mViewModel.mNavigationResponse?.distance =
                                    data.calculateRouteResult?.routes?.get(0)?.summary
                                        ?.distance
                                        ?.toDouble()
                                val getLastTime = legs.last().vehicleLegDetails?.arrival?.time
                                    ?: legs.last().pedestrianLegDetails?.arrival?.time
                                    ?: legs.last().ferryLegDetails?.arrival?.time
                                mViewModel.mNavigationResponse?.time =
                                    getLastTime?.let { formatToDisplayTime(it, HH_MM) }
                                for (leg in legs) {
                                    if (leg.vehicleLegDetails != null) {
                                        leg.vehicleLegDetails?.travelSteps?.forEach {
                                            mNavigationListModel.add(
                                                NavigationData(
                                                    isDataSuccess = true,
                                                    destinationAddress = it.instruction,
                                                    distance = it.distance.toDouble(),
                                                    duration = it.duration.toDouble(),
                                                    type = it.type.value,
                                                    routeTurnStepDetails = it.turnStepDetails,
                                                    routeContinueHighwayStepDetails = it.continueHighwayStepDetails,
                                                    routeContinueStepDetails = it.continueStepDetails,
                                                    routeEnterHighwayStepDetails = it.enterHighwayStepDetails,
                                                    routeExitStepDetails = it.exitStepDetails,
                                                    routeKeepStepDetails = it.keepStepDetails,
                                                    routeRampStepDetails = it.rampStepDetails,
                                                    routeRoundaboutEnterStepDetails = it.roundaboutEnterStepDetails,
                                                    routeRoundaboutExitStepDetails = it.roundaboutExitStepDetails,
                                                    routeRoundaboutPassStepDetails = it.roundaboutPassStepDetails,
                                                    routeUTurnStepDetails = it.uTurnStepDetails
                                                ),
                                            )
                                        }
                                    } else if (leg.pedestrianLegDetails != null) {
                                        leg.pedestrianLegDetails?.travelSteps?.forEach {
                                            mNavigationListModel.add(
                                                NavigationData(
                                                    isDataSuccess = true,
                                                    destinationAddress = it.instruction,
                                                    distance = it.distance.toDouble(),
                                                    duration = it.duration.toDouble(),
                                                    type = it.type.value,
                                                    routeTurnStepDetails = it.turnStepDetails,
                                                    routeContinueStepDetails = it.continueStepDetails,
                                                    routeKeepStepDetails = it.keepStepDetails,
                                                    routeRoundaboutEnterStepDetails = it.roundaboutEnterStepDetails,
                                                    routeRoundaboutExitStepDetails = it.roundaboutExitStepDetails,
                                                    routeRoundaboutPassStepDetails = it.roundaboutPassStepDetails,
                                                ),
                                            )
                                        }
                                    } else if (leg.ferryLegDetails != null) {
                                        leg.ferryLegDetails?.travelSteps?.forEach {
                                            mNavigationListModel.add(
                                                NavigationData(
                                                    isDataSuccess = true,
                                                    destinationAddress = it.instruction,
                                                    distance = it.distance.toDouble(),
                                                    duration = it.duration.toDouble(),
                                                    type = it.type.value,
                                                ),
                                            )
                                        }
                                    }
                                }
                                if (mNavigationListModel.isNotEmpty()) {
                                    val index = if (mNavigationListModel.size > 1) 1 else 0
                                    mNavigationListModel[0].distance?.let { duration ->
                                        setNavigationTimeDialog(
                                            duration,
                                            mNavigationListModel[index].destinationAddress.orEmpty(),
                                            mNavigationListModel[index].type,
                                            mNavigationListModel[index]
                                        )
                                    }
                                }
                                mViewModel.mNavigationResponse?.destinationAddress =
                                    mViewModel.mSearchSuggestionData?.amazonLocationAddress?.label
                                mViewModel.mNavigationResponse?.navigationList = mNavigationListModel
                                mViewModel.mNavigationResponse?.navigationList?.let { it1 ->
                                    mNavigationList.clear()
                                    mNavigationList.addAll(
                                        it1
                                    )
                                    mNavigationAdapter?.notifyDataSetChanged()
                                }
                            }
                            mBinding.bottomSheetNavigation.apply {
                                tvNavigationTime.text =
                                    data.calculateRouteResult?.routes?.get(0)?.summary?.duration?.let { it1 ->
                                        getTime(
                                            requireContext(),
                                            it1,
                                        )
                                    }
                                val isMetric =
                                    isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
                                tvNavigationDistance.text =
                                    data.calculateRouteResult
                                        ?.routes
                                        ?.get(0)
                                        ?.summary
                                        ?.distance
                                        ?.let { it1 ->
                                            getMetricsNew(
                                                requireContext(),
                                                it1.toDouble(),
                                                isMetric,
                                                true,
                                            )
                                        }
                            }
                        }
                    }.onError {
                        showError(it.messageResource.toString())
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mViewModel.mUpdateRoute.collect { handleResult ->
                handleResult
                    .onLoading {}
                    .onSuccess {
                        when (it.name) {
                            RouteTravelMode.Car.value -> {
                                mViewModel.mCarData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mCarData?.routes?.get(0)?.legs?.let { legs ->
                                        setSelectedMode()
                                        drawPolyLineOnMap(
                                            legs,
                                            isLineUpdate = false,
                                            isWalk = false,
                                            isLocationIcon = false,
                                        )
                                    }
                                }
                            }

                            RouteTravelMode.Pedestrian.value -> {
                                mViewModel.mWalkingData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mWalkingData?.routes?.get(0)?.legs?.let { walkingData ->
                                        setSelectedMode()
                                        drawPolyLineOnMap(
                                            walkingData,
                                            isLineUpdate = false,
                                            isWalk = true,
                                            isLocationIcon = false,
                                        )
                                    }
                                }
                            }

                            RouteTravelMode.Truck.value -> {
                                mViewModel.mTruckData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mTruckData?.routes?.get(0)?.legs?.let { truckData ->
                                        setSelectedMode()
                                        drawPolyLineOnMap(
                                            truckData,
                                            isLineUpdate = false,
                                            isWalk = false,
                                            isLocationIcon = false,
                                        )
                                    }
                                }
                            }

                            RouteTravelMode.Scooter.value -> {
                                mViewModel.mScooterData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mScooterData?.routes?.get(0)?.legs?.let { scooterData ->
                                        setSelectedMode()
                                        drawPolyLineOnMap(
                                            scooterData,
                                            isLineUpdate = false,
                                            isWalk = false,
                                            isLocationIcon = false,
                                        )
                                    }
                                }
                            }
                        }

                        mMapHelper.navigationZoomCamera(mMapHelper.getBestAvailableLocation(), mViewModel.isZooming)

                        it.calculateRouteResult?.let { it1 ->
                            mMapHelper.clearOriginMarker()
                            fetchAddressFromLatLng(it1)
                        }
                    }.onError {
                        mBinding.bottomSheetDirection.apply {
                            groupDistance.invisible()
                            tvDirectionTime.hide()
                        }
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mViewModel.addressLineData.collect { handleResult ->
                handleResult
                    .onLoading {}
                    .onSuccess { response ->
                        if (!mBottomSheetHelper.isSearchPlaceSheetVisible() || mBottomSheetHelper.isDirectionSheetVisible()) {
                            val searchSuggestionData = SearchSuggestionData()
                            if (!response.reverseGeocodeResponse?.resultItems.isNullOrEmpty()) {
                                response.reverseGeocodeResponse?.let { searchPlaceIndexForPositionResult ->
                                    searchSuggestionData.text =
                                        searchPlaceIndexForPositionResult.resultItems?.get(0)?.title
                                    searchSuggestionData.searchText =
                                        searchPlaceIndexForPositionResult.resultItems?.get(0)?.title
                                    searchSuggestionData.distance =
                                        searchPlaceIndexForPositionResult.resultItems
                                            ?.get(0)
                                            ?.distance
                                            ?.toDouble()
                                    searchSuggestionData.isDestination = true
                                    searchSuggestionData.placeId =
                                        searchPlaceIndexForPositionResult.resultItems?.get(0)?.placeId
                                    searchSuggestionData.isPlaceIndexForPosition = false
                                    response.latitude?.let { lat ->
                                        response.longitude?.let { lng ->
                                            searchSuggestionData.position = listOf(lng, lat)
                                        }
                                    }
                                    searchSuggestionData.amazonLocationAddress =
                                        searchPlaceIndexForPositionResult.resultItems?.get(0)?.address
                                }
                            } else {
                                searchSuggestionData.text =
                                    String.format(
                                        Locale.getDefault(),
                                        STRING_FORMAT,
                                        response.latitude,
                                        response.longitude,
                                    )
                                searchSuggestionData.searchText =
                                    String.format(
                                        Locale.getDefault(),
                                        STRING_FORMAT,
                                        response.latitude,
                                        response.longitude,
                                    )
                                searchSuggestionData.distance = null
                                searchSuggestionData.isDestination = true
                                searchSuggestionData.placeId = null
                                searchSuggestionData.isPlaceIndexForPosition = false
                                response.latitude?.let { lat ->
                                    response.longitude?.let { lng ->
                                        searchSuggestionData.position = listOf(lng, lat)
                                    }
                                }
                                val place =
                                    Address {
                                        label =
                                            String.format(
                                                Locale.getDefault(),
                                                STRING_FORMAT,
                                                response.latitude,
                                                response.longitude,
                                            )
                                        addressNumber =
                                            String.format(
                                                Locale.getDefault(),
                                                STRING_FORMAT,
                                                response.latitude,
                                                response.longitude,
                                            )
                                        street =
                                            String.format(
                                                Locale.getDefault(),
                                                STRING_FORMAT,
                                                response.latitude,
                                                response.longitude,
                                            )
                                        postalCode =
                                            String.format(
                                                Locale.getDefault(),
                                                STRING_FORMAT,
                                                response.latitude,
                                                response.longitude,
                                            )
                                    }
                                searchSuggestionData.amazonLocationAddress = place
                            }
                            setDirectionData(searchSuggestionData, true)
                            return@onSuccess
                        }
                        response.reverseGeocodeResponse?.let { searchPlaceIndexForPositionResult ->
                            if (searchPlaceIndexForPositionResult.resultItems?.isNotEmpty() == true) {
                                val label =
                                    searchPlaceIndexForPositionResult.resultItems?.get(0)?.title
                                if (label != null) {
                                    if (label.contains(",")) {
                                        val index = label.indexOf(",")
                                        val result: String =
                                            index.let { it1 -> label.substring(0, it1) }
                                        mBaseActivity?.mGeofenceUtils?.setSearchText(result)
                                    } else {
                                        mBaseActivity?.mGeofenceUtils?.setSearchText(label)
                                    }
                                }
                            }
                        }
                    }.onError {
                        showError(it.messageResource.toString())
                    }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mViewModel.placeData.collect { handleResult ->
                handleResult
                    .onLoading {
                        if (mBottomSheetHelper.isDirectionSheetVisible()) {
                            mBinding.bottomSheetDirection.apply {
                                groupPlaceDetailsLoad.show()
                                viewDivider.show()
                                hideDirectionData()
                            }
                        }
                    }.onSuccess { response ->
                        if (mBottomSheetHelper.isDirectionSheetVisible()) {
                            mViewModel.mSearchSuggestionData?.let {
                                it.contacts = response.contacts
                                it.openingHours = response.openingHours
                            }
                            mViewModel.mSearchDirectionDestinationData?.let {
                                it.contacts = response.contacts
                                it.openingHours = response.openingHours
                            }
                            if (response.contacts == null && response.openingHours == null) {
                                mBinding.bottomSheetDirection.apply {
                                    viewDivider.hide()
                                    hideDirectionData()
                                }
                            }
                            mBinding.bottomSheetDirection.apply {
                                groupPlaceDetailsLoad.hide()
                            }
                            mViewModel.mSearchSuggestionData?.let { setContactPlaceData(it) }
                            return@onSuccess
                        }
                    }.onError {
                        if (mBottomSheetHelper.isDirectionSheetVisible()) {
                            mBinding.bottomSheetDirection.apply {
                                groupPlaceDetailsLoad.hide()
                                viewDivider.hide()
                                hideDirectionData()
                            }
                        }
                    }
            }
        }
    }

    private fun FragmentExploreBinding.hideSearchSheetData() {
        hideViews(
            bottomSheetSearch.layoutNoDataFound.root,
            bottomSheetSearch.nsSearchPlaces,
        )
        bottomSheetSearch.clNoInternetConnectionSearchSheet.show()
    }

    private fun FragmentExploreBinding.hideDirectionSearchData() {
        hideViews(
            bottomSheetDirectionSearch.layoutNoDataFound.root,
            bottomSheetDirectionSearch.layoutCardError.root,
            bottomSheetDirectionSearch.rvSearchPlacesDirection,
            bottomSheetDirectionSearch.rvSearchPlacesSuggestionDirection,
        )
        bottomSheetDirectionSearch.clNoInternetConnectionDirectionSearch.show()
    }

    private fun setScooterRouteData(it: CalculateDistanceResponse) {
        mBinding.bottomSheetDirectionSearch.apply {
            mViewModel.mScooterData?.routes?.get(0).let { route ->
                setGoButtonState(
                    edtSearchDirection.text.toString(),
                    cardScooterGo,
                    clScooter,
                )
                route?.summary?.let { summary ->
                    tvScooterDistance.text =
                        mPreferenceManager
                            .getValue(
                                KEY_UNIT_SYSTEM,
                                "",
                            ).let { unitSystem ->
                                val isMetric = isMetric(unitSystem)
                                getMetricsNew(
                                    requireContext(),
                                    summary.distance.toDouble(),
                                    isMetric,
                                    true,
                                )
                            }
                    tvScooterMinute.text =
                        getTime(
                            requireContext(),
                            summary.duration,
                        )
                    if (mViewModel.mSelectedDepartOption == DepartOption.ARRIVE_TIME.name) {
                        tvScooterLeaveTime.show()
                        val getTime = getFirstLegDepartTime(route)
                        tvScooterLeaveTime.text = buildString {
                            append(getString(R.string.label_leave_at))
                            append(" ")
                            append(getTime?.let { formatToDisplayTime(it, HH_MM_AA) })
                        }
                    } else {
                        tvScooterLeaveTime.hide()
                    }
                }
                if (mViewModel.mTravelMode == RouteTravelMode.Scooter.value) {
                    setSelectedMode()
                    route?.let { it1 ->
                        drawPolyLineOnMap(
                            it1.legs,
                            isLineUpdate = false,
                            isWalk = false,
                            isLocationIcon = false,
                            sourceLatLng = it.sourceLatLng,
                            destinationLatLng = it.destinationLatLng,
                        )
                    }
                }
            }
        }
    }

    private fun setTruckRouteData(it: CalculateDistanceResponse) {
        mBinding.bottomSheetDirectionSearch.apply {
            mViewModel.mTruckData?.routes?.get(0).let { route ->
                setGoButtonState(
                    edtSearchDirection.text.toString(),
                    cardTruckGo,
                    clTruck,
                )
                route?.summary?.let { summary ->
                    tvTruckDistance.text =
                        mPreferenceManager
                            .getValue(
                                KEY_UNIT_SYSTEM,
                                "",
                            ).let { unitSystem ->
                                val isMetric = isMetric(unitSystem)
                                getMetricsNew(
                                    requireContext(),
                                    summary.distance.toDouble(),
                                    isMetric,
                                    true,
                                )
                            }
                    tvTruckMinute.text =
                        getTime(
                            requireContext(),
                            summary.duration,
                        )
                    if (mViewModel.mSelectedDepartOption == DepartOption.ARRIVE_TIME.name) {
                        tvTruckLeaveTime.show()
                        val getTime = getFirstLegDepartTime(route)
                        tvTruckLeaveTime.text = buildString {
                            append(getString(R.string.label_leave_at))
                            append(" ")
                            append(getTime?.let { formatToDisplayTime(it, HH_MM_AA) })
                        }
                    } else {
                        tvTruckLeaveTime.hide()
                    }
                }
                if (mViewModel.mTravelMode == RouteTravelMode.Truck.value) {
                    setSelectedMode()
                    route?.let { it1 ->
                        drawPolyLineOnMap(
                            it1.legs,
                            isLineUpdate = false,
                            isWalk = false,
                            isLocationIcon = false,
                            sourceLatLng = it.sourceLatLng,
                            destinationLatLng = it.destinationLatLng,
                        )
                    }
                }
            }
        }
    }

    private fun setWalkingRouteData(it: CalculateDistanceResponse) {
        mBinding.bottomSheetDirectionSearch.apply {
            mViewModel.mWalkingData?.routes?.get(0).let { route ->
                setGoButtonState(
                    edtSearchDirection.text.toString(),
                    cardWalkGo,
                    clWalk,
                )
                route?.summary?.let { summary ->
                    tvWalkDistance.text =
                        mPreferenceManager
                            .getValue(
                                KEY_UNIT_SYSTEM,
                                "",
                            ).let { unitSystem ->
                                val isMetric = isMetric(unitSystem)
                                getMetricsNew(
                                    requireContext(),
                                    summary.distance.toDouble(),
                                    isMetric,
                                    true,
                                )
                            }
                    tvWalkMinute.text =
                        getTime(
                            requireContext(),
                            summary.duration,
                        )

                    if (mViewModel.mSelectedDepartOption == DepartOption.ARRIVE_TIME.name) {
                        tvWalkLeaveTime.show()
                        val getTime = getFirstLegDepartTime(route)
                        tvWalkLeaveTime.text = buildString {
                            append(getString(R.string.label_leave_at))
                            append(" ")
                            append(getTime?.let { formatToDisplayTime(it, HH_MM_AA) })
                        }
                    } else {
                        tvWalkLeaveTime.hide()
                    }
                }
                if (mViewModel.mTravelMode == RouteTravelMode.Pedestrian.value) {
                    setSelectedMode()
                    route?.let { it1 ->
                        drawPolyLineOnMap(
                            it1.legs,
                            isLineUpdate = false,
                            isWalk = true,
                            isLocationIcon = false,
                            sourceLatLng = it.sourceLatLng,
                            destinationLatLng = it.destinationLatLng,
                        )
                    }
                }
            }
        }
    }

    private fun setCarRouteData(it: CalculateDistanceResponse) {
        mBinding.bottomSheetDirection.apply {
            mViewModel.mCarData?.routes?.get(0).let { route ->
                if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                    route?.summary?.let { summary ->
                        tvDirectionDistance.text =
                            mPreferenceManager
                                .getValue(
                                    KEY_UNIT_SYSTEM,
                                    "",
                                ).let { unitSystem ->
                                    val isMetric = isMetric(unitSystem)
                                    getMetricsNew(
                                        requireContext(),
                                        summary.distance.toDouble(),
                                        isMetric,
                                        true,
                                    )
                                }
                        groupDistance.show()
                        tvDirectionDot.show()
                        tvDirectionTime.show()
                        tvDirectionTime.text =
                            getTime(
                                requireContext(),
                                summary.duration,
                            )
                    }
                }
                mBinding.bottomSheetDirectionSearch.apply {
                    if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                        mViewModel.mIsDirectionDataSet = true
                        edtSearchDest.setText(tvDirectionAddress.text)
                        lifecycleScope.launch {
                            delay(CLICK_DEBOUNCE_ENABLE)
                            mViewModel.mIsDirectionDataSet = false
                        }
                    } else {
                        if (mViewModel.mTravelMode == RouteTravelMode.Car.value) {
                            setSelectedMode()
                            route?.let { it1 ->
                                drawPolyLineOnMap(
                                    it1.legs,
                                    isLineUpdate = false,
                                    isWalk = false,
                                    isLocationIcon = false,
                                    sourceLatLng = it.sourceLatLng,
                                    destinationLatLng = it.destinationLatLng,
                                )
                            }
                        }
                    }

                    setGoButtonState(
                        edtSearchDirection.text.toString(),
                        cardDriveGo,
                        clDrive,
                    )
                    route?.summary?.let { summary ->
                        tvDriveDistance.text =
                            mPreferenceManager
                                .getValue(
                                    KEY_UNIT_SYSTEM,
                                    "",
                                ).let { unitSystem ->
                                    val isMetric = isMetric(unitSystem)
                                    getMetricsNew(
                                        requireContext(),
                                        summary.distance.toDouble(),
                                        isMetric,
                                        true,
                                    )
                                }
                        tvDriveMinute.text =
                            getTime(
                                requireContext(),
                                summary.duration,
                            )
                        if (mViewModel.mSelectedDepartOption == DepartOption.ARRIVE_TIME.name) {
                            tvDriveLeaveTime.show()
                            val getTime = getFirstLegDepartTime(route)
                            tvDriveLeaveTime.text = buildString {
                                append(getString(R.string.label_leave_at))
                                append(" ")
                                append(getTime?.let { formatToDisplayTime(it, HH_MM_AA) })
                            }
                        } else {
                            tvDriveLeaveTime.hide()
                        }
                    }
                }
            }
        }
    }

    private fun getFirstLegDepartTime(route: Route): String? {
        val getTime =
            if (route.legs.first().vehicleLegDetails != null) {
                route.legs.first().vehicleLegDetails?.departure?.time
            } else if (route.legs.first().pedestrianLegDetails != null) {
                route.legs.first()
                    .pedestrianLegDetails?.departure?.time
            } else if (route.legs.first().ferryLegDetails != null) {
                route.legs.first()
                    .ferryLegDetails?.departure?.time
            } else {
                ""
            }
        return getTime
    }

    private fun showCalculateRouteAPIError(value: String) {
        if (mViewModel.mTravelMode == value) {
            showError(getString(R.string.no_route_found))
        }
    }

    private fun checkAllApiCallFailed() {
        mBinding.apply {
            bottomSheetDirectionSearch.apply {
                if (mViewModel.isCalculateDriveApiError && mViewModel.isCalculateWalkApiError && mViewModel.isCalculateTruckApiError) {
                    showAllApiFailed()
                } else {
                    hideAllApiCallFailed()
                }
            }
        }
    }

    private fun BottomSheetDirectionSearchBinding.hideAllApiCallFailed() {
        cardMapOption.show()
        layoutCardError.groupCardErrorNoSearchFound.hide()
        layoutCardError.root.hide()
    }

    private fun BottomSheetDirectionSearchBinding.showAllApiFailed() {
        clMyLocation.root.hide()
        cardMapOption.hide()
        layoutCardError.groupCardErrorNoSearchFound.show()
        layoutCardError.root.show()
        layoutCardError.tvCardError1.text = getString(R.string.no_route_found)
        layoutCardError.tvCardError2.hide()
    }

    private fun BottomSheetDirectionBinding.locationErrorDirection() {
        tvDirectionError.hide()
        showViews(tvDirectionError2, ivInfo)
    }

    private fun setGoButtonState(
        source: String,
        cardView: MaterialCardView,
        cl: ConstraintLayout,
    ) {
        if (source == resources.getString(R.string.label_my_location)) {
            mBinding.bottomSheetDirectionSearch.apply {
                when (cardView) {
                    cardWalkGo -> {
                        tvWalkGo.text = getString(R.string.btn_go)
                    }

                    cardDriveGo -> {
                        tvDriveGo.text = getString(R.string.btn_go)
                    }

                    cardTruckGo -> {
                        tvTruckGo.text = getString(R.string.btn_go)
                    }

                    cardScooterGo -> {
                        tvScooterGo.text = getString(R.string.btn_go)
                    }
                }
            }
        } else {
            mBinding.bottomSheetDirectionSearch.apply {
                when (cardView) {
                    cardWalkGo -> {
                        tvWalkGo.text = getString(R.string.label_preview)
                    }

                    cardDriveGo -> {
                        tvDriveGo.text = getString(R.string.label_preview)
                    }

                    cardTruckGo -> {
                        tvTruckGo.text = getString(R.string.label_preview)
                    }

                    cardScooterGo -> {
                        tvScooterGo.text = getString(R.string.label_preview)
                    }
                }
            }
        }
        cardView.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_dark_yellow,
            ),
        )
        cardView.isClickable = true
        cl.isClickable = true
    }

    private fun setNavigationTimeDialog(
        distance: Double,
        address: String,
        type: String?,
        data: NavigationData?,
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            mBinding.apply {
                if (!mViewModel.mRouteFinish) cardNavigationTimeDialog.show() else cardNavigationTimeDialog.hide()
                tvDistance.text =
                    mPreferenceManager
                        .getValue(
                            KEY_UNIT_SYSTEM,
                            "",
                        ).let { unitSystem ->
                            val isMetric = isMetric(unitSystem)
                            getMetricsNew(
                                requireContext(),
                                distance,
                                isMetric,
                                true,
                            )
                        }
                if (address.contains(",")) {
                    tvNavigationName.text = address.split(",")[0]
                } else if (address.contains(".")) {
                    tvNavigationName.text = address.split(".")[0]
                } else {
                    tvNavigationName.text = address
                }
                when (type) {
                    TYPE_TURN -> {
                        data?.routeTurnStepDetails?.let { routeTurnStepDetails ->
                            if (routeTurnStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_left_black)
                            } else if (routeTurnStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_right_black)
                            }
                        }
                    }

                    TYPE_ARRIVE -> {
                        ivDirection.setImageResource(R.drawable.ic_arrive_black)
                    }

                    TYPE_CONTINUE -> {
                        ivDirection.setImageResource(R.drawable.ic_continue_black)
                    }

                    TYPE_CONTINUE_HIGHWAY -> {
                        ivDirection.setImageResource(R.drawable.ic_continue_black)
                    }

                    TYPE_DEPART -> {
                        ivDirection.setImageResource(R.drawable.ic_arrive_black)
                    }

                    TYPE_ENTER_HIGHWAY -> {
                        data?.routeEnterHighwayStepDetails?.let { highwayStepDetails ->
                            if (highwayStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_ramp_left_black)
                            } else if (highwayStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_ramp_right_black)
                            }
                        }
                    }

                    TYPE_EXIT -> {
                        data?.routeExitStepDetails?.let { exitStepDetails ->
                            if (exitStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_exit_left_black)
                            } else if (exitStepDetails.steeringDirection?.value.equals(
                                    TURN_RIGHT,
                                    true,
                                )
                            ) {
                                ivDirection.setImageResource(R.drawable.ic_exit_right_black)
                            }
                        }
                    }

                    TYPE_KEEP -> {
                        ivDirection.setImageResource(R.drawable.ic_continue_black)
                    }

                    TYPE_RAMP -> {
                        data?.routeRampStepDetails?.let { rampStepDetails ->
                            if (rampStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_ramp_left_black)
                            } else if (rampStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_ramp_right_black)
                            }
                        }
                    }

                    TYPE_ROUNDABOUT_ENTER -> {
                        ivDirection.setImageResource(R.drawable.ic_roundabout_enter_black)
                    }

                    TYPE_ROUNDABOUT_EXIT -> {
                        data?.routeRoundaboutExitStepDetails?.let { exitStepDetails ->
                            if (exitStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_roundabout_exit_left_black)
                            } else if (exitStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_roundabout_exit_right_black)
                            }
                        }
                    }

                    TYPE_ROUNDABOUT_PASS -> {
                        data?.routeRoundaboutPassStepDetails?.let { passStepDetails ->
                            if (passStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_roundabout_pass_left_black)
                            } else if (passStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_roundabout_pass_right_black)
                            }
                        }
                    }

                    TYPE_U_TURN -> {
                        data?.routeUTurnStepDetails?.let { uTurnStepDetails ->
                            if (uTurnStepDetails.steeringDirection?.value.equals(TURN_LEFT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_uturn_left_black)
                            } else if (uTurnStepDetails.steeringDirection?.value.equals(TURN_RIGHT, true)) {
                                ivDirection.setImageResource(R.drawable.ic_uturn_right_black)
                            }
                        }
                    }

                    TYPE_SDK_UNKNOWN -> {
                        ivDirection.setImageResource(R.drawable.ic_sdk_unkown_black)
                    }

                    else -> {
                        ivDirection.setImageResource(R.drawable.ic_continue_black)
                    }
                }
                hideViews(cardDirection, cardMap, cardGeofenceMap)
            }
        }
    }

    private fun addPlaceDataInList(
        it: SearchSuggestionResponse,
        searchPlaceIndexText: SearchApiEnum,
    ) {
        val mText =
            mBinding.bottomSheetSearch.edtSearchPlaces.text
                .toString()
        if (validateLatLng(mText.trim()) != null) {
            val mLatLng = validateLatLng(mText.trim())
            if (it.text == (mLatLng?.latitude.toString() + "," + mLatLng?.longitude.toString())) {
                setPlaceData(it, searchPlaceIndexText)
            }
        } else if (!it.text.isNullOrEmpty()) {
            setPlaceData(it, searchPlaceIndexText)
        }
        mBinding.apply {
            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                bottomSheetSearch.apply {
                    clSearchLoaderSearchSheet.root.hide()
                }
            } else {
                bottomSheetDirectionSearch.apply {
                    clSearchLoaderDirectionSearch.root.hide()
                    rvSearchPlacesDirection.show()
                }
            }
        }
    }

    private fun setPlaceData(
        it: SearchSuggestionResponse,
        searchPlaceIndexText: SearchApiEnum,
    ) {
        mPlaceList.clear()
        mPlaceList.addAll(it.data)
        if (mPlaceList.isNotEmpty()) {
            activity?.hideKeyboard()
            mBottomSheetHelper.halfExpandBottomSheet()
        }
        mBaseActivity?.bottomNavigationVisibility(false)
        mMapHelper.addMultipleMarker(
            requireActivity(),
            MarkerEnum.NONE,
            it.data,
            mMarkerClickInterface,
        )
        showNoPlaceFoundUI(searchPlaceIndexText)
    }

    private fun addPlaceDirectionDataInList(
        it: SearchSuggestionResponse,
        searchPlaceIndexText: SearchApiEnum,
    ) {
        var mText: String =
            if (!mViewModel.isDataSearchForDestination) {
                mBinding.bottomSheetDirectionSearch.edtSearchDirection.text
                    .toString()
                    .trim()
            } else {
                mBinding.bottomSheetDirectionSearch.edtSearchDest.text
                    .toString()
                    .trim()
            }
        if (validateLatLng(mText) != null) {
            val mLatLng = validateLatLng(mText)
            mText = mLatLng?.latitude.toString() + "," + mLatLng?.longitude.toString()
        }
        if (!it.text.isNullOrEmpty() &&
            it.text
                .toString()
                .trim()
                .equals(mText, true)
        ) {
            mBinding.apply {
                bottomSheetDirectionSearch.apply {
                    clSearchLoaderDirectionSearch.root.hide()
                    clDriveLoader.hide()
                    clWalkLoader.hide()
                    clTruckLoader.hide()
                    clScooterLoader.hide()
                    clMapOptionRoute.show()
                    when (searchPlaceIndexText) {
                        SearchApiEnum.SEARCH_PLACE_INDEX_TEXT -> {
                            rvSearchPlacesDirection.show()
                            rvSearchPlacesSuggestionDirection.hide()
                        }

                        SearchApiEnum.SEARCH_PLACE_SUGGESTION -> {
                            rvSearchPlacesSuggestionDirection.show()
                            rvSearchPlacesDirection.hide()
                        }
                    }
                }
            }
            mPlaceList.clear()
            mPlaceList.addAll(it.data)
            if (mPlaceList.isNotEmpty()) {
                activity?.hideKeyboard()
            }
            showNoPlaceFoundDirectionUI(searchPlaceIndexText)
        }
    }

    // get marker click
    private val mMarkerClickInterface =
        object : MarkerClickInterface {
            override fun markerClick(placeData: String) {
                mPlaceList.forEach {
                    if (placeData == it.amazonLocationAddress?.label) {
                        setDirectionData(it, false)
                        return
                    }
                }
            }
        }

    // Based on list user able to see UI on screen
    private fun showNoPlaceFoundDirectionUI(searchPlaceIndexText: SearchApiEnum) {
        mBinding.bottomSheetDirectionSearch.apply {
            if (mPlaceList.isNotEmpty()) {
                clNoInternetConnectionDirectionSearch.hide()
                clMyLocationParent.show()
                layoutNoDataFound.root.hide()
                layoutCardError.groupCardErrorNoSearchFound.hide()
                when (searchPlaceIndexText) {
                    SearchApiEnum.SEARCH_PLACE_INDEX_TEXT -> {
                        rvSearchPlacesDirection.show()
                        rvSearchPlacesSuggestionDirection.hide()
                    }

                    SearchApiEnum.SEARCH_PLACE_SUGGESTION -> {
                        rvSearchPlacesSuggestionDirection.show()
                        rvSearchPlacesDirection.hide()
                    }
                }
            } else {
                hideViews(rvSearchPlacesDirection, clMyLocationParent)
                layoutNoDataFound.root.show()
            }
        }
    }

    // Based on list user able to see UI on screen
    private fun showNoPlaceFoundUI(searchPlaceIndexText: SearchApiEnum) {
        mBinding.bottomSheetSearch.apply {
            clNoInternetConnectionSearchSheet.hide()
            if (mPlaceList.isNotEmpty()) {
                nsSearchPlaces.show()
                layoutNoDataFound.groupNoSearchFound.hide()
                when (searchPlaceIndexText) {
                    SearchApiEnum.SEARCH_PLACE_INDEX_TEXT -> {
                        rvSearchPlaces.show()
                        rvSearchPlacesSuggestion.hide()
                    }

                    SearchApiEnum.SEARCH_PLACE_SUGGESTION -> {
                        rvSearchPlacesSuggestion.show()
                        rvSearchPlaces.hide()
                    }
                }
            } else {
                hideViews(rvSearchPlaces, nsSearchPlaces)
                layoutNoDataFound.groupNoSearchFound.show()
            }
        }
    }

    // search places using text
    private fun searchPlaces(searchText: String) {
        clearSearchList()
        mViewModel.searchPlaceSuggestion(
            searchText,
        )
    }

    // check permission granted or not
    fun checkPermissionFromWelcome() {
        checkPermission()
    }

    // check permission granted or not
    private fun checkPermission() {
        if (activity?.checkLocationPermission() == true) {
            checkGpsLocationProvider(
                true,
                isCurrentLocationClicked = false,
                false,
            )
        } else {
            permissionReqLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val permissionReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            when {
                it -> {
                    checkGpsLocationProvider(
                        false,
                        isCurrentLocationClicked = false,
                        false,
                    )
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) -> {
                    mBaseActivity?.updateLocationPermission(false)
                }

                else -> {
                    mBaseActivity?.updateLocationPermission(true)
                }
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    @OptIn(FlowPreview::class)
    private fun clickListener() {
        mBinding.apply {
            cardGeofenceMap.setOnClickListener {
                lifecycleScope.launch {
                    if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                        bottomSheetDirectionSearch.ivDirectionCloseDirectionSearch.performClick()
                    }
                    if (mBottomSheetHelper.isDirectionSheetVisible()) {
                        bottomSheetDirection.ivDirectionCloseDirection.performClick()
                    }
                    (activity as MainActivity).geofenceClick()
                    delay(DELAY_FOR_BOTTOM_SHEET_LOAD)
                    mBottomSheetHelper.hideSearchBottomSheet(true)
                }
            }

            cardNavigation.setOnClickListener {
                checkLocationPermission(true)
            }

            cardMap.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mViewModel.mLastClickTime < CLICK_TIME_DIFFERENCE) {
                    return@setOnClickListener
                }
                mViewModel.mLastClickTime = SystemClock.elapsedRealtime()
                activity?.supportFragmentManager.let {
                    if (it != null) {
                        mapStyleBottomSheetFragment?.show(
                            it,
                            MapStyleBottomSheetFragment::class.java.name,
                        )
                    }
                }
                if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                    mBottomSheetHelper.collapseDirectionSearch()
                }
                mBottomSheetHelper.hideSearchBottomSheet(true)
                mBottomSheetHelper.directionSheetDraggable(false)
                activity?.hideKeyboard()
                mBaseActivity?.bottomNavigationVisibility(false)
                when {
                    mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true -> {
                        mBaseActivity?.mSimulationUtils?.setSimulationDraggable(false)
                    }

                    mBaseActivity?.mTrackingUtils?.isTrackingExpandedOrHalfExpand() == true -> {
                        mBaseActivity?.mTrackingUtils?.collapseTracking()
                    }

                    mBaseActivity?.mGeofenceUtils?.isGeofenceListExpandedOrHalfExpand() == true -> {
                        mBaseActivity?.mGeofenceUtils?.collapseGeofenceList()
                    }
                }
            }

            cardExit.setOnClickListener {
                requireContext().simulationExit(
                    object : SimulationDialogInterface {
                        override fun onExitClick(dialog: DialogInterface) {
                            cardSimulationPopup.hide()
                            (activity as MainActivity).hideSimulationSheet()
                        }
                    },
                )
            }
            bottomSheetNavigationComplete.ivNavigationCompleteClose.setOnClickListener {
                if (checkInternetConnection()) {
                    hideArrivedBottomSheet()
                }
            }

            bottomSheetNavigationComplete.btnNavigationComplete.setOnClickListener {
                if (checkInternetConnection()) {
                    hideArrivedBottomSheet()
                    mBaseActivity?.bottomNavigationVisibility(true)
                    mBottomSheetHelper.hideSearchBottomSheet(false)
                }
            }

            bottomSheetNavigation.cardNavigationLocation.setOnClickListener {
                if (checkInternetConnection()) {
                    checkLocationPermission(true)
                }
            }

            cardDirection.setOnClickListener {
                (activity as MainActivity).mTrackingUtils?.hideTrackingBottomSheet()
                clearAllMapData()
                if (checkInternetConnection()) {
                    openDirectionBottomSheet()
                }
            }

            bottomSheetSearch.apply {
                tvSearchCancel?.setOnClickListener {
                    mBottomSheetHelper.collapseSearchBottomSheet()
                }
            }
            bottomSheetSearch.edtSearchPlaces
                .textChanges()
                .debounce(CLICK_DEBOUNCE)
                .onEach { text ->
                    updateSearchUI(text.isNullOrEmpty())
                    if (mViewModel.mIsPlaceSuggestion) {
                        if (!text.isNullOrEmpty()) {
                            searchPlaces(text.toString())
                            val properties =
                                listOf(
                                    Pair(AnalyticsAttribute.VALUE, text.toString()),
                                    Pair(
                                        AnalyticsAttribute.TYPE,
                                        if (validateLatLng(text.toString()) !=
                                            null
                                        ) {
                                            AnalyticsAttributeValue.COORDINATES
                                        } else {
                                            AnalyticsAttributeValue.TEXT
                                        },
                                    ),
                                    Pair(AnalyticsAttribute.TRIGGERED_BY, PLACE_SEARCH),
                                    Pair(
                                        AnalyticsAttribute.ACTION,
                                        AnalyticsAttributeValue.AUTOCOMPLETE,
                                    ),
                                )
                            (activity as MainActivity).analyticsUtils?.recordEvent(
                                PLACE_SEARCH,
                                properties,
                            )
                        }
                    }
                }.launchIn(lifecycleScope)

            bottomSheetNavigation.apply {
                btnExit.setOnClickListener {
                    navigationExit()
                }
            }

            bottomSheetDirectionSearch.apply {
                clDrive.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mCarData == null) {
                            showError(getString(R.string.no_route_found))
                            return@setOnClickListener
                        }
                        mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
                        setCarClickData()
                    }
                }

                clWalk.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mWalkingData == null) {
                            showError(getString(R.string.no_route_found))
                            return@setOnClickListener
                        }
                        mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
                        mViewModel.mTravelMode = RouteTravelMode.Pedestrian.value
                        mViewModel.mWalkingData?.let {
                            setSelectedMode()
                            if (mViewModel.mIsRouteOptionsOpened) {
                                mViewModel.mIsRouteOptionsOpened = false
                                changeRouteListUI()
                            }
                            adjustMapBound()
                            drawPolyLineOnMapCardClick(
                                it.routes[0].legs,
                                isLineUpdate = false,
                                isWalk = true,
                                isLocationIcon = false,
                            )
                        }
                    }
                }

                clTruck.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mTruckData == null) {
                            showError(getString(R.string.no_route_found))
                            return@setOnClickListener
                        }
                        mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
                        mViewModel.mTravelMode = RouteTravelMode.Truck.value
                        mViewModel.mTruckData?.let {
                            setSelectedMode()
                            adjustMapBound()
                            drawPolyLineOnMapCardClick(
                                it.routes[0].legs,
                                isLineUpdate = false,
                                isWalk = false,
                                isLocationIcon = false,
                            )
                        }
                    }
                }

                clScooter.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mScooterData == null) {
                            showError(getString(R.string.no_route_found))
                            return@setOnClickListener
                        }
                        mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
                        mViewModel.mTravelMode = RouteTravelMode.Scooter.value
                        if (mViewModel.mIsRouteOptionsOpened) {
                            mViewModel.mIsRouteOptionsOpened = false
                            changeRouteListUI()
                        }
                        mViewModel.mScooterData?.let {
                            setSelectedMode()
                            adjustMapBound()
                            drawPolyLineOnMapCardClick(
                                it.routes[0].legs,
                                isLineUpdate = false,
                                isWalk = false,
                                isLocationIcon = false,
                            )
                        }
                    }
                }

                cardDriveGo.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mCarData
                                ?.routes
                                ?.get(0)
                                ?.legs != null
                        ) {
                            mViewModel.mTravelMode = RouteTravelMode.Car.value
                            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                                mRedirectionType = RedirectionType.ROUTE_OPTION.name
                                checkLocationPermission(false)
                            } else {
                                checkRouteData()
                            }
                        } else {
                            checkRouteValidation()
                        }
                    }
                }

                cardWalkGo.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mWalkingData
                                ?.routes
                                ?.get(0)
                                ?.legs != null
                        ) {
                            mViewModel.mTravelMode = RouteTravelMode.Pedestrian.value
                            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                                mRedirectionType = RedirectionType.ROUTE_OPTION.name
                                checkLocationPermission(false)
                            } else {
                                checkRouteData()
                            }
                        } else {
                            checkRouteValidation()
                        }
                    }
                }

                cardTruckGo.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mTruckData
                                ?.routes
                                ?.get(0)
                                ?.legs != null
                        ) {
                            mViewModel.mTravelMode = RouteTravelMode.Truck.value
                            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                                mRedirectionType = RedirectionType.ROUTE_OPTION.name
                                checkLocationPermission(false)
                            } else {
                                checkRouteData()
                            }
                        } else {
                            checkRouteValidation()
                        }
                    }
                }

                cardScooterGo.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mScooterData
                                ?.routes
                                ?.get(0)
                                ?.legs != null
                        ) {
                            mViewModel.mTravelMode = RouteTravelMode.Scooter.value
                            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                                mRedirectionType = RedirectionType.ROUTE_OPTION.name
                                checkLocationPermission(false)
                            } else {
                                checkRouteData()
                            }
                        } else {
                            checkRouteValidation()
                        }
                    }
                }

                ivSwapLocation.setOnClickListener {
                    if (checkInternetConnection() && !mViewModel.mIsSwapClicked && !checkDirectionLoaderVisible()) {
                        mViewModel.mIsSwapClicked = true
                        mViewModel.mLastClickTime = SystemClock.elapsedRealtime()
                        if (!edtSearchDest.text.isNullOrEmpty() && !edtSearchDirection.text.isNullOrEmpty()) {
                            showDirectionSearchShimmer()
                        }
                        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                            mMapHelper.removeMarkerAndLine()
                            clearDirectionData()
                            edtSearchDirection.setText(edtSearchDest.text.toString().trim())
                            edtSearchDest.setText(resources.getString(R.string.label_my_location))
                            clMyLocation.root.hide()
                            enableDirectionSearch()
                            mViewModel.mSearchDirectionOriginData =
                                mViewModel.mSearchDirectionDestinationData
                            mViewModel.mSearchDirectionOriginData?.isDestination = false
                            mViewModel.mSearchDirectionDestinationData = null
                            mViewModel.mSearchDirectionOriginData?.let { it1 ->
                                showCurrentLocationOriginRoute(
                                    it1,
                                )
                            }
                        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                            mMapHelper.removeMarkerAndLine()
                            clearDirectionData()
                            edtSearchDest.setText(edtSearchDirection.text.toString().trim())
                            edtSearchDirection.setText(resources.getString(R.string.label_my_location))
                            clMyLocation.root.hide()
                            enableDirectionSearch()
                            mViewModel.mSearchDirectionDestinationData =
                                mViewModel.mSearchDirectionOriginData
                            mViewModel.mSearchDirectionDestinationData?.isDestination = true
                            mViewModel.mSearchDirectionOriginData = null
                            mViewModel.mSearchDirectionDestinationData?.let { it1 ->
                                showCurrentLocationDestinationRoute(
                                    it1,
                                )
                            }
                        } else {
                            mMapHelper.removeMarkerAndLine()
                            clearDirectionData()
                            val searchDest = edtSearchDest.text.toString().trim()
                            edtSearchDest.setText(edtSearchDirection.text.toString().trim())
                            edtSearchDirection.setText(searchDest)
                            enableDirectionSearch()
                            if (mViewModel.mSearchDirectionOriginData != null && mViewModel.mSearchDirectionDestinationData != null) {
                                val originData = mViewModel.mSearchDirectionOriginData
                                mViewModel.mSearchDirectionOriginData =
                                    mViewModel.mSearchDirectionDestinationData
                                mViewModel.mSearchDirectionOriginData?.isDestination = false
                                mViewModel.mSearchDirectionDestinationData = originData
                                mViewModel.mSearchDirectionDestinationData?.isDestination = true
                                showOriginToDestinationRoute()
                            } else if (mViewModel.mSearchDirectionOriginData != null) {
                                mViewModel.mSearchDirectionDestinationData =
                                    mViewModel.mSearchDirectionOriginData
                                mViewModel.mSearchDirectionOriginData = null
                            } else if (mViewModel.mSearchDirectionDestinationData != null) {
                                mViewModel.mSearchDirectionOriginData =
                                    mViewModel.mSearchDirectionDestinationData
                                mViewModel.mSearchDirectionDestinationData = null
                            }
                        }
                        activity?.hideKeyboard()
                        lifecycleScope.launch {
                            delay(DELAY_500)
                            mViewModel.mIsSwapClicked = false
                        }
                    }
                }
                switchAvoidTools.setOnCheckedChangeListener { _, isChecked ->
                    if (checkInternetConnection() && mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                        mMapHelper.removeMarkerAndLine()
                        clearDirectionData()
                        mViewModel.mIsAvoidTolls = isChecked
                        checkedSwitchCount()
                        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionDestinationData?.let {
                                showCurrentLocationDestinationRoute(it)
                            }
                        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionOriginData?.let {
                                showCurrentLocationOriginRoute(it)
                            }
                        } else if (!edtSearchDirection.text.isNullOrEmpty() &&
                            !edtSearchDest.text.isNullOrEmpty()
                        ) {
                            showOriginToDestinationRoute()
                        }
                    }
                }

                switchAvoidFerries.setOnCheckedChangeListener { _, isChecked ->
                    if (checkInternetConnection() && mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                        mMapHelper.removeMarkerAndLine()
                        clearDirectionData()
                        mViewModel.mIsAvoidFerries = isChecked
                        checkedSwitchCount()
                        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionDestinationData?.let {
                                showCurrentLocationDestinationRoute(it)
                            }
                        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionOriginData?.let {
                                showCurrentLocationOriginRoute(it)
                            }
                        } else if (!edtSearchDirection.text.isNullOrEmpty() &&
                            !edtSearchDest.text.isNullOrEmpty()
                        ) {
                            showOriginToDestinationRoute()
                        }
                    }
                }

                switchAvoidDirtRoads.setOnCheckedChangeListener { _, isChecked ->
                    if (checkInternetConnection() && mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                        mMapHelper.removeMarkerAndLine()
                        clearDirectionData()
                        mViewModel.mIsAvoidDirtRoads = isChecked
                        checkedSwitchCount()
                        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionDestinationData?.let {
                                showCurrentLocationDestinationRoute(it)
                            }
                        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionOriginData?.let {
                                showCurrentLocationOriginRoute(it)
                            }
                        } else if (!edtSearchDirection.text.isNullOrEmpty() &&
                            !edtSearchDest.text.isNullOrEmpty()
                        ) {
                            showOriginToDestinationRoute()
                        }
                    }
                }

                switchAvoidUTurns.setOnCheckedChangeListener { _, isChecked ->
                    if (checkInternetConnection() && mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                        mMapHelper.removeMarkerAndLine()
                        clearDirectionData()
                        mViewModel.mIsAvoidUTurns = isChecked
                        checkedSwitchCount()
                        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionDestinationData?.let {
                                showCurrentLocationDestinationRoute(it)
                            }
                        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionOriginData?.let {
                                showCurrentLocationOriginRoute(it)
                            }
                        } else if (!edtSearchDirection.text.isNullOrEmpty() &&
                            !edtSearchDest.text.isNullOrEmpty()
                        ) {
                            showOriginToDestinationRoute()
                        }
                    }
                }

                switchAvoidTunnels.setOnCheckedChangeListener { _, isChecked ->
                    if (checkInternetConnection() && mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                        mMapHelper.removeMarkerAndLine()
                        clearDirectionData()
                        mViewModel.mIsAvoidTunnels = isChecked
                        checkedSwitchCount()
                        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionDestinationData?.let {
                                showCurrentLocationDestinationRoute(it)
                            }
                        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionOriginData?.let {
                                showCurrentLocationOriginRoute(it)
                            }
                        } else if (!edtSearchDirection.text.isNullOrEmpty() &&
                            !edtSearchDest.text.isNullOrEmpty()
                        ) {
                            showOriginToDestinationRoute()
                        }
                    }
                }
                edtSearchDest.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        mViewModel.isDataSearchForDestination = true
                    }
                }
                edtSearchDirection.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        mViewModel.isDataSearchForDestination = false
                    }
                }
                edtSearchDest
                    .textChanges()
                    .debounce(DELAY_500)
                    .onEach { text ->
                        updateDirectionSearchUI(text.isNullOrEmpty())
                        if (text
                                ?.trim()
                                .toString()
                                .lowercase() ==
                            getString(R.string.label_my_location)
                                .trim()
                                .lowercase()
                        ) {
                            return@onEach
                        }
                        if (text.isNullOrEmpty()) {
                            mViewModel.mSearchDirectionDestinationData = null
                            hideViews(
                                cardRouteDepartOptions,
                                cardMapOption,
                                cardListRoutesOption,
                                layoutCardError.root,
                                clDriveLoader,
                                clWalkLoader,
                                clTruckLoader,
                                clScooterLoader,
                            )
                            return@onEach
                        }
                        if (mBottomSheetHelper.isDirectionSearchSheetVisible() &&
                            !mViewModel.mIsDirectionDataSet &&
                            !mViewModel.mIsSwapClicked &&
                            mViewModel.mIsPlaceSuggestion
                        ) {
                            cardRouteOptionHide()
                            clearMapLineMarker()
                            mViewModel.mSearchDirectionDestinationData = null
                            searchPlaces(text.toString())
                            val properties =
                                listOf(
                                    Pair(AnalyticsAttribute.VALUE, text.toString()),
                                    Pair(
                                        AnalyticsAttribute.TYPE,
                                        if (validateLatLng(text.toString()) !=
                                            null
                                        ) {
                                            AnalyticsAttributeValue.COORDINATES
                                        } else {
                                            AnalyticsAttributeValue.TEXT
                                        },
                                    ),
                                    Pair(
                                        AnalyticsAttribute.TRIGGERED_BY,
                                        AnalyticsAttributeValue.ROUTE_MODULE,
                                    ),
                                    Pair(
                                        AnalyticsAttribute.ACTION,
                                        AnalyticsAttributeValue.TO_SEARCH_AUTOCOMPLETE,
                                    ),
                                )
                            (activity as MainActivity).analyticsUtils?.recordEvent(
                                PLACE_SEARCH,
                                properties,
                            )
                        }
                        checkMyLocationUI(text, edtSearchDirection)
                    }.launchIn(lifecycleScope)

                edtSearchDirection
                    .textChanges()
                    .debounce(DELAY_500)
                    .onEach { text ->
                        updateDirectionSearchUI(text.isNullOrEmpty())
                        if (text
                                ?.trim()
                                .toString()
                                .lowercase() ==
                            getString(R.string.label_my_location)
                                .trim()
                                .lowercase()
                        ) {
                            return@onEach
                        }
                        if (text.isNullOrEmpty()) {
                            mViewModel.mSearchDirectionOriginData = null
                            hideViews(
                                cardRouteDepartOptions,
                                cardMapOption,
                                cardListRoutesOption,
                                layoutCardError.root,
                                clDriveLoader,
                                clWalkLoader,
                                clTruckLoader,
                                clScooterLoader,
                            )
                            return@onEach
                        }
                        if (mBottomSheetHelper.isDirectionSearchSheetVisible() &&
                            !mViewModel.mIsDirectionDataSetNew &&
                            !mViewModel.mIsSwapClicked &&
                            !mViewModel.mIsDirectionDataSet &&
                            mViewModel.mIsPlaceSuggestion
                        ) {
                            cardRouteOptionHide()
                            clearMapLineMarker()
                            mViewModel.mSearchDirectionOriginData = null
                            searchPlaces(text.toString())
                            val properties =
                                listOf(
                                    Pair(AnalyticsAttribute.VALUE, text.toString()),
                                    Pair(
                                        AnalyticsAttribute.TYPE,
                                        if (validateLatLng(text.toString()) !=
                                            null
                                        ) {
                                            AnalyticsAttributeValue.COORDINATES
                                        } else {
                                            AnalyticsAttributeValue.TEXT
                                        },
                                    ),
                                    Pair(
                                        AnalyticsAttribute.TRIGGERED_BY,
                                        AnalyticsAttributeValue.ROUTE_MODULE,
                                    ),
                                    Pair(
                                        AnalyticsAttribute.ACTION,
                                        AnalyticsAttributeValue.FROM_SEARCH_AUTOCOMPLETE,
                                    ),
                                )
                            (activity as MainActivity).analyticsUtils?.recordEvent(
                                PLACE_SEARCH,
                                properties,
                            )
                        }
                        checkMyLocationUI(text, edtSearchDest)
                    }.launchIn(lifecycleScope)

                clMyLocation.root.setOnClickListener {
                    if (checkInternetConnection()) {
                        mRedirectionType = RedirectionType.MY_LOCATION.name
                        checkLocationPermission(false)
                    }
                }
                cardRoutingOption.setOnClickListener {
                    if (checkInternetConnection()) {
                        mBottomSheetHelper.expandDirectionSearchSheet(this@ExploreFragment)
                        mViewModel.mIsRouteOptionsOpened = !mViewModel.mIsRouteOptionsOpened
                        changeRouteListUI()
                    }
                }
                cardDepartOptions.setOnClickListener {
                    if (checkInternetConnection()) {
                        mBottomSheetHelper.expandDirectionSearchSheet(this@ExploreFragment)
                        mViewModel.mIsDepartOptionsOpened = !mViewModel.mIsDepartOptionsOpened
                        changeDepartListUI()
                    }
                }
                clLeaveNow.setOnClickListener {
                    calendar = Calendar.getInstance()
                    setCurrentDateAndTime()
                    mViewModel.mSelectedDepartOption = DepartOption.LEAVE_NOW.name
                    setDepartOptionSelected(clLeaveNow, tvLeaveNow)
                    tvDepartOptions.text = getString(R.string.label_leave_now)
                    timeDepart = ""
                    disableCalendar()
                    calculateRouteAllMode()
                }
                clLeaveAt.setOnClickListener {
                    setCurrentDateAndTime()
                    calendar?.let {
                        val displayDate = formatToDisplayDate(it.time)
                        val isoDate = formatToISO8601(it.time)
                        tvPickedTime.text = displayDate.split(" ")[1]
                        timeDepart = isoDate
                        tvDepartOptions.text = buildString {
                            append(getString(R.string.label_leave_at))
                            append(" ")
                            append(displayDate)
                        }
                    }
                    mViewModel.mSelectedDepartOption = DepartOption.DEPART_TIME.name
                    setDepartOptionSelected(clLeaveAt, tvLeaveAt)
                    enableCalendar()
                    calculateRouteAllMode()
                }
                clArriveBy.setOnClickListener {
                    setCurrentDateAndTime()
                    calendar?.let {
                        val displayDate = formatToDisplayDate(it.time)
                        val isoDate = formatToISO8601(it.time)
                        tvPickedTime.text = displayDate.split(" ")[1]
                        timeDepart = isoDate
                        tvDepartOptions.text = buildString {
                            append(getString(R.string.label_arrive_by))
                            append(" ")
                            append(displayDate)
                        }
                    }
                    mViewModel.mSelectedDepartOption = DepartOption.ARRIVE_TIME.name
                    setDepartOptionSelected(clArriveBy, tvArriveBy)
                    enableCalendar()
                    calculateRouteAllMode()
                }
                cardTimePick.setOnClickListener {
                    showTimePicker(
                        onDateTimeSelected = { isoDate, displayDate ->
                            if (mViewModel.mSelectedDepartOption == DepartOption.DEPART_TIME.name) {
                                tvDepartOptions.text = buildString {
                                    append(getString(R.string.label_leave_at))
                                    append(" ")
                                    append(displayDate)
                                }
                            } else if (mViewModel.mSelectedDepartOption == DepartOption.ARRIVE_TIME.name) {
                                tvDepartOptions.text = buildString {
                                    append(getString(R.string.label_arrive_by))
                                    append(" ")
                                    append(displayDate)
                                }
                            }
                            tvPickedTime.text = displayDate.split(" ")[1]
                            timeDepart = isoDate
                            calculateRouteAllMode()
                        },
                        onCancel = {
                        },
                    )
                }
                calDepart.setOnDateChangeListener { _, year, month, dayOfMonth ->
                    calendar?.set(Calendar.YEAR, year)
                    calendar?.set(Calendar.MONTH, month)
                    calendar?.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    if (mViewModel.mSelectedDepartOption == DepartOption.DEPART_TIME.name) {
                        setCurrentDateAndTime()
                        calendar?.let {
                            val displayDate = formatToDisplayDate(it.time)
                            val isoDate = formatToISO8601(it.time)
                            tvPickedTime.text = displayDate.split(" ")[1]
                            timeDepart = isoDate
                            tvDepartOptions.text = buildString {
                                append(getString(R.string.label_leave_at))
                                append(" ")
                                append(displayDate)
                            }
                        }
                        calculateRouteAllMode()
                    } else if (mViewModel.mSelectedDepartOption == DepartOption.ARRIVE_TIME.name) {
                        setCurrentDateAndTime()
                        calendar?.let {
                            val displayDate = formatToDisplayDate(it.time)
                            val isoDate = formatToISO8601(it.time)
                            tvPickedTime.text = displayDate.split(" ")[1]
                            timeDepart = isoDate
                            tvDepartOptions.text = buildString {
                                append(getString(R.string.label_leave_at))
                                append(" ")
                                append(displayDate)
                            }
                        }
                        calculateRouteAllMode()
                    }
                }
                bottomSheetSearch.ivAmazonInfoSearchSheet.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetDirectionSearch.ivAmazonInfoDirectionSearchSheet.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetDirection.ivAmazonInfoDirection?.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetNavigation.ivAmazonInfoNavigation.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetNavigationComplete.ivAmazonInfoNavigationComplete.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetGeofenceList.ivAmazonInfoGeofenceList?.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetAddGeofence.ivAmazonInfoAddGeofence?.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetTracking.ivAmazonInfoTrackingSheet?.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetTrackSimulation.ivAmazonInfoTrackingSheet.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                mBinding.bottomSheetAttribution.apply {
                    btnLearnMoreSa.setOnClickListener {
                        startActivity(
                            Intent(
                                context,
                                WebViewActivity::class.java,
                            ).putExtra(
                                KEY_URL,
                                BuildConfig.BASE_DOMAIN + BuildConfig.AWS_SOFTWARE_ATTRIBUTION_URL,
                            ),
                        )
                    }
                    btnLearnMore.setOnClickListener {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(BuildConfig.ATTRIBUTION_LEARN_MORE_HERE_URL),
                            ),
                        )
                    }
                    ivBack.setOnClickListener {
                        hideAttribution()
                    }
                }
            }

            bottomSheetSearch.ivClose.setOnClickListener {
                bottomSheetSearch.edtSearchPlaces.setText("")
                mMapHelper.clearMarker()
                clearSearchList()
            }

            bottomSheetSearch.edtSearchPlaces.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    mBottomSheetHelper.expandSearchBottomSheet()
                }
            }

            bottomSheetSearch.edtSearchPlaces.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    notifyAdapters()
                    mViewModel.searchPlaceIndexForText(bottomSheetSearch.edtSearchPlaces.text.toString())
                    true
                } else {
                    false
                }
            }

            bottomSheetDirectionSearch.edtSearchDest.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (checkInternetConnection() &&
                        bottomSheetDirectionSearch.edtSearchDest.text
                            .toString()
                            .trim()
                            .isNotEmpty()
                    ) {
                        mBinding.bottomSheetDirectionSearch.apply {
                            clNoInternetConnectionDirectionSearch.hide()
                        }
                        notifyAdapters()
                        mViewModel.searchPlaceIndexForText(bottomSheetDirectionSearch.edtSearchDest.text.toString())
                    }
                    true
                } else {
                    false
                }
            }

            bottomSheetDirection.ivDirectionCloseDirection.setOnClickListener {
                clearDirectionBottomSheet()
            }

            bottomSheetDirectionSearch.ivDirectionCloseDirectionSearch.setOnClickListener {
                lifecycleScope.launch {
                    bottomSheetDirectionSearch.apply {
                        setCurrentDateAndTime()
                        mViewModel.mSelectedDepartOption = DepartOption.LEAVE_NOW.name
                        setDepartOptionSelected(clLeaveNow, tvLeaveNow)
                        tvDepartOptions.text = getString(R.string.label_leave_now)
                        timeDepart = ""
                        disableCalendar()
                    }
                    activity?.hideKeyboard()
                    delay(DELAY_300)
                    mBinding.bottomSheetSearch.clSearchLoaderSearchSheet.root
                        .hide()
                    mMapHelper.addLiveLocationMarker(false)
                    mBottomSheetHelper.hideDirectionSearchBottomSheet(this@ExploreFragment)
                    hideDirectionBottomSheet()
                }
            }

            mBinding.bottomSheetDirection.apply {
                btnDirection.setOnClickListener {
                    if (!checkInternetConnection()) return@setOnClickListener
                    if (cardLoaderSheet1.isVisible) return@setOnClickListener
                    if (activity?.checkLocationPermission() != true) {
                        mViewModel.mCarData = null
                        openDirectionWithError()
                        return@setOnClickListener
                    }
                    if (!isGPSEnabled(requireContext())) {
                        mViewModel.mCarData = null
                        openDirectionWithError()
                        return@setOnClickListener
                    }
                    if (mViewModel.mCarData
                            ?.routes
                            ?.get(0)
                            ?.legs == null
                    ) {
                        openDirectionWithError()
                    } else {
                        routeOption()
                    }
                }
                ivArrow.setOnClickListener {
                    tvScheduleDetails.visibility =
                        if (tvScheduleDetails.isVisible) View.GONE else View.VISIBLE
                    if (ivArrow.rotation == 0F) ivArrow.rotation = 180F else ivArrow.rotation = 0F
                }
                ivInfo.setOnClickListener {
                    if (tvDirectionError2.isVisible) {
                        if (tvDirectionError2.text.equals(getString(R.string.label_location_permission_denied))) {
                            activity?.locationPermissionDialog()
                        }
                    }
                }
                ivCopyAddress.setOnClickListener {
                    copyTextToClipboard(
                        requireContext(),
                        sheetDirectionTvDirectionStreet.text.toString(),
                    )
                    showError(getString(R.string.label_copied_to_clipboard))
                }
            }

            bottomSheetAddGeofence.edtAddGeofenceSearch
                .textChanges()
                .debounce(CLICK_DEBOUNCE)
                .onEach { text ->
                    if (mBaseActivity?.mGeofenceUtils?.isAddGeofenceBottomSheetVisible() == true) {
                        if (text
                                .toString()
                                .isEmpty()
                        ) {
                            mMapLibreMap?.removeOnMapClickListener(this@ExploreFragment)
                            mBaseActivity?.mGeofenceUtils?.showAddGeofenceDefaultUI()
                        }
                        if (bottomSheetAddGeofence.edtAddGeofenceSearch.hasFocus()) {
                            val dataToSearch = text.toString().replace(", ", ",")
                            if (dataToSearch.isNotEmpty()) {
                                mGeofenceViewModel.geofenceSearchPlaceSuggestion(
                                    dataToSearch,
                                    mViewModel.mLatLng,
                                )
                            }
                        }
                    }
                }.launchIn(lifecycleScope)

            bottomSheetAddGeofence.edtAddGeofenceSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    notifyAdapters()
                    mGeofenceViewModel.geofenceSearchPlaceIndexForText(
                        bottomSheetAddGeofence.edtAddGeofenceSearch.text.toString(),
                        mViewModel.mLatLng,
                    )
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun BottomSheetDirectionSearchBinding.setCurrentDateAndTime() {
        if (calendar == null) {
            calendar = Calendar.getInstance()
        }
        calendar?.let {
            tvPickedTime.text = String.format(Locale.getDefault(),STRING_FORMAT_TIME, it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
            calDepart.setDate(it.timeInMillis, true, true)
        }
    }

    private fun BottomSheetDirectionSearchBinding.setDepartOptionSelected(card: MaterialCardView, textView: AppCompatTextView) {
        tvLeaveAt.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        clLeaveAt.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
        tvLeaveNow.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        clLeaveNow.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
        tvArriveBy.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        clArriveBy.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun BottomSheetDirectionSearchBinding.disableCalendar() {
        hideViews(calDepart, tvTime, cardTimePick)
    }

    private fun BottomSheetDirectionSearchBinding.enableCalendar() {
        showViews(calDepart, tvTime, cardTimePick)
    }

    private fun BottomSheetDirectionSearchBinding.calculateRouteAllMode() {
        if (checkInternetConnection()) {
            mMapHelper.removeMarkerAndLine()
            clearDirectionData()
            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                mViewModel.mSearchDirectionDestinationData?.let {
                    showCurrentLocationDestinationRoute(it)
                }
            } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                mViewModel.mSearchDirectionOriginData?.let {
                    showCurrentLocationOriginRoute(it)
                }
            } else if (!edtSearchDirection.text.isNullOrEmpty() &&
                !edtSearchDest.text.isNullOrEmpty()
            ) {
                showOriginToDestinationRoute()
            }
        }
    }

    fun hideAttribution() {
        mBottomSheetHelper.hideAttributeSheet()
        if (mViewModel.isFromMapStyle) {
            mViewModel.isFromMapStyle = false
            mBinding.cardMap.performClick()
        }
    }

    private fun BottomSheetDirectionSearchBinding.setCarClickData() {
        mViewModel.mTravelMode = RouteTravelMode.Car.value
        mViewModel.mCarData?.let {
            setSelectedMode()
            adjustMapBound()
            drawPolyLineOnMapCardClick(
                it.routes[0].legs,
                isLineUpdate = false,
                isWalk = false,
                isLocationIcon = false,
            )
        }
        recordTravelModeChange()
    }

    private fun BottomSheetDirectionSearchBinding.setSelectedMode() {
        hideViews(tvDriveSelected, tvTruckSelected, tvWalkSelected, tvScooterSelected)
        tvDriveMinute.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_medium_black
            )
        )
        ivCar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_medium_black))
        tvWalkMinute.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_medium_black
            )
        )
        ivWalk.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_medium_black))
        tvTruckMinute.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_medium_black
            )
        )
        ivTruck.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_medium_black))
        tvScooterMinute.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_medium_black
            )
        )
        ivScooter.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_medium_black
            )
        )
        when (mViewModel.mTravelMode) {
            RouteTravelMode.Car.value -> {
                tvDriveSelected.show()
                tvDriveMinute.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_primary_green
                    )
                )
                ivCar.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_primary_green
                    )
                )
            }

            RouteTravelMode.Pedestrian.value -> {
                tvWalkSelected.show()
                tvWalkMinute.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_primary_green
                    )
                )
                ivWalk.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_primary_green
                    )
                )
            }

            RouteTravelMode.Truck.value -> {
                tvTruckSelected.show()
                tvTruckMinute.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_primary_green
                    )
                )
                ivTruck.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_primary_green
                    )
                )
            }

            RouteTravelMode.Scooter.value -> {
                tvScooterSelected.show()
                tvScooterMinute.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_primary_green
                    )
                )
                ivScooter.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_primary_green
                    )
                )
            }
        }
    }

    private fun recordTravelModeChange() {
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
            val properties =
                listOf(
                    Pair(AnalyticsAttribute.TRAVEL_MODE, mViewModel.mTravelMode),
                    Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
                    Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
                )
            (activity as MainActivity).analyticsUtils?.recordEvent(
                ROUTE_OPTION_CHANGED,
                properties,
            )
        }
    }

    fun showSimulationTop() {
        mBinding.apply {
            cardSimulationPopup.show()
            val insideOutAnimation: Animation =
                ScaleAnimation(
                    0.0f,
                    1.0f, // Initial and final scaleX value
                    0.0f,
                    1.0f, // Initial and final scaleY value
                    Animation.RELATIVE_TO_SELF,
                    0.5f, // Pivot X (0.5 means the middle of the view)
                    Animation.RELATIVE_TO_SELF,
                    0.5f, // Pivot Y (0.5 means the middle of the view)
                ).apply {
                    duration = 3000
                    repeatMode = Animation.ABSOLUTE
                    repeatCount = Animation.INFINITE
                }
            ivOvalExternal.startAnimation(insideOutAnimation)
        }
    }

    fun checkMapLoaded(): Boolean = !mBinding.groupMapLoad.isVisible

    fun isMapStyleExpandedOrHalfExpand(): Boolean = mapStyleBottomSheetFragment?.isMapStyleExpandedOrHalfExpand() ?: false

    fun hideMapStyleSheet() {
        mapStyleBottomSheetFragment?.hideMapStyleSheet()
    }

    fun setAttributionDataAndExpandSheet() {
        setAttributionData()
        mBottomSheetHelper.expandAttributeSheet()
    }

    private fun setAttributionData() {
        mBinding.apply {
            bottomSheetAttribution.apply {
                tvAttribution.text =
                    mPreferenceManager
                        .getValue(MAP_STYLE_ATTRIBUTION, "")
                        ?.replace(Regex(attributionPattern), "") ?: ""
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun openDirectionWithError() {
        getUpdatedAvoidOptionValue()
        mBinding.bottomSheetDirectionSearch.apply {
            clearDirectionData()
            switchAvoidTools.isChecked = mViewModel.mIsAvoidTolls
            switchAvoidFerries.isChecked = mViewModel.mIsAvoidFerries
            switchAvoidDirtRoads.isChecked = mViewModel.mIsAvoidDirtRoads
            switchAvoidUTurns.isChecked = mViewModel.mIsAvoidUTurns
            switchAvoidTunnels.isChecked = mViewModel.mIsAvoidTunnels
            checkedSwitchCount()
            tvDriveGo.text = getString(R.string.btn_go)
            mViewModel.mIsDirectionDataSet = true
            if (mViewModel.mCarData
                    ?.routes
                    ?.get(0)
                    ?.legs == null
            ) {
                edtSearchDest.setText(
                    mBinding.bottomSheetDirection.tvDirectionAddress.text
                        .trim(),
                )
            }
            lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_ENABLE)
                mViewModel.mIsDirectionDataSet = false
            }
            hideViews(
                rvSearchPlacesSuggestionDirection,
                rvSearchPlacesDirection,
                clMyLocation.root,
                clSearchLoaderDirectionSearch.root,
                clDriveLoader,
                cardRouteDepartOptions,
                cardMapOption,
                clTruckLoader,
                clScooterLoader,
                clWalkLoader,
                clDrive,
            )
            clTruck.invisible()
            clScooter.invisible()
            clWalk.invisible()
            mPlaceList.clear()
            mAdapterDirection?.notifyDataSetChanged()
            mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
        }
        openDirectionSearch()
    }

    private fun checkRouteValidation() {
        checkErrorDistance()
    }

    private fun checkErrorDirectionDistance() {
        if (activity?.checkLocationPermission() == true) {
            if (isGPSEnabled(requireContext())) {
                var destinationLatLng: LatLng? = null

                val position =  mViewModel.mSearchSuggestionData?.position

                position?.let {
                    destinationLatLng =
                        LatLng(
                            it[1],
                            it[0],
                        )
                }
                val distance =
                    destinationLatLng?.latitude?.let { it1 ->
                        destinationLatLng?.longitude?.let { it2 ->
                            mPlacesProvider.getDistance(
                                mMapHelper.getBestAvailableLocation(),
                                it1,
                                it2,
                            )
                        }
                    }
                if (distance != null) {
                    mBinding.bottomSheetDirection.tvDirectionError.show()
                    hideViews(
                        mBinding.bottomSheetDirection.tvDirectionError2,
                        mBinding.bottomSheetDirection.ivInfo,
                    )
                    mBinding.bottomSheetDirection.tvDirectionError.text =
                        getString(R.string.error_route)
                } else {
                    mBinding.bottomSheetDirection.tvDirectionError.text =
                        getString(R.string.error_route)
                }
            } else {
                showLocationError()
                mBinding.bottomSheetDirection.tvDirectionError2.text =
                    getString(R.string.label_location_permission_denied)
            }
        } else {
            showLocationError()
            mBinding.bottomSheetDirection.tvDirectionError2.text =
                getString(R.string.label_location_permission_denied)
        }
    }

    private fun showLocationError() {
        mBinding.bottomSheetDirection.tvDirectionError.hide()
        showViews(
            mBinding.bottomSheetDirection.tvDirectionError2,
            mBinding.bottomSheetDirection.ivInfo,
        )
    }

    private fun checkErrorDistance() {
        showError(getString(R.string.no_route_found))
    }

    fun navigationExit() {
        mBinding.apply {
            bottomSheetNavigation.apply {
                if (checkInternetConnection()) {
                    showViews(
                        cardDirection,
                        cardNavigation,
                        cardMap,
                        cardGeofenceMap,
                    )
                    clearNavigationExitData()
                }
            }
            bottomSheetDirectionSearch.apply {
                nsvDirection.smoothScrollTo(0, 0)
            }
        }
    }

    private fun checkInternetConnection(): Boolean =
        if (context?.isInternetAvailable() == true) {
            true
        } else {
            showError(getString(R.string.check_your_internet_connection_and_try_again))
            false
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentExploreBinding.openDirectionBottomSheet() {
        notifyAdapters()
        getUpdatedAvoidOptionValue()
        cardDirection.hide()
        bottomSheetDirectionSearch.clSearchLoaderDirectionSearch.root.hide()
        bottomSheetDirectionSearch.layoutNoDataFound.root.hide()
        bottomSheetSearch.edtSearchPlaces.setText("")
        bottomSheetSearch.edtSearchPlaces.clearFocus()
        mBaseActivity?.bottomNavigationVisibility(false)
        mBottomSheetHelper.hideSearchBottomSheet(true)
        mBottomSheetHelper.hideDirectionSheet()
        bottomSheetDirectionSearch.apply {
            calendar = Calendar.getInstance()
            calendar?.let {
                calDepart.minDate = it.timeInMillis
                mViewModel.mSelectedDepartOption = DepartOption.LEAVE_NOW.name
                tvPickedTime.text = String.format(Locale.getDefault(), STRING_FORMAT_TIME, it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
            }
            setDepartOptionSelected(clLeaveNow, tvLeaveNow)
            disableCalendar()
            tvDepartOptions.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_primary_green
                )
            )
            mViewModel.mIsDirectionDataSet = true
            edtSearchDest.setText("")
            lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_ENABLE)
                mViewModel.mIsDirectionDataSet = false
            }
            hideViews(
                cardListRoutesOption,
                cardRouteDepartOptions,
                cardMapOption,
            )
            if (activity?.checkLocationPermission() == true) {
                if (isGPSEnabled(requireContext())) {
                    edtSearchDirection.setText(getString(R.string.label_my_location))
                    clMyLocation.root.hide()
                } else {
                    clMyLocation.root.show()
                }
            } else {
                clMyLocation.root.show()
            }
            switchAvoidTools.isChecked = mViewModel.mIsAvoidTolls
            switchAvoidFerries.isChecked = mViewModel.mIsAvoidFerries
            switchAvoidDirtRoads.isChecked = mViewModel.mIsAvoidDirtRoads
            switchAvoidUTurns.isChecked = mViewModel.mIsAvoidUTurns
            switchAvoidTunnels.isChecked = mViewModel.mIsAvoidTunnels
            checkedSwitchCount()
            mPlaceList.clear()
            mAdapterDirection?.notifyDataSetChanged()
            mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
            if (!edtSearchDest.hasFocus()) {
                edtSearchDirection.requestFocus()
            }
            mBaseActivity?.showKeyboard()
        }
        mBottomSheetHelper.expandDirectionSearchSheet(this@ExploreFragment)
        mViewModel.mIsDirectionSheetHalfExpanded = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun routeOption() {
        getUpdatedAvoidOptionValue()
        if (mViewModel.mCarData
                ?.routes
                ?.get(0)
                ?.legs != null
        ) {
            mViewModel.mIsDirectionDataSetNew = true
            mViewModel.mCarData?.routes?.get(0)?.legs?.let { legs ->
                adjustMapBound()
                drawPolyLineOnMap(
                    legs,
                    isLineUpdate = false,
                    isWalk = false,
                    isLocationIcon = false,
                )
                mBinding.bottomSheetDirectionSearch.apply {
                    clearTruckAndWalkData()
                    calendar = Calendar.getInstance()
                    calendar?.let {
                        calDepart.minDate = it.timeInMillis
                        mViewModel.mSelectedDepartOption = DepartOption.LEAVE_NOW.name
                        tvPickedTime.text = String.format(Locale.getDefault(), STRING_FORMAT_TIME, it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
                    }
                    setDepartOptionSelected(clLeaveNow, tvLeaveNow)
                    disableCalendar()
                    tvDepartOptions.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_primary_green
                        )
                    )
                    tvDriveGo.text = getString(R.string.btn_go)
                    switchAvoidTools.isChecked = mViewModel.mIsAvoidTolls
                    switchAvoidFerries.isChecked = mViewModel.mIsAvoidFerries
                    switchAvoidDirtRoads.isChecked = mViewModel.mIsAvoidDirtRoads
                    switchAvoidUTurns.isChecked = mViewModel.mIsAvoidUTurns
                    switchAvoidTunnels.isChecked = mViewModel.mIsAvoidTunnels
                    checkedSwitchCount()
                    edtSearchDirection.setText(getString(R.string.label_my_location))
                    showViews(
                        cardRouteDepartOptions,
                        cardMapOption,
                        clTruckLoader,
                        clScooterLoader,
                        clWalkLoader,
                        clDrive,
                        viewDividerOptionTruck,
                        viewDividerOptionScooter,
                    )
                    hideViews(
                        rvSearchPlacesSuggestionDirection,
                        rvSearchPlacesDirection,
                        clMyLocation.root,
                        clSearchLoaderDirectionSearch.root,
                        clDriveLoader,
                    )
                    clTruck.invisible()
                    clWalk.invisible()
                    clScooter.invisible()
                    layoutCardError.groupCardErrorNoSearchFound.hide()
                    layoutCardError.root.hide()
                    mPlaceList.clear()
                    mAdapterDirection?.notifyDataSetChanged()
                    mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
                }
                openDirectionSearch()

                val mLatLng = mMapHelper.getBestAvailableLocation()
                mViewModel.isCalculateWalkApiError = false
                mViewModel.isCalculateTruckApiError = false
                mViewModel.isCalculateScooterApiError = false
                val position =  mViewModel.mSearchSuggestionData?.position

                mViewModel.calculateDistance(
                    latitude = mLatLng.latitude,
                    longitude = mLatLng.longitude,
                    latDestination =
                        position?.get(1),
                    lngDestination =
                        position?.get(0),
                    avoidanceOptions = mViewModel.getAvoidanceOptions(),
                    departOption = mViewModel.mSelectedDepartOption,
                    time = timeDepart,
                    isWalkingAndTruckCall = true,
                )
                recordEventForAllMode(isWalkingAndTruckCall = false)
            }
            lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_ENABLE)
                mViewModel.mIsDirectionDataSetNew = false
            }
        } else {
            showError(getString(R.string.no_route_found))
        }
    }

    private fun getUpdatedAvoidOptionValue() {
        mViewModel.mIsAvoidTolls = mPreferenceManager.getValue(KEY_AVOID_TOLLS, false)
        mViewModel.mIsAvoidFerries = mPreferenceManager.getValue(KEY_AVOID_FERRIES, false)
        mViewModel.mIsAvoidDirtRoads = mPreferenceManager.getValue(KEY_AVOID_DIRT_ROADS, false)
        mViewModel.mIsAvoidUTurns = mPreferenceManager.getValue(KEY_AVOID_U_TURNS, false)
        mViewModel.mIsAvoidTunnels = mPreferenceManager.getValue(KEY_AVOID_TUNNELS, false)
    }

    private fun BottomSheetDirectionSearchBinding.checkedSwitchCount() {
        var checkedCount = 0
        if (mViewModel.mIsAvoidTolls) {
            checkedCount++
        }
        if (mViewModel.mIsAvoidFerries) {
            checkedCount++
        }
        if (mViewModel.mIsAvoidDirtRoads) {
            checkedCount++
        }
        if (mViewModel.mIsAvoidUTurns) {
            checkedCount++
        }
        if (mViewModel.mIsAvoidTunnels) {
            checkedCount++
        }
        if (checkedCount > 0) {
            tvRoutingOption.text =
                buildString {
                    append(checkedCount)
                    append(" ")
                    append(getString(R.string.text_switch_options))
                }
            tvRoutingOption.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_primary_green
                )
            )
            tvRoutingOption.typeface = ResourcesCompat.getFont(requireContext(), R.font.amazon_ember_bold)
        } else {
            tvRoutingOption.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_medium_black
                )
            )
            tvRoutingOption.text = getString(R.string.label_route_options)
            tvRoutingOption.typeface = ResourcesCompat.getFont(requireContext(), R.font.amazon_ember_medium)
        }
    }

    private fun recordEventForAllMode(isWalkingAndTruckCall: Boolean) {
        val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
        val propertiesCar =
            listOf(
                Pair(AnalyticsAttribute.TRAVEL_MODE, RouteTravelMode.Car.value),
                Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
                Pair(AnalyticsAttribute.AVOID_FERRIES, mViewModel.mIsAvoidFerries.toString()),
                Pair(AnalyticsAttribute.AVOID_TOLLS, mViewModel.mIsAvoidTolls.toString()),
                Pair(AnalyticsAttribute.AVOID_DIRT_ROADS, mViewModel.mIsAvoidDirtRoads.toString()),
                Pair(AnalyticsAttribute.AVOID_U_TURNS, mViewModel.mIsAvoidUTurns.toString()),
                Pair(AnalyticsAttribute.AVOID_TUNNELS, mViewModel.mIsAvoidTunnels.toString()),
            )
        val propertiesTruck =
            listOf(
                Pair(AnalyticsAttribute.TRAVEL_MODE, RouteTravelMode.Truck.value),
                Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
                Pair(AnalyticsAttribute.AVOID_FERRIES, mViewModel.mIsAvoidFerries.toString()),
                Pair(AnalyticsAttribute.AVOID_TOLLS, mViewModel.mIsAvoidTolls.toString()),
                Pair(AnalyticsAttribute.AVOID_DIRT_ROADS, mViewModel.mIsAvoidDirtRoads.toString()),
                Pair(AnalyticsAttribute.AVOID_U_TURNS, mViewModel.mIsAvoidUTurns.toString()),
                Pair(AnalyticsAttribute.AVOID_TUNNELS, mViewModel.mIsAvoidTunnels.toString()),
            )
        val propertiesWalk =
            listOf(
                Pair(AnalyticsAttribute.TRAVEL_MODE, RouteTravelMode.Pedestrian.value),
                Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
                Pair(AnalyticsAttribute.AVOID_FERRIES, mViewModel.mIsAvoidFerries.toString()),
                Pair(AnalyticsAttribute.AVOID_TOLLS, mViewModel.mIsAvoidTolls.toString()),
                Pair(AnalyticsAttribute.AVOID_DIRT_ROADS, mViewModel.mIsAvoidDirtRoads.toString()),
                Pair(AnalyticsAttribute.AVOID_U_TURNS, mViewModel.mIsAvoidUTurns.toString()),
                Pair(AnalyticsAttribute.AVOID_TUNNELS, mViewModel.mIsAvoidTunnels.toString()),
            )
        if (isWalkingAndTruckCall) {
            (activity as MainActivity).analyticsUtils?.recordEvent(ROUTE_SEARCH, propertiesTruck)
            (activity as MainActivity).analyticsUtils?.recordEvent(ROUTE_SEARCH, propertiesWalk)
        } else {
            (activity as MainActivity).analyticsUtils?.recordEvent(ROUTE_SEARCH, propertiesCar)
        }
    }

    private fun openDirectionSearch() {
        mBaseActivity?.bottomNavigationVisibility(false)
        mBottomSheetHelper.hideDirectionSheet()
        mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
        mViewModel.mIsDirectionSheetHalfExpanded = true
        mBaseActivity?.isTablet?.let {
            if (it) {
                mBinding.cardDirection.hide()
                mBinding.cardNavigation.show()
            }
        }
    }

    private fun checkLocationPermission(isCurrentLocationClicked: Boolean) {
        if (activity?.checkLocationPermission() != true) {
            if (mBaseActivity?.getLocationPermissionCount() == 2) {
                mBaseActivity?.showLocationPermissionDialogBox()
            } else {
                checkPermission()
            }
        } else {
            checkGpsLocationProvider(
                false,
                isCurrentLocationClicked = true,
                isCurrentLocationClicked,
            )
        }
    }

    private fun checkRouteData() {
        mBinding.bottomSheetDirectionSearch.apply {
            val mData =
                when (mViewModel.mTravelMode) {
                    RouteTravelMode.Car.value -> mViewModel.mCarData
                    RouteTravelMode.Pedestrian.value -> mViewModel.mWalkingData
                    RouteTravelMode.Truck.value -> mViewModel.mTruckData
                    RouteTravelMode.Scooter.value -> mViewModel.mScooterData
                    else -> mViewModel.mCarData
                }
            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                mData?.let {
                    drawPolyLineOnMap(
                        it.routes[0].legs,
                        isLineUpdate = false,
                        isWalk = mViewModel.mTravelMode == RouteTravelMode.Pedestrian.value,
                        isLocationIcon = true,
                    )
                    mMapHelper.navigationZoomCamera(mMapHelper.getBestAvailableLocation(), mViewModel.isZooming)
                    mMapHelper.clearOriginMarker()
                    mViewModel.isLocationUpdatedNeeded = true
                    fetchAddressFromLatLng(it)
                }
            } else {
                mViewModel.isLocationUpdatedNeeded = false
                val position = mViewModel.mSearchDirectionOriginData?.position
                position?.let {
                    LatLng(
                        it[1],
                        it[0],
                    ).let { it2 -> mMapHelper.navigationZoomCamera(it2, mViewModel.isZooming) }
                }
                mData?.let {
                    drawPolyLineOnMapCardClick(
                        it.routes[0].legs,
                        isLineUpdate = false,
                        isWalk = mViewModel.mTravelMode == RouteTravelMode.Pedestrian.value,
                        isLocationIcon = true,
                    )
                    fetchAddressFromLatLng(it)
                }
            }
        }
    }

    private fun directionMyLocation() {
        mBinding.bottomSheetDirectionSearch.apply {
            mViewModel.mIsDirectionDataSet = true
            if (!mViewModel.isDataSearchForDestination) {
                edtSearchDirection.setText(getString(R.string.label_my_location))
                edtSearchDirection.text?.length?.let { it1 -> edtSearchDirection.setSelection(it1) }
            } else {
                edtSearchDest.setText(getString(R.string.label_my_location))
                edtSearchDest.text?.length?.let { it1 -> edtSearchDest.setSelection(it1) }
            }
            requireActivity().hideKeyboard()
            clMyLocation.root.hide()
            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                if (edtSearchDest.text
                        .toString()
                        .trim()
                        .isNotEmpty() &&
                    mViewModel.mSearchDirectionDestinationData != null
                ) {
                    mViewModel.mSearchDirectionDestinationData?.let { it1 ->
                        showCurrentLocationDestinationRoute(
                            it1,
                        )
                    }
                } else {
                    hideViews(rvSearchPlacesDirection, rvSearchPlacesSuggestionDirection)
                }
            } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                if (edtSearchDirection.text
                        .toString()
                        .trim()
                        .isNotEmpty() &&
                    mViewModel.mSearchDirectionOriginData != null
                ) {
                    mViewModel.mSearchDirectionOriginData?.let { it1 ->
                        showCurrentLocationOriginRoute(
                            it1,
                        )
                    }
                } else {
                    hideViews(rvSearchPlacesDirection, rvSearchPlacesSuggestionDirection)
                }
            }
            enableDirectionSearch()
        }
    }

    private fun checkMyLocationUI(
        text: CharSequence?,
        edtSearch: TextInputEditText,
    ) {
        if (!text.isNullOrEmpty() && text.toString() == getString(R.string.label_my_location)) {
            mBinding.bottomSheetDirectionSearch.clMyLocation.root
                .hide()
        } else if (!edtSearch.text.isNullOrEmpty() &&
            edtSearch.text.toString() ==
            getString(
                R.string.label_my_location,
            )
        ) {
            mBinding.bottomSheetDirectionSearch.clMyLocation.root
                .hide()
        } else {
            if (mBinding.bottomSheetDirectionSearch.cardMapOption.visibility == View.GONE) {
                if (!mBinding.cardNavigation.isEnabled) {
                    mBinding.bottomSheetDirectionSearch.clMyLocation.root
                        .hide()
                } else {
                    if (!mBinding.bottomSheetDirectionSearch.layoutCardError.root.isVisible) {
                        mBinding.bottomSheetDirectionSearch.clMyLocation.root
                            .show()
                    }
                }
            }
        }
    }

    private fun FragmentExploreBinding.hideArrivedBottomSheet() {
        bottomSheetNavigationComplete.tvNavigationCompleteAddress.text = ""
        mBottomSheetHelper.hideNavigationCompleteSheet()
        clearNavigationData()
    }

    private fun adjustMapBound() {
        val latLngList: ArrayList<LatLng> = ArrayList()
        if (mViewModel.mSearchDirectionDestinationData?.isDestination == true) {
            mViewModel.mSearchDirectionDestinationData
                ?.position
                ?.get(1)
                ?.let {
                    mViewModel.mSearchDirectionDestinationData
                        ?.position
                        ?.get(0)
                        ?.let { it1 ->
                            LatLng(
                                it,
                                it1,
                            )
                        }
                }?.let {
                    latLngList.add(
                        it,
                    )
                }
        } else if (mViewModel.mSearchDirectionOriginData?.isDestination == true) {
            val position = mViewModel.mSearchDirectionOriginData?.position
            position
                ?.let {
                    LatLng(
                        it[1],
                        it[0],
                    )
                }?.let {
                    latLngList.add(
                        it,
                    )
                }
        }

        if (mViewModel.mSearchDirectionOriginData != null) {
            val position = mViewModel.mSearchDirectionOriginData?.position
            position
                ?.let {
                    LatLng(
                        it[1],
                        it[0],
                    )
                }?.let {
                    latLngList.add(
                        it,
                    )
                }
        }
        if(latLngList.size < 2) {
            latLngList.add(mMapHelper.getBestAvailableLocation())
        }
        mMapHelper.adjustMapBounds(
            latLngList,
            resources.getDimension(R.dimen.dp_90).roundToInt(),
        )
    }

    private fun clearNavigationData() {
        clearNavigationSheetData()
        mViewModel.mStartLatLng = null
        mViewModel.mDestinationLatLng = null
        mViewModel.mNavigationResponse = null
        hideDirectionBottomSheet()
    }

    private fun clearNavigationExitData() {
        clearNavigationSheetData()
        mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
        showDirectionAndCurrentLocationIcon()
        adjustMapBound()
        mBaseActivity?.isTablet?.let {
            if (it) {
                mBinding.cardDirection.hide()
                mBinding.cardNavigation.show()
            }
        }
        lifecycleScope.launch {
            delay(CLICK_DEBOUNCE_ENABLE)
            if (mBinding.bottomSheetDirectionSearch.clDriveLoader.isVisible &&
                mViewModel.mCarData != null &&
                mViewModel.mCarCalculateDistanceResponse != null
            ) {
                mBinding.bottomSheetDirectionSearch.clWalkLoader.hide()
                mBinding.bottomSheetDirectionSearch.clWalk.show()
                setCarRouteData(mViewModel.mCarCalculateDistanceResponse!!)
            }
            if (mBinding.bottomSheetDirectionSearch.clWalkLoader.isVisible &&
                mViewModel.mWalkingData != null &&
                mViewModel.mWalkCalculateDistanceResponse != null
            ) {
                mBinding.bottomSheetDirectionSearch.clDriveLoader.hide()
                mBinding.bottomSheetDirectionSearch.clDrive.show()
                setWalkingRouteData(mViewModel.mWalkCalculateDistanceResponse!!)
            }
            if (mBinding.bottomSheetDirectionSearch.clTruckLoader.isVisible &&
                mViewModel.mTruckData != null &&
                mViewModel.mTruckCalculateDistanceResponse != null
            ) {
                mBinding.bottomSheetDirectionSearch.clTruckLoader.hide()
                mBinding.bottomSheetDirectionSearch.clTruck.show()
                setTruckRouteData(mViewModel.mTruckCalculateDistanceResponse!!)
            }
            if (mBinding.bottomSheetDirectionSearch.clScooterLoader.isVisible &&
                mViewModel.mScooterData != null &&
                mViewModel.mScooterCalculateDistanceResponse != null
            ) {
                mBinding.bottomSheetDirectionSearch.clScooterLoader.hide()
                mBinding.bottomSheetDirectionSearch.clScooter.show()
                setScooterRouteData(mViewModel.mScooterCalculateDistanceResponse!!)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearNavigationSheetData() {
        mBinding.bottomSheetNavigation.apply {
            tvNavigationDistance.text = ""
            tvNavigationTime.text = ""
            tvArrivalTime.text = ""
        }
        mMapHelper.addLiveLocationMarker(false)
        mBinding.tvDistance.text = ""
        mBinding.tvNavigationName.text = ""
        mBinding.bottomSheetDirectionSearch.apply {
            setCarClickData()
        }
        mViewModel.mRouteFinish = true
        mNavigationList.clear()
        mMapHelper.removeLocationListener()
        mMapLibreMap?.removeOnScaleListener(this)
        mBinding.cardNavigationTimeDialog.hide()
        mNavigationAdapter?.notifyDataSetChanged()
        mBottomSheetHelper.hideNavigationSheet()
    }

    private fun drawPolyLineOnMapCardClick(
        legs: List<RouteLeg>,
        isLineUpdate: Boolean,
        isWalk: Boolean = false,
        isLocationIcon: Boolean,
        sourceLatLng: LatLng? = null,
        destinationLatLng: LatLng? = null,
    ) {
        val lineString = arrayListOf<Point>()

        val dotStartPoint = arrayListOf<Point>()
        val dotDestinationPoint = arrayListOf<Point>()

        mBinding.apply {
            bottomSheetDirectionSearch.apply {
                if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                    setMapLineData(
                        sourceLatLng,
                        dotStartPoint,
                        legs,
                        destinationLatLng,
                        dotDestinationPoint,
                        false,
                    )
                } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                    setMapLineData(
                        sourceLatLng,
                        dotStartPoint,
                        legs,
                        destinationLatLng,
                        dotDestinationPoint,
                        true,
                    )
                } else if (!edtSearchDirection.text.isNullOrEmpty() && !edtSearchDest.text.isNullOrEmpty()) {
                    setMapLineData(
                        sourceLatLng,
                        dotStartPoint,
                        legs,
                        destinationLatLng,
                        dotDestinationPoint,
                        false,
                    )
                }
            }
        }
        for (leg in legs) {
            leg.geometry?.lineString?.let {
                for (data in it) {
                    lineString.add(fromLngLat(data[0], data[1]))
                }
            }
        }
        if (isLineUpdate) {
            mMapHelper.updateLine(lineString)
            mMapHelper.removeStartDot()
        } else {
            mMapHelper.addLine(lineString, isWalk)
            mBinding.apply {
                bottomSheetDirectionSearch.apply {
                    if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                        mMapHelper.addStartDot(dotStartPoint)
                    } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                        mMapHelper.addStartDot(dotStartPoint)
                    }
                }
            }
            mMapHelper.addDotDestination(dotDestinationPoint)
        }
        mBinding.apply {
            bottomSheetDirectionSearch.apply {
                if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                    mMapHelper.addLiveLocationMarker(isLocationIcon)
                }
            }
        }
    }

    private fun setMapLineData(
        sourceLatLng: LatLng?,
        dotStartPoint: ArrayList<Point>,
        legs: List<RouteLeg>,
        destinationLatLng: LatLng?,
        dotDestinationPoint: ArrayList<Point>,
        isDestination: Boolean,
    ) {
        if (sourceLatLng != null) {
            dotStartPoint.add(
                fromLngLat(
                    sourceLatLng.longitude,
                    sourceLatLng.latitude,
                ),
            )
        } else {
            if (isDestination) {
                mViewModel.mDestinationLatLng
                    ?.longitude
                    ?.let {
                        mViewModel.mDestinationLatLng?.latitude?.let { it1 ->
                            fromLngLat(
                                it,
                                it1,
                            )
                        }
                    }?.let {
                        dotStartPoint.add(
                            it,
                        )
                    }
            } else {
                val mLatLng = mMapHelper.getBestAvailableLocation()
                fromLngLat(
                    mLatLng.longitude, mLatLng.latitude,
                )?.let {
                    dotStartPoint.add(
                        it,
                    )
                }
            }
        }
        if (isDestination) {
            legs.last().geometry?.lineString?.let {
                val lastLeg = it[it.size - 1]
                dotStartPoint.add(fromLngLat(lastLeg[0], lastLeg[1]))
            }
        } else {
            legs.first().geometry?.lineString?.let {
                dotStartPoint.add(
                    fromLngLat(
                        it[0][0],
                        it[0][1],
                    ),
                )
            }
        }
        if (destinationLatLng != null) {
            dotDestinationPoint.add(
                fromLngLat(
                    destinationLatLng.longitude,
                    destinationLatLng.latitude,
                ),
            )
        } else {
            if (isDestination) {
                mViewModel.mStartLatLng
                    ?.longitude
                    ?.let {
                        mViewModel.mStartLatLng?.latitude?.let { it1 ->
                            fromLngLat(
                                it,
                                it1,
                            )
                        }
                    }?.let {
                        dotDestinationPoint.add(
                            it,
                        )
                    }
            } else {
                mViewModel.mDestinationLatLng
                    ?.latitude
                    ?.let {
                        mViewModel.mDestinationLatLng?.longitude?.let { it1 ->
                            fromLngLat(
                                it1,
                                it,
                            )
                        }
                    }?.let {
                        dotDestinationPoint.add(
                            it,
                        )
                    }
            }
        }
        if (isDestination) {
            legs.first().geometry?.lineString?.let {
                dotDestinationPoint.add(
                    fromLngLat(
                        it[0][0],
                        it[0][1],
                    ),
                )
            }
        } else {
            legs.last().geometry?.lineString?.let {
                val lastLeg = it[it.size - 1]
                dotDestinationPoint.add(fromLngLat(lastLeg[0], lastLeg[1]))
            }
        }
    }

    private fun drawPolyLineOnMap(
        legs: List<RouteLeg>,
        isLineUpdate: Boolean,
        isWalk: Boolean = false,
        isLocationIcon: Boolean,
        sourceLatLng: LatLng? = null,
        destinationLatLng: LatLng? = null,
    ) {
        val lineString = arrayListOf<Point>()

        val dotStartPoint = arrayListOf<Point>()
        val dotDestinationPoint = arrayListOf<Point>()

        setMapLineData(
            sourceLatLng,
            dotStartPoint,
            legs,
            destinationLatLng,
            dotDestinationPoint,
            false,
        )
        for (leg in legs) {
            for (data in leg.geometry?.lineString!!) {
                lineString.add(fromLngLat(data[0], data[1]))
            }
        }
        if (isLineUpdate) {
            mMapHelper.updateLine(lineString)
            mMapHelper.removeStartDot()
        } else {
            mMapHelper.addLine(lineString, isWalk)
            mMapHelper.addStartDot(dotStartPoint)
            mMapHelper.addDotDestination(dotDestinationPoint)
        }
        mMapHelper.addLiveLocationMarker(isLocationIcon)
    }

    private fun fetchAddressFromLatLng(it: CalculateRoutesResponse) {
        mViewModel.mRouteFinish = false
        activity?.hideKeyboard()
        mViewModel.calculateNavigationLine(requireContext(), it)
        mBottomSheetHelper.showNavigationSheet()
        mBottomSheetHelper.hideDirectionSearch(this@ExploreFragment)
        mBaseActivity?.isTablet?.let {
            if (it) {
                mBinding.bottomSheetNavigation.cardNavigationLocation.hide()
            }
        }
    }

    private fun BottomSheetDirectionSearchBinding.changeRouteListUI() {
        if (mViewModel.mIsRouteOptionsOpened) {
            departOptionClose()
            ivUp.show()
            ivDown.hide()
            cardListRoutesOption.show()
        } else {
            routeOptionClose()
        }
    }

    private fun BottomSheetDirectionSearchBinding.changeDepartListUI() {
        if (mViewModel.mIsDepartOptionsOpened) {
            routeOptionClose()
            ivUpDepartOptions.show()
            ivDownDepartOptions.hide()
            cardListDepartOptions.show()
        } else {
            departOptionClose()
        }
    }

    private fun BottomSheetDirectionSearchBinding.routeOptionClose() {
        mViewModel.mIsRouteOptionsOpened = false
        ivDown.show()
        hideViews(cardListRoutesOption, ivUp)
        cardMapOption.radius =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8f,
                requireContext().resources.displayMetrics,
            )
    }

    private fun BottomSheetDirectionSearchBinding.departOptionClose() {
        mViewModel.mIsDepartOptionsOpened = false
        ivDownDepartOptions.show()
        hideViews(cardListDepartOptions, ivUpDepartOptions)
        cardMapOption.radius =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8f,
                requireContext().resources.displayMetrics,
            )
    }

    private fun BottomSheetDirectionSearchBinding.clearDirectionData() {
        clearTruckAndWalkData()
        tvDriveDistance.text = ""
        tvDriveMinute.text = ""
    }

    private fun BottomSheetDirectionSearchBinding.clearTruckAndWalkData() {
        tvTruckMinute.text = ""
        tvTruckDistance.text = ""
        tvWalkMinute.text = ""
        tvWalkDistance.text = ""
        tvScooterMinute.text = ""
        tvScooterDistance.text = ""
    }

    private fun clearMapLineMarker() {
        mMapHelper.removeLine()
        mMapHelper.clearMarker()
    }

    fun hideDirectionBottomSheet() {
        mViewModel.mIsDirectionDataSet = true
        mViewModel.isDataSearchForDestination = false
        mBinding.bottomSheetDirectionSearch.edtSearchDirection.setText("")
        mBinding.bottomSheetDirectionSearch.edtSearchDest.setText("")
        clearDirectionBottomSheet()
        mViewModel.mIsDirectionDataSet = false
        mViewModel.mIsPlaceSuggestion = true
        mBinding.bottomSheetDirectionSearch.apply {
            routeOptionClose()
            departOptionClose()
        }
        activity?.hideKeyboard()
        lifecycleScope.launch {
            delay(CLICK_DEBOUNCE_ENABLE)
            mViewModel.mIsDirectionDataSet = false
        }
    }

    fun showDirectionAndCurrentLocationIcon() {
        mBinding.let {
            showViews(
                it.cardDirection,
                it.cardNavigation,
            )
        }
    }

    fun showCurrentLocationIcon() {
        mBinding.let {
            it.cardDirection.hide()
            it.cardNavigation.show()
        }
    }

    fun hideGeofence() {
        mBinding.cardGeofenceMap.hide()
        val defaultShapeAppearance =
            ShapeAppearanceModel
                .builder()
                .build()
        mBinding.cardMap.shapeAppearanceModel = defaultShapeAppearance
        mBinding.cardMap.radius = resources.getDimensionPixelSize(R.dimen.dp_8).toFloat()
    }

    fun showGeofence() {
        mBinding.cardGeofenceMap.show()
        mBinding.cardMap.radius = resources.getDimensionPixelSize(R.dimen.dp_0).toFloat()
        val shapeAppearanceModel =
            ShapeAppearanceModel
                .builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 16f)
                .setTopRightCorner(CornerFamily.ROUNDED, 16f)
                .build()
        mBinding.cardMap.shapeAppearanceModel = shapeAppearanceModel
    }

    fun hideDirectionAndCurrentLocationIcon() {
        hideViews(
            mBinding.cardDirection,
            mBinding.cardNavigation,
        )
    }

    // clear direction sheet with data
    private fun clearDirectionBottomSheet() {
        lifecycleScope.launch {
            mViewModel.mSearchDirectionOriginData = null
            mViewModel.mSearchDirectionDestinationData = null
            mViewModel.mCarData = null
            mViewModel.mWalkingData = null
            mViewModel.mTruckData = null
            mViewModel.mScooterData = null
            mViewModel.mTravelMode = RouteTravelMode.Car.value
            mBinding.bottomSheetDirectionSearch.apply {
                setSelectedMode()
                hideViews(
                    layoutCardError.root,
                    layoutNoDataFound.root,
                )
            }
            mBinding.bottomSheetDirection.apply {
                tvDirectionError.invisible()
                groupDistanceLoad.show()
            }
            hideDirectionData()
            showViews(
                mBinding.cardMap,
            )
            if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() != true &&
                mBaseActivity?.mTrackingUtils?.isChangeDataProviderClicked != true &&
                mBaseActivity?.mGeofenceUtils?.isChangeDataProviderClicked != true
            ) {
                mBinding.cardGeofenceMap.show()
                mBaseActivity?.isTablet?.let {
                    if (!it) {
                        mBinding.cardDirection.show()
                        mBinding.cardNavigation.show()
                    }
                }
            }
            clearMapLineMarker()
            clearSearchList()
            if (!mapStyleBottomSheetFragment?.isVisible!! &&
                (
                    mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() != true &&
                        mBaseActivity?.mTrackingUtils?.isChangeDataProviderClicked != true &&
                        mBaseActivity?.mGeofenceUtils?.isChangeDataProviderClicked != true
                )
            ) {
                mBaseActivity?.bottomNavigationVisibility(true)
                mBottomSheetHelper.hideSearchBottomSheet(false)
            }
            mBottomSheetHelper.hideDirectionSheet()
            mViewModel.mSearchSuggestionData = null
            mMapHelper.moveCameraToLocation(mMapHelper.getBestAvailableLocation())
            mBaseActivity?.isTablet?.let {
                if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true ||
                    mBaseActivity?.mTrackingUtils?.isChangeDataProviderClicked == true ||
                    mBaseActivity?.mGeofenceUtils?.isChangeDataProviderClicked == true
                ) {
                    return@let
                }
                if (it) {
                    mBinding.cardNavigation.show()
                    mBinding.cardDirection.show()
                }
            }
        }
    }

    // clear search list
    private fun clearSearchList() {
        mViewModel.mIsPlaceSuggestion = true
        mBinding.bottomSheetSearch.apply {
            layoutNoDataFound.groupNoSearchFound.hide()
            nsSearchPlaces.show()
        }
        notifyAdapters()
    }

    // update search ui
    private fun updateSearchUI(isSearchText: Boolean = false) {
        mBinding.bottomSheetSearch.apply {
            if (isSearchText) {
                clSearchLoaderSearchSheet.root.hide()
                clNoInternetConnectionSearchSheet.hide()
                ivClose.hide()
                clearSearchList()
            } else {
                ivClose.show()
            }
        }
    }

    // update search ui
    @SuppressLint("NotifyDataSetChanged")
    private fun updateDirectionSearchUI(isSearchText: Boolean = false) {
        mBinding.bottomSheetDirectionSearch.apply {
            if (isSearchText) {
                hideViews(
                    rvSearchPlacesDirection,
                    rvSearchPlacesSuggestionDirection,
                    clSearchLoaderDirectionSearch.root,
                )
                mPlaceList.clear()
                mAdapterDirection?.notifyDataSetChanged()
                mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
            }
        }
    }

    /**
     * Set search places adapter
     * Tap on item user navigate to direction sheet
     * */
    private fun setSearchPlaceDirectionAdapter() {
        mBinding.bottomSheetDirectionSearch.apply {
            rvSearchPlacesDirection.layoutManager = LinearLayoutManager(requireContext())
            mAdapterDirection =
                SearchPlacesAdapter(
                    mPlaceList,
                    mPreferenceManager,
                    true,
                    object : SearchPlacesAdapter.SearchPlaceInterface {
                        override fun placeClick(position: Int) {
                            if (checkInternetConnection()) {
                                mViewModel.mIsDirectionDataSet = true
                                setPlaceData(position)
                                notifyDirectionAdapters()
                            }
                        }
                    },
                )
            rvSearchPlacesDirection.adapter = mAdapterDirection
        }
    }

    /**
     * Set search places adapter
     * Tap on item user navigate to direction sheet
     * */
    private fun setSearchPlaceDirectionSuggestionAdapter() {
        mBinding.bottomSheetDirectionSearch.apply {
            rvSearchPlacesSuggestionDirection.layoutManager = LinearLayoutManager(requireContext())
            mSearchPlacesDirectionSuggestionAdapter =
                SearchPlacesSuggestionAdapter(
                    mPlaceList,
                    mPreferenceManager,
                    true,
                    object : SearchPlacesSuggestionAdapter.SearchPlaceSuggestionInterface {
                        override fun suggestedPlaceClick(position: Int) {
                            if (checkInternetConnection()) {
                                mViewModel.mIsPlaceSuggestion = false
                                mViewModel.mIsDirectionDataSet = true
                                if (!mViewModel.isDataSearchForDestination) {
                                    edtSearchDirection.setText(mPlaceList[position].text)
                                    edtSearchDirection.text?.length?.let {
                                        edtSearchDirection.setSelection(
                                            it,
                                        )
                                    }
                                } else {
                                    edtSearchDest.setText(mPlaceList[position].text)
                                    edtSearchDest.text?.length?.let { edtSearchDest.setSelection(it) }
                                }
                                lifecycleScope.launch {
                                    delay(CLICK_DEBOUNCE_ENABLE)
                                    mViewModel.mIsDirectionDataSet = false
                                }
                                if (mPlaceList[position].placeId.isNullOrEmpty() && !mPlaceList[position].queryId.isNullOrEmpty()) {
                                    mPlaceList[position].queryId?.let {
                                        mViewModel.searchPlaceIndexForText(
                                            queryId = it
                                        )
                                    }
                                } else {
                                    setPlaceData(position)
                                }
                                notifyDirectionAdapters()
                            }
                        }
                    },
                )
            rvSearchPlacesSuggestionDirection.adapter = mSearchPlacesDirectionSuggestionAdapter
        }
    }

    private fun BottomSheetDirectionSearchBinding.setPlaceData(position: Int) {
        mViewModel.mIsDirectionDataSet = true
        activity?.hideKeyboard()
        changeRouteListUI()
        mPlaceList[position].let {
            if (!mViewModel.isDataSearchForDestination) {
                edtSearchDirection.setText(it.text)
                mViewModel.mSearchDirectionOriginData = it
                mViewModel.mSearchDirectionOriginData?.isDestination = false
            } else {
                edtSearchDest.setText(it.text)
                mViewModel.mSearchDirectionDestinationData = it
                mViewModel.mSearchDirectionDestinationData?.isDestination = true
            }
            edtSearchDirection.clearFocus()
            edtSearchDest.clearFocus()
            clMyLocation.root.hide()
            enableDirectionSearch()
            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                if (mViewModel.mSearchDirectionDestinationData != null) {
                    showCurrentLocationDestinationRoute(it)
                }
            } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                if (mViewModel.mSearchDirectionOriginData != null) {
                    showCurrentLocationOriginRoute(it)
                }
            } else if (!edtSearchDirection.text.isNullOrEmpty() && !edtSearchDest.text.isNullOrEmpty()) {
                if (mViewModel.mSearchDirectionOriginData != null && mViewModel.mSearchDirectionDestinationData != null) {
                    showOriginToDestinationRoute()
                }
            }
        }
    }

    private fun enableDirectionSearch() {
        lifecycleScope.launch {
            delay(CLICK_DEBOUNCE_ENABLE)
            mViewModel.mIsDirectionDataSet = false
            mViewModel.mIsPlaceSuggestion = true
        }
    }

    private fun BottomSheetDirectionSearchBinding.showOriginToDestinationRoute() {
        clearDirectionData()
        setApiError()
        val positionOrigin = mViewModel.mSearchDirectionOriginData?.position
        val positionDestination = mViewModel.mSearchDirectionDestinationData?.position
        mViewModel.calculateDistance(
            latitude =
                positionOrigin?.get(1),
            longitude =
                positionOrigin?.get(0),
            latDestination =
                positionDestination?.get(1),
            lngDestination =
                positionDestination?.get(0),
            avoidanceOptions = mViewModel.getAvoidanceOptions(),
            departOption = mViewModel.mSelectedDepartOption,
            time = timeDepart,
            isWalkingAndTruckCall = false,
        )
        mViewModel.calculateDistance(
            latitude =
                positionOrigin?.get(1),
            longitude =
                positionOrigin?.get(0),
            latDestination =
                positionDestination?.get(1),
            lngDestination =
                positionDestination?.get(0),
            avoidanceOptions = mViewModel.getAvoidanceOptions(),
            departOption = mViewModel.mSelectedDepartOption,
            time = timeDepart,
            isWalkingAndTruckCall = true,
        )
        recordEventForAllMode(isWalkingAndTruckCall = true)
        showDirectionSearchShimmer()
        mViewModel.mSearchDirectionOriginData?.let { it1 ->
            mMapHelper.addMarker(
                requireActivity(),
                MarkerEnum.ORIGIN_ICON,
                it1,
            )
        }
        mViewModel.mSearchDirectionDestinationData?.let { it1 ->
            mViewModel.mSearchDirectionOriginData?.let { data ->
                mMapHelper.addMarker(
                    requireActivity(),
                    MarkerEnum.DIRECTION_ICON,
                    it1,
                    data,
                )
            }
        }
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            cardRouteOptionShow()
            mBaseActivity?.isTablet?.let {
                if (it) {
                    mBinding.cardDirection.hide()
                    mBinding.cardNavigation.show()
                }
            }
        }
    }

    private fun BottomSheetDirectionSearchBinding.setApiError() {
        layoutCardError.groupCardErrorNoSearchFound.hide()
        layoutCardError.root.hide()
        mViewModel.isCalculateDriveApiError = false
        mViewModel.isCalculateWalkApiError = false
        mViewModel.isCalculateTruckApiError = false
        mViewModel.isCalculateScooterApiError = false
    }

    private fun BottomSheetDirectionSearchBinding.showCurrentLocationDestinationRoute(it: SearchSuggestionData) {
        setApiError()
        clearDirectionData()
        val liveLocationLatLng: LatLng =
            if (isRunningTestLiveLocation || isRunningTest2LiveLocation || isRunningTest3LiveLocation) {
                LatLng(22.995545, 72.534031)
            } else {
                mMapHelper.getBestAvailableLocation()
            }
        mViewModel.calculateDistance(
            latitude = liveLocationLatLng.latitude,
            longitude = liveLocationLatLng.longitude,
            latDestination = it.position?.get(1),
            lngDestination = it.position?.get(0),
            avoidanceOptions = mViewModel.getAvoidanceOptions(),
            departOption = mViewModel.mSelectedDepartOption,
            time = timeDepart,
            isWalkingAndTruckCall = false,
        )
        mViewModel.calculateDistance(
            latitude = liveLocationLatLng.latitude,
            longitude = liveLocationLatLng.longitude,
            latDestination = it.position?.get(1),
            lngDestination = it.position?.get(0),
            avoidanceOptions = mViewModel.getAvoidanceOptions(),
            departOption = mViewModel.mSelectedDepartOption,
            time = timeDepart,
            isWalkingAndTruckCall = true,
        )
        recordEventForAllMode(isWalkingAndTruckCall = true)

        showDirectionSearchShimmer()
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            mMapHelper.addMarker(requireActivity(), MarkerEnum.DIRECTION_ICON, it)
            cardRouteOptionShow()
            mBaseActivity?.isTablet?.let {
                if (it) {
                    mBinding.cardNavigation.show()
                    mBinding.cardDirection.hide()
                }
            }
        }
    }

private fun BottomSheetDirectionSearchBinding.showCurrentLocationOriginRoute(it: SearchSuggestionData) {
        setApiError()
        clearDirectionData()
        val mLatLng = mMapHelper.getBestAvailableLocation()
        mViewModel.calculateDistance(
            latitude = it.position?.get(1),
            longitude = it.position?.get(0),
            latDestination = mLatLng.latitude,
            lngDestination = mLatLng.longitude,
            avoidanceOptions = mViewModel.getAvoidanceOptions(),
            departOption = mViewModel.mSelectedDepartOption,
            time = timeDepart,
            isWalkingAndTruckCall = false,
        )
        mViewModel.calculateDistance(
            latitude = it.position?.get(1),
            longitude = it.position?.get(0),
            latDestination = mLatLng.latitude,
            lngDestination = mLatLng.longitude,
            avoidanceOptions = mViewModel.getAvoidanceOptions(),
            departOption = mViewModel.mSelectedDepartOption,
            time = timeDepart,
            isWalkingAndTruckCall = true,
        )
        recordEventForAllMode(isWalkingAndTruckCall = true)

        showDirectionSearchShimmer()
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            mViewModel.mSearchDirectionOriginData?.let { it1 ->
                mMapHelper.addMarker(
                    requireActivity(),
                    MarkerEnum.ORIGIN_ICON,
                    it1,
                )
                it1.position
                    ?.get(1)
                    ?.let { latitude ->
                        it1.position?.get(0)?.let { longitude ->
                            LatLng(
                                latitude,
                                longitude,
                            )
                        }
                    }?.let { latLng ->
                        mMapHelper.setDirectionMarker(
                            latLng,
                            mLatLng.latitude,
                            mLatLng.longitude,
                            requireActivity(),
                            MarkerEnum.DIRECTION_ICON,
                            "",
                        )
                    }
            }
            cardRouteOptionShow()
            mBaseActivity?.isTablet?.let {
                if (it) {
                    mBinding.cardNavigation.show()
                    mBinding.cardDirection.hide()
                }
            }
        }
    }

    private fun checkDirectionLoaderVisible(): Boolean {
        mBinding.bottomSheetDirectionSearch.apply {
            return clDriveLoader.isVisible ||
                clWalkLoader.isVisible ||
                clTruckLoader.isVisible ||
                clScooterLoader.isVisible
        }
    }

    private fun showDirectionSearchShimmer() {
        mBinding.bottomSheetDirectionSearch.apply {
            clDriveLoader.show()
            clWalkLoader.show()
            clTruckLoader.show()
            clScooterLoader.show()
            clScooter.invisible()
            clTruck.invisible()
            clWalk.invisible()
            clDrive.invisible()
        }
    }

    private fun BottomSheetDirectionSearchBinding.cardRouteOptionShow() {
        hideViews(rvSearchPlacesDirection, rvSearchPlacesSuggestionDirection, clMyLocation.root)
        showViews(cardMapOption, cardRouteDepartOptions)
    }

    private fun BottomSheetDirectionSearchBinding.cardRouteOptionHide() {
        showViews(rvSearchPlacesDirection, rvSearchPlacesSuggestionDirection)
        hideViews(cardRouteDepartOptions, cardMapOption, cardListRoutesOption, layoutCardError.root)
    }

    /**
     * Set search places adapter
     * Tap on item user navigate to direction sheet
     * */
    private fun setSearchPlaceAdapter() {
        mBinding.bottomSheetSearch.apply {
            rvSearchPlaces.layoutManager = LinearLayoutManager(requireContext())
            mAdapter =
                SearchPlacesAdapter(
                    mPlaceList,
                    mPreferenceManager,
                    false,
                    object : SearchPlacesAdapter.SearchPlaceInterface {
                        override fun placeClick(position: Int) {
                            if (checkInternetConnection()) {
                                setDirectionData(mPlaceList[position], false)
                            }
                        }
                    },
                )
            rvSearchPlaces.adapter = mAdapter
        }
    }

    /**
     * Set search places adapter
     * Tap on item user navigate to direction sheet
     * */
    private fun setSearchPlaceSuggestionAdapter() {
        mBinding.bottomSheetSearch.apply {
            rvSearchPlacesSuggestion.layoutManager = LinearLayoutManager(requireContext())
            mSearchPlacesSuggestionAdapter =
                SearchPlacesSuggestionAdapter(
                    mPlaceList,
                    mPreferenceManager,
                    false,
                    object : SearchPlacesSuggestionAdapter.SearchPlaceSuggestionInterface {
                        override fun suggestedPlaceClick(position: Int) {
                            if (checkInternetConnection()) {
                                mViewModel.mIsPlaceSuggestion = false
                                edtSearchPlaces.setText(mPlaceList[position].text)
                                edtSearchPlaces.setSelection(edtSearchPlaces.text.toString().length)
                                if (mPlaceList[position].placeId.isNullOrEmpty() && !mPlaceList[position].queryId.isNullOrEmpty()) {
                                    mPlaceList[position].queryId?.let {
                                        mViewModel.searchPlaceIndexForText(
                                            queryId = it,
                                        )
                                    }
                                } else {
                                    setDirectionData(mPlaceList[position], false)
                                }
                                notifyAdapters()
                            }
                        }
                    },
                )
            rvSearchPlacesSuggestion.adapter = mSearchPlacesSuggestionAdapter
        }
    }

    // get direction data from places
    private fun setDirectionData(
        data: SearchSuggestionData,
        isFromMapClick: Boolean,
    ) {
        lifecycleScope.launch {
            activity?.hideKeyboard()
            mBinding.bottomSheetSearch.apply {
                ivClose.hide()
                edtSearchPlaces.clearFocus()
                edtSearchPlaces.setText("")
            }
            mBottomSheetHelper.hideSearchBottomSheet(true)
            mBottomSheetHelper.isSearchSheetOpen = false
            mBaseActivity?.bottomNavigationVisibility(false)
            mBottomSheetHelper.expandDirectionSheet()
            mViewModel.mSearchSuggestionData = data
            mViewModel.mSearchDirectionDestinationData = data
            mViewModel.mSearchDirectionDestinationData?.isDestination = true
            hideViews(mBinding.cardDirection, mBinding.cardNavigation)
            mBinding.bottomSheetDirection.apply {
                tvDirectionTime.hide()
                groupDistance.invisible()
                hideViews(tvDirectionError, ivInfo)
                tvDirectionError2.hide()

                val mLatLng = mMapHelper.getBestAvailableLocation()
                mViewModel.isCalculateDriveApiError = false

                if (data.placeId != null) {
                    data.placeId?.let {
                        groupPlaceDetailsLoad.show()
                        mViewModel.getPlaceData(it)
                    }
                } else {
                    groupPlaceDetailsLoad.hide()
                    setContactPlaceData(data)
                }
                mViewModel.calculateDistance(
                    latitude = mLatLng.latitude,
                    longitude = mLatLng.longitude,
                    latDestination = data.position?.get(1),
                    lngDestination = data.position?.get(0),
                    avoidanceOptions = mViewModel.getAvoidanceOptions(),
                    departOption = mViewModel.mSelectedDepartOption,
                    time = timeDepart,
                    isWalkingAndTruckCall = false,
                )
                recordEventForAllMode(isWalkingAndTruckCall = false)
                if (data.amazonLocationAddress?.label?.let { validateLatLng(it) } != null) {
                    tvDirectionAddress.text = data.amazonLocationAddress?.label
                    sheetDirectionTvDirectionStreet.hide()
                } else {
                    val label = data.amazonLocationAddress?.label
                    if (label != null) {
                        if (label.contains(",")) {
                            val index = label.indexOf(",")
                            val result: String = index.let { label.substring(0, it) }
                            tvDirectionAddress.text = result
                            sheetDirectionTvDirectionStreet.show()
                            sheetDirectionTvDirectionStreet.text =
                                index.let { label.substring(index + 1, label.length).trim() }
                        } else {
                            tvDirectionAddress.text = label
                        }
                    }
                }
                data.distance?.let {
                    val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
                    val showDistance =
                        if (isMetric) {
                            it > 400
                        } else {
                            it > 248.5
                        }
                    if (showDistance) {
                        tvDirectionDistance.text =
                            getMetricsNew(requireContext(), it, isMetric, true)
                    }
                }
                notifyAdapters()
            }
            mMapHelper.clearMarker()
            mMapHelper.addDirectionMarker(
                requireActivity(),
                MarkerEnum.DIRECTION_ICON,
                mViewModel.mSearchDirectionDestinationData,
                isFromMapClick,
            )
        }
    }

    private fun setContactPlaceData(data: SearchSuggestionData) {
        mBinding.bottomSheetDirection.apply {
            data.contacts?.let {
                if (!it.phones.isNullOrEmpty()) {
                    tvPhone.movementMethod = LinkMovementMethod.getInstance()
                    showViews(tvPhone, ivPhone, viewDivider)
                    it.phones!!.forEachIndexed { index, phones ->
                        val spannableString = SpannableString(phones.value)
                        val color =
                            ContextCompat.getColor(requireContext(), R.color.color_medium_black)
                        spannableString.setSpan(
                            object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse("tel:${phones.value}"),
                                        ),
                                    )
                                }

                                override fun updateDrawState(ds: TextPaint) {
                                    super.updateDrawState(ds)
                                    ds.color = color
                                    ds.isUnderlineText = true
                                }
                            },
                            0,
                            spannableString.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                        )
                        tvPhone.append(spannableString)
                        if (index != it.phones!!.lastIndex) {
                            tvPhone.append("\n")
                        }
                    }
                }
                if (!it.websites.isNullOrEmpty()) {
                    tvPlaceLink.movementMethod = LinkMovementMethod.getInstance()
                    showViews(tvPlaceLink, ivPlaceLink, viewDivider)
                    it.websites!!.forEachIndexed { index, website ->
                        val spannableString = SpannableString(website.value)
                        val color =
                            ContextCompat.getColor(requireContext(), R.color.color_primary_green)
                        spannableString.setSpan(
                            object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(website.value),
                                        ),
                                    )
                                }

                                override fun updateDrawState(ds: TextPaint) {
                                    super.updateDrawState(ds)
                                    ds.color = color
                                    ds.isUnderlineText = false
                                }
                            },
                            0,
                            spannableString.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                        )

                        tvPlaceLink.append(spannableString)

                        if (index != it.websites!!.lastIndex) {
                            tvPlaceLink.append("\n")
                        }
                    }
                }
            }
            data.openingHours?.let {
                if (!data.openingHours.isNullOrEmpty()) {
                    showViews(tvSchedule, ivSchedule, ivArrow, viewDivider)
                    data.openingHours!!.forEachIndexed { index, openingHour ->
                        openingHour.display?.forEachIndexed { displayIndex, displayText ->
                            tvScheduleDetails.append(displayText)
                            if (index != data.openingHours!!.lastIndex || displayIndex != openingHour.display!!.lastIndex) {
                                tvScheduleDetails.append("\n")
                            }
                        }
                    }
                }
            }
        }
    }

    // clear markers from map and clear places list
    @SuppressLint("NotifyDataSetChanged")
    private fun notifyAdapters() {
        if (mPlaceList.isNotEmpty()) {
            mMapHelper.clearMarker()
            mPlaceList.clear()
            mAdapter?.notifyDataSetChanged()
            mSearchPlacesSuggestionAdapter?.notifyDataSetChanged()
        }
    }

    // clear markers from map and clear places list
    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDirectionAdapters() {
        if (mPlaceList.isNotEmpty()) {
            mPlaceList.clear()
            mAdapterDirection?.notifyDataSetChanged()
            mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
        }
    }

    private fun setMap(savedInstanceState: Bundle?) {
        mBinding.mapView.onCreate(savedInstanceState)
        mBinding.mapView.getMapAsync(this)
    }

    override fun logout(
        dialog: DialogInterface,
        isDisconnectFromAWSRequired: Boolean,
    ) {
        (activity as MainActivity).openSignOut()
        dialog.dismiss()
    }

    override fun onMapReady(mapLibreMap: MapLibreMap) {
        mapLibreMap.addOnMapClickListener(this)
        val mapStyleNameDisplay =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_standard))
                ?: getString(R.string.map_standard)
        val colorScheme =
            mPreferenceManager.getValue(KEY_COLOR_SCHEMES, ATTRIBUTE_LIGHT) ?: ATTRIBUTE_LIGHT
        mMapHelper.initSymbolManager(
            mBinding.mapView,
            mapLibreMap,
            mapStyleNameDisplay,
            colorScheme,
            this,
            this,
            activity,
            mPreferenceManager,
        )
        activity?.let {
            mBaseActivity?.mGeofenceUtils?.setMapBox(
                it,
                mapLibreMap,
                mMapHelper,
                mPreferenceManager,
            )
            mBaseActivity?.mTrackingUtils?.setMapBox(
                it,
                mapLibreMap,
                mMapHelper,
            )
        }
        mapLibreMap.uiSettings.isCompassEnabled = false
        this.mMapLibreMap = mapLibreMap
        mapLibreMap.addOnCameraIdleListener {
            mViewModel.mLatLng =
                mapLibreMap.cameraPosition.target?.let {
                    LatLng(
                        it.latitude,
                        it.longitude,
                    )
                }
        }
        setMapBoxInSimulation()
    }

    fun setMapBoxInSimulation() {
        activity?.let {
            mMapLibreMap?.let { it1 ->
                mBaseActivity?.mSimulationUtils?.setMapBox(
                    it,
                    it1,
                    mMapHelper,
                )
            }
        }
    }

    // check gps enable or not
    @SuppressLint("VisibleForTests")
    private fun checkGpsLocationProvider(
        isLocationAlreadyEnabled: Boolean,
        isCurrentLocationClicked: Boolean,
        isLiveLocationClick: Boolean,
    ) {
        val locationRequest: LocationRequest =
            LocationRequest
                .Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    10000L,
                ).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val result: Task<LocationSettingsResponse> =
            LocationServices
                .getSettingsClient(requireActivity())
                .checkLocationSettings(builder.build())
        builder.setAlwaysShow(true)
        mViewModel.mIsLocationAlreadyEnabled = isLocationAlreadyEnabled
        mViewModel.mIsCurrentLocationClicked = isCurrentLocationClicked
        mViewModel.isLiveLocationClick = isLiveLocationClick
        result.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
                checkAndEnableLocation()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            val resolvable = exception as ResolvableApiException
                            val intentSenderRequest =
                                IntentSenderRequest.Builder(resolvable.resolution).build()
                            if (isAdded) {
                                gpsActivityResult.launch(intentSenderRequest)
                            }
                        } catch (_: IntentSender.SendIntentException) {
                        } catch (_: ClassCastException) {
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

    private fun checkAndEnableLocation() {
        if (mViewModel.mIsTrackingLocationClicked) {
            mViewModel.mIsTrackingLocationClicked = false
            mPreferenceManager.setValue(IS_LOCATION_TRACKING_ENABLE, true)
            mBaseActivity?.mTrackingUtils?.locationPermissionAdded()
        } else {
            if (mViewModel.mIsCurrentLocationClicked) {
                mBaseActivity?.resetLocationPermission()
                mMapHelper.checkLocationComponentEnable()
            } else {
                if (mViewModel.mIsLocationAlreadyEnabled) {
                    mMapHelper.checkLocationComponentEnable()
                } else {
                    mBaseActivity?.resetLocationPermission()
                    mMapHelper.enableLocationComponent()
                    mMapHelper.setInitialLocation()
                }
            }
            mViewModel.isLiveLocationClick = false
            mRedirectionType?.let { type ->
                when (type) {
                    RedirectionType.ROUTE_OPTION.name -> {
                        checkRouteData()
                    }

                    RedirectionType.SEARCH_DIRECTION_CAR.name -> {
                        routeOption()
                    }

                    RedirectionType.MY_LOCATION.name -> directionMyLocation()
                }
                mRedirectionType = null
            }
        }
    }

    fun changeDirectionCardMargin(marginBottom: Int) {
        mBaseActivity?.isTablet?.let {
            if (!it) {
                showViews(mBinding.cardDirection, mBinding.cardNavigation)
                val layoutParams: ViewGroup.MarginLayoutParams =
                    mBinding.cardDirection.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(
                    resources.getDimension(R.dimen.dp_16).toInt(),
                    0,
                    resources.getDimension(R.dimen.dp_16).toInt(),
                    marginBottom,
                )
                mBinding.cardDirection.requestLayout()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mBinding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mBinding.mapView.onResume()
        if (mBottomSheetHelper.isDirectionSheetVisible()) {
            mBinding.bottomSheetDirection.apply {
                if (tvDirectionError2.isVisible) {
                    if (tvDirectionError2.text.equals(getString(R.string.label_location_permission_denied))) {
                        if (activity?.checkLocationPermission() == true) {
                            if (!isGPSEnabled(requireContext())) {
                                mBinding.cardNavigation.performClick()
                                tvDirectionError2.text =
                                    getString(R.string.label_location_permission_denied)
                            } else {
                                hideKeyBoardAndGetLiveData()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun BottomSheetDirectionBinding.hideKeyBoardAndGetLiveData() {
        hideViews(tvDirectionError, tvDirectionError2, ivInfo)
        mBinding.cardNavigation.performClick()
        lifecycleScope.launch {
            delay(1000)
            mViewModel.mSearchSuggestionData.let {

                val mLatLng = mMapHelper.getBestAvailableLocation()
                mViewModel.isCalculateDriveApiError = false

                mViewModel.calculateDistance(
                    latitude = mLatLng.latitude,
                    longitude = mLatLng.longitude,
                    latDestination = it?.position?.get(1),
                    lngDestination = it?.position?.get(0),
                    avoidanceOptions = mViewModel.getAvoidanceOptions(),
                    departOption = mViewModel.mSelectedDepartOption,
                    time = timeDepart,
                    isWalkingAndTruckCall = false,
                )
                recordEventForAllMode(isWalkingAndTruckCall = false)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mBinding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this::mBinding.isInitialized) {
            mBinding.mapView.onSaveInstanceState(outState)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mBinding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        gpsActivityResult.unregister()
        if (this::mBinding.isInitialized) {
            mBinding.mapView.onDestroy()
        }
    }

    fun mapStyleChange(mapStyleName: String) {
        changeMapStyle(mapStyleName)
    }

    private fun changeMapStyle(mapStyleName: String) {
        activity?.runOnUiThread {
            changeStyle(mapStyleName)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeStyle(mapStyleName: String) {
        clearAllMapData()
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
                            if (mapStyleName == getString(R.string.map_satellite)) {
                                mPreferenceManager.setValue(KEY_POLITICAL_VIEW, "")
                            }
                            innerData.isSelected = true
                            innerData.mapName?.let { it1 ->
                                val colorScheme =
                                    mPreferenceManager.getValue(KEY_COLOR_SCHEMES, ATTRIBUTE_LIGHT)
                                        ?: ATTRIBUTE_LIGHT
                                mMapHelper.updateStyle(
                                    it1,
                                    colorScheme,
                                )
                            }
                            innerData.mapName?.let { mapName ->
                                val properties =
                                    listOf(
                                        Pair(AnalyticsAttribute.PROVIDER, mapName),
                                        Pair(
                                            AnalyticsAttribute.TRIGGERED_BY,
                                            AnalyticsAttributeValue.EXPLORER,
                                        ),
                                    )
                                (activity as MainActivity).analyticsUtils?.recordEvent(
                                    EventType.MAP_STYLE_CHANGE,
                                    properties,
                                )
                                mPreferenceManager.setValue(
                                    KEY_MAP_STYLE_NAME,
                                    mapName,
                                )
                            }
                        }
                    }
                }
            }
        }
        mBaseActivity?.isTablet?.let {
            mapStyleBottomSheetFragment?.notifyAdapter()
        }
    }

    private fun clearAllMapData() {
        try {
            mBinding.bottomSheetSearch.edtSearchPlaces.setText("")
            mBinding.bottomSheetSearch.edtSearchPlaces.clearFocus()
            mMapHelper.addLiveLocationMarker(false)
            mBottomSheetHelper.hideDirectionSearchBottomSheet(this@ExploreFragment)
            hideDirectionBottomSheet()
            mBinding.apply {
                bottomSheetNavigation.apply {
                    showViews(
                        cardMap,
                    )
                    if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() != true &&
                        mBaseActivity?.mTrackingUtils?.isChangeDataProviderClicked != true &&
                        mBaseActivity?.mGeofenceUtils?.isChangeDataProviderClicked != true
                    ) {
                        cardGeofenceMap.show()
                        mBaseActivity?.isTablet?.let {
                            if (!it) {
                                cardDirection.show()
                                cardNavigation.show()
                            }
                        }
                    }
                }
            }
            clearNavigationData()
            if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() != true &&
                mBaseActivity?.mTrackingUtils?.isChangeDataProviderClicked != true &&
                mBaseActivity?.mGeofenceUtils?.isChangeDataProviderClicked != true
            ) {
                if (activity is MainActivity) {
                    (activity as MainActivity).moveToExploreScreen()
                    (activity as MainActivity).mGeofenceUtils?.hideAllGeofenceBottomSheet()
                    (activity as MainActivity).mTrackingUtils?.hideTrackingBottomSheet()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearKeyboardFocus() {
        mBinding.bottomSheetSearch.edtSearchPlaces.clearFocus()
    }

    override fun onMapClick(point: LatLng): Boolean {
        if (!mBottomSheetHelper.isSearchPlaceSheetVisible() || mBottomSheetHelper.isDirectionSheetVisible()) {
            mMapHelper.clearMarker()
            mMapHelper.setMarker(
                point.latitude,
                point.longitude,
                requireActivity(),
                MarkerEnum.DIRECTION_ICON,
                "",
            )
            hideDirectionData()
            mViewModel.getAddressLineFromLatLng(point.longitude, point.latitude)
            val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
            val properties =
                listOf(
                    Pair(AnalyticsAttribute.TRAVEL_MODE, RouteTravelMode.Car.value),
                    Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.PLACES_POPUP),
                    Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
                )
            (activity as MainActivity).analyticsUtils?.recordEvent(ROUTE_SEARCH, properties)
            return true
        }
        if (mBaseActivity?.mGeofenceUtils?.isAddGeofenceBottomSheetVisible() == true) {
            mBaseActivity?.mGeofenceUtils?.mapClick(point)
            mViewModel.getAddressLineFromLatLng(point.longitude, point.latitude)
            return true
        }
        return false
    }

    private fun hideDirectionData() {
        mBinding.bottomSheetDirection.apply {
            tvPhone.text = ""
            tvPlaceLink.text = ""
            tvScheduleDetails.text = ""
            ivArrow.rotation = 0F
            hideViews(
                tvPhone,
                ivPhone,
                tvPlaceLink,
                ivPlaceLink,
                tvSchedule,
                tvScheduleDetails,
                ivSchedule,
                ivArrow,
            )
        }
    }

    private fun showTimePicker(
        onDateTimeSelected: (isoDate: String, displayDate: String) -> Unit,
        onCancel: () -> Unit,
    ) {
        calendar?.let { cal->
            val timePicker = MaterialTimePicker.Builder()
                .setTitleText(getString(R.string.time))
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(cal.get(Calendar.HOUR_OF_DAY))
                .setMinute(cal.get(Calendar.MINUTE))
                .build()

            timePicker.addOnPositiveButtonClickListener {
                cal.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                cal.set(Calendar.MINUTE, timePicker.minute)

                val currentDateTime = Calendar.getInstance()
                if (cal.timeInMillis < currentDateTime.timeInMillis) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.label_selected_time_cannot_be_in_the_past),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val displayDate = formatToDisplayDate(cal.time)
                    val isoDate = formatToISO8601(cal.time)
                    onDateTimeSelected(isoDate, displayDate)
                }
            }

            timePicker.addOnCancelListener {
                onCancel()
            }

            timePicker.show(parentFragmentManager, "MaterialTimePicker")
        }
    }

    override fun mapLoadedSuccess() {
        mBinding.mapView.contentDescription = "Amazon Map Ready"
        mBinding.groupMapLoad.hide()
    }

    override fun onScaleBegin(detector: StandardScaleGestureDetector) {
        mViewModel.isZooming = true
    }

    override fun onScale(detector: StandardScaleGestureDetector) {
        mMapLibreMap?.easeCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition
                    .Builder()
                    .target(mMapHelper.getBestAvailableLocation())
                    .build(),
            ),
            Durations.CAMERA_DURATION_1000,
        )
    }

    override fun onScaleEnd(detector: StandardScaleGestureDetector) {
        mViewModel.isZooming = false
        mMapHelper.navigationZoomCamera(mMapHelper.getBestAvailableLocation(), false)
    }

    override fun onMapStyleChanged(mapStyle: String) {
        val colorScheme =
            mPreferenceManager.getValue(KEY_COLOR_SCHEMES, ATTRIBUTE_LIGHT) ?: ATTRIBUTE_LIGHT
        var logoResId =
            when (colorScheme) {
                ATTRIBUTE_LIGHT,
                    -> R.drawable.ic_amazon_logo_on_light

                ATTRIBUTE_DARK,
                    -> R.drawable.ic_amazon_logo_on_dark

                else -> R.drawable.ic_amazon_logo_on_light
            }
        val mapStyleName =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_standard))
                ?: getString(R.string.map_standard)
        if (mapStyleName == getString(R.string.map_satellite) || mapStyleName == getString(R.string.map_hybrid)) {
            logoResId = R.drawable.ic_amazon_logo_on_dark
        }
        lifecycleScope.launch {
            delay(DELAY_500)
            mMapLibreMap?.setLatLngBoundsForCameraTarget(null)
            mMapLibreMap?.style?.let { mMapHelper.updateZoomRange(it) }
            mMapHelper.checkLocationComponentEnable()
        }
        if (activity is MainActivity) {
            (activity as MainActivity).changeAmazonLogo(logoResId)
        }
        mBaseActivity?.isTablet?.let {
            if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true) {
                return@let
            }
            if (it) {
                mBinding.cardNavigation.show()
                mBinding.cardDirection.show()
            }
        }
        mBinding.bottomSheetSearch.imgAmazonLogoSearchSheet.setImageResource(logoResId)
        mBinding.bottomSheetDirection.imgAmazonLogoDirection?.setImageResource(logoResId)
        mBinding.bottomSheetDirectionSearch.imgAmazonLogoDirectionSearchSheet.setImageResource(
            logoResId,
        )
        mBinding.bottomSheetNavigation.imgAmazonLogoNavigation.setImageResource(logoResId)
        mBinding.bottomSheetNavigationComplete.imgAmazonLogoNavigationComplete.setImageResource(
            logoResId,
        )
        mBinding.bottomSheetGeofenceList.imgAmazonLogoGeofenceList?.setImageResource(logoResId)
        mBinding.bottomSheetAddGeofence.imgAmazonLogoAddGeofence?.setImageResource(logoResId)
        mBinding.bottomSheetTracking.imgAmazonLogoTrackingSheet?.setImageResource(logoResId)
        mBaseActivity?.mSimulationUtils?.setImageIcon(logoResId)
        if (mapStyleBottomSheetFragment != null && mapStyleBottomSheetFragment?.isVisible == true) {
            mapStyleBottomSheetFragment?.setImageIcon(logoResId)
        }
        if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true) {
            debouncer.debounce(DELAY_500) {
                mBaseActivity?.mSimulationUtils?.setSimulationData()
            }
        }
    }
}
