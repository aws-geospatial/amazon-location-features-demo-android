package com.aws.amazonlocation.ui.main.simulation

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.AdapterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.services.geo.AmazonLocationClient
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.BusRouteCoordinates
import com.aws.amazonlocation.data.response.NotificationSimulationData
import com.aws.amazonlocation.data.response.RouteSimulationDataItem
import com.aws.amazonlocation.data.response.SimulationGeofenceData
import com.aws.amazonlocation.data.response.SimulationHistoryData
import com.aws.amazonlocation.data.response.SimulationHistoryInnerData
import com.aws.amazonlocation.databinding.BottomSheetTrackSimulationBinding
import com.aws.amazonlocation.domain.`interface`.SimulationInterface
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AWSLocationHelper
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.CLICK_DEBOUNCE_ENABLE
import com.aws.amazonlocation.utils.DELAY_PROCESS_1000
import com.aws.amazonlocation.utils.DELAY_SIMULATION_2000
import com.aws.amazonlocation.utils.ENTER
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.GeofenceCons
import com.aws.amazonlocation.utils.KEY_NEAREST_REGION
import com.aws.amazonlocation.utils.KEY_SELECTED_REGION
import com.aws.amazonlocation.utils.LABEL_IN_BETWEEN
import com.aws.amazonlocation.utils.LABEL_PRE_DRAW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.LAYER
import com.aws.amazonlocation.utils.LAYER_SIMULATION_ICON
import com.aws.amazonlocation.utils.MapCameraZoom.SIMULATION_CAMERA_ZOOM_1
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.NotificationHelper
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.SOURCE
import com.aws.amazonlocation.utils.SOURCE_SIMULATION_ICON
import com.aws.amazonlocation.utils.TRACKER
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.Units.checkInternetConnection
import com.aws.amazonlocation.utils.Units.getSimulationWebSocketUrl
import com.aws.amazonlocation.utils.Units.readRouteData
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfConstants
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfTransformation
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.notificationData
import com.aws.amazonlocation.utils.regionDisplayName
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.simulationCollectionName
import com.aws.amazonlocation.utils.simulationLatNorth
import com.aws.amazonlocation.utils.simulationLatSouth
import com.aws.amazonlocation.utils.simulationLonEast
import com.aws.amazonlocation.utils.simulationLonWest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

