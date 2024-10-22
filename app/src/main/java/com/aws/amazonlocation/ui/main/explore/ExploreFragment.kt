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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.inputmethod.EditorInfo
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.LinearLayoutManager
import aws.sdk.kotlin.services.location.model.CalculateRouteResponse
import aws.sdk.kotlin.services.location.model.Leg
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import aws.sdk.kotlin.services.location.model.Place
import aws.sdk.kotlin.services.location.model.PlaceGeometry
import aws.sdk.kotlin.services.location.model.TravelMode
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
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.databinding.BottomSheetDirectionBinding
import com.aws.amazonlocation.databinding.BottomSheetDirectionSearchBinding
import com.aws.amazonlocation.databinding.BottomSheetMapStyleBinding
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
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.CLICK_DEBOUNCE
import com.aws.amazonlocation.utils.CLICK_DEBOUNCE_ENABLE
import com.aws.amazonlocation.utils.CLICK_TIME_DIFFERENCE
import com.aws.amazonlocation.utils.DELAY_300
import com.aws.amazonlocation.utils.DELAY_500
import com.aws.amazonlocation.utils.Distance.DISTANCE_IN_METER_10
import com.aws.amazonlocation.utils.Durations
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.EventType.PLACE_SEARCH
import com.aws.amazonlocation.utils.EventType.ROUTE_OPTION_CHANGED
import com.aws.amazonlocation.utils.EventType.ROUTE_SEARCH
import com.aws.amazonlocation.utils.IS_LOCATION_TRACKING_ENABLE
import com.aws.amazonlocation.utils.KEY_AVOID_FERRIES
import com.aws.amazonlocation.utils.KEY_AVOID_TOLLS
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_GRAB_DONT_ASK
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_NEAREST_REGION
import com.aws.amazonlocation.utils.KEY_OPEN_DATA_DONT_ASK
import com.aws.amazonlocation.utils.KEY_SELECTED_REGION
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.KEY_URL
import com.aws.amazonlocation.utils.KILOMETERS
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.MAP_STYLE_ATTRIBUTION
import com.aws.amazonlocation.utils.MILES
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.MapNames
import com.aws.amazonlocation.utils.MapNames.ESRI_LIGHT
import com.aws.amazonlocation.utils.MapStyleRestartInterface
import com.aws.amazonlocation.utils.MapStyles
import com.aws.amazonlocation.utils.MapStyles.VECTOR_ESRI_TOPOGRAPHIC
import com.aws.amazonlocation.utils.PREFS_KEY_IDENTITY_ID
import com.aws.amazonlocation.utils.PREFS_NAME_AUTH
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.STRING_FORMAT
import com.aws.amazonlocation.utils.SignOutInterface
import com.aws.amazonlocation.utils.SimulationDialogInterface
import com.aws.amazonlocation.utils.TRAVEL_MODE_BICYCLE
import com.aws.amazonlocation.utils.TRAVEL_MODE_CAR
import com.aws.amazonlocation.utils.TRAVEL_MODE_MOTORCYCLE
import com.aws.amazonlocation.utils.TrackerCons
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.Units.convertToLowerUnit
import com.aws.amazonlocation.utils.Units.getDeviceId
import com.aws.amazonlocation.utils.Units.getMetricsNew
import com.aws.amazonlocation.utils.Units.getTime
import com.aws.amazonlocation.utils.Units.isGPSEnabled
import com.aws.amazonlocation.utils.Units.isMetric
import com.aws.amazonlocation.utils.attributionPattern
import com.aws.amazonlocation.utils.checkLocationPermission
import com.aws.amazonlocation.utils.enableOpenData
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.getRegion
import com.aws.amazonlocation.utils.getUserName
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideKeyboard
import com.aws.amazonlocation.utils.hideSoftKeyboard
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.invisible
import com.aws.amazonlocation.utils.isGrabMapEnable
import com.aws.amazonlocation.utils.isGrabMapSelected
import com.aws.amazonlocation.utils.isInternetAvailable
import com.aws.amazonlocation.utils.isRunningTest
import com.aws.amazonlocation.utils.isRunningTest2LiveLocation
import com.aws.amazonlocation.utils.isRunningTest3LiveLocation
import com.aws.amazonlocation.utils.isRunningTestLiveLocation
import com.aws.amazonlocation.utils.latNorth
import com.aws.amazonlocation.utils.latSouth
import com.aws.amazonlocation.utils.locationPermissionDialog
import com.aws.amazonlocation.utils.lonEast
import com.aws.amazonlocation.utils.lonWest
import com.aws.amazonlocation.utils.regionDisplayName
import com.aws.amazonlocation.utils.restartAppMapStyleDialog
import com.aws.amazonlocation.utils.restartApplication
import com.aws.amazonlocation.utils.show
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
import com.mapbox.android.gestures.StandardScaleGestureDetector
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.geojson.Point
import org.maplibre.geojson.Point.fromLngLat
import software.amazon.location.auth.EncryptedSharedPreferences

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
    private var isFilterApplied: Boolean = false
    private var isNoDataFoundVisible: Boolean = false
    private var mProviderAdapter: SortingAdapter? = null
    private var mAttributeAdapter: SortingAdapter? = null
    private var mTypeAdapter: SortingAdapter? = null
    private var isDataSearchForDestination: Boolean = false
    private var mapStyleBottomSheetFragment: MapStyleBottomSheetFragment?= null
    private var isCalculateDriveApiError: Boolean = false
    private var isCalculateWalkApiError: Boolean = false
    private var isCalculateTruckApiError: Boolean = false
    private var isCalculateBicycleApiError: Boolean = false
    private var isCalculateMotorcycleApiError: Boolean = false
    private var isLocationUpdatedNeeded: Boolean = false
    private var isZooming: Boolean = false
    private var mLastClickTime: Long = 0
    private var mIsSwapClicked: Boolean = false
    private var mIsDirectionDataSet: Boolean = false
    private var mIsDirectionDataSetNew: Boolean = false
    private var mIsDirectionSheetHalfExpanded: Boolean = false
    private var mIsLocationAlreadyEnabled: Boolean = false
    private var mIsCurrentLocationClicked: Boolean = false
    private var mIsTrackingLocationClicked: Boolean = false
    private var isLiveLocationClick: Boolean = false
    private lateinit var mBinding: FragmentExploreBinding
    private var mMapboxMap: MapLibreMap? = null
    private var mAdapter: SearchPlacesAdapter? = null
    private var mMapStyleAdapter: MapStyleAdapter? = null
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
    private var mIsAvoidTolls: Boolean = false
    private var mIsAvoidFerries: Boolean = false
    private var mIsRouteOptionsOpened = false
    private var mTravelMode: String = TravelMode.Car.value
    private var mRouteFinish: Boolean = false
    private var mRedirectionType: String? = null
    private var encryptedSharedPreferences: EncryptedSharedPreferences?= null
    private val bounds =
        LatLngBounds
            .Builder()
            .include(LatLng(latNorth, lonWest))
            .include(LatLng(latSouth, lonEast))
            .build()

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
                    bottomSheetDirection.clPersistentBottomSheetDirection.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    bottomSheetDirectionSearch.clDirectionSearchSheet.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    bottomSheetNavigation.clNavigationParent.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    bottomSheetNavigationComplete.clPersistentBottomSheetNavigationComplete.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    bottomSheetMapStyle.clMapStyleBottomSheet.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    bottomSheetAttribution.clMain.layoutDirection = View.LAYOUT_DIRECTION_RTL
                }
            }
        }
    }

    fun showKeyBoard() {
        mBaseActivity?.mGeofenceUtils?.let {
            if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                mBottomSheetHelper.expandDirectionSearchSheet(this@ExploreFragment)
            } else if (it.geofenceBottomSheetVisibility()) {
                mBaseActivity?.mGeofenceUtils?.expandAddGeofenceBottomSheet()
            } else if (mBottomSheetHelper.isMapStyleExpandedOrHalfExpand()) {
                mBottomSheetHelper.expandMapStyleSheet()
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
                        edtSearchDest.clearFocus()
                        edtSearchDirection.clearFocus()
                    }
                }
            } else if (mBottomSheetHelper.isMapStyleExpandedOrHalfExpand()) {
                mBinding.apply {
                    bottomSheetMapStyle.apply {
                        etSearchMap.clearFocus()
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
            mIsAvoidTolls = mPreferenceManager.getValue(KEY_AVOID_TOLLS, false)
            mIsAvoidFerries = mPreferenceManager.getValue(KEY_AVOID_FERRIES, false)
            mBinding.bottomSheetDirectionSearch.switchAvoidTools.isChecked = mIsAvoidTolls
            mBinding.bottomSheetDirectionSearch.switchAvoidFerries.isChecked = mIsAvoidFerries

            mBottomSheetHelper.setNavigationBottomSheet(mBinding.bottomSheetNavigation)
            mBottomSheetHelper.setNavigationCompleteBottomSheet(
                mBinding.bottomSheetNavigationComplete.clPersistentBottomSheetNavigationComplete,
            )
            mBottomSheetHelper.setDirectionBottomSheet(mBinding.bottomSheetDirection.clPersistentBottomSheetDirection)
            mBaseActivity?.isTablet?.let {
                if (!it) {
                    mBottomSheetHelper.setMapStyleBottomSheet(
                        activity,
                        mBinding.bottomSheetMapStyle,
                        mBaseActivity,
                    )
                } else {
                    mBinding.bottomSheetMapStyle.clMapStyleBottomSheet.hide()
                }
            }
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
                if (!mAWSLocationHelper.checkClientInitialize()) {
                    val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
                    if (mAuthStatus == AuthEnum.SIGNED_IN.name) {
                        if (mAWSLocationHelper.isAuthTokenExpired()) {
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
            setMapStyleAdapter()
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
                        handleResult.onLoading {
                            rvGeofence.hide()
                            clSearchLoaderGeofenceList.root.show()
                        }.onSuccess {
                            val propertiesAws = listOf(
                                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.GEOFENCES)
                            )
                            (activity as MainActivity).analyticsUtils?.recordEvent(EventType.GET_GEOFENCES_LIST_SUCCESSFUL, propertiesAws)
                            clSearchLoaderGeofenceList.root.hide()
                            rvGeofence.show()
                            lifecycleScope.launch(Dispatchers.Main) {
                                mBaseActivity?.mGeofenceUtils?.manageGeofenceListUI(it)
                            }
                        }.onError {
                            val propertiesAws = listOf(
                                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.GEOFENCES)
                            )
                            (activity as MainActivity).analyticsUtils?.recordEvent(EventType.GET_GEOFENCES_LIST_FAILED, propertiesAws)
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
                handleResult.onLoading {
                }.onSuccess {
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.GEOFENCES)
                    )
                    (activity as MainActivity).analyticsUtils?.recordEvent(EventType.GEOFENCE_CREATION_SUCCESSFUL, propertiesAws)
                    lifecycleScope.launch(Dispatchers.Main) {
                        mBaseActivity?.mGeofenceUtils?.mangeAddGeofenceUI(requireActivity())
                        mBaseActivity?.bottomNavigationVisibility(true)
                        showViews(mBinding.cardGeofenceMap, mBinding.cardMap)
                        activity?.hideKeyboard()
                    }
                }.onError {
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.GEOFENCES)
                    )
                    (activity as MainActivity).analyticsUtils?.recordEvent(EventType.GEOFENCE_CREATION_FAILED, propertiesAws)
                    if (it.messageResource.toString()
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
                handleResult.onLoading {
                }.onSuccess {
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.GEOFENCES)
                    )
                    (activity as MainActivity).analyticsUtils?.recordEvent(EventType.GEOFENCE_DELETION_SUCCESSFUL, propertiesAws)
                    lifecycleScope.launch(Dispatchers.Main) {
                        mGeofenceInterface.hideShowBottomNavigationBar(
                            false,
                            GeofenceBottomSheetEnum.NONE
                        )
                        it.position?.let { position ->
                            mBaseActivity?.mGeofenceUtils?.notifyGeofenceList(
                                position,
                                requireActivity()
                            )
                        }
                    }
                }.onError {
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.GEOFENCES)
                    )
                    (activity as MainActivity).analyticsUtils?.recordEvent(EventType.GEOFENCE_DELETION_FAILED, propertiesAws)
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
                if (encryptedSharedPreferences == null) {
                    encryptedSharedPreferences = EncryptedSharedPreferences(requireContext(), PREFS_NAME_AUTH).apply { initEncryptedSharedPreferences() }
                }
                val identityId = encryptedSharedPreferences?.get(PREFS_KEY_IDENTITY_ID)
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
                mIsTrackingLocationClicked = true
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

    @SuppressLint("NotifyDataSetChanged")
    private fun setMapStyleAdapter() {
        mBinding.bottomSheetMapStyle.apply {
            mViewModel.setMapListData(rvMapStyle.context, isGrabMapEnable(mPreferenceManager))
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
            layoutNoDataFound.tvNoMatchingFound.text =
                getString(R.string.label_style_search_error_title)
            layoutNoDataFound.tvMakeSureSpelledCorrect.text =
                getString(R.string.label_style_search_error_des)
            setMapTileSelection(mapName)
            rvMapStyle.layoutManager = LinearLayoutManager(requireContext())
            mMapStyleAdapter =
                MapStyleAdapter(
                    mViewModel.mStyleList,
                    object : MapStyleAdapter.MapInterface {
                        override fun mapStyleClick(
                            position: Int,
                            innerPosition: Int,
                        ) {
                            if (checkInternetConnection()) {
                                if (position != -1 && innerPosition != -1) {
                                    val selectedProvider =
                                        mViewModel.mStyleList[position].styleNameDisplay
                                    val selectedInnerData =
                                        mViewModel.mStyleList[position]
                                            .mapInnerData
                                            ?.get(
                                                innerPosition,
                                            )?.mapName
                                    for (data in mViewModel.mStyleListForFilter) {
                                        if (data.styleNameDisplay.equals(selectedProvider)) {
                                            data.mapInnerData.let {
                                                if (it != null) {
                                                    for (innerData in it) {
                                                        if (innerData.mapName.equals(selectedInnerData)) {
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
                                                it1,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                )
            rvMapStyle.adapter = mMapStyleAdapter

            rvProvider.layoutManager = LinearLayoutManager(requireContext())
            mProviderAdapter =
                SortingAdapter(
                    mViewModel.providerOptions,
                    object : SortingAdapter.MapInterface {
                        override fun mapClick(
                            position: Int,
                            isSelected: Boolean,
                        ) {
                            if (position != -1) {
                                mViewModel.providerOptions[position].isSelected = isSelected
                            }
                        }
                    },
                )
            rvProvider.adapter = mProviderAdapter

            rvAttribute.layoutManager = LinearLayoutManager(requireContext())
            mAttributeAdapter =
                SortingAdapter(
                    mViewModel.attributeOptions,
                    object : SortingAdapter.MapInterface {
                        override fun mapClick(
                            position: Int,
                            isSelected: Boolean,
                        ) {
                            if (position != -1) {
                                mViewModel.attributeOptions[position].isSelected = isSelected
                            }
                        }
                    },
                )
            rvAttribute.adapter = mAttributeAdapter

            rvType.layoutManager = LinearLayoutManager(requireContext())
            mTypeAdapter =
                SortingAdapter(
                    mViewModel.typeOptions,
                    object : SortingAdapter.MapInterface {
                        override fun mapClick(
                            position: Int,
                            isSelected: Boolean,
                        ) {
                            if (position != -1) {
                                mViewModel.typeOptions[position].isSelected = isSelected
                            }
                        }
                    },
                )
            rvType.adapter = mTypeAdapter
            if (!isGrabMapEnable(mPreferenceManager)) {
                cardGrabMap.hide()
            }
        }
    }

    private fun BottomSheetMapStyleBinding.setMapTileSelection(mapName: String) {
        val colorToSet = ContextCompat.getColor(requireContext(), R.color.color_primary_green)

        val selectedCard: MaterialCardView =
            when (mapName) {
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
            card.strokeColor =
                if (card == selectedCard) {
                    colorToSet
                } else {
                    ContextCompat.getColor(requireContext(), R.color.white)
                }
        }
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
                if (mBottomSheetHelper.isNavigationSheetVisible() && isLocationUpdatedNeeded) {
                    if (mViewModel.mNavigationResponse != null) {
                        mViewModel.mDestinationLatLng?.latitude?.let { latitude ->
                            mViewModel.mDestinationLatLng?.longitude?.let { longitude ->
                                val destinationLocation = Location("destination")
                                destinationLocation.latitude = latitude
                                destinationLocation.longitude = longitude
                                val distance = destinationLocation.distanceTo(latLng)
                                if (distance < DISTANCE_IN_METER_10) {
                                    mBottomSheetHelper.hideNavigationSheet()
                                    mBottomSheetHelper.expandNavigationCompleteSheet()
                                    mBinding.bottomSheetNavigationComplete.tvNavigationCompleteAddress.text =
                                        mViewModel.mSearchDirectionDestinationData
                                            ?.amazonLocationPlace
                                            ?.label
                                            ?.split(
                                                ",",
                                            )?.toTypedArray()
                                            ?.get(0)
                                            ?: mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.label

                                    mBinding.bottomSheetNavigationComplete.sheetNavigationCompleteTvDirectionStreet.text =
                                        getRegion(
                                            mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.region,
                                            mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.subRegion,
                                            mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.country,
                                        )

                                    mBinding.cardNavigationTimeDialog.hide()
                                    mMapHelper.removeLine()
                                    mMapHelper.removeLocationListener()
                                    mMapboxMap?.removeOnScaleListener(this@ExploreFragment)
                                } else {
                                    bearing?.let { mMapHelper.bearingCamera(it, latLng) }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        mViewModel.updateCalculateDistanceFromMode(
                                            latLng.latitude,
                                            latLng.longitude,
                                            mViewModel.mDestinationLatLng?.latitude,
                                            mViewModel.mDestinationLatLng?.longitude,
                                            mIsAvoidFerries,
                                            mIsAvoidTolls,
                                            mTravelMode,
                                        )
                                        val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
                                        val properties = listOf(
                                            Pair(AnalyticsAttribute.TRAVEL_MODE, mTravelMode),
                                            Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
                                            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
                                            Pair(AnalyticsAttribute.AVOID_FERRIES, mIsAvoidFerries.toString()),
                                            Pair(AnalyticsAttribute.AVOID_TOLLS, mIsAvoidTolls.toString())
                                        )
                                        (activity as MainActivity).analyticsUtils?.recordEvent(
                                            ROUTE_SEARCH, properties)
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
                            rvNavigationList.hide()
                        }.onSuccess {
                            clSearchLoaderNavigation.root.hide()
                            rvNavigationList.show()
                            mBinding.bottomSheetNavigation.apply {
                                tvNavigationDistance.text =
                                    it.distance
                                        ?.let { it1 ->
                                            convertToLowerUnit(
                                                it1,
                                                isMetric(
                                                    mPreferenceManager.getValue(
                                                        KEY_UNIT_SYSTEM,
                                                        "",
                                                    ),
                                                ),
                                            )
                                        }?.let { it2 ->
                                            getMetricsNew(
                                                requireContext(),
                                                it2,
                                                isMetric(
                                                    mPreferenceManager.getValue(
                                                        KEY_UNIT_SYSTEM,
                                                        "",
                                                    ),
                                                ),
                                            )
                                        }
                                tvNavigationTime.text = it.duration
                            }
                            if (it.navigationList.isNotEmpty()) {
                                it.navigationList[0].distance?.let { distance ->
                                    setNavigationTimeDialog(
                                        distance,
                                        it.navigationList[0].getAddress(),
                                    )
                                }
                            }
                            mNavigationList.clear()
                            mNavigationList.addAll(it.navigationList)
                            mNavigationAdapter?.notifyDataSetChanged()
                            if (mTravelMode == TravelMode.Walking.value) {
                                mNavigationAdapter?.setIsRounded(true)
                            } else {
                                mNavigationAdapter?.setIsRounded(false)
                            }
                            if (isLocationUpdatedNeeded) {
                                mMapHelper.setUpdateRoute(mRouteUpDate)
                                mMapboxMap?.addOnScaleListener(this@ExploreFragment)
                            }
                        }.onError { it ->
                            isLocationUpdatedNeeded = false
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
                                    clMapOptionRoute.show()
                                }
                            }
                            if (it.messageResource.toString() ==
                                resources.getString(R.string.check_your_internet_connection_and_try_again)
                            ) {
                                mBinding.apply {
                                    if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                        hideViews(
                                            bottomSheetDirectionSearch.layoutNoDataFound.root,
                                            bottomSheetDirectionSearch.layoutCardError.root,
                                            bottomSheetDirectionSearch.rvSearchPlacesDirection,
                                            bottomSheetDirectionSearch.rvSearchPlacesSuggestionDirection,
                                        )
                                        bottomSheetDirectionSearch.clNoInternetConnectionDirectionSearch.show()
                                    } else if (mBottomSheetHelper.isSearchPlaceSheetVisible()) {
                                        hideViews(
                                            bottomSheetSearch.layoutNoDataFound.root,
                                            bottomSheetSearch.nsSearchPlaces,
                                        )
                                        bottomSheetSearch.clNoInternetConnectionSearchSheet.show()
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
                                    clSearchLoaderDirectionSearch.root.show()
                                    rvSearchPlacesSuggestionDirection.hide()
                                    rvSearchPlacesDirection.hide()
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
                                        hideViews(
                                            bottomSheetDirectionSearch.layoutNoDataFound.root,
                                            bottomSheetDirectionSearch.layoutCardError.root,
                                            bottomSheetDirectionSearch.rvSearchPlacesDirection,
                                            bottomSheetDirectionSearch.rvSearchPlacesSuggestionDirection,
                                        )
                                        bottomSheetDirectionSearch.clNoInternetConnectionDirectionSearch.show()
                                    } else if (!mBottomSheetHelper.isSearchPlaceSheetVisible()) {
                                        hideViews(
                                            bottomSheetSearch.layoutNoDataFound.root,
                                            bottomSheetSearch.nsSearchPlaces,
                                        )
                                        bottomSheetSearch.clNoInternetConnectionSearchSheet.show()
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
                                    TravelMode.Walking.value -> {
                                        mBinding.bottomSheetDirectionSearch.clWalkLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clWalk.show()
                                    }

                                    TravelMode.Car.value -> {
                                        mBinding.bottomSheetDirectionSearch.clDriveLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clDrive.show()
                                    }

                                    TravelMode.Truck.value -> {
                                        mBinding.bottomSheetDirectionSearch.clTruckLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clTruck.show()
                                    }

                                    TRAVEL_MODE_BICYCLE -> {
                                        mBinding.bottomSheetDirectionSearch.clBicycleLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clBicycle.show()
                                    }

                                    TRAVEL_MODE_MOTORCYCLE -> {
                                        mBinding.bottomSheetDirectionSearch.clMotorcycleLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clMotorcycle.show()
                                    }
                                }
                                mBinding.bottomSheetDirectionSearch.clMapOptionRoute.show()
                            }
                        }
                        when (it.name) {
                            TravelMode.Car.value -> {
                                mViewModel.mCarData = it.calculateRouteResult
                                mBinding.bottomSheetDirection.apply {
                                    mViewModel.mCarData?.legs?.let { legs ->
                                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                            legs.firstOrNull()?.let { firstLeg ->
                                                firstLeg.distance.let { distance ->
                                                    tvDirectionDistance.text =
                                                        mPreferenceManager
                                                            .getValue(
                                                                KEY_UNIT_SYSTEM,
                                                                "",
                                                            ).let { unitSystem ->
                                                                val isMetric = isMetric(unitSystem)
                                                                getMetricsNew(
                                                                    requireContext(),
                                                                    convertToLowerUnit(
                                                                        distance,
                                                                        isMetric,
                                                                    ),
                                                                    isMetric,
                                                                )
                                                            }
                                                }
                                                groupDistance.show()
                                                tvDirectionDot.show()
                                                tvDirectionTime.show()
                                                tvDirectionTime.text =
                                                    getTime(
                                                        requireContext(),
                                                        firstLeg.durationSeconds,
                                                    )
                                            }
                                        }
                                        mBinding.bottomSheetDirectionSearch.apply {
                                            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                                mIsDirectionDataSet = true
                                                edtSearchDest.setText(tvDirectionAddress.text)
                                                lifecycleScope.launch {
                                                    delay(CLICK_DEBOUNCE_ENABLE)
                                                    mIsDirectionDataSet = false
                                                }
                                            } else {
                                                if (mTravelMode == TravelMode.Car.value) {
                                                    tvDriveSelected.show()
                                                    hideViews(
                                                        tvTruckSelected,
                                                        tvWalkSelected,
                                                        tvBicycleSelected,
                                                        tvMotorcycleSelected,
                                                    )
                                                    drawPolyLineOnMap(
                                                        legs,
                                                        isLineUpdate = false,
                                                        isWalk = false,
                                                        isLocationIcon = false,
                                                        sourceLatLng = it.sourceLatLng,
                                                        destinationLatLng = it.destinationLatLng,
                                                    )
                                                }
                                            }

                                            setGOButtonState(
                                                edtSearchDirection.text.toString(),
                                                cardDriveGo,
                                                clDrive,
                                            )
                                            legs.firstOrNull()?.let { firstLeg ->
                                                tvDriveDistance.text =
                                                    mPreferenceManager
                                                        .getValue(
                                                            KEY_UNIT_SYSTEM,
                                                            "",
                                                        ).let { unitSystem ->
                                                            val isMetric = isMetric(unitSystem)
                                                            getMetricsNew(
                                                                requireContext(),
                                                                convertToLowerUnit(
                                                                    firstLeg.distance,
                                                                    isMetric,
                                                                ),
                                                                isMetric,
                                                            )
                                                        }
                                                tvDriveMinute.text =
                                                    getTime(
                                                        requireContext(),
                                                        firstLeg.durationSeconds,
                                                    )
                                            }
                                        }
                                    }
                                }
                            }

                            TravelMode.Walking.value -> {
                                mViewModel.mWalkingData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mWalkingData?.legs?.let { walkingData ->
                                        setGOButtonState(
                                            edtSearchDirection.text.toString(),
                                            cardWalkGo,
                                            clWalk,
                                        )
                                        tvWalkDistance.text =
                                            mPreferenceManager
                                                .getValue(
                                                    KEY_UNIT_SYSTEM,
                                                    "",
                                                ).let { unitSystem ->
                                                    val isMetric = isMetric(unitSystem)
                                                    getMetricsNew(
                                                        requireContext(),
                                                        convertToLowerUnit(
                                                            walkingData[0].distance,
                                                            isMetric,
                                                        ),
                                                        isMetric,
                                                    )
                                                }
                                        tvWalkMinute.text =
                                            getTime(
                                                requireContext(),
                                                walkingData[0].durationSeconds,
                                            )
                                        if (mTravelMode == TravelMode.Walking.value) {
                                            tvWalkSelected.show()
                                            hideViews(
                                                tvTruckSelected,
                                                tvDriveSelected,
                                                tvBicycleSelected,
                                                tvMotorcycleSelected,
                                            )
                                            drawPolyLineOnMap(
                                                walkingData,
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

                            TravelMode.Truck.value -> {
                                mViewModel.mTruckData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mTruckData?.legs?.let { truckData ->
                                        setGOButtonState(
                                            edtSearchDirection.text.toString(),
                                            cardTruckGo,
                                            clTruck,
                                        )

                                        tvTruckDistance.text =
                                            mPreferenceManager
                                                .getValue(
                                                    KEY_UNIT_SYSTEM,
                                                    "",
                                                ).let { unitSystem ->
                                                    val isMetric = isMetric(unitSystem)
                                                    getMetricsNew(
                                                        requireContext(),
                                                        convertToLowerUnit(
                                                            truckData[0].distance,
                                                            isMetric,
                                                        ),
                                                        isMetric,
                                                    )
                                                }
                                        tvTruckMinute.text =
                                            getTime(requireContext(), truckData[0].durationSeconds)
                                        if (mTravelMode == TravelMode.Truck.value) {
                                            tvTruckSelected.show()
                                            hideViews(
                                                tvWalkSelected,
                                                tvDriveSelected,
                                                tvBicycleSelected,
                                                tvMotorcycleSelected,
                                            )
                                            drawPolyLineOnMap(
                                                truckData,
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

                            TRAVEL_MODE_BICYCLE -> {
                                mViewModel.mBicycleData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mBicycleData?.legs?.let { bicycleData ->
                                        setGOButtonState(
                                            edtSearchDirection.text.toString(),
                                            cardBicycleGo,
                                            clBicycle,
                                        )
                                        tvBicycleDistance.text =
                                            mPreferenceManager
                                                .getValue(
                                                    KEY_UNIT_SYSTEM,
                                                    "",
                                                ).let { unitSystem ->
                                                    val isMetric = isMetric(unitSystem)
                                                    getMetricsNew(
                                                        requireContext(),
                                                        convertToLowerUnit(
                                                            bicycleData[0].distance,
                                                            isMetric,
                                                        ),
                                                        isMetric,
                                                    )
                                                }
                                        tvBicycleMinute.text =
                                            getTime(
                                                requireContext(),
                                                bicycleData[0].durationSeconds,
                                            )
                                        if (mTravelMode == TRAVEL_MODE_BICYCLE) {
                                            tvBicycleSelected.show()
                                            hideViews(
                                                tvWalkSelected,
                                                tvDriveSelected,
                                                tvTruckSelected,
                                                tvMotorcycleSelected,
                                            )
                                            drawPolyLineOnMap(
                                                bicycleData,
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

                            TRAVEL_MODE_MOTORCYCLE -> {
                                mViewModel.mMotorcycleData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mMotorcycleData?.legs?.let { motorcycleData ->
                                        setGOButtonState(
                                            edtSearchDirection.text.toString(),
                                            cardMotorcycleGo,
                                            clMotorcycle,
                                        )

                                        tvMotorcycleDistance.text =
                                            mPreferenceManager
                                                .getValue(
                                                    KEY_UNIT_SYSTEM,
                                                    "",
                                                ).let { unitSystem ->
                                                    val isMetric = isMetric(unitSystem)
                                                    getMetricsNew(
                                                        requireContext(),
                                                        convertToLowerUnit(
                                                            motorcycleData[0].distance,
                                                            isMetric,
                                                        ),
                                                        isMetric,
                                                    )
                                                }
                                        tvMotorcycleMinute.text =
                                            getTime(
                                                requireContext(),
                                                motorcycleData[0].durationSeconds,
                                            )
                                        if (mTravelMode == TRAVEL_MODE_MOTORCYCLE) {
                                            tvMotorcycleSelected.show()
                                            hideViews(
                                                tvWalkSelected,
                                                tvDriveSelected,
                                                tvTruckSelected,
                                                tvBicycleSelected,
                                            )
                                            drawPolyLineOnMap(
                                                motorcycleData,
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
                    }.onError {
                        mBinding.apply {
                            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                bottomSheetDirection.apply {
                                    groupDistanceLoad.hide()
                                    groupDistance.show()
                                    tvDirectionTime.hide()
                                    tvDirectionDot.invisible()
                                    tvDirectionTime.text = ""
                                    tvDirectionDistance.text = ""
                                    mViewModel.mCarData = null
                                }
                                checkErrorDirectionDistance()
                                isCalculateDriveApiError = true
                            } else {
                                bottomSheetDirectionSearch.apply {
                                    when (it.messageResource.toString()) {
                                        TravelMode.Walking.value -> {
                                            cardWalkGo.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireContext(),
                                                    R.color.btn_go_disable,
                                                ),
                                            )
                                            showCalculateRouteAPIError(TravelMode.Walking.value)
                                            mViewModel.mWalkingData = null
                                            isCalculateWalkApiError = true
                                            mBinding.bottomSheetDirectionSearch.clWalkLoader.hide()
                                            mBinding.bottomSheetDirectionSearch.clWalk.show()
                                        }

                                        TravelMode.Car.value -> {
                                            cardDriveGo.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireContext(),
                                                    R.color.btn_go_disable,
                                                ),
                                            )
                                            showCalculateRouteAPIError(TravelMode.Car.value)
                                            isCalculateDriveApiError = true
                                            mViewModel.mCarData = null
                                            mBinding.bottomSheetDirectionSearch.clDriveLoader.hide()
                                            mBinding.bottomSheetDirectionSearch.clDrive.show()
                                        }

                                        TravelMode.Truck.value -> {
                                            cardTruckGo.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireContext(),
                                                    R.color.btn_go_disable,
                                                ),
                                            )
                                            showCalculateRouteAPIError(TravelMode.Truck.value)
                                            mViewModel.mTruckData = null
                                            isCalculateTruckApiError = true
                                            mBinding.bottomSheetDirectionSearch.clTruckLoader.hide()
                                            mBinding.bottomSheetDirectionSearch.clTruck.show()
                                        }

                                        TRAVEL_MODE_BICYCLE -> {
                                            cardBicycleGo.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireContext(),
                                                    R.color.btn_go_disable,
                                                ),
                                            )
                                            showCalculateRouteAPIError(TRAVEL_MODE_BICYCLE)
                                            mViewModel.mBicycleData = null
                                            isCalculateBicycleApiError = true
                                            mBinding.bottomSheetDirectionSearch.clBicycleLoader.hide()
                                            mBinding.bottomSheetDirectionSearch.clBicycle.show()
                                        }

                                        TRAVEL_MODE_MOTORCYCLE -> {
                                            cardMotorcycleGo.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireContext(),
                                                    R.color.btn_go_disable,
                                                ),
                                            )
                                            showCalculateRouteAPIError(TRAVEL_MODE_MOTORCYCLE)
                                            mViewModel.mMotorcycleData = null
                                            isCalculateMotorcycleApiError = true
                                            mBinding.bottomSheetDirectionSearch.clMotorcycleLoader.hide()
                                            mBinding.bottomSheetDirectionSearch.clMotorcycle.show()
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
                    .onSuccess {
                        var isWalk = false
                        when (it.name) {
                            TravelMode.Car.value -> {
                                isWalk = false
                                mViewModel.mCarData = it.calculateRouteResult
                            }

                            TravelMode.Walking.value -> {
                                isWalk = true
                                mViewModel.mWalkingData = it.calculateRouteResult
                            }

                            TravelMode.Truck.value -> {
                                isWalk = false
                                mViewModel.mTruckData = it.calculateRouteResult
                            }

                            TRAVEL_MODE_BICYCLE -> {
                                isWalk = false
                                mViewModel.mBicycleData = it.calculateRouteResult
                            }

                            TRAVEL_MODE_MOTORCYCLE -> {
                                isWalk = false
                                mViewModel.mMotorcycleData = it.calculateRouteResult
                            }
                        }

                        activity?.runOnUiThread {
                            it.calculateRouteResult?.legs?.let { it1 ->
                                drawPolyLineOnMap(
                                    it1,
                                    true,
                                    isWalk,
                                    isLocationIcon = mBottomSheetHelper.isNavigationSheetVisible(),
                                )
                            }

                            mBinding.bottomSheetNavigation.apply {
                                tvNavigationTime.text =
                                    it.calculateRouteResult?.legs?.get(0)?.durationSeconds?.let { it1 ->
                                        getTime(
                                            requireContext(),
                                            it1,
                                        )
                                    }
                                val isMetric =
                                    isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
                                tvNavigationDistance.text =
                                    it.calculateRouteResult
                                        ?.legs
                                        ?.get(0)
                                        ?.distance
                                        ?.let { it1 ->
                                            convertToLowerUnit(it1, isMetric)
                                        }?.let { it2 ->
                                            getMetricsNew(
                                                requireContext(),
                                                it2,
                                                isMetric,
                                            )
                                        }
                            }
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            it.calculateRouteResult?.legs?.get(0)?.steps?.get(0)?.let { it1 ->
                                mViewModel.getAddressFromLatLng(
                                    it.calculateRouteResult
                                        ?.legs
                                        ?.get(0)
                                        ?.startPosition
                                        ?.get(1),
                                    it.calculateRouteResult
                                        ?.legs
                                        ?.get(0)
                                        ?.startPosition
                                        ?.get(0),
                                    it1,
                                    true,
                                )
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
                            TravelMode.Car.value -> {
                                mViewModel.mCarData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mCarData?.legs?.let { legs ->
                                        tvDriveSelected.show()
                                        hideViews(
                                            tvTruckSelected,
                                            tvWalkSelected,
                                            tvBicycleSelected,
                                            tvMotorcycleSelected,
                                        )
                                        drawPolyLineOnMap(
                                            legs,
                                            isLineUpdate = false,
                                            isWalk = false,
                                            isLocationIcon = false,
                                        )
                                    }
                                }
                            }

                            TravelMode.Walking.value -> {
                                mViewModel.mWalkingData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mWalkingData?.legs?.let { walkingData ->
                                        tvWalkSelected.show()
                                        hideViews(
                                            tvTruckSelected,
                                            tvDriveSelected,
                                            tvBicycleSelected,
                                            tvMotorcycleSelected,
                                        )
                                        drawPolyLineOnMap(
                                            walkingData,
                                            isLineUpdate = false,
                                            isWalk = true,
                                            isLocationIcon = false,
                                        )
                                    }
                                }
                            }

                            TravelMode.Truck.value -> {
                                mViewModel.mTruckData = it.calculateRouteResult
                                mBinding.bottomSheetDirectionSearch.apply {
                                    mViewModel.mTruckData?.legs?.let { truckData ->
                                        tvTruckSelected.show()
                                        hideViews(
                                            tvWalkSelected,
                                            tvDriveSelected,
                                            tvBicycleSelected,
                                            tvMotorcycleSelected,
                                        )
                                        drawPolyLineOnMap(
                                            truckData,
                                            isLineUpdate = false,
                                            isWalk = false,
                                            isLocationIcon = false,
                                        )
                                    }
                                }
                            }
                        }
                        mMapHelper.getLiveLocation()?.let { mLatLng ->
                            mMapHelper.navigationZoomCamera(mLatLng, isZooming)
                        }
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
            mViewModel.mNavigationTimeDialogData.collect { handleResult ->
                handleResult
                    .onLoading {}
                    .onSuccess {
                        it.distance?.let { distance ->
                            setNavigationTimeDialog(
                                distance,
                                it.getAddress(),
                            )
                        }
                    }.onError {
                        showError(it.messageResource.toString())
                    }
            }
        }
        lifecycleScope.launch {
            withStarted { }
            mViewModel.addressLineData.collect { handleResult ->
                handleResult
                    .onLoading {}
                    .onSuccess {
                        if (!mBottomSheetHelper.isSearchPlaceSheetVisible() || mBottomSheetHelper.isDirectionSheetVisible()) {
                            val searchSuggestionData = SearchSuggestionData()
                            if (!it.searchPlaceIndexForPositionResult?.results.isNullOrEmpty()) {
                                it.searchPlaceIndexForPositionResult?.let { searchPlaceIndexForPositionResult ->
                                    searchSuggestionData.text =
                                        searchPlaceIndexForPositionResult.results[0].place?.label
                                    searchSuggestionData.searchText =
                                        searchPlaceIndexForPositionResult.results[0].place?.label
                                    searchSuggestionData.distance =
                                        searchPlaceIndexForPositionResult.results[0].distance
                                    searchSuggestionData.isDestination = true
                                    searchSuggestionData.placeId =
                                        searchPlaceIndexForPositionResult.results[0].placeId
                                    searchSuggestionData.isPlaceIndexForPosition = false

                                    searchSuggestionData.amazonLocationPlace =
                                        searchPlaceIndexForPositionResult.results[0].place
                                }
                            } else {
                                searchSuggestionData.text =
                                    String.format(STRING_FORMAT, it.latitude, it.longitude)
                                searchSuggestionData.searchText =
                                    String.format(STRING_FORMAT, it.latitude, it.longitude)
                                searchSuggestionData.distance = null
                                searchSuggestionData.isDestination = true
                                searchSuggestionData.placeId = "11"
                                searchSuggestionData.isPlaceIndexForPosition = false
                                val place = Place {
                                    label = String.format(STRING_FORMAT, it.latitude, it.longitude)
                                    geometry = PlaceGeometry {
                                        point = listOf(it.longitude!!, it.latitude!!)
                                    }
                                    addressNumber = String.format(STRING_FORMAT, it.latitude, it.longitude)
                                    street = String.format(STRING_FORMAT, it.latitude, it.longitude)
                                    country = String.format(STRING_FORMAT, it.latitude, it.longitude)
                                    region = String.format(STRING_FORMAT, it.latitude, it.longitude)
                                    subRegion = String.format(STRING_FORMAT, it.latitude, it.longitude)
                                    municipality = String.format(STRING_FORMAT, it.latitude, it.longitude)
                                    neighborhood = String.format(STRING_FORMAT, it.latitude, it.longitude)
                                    postalCode = String.format(STRING_FORMAT, it.latitude, it.longitude)
                                }
                                searchSuggestionData.amazonLocationPlace = place
                            }
                            setDirectionData(searchSuggestionData, true)
                            return@onSuccess
                        }
                        it.searchPlaceIndexForPositionResult?.let { searchPlaceIndexForPositionResult ->
                            if (searchPlaceIndexForPositionResult.results.isNotEmpty()) {
                                val label = searchPlaceIndexForPositionResult.results[0].place?.label
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
    }

    private fun showCalculateRouteAPIError(value: String) {
        if (mTravelMode == value) {
            showError(getString(R.string.no_route_found))
        }
    }

    private fun checkAllApiCallFailed() {
        mBinding.apply {
            bottomSheetDirectionSearch.apply {
                if (isGrabMapSelected(mPreferenceManager, requireContext())) {
                    if (isCalculateDriveApiError &&
                        isCalculateWalkApiError &&
                        isCalculateBicycleApiError &&
                        isCalculateMotorcycleApiError
                    ) {
                        showAllApiFailed()
                    } else {
                        hideAllApiCallFailed()
                    }
                } else {
                    if (isCalculateDriveApiError && isCalculateWalkApiError && isCalculateTruckApiError) {
                        showAllApiFailed()
                    } else {
                        hideAllApiCallFailed()
                    }
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
        val mapName =
            mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
        if (mapName == getString(R.string.map_esri)) {
            val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
            if (isMetric) {
                layoutCardError.tvCardError1.text =
                    getString(R.string.distance_is_greater_than_400_km)
            } else {
                layoutCardError.tvCardError1.text =
                    String.format(
                        getString(R.string.distance_is_greater_than_248_mi),
                        getFormatter()?.format(248.5),
                    )
            }
            layoutCardError.tvCardError2.text =
                getString(R.string.can_t_calculate_via_esri_kindly_switch_to_here_provider)
            layoutCardError.tvCardError2.show()
        } else {
            layoutCardError.tvCardError1.text = getString(R.string.no_route_found)
            layoutCardError.tvCardError2.hide()
        }
    }

    private fun BottomSheetDirectionBinding.locationErrorDirection() {
        tvDirectionError.hide()
        showViews(tvDirectionError2, ivInfo)
    }

    private fun setGOButtonState(
        source: String,
        cardView: MaterialCardView,
        cl: ConstraintLayout,
    ) {
        if (source == resources.getString(R.string.label_my_location)
        ) {
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

                    cardBicycleGo -> {
                        tvBicycleGo.text = getString(R.string.btn_go)
                    }

                    cardMotorcycleGo -> {
                        tvMotorcycleGo.text = getString(R.string.btn_go)
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

                    cardBicycleGo -> {
                        tvBicycleGo.text = getString(R.string.label_preview)
                    }

                    cardMotorcycleGo -> {
                        tvMotorcycleGo.text = getString(R.string.label_preview)
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
        region: String,
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            mBinding.apply {
                if (!mRouteFinish) cardNavigationTimeDialog.show() else cardNavigationTimeDialog.hide()
                tvDistance.text =
                    mPreferenceManager
                        .getValue(
                            KEY_UNIT_SYSTEM,
                            "",
                        ).let { unitSystem ->
                            val isMetric = isMetric(unitSystem)
                            getMetricsNew(
                                requireContext(),
                                convertToLowerUnit(distance, isMetric),
                                isMetric,
                            )
                        }
                tvNavigationName.text = region
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
        if (validateLatLng(mText) != null) {
            val mLatLng = validateLatLng(mText)
            if (it.text == (mLatLng?.latitude.toString() + "," + mLatLng?.longitude.toString())) {
                setPlaceData(it, searchPlaceIndexText)
            }
        } else if (!it.text.isNullOrEmpty() && it.text == mText) {
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
        val mText: String =
            if (!isDataSearchForDestination) {
                mBinding.bottomSheetDirectionSearch.edtSearchDirection.text
                    .toString()
                    .trim()
            } else {
                mBinding.bottomSheetDirectionSearch.edtSearchDest.text
                    .toString()
                    .trim()
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
                    if (placeData == it.amazonLocationPlace?.label) {
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
                nsDirectionSearchPlaces.show()
                layoutNoDataFound.groupNoSearchFound.hide()
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
                hideViews(rvSearchPlacesDirection, nsDirectionSearchPlaces)
                layoutNoDataFound.groupNoSearchFound.show()
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
            isGrabMapSelected(mPreferenceManager, requireContext()),
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
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun clickListener() {
        mBinding.apply {
            cardGeofenceMap.setOnClickListener {
                (activity as MainActivity).geofenceClick()
            }

            cardNavigation.setOnClickListener {
                checkLocationPermission(true)
            }

            cardMap.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < CLICK_TIME_DIFFERENCE) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                mBaseActivity?.isTablet?.let { it1 ->
                    if (it1) {
                        mapStyleBottomSheetFragment =
                            MapStyleBottomSheetFragment(
                                mViewModel,
                                mBaseActivity,
                                object : MapStyleAdapter.MapInterface {
                                    override fun mapStyleClick(
                                        position: Int,
                                        innerPosition: Int,
                                    ) {
                                        if (checkInternetConnection() && position != -1 && innerPosition != -1) {
                                            val selectedProvider =
                                                mViewModel.mStyleList[position].styleNameDisplay
                                            val selectedInnerData =
                                                mViewModel.mStyleList[position]
                                                    .mapInnerData
                                                    ?.get(
                                                        innerPosition,
                                                    )?.mapName
                                            for (data in mViewModel.mStyleListForFilter) {
                                                if (data.styleNameDisplay.equals(
                                                        selectedProvider,
                                                    )
                                                ) {
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
                                            }
                                            selectedProvider?.let { it2 ->
                                                selectedInnerData?.let { it3 ->
                                                    mapStyleChange(
                                                        false,
                                                        it2,
                                                        it3,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                            )
                        activity?.supportFragmentManager.let {
                            if (it != null) {
                                mapStyleBottomSheetFragment?.show(
                                    it,
                                    MapStyleBottomSheetFragment::class.java.name,
                                )
                            }
                        }
                    } else {
                        if (!isGrabMapEnable(mPreferenceManager)) {
                            mBinding.bottomSheetMapStyle.cardGrabMap.hide()
                        }
                        if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true) {
                            mBinding.bottomSheetMapStyle.cardGrabMap.isClickable = false
                            mBinding.bottomSheetMapStyle.cardGrabMap.setCardBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.color_img_tint),
                            )
                        } else {
                            mBinding.bottomSheetMapStyle.cardGrabMap.isClickable = true
                            mBinding.bottomSheetMapStyle.cardGrabMap.setCardBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.white),
                            )
                        }
                        mViewModel.mStyleList.forEachIndexed { index, mapStyleData ->
                            if (mapStyleData.styleNameDisplay.equals(getString(R.string.grab))) {
                                mapStyleData.isDisable =
                                    mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true
                                mMapStyleAdapter?.notifyItemChanged(index)
                            }
                        }
                        mBottomSheetHelper.halfExpandMapStyleSheet()
                        mBaseActivity?.bottomNavigationVisibility(false)
                    }
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
                    openDirectionBottomSheet()
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
            bottomSheetSearch.edtSearchPlaces.textChanges().debounce(CLICK_DEBOUNCE).onEach { text ->
                updateSearchUI(text.isNullOrEmpty())
                if (mViewModel.mIsPlaceSuggestion) {
                    if (!text.isNullOrEmpty()) {
                        searchPlaces(text.toString())
                        val properties = listOf(
                            Pair(AnalyticsAttribute.VALUE, text.toString()),
                            Pair(AnalyticsAttribute.TYPE, if (validateLatLng(text.toString()) != null) AnalyticsAttributeValue.COORDINATES else AnalyticsAttributeValue.TEXT),
                            Pair(AnalyticsAttribute.TRIGGERED_BY, PLACE_SEARCH),
                            Pair(AnalyticsAttribute.ACTION, AnalyticsAttributeValue.AUTOCOMPLETE)
                        )
                        (activity as MainActivity).analyticsUtils?.recordEvent(PLACE_SEARCH, properties)
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
                        setCarClickData()
                    }
                }

                clWalk.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mWalkingData == null) {
                            showError(getString(R.string.no_route_found))
                            return@setOnClickListener
                        }
                        mTravelMode = TravelMode.Walking.value
                        mViewModel.mWalkingData?.let {
                            tvWalkSelected.show()
                            if (mIsRouteOptionsOpened) {
                                mIsRouteOptionsOpened = false
                                changeRouteListUI()
                            }
                            cardRoutingOption.hide()
                            hideViews(
                                tvDriveSelected,
                                tvTruckSelected,
                                tvBicycleSelected,
                                tvMotorcycleSelected,
                            )
                            adjustMapBound()
                            drawPolyLineOnMapCardClick(
                                it.legs,
                                isLineUpdate = false,
                                isWalk = true,
                                isLocationIcon = false,
                            )
                        }
                        recordTravelModeChange()
                    }
                }

                clTruck.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mTruckData == null) {
                            showError(getString(R.string.no_route_found))
                            return@setOnClickListener
                        }
                        mTravelMode = TravelMode.Truck.value
                        mViewModel.mTruckData?.let {
                            tvTruckSelected.show()
                            showViews(cardRoutingOption)
                            hideViews(
                                tvDriveSelected,
                                tvWalkSelected,
                                tvBicycleSelected,
                                tvMotorcycleSelected,
                            )
                            adjustMapBound()
                            drawPolyLineOnMapCardClick(
                                it.legs,
                                isLineUpdate = false,
                                isWalk = false,
                                isLocationIcon = false,
                            )
                        }
                        recordTravelModeChange()
                    }
                }

                clBicycle.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mBicycleData == null) {
                            showError(getString(R.string.no_route_found))
                            return@setOnClickListener
                        }
                        mTravelMode = TRAVEL_MODE_BICYCLE
                        mViewModel.mBicycleData?.let {
                            tvBicycleSelected.show()
                            if (mIsRouteOptionsOpened) {
                                mIsRouteOptionsOpened = false
                                changeRouteListUI()
                            }
                            cardRoutingOption.hide()
                            hideViews(tvDriveSelected, tvWalkSelected, tvMotorcycleSelected)
                            adjustMapBound()
                            drawPolyLineOnMapCardClick(
                                it.legs,
                                isLineUpdate = false,
                                isWalk = false,
                                isLocationIcon = false,
                            )
                        }
                        recordTravelModeChange()
                    }
                }

                clMotorcycle.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mMotorcycleData == null) {
                            showError(getString(R.string.no_route_found))
                            return@setOnClickListener
                        }
                        mTravelMode = TRAVEL_MODE_MOTORCYCLE
                        mViewModel.mMotorcycleData?.let {
                            tvMotorcycleSelected.show()
                            if (mIsRouteOptionsOpened) {
                                mIsRouteOptionsOpened = false
                                changeRouteListUI()
                            }
                            cardRoutingOption.hide()
                            hideViews(tvDriveSelected, tvWalkSelected, tvBicycleSelected)
                            adjustMapBound()
                            drawPolyLineOnMapCardClick(
                                it.legs,
                                isLineUpdate = false,
                                isWalk = false,
                                isLocationIcon = false,
                            )
                        }
                        recordTravelModeChange()
                    }
                }

                cardDriveGo.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mCarData?.legs != null) {
                            mTravelMode = TravelMode.Car.value
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
                        if (mViewModel.mWalkingData?.legs != null) {
                            mTravelMode = TravelMode.Walking.value
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
                        if (mViewModel.mTruckData?.legs != null) {
                            mTravelMode = TravelMode.Truck.value
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

                cardBicycleGo.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mBicycleData?.legs != null) {
                            mTravelMode = TRAVEL_MODE_BICYCLE
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

                cardMotorcycleGo.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mMotorcycleData?.legs != null) {
                            mTravelMode = TRAVEL_MODE_MOTORCYCLE
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
                    if (checkInternetConnection() && !mIsSwapClicked && !checkDirectionLoaderVisible()) {
                        mIsSwapClicked = true
                        mLastClickTime = SystemClock.elapsedRealtime()
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
                            mIsSwapClicked = false
                        }
                    }
                }
                switchAvoidTools.setOnCheckedChangeListener { _, isChecked ->
                    if (checkInternetConnection()) {
                        mMapHelper.removeMarkerAndLine()
                        clearDirectionData()
                        mIsAvoidTolls = isChecked
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
                    if (checkInternetConnection()) {
                        mMapHelper.removeMarkerAndLine()
                        clearDirectionData()
                        mIsAvoidFerries = isChecked
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
                        isDataSearchForDestination = true
                    }
                }
                edtSearchDirection.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        isDataSearchForDestination = false
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
                                cardRoutingOption,
                                cardMapOption,
                                cardListRoutesOption,
                                layoutCardError.root,
                                clDriveLoader,
                                clWalkLoader,
                                clTruckLoader,
                            )
                            return@onEach
                        }
                        if (mBottomSheetHelper.isDirectionSearchSheetVisible() &&
                            !mIsDirectionDataSet &&
                            !mIsSwapClicked &&
                            mViewModel.mIsPlaceSuggestion
                        ) {
                            cardRouteOptionHide()
                            clearMapLineMarker()
                            mViewModel.mSearchDirectionDestinationData = null
                            searchPlaces(text.toString())
                            val properties = listOf(
                                Pair(AnalyticsAttribute.VALUE, text.toString()),
                                Pair(AnalyticsAttribute.TYPE, if (validateLatLng(text.toString()) != null) AnalyticsAttributeValue.COORDINATES else AnalyticsAttributeValue.TEXT),
                                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
                                Pair(AnalyticsAttribute.ACTION, AnalyticsAttributeValue.TO_SEARCH_AUTOCOMPLETE)
                            )
                            (activity as MainActivity).analyticsUtils?.recordEvent(PLACE_SEARCH, properties)
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
                                cardRoutingOption,
                                cardMapOption,
                                cardListRoutesOption,
                                layoutCardError.root,
                                clDriveLoader,
                                clWalkLoader,
                                clTruckLoader,
                            )
                            return@onEach
                        }
                        if (mBottomSheetHelper.isDirectionSearchSheetVisible() &&
                            !mIsDirectionDataSetNew &&
                            !mIsSwapClicked &&
                            !mIsDirectionDataSet &&
                            mViewModel.mIsPlaceSuggestion
                        ) {
                            cardRouteOptionHide()
                            clearMapLineMarker()
                            mViewModel.mSearchDirectionOriginData = null
                            searchPlaces(text.toString())
                            val properties = listOf(
                                Pair(AnalyticsAttribute.VALUE, text.toString()),
                                Pair(AnalyticsAttribute.TYPE, if (validateLatLng(text.toString()) != null) AnalyticsAttributeValue.COORDINATES else AnalyticsAttributeValue.TEXT),
                                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
                                Pair(AnalyticsAttribute.ACTION, AnalyticsAttributeValue.FROM_SEARCH_AUTOCOMPLETE)
                            )
                            (activity as MainActivity).analyticsUtils?.recordEvent(PLACE_SEARCH, properties)
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
                        mIsRouteOptionsOpened = !mIsRouteOptionsOpened
                        changeRouteListUI()
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
                bottomSheetMapStyle.ivAmazonInfoMapStyle?.setOnClickListener {
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
                        val mapName =
                            mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                        if (mapName == getString(R.string.map_esri)) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(BuildConfig.ATTRIBUTION_LEARN_MORE_ESRI_URL),
                                ),
                            )
                        } else {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(BuildConfig.ATTRIBUTION_LEARN_MORE_HERE_URL),
                                ),
                            )
                        }
                    }
                    ivBack.setOnClickListener {
                        mBottomSheetHelper.hideAttributeSheet()
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

            bottomSheetMapStyle.apply {
                ivMapStyleClose.setOnClickListener {
                    mapStyleShowList()
                    mBottomSheetHelper.hideMapStyleSheet()
                }

                etSearchMap
                    .textChanges()
                    .debounce(DELAY_300)
                    .onEach { text ->
                        mapStyleShowList()
                        searchText(text)
                        tilSearch.isEndIconVisible = !text.isNullOrEmpty()
                        val providerNames = arrayListOf<String>()
                        val attributeNames = arrayListOf<String>()
                        val typeNames = arrayListOf<String>()
                        mViewModel.providerOptions
                            .filter { it.isSelected }
                            .forEach { data -> providerNames.add(data.name) }
                        mViewModel.attributeOptions
                            .filter { it.isSelected }
                            .forEach { data -> attributeNames.add(data.name) }
                        mViewModel.typeOptions
                            .filter { it.isSelected }
                            .forEach { data -> typeNames.add(data.name) }
                        val filterList =
                            mViewModel.filterAndSortItems(
                                requireContext(),
                                text.toString().ifEmpty { null },
                                providerNames.ifEmpty { null },
                                attributeNames.ifEmpty { null },
                                typeNames.ifEmpty { null },
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
                            isNoDataFoundVisible = false
                        } else {
                            mViewModel.mStyleList.clear()
                            activity?.runOnUiThread {
                                mMapStyleAdapter?.notifyDataSetChanged()
                            }
                            isNoDataFoundVisible = true
                            layoutNoDataFound.root.show()
                            rvMapStyle.hide()
                        }
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
                    mViewModel.providerOptions
                        .filter { it.isSelected }
                        .forEach { data -> providerNames.add(data.name) }
                    mViewModel.attributeOptions
                        .filter { it.isSelected }
                        .forEach { data -> attributeNames.add(data.name) }
                    mViewModel.typeOptions
                        .filter { it.isSelected }
                        .forEach { data -> typeNames.add(data.name) }
                    val filterList =
                        mViewModel.filterAndSortItems(
                            requireContext(),
                            null,
                            providerNames.ifEmpty { null },
                            attributeNames.ifEmpty { null },
                            typeNames.ifEmpty { null },
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
                    mViewModel.providerOptions
                        .filter { it.isSelected }
                        .forEach { data -> providerNames.add(data.name) }
                    mViewModel.attributeOptions
                        .filter { it.isSelected }
                        .forEach { data -> attributeNames.add(data.name) }
                    mViewModel.typeOptions
                        .filter { it.isSelected }
                        .forEach { data -> typeNames.add(data.name) }
                    val filterList =
                        mViewModel.filterAndSortItems(
                            requireContext(),
                            etSearchMap.text
                                .toString()
                                .trim()
                                .ifEmpty { null },
                            providerNames.ifEmpty { null },
                            attributeNames.ifEmpty { null },
                            typeNames.ifEmpty { null },
                        )
                    if (providerNames.isNotEmpty() || attributeNames.isNotEmpty() || typeNames.isNotEmpty()) {
                        imgFilterSelected.show()
                        imgFilter.setColorFilter(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_primary_green,
                            ),
                        )
                        isFilterApplied = true
                        if (providerNames.isNotEmpty()) {
                            providerNames.forEach { name ->
                                mViewModel.providerOptions
                                    .filter { it.name == name }
                                    .forEach { data -> data.isApplyFilter = true }
                            }
                        }
                        if (attributeNames.isNotEmpty()) {
                            attributeNames.forEach { name ->
                                mViewModel.attributeOptions
                                    .filter { it.name == name }
                                    .forEach { data -> data.isApplyFilter = true }
                            }
                        }
                        if (typeNames.isNotEmpty()) {
                            typeNames.forEach { name ->
                                mViewModel.typeOptions
                                    .filter { it.name == name }
                                    .forEach { data -> data.isApplyFilter = true }
                            }
                        }
                        mViewModel.providerOptions
                            .filter { !it.isSelected }
                            .forEach { data -> data.isApplyFilter = false }
                        mViewModel.attributeOptions
                            .filter { !it.isSelected }
                            .forEach { data -> data.isApplyFilter = false }
                        mViewModel.typeOptions
                            .filter { !it.isSelected }
                            .forEach { data -> data.isApplyFilter = false }
                    } else {
                        clearSortingSelection()
                        notifySortingAdapter()
                        setFilterNotSelected()
                        collapseSearchMap(params)
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
                                R.color.color_primary_green,
                            ),
                        )
                    }
                }
                clSearchMapStyle.setOnClickListener {
                    if (etSearchMap.text.isNullOrEmpty() && tilSearch.isVisible && !nsvFilter.isVisible) {
                        collapseSearchMap(params)
                    }
                }
                cardEsri.setOnClickListener {
                    val mapName =
                        mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                    if (mapName != getString(R.string.esri)) {
                        mapStyleChange(
                            false,
                            getString(R.string.esri),
                            getString(R.string.map_light),
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
                            getString(R.string.map_explore),
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
                            getString(R.string.map_standard_light),
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
                            getString(R.string.map_grab_light),
                        )
                    }
                }
            }
            bottomSheetDirectionSearch.ivDirectionCloseDirectionSearch.setOnClickListener {
                lifecycleScope.launch {
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
                    if (checkInternetConnection()) {
                        if (activity?.checkLocationPermission() == true) {
                            if (isGPSEnabled(requireContext())) {
                                if (mMapHelper.isGrabSelectedAndOutsideBound) {
                                    openDirectionWithError()
                                } else {
                                    if (mViewModel.mCarData?.legs != null) {
                                        routeOption()
                                    } else {
                                        openDirectionWithError()
                                    }
                                }
                            } else {
                                mViewModel.mCarData = null
                                openDirectionWithError()
                            }
                        } else {
                            mViewModel.mCarData = null
                            openDirectionWithError()
                        }
                    }
                }
                ivInfo.setOnClickListener {
                    if (tvDirectionError2.isVisible) {
                        if (tvDirectionError2.text.equals(getString(R.string.label_location_permission_denied))) {
                            activity?.locationPermissionDialog()
                        } else if (tvDirectionError2.text.equals(getString(R.string.label_current_location_disabled))) {
                            showError(getString(R.string.label_grab_limitation))
                        }
                    }
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
                            mMapboxMap?.removeOnMapClickListener(this@ExploreFragment)
                            mBaseActivity?.mGeofenceUtils?.showAddGeofenceDefaultUI()
                        }
                        if (bottomSheetAddGeofence.edtAddGeofenceSearch.hasFocus()) {
                            val dataToSearch = text.toString().replace(", ", ",")
                            if (dataToSearch.isNotEmpty()) {
                                mGeofenceViewModel.geofenceSearchPlaceSuggestion(
                                    dataToSearch,
                                    mViewModel.mLatLng,
                                    isGrabMapSelected(mPreferenceManager, requireContext()),
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

    private fun BottomSheetDirectionSearchBinding.setCarClickData() {
        mTravelMode = TravelMode.Car.value
        mViewModel.mCarData?.let {
            tvDriveSelected.show()
            showViews(cardRoutingOption)
            hideViews(tvTruckSelected, tvWalkSelected, tvBicycleSelected, tvMotorcycleSelected)
            adjustMapBound()
            drawPolyLineOnMapCardClick(
                it.legs,
                isLineUpdate = false,
                isWalk = false,
                isLocationIcon = false
            )
        }
        recordTravelModeChange()
    }

    private fun recordTravelModeChange() {
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
            val properties = listOf(
                Pair(AnalyticsAttribute.TRAVEL_MODE, mTravelMode),
                Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE)
            )
            (activity as MainActivity).analyticsUtils?.recordEvent(
                ROUTE_OPTION_CHANGED,
                properties
            )
        }
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

    private fun BottomSheetMapStyleBinding.searchText(text: CharSequence?) {
        tilSearch.isEndIconVisible = !text.isNullOrEmpty()
        val providerNames = arrayListOf<String>()
        val attributeNames = arrayListOf<String>()
        val typeNames = arrayListOf<String>()
        mViewModel.providerOptions
            .filter { it.isSelected }
            .forEach { data -> providerNames.add(data.name) }
        mViewModel.attributeOptions
            .filter { it.isSelected }
            .forEach { data -> attributeNames.add(data.name) }
        mViewModel.typeOptions
            .filter { it.isSelected }
            .forEach { data -> typeNames.add(data.name) }
        val filterList =
            mViewModel.filterAndSortItems(
                requireContext(),
                text.toString().ifEmpty { null },
                providerNames.ifEmpty { null },
                attributeNames.ifEmpty { null },
                typeNames.ifEmpty { null },
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
                R.color.color_img_tint,
            ),
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

    private fun BottomSheetMapStyleBinding.collapseSearchMap(params: ViewGroup.LayoutParams?) {
        if (!isFilterApplied) {
            imgFilter.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_img_tint,
                ),
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
            cardOpenData.show()
        } else {
            groupFilterButton.show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifySortingAdapter() {
        mTypeAdapter?.notifyDataSetChanged()
        mAttributeAdapter?.notifyDataSetChanged()
        mProviderAdapter?.notifyDataSetChanged()
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

    private fun checkSimulationDisableForGrab() {
        mViewModel.mStyleList.forEachIndexed { _, mapStyleData ->
            if (mapStyleData.styleNameDisplay.equals(getString(R.string.grab))) {
                mapStyleData.isDisable =
                    mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true
            }
        }
    }

    private fun BottomSheetMapStyleBinding.mapStyleShowSorting() {
        showViews(
            nsvFilter,
            viewDivider,
            tvClearSelection,
            btnApplyFilter,
        )
        layoutNoDataFound.root.hide()
        tvClearFilter.hide()
        rvMapStyle.hide()
        mBottomSheetHelper.expandMapStyleSheet()
    }

    private fun BottomSheetMapStyleBinding.mapStyleShowList() {
        hideViews(
            nsvFilter,
            viewDivider,
            tvClearSelection,
            btnApplyFilter,
        )
        rvMapStyle.show()
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

    fun setAttributionDataAndExpandSheet() {
        setAttributionData()
        mBottomSheetHelper.expandAttributeSheet()
    }

    private fun setAttributionData() {
        mBinding.apply {
            bottomSheetAttribution.apply {
                tvAttribution.text =
                    mPreferenceManager
                        .getValue(MAP_STYLE_ATTRIBUTION, getString(R.string.esri))
                        ?.replace(Regex(attributionPattern), "") ?: ""
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun openDirectionWithError() {
        mIsAvoidTolls = mPreferenceManager.getValue(KEY_AVOID_TOLLS, false)
        mIsAvoidFerries = mPreferenceManager.getValue(KEY_AVOID_FERRIES, false)
        mBinding.bottomSheetDirectionSearch.apply {
            clearDirectionData()
            switchAvoidTools.isChecked = mIsAvoidTolls
            switchAvoidFerries.isChecked = mIsAvoidFerries
            tvDriveGo.text = getString(R.string.btn_go)
            mIsDirectionDataSet = true
            if (mViewModel.mCarData?.legs == null) {
                edtSearchDest.setText(
                    mBinding.bottomSheetDirection.tvDirectionAddress.text
                        .trim(),
                )
            }
            lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_ENABLE)
                mIsDirectionDataSet = false
            }
            hideViews(
                rvSearchPlacesSuggestionDirection,
                rvSearchPlacesDirection,
                clMyLocation.root,
                clSearchLoaderDirectionSearch.root,
                clDriveLoader,
                cardRoutingOption,
                cardMapOption,
                clTruckLoader,
                clWalkLoader,
                clDrive,
            )
            clTruck.invisible()
            clWalk.invisible()
            mPlaceList.clear()
            mAdapterDirection?.notifyDataSetChanged()
            mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
        }
        openDirectionSearch()
    }

    private fun BottomSheetDirectionSearchBinding.checkRouteValidation() {
        var originLatLng: LatLng? = null
        var destinationLatLng: LatLng? = null
        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
            originLatLng = mMapHelper.getLiveLocation()
            mViewModel.mSearchDirectionDestinationData?.let {
                destinationLatLng =
                    it.amazonLocationPlace?.geometry?.point?.get(1)?.let { it1 ->
                        it.amazonLocationPlace?.geometry?.point?.get(0)?.let { it2 ->
                            LatLng(
                                it1,
                                it2,
                            )
                        }
                    }
            }
        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
            destinationLatLng = mMapHelper.getLiveLocation()
            mViewModel.mSearchDirectionOriginData?.let {
                originLatLng =
                    it.amazonLocationPlace?.geometry?.point?.get(1)?.let { it1 ->
                        it.amazonLocationPlace?.geometry?.point?.get(0)?.let { it2 ->
                            LatLng(
                                it1,
                                it2,
                            )
                        }
                    }
            }
        } else if (!edtSearchDirection.text.isNullOrEmpty() && !edtSearchDest.text.isNullOrEmpty()) {
            mViewModel.mSearchDirectionOriginData?.let {
                originLatLng =
                    it.amazonLocationPlace?.geometry?.point?.get(1)?.let { it1 ->
                        it.amazonLocationPlace?.geometry?.point?.get(0)?.let { it2 ->
                            LatLng(
                                it1,
                                it2,
                            )
                        }
                    }
            }
            mViewModel.mSearchDirectionDestinationData?.let {
                destinationLatLng =
                    it.amazonLocationPlace?.geometry?.point?.get(1)?.let { it1 ->
                        it.amazonLocationPlace?.geometry?.point?.get(0)?.let { it2 ->
                            LatLng(
                                it1,
                                it2,
                            )
                        }
                    }
            }
        }
        checkErrorDistance(originLatLng, destinationLatLng)
    }

    private fun checkErrorDirectionDistance() {
        if (activity?.checkLocationPermission() == true) {
            if (isGPSEnabled(requireContext())) {
                var destinationLatLng: LatLng? = null
                val originLatLng: LatLng? = mMapHelper.getLiveLocation()
                mViewModel.mSearchSuggestionData?.let {
                    destinationLatLng =
                        it.amazonLocationPlace?.geometry?.point?.get(1)?.let { it1 ->
                            it.amazonLocationPlace?.geometry?.point?.get(0)?.let { it2 ->
                                LatLng(
                                    it1,
                                    it2,
                                )
                            }
                        }
                }
                originLatLng?.let {
                    val distance =
                        destinationLatLng?.latitude?.let { it1 ->
                            destinationLatLng?.longitude?.let { it2 ->
                                mAWSLocationHelper.getDistance(
                                    it,
                                    it1,
                                    it2,
                                )
                            }
                        }
                    if (distance != null) {
                        val mapName =
                            mPreferenceManager.getValue(
                                KEY_MAP_NAME,
                                getString(R.string.map_esri),
                            )
                        mBinding.bottomSheetDirection.tvDirectionError.show()
                        hideViews(
                            mBinding.bottomSheetDirection.tvDirectionError2,
                            mBinding.bottomSheetDirection.ivInfo,
                        )
                        if (mapName == getString(R.string.map_esri)) {
                            val isMetric =
                                isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
                            val canEsriShowRoute =
                                if (isMetric) {
                                    (distance / 1000) < 400
                                } else {
                                    (distance / 5280) < 248.5
                                }
                            if (canEsriShowRoute) {
                                mBinding.bottomSheetDirection.tvDirectionError.text =
                                    getString(R.string.error_route)
                            } else {
                                mBinding.bottomSheetDirection.tvDirectionDistance.text =
                                    getMetricsNew(requireContext(), distance, isMetric)
                                if (isMetric) {
                                    mBinding.bottomSheetDirection.tvDirectionError.text =
                                        getString(R.string.error_switch_to_here)
                                } else {
                                    mBinding.bottomSheetDirection.tvDirectionError.text =
                                        String.format(
                                            getString(R.string.error_switch_to_here_miles),
                                            getFormatter()?.format(248.5),
                                        )
                                }
                            }
                        } else {
                            mBinding.bottomSheetDirection.tvDirectionError.text =
                                getString(R.string.error_route)
                        }
                    } else {
                        mBinding.bottomSheetDirection.tvDirectionError.text =
                            getString(R.string.error_route)
                    }
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

    private fun checkErrorDistance(
        originLatLng: LatLng?,
        destinationLatLng: LatLng?,
    ) {
        originLatLng?.let {
            val distance =
                destinationLatLng?.latitude?.let { it1 ->
                    destinationLatLng.longitude.let { it2 ->
                        mAWSLocationHelper.getDistance(
                            it,
                            it1,
                            it2,
                        )
                    }
                }
            if (distance != null) {
                val mapName =
                    mPreferenceManager.getValue(
                        KEY_MAP_NAME,
                        getString(R.string.map_esri),
                    )
                if (mapName == getString(R.string.map_esri)) {
                    if (distance < 400) {
                        showError(getString(R.string.no_route_found))
                    } else {
                        val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
                        if (isMetric) {
                            showError(getString(R.string.error_distance_400))
                        } else {
                            showError(
                                String.format(
                                    getString(R.string.error_distance_248_miles),
                                    getFormatter()?.format(248.5),
                                    getFormatter()?.format(24.85),
                                ),
                            )
                        }
                    }
                } else {
                    showError(getString(R.string.no_route_found))
                }
            } else {
                showError(getString(R.string.no_route_found))
            }
        }
    }

    private fun getFormatter(): NumberFormat? {
        val formatter =
            NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                maximumFractionDigits = 1
            }
        return formatter
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
        mIsAvoidTolls = mPreferenceManager.getValue(KEY_AVOID_TOLLS, false)
        mIsAvoidFerries = mPreferenceManager.getValue(KEY_AVOID_FERRIES, false)
        cardDirection.hide()
        bottomSheetDirectionSearch.clSearchLoaderDirectionSearch.root.hide()
        bottomSheetSearch.edtSearchPlaces.setText("")
        bottomSheetSearch.edtSearchPlaces.clearFocus()
        mBaseActivity?.bottomNavigationVisibility(false)
        mBottomSheetHelper.hideSearchBottomSheet(true)
        mBottomSheetHelper.hideDirectionSheet()
        bottomSheetDirectionSearch.apply {
            mIsDirectionDataSet = true
            edtSearchDest.setText("")
            lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_ENABLE)
                mIsDirectionDataSet = false
            }
            hideViews(
                cardListRoutesOption,
                cardRoutingOption,
                cardMapOption,
            )
            if (mMapHelper.isGrabSelectedAndOutsideBound) {
                clMyLocation.root.hide()
            } else {
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
            }
            switchAvoidTools.isChecked = mIsAvoidTolls
            switchAvoidFerries.isChecked = mIsAvoidFerries
            mPlaceList.clear()
            mAdapterDirection?.notifyDataSetChanged()
            mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
        }
        mBottomSheetHelper.expandDirectionSearchSheet(this@ExploreFragment)
        mIsDirectionSheetHalfExpanded = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun routeOption() {
        mIsAvoidTolls = mPreferenceManager.getValue(KEY_AVOID_TOLLS, false)
        mIsAvoidFerries = mPreferenceManager.getValue(KEY_AVOID_FERRIES, false)
        if (mViewModel.mCarData?.legs != null) {
            mIsDirectionDataSetNew = true
            mViewModel.mCarData?.legs?.let { legs ->
                adjustMapBound()
                drawPolyLineOnMap(
                    legs,
                    isLineUpdate = false,
                    isWalk = false,
                    isLocationIcon = false,
                )
                mBinding.bottomSheetDirectionSearch.apply {
                    clearTruckAndWalkData()
                    tvDriveGo.text = getString(R.string.btn_go)
                    switchAvoidTools.isChecked = mIsAvoidTolls
                    switchAvoidFerries.isChecked = mIsAvoidFerries
                    edtSearchDirection.setText(getString(R.string.label_my_location))
                    if (isGrabMapSelected(mPreferenceManager, requireContext())) {
                        showViews(
                            cardRoutingOption,
                            cardMapOption,
                            clDrive,
                            clBicycleLoader,
                            clMotorcycleLoader,
                            clWalkLoader,
                            viewDividerOptionBicycle,
                            viewDividerOptionMotorcycle,
                        )
                        hideViews(
                            rvSearchPlacesSuggestionDirection,
                            rvSearchPlacesDirection,
                            clMyLocation.root,
                            clSearchLoaderDirectionSearch.root,
                            clDriveLoader,
                            clTruckLoader,
                            viewDividerOptionTruck,
                        )
                        clTruck.hide()
                        clWalk.invisible()
                        clBicycle.invisible()
                        clMotorcycle.invisible()
                    } else {
                        showViews(
                            cardRoutingOption,
                            cardMapOption,
                            clTruckLoader,
                            clWalkLoader,
                            clDrive,
                            viewDividerOptionTruck,
                        )
                        hideViews(
                            rvSearchPlacesSuggestionDirection,
                            rvSearchPlacesDirection,
                            clMyLocation.root,
                            clSearchLoaderDirectionSearch.root,
                            clDriveLoader,
                            clBicycleLoader,
                            clMotorcycleLoader,
                            viewDividerOptionBicycle,
                            viewDividerOptionMotorcycle,
                        )
                        clTruck.invisible()
                        clWalk.invisible()
                        clBicycle.hide()
                        clMotorcycle.hide()
                    }
                    layoutCardError.groupCardErrorNoSearchFound.hide()
                    layoutCardError.root.hide()
                    mPlaceList.clear()
                    mAdapterDirection?.notifyDataSetChanged()
                    mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
                }
                openDirectionSearch()
                val liveLocationLatLng = mMapHelper.getLiveLocation()
                isCalculateWalkApiError = false
                isCalculateTruckApiError = false
                isCalculateBicycleApiError = false
                isCalculateMotorcycleApiError = false
                if (isGrabMapSelected(mPreferenceManager, requireContext())) {
                    mViewModel.calculateGrabDistance(
                        latitude = liveLocationLatLng?.latitude,
                        longitude = liveLocationLatLng?.longitude,
                        latDestination =
                            mViewModel.mSearchSuggestionData?.amazonLocationPlace?.geometry?.point?.get(
                                1,
                            ),
                        lngDestination =
                            mViewModel.mSearchSuggestionData?.amazonLocationPlace?.geometry?.point?.get(
                                0,
                            ),
                        isAvoidFerries = mIsAvoidFerries,
                        isAvoidTolls = mIsAvoidTolls,
                    )
                    recordEventForAllGrabMode()
                } else {
                    mViewModel.calculateDistance(
                        latitude = liveLocationLatLng?.latitude,
                        longitude = liveLocationLatLng?.longitude,
                        latDestination =
                            mViewModel.mSearchSuggestionData?.amazonLocationPlace?.geometry?.point?.get(
                                1,
                            ),
                        lngDestination =
                            mViewModel.mSearchSuggestionData?.amazonLocationPlace?.geometry?.point?.get(
                                0,
                            ),
                        isAvoidFerries = mIsAvoidFerries,
                        isAvoidTolls = mIsAvoidTolls,
                        isWalkingAndTruckCall = true,
                    )
                    recordEventForAllMode(isWalkingAndTruckCall = true)
                }
            }
            lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_ENABLE)
                mIsDirectionDataSetNew = false
            }
        } else {
            showError(getString(R.string.no_route_found))
        }
    }

    private fun recordEventForAllGrabMode() {
        val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
        val propertiesWalk = listOf(
            Pair(AnalyticsAttribute.TRAVEL_MODE, TravelMode.Walking.value),
            Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
            Pair(AnalyticsAttribute.AVOID_FERRIES, mIsAvoidFerries.toString()),
            Pair(AnalyticsAttribute.AVOID_TOLLS, mIsAvoidTolls.toString())
        )
        val propertiesBicycle = listOf(
            Pair(AnalyticsAttribute.TRAVEL_MODE, TRAVEL_MODE_BICYCLE),
            Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
            Pair(AnalyticsAttribute.AVOID_FERRIES, mIsAvoidFerries.toString()),
            Pair(AnalyticsAttribute.AVOID_TOLLS, mIsAvoidTolls.toString())
        )
        val propertiesMotorCycle = listOf(
            Pair(AnalyticsAttribute.TRAVEL_MODE, TRAVEL_MODE_MOTORCYCLE),
            Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
            Pair(AnalyticsAttribute.AVOID_FERRIES, mIsAvoidFerries.toString()),
            Pair(AnalyticsAttribute.AVOID_TOLLS, mIsAvoidTolls.toString())
        )
        (activity as MainActivity).analyticsUtils?.recordEvent(ROUTE_SEARCH, propertiesWalk)
        (activity as MainActivity).analyticsUtils?.recordEvent(ROUTE_SEARCH, propertiesBicycle)
        (activity as MainActivity).analyticsUtils?.recordEvent(ROUTE_SEARCH, propertiesMotorCycle)
    }

    private fun recordEventForAllMode(isWalkingAndTruckCall: Boolean) {
        val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
        val propertiesCar = listOf(
            Pair(AnalyticsAttribute.TRAVEL_MODE, TravelMode.Car.value),
            Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
            Pair(AnalyticsAttribute.AVOID_FERRIES, mIsAvoidFerries.toString()),
            Pair(AnalyticsAttribute.AVOID_TOLLS, mIsAvoidTolls.toString())
        )
        val propertiesTruck = listOf(
            Pair(AnalyticsAttribute.TRAVEL_MODE, TravelMode.Truck.value),
            Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
            Pair(AnalyticsAttribute.AVOID_FERRIES, mIsAvoidFerries.toString()),
            Pair(AnalyticsAttribute.AVOID_TOLLS, mIsAvoidTolls.toString())
        )
        val propertiesWalk = listOf(
            Pair(AnalyticsAttribute.TRAVEL_MODE, TravelMode.Walking.value),
            Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.ROUTE_MODULE),
            Pair(AnalyticsAttribute.AVOID_FERRIES, mIsAvoidFerries.toString()),
            Pair(AnalyticsAttribute.AVOID_TOLLS, mIsAvoidTolls.toString())
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
        mIsDirectionSheetHalfExpanded = true
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
                when (mTravelMode) {
                    TravelMode.Car.value -> mViewModel.mCarData
                    TravelMode.Walking.value -> mViewModel.mWalkingData
                    TravelMode.Truck.value -> mViewModel.mTruckData
                    TRAVEL_MODE_BICYCLE -> mViewModel.mBicycleData
                    TRAVEL_MODE_MOTORCYCLE -> mViewModel.mMotorcycleData
                    else -> mViewModel.mCarData
                }
            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                mData?.let {
                    drawPolyLineOnMap(
                        it.legs,
                        isLineUpdate = false,
                        isWalk = mTravelMode == TravelMode.Walking.value,
                        isLocationIcon = true,
                    )
                    mMapHelper.getLiveLocation()?.let { mLatLng ->
                        mMapHelper.navigationZoomCamera(mLatLng, isZooming)
                    }
                    mMapHelper.clearOriginMarker()
                    isLocationUpdatedNeeded = true
                    fetchAddressFromLatLng(it)
                }
            } else {
                isLocationUpdatedNeeded = false
                mViewModel.mSearchDirectionOriginData?.let {
                    it.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(1)
                        ?.let { it1 ->
                            it.amazonLocationPlace?.geometry?.point?.get(0)?.let { it2 ->
                                LatLng(
                                    it1,
                                    it2,
                                )
                            }
                        }?.let { it2 -> mMapHelper.navigationZoomCamera(it2, isZooming) }
                }
                mData?.let {
                    drawPolyLineOnMapCardClick(
                        it.legs,
                        isLineUpdate = false,
                        isWalk = mTravelMode == TravelMode.Walking.value,
                        isLocationIcon = true,
                    )
                    fetchAddressFromLatLng(it)
                }
            }
        }
    }

    private fun directionMyLocation() {
        mBinding.bottomSheetDirectionSearch.apply {
            mIsDirectionDataSet = true
            if (!isDataSearchForDestination) {
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
                    if (mMapHelper.isGrabSelectedAndOutsideBound) {
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
                ?.amazonLocationPlace
                ?.geometry
                ?.point
                ?.get(1)
                ?.let {
                    mViewModel.mSearchDirectionDestinationData
                        ?.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(
                            0,
                        )?.let { it1 ->
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
            mViewModel.mSearchDirectionOriginData
                ?.amazonLocationPlace
                ?.geometry
                ?.point
                ?.get(1)
                ?.let {
                    mViewModel.mSearchDirectionOriginData
                        ?.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(
                            0,
                        )?.let { it1 ->
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
        }

        if (mViewModel.mSearchDirectionOriginData != null) {
            mViewModel.mSearchDirectionOriginData
                ?.amazonLocationPlace
                ?.geometry
                ?.point
                ?.get(1)
                ?.let {
                    mViewModel.mSearchDirectionOriginData
                        ?.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(
                            0,
                        )?.let { it1 ->
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
        }
        mMapHelper.getLiveLocation()?.let { it1 -> latLngList.add(it1) }
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
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearNavigationSheetData() {
        mBinding.bottomSheetNavigation.apply {
            tvNavigationDistance.text = ""
            tvNavigationTime.text = ""
        }
        mMapHelper.addLiveLocationMarker(false)
        mBinding.tvDistance.text = ""
        mBinding.tvNavigationName.text = ""
        mBinding.bottomSheetDirectionSearch.apply {
            setCarClickData()
        }
        mRouteFinish = true
        mNavigationList.clear()
        mMapHelper.removeLocationListener()
        mMapboxMap?.removeOnScaleListener(this)
        mBinding.cardNavigationTimeDialog.hide()
        mNavigationAdapter?.notifyDataSetChanged()
        mBottomSheetHelper.hideNavigationSheet()
    }

    private fun drawPolyLineOnMapCardClick(
        legs: List<Leg>,
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
        legs[0].geometry?.lineString?.let {
            for (data in it) {
                lineString.add(fromLngLat(data[0], data[1]))
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
        legs: List<Leg>,
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
                mMapHelper
                    .getLiveLocation()
                    ?.longitude
                    ?.let {
                        mMapHelper.getLiveLocation()?.latitude?.let { it1 ->
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
            }
        }
        legs[0].geometry?.lineString?.let {
            if (isDestination) {
                val lastLeg = it[it.size - 1]
                dotStartPoint.add(fromLngLat(lastLeg[0], lastLeg[1]))
            } else {
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
        legs[0].geometry?.lineString?.let {
            if (isDestination) {
                dotDestinationPoint.add(
                    fromLngLat(
                        it[0][0],
                        it[0][1],
                    ),
                )
            } else {
                val lastLeg = it[it.size - 1]
                dotDestinationPoint.add(fromLngLat(lastLeg[0], lastLeg[1]))
            }
        }
    }

    private fun drawPolyLineOnMap(
        legs: List<Leg>,
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
        for (data in legs[0].geometry?.lineString!!) {
            lineString.add(fromLngLat(data[0], data[1]))
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

    private fun fetchAddressFromLatLng(it: CalculateRouteResponse) {
        mRouteFinish = false
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
        if (mIsRouteOptionsOpened) {
            ivUp.show()
            cardRoutingOption.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white),
            )
            ivDown.hide()
            cardListRoutesOption.show()
            cardRoutingOption.shapeAppearanceModel =
                cardRoutingOption.shapeAppearanceModel
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, 12f)
                    .setTopRightCorner(CornerFamily.ROUNDED, 12f)
                    .setBottomRightCornerSize(0F)
                    .setBottomLeftCornerSize(0F)
                    .build()
            cardListRoutesOption.shapeAppearanceModel =
                cardListRoutesOption.shapeAppearanceModel
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, 12f)
                    .setBottomRightCorner(CornerFamily.ROUNDED, 12f)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, 12f)
                    .setTopRightCornerSize(0f)
                    .build()
        } else {
            routeOptionClose()
        }
    }

    private fun BottomSheetDirectionSearchBinding.routeOptionClose() {
        mIsRouteOptionsOpened = false
        ivDown.show()
        cardRoutingOption.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_route_option_unselected,
            ),
        )
        hideViews(cardListRoutesOption, ivUp)
        cardRoutingOption.radius =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8f,
                requireContext().resources.displayMetrics,
            )
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
        tvBicycleMinute.text = ""
        tvBicycleDistance.text = ""
        tvMotorcycleMinute.text = ""
        tvMotorcycleDistance.text = ""
    }

    private fun clearMapLineMarker() {
        mMapHelper.removeLine()
        mMapHelper.clearMarker()
    }

    fun hideDirectionBottomSheet() {
        mIsDirectionDataSet = true
        isDataSearchForDestination = false
        mBinding.bottomSheetDirectionSearch.edtSearchDirection.setText("")
        mBinding.bottomSheetDirectionSearch.edtSearchDest.setText("")
        clearDirectionBottomSheet()
        mIsDirectionDataSet = false
        mViewModel.mIsPlaceSuggestion = true
        mBinding.bottomSheetDirectionSearch.apply {
            routeOptionClose()
        }
        activity?.hideKeyboard()
        lifecycleScope.launch {
            delay(CLICK_DEBOUNCE_ENABLE)
            mIsDirectionDataSet = false
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
            mTravelMode = TravelMode.Car.value
            mBinding.bottomSheetDirectionSearch.apply {
                tvDriveSelected.show()
                hideViews(
                    tvTruckSelected,
                    tvWalkSelected,
                    tvBicycleSelected,
                    tvMotorcycleSelected,
                    layoutCardError.root,
                    layoutNoDataFound.groupNoSearchFound,
                )
            }
            mBinding.bottomSheetDirection.apply {
                tvDirectionError.invisible()
            }
            showViews(
                mBinding.cardMap,
            )
            if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() != true && mBaseActivity?.mTrackingUtils?.isChangeDataProviderClicked != true && mBaseActivity?.mGeofenceUtils?.isChangeDataProviderClicked != true) {
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
            if (!mBottomSheetHelper.isMapStyleVisible() && (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() != true && mBaseActivity?.mTrackingUtils?.isChangeDataProviderClicked != true && mBaseActivity?.mGeofenceUtils?.isChangeDataProviderClicked != true)) {
                mBaseActivity?.bottomNavigationVisibility(true)
                mBottomSheetHelper.hideSearchBottomSheet(false)
            }
            mBottomSheetHelper.hideDirectionSheet()
            mViewModel.mSearchSuggestionData = null
            mMapHelper.getLiveLocation()?.let { it1 ->
                mMapHelper.moveCameraToLocation(it1)
            }
            mBaseActivity?.isTablet?.let {
                if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true  || mBaseActivity?.mTrackingUtils?.isChangeDataProviderClicked == true || mBaseActivity?.mGeofenceUtils?.isChangeDataProviderClicked == true) {
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
                    object : SearchPlacesAdapter.SearchPlaceInterface {
                        override fun placeClick(position: Int) {
                            if (checkInternetConnection()) {
                                mIsDirectionDataSet = true
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
                    object : SearchPlacesSuggestionAdapter.SearchPlaceSuggestionInterface {
                        override fun suggestedPlaceClick(position: Int) {
                            if (checkInternetConnection()) {
                                mViewModel.mIsPlaceSuggestion = false
                                mIsDirectionDataSet = true
                                if (!isDataSearchForDestination) {
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
                                    mIsDirectionDataSet = false
                                }
                                if (mPlaceList[position].placeId.isNullOrEmpty()) {
                                    mPlaceList[position].text?.let {
                                        mViewModel.searchPlaceIndexForText(
                                            it,
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
        mIsDirectionDataSet = true
        activity?.hideKeyboard()
        changeRouteListUI()
        mPlaceList[position].let {
            if (!isDataSearchForDestination) {
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
            mIsDirectionDataSet = false
            mViewModel.mIsPlaceSuggestion = true
        }
    }

    private fun BottomSheetDirectionSearchBinding.showOriginToDestinationRoute() {
        clearDirectionData()
        setApiError()
        mViewModel.calculateDistance(
            latitude =
                mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.geometry?.point?.get(
                    1,
                ),
            longitude =
                mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.geometry?.point?.get(
                    0,
                ),
            latDestination =
                mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.geometry?.point?.get(
                    1,
                ),
            lngDestination =
                mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.geometry?.point?.get(
                    0,
                ),
            isAvoidFerries = mIsAvoidFerries,
            isAvoidTolls = mIsAvoidTolls,
            isWalkingAndTruckCall = false,
        )
        recordEventForAllMode(isWalkingAndTruckCall = false)
        if (isGrabMapSelected(mPreferenceManager, requireContext())) {
            mViewModel.calculateGrabDistance(
                latitude =
                    mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.geometry?.point?.get(
                        1,
                    ),
                longitude =
                    mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.geometry?.point?.get(
                        0,
                    ),
                latDestination =
                    mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.geometry?.point?.get(
                        1,
                    ),
                lngDestination =
                    mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.geometry?.point?.get(
                        0,
                    ),
                isAvoidFerries = mIsAvoidFerries,
                isAvoidTolls = mIsAvoidTolls,
            )
            recordEventForAllGrabMode()
        } else {
            mViewModel.calculateDistance(
                latitude =
                    mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.geometry?.point?.get(
                        1,
                    ),
                longitude =
                    mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.geometry?.point?.get(
                        0,
                    ),
                latDestination =
                    mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.geometry?.point?.get(
                        1,
                    ),
                lngDestination =
                    mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.geometry?.point?.get(
                        0,
                    ),
                isAvoidFerries = mIsAvoidFerries,
                isAvoidTolls = mIsAvoidTolls,
                isWalkingAndTruckCall = true,
            )
            recordEventForAllMode(isWalkingAndTruckCall = true)
        }
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
            mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
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
        isCalculateDriveApiError = false
        isCalculateWalkApiError = false
        isCalculateTruckApiError = false
    }

    private fun BottomSheetDirectionSearchBinding.showCurrentLocationDestinationRoute(it: SearchSuggestionData) {
        setApiError()
        clearDirectionData()
        val liveLocationLatLng: LatLng? =
            if (isRunningTestLiveLocation || isRunningTest2LiveLocation || isRunningTest3LiveLocation) {
                LatLng(22.995545, 72.534031)
            } else {
                mMapHelper.getLiveLocation()
            }
        mViewModel.calculateDistance(
            latitude = liveLocationLatLng?.latitude,
            longitude = liveLocationLatLng?.longitude,
            latDestination =
                it.amazonLocationPlace
                    ?.geometry
                    ?.point
                    ?.get(1),
            lngDestination =
                it.amazonLocationPlace
                    ?.geometry
                    ?.point
                    ?.get(0),
            isAvoidFerries = mIsAvoidFerries,
            isAvoidTolls = mIsAvoidTolls,
            isWalkingAndTruckCall = false,
        )
        recordEventForAllMode(isWalkingAndTruckCall = false)
        if (isGrabMapSelected(mPreferenceManager, requireContext())) {
            mViewModel.calculateGrabDistance(
                latitude = liveLocationLatLng?.latitude,
                longitude = liveLocationLatLng?.longitude,
                latDestination =
                    it.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(1),
                lngDestination =
                    it.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(0),
                isAvoidFerries = mIsAvoidFerries,
                isAvoidTolls = mIsAvoidTolls,
            )
            recordEventForAllGrabMode()
        } else {
            mViewModel.calculateDistance(
                latitude = liveLocationLatLng?.latitude,
                longitude = liveLocationLatLng?.longitude,
                latDestination =
                    it.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(1),
                lngDestination =
                    it.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(0),
                isAvoidFerries = mIsAvoidFerries,
                isAvoidTolls = mIsAvoidTolls,
                isWalkingAndTruckCall = true,
            )
            recordEventForAllMode(isWalkingAndTruckCall = true)
        }

        showDirectionSearchShimmer()
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            mMapHelper.addMarker(requireActivity(), MarkerEnum.DIRECTION_ICON, it)
            mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
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
        val liveLocationLatLng = mMapHelper.getLiveLocation()
        mViewModel.calculateDistance(
            latitude =
                it.amazonLocationPlace
                    ?.geometry
                    ?.point
                    ?.get(1),
            longitude =
                it.amazonLocationPlace
                    ?.geometry
                    ?.point
                    ?.get(0),
            latDestination = liveLocationLatLng?.latitude,
            lngDestination = liveLocationLatLng?.longitude,
            isAvoidFerries = mIsAvoidFerries,
            isAvoidTolls = mIsAvoidTolls,
            isWalkingAndTruckCall = false,
        )
        recordEventForAllMode(isWalkingAndTruckCall = false)
        if (isGrabMapSelected(mPreferenceManager, requireContext())) {
            mViewModel.calculateGrabDistance(
                latitude =
                    it.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(1),
                longitude =
                    it.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(0),
                latDestination = liveLocationLatLng?.latitude,
                lngDestination = liveLocationLatLng?.longitude,
                isAvoidFerries = mIsAvoidFerries,
                isAvoidTolls = mIsAvoidTolls,
            )
            recordEventForAllGrabMode()
        } else {
            mViewModel.calculateDistance(
                latitude =
                    it.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(1),
                longitude =
                    it.amazonLocationPlace
                        ?.geometry
                        ?.point
                        ?.get(0),
                latDestination = liveLocationLatLng?.latitude,
                lngDestination = liveLocationLatLng?.longitude,
                isAvoidFerries = mIsAvoidFerries,
                isAvoidTolls = mIsAvoidTolls,
                isWalkingAndTruckCall = true,
            )
            recordEventForAllMode(isWalkingAndTruckCall = true)
        }

        showDirectionSearchShimmer()
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            mViewModel.mSearchDirectionOriginData?.let { it1 ->
                mMapHelper.addMarker(
                    requireActivity(),
                    MarkerEnum.ORIGIN_ICON,
                    it1,
                )
                it1.amazonLocationPlace
                    ?.geometry
                    ?.point
                    ?.get(1)
                    ?.let { latitude ->
                        it1.amazonLocationPlace?.geometry?.point?.get(0)?.let { longitude ->
                            LatLng(
                                latitude,
                                longitude,
                            )
                        }
                    }?.let { latLng ->
                        liveLocationLatLng?.latitude?.let { latitude ->
                            mMapHelper.setDirectionMarker(
                                latLng,
                                latitude,
                                liveLocationLatLng.longitude,
                                requireActivity(),
                                MarkerEnum.DIRECTION_ICON,
                                "",
                            )
                        }
                    }
            }
            mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
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
                clBicycleLoader.isVisible ||
                clMotorcycleLoader.isVisible
        }
    }

    private fun showDirectionSearchShimmer() {
        mBinding.bottomSheetDirectionSearch.apply {
            clDriveLoader.show()
            clWalkLoader.show()
            if (isGrabMapSelected(mPreferenceManager, requireContext())) {
                clBicycleLoader.show()
                clBicycle.invisible()
                clMotorcycleLoader.show()
                clMotorcycle.invisible()
                clTruckLoader.hide()
                clTruck.hide()
                clTruck.hide()
                viewDividerOptionTruck.hide()
                viewDividerOptionBicycle.show()
                viewDividerOptionMotorcycle.show()
            } else {
                clTruckLoader.show()
                clTruck.invisible()
            }
            clWalk.invisible()
            clDrive.invisible()
        }
    }

    private fun BottomSheetDirectionSearchBinding.cardRouteOptionShow() {
        hideViews(rvSearchPlacesDirection, rvSearchPlacesSuggestionDirection, clMyLocation.root)
        if (mTravelMode == TravelMode.Car.value || mTravelMode == TravelMode.Truck.value) {
            cardRoutingOption.show()
        }
        showViews(cardMapOption)
    }

    private fun BottomSheetDirectionSearchBinding.cardRouteOptionHide() {
        showViews(rvSearchPlacesDirection, rvSearchPlacesSuggestionDirection)
        hideViews(cardRoutingOption, cardMapOption, cardListRoutesOption, layoutCardError.root)
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
                    object : SearchPlacesSuggestionAdapter.SearchPlaceSuggestionInterface {
                        override fun suggestedPlaceClick(position: Int) {
                            if (checkInternetConnection()) {
                                mViewModel.mIsPlaceSuggestion = false
                                edtSearchPlaces.setText(mPlaceList[position].text)
                                edtSearchPlaces.setSelection(edtSearchPlaces.text.toString().length)
                                if (mPlaceList[position].placeId.isNullOrEmpty()) {
                                    mPlaceList[position].text?.let {
                                        mViewModel.searchPlaceIndexForText(
                                            it,
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
            mViewModel.mSearchSuggestionData = data
            mViewModel.mSearchDirectionDestinationData = data
            mViewModel.mSearchDirectionDestinationData?.isDestination = true
            hideViews(mBinding.cardDirection, mBinding.cardNavigation)
            mBinding.bottomSheetDirection.apply {
                tvDirectionTime.hide()
                groupDistance.invisible()
                hideViews(tvDirectionError, ivInfo)
                tvDirectionError2.hide()
                if (!mMapHelper.isGrabSelectedAndOutsideBound) {
                    val liveLocationLatLng = mMapHelper.getLiveLocation()
                    isCalculateDriveApiError = false
                    mViewModel.calculateDistance(
                        latitude = liveLocationLatLng?.latitude,
                        longitude = liveLocationLatLng?.longitude,
                        latDestination =
                            data.amazonLocationPlace
                                ?.geometry
                                ?.point
                                ?.get(1),
                        lngDestination =
                            data.amazonLocationPlace
                                ?.geometry
                                ?.point
                                ?.get(0),
                        isAvoidFerries = mIsAvoidFerries,
                        isAvoidTolls = mIsAvoidTolls,
                        isWalkingAndTruckCall = false,
                    )
                    recordEventForAllMode(isWalkingAndTruckCall = false)
                } else {
                    mBinding.bottomSheetDirection.tvDirectionError2.text =
                        getString(R.string.label_current_location_disabled)
                    tvDirectionError2.show()
                    ivInfo.show()
                    cardLoaderSheet1.hide()
                    cardLoaderSheet2.hide()
                }
                if (data.amazonLocationPlace?.label?.let { validateLatLng(it) } != null) {
                    tvDirectionAddress.text = data.amazonLocationPlace?.label
                    sheetDirectionTvDirectionStreet.hide()
                } else {
                    val label = data.amazonLocationPlace?.label
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
                        tvDirectionDistance.text = getMetricsNew(requireContext(), it, isMetric)
                    }
                }
                notifyAdapters()
                mBottomSheetHelper.hideSearchBottomSheet(true)
                mBaseActivity?.bottomNavigationVisibility(false)
                mBottomSheetHelper.expandDirectionSheet()
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

    override fun onMapReady(mapboxMap: MapLibreMap) {
        mapboxMap.addOnMapClickListener(this)
        val mapStyleNameDisplay =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_light))
                ?: getString(R.string.map_light)
        val mapNameSelected =
            mBaseActivity
                ?.getString(R.string.map_esri)
                ?.let { mPreferenceManager.getValue(KEY_MAP_NAME, it) }
                ?: mBaseActivity?.getString(R.string.map_esri)
        val mapName: String
        val mapStyleName: String
        if (mapNameSelected == getString(R.string.grab)) {
            when (mapStyleNameDisplay) {
                resources.getString(R.string.map_grab_light) -> {
                    mapName = MapNames.GRAB_LIGHT
                    mapStyleName = MapStyles.GRAB_LIGHT
                }

                resources.getString(R.string.map_grab_dark) -> {
                    mapName = MapNames.GRAB_DARK
                    mapStyleName = MapStyles.GRAB_DARK
                }

                else -> {
                    mapName = ESRI_LIGHT
                    mapStyleName = VECTOR_ESRI_TOPOGRAPHIC
                }
            }
        } else {
            when (mapStyleNameDisplay) {
                getString(R.string.map_light) -> {
                    mapName = ESRI_LIGHT
                    mapStyleName = VECTOR_ESRI_TOPOGRAPHIC
                }

                getString(R.string.map_streets) -> {
                    mapName = MapNames.ESRI_STREET_MAP
                    mapStyleName = MapStyles.VECTOR_ESRI_STREETS
                }

                getString(R.string.map_navigation) -> {
                    mapName = MapNames.ESRI_NAVIGATION
                    mapStyleName = MapStyles.VECTOR_ESRI_NAVIGATION
                }

                getString(R.string.map_dark_gray) -> {
                    mapName = MapNames.ESRI_DARK_GRAY_CANVAS
                    mapStyleName = MapStyles.VECTOR_ESRI_DARK_GRAY_CANVAS
                }

                getString(R.string.map_light_gray) -> {
                    mapName = MapNames.ESRI_LIGHT_GRAY_CANVAS
                    mapStyleName = MapStyles.VECTOR_ESRI_LIGHT_GRAY_CANVAS
                }

                getString(R.string.map_imagery) -> {
                    mapName = MapNames.ESRI_IMAGERY
                    mapStyleName = MapStyles.RASTER_ESRI_IMAGERY
                }

                resources.getString(R.string.map_contrast) -> {
                    mapName = MapNames.HERE_CONTRAST
                    mapStyleName = MapStyles.VECTOR_HERE_CONTRAST
                }

                resources.getString(R.string.map_explore) -> {
                    mapName = MapNames.HERE_EXPLORE
                    mapStyleName = MapStyles.VECTOR_HERE_EXPLORE
                }

                resources.getString(R.string.map_explore_truck) -> {
                    mapName = MapNames.HERE_EXPLORE_TRUCK
                    mapStyleName = MapStyles.VECTOR_HERE_EXPLORE_TRUCK
                }

                resources.getString(R.string.map_hybrid) -> {
                    mapName = MapNames.HERE_HYBRID
                    mapStyleName = MapStyles.HYBRID_HERE_EXPLORE_SATELLITE
                }

                resources.getString(R.string.map_raster) -> {
                    mapName = MapNames.HERE_IMAGERY
                    mapStyleName = MapStyles.RASTER_HERE_EXPLORE_SATELLITE
                }

                resources.getString(R.string.map_standard_light) -> {
                    mapName = MapNames.OPEN_DATA_STANDARD_LIGHT
                    mapStyleName = MapStyles.VECTOR_OPEN_DATA_STANDARD_LIGHT
                }

                resources.getString(R.string.map_standard_dark) -> {
                    mapName = MapNames.OPEN_DATA_STANDARD_DARK
                    mapStyleName = MapStyles.VECTOR_OPEN_DATA_STANDARD_DARK
                }

                resources.getString(R.string.map_visualization_light) -> {
                    mapName = MapNames.OPEN_DATA_VISUALIZATION_LIGHT
                    mapStyleName = MapStyles.VECTOR_OPEN_DATA_VISUALIZATION_LIGHT
                }

                resources.getString(R.string.map_visualization_dark) -> {
                    mapName = MapNames.OPEN_DATA_VISUALIZATION_DARK
                    mapStyleName = MapStyles.VECTOR_OPEN_DATA_VISUALIZATION_DARK
                }

                else -> {
                    mapName = ESRI_LIGHT
                    mapStyleName = VECTOR_ESRI_TOPOGRAPHIC
                }
            }
        }
        mMapHelper.initSymbolManager(
            mBinding.mapView,
            mapboxMap,
            mapName,
            mapStyleName,
            this,
            this,
            activity,
            mPreferenceManager,
        )
        activity?.let {
            mBaseActivity?.mGeofenceUtils?.setMapBox(
                it,
                mapboxMap,
                mMapHelper,
                mPreferenceManager,
            )
            mBaseActivity?.mTrackingUtils?.setMapBox(
                it,
                mapboxMap,
                mMapHelper,
            )
        }
        mapboxMap.uiSettings.isCompassEnabled = false
        this.mMapboxMap = mapboxMap
        mapboxMap.addOnCameraIdleListener {
            if (isGrabMapSelected(mPreferenceManager, requireContext())) {
                mMapboxMap?.limitViewToBounds(bounds)
            } else {
                mViewModel.mLatLng =
                    mapboxMap.cameraPosition.target?.let {
                        LatLng(
                            it.latitude,
                            mapboxMap.cameraPosition.target!!.longitude,
                        )
                    }
            }
        }
        setMapBoxInSimulation()
    }

    fun setMapBoxInSimulation() {
        activity?.let {
            mMapboxMap?.let { it1 ->
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
        val locationRequest: LocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val result: Task<LocationSettingsResponse> =
            LocationServices
                .getSettingsClient(requireActivity())
                .checkLocationSettings(builder.build())
        builder.setAlwaysShow(true)
        this.mIsLocationAlreadyEnabled = isLocationAlreadyEnabled
        this.mIsCurrentLocationClicked = isCurrentLocationClicked
        this.isLiveLocationClick = isLiveLocationClick
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
        if (mIsTrackingLocationClicked) {
            mIsTrackingLocationClicked = false
            mPreferenceManager.setValue(IS_LOCATION_TRACKING_ENABLE, true)
            mBaseActivity?.mTrackingUtils?.locationPermissionAdded()
        } else {
            if (mIsCurrentLocationClicked) {
                mBaseActivity?.resetLocationPermission()
                mMapHelper.checkLocationComponentEnable(mBaseActivity, isLiveLocationClick)
            } else {
                if (mIsLocationAlreadyEnabled) {
                    mMapHelper.checkLocationComponentEnable(mBaseActivity, false)
                } else {
                    mBaseActivity?.resetLocationPermission()
                    mMapHelper.enableLocationComponent()
                    mMapHelper.setInitialLocation()
                }
            }
            isLiveLocationClick = false
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
                val liveLocationLatLng = mMapHelper.getLiveLocation()
                isCalculateDriveApiError = false
                mViewModel.calculateDistance(
                    latitude = liveLocationLatLng?.latitude,
                    longitude = liveLocationLatLng?.longitude,
                    latDestination =
                        it
                            ?.amazonLocationPlace
                            ?.geometry
                            ?.point
                            ?.get(1),
                    lngDestination =
                        it
                            ?.amazonLocationPlace
                            ?.geometry
                            ?.point
                            ?.get(0),
                    isAvoidFerries = mIsAvoidFerries,
                    isAvoidTolls = mIsAvoidTolls,
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

    @SuppressLint("NotifyDataSetChanged")
    fun mapStyleChange(
        isMapClick: Boolean,
        selectedProvider: String,
        selectedInnerData: String,
    ) {
        val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
        val defaultIdentityPoolId: String =
            Units.getDefaultIdentityPoolId(
                mPreferenceManager.getValue(
                    KEY_SELECTED_REGION,
                    regionDisplayName[0],
                ),
                mPreferenceManager.getValue(KEY_NEAREST_REGION, ""),
            )
        var isRestartNeeded =
            if (defaultIdentityPoolId == BuildConfig.DEFAULT_IDENTITY_POOL_ID_AP) {
                false
            } else {
                if (mapName == getString(R.string.esri) || mapName == getString(R.string.here)) {
                    selectedProvider == getString(R.string.grab)
                } else {
                    selectedProvider != getString(R.string.grab)
                }
            }
        val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        if (mAuthStatus == AuthEnum.SIGNED_IN.name || mAuthStatus == AuthEnum.AWS_CONNECTED.name) {
            isRestartNeeded = false
        }
        if (selectedProvider == getString(R.string.grab) && mapName != getString(R.string.grab)) {
            val shouldShowGrabDialog = !mPreferenceManager.getValue(KEY_GRAB_DONT_ASK, false)
            if (shouldShowGrabDialog) {
                activity?.restartAppMapStyleDialog(
                    object : MapStyleRestartInterface {
                        override fun onOkClick(
                            dialog: DialogInterface,
                            dontAskAgain: Boolean,
                        ) {
                            mPreferenceManager.setValue(KEY_GRAB_DONT_ASK, dontAskAgain)
                            changeMapStyle(isMapClick, selectedProvider, selectedInnerData)
                            if (isRestartNeeded) {
                                mPreferenceManager.setValue(
                                    KEY_SELECTED_REGION,
                                    regionDisplayName[2],
                                )
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
                                    Uri.parse(BuildConfig.GRAB_LEARN_MORE),
                                ),
                            )
                        }
                    },
                )
            } else {
                changeMapStyle(isMapClick, selectedProvider, selectedInnerData)
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
        } else if (selectedProvider == getString(R.string.open_data) && mapName != getString(R.string.open_data)) {
            val shouldShowOpenDataDialog =
                !mPreferenceManager.getValue(KEY_OPEN_DATA_DONT_ASK, false)
            if (shouldShowOpenDataDialog) {
                activity?.enableOpenData(
                    object : MapStyleRestartInterface {
                        override fun onOkClick(
                            dialog: DialogInterface,
                            dontAskAgain: Boolean,
                        ) {
                            changeMapStyle(isMapClick, selectedProvider, selectedInnerData)
                            mPreferenceManager.setValue(KEY_OPEN_DATA_DONT_ASK, dontAskAgain)
                        }

                        override fun onLearnMoreClick(dialog: DialogInterface) {}
                    },
                )
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
        selectedInnerData: String,
    ) {
        activity?.runOnUiThread {
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
                mBaseActivity?.isTablet?.let {
                    if (it) {
                        mapStyleBottomSheetFragment?.notifyAdapter()
                    } else {
                        mMapStyleAdapter?.notifyDataSetChanged()
                    }
                }
            } else {
                changeStyle(selectedProvider, selectedInnerData)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeStyle(
        selectedProvider: String,
        selectedInnerData: String,
    ) {
        clearAllMapData()
        mBinding.bottomSheetMapStyle.apply {
            mBaseActivity?.isTablet?.let {
                if (it) {
                    mapStyleBottomSheetFragment?.refreshMapTile(selectedProvider)
                } else {
                    setMapTileSelection(selectedProvider)
                }
            }
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
                                            selectedId = ESRI_LIGHT
                                            mMapHelper.updateStyle(
                                                ESRI_LIGHT,
                                                VECTOR_ESRI_TOPOGRAPHIC,
                                            )
                                        }

                                        getString(R.string.map_streets) -> {
                                            selectedId = MapNames.ESRI_STREET_MAP
                                            mMapHelper.updateStyle(
                                                MapNames.ESRI_STREET_MAP,
                                                MapStyles.VECTOR_ESRI_STREETS,
                                            )
                                        }

                                        getString(R.string.map_navigation) -> {
                                            selectedId = MapNames.ESRI_NAVIGATION
                                            mMapHelper.updateStyle(
                                                MapNames.ESRI_NAVIGATION,
                                                MapStyles.VECTOR_ESRI_NAVIGATION,
                                            )
                                        }

                                        getString(R.string.map_dark_gray) -> {
                                            selectedId = MapNames.ESRI_DARK_GRAY_CANVAS
                                            mMapHelper.updateStyle(
                                                MapNames.ESRI_DARK_GRAY_CANVAS,
                                                MapStyles.VECTOR_ESRI_DARK_GRAY_CANVAS,
                                            )
                                        }

                                        getString(R.string.map_light_gray) -> {
                                            selectedId = MapNames.ESRI_LIGHT_GRAY_CANVAS
                                            mMapHelper.updateStyle(
                                                MapNames.ESRI_LIGHT_GRAY_CANVAS,
                                                MapStyles.VECTOR_ESRI_LIGHT_GRAY_CANVAS,
                                            )
                                        }

                                        getString(R.string.map_imagery) -> {
                                            selectedId = MapNames.ESRI_IMAGERY
                                            mMapHelper.updateStyle(
                                                MapNames.ESRI_IMAGERY,
                                                MapStyles.RASTER_ESRI_IMAGERY,
                                            )
                                        }

                                        resources.getString(R.string.map_contrast) -> {
                                            selectedId = MapNames.HERE_CONTRAST
                                            mMapHelper.updateStyle(
                                                MapNames.HERE_CONTRAST,
                                                MapStyles.VECTOR_HERE_CONTRAST,
                                            )
                                        }

                                        resources.getString(R.string.map_explore) -> {
                                            selectedId = MapNames.HERE_EXPLORE
                                            mMapHelper.updateStyle(
                                                MapNames.HERE_EXPLORE,
                                                MapStyles.VECTOR_HERE_EXPLORE,
                                            )
                                        }

                                        resources.getString(R.string.map_explore_truck) -> {
                                            selectedId = MapNames.HERE_EXPLORE_TRUCK
                                            mMapHelper.updateStyle(
                                                MapNames.HERE_EXPLORE_TRUCK,
                                                MapStyles.VECTOR_HERE_EXPLORE_TRUCK,
                                            )
                                        }

                                        resources.getString(R.string.map_hybrid) -> {
                                            selectedId = MapNames.HERE_HYBRID
                                            mMapHelper.updateStyle(
                                                MapNames.HERE_HYBRID,
                                                MapStyles.HYBRID_HERE_EXPLORE_SATELLITE,
                                            )
                                        }

                                        resources.getString(R.string.map_raster) -> {
                                            selectedId = MapNames.HERE_IMAGERY
                                            mMapHelper.updateStyle(
                                                MapNames.HERE_IMAGERY,
                                                MapStyles.RASTER_HERE_EXPLORE_SATELLITE,
                                            )
                                        }

                                        resources.getString(R.string.map_standard_light) -> {
                                            mMapHelper.updateStyle(
                                                MapNames.OPEN_DATA_STANDARD_LIGHT,
                                                MapStyles.VECTOR_OPEN_DATA_STANDARD_LIGHT,
                                            )
                                        }

                                        resources.getString(R.string.map_standard_dark) -> {
                                            selectedId = MapNames.OPEN_DATA_STANDARD_DARK
                                            mMapHelper.updateStyle(
                                                MapNames.OPEN_DATA_STANDARD_DARK,
                                                MapStyles.VECTOR_OPEN_DATA_STANDARD_DARK,
                                            )
                                        }

                                        resources.getString(R.string.map_visualization_light) -> {
                                            selectedId = MapNames.OPEN_DATA_VISUALIZATION_LIGHT
                                            mMapHelper.updateStyle(
                                                MapNames.OPEN_DATA_VISUALIZATION_LIGHT,
                                                MapStyles.VECTOR_OPEN_DATA_VISUALIZATION_LIGHT,
                                            )
                                        }

                                        resources.getString(R.string.map_visualization_dark) -> {
                                            selectedId = MapNames.OPEN_DATA_VISUALIZATION_DARK
                                            mMapHelper.updateStyle(
                                                MapNames.OPEN_DATA_VISUALIZATION_DARK,
                                                MapStyles.VECTOR_OPEN_DATA_VISUALIZATION_DARK,
                                            )
                                        }
                                    }
                                } else {
                                    when (innerData.mapName) {
                                        resources.getString(R.string.map_grab_light) -> {
                                            selectedId = MapNames.GRAB_LIGHT
                                            mMapHelper.updateStyle(
                                                MapNames.GRAB_LIGHT,
                                                MapStyles.GRAB_LIGHT,
                                            )
                                        }

                                        resources.getString(R.string.map_grab_dark) -> {
                                            selectedId = MapNames.GRAB_DARK
                                            mMapHelper.updateStyle(
                                                MapNames.GRAB_DARK,
                                                MapStyles.GRAB_DARK,
                                            )
                                        }
                                    }
                                }
                                innerData.mapName?.let { mapName ->
                                    val properties = listOf(
                                        Pair(AnalyticsAttribute.PROVIDER, mapName),
                                        Pair(AnalyticsAttribute.ID, selectedId),
                                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.EXPLORER)
                                    )
                                    (activity as MainActivity).analyticsUtils?.recordEvent(EventType.MAP_STYLE_CHANGE, properties)
                                    mPreferenceManager.setValue(
                                        KEY_MAP_STYLE_NAME,
                                        mapName,
                                    )
                                }
                                data.styleNameDisplay?.let { it1 ->
                                    mPreferenceManager.setValue(
                                        KEY_MAP_NAME,
                                        it1,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        mBaseActivity?.isTablet?.let {
            if (it) {
                mapStyleBottomSheetFragment?.notifyAdapter()
            } else {
                mMapStyleAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun setBounds(latLng: LatLng) {
        // Set the map bounds
        mMapboxMap?.limitViewToBounds(bounds)
        mMapboxMap?.setMinZoomPreference(5.0)
        lifecycleScope.launch {
            delay(CLICK_DEBOUNCE)
            mMapHelper.navigationZoomCamera(latLng, false)
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
                    if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() != true && mBaseActivity?.mTrackingUtils?.isChangeDataProviderClicked != true && mBaseActivity?.mGeofenceUtils?.isChangeDataProviderClicked != true) {
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
            if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() != true && mBaseActivity?.mTrackingUtils?.isChangeDataProviderClicked != true && mBaseActivity?.mGeofenceUtils?.isChangeDataProviderClicked != true) {
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
            mViewModel.getAddressLineFromLatLng(point.longitude, point.latitude)
            val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
            val properties = listOf(
                Pair(AnalyticsAttribute.TRAVEL_MODE, TRAVEL_MODE_CAR),
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.PLACES_POPUP),
                Pair(AnalyticsAttribute.DISTANCE_UNIT, if (isMetric) KILOMETERS else MILES)
            )
            (activity as MainActivity).analyticsUtils?.recordEvent(EventType.ROUTE_SEARCH, properties)
            return true
        }
        if (mBaseActivity?.mGeofenceUtils?.isAddGeofenceBottomSheetVisible() == true) {
            mBaseActivity?.mGeofenceUtils?.mapClick(point)
            mViewModel.getAddressLineFromLatLng(point.longitude, point.latitude)
            return true
        }
        return false
    }

    override fun mapLoadedSuccess() {
        mBinding.mapView.contentDescription = "Amazon Map Ready"
        mBinding.groupMapLoad.hide()
    }

    override fun onScaleBegin(detector: StandardScaleGestureDetector) {
        isZooming = true
    }

    override fun onScale(detector: StandardScaleGestureDetector) {
        mMapHelper.getLiveLocation()?.let { mLatLng ->
            mMapboxMap?.easeCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition
                        .Builder()
                        .target(mLatLng)
                        .build(),
                ),
                Durations.CAMERA_DURATION_1000,
            )
        }
    }

    override fun onScaleEnd(detector: StandardScaleGestureDetector) {
        isZooming = false
        mMapHelper.getLiveLocation()?.let { mLatLng ->
            mMapHelper.navigationZoomCamera(mLatLng, false)
        }
    }

    override fun onMapStyleChanged(mapStyle: String) {
        val logoResId =
            when (mapStyle) {
                ESRI_LIGHT,
                MapNames.ESRI_STREET_MAP,
                MapNames.ESRI_NAVIGATION,
                MapNames.ESRI_LIGHT_GRAY_CANVAS,
                MapNames.HERE_CONTRAST,
                MapNames.HERE_EXPLORE,
                MapNames.HERE_EXPLORE_TRUCK,
                MapNames.GRAB_LIGHT,
                MapNames.OPEN_DATA_STANDARD_LIGHT,
                MapNames.OPEN_DATA_VISUALIZATION_LIGHT,
                -> R.drawable.ic_amazon_logo_on_light

                MapNames.ESRI_DARK_GRAY_CANVAS,
                MapNames.ESRI_IMAGERY,
                MapNames.HERE_IMAGERY,
                MapNames.HERE_HYBRID,
                MapNames.GRAB_DARK,
                MapNames.OPEN_DATA_STANDARD_DARK,
                MapNames.OPEN_DATA_VISUALIZATION_DARK,
                -> R.drawable.ic_amazon_logo_on_dark

                else -> {
                    R.drawable.ic_amazon_logo_on_light
                }
            }
        lifecycleScope.launch {
            delay(DELAY_500)
            if (mapStyle == MapNames.GRAB_LIGHT || mapStyle == MapNames.GRAB_DARK) {
                checkGrabMapInsideBounds()
            } else {
                mMapboxMap?.setLatLngBoundsForCameraTarget(null)
                mMapboxMap?.style?.let { mMapHelper.updateZoomRange(it) }
                mMapHelper.checkLocationComponentEnable(mBaseActivity, false)
            }
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
        mBinding.bottomSheetMapStyle.imgAmazonLogoMapStyle?.setImageResource(logoResId)
        if (mBaseActivity?.mSimulationUtils?.isSimulationBottomSheetVisible() == true) {
            lifecycleScope.launch {
                delay(DELAY_500)
                mBaseActivity?.mSimulationUtils?.setSimulationData()
            }
        }
    }

    private fun checkGrabMapInsideBounds() {
        val currentLocation = mMapHelper.getLiveLocation()
        currentLocation?.let {
            setBounds(it)
        }
    }

    private fun MapLibreMap.limitViewToBounds(bounds: LatLngBounds) {
        val newBoundsHeight =
            bounds.latitudeSpan - projection.visibleRegion.latLngBounds.latitudeSpan
        val newBoundsWidth =
            bounds.longitudeSpan - projection.visibleRegion.latLngBounds.longitudeSpan
        val leftTopLatLng =
            LatLng(
                bounds.latitudeNorth - (bounds.latitudeSpan - newBoundsHeight) / 2,
                bounds.longitudeEast - (bounds.longitudeSpan - newBoundsWidth) / 2 - newBoundsWidth,
            )
        val rightBottomLatLng =
            LatLng(
                bounds.latitudeNorth - (bounds.latitudeSpan - newBoundsHeight) / 2 - newBoundsHeight,
                bounds.longitudeEast - (bounds.longitudeSpan - newBoundsWidth) / 2,
            )
        val newBounds =
            LatLngBounds
                .Builder()
                .include(leftTopLatLng)
                .include(rightBottomLatLng)
                .build()
        setLatLngBoundsForCameraTarget(newBounds)
    }
}