class SimulationUtils(
    val mPreferenceManager: PreferenceManager? = null,
    val activity: Activity?,
    val mAWSLocationHelper: AWSLocationHelper
) {
    private var routeData: ArrayList<RouteSimulationDataItem>? = null
    private var isCoroutineStarted: Boolean = false
    private var notificationId: Int = 1
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var mqttManager: AWSIotMqttManager? = null
    private var simulationTrackingListAdapter: SimulationListAdapter? = null
    private var simulationNotificationAdapter: SimulationNotificationAdapter? = null
    private var mBottomSheetSimulationBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var simulationBinding: BottomSheetTrackSimulationBinding? = null
    private var mFragmentActivity: FragmentActivity? = null
    private var simulationInterface: SimulationInterface? = null
    private var mMapHelper: MapHelper? = null
    private var mMapboxMap: MapboxMap? = null
    private var mClient: AmazonLocationClient? = null
    private var mActivity: Activity? = null
    private var mIsLocationUpdateEnable = false
    private var simulationHistoryData = arrayListOf<SimulationHistoryData>()
    private val mCircleUnit: String = TurfConstants.UNIT_METERS
    private var mIsDefaultGeofence = false
    private var selectedTrackerIndex = 0
    private var timerCounts: MutableList<Int>? = null
    private var busesCoordinates = MutableList(notificationData.size) { mutableListOf<Point>() }
    private var busSimulationNotificationFlags: MutableList<Boolean>? = null
    private var busSimulationHistoryData =
        MutableList(notificationData.size) { mutableListOf<SimulationHistoryData>() }
    private var mGeofenceList =
        MutableList(notificationData.size) { mutableListOf<ListGeofenceResponseEntry>() }
    private var mPreDrawTrackerLine = MutableList(notificationData.size) { mutableListOf<Point>() }
    private var busStopCounts: MutableList<Int>? = null
    private var notificationHelper: NotificationHelper? = null

    fun setMapBox(
        activity: Activity,
        mapboxMap: MapboxMap,
        mMapHelper: MapHelper
    ) {
        mClient = AmazonLocationClient(AWSMobileClient.getInstance())
        this.mMapHelper = mMapHelper
        this.mMapboxMap = mapboxMap
        this.mActivity = activity
    }

    fun showSimulationBottomSheet() {
        mBottomSheetSimulationBehavior?.isHideable = false
        mBottomSheetSimulationBehavior?.isFitToContents = false
        simulationBinding?.clPersistentBottomSheetSimulation?.show()
        simulationBinding?.clTracking?.context?.let {
            if ((activity as MainActivity).isTablet) {
                mBottomSheetSimulationBehavior?.peekHeight =
                    it.resources.getDimensionPixelSize(R.dimen.dp_280)
                mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                mBottomSheetSimulationBehavior?.peekHeight =
                    it.resources.getDimensionPixelSize(R.dimen.dp_250)
                mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        mActivity?.let {
            if (checkInternetConnection(it.applicationContext)) {
                simulationInterface?.getGeofenceList()
            }
        }
        setBounds()
        mFragmentActivity?.applicationContext?.let { context ->
            notificationHelper = NotificationHelper(context)
            notificationHelper?.createNotificationChannel()
            routeData = readRouteData(context)?.busRoutesData

            routeData?.let { route ->
                timerCounts = MutableList(route.size) { 0 }
                busSimulationNotificationFlags = MutableList(route.size) { false }
                busStopCounts = MutableList(route.size) { 0 }
            }
        }
        simulationBinding?.cardStartTracking?.performClick()
    }

    private fun setBounds() {
        // Define your bounds coordinates
        val bounds = LatLngBounds.Builder()
            .include(LatLng(simulationLatNorth, simulationLonEast)) // Northeast corner
            .include(LatLng(simulationLatSouth, simulationLonWest)) // Southwest corner
            .build()

        // Set the bounds to restrict the visible area on the map
        mMapboxMap?.limitViewToBounds(bounds)
        mMapboxMap?.setMinZoomPreference(SIMULATION_CAMERA_ZOOM_1)
    }

    private fun initData() {
        mFragmentActivity?.applicationContext?.let { context ->
            routeData?.let { route ->
                if (!isAnyBusTrackerEnable()) {
                    busSimulationNotificationFlags?.set(0, true)
                    notificationData[0].isSelected = true
                    setSelectedNotificationCount()
                    simulationNotificationAdapter?.notifyItemChanged(0)
                    mActivity?.let { activity ->
                        getFirstCoordinates()?.forEachIndexed { index, busRouteCoordinates ->
                            if (isBusTrackerNotificationEnable(index)) {
                                addMarkerSimulation(activity, index, busRouteCoordinates)
                            }
                        }
                    }
                    drawGeofenceWithPosition(0)
                    addPreDrawTrackerLine(route, 0)
                    zoomCamera()
                }
                coroutineScope.launch {
                    while (isActive) {
                        if (mIsLocationUpdateEnable) {
                            getNextCoordinates()?.forEachIndexed { index, busRouteCoordinates ->
                                if (busRouteCoordinates.isUpdateNeeded) {
                                    busRouteCoordinates.coordinates?.let { list ->
                                        route[index].coordinates?.size?.let { size ->
                                            updateSimulationLocation(
                                                busRouteCoordinates.id,
                                                index,
                                                list,
                                                context,
                                                size
                                            )
                                        }
                                    }
                                }
                            }
                            simulationHistoryData.clear()
                            simulationHistoryData.addAll(
                                getSelectedBusTrackingData(
                                    selectedTrackerIndex
                                )
                            )
                            withContext(Dispatchers.Main) {
                                simulationTrackingListAdapter?.submitList(simulationHistoryData.toMutableList())
                            }
                        }
                        delay(DELAY_SIMULATION_2000)
                    }
                }
                isCoroutineStarted = true
            }
        }
    }

    private fun drawGeofenceWithPosition(position: Int) {
        val mLatLngList = arrayListOf<LatLng>()
        if (mGeofenceList.isNotEmpty()) {
            mGeofenceList[position].forEachIndexed { index, data ->
                val latLng =
                    LatLng(data.geometry.circle.center[1], data.geometry.circle.center[0])
                setDefaultIconWithGeofence("$position$index")
                mLatLngList.add(latLng)
                drawSimulationPolygonCircle(
                    Point.fromLngLat(latLng.longitude, latLng.latitude),
                    data.geometry.circle.radius.toInt(),
                    "$position$index"
                )
            }
        }
    }

    private fun removeGeofenceWithPosition(position: Int) {
        if (mGeofenceList.isNotEmpty()) {
            mGeofenceList[position].forEachIndexed { index, _ ->
                mMapboxMap?.style?.removeLayer(GeofenceCons.CIRCLE_CENTER_LAYER_ID + "$position$index")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + "$position$index")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$position$index")
            }
        }
    }

    private fun addPreDrawTrackerLine(route: ArrayList<RouteSimulationDataItem>, index: Int) {
        val trackerLineToPreDraw = arrayListOf<Point>()
        route[index].coordinates?.forEach {
            trackerLineToPreDraw.add(Point.fromLngLat(it[0], it[1]))
        }
        mPreDrawTrackerLine[index] = trackerLineToPreDraw
        addTrackerLine(trackerLineToPreDraw, route, index)
    }

    private fun zoomCamera() {
        val cameraZoomList = arrayListOf<LatLng>()
        notificationData.forEachIndexed { index, notificationData ->
            if (notificationData.isSelected) {
                cameraZoomList.add(
                    LatLng(
                        mPreDrawTrackerLine[index].first().latitude(),
                        mPreDrawTrackerLine[index].first().longitude()
                    )
                )
                cameraZoomList.add(
                    LatLng(
                        mPreDrawTrackerLine[index].last().latitude(),
                        mPreDrawTrackerLine[index].last().longitude()
                    )
                )
            }
        }
        if ((activity as MainActivity).isTablet) {
            mActivity?.resources?.getDimension(R.dimen.dp_175)?.toInt()
                ?.let { mMapHelper?.adjustMapBounds(cameraZoomList, it, it) }
        } else {
            mActivity?.resources?.getDimension(R.dimen.dp_80)?.toInt()
                ?.let { mMapHelper?.adjustMapBounds(cameraZoomList, it) }
        }
    }

    private fun addTrackerLine(
        trackerLineToPreDraw: ArrayList<Point>,
        route: ArrayList<RouteSimulationDataItem>,
        index: Int
    ) {
        mMapHelper?.addTrackerLine(
            trackerLineToPreDraw,
            true,
            "$LAYER${route[index].id}$LABEL_PRE_DRAW",
            "$SOURCE${route[index].id}$LABEL_PRE_DRAW",
            R.color.color_hint_text
        )
    }

    private fun removePreDrawTrackerLine(route: ArrayList<RouteSimulationDataItem>, index: Int) {
        route[index].let {
            mMapHelper?.removeSource("$SOURCE_SIMULATION_ICON${it.id}")
            mMapHelper?.removeLayer("$LAYER_SIMULATION_ICON${it.id}")
            mMapHelper?.removeLayer("$LAYER${it.id}")
            mMapHelper?.removeSource("$SOURCE${it.id}")
            mMapHelper?.removeLayer("$LAYER${it.id}$LABEL_IN_BETWEEN")
            mMapHelper?.removeSource("$SOURCE${it.id}$LABEL_IN_BETWEEN")
            mMapHelper?.removeLayer("$LAYER${route[index].id}$LABEL_PRE_DRAW$LABEL_IN_BETWEEN")
            mMapHelper?.removeSource("$LAYER${route[index].id}$LABEL_PRE_DRAW$LABEL_IN_BETWEEN")
            mMapHelper?.removeLayer("$LAYER${it.id}$LABEL_PRE_DRAW")
            mMapHelper?.removeSource("$SOURCE${it.id}$LABEL_PRE_DRAW")
        }
    }

    private fun getNextCoordinates(): List<BusRouteCoordinates>? {
        val nextCoordinates = routeData?.mapIndexedNotNull { index, routeSimulationDataItem ->
            if (isBusTrackerNotificationEnable(index)) {
                val coordinatesSize = routeSimulationDataItem.coordinates?.size ?: 0
                if (coordinatesSize > 0) {
                    timerCounts?.let {
                        val currentIndex = (it[index]) % coordinatesSize
                        if (currentIndex == 0) {
                            (activity as MainActivity).lifecycleScope.launch {
                                mMapHelper?.removeLayer("$LAYER${routeSimulationDataItem.id}")
                                mMapHelper?.removeLayer("$SOURCE${routeSimulationDataItem.id}")
                                busesCoordinates[index].clear()
                                busSimulationHistoryData[index].clear()
                                busStopCounts?.set(index, 0)
                                routeData?.let { route ->
                                    addPreDrawTrackerLine(route, index)
                                }
                            }
                        }
                        it[index] = (it[index] + 1) % coordinatesSize
                        BusRouteCoordinates(
                            routeSimulationDataItem.id,
                            routeSimulationDataItem.geofenceCollection,
                            routeSimulationDataItem.coordinates?.get(currentIndex),
                            true
                        )
                    }
                } else {
                    null
                }
            } else {
                BusRouteCoordinates(
                    routeSimulationDataItem.id,
                    routeSimulationDataItem.geofenceCollection,
                    null,
                    false
                )
            }
        }
        return nextCoordinates
    }

    private fun getFirstCoordinates(): List<BusRouteCoordinates>? {
        return routeData?.map { route ->
            BusRouteCoordinates(
                route.id,
                route.geofenceCollection,
                route.coordinates?.first()
            )
        }
    }

    private fun getFirstCoordinates(position: Int): BusRouteCoordinates {
        return BusRouteCoordinates(
            routeData?.get(position)?.id,
            routeData?.get(position)?.geofenceCollection,
            routeData?.get(position)?.coordinates?.first()
        )
    }

    private suspend fun updateSimulationLocation(
        busId: String?,
        busIndex: Int,
        point: List<Double>,
        context: Context,
        size: Int
    ) {
        (activity as MainActivity).lifecycleScope.launch {
            val latLng = LatLng(point[1], point[0])
            val position = arrayListOf<Double>()
            position.add(latLng.longitude)
            position.add(latLng.latitude)
            mActivity?.let {
                if (checkInternetConnection(it.applicationContext)) {
                    simulationInterface?.evaluateGeofence(simulationCollectionName[busIndex], position)
                }
            }

            mMapHelper?.startAnimation(latLng, busIndex)
            delay(DELAY_PROCESS_1000)
            val latLngPoint = Point.fromLngLat(point[0], point[1])
            busesCoordinates[busIndex].add(latLngPoint)
            val positionData: String = when (busesCoordinates[busIndex].size) {
                1 -> context.getString(R.string.label_position_start)
                size -> context.getString(R.string.label_position_end)
                else -> context.getString(R.string.label_position_data)
            }
            val historyData = SimulationHistoryData(
                positionData,
                false,
                0,
                SimulationHistoryInnerData(point[1], point[0], Date())
            )
            updateTrackingHistoryData(busIndex, historyData)
            if (busesCoordinates[busIndex].size > 1) {
                mMapHelper?.addTrackerLine(
                    busesCoordinates[busIndex],
                    true,
                    "$LAYER$busId",
                    "$SOURCE$busId",
                    R.color.color_primary_green
                )
                mPreDrawTrackerLine[busIndex].removeAt(0)
                routeData?.let { route ->
                    addTrackerLine(
                        mPreDrawTrackerLine[busIndex] as ArrayList<Point>,
                        route,
                        busIndex
                    )
                }
            }
        }
    }

    private fun isAnyBusTrackerEnable(): Boolean {
        busSimulationNotificationFlags?.let {
            it.forEach { it1 ->
                if (it1) {
                    return true
                }
            }
        }
        return false
    }

    private fun isBusTrackerNotificationEnable(busIndex: Int): Boolean {
        busSimulationNotificationFlags?.let {
            return when (busIndex) {
                in it.indices -> {
                    it[busIndex]
                }
                else -> false
            }
        }
        return false
    }

    private fun getSelectedBusTrackingData(busIndex: Int): List<SimulationHistoryData> {
        return when (busIndex) {
            in 0..9 -> busSimulationHistoryData[busIndex]
            else -> emptyList()
        }
    }

    private fun updateTrackingHistoryData(busIndex: Int, data: SimulationHistoryData) {
        if (busIndex in 0..9) {
            busSimulationHistoryData[busIndex].add(data)
        }
    }

    private fun addMarkerSimulation(
        activity1: Activity,
        index: Int,
        routeSimulationDataItem: BusRouteCoordinates
    ) {
        routeSimulationDataItem.coordinates?.get(0)?.let { longitude ->
            routeSimulationDataItem.coordinates?.get(1)
                ?.let { latitude -> LatLng(latitude, longitude) }
                .let {
                    routeSimulationDataItem.id?.let { id ->
                        it?.let { latLng ->
                            mMapHelper?.addMarkerSimulation(
                                id,
                                activity1,
                                latLng,
                                index
                            )
                        }
                    }
                }
        }
    }

    fun initSimulationView(
        fragmentActivity: FragmentActivity?,
        bottomSheetTrackSimulationBinding: BottomSheetTrackSimulationBinding,
        simulationInterface1: SimulationInterface
    ) {
        this.simulationInterface = simulationInterface1
        this.mFragmentActivity = fragmentActivity
        this.simulationBinding = bottomSheetTrackSimulationBinding
        initSimulationBottomSheet()
    }

    private fun initSimulationBottomSheet() {
        simulationBinding?.apply {
            mBottomSheetSimulationBehavior = BottomSheetBehavior.from(root)
            mBottomSheetSimulationBehavior?.isHideable = true
            mBottomSheetSimulationBehavior?.isDraggable = true
            mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            mBottomSheetSimulationBehavior?.isFitToContents = false
            mBottomSheetSimulationBehavior?.halfExpandedRatio = 0.6f

            mBottomSheetSimulationBehavior?.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                imgAmazonLogoTrackingSheet.alpha = 1f
                                ivAmazonInfoTrackingSheet.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                imgAmazonLogoTrackingSheet.alpha = 0f
                                ivAmazonInfoTrackingSheet.alpha = 0f
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                                imgAmazonLogoTrackingSheet.alpha = 1f
                                ivAmazonInfoTrackingSheet.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_HIDDEN -> {}
                            BottomSheetBehavior.STATE_SETTLING -> {}
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }
                })
            initClick()
            initAdapter()
            setSpinnerData()
            setSelectedNotificationCount()
            if ((activity as MainActivity).isTablet) {
                val languageCode = getLanguageCode()
                val isRtl =
                    languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW || languageCode == LANGUAGE_CODE_HEBREW_1
                if (isRtl) {
                    ViewCompat.setLayoutDirection(clPersistentBottomSheetSimulation, ViewCompat.LAYOUT_DIRECTION_RTL)
                }
            }
        }
    }

    private fun drawSimulationPolygonCircle(circleCenter: Point, radius: Int, index: String) {
        mMapboxMap?.getStyle { style ->
            // Use Turf to calculate the Polygon's coordinates
            val polygonArea: Polygon = getTurfPolygon(circleCenter, radius.toDouble())
            val pointList = TurfMeta.coordAll(polygonArea, false)

            // Update the source's GeoJSON to draw a new fill circle
            val polygonCircleSource =
                style.getSourceAs<GeoJsonSource>(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + index)
            polygonCircleSource?.setGeoJson(
                Polygon.fromOuterInner(
                    LineString.fromLngLats(pointList)
                )
            )
        }
    }

    /**
     * Use the Turf library {@link TurfTransformation#circle(Point, double, int, String)} method to
     * retrieve a {@link Polygon} .
     *
     * @param centerPoint a {@link Point} which the circle will center around
     * @param radius      the radius of the circle
     * @return a Polygon which represents the newly created circle
     */
    private fun getTurfPolygon(
        centerPoint: Point,
        radius: Double
    ): Polygon {
        return TurfTransformation.circle(centerPoint, radius, 360, mCircleUnit)
    }

    private fun setSpinnerData() {
        simulationBinding?.apply {
            val adapter = ChangeBusSpinnerAdapter(spinnerChangeBus.context, notificationData)
            spinnerChangeBus.adapter = adapter

            spinnerChangeBus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (selectedTrackerIndex != position) {
                        selectedTrackerIndex = position
                        var isLocationStartNeeded = false
                        if (mIsLocationUpdateEnable) {
                            mIsLocationUpdateEnable = false
                            isLocationStartNeeded = true
                        }
                        CoroutineScope(Dispatchers.Default).launch {
                            delay(DELAY_PROCESS_1000)
                            withContext(Dispatchers.Main) {
                                simulationHistoryData.clear()
                                simulationHistoryData.addAll(
                                    getSelectedBusTrackingData(
                                        selectedTrackerIndex
                                    )
                                )
                                simulationTrackingListAdapter?.submitList(simulationHistoryData.toMutableList())
                            }
                            if (isLocationStartNeeded) {
                                mIsLocationUpdateEnable = true
                            }
                        }
                    }
                    val selectedData = notificationData[selectedTrackerIndex].name
                    val properties = listOf(
                        Pair(AnalyticsAttribute.SCREEN_NAME, AnalyticsAttributeValue.SIMULATION),
                        Pair(AnalyticsAttribute.BUS_NAME, selectedData)
                    )
                    (activity as MainActivity).analyticsHelper?.recordEvent(EventType.CHANGE_BUS_TRACKING_HISTORY, properties)
                    tvChangeRoute.text =
                        buildString {
                            append(selectedData.split(" ")[0])
                            append(" ")
                            append(selectedData.split(" ")[1])
                        }
                    tvChangeRouteName.text = selectedData.split(" ")[2]
                    adapter.setSelection(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do something when nothing is selected
                }
            }
        }
    }

    private fun setDefaultIconWithGeofence(index: String) {
        mMapboxMap?.getStyle { style ->
            if (style.getSource(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + index) == null) {
                style.addSource(GeoJsonSource(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + index))
            }
            initPolygonCircleFillLayer(index)
        }
        mIsDefaultGeofence = true
    }

    /**
     * Add a [FillLayer] to display a [Polygon] in a the shape of a circle.
     */
    private fun initPolygonCircleFillLayer(index: String) {
        mMapboxMap?.getStyle { style ->
            val fillLayer = FillLayer(
                GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + index,
                GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + index
            )

            mFragmentActivity?.applicationContext?.let {
                fillLayer.setProperties(
                    PropertyFactory.fillColor(
                        ContextCompat.getColor(
                            it,
                            R.color.color_bn_selected
                        )
                    ),
                    PropertyFactory.fillOutlineColor(
                        ContextCompat.getColor(
                            it,
                            R.color.color_bn_selected
                        )
                    ),
                    PropertyFactory.fillOpacity(0.2f)
                )
            }

            if (style.getLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + index) == null) {
                style.addLayerBelow(fillLayer, GeofenceCons.CIRCLE_CENTER_LAYER_ID + index)
            }
        }
    }

    private fun initClick() {
        simulationBinding?.apply {
            cardStartTracking.setOnClickListener {
                if ((activity as MainActivity).checkMapLoaded() && !viewLoader.isVisible) {
                    if (mIsLocationUpdateEnable) {
                        stopTracker()
                        stopMqttManager()
                    } else {
                        viewLoader.show()
                        tvStopTracking.hide()
                        cardStartTracking.isEnabled = false
                        mActivity?.let {
                            if (checkInternetConnection(it.applicationContext)) {
                                startMqttManager()
                            } else {
                                mIsLocationUpdateEnable = true
                                startTracking()
                            }
                        }
                    }
                }
            }
            ivBackArrowRouteNotifications.setOnClickListener {
                if (rvRouteNotifications.isVisible) {
                    closeNotificationCard()
                    if (!rvTrackingSimulation.isVisible) {
                        mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                } else {
                    showViews(rvRouteNotifications, viewDividerNotification)
                    ivBackArrowRouteNotifications.rotation = 180f
                    mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            ivBackArrowChangeRoute.setOnClickListener {
                if (rvTrackingSimulation.isVisible) {
                    closeTrackingCard()
                    if (!rvRouteNotifications.isVisible) {
                        mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                } else {
                    showViews(rvTrackingSimulation, viewDividerBus)
                    ivBackArrowChangeRoute.rotation = 180f
                    mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    private fun BottomSheetTrackSimulationBinding.closeNotificationCard() {
        hideViews(rvRouteNotifications, viewDividerNotification)
        ivBackArrowRouteNotifications.rotation = 0f
    }

    private fun BottomSheetTrackSimulationBinding.closeTrackingCard() {
        hideViews(rvTrackingSimulation, viewDividerBus)
        ivBackArrowChangeRoute.rotation = 0f
    }

    private fun BottomSheetTrackSimulationBinding.stopTracker() {
        mActivity?.getColor(R.color.color_primary_green)
            ?.let { it1 -> cardStartTracking.setCardBackgroundColor(it1) }
        tvStopTracking.text = mActivity?.getText(R.string.label_start_tracking)
        tvTrackingYourActivity.text =
            mActivity?.getText(R.string.label_tracking_in_active)
        tvTrackingYourActivity.context?.let {
            tvTrackingYourActivity.setTextColor(
                ContextCompat.getColor(
                    it,
                    R.color.color_hint_text
                )
            )
        }
    }

    private fun stopMqttManager() {
        mIsLocationUpdateEnable = false
        if (mqttManager != null) {
            try {
                mqttManager?.unsubscribeTopic("${mAWSLocationHelper.getCognitoCachingCredentialsProvider()?.identityId}/$TRACKER")
            } catch (_: Exception) {
            }

            try {
                mqttManager?.disconnect()
            } catch (_: Exception) {
            }
            mqttManager = null
            val properties = listOf(
                Pair(AnalyticsAttribute.SCREEN_NAME, AnalyticsAttributeValue.SIMULATION)
            )
            (activity as MainActivity).analyticsHelper?.recordEvent(
                EventType.STOP_TRACKING,
                properties
            )
        }
    }

    private fun startMqttManager() {
        if (mqttManager != null) stopMqttManager()
        val identityId: String? =
            mAWSLocationHelper.getCognitoCachingCredentialsProvider()?.identityId
        val defaultIdentityPoolId: String = Units.getDefaultIdentityPoolId(
            mPreferenceManager?.getValue(
                KEY_SELECTED_REGION,
                regionDisplayName[0]
            ),
            mPreferenceManager?.getValue(KEY_NEAREST_REGION, "")
        )
        mqttManager =
            AWSIotMqttManager(
                identityId,
                getSimulationWebSocketUrl(defaultIdentityPoolId)
            )
        mqttManager?.isAutoReconnect =
            false // To be able to display Exceptions and debug the problem.
        mqttManager?.keepAlive = 60
        mqttManager?.setCleanSession(true)
        try {
            val instance = mAWSLocationHelper.getCognitoCachingCredentialsProvider()
            mqttManager?.connect(instance) { status, throwable ->
                runOnUiThread {
                    when (status) {
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connecting -> {
                        }
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected -> {
                            mIsLocationUpdateEnable = true
                            startTracking()
                            identityId?.let { subscribeTopic(it) }
                        }
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting -> {
                        }
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost -> {
                            throwable?.printStackTrace()
                        }
                        else -> {
                            simulationBinding?.apply {
                                viewLoader.hide()
                                tvStopTracking.show()
                                cardStartTracking.isEnabled = true
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startTracking() {
        simulationBinding?.apply {
            mActivity?.getColor(R.color.color_red)
                ?.let { it1 -> cardStartTracking.setCardBackgroundColor(it1) }
            tvStopTracking.text =
                mActivity?.getText(R.string.label_stop_tracking)
            tvTrackingYourActivity.text =
                mActivity?.getText(R.string.label_tracking_active)
            tvTrackingYourActivity.context.let {
                tvTrackingYourActivity.setTextColor(
                    ContextCompat.getColor(
                        it,
                        R.color.color_red
                    )
                )
            }
            viewLoader.hide()
            tvStopTracking.show()
            cardStartTracking.isEnabled = true
            if (!isCoroutineStarted) {
                initData()
            } else {
                if (isAnyBusTrackerEnable()) {
                    return
                }
                busSimulationNotificationFlags?.set(0, true)
                notificationData[0].isSelected = true
                setSelectedNotificationCount()
                simulationNotificationAdapter?.notifyItemChanged(0)
                routeData?.let { route ->
                    mActivity?.let { activity ->
                        getFirstCoordinates()?.forEachIndexed { index, busRouteCoordinates ->
                            if (isBusTrackerNotificationEnable(index)) {
                                addMarkerSimulation(activity, index, busRouteCoordinates)
                            }
                        }
                    }
                    drawGeofenceWithPosition(0)
                    addPreDrawTrackerLine(route, 0)
                    zoomCamera()
                }
            }
            val properties = listOf(
                Pair(AnalyticsAttribute.SCREEN_NAME, AnalyticsAttributeValue.SIMULATION)
            )
            (activity as MainActivity).analyticsHelper?.recordEvent(EventType.START_TRACKING, properties)
        }
    }

    private fun subscribeTopic(identityId: String) {
        try {
            mqttManager?.subscribeToTopic(
                "$identityId/$TRACKER",
                AWSIotMqttQos.QOS0
            ) { _, data ->

                val stringData = String(data)
                if (stringData.isNotEmpty()) {
                    val notificationData =
                        Gson().fromJson(stringData, NotificationSimulationData::class.java)
                    var subTitle = ""
                    mFragmentActivity?.let {
                        if (notificationData.trackerEventType.equals(ENTER, true)) {
                            subTitle = "${it.getString(R.string.label_bus)} ${
                            notificationData.geofenceId?.split("-")?.get(0)?.split("_")?.get(2)
                            }: ${it.getString(R.string.label_entered)} ${notificationData.stopName} ${
                            it.getString(
                                R.string.label_geofence
                            )
                            }"
                            routeData?.forEachIndexed { index, routeSimulationDataItem ->
                                if (routeSimulationDataItem.geofenceCollection == notificationData.geofenceCollection) {
                                    busStopCounts?.set(
                                        index,
                                        busStopCounts?.get(index)?.plus(1) ?: 0
                                    )
                                    busStopCounts?.get(index)
                                        ?.let { addDataInList(index, it, notificationData) }
                                    return@forEachIndexed
                                }
                            }
                        } else {
                            subTitle = "${it.getString(R.string.label_bus)} ${
                            notificationData.geofenceId?.split("-")?.get(0)?.split("_")?.get(2)
                            }: ${it.getString(R.string.label_exited)} ${notificationData.stopName} ${
                            it.getString(
                                R.string.label_geofence
                            )
                            }"
                        }
                    }
                    notificationId++
                    mActivity?.let {
                        if (notificationHelper?.isNotificationGroupActive() == true) {
                            notificationHelper?.showNotification(
                                notificationId,
                                subTitle,
                                false
                            )
                        } else {
                            notificationHelper?.showNotification(
                                notificationId,
                                subTitle,
                                true
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addDataInList(
        selectedIndex: Int,
        busStopCount: Int,
        notificationData: NotificationSimulationData
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            delay(DELAY_PROCESS_1000)
            withContext(Dispatchers.Main) {
            }
        }
        mFragmentActivity?.applicationContext?.let {
            busSimulationHistoryData[selectedIndex].add(
                SimulationHistoryData(
                    it.getString(R.string.label_position_data),
                    true,
                    busStopCount,
                    notificationData.coordinates?.get(0)?.let { longitude ->
                        notificationData.coordinates?.get(1)?.let { latitude ->
                            SimulationHistoryInnerData(
                                latitude,
                                longitude,
                                Date()
                            )
                        }
                    }
                )
            )
        }
    }

    private fun initAdapter() {
        val notificationLayoutManager = LinearLayoutManager(mActivity?.applicationContext)
        simulationNotificationAdapter = SimulationNotificationAdapter(
            notificationData,
            object : SimulationNotificationAdapter.NotificationInterface {
                override fun click(position: Int, isSelected: Boolean) {
                    notificationData[position].isSelected = isSelected
                    busSimulationNotificationFlags?.set(position, isSelected)
                    setSelectedNotificationCount()
                    routeData?.let { route ->
                        if (isSelected) {
                            addPreDrawTrackerLine(route, position)
                            drawGeofenceWithPosition(position)
                            mActivity?.let { activity ->
                                val data = getFirstCoordinates(position)
                                addMarkerSimulation(activity, position, data)
                            }
                            val properties = listOf(
                                Pair(AnalyticsAttribute.SCREEN_NAME, AnalyticsAttributeValue.SIMULATION),
                                Pair(AnalyticsAttribute.BUS_NAME, notificationData[position].name)
                            )
                            (activity as MainActivity).analyticsHelper?.recordEvent(EventType.ENABLE_NOTIFICATION, properties)
                        } else {
                            val needToStart = mIsLocationUpdateEnable
                            mIsLocationUpdateEnable = false
                            (activity as MainActivity).lifecycleScope.launch {
                                delay(CLICK_DEBOUNCE_ENABLE)
                                busSimulationHistoryData[position].clear()
                                removePreDrawTrackerLine(route, position)
                                removeGeofenceWithPosition(position)
                                timerCounts?.set(position, 0)
                                mMapHelper?.removeGeoJsonSourceData(position)
                                if (needToStart) {
                                    mIsLocationUpdateEnable = true
                                }
                            }
                            val properties = listOf(
                                Pair(AnalyticsAttribute.SCREEN_NAME, AnalyticsAttributeValue.SIMULATION),
                                Pair(AnalyticsAttribute.BUS_NAME, notificationData[position].name)
                            )
                            activity.analyticsHelper?.recordEvent(EventType.DISABLE_NOTIFICATION, properties)
                        }
                        zoomCamera()
                    }
                }
            }
        )
        simulationBinding?.rvRouteNotifications?.adapter = simulationNotificationAdapter
        simulationBinding?.rvRouteNotifications?.layoutManager = notificationLayoutManager

        val layoutManager = LinearLayoutManager(mActivity?.applicationContext)
        simulationTrackingListAdapter = SimulationListAdapter()
        simulationBinding?.rvTrackingSimulation?.adapter = simulationTrackingListAdapter
        simulationBinding?.rvTrackingSimulation?.layoutManager = layoutManager
    }

    private fun setSelectedNotificationCount() {
        var totalCount = 0
        busSimulationNotificationFlags?.forEach {
            if (it) {
                totalCount++
            }
        }
        simulationBinding?.apply {
            tvRouteNotificationsName.text = buildString {
                append(totalCount)
                append(" ")
                if (totalCount > 1) append(tvRouteNotificationsName.context.getString(R.string.routes_active)) else append(tvRouteNotificationsName.context.getString(R.string.route_active))
            }
        }
    }

    fun isSimulationBottomSheetVisible(): Boolean {
        return mBottomSheetSimulationBehavior?.state != BottomSheetBehavior.STATE_HIDDEN
    }

    fun setSimulationDraggable(isDrag: Boolean) {
        if (!isDrag) {
            mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        mBottomSheetSimulationBehavior?.isDraggable = isDrag
    }

    fun setSimulationDraggable() {
        setSimulationDraggable(true)
    }

    fun setSimulationData() {
        setBounds()
        mMapHelper?.setGeoJsonSourceEmpty()
        notificationData.forEachIndexed { index, notificationData ->
            if (notificationData.isSelected) {
                val coordinatesSize = routeData?.get(index)?.coordinates?.size ?: 0
                if (coordinatesSize > 0) {
                    timerCounts?.let {
                        val currentIndex = (it[index]) % coordinatesSize
                        val data = BusRouteCoordinates(
                            routeData?.get(index)?.id,
                            routeData?.get(index)?.geofenceCollection,
                            routeData?.get(index)?.coordinates?.get(currentIndex)
                        )
                        mActivity?.let { activity ->
                            addMarkerSimulation(activity, index, data)
                        }
                        drawGeofenceWithPosition(index)
                        if (!mIsLocationUpdateEnable) {
                            routeData?.let { route ->
                                addTrackerLine(
                                    mPreDrawTrackerLine[index] as ArrayList<Point>,
                                    route,
                                    index
                                )
                                if (busesCoordinates[index].size > 1) {
                                    mMapHelper?.addTrackerLine(
                                        busesCoordinates[index],
                                        true,
                                        "$LAYER${route[index].id}",
                                        "$SOURCE${route[index].id}",
                                        R.color.color_primary_green
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        (activity as MainActivity).lifecycleScope.launch {
            delay(DELAY_PROCESS_1000)
            zoomCamera()
        }
    }

    fun hideSimulationBottomSheet() {
        mMapboxMap?.removeViewBounds()
        simulationBinding?.apply {
            closeTrackingCard()
            closeNotificationCard()
            clPersistentBottomSheetSimulation.hide()
        }
        simulationBinding?.apply {
            stopTracker()
        }
        isCoroutineStarted = false
        coroutineScope.cancel()
        (activity as MainActivity).lifecycleScope.launch {
            delay(DELAY_PROCESS_1000)
            routeData?.forEach { routeSimulationDataItem ->
                routeSimulationDataItem.id?.let { it1 ->
                    mMapHelper?.removeSource("$SOURCE_SIMULATION_ICON$it1")
                    mMapHelper?.removeLayer("$LAYER_SIMULATION_ICON$it1")
                    mMapHelper?.removeLayer("$LAYER$it1")
                    mMapHelper?.removeLayer("$SOURCE$it1")
                    mMapHelper?.removeLayer("$LAYER$it1$LABEL_PRE_DRAW")
                    mMapHelper?.removeLayer("$SOURCE$it1$LABEL_PRE_DRAW")
                    mMapHelper?.removeLayer("$LAYER${it1}$LABEL_IN_BETWEEN")
                    mMapHelper?.removeSource("$SOURCE${it1}$LABEL_IN_BETWEEN")
                    mMapHelper?.removeLayer("$LAYER${it1}$LABEL_PRE_DRAW$LABEL_IN_BETWEEN")
                    mMapHelper?.removeSource("$LAYER${it1}$LABEL_PRE_DRAW$LABEL_IN_BETWEEN")
                }
            }
            mMapHelper?.clearMarker()
            mMapHelper?.removeLine()
            mMapHelper?.removeSimulationData()
            routeData?.clear()
            routeData = null
            notificationData.forEach { data ->
                data.isSelected = false
            }
            notificationId = 1
            busSimulationNotificationFlags = null
            mIsLocationUpdateEnable = false
        }
        mGeofenceList.forEachIndexed { index, data ->
            data.forEachIndexed { indexInner, _ ->
                mMapboxMap?.style?.removeLayer(GeofenceCons.CIRCLE_CENTER_LAYER_ID + "$index$indexInner")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + "$index$indexInner")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index$indexInner")
            }
        }
        mGeofenceList.clear()
        hideSimulationSheet(activity)
    }

    private fun hideSimulationSheet(activity: MainActivity) {
        mBottomSheetSimulationBehavior.let {
            it?.isHideable = true
            it?.state = BottomSheetBehavior.STATE_HIDDEN
            it?.isFitToContents = false
            stopMqttManager()
            activity.reInitializeSimulation()
        }
    }

    fun manageGeofenceListUI(dataGeofence: SimulationGeofenceData) {
        val position =
            simulationCollectionName.indexOfFirst { it.contains(dataGeofence.collectionName) }
        mGeofenceList[position].clear()
        mGeofenceList[position] = dataGeofence.devicePositionData
        if (isBusTrackerNotificationEnable(position)) {
            drawGeofenceWithPosition(position)
        }
    }

    private fun MapboxMap.limitViewToBounds(bounds: LatLngBounds) {
        val newBoundsHeight =
            bounds.latitudeSpan - projection.visibleRegion.latLngBounds.latitudeSpan
        val newBoundsWidth =
            bounds.longitudeSpan - projection.visibleRegion.latLngBounds.longitudeSpan
        val leftTopLatLng = LatLng(
            bounds.latNorth - (bounds.latitudeSpan - newBoundsHeight) / 2,
            bounds.lonEast - (bounds.longitudeSpan - newBoundsWidth) / 2 - newBoundsWidth
        )
        val rightBottomLatLng = LatLng(
            bounds.latNorth - (bounds.latitudeSpan - newBoundsHeight) / 2 - newBoundsHeight,
            bounds.lonEast - (bounds.longitudeSpan - newBoundsWidth) / 2
        )
        val newBounds = LatLngBounds.Builder()
            .include(leftTopLatLng)
            .include(rightBottomLatLng)
            .build()
        setLatLngBoundsForCameraTarget(newBounds)
    }

    private fun MapboxMap.removeViewBounds() {
        // Set the LatLngBounds for the camera target to null
        setLatLngBoundsForCameraTarget(null)
        style?.let { mMapHelper?.updateZoomRange(it) }
        mMapHelper?.checkLocationComponentEnable((mActivity as BaseActivity), false)
    }
}
