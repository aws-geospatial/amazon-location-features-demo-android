package com.aws.amazonlocation.ui.main.tracking

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.view.View
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
import com.amazonaws.services.geo.model.DevicePosition
import com.amazonaws.services.geo.model.GetDevicePositionHistoryResult
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.MarkerEnum
import com.aws.amazonlocation.data.enum.TrackingEnum
import com.aws.amazonlocation.data.response.TrackingHistoryData
import com.aws.amazonlocation.databinding.BottomSheetTrackingBinding
import com.aws.amazonlocation.domain.`interface`.TrackingInterface
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.simulation.SimulationBottomSheetFragment
import com.aws.amazonlocation.ui.main.welcome.WelcomeBottomSheetFragment
import com.aws.amazonlocation.utils.*
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfConstants
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfTransformation
import com.aws.amazonlocation.utils.stickyHeaders.StickyHeaderDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.JsonParser
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

class TrackingUtils(
    val mPreferenceManager: PreferenceManager? = null,
    val activity: Activity?,
    val mAWSLocationHelper: AWSLocationHelper
) {
    private var imageId: Int = 0
    private var headerIdsToRemove = arrayListOf<String>()
    private var sourceIdsToRemove = arrayListOf<String>()
    private var mqttManager: AWSIotMqttManager? = null
    private var adapter: TrackingHistoryAdapter? = null
    private var mBottomSheetTrackingBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var mBindingTracking: BottomSheetTrackingBinding? = null
    private var mFragmentActivity: FragmentActivity? = null
    private var mTrackingInterface: TrackingInterface? = null
    private var mMapHelper: MapHelper? = null
    private var mMapboxMap: MapboxMap? = null
    private var mClient: AmazonLocationClient? = null
    private var mActivity: Activity? = null
    private var mIsLocationUpdateEnable = false
    private var mGeofenceList = ArrayList<ListGeofenceResponseEntry>()
    private var mIsDefaultGeofence = false
    private var isLoading = true
    private val mCircleUnit: String = TurfConstants.UNIT_METERS
    private var trackingHistoryData = arrayListOf<TrackingHistoryData>()

    private var headerId = 0
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

    fun isTrackingExpandedOrHalfExpand(): Boolean {
        return mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_EXPANDED || mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun isTrackingSheetCollapsed(): Boolean {
        return mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED
    }

    fun isTrackingSheetHidden(): Boolean {
        return mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_HIDDEN || mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_SETTLING
    }

    fun collapseTracking() {
        mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        mBottomSheetTrackingBehavior?.isDraggable = true
    }

    fun showTrackingBottomSheet(enableTracking: TrackingEnum) {
        mBottomSheetTrackingBehavior?.isHideable = false
        when (enableTracking) {
            TrackingEnum.ENABLE_TRACKING -> {
                mBottomSheetTrackingBehavior?.isDraggable = true
                mBottomSheetTrackingBehavior?.isFitToContents = false
                mBindingTracking?.clTracking?.post {
                    val clEnableTrackingInnerHeight =
                        mBindingTracking?.clEnableTrackingInner?.height ?: 0
                    val clPersistentBottomSheetHeight =
                        mBindingTracking?.clPersistentBottomSheet?.height ?: 0
                    val clTrackingHeight =
                        mBindingTracking?.clTracking?.height ?: 0

                    val topLogoMargin = clPersistentBottomSheetHeight - clTrackingHeight
                    val screenHeight = (mActivity as MainActivity).resources.displayMetrics.heightPixels
                    val isTablet = (mActivity as MainActivity).isTablet
                    val bottomNavHeight = if (isTablet) (mActivity as MainActivity).getBottomNavHeight().toFloat() else 0f
                    val topLogoHeight = if (isTablet) 0f else topLogoMargin.toFloat()

                    val halfExpandedRatio = if (screenHeight.toFloat() != 0.toFloat()) {
                        ((clEnableTrackingInnerHeight.toFloat() + bottomNavHeight + topLogoHeight)) / screenHeight
                    } else {
                        0.55f
                    }
                    mBottomSheetTrackingBehavior?.halfExpandedRatio = halfExpandedRatio
                }
                mBindingTracking?.clEnableTracking?.context?.let {
                    if ((activity as MainActivity).isTablet) {
                        mBottomSheetTrackingBehavior?.peekHeight = it.resources.getDimensionPixelSize(R.dimen.dp_150)
                    } else {
                        mBottomSheetTrackingBehavior?.peekHeight = it.resources.getDimensionPixelSize(R.dimen.dp_110)
                    }
                    mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
                mBindingTracking?.apply {
                    clPersistentBottomSheet.show()
                    clEnableTracking.show()
                    clTracking.hide()
                }
            }
            TrackingEnum.TRACKING_HISTORY -> {
                mTrackingInterface?.getCheckPermission()
            }
        }
    }

    fun initTrackingView(
        fragmentActivity: FragmentActivity?,
        bottomSheetGeofenceList: BottomSheetTrackingBinding,
        mGeofenceInterface: TrackingInterface
    ) {
        this.mTrackingInterface = mGeofenceInterface
        this.mFragmentActivity = fragmentActivity
        this.mBindingTracking = bottomSheetGeofenceList
        initTrackingBottomSheet()
    }

    fun locationPermissionAdded() {
        mBindingTracking?.apply {
            clEnableTracking.hide()
            clTracking.show()
            mTrackingInterface?.getGeofenceList(GeofenceCons.GEOFENCE_COLLECTION)

            trackingHistoryData.clear()
            getCurrentDateData()

            val clTracking = mBindingTracking?.clTracking?.height ?: 0
            val clPersistentBottomSheetHeight = mBindingTracking?.clPersistentBottomSheet?.height ?: 0
            val clTrackingInnerHeight = mBindingTracking?.clTrackingInner?.height ?: 0
            val totalHeight = (clPersistentBottomSheetHeight - clTracking) + clTrackingInnerHeight
            mBottomSheetTrackingBehavior?.peekHeight = totalHeight

            mBottomSheetTrackingBehavior?.isDraggable = true
            if ((activity as MainActivity).isTablet) {
                mBottomSheetTrackingBehavior?.peekHeight =
                    totalHeight
            } else {
                mBottomSheetTrackingBehavior?.peekHeight =
                    totalHeight
            }
            mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    private fun initTrackingBottomSheet() {
        mBindingTracking?.apply {
            imageId = 0
            mBottomSheetTrackingBehavior = BottomSheetBehavior.from(root)
            mBottomSheetTrackingBehavior?.isHideable = true
            mBottomSheetTrackingBehavior?.isDraggable = true
            mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            mBottomSheetTrackingBehavior?.isFitToContents = false
            mBottomSheetTrackingBehavior?.halfExpandedRatio = 0.58f
            btnEnableTracking.setOnClickListener {
                (activity as MainActivity).openCloudFormation()
            }
            cardTrackerGeofenceSimulation.hide()
            btnTryTracker.setOnClickListener {
                mPreferenceManager?.let {
                    if (isGrabMapSelected(it, btnTryTracker.context)) {
                        mActivity?.changeDataProviderDialog(object : ChangeDataProviderInterface {
                            override fun changeDataProvider(dialog: DialogInterface) {
                                mActivity?.getString(R.string.map_light)?.let { it1 ->
                                    mPreferenceManager.setValue(
                                        KEY_MAP_STYLE_NAME,
                                        it1
                                    )
                                }
                                mActivity?.getString(R.string.esri)?.let { it1 ->
                                    mPreferenceManager.setValue(
                                        KEY_MAP_NAME,
                                        it1
                                    )
                                }
                                (activity as MainActivity).lifecycleScope.launch {
                                    if (!isRunningTest) {
                                        delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                                        activity.restartApplication()
                                    }
                                }
                            }
                        })
                    } else {
                        openSimulationWelcome()
                    }
                }
            }
            if ((activity as MainActivity).isTablet) {
                val languageCode = getLanguageCode()
                val isRtl =
                    languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW || languageCode == LANGUAGE_CODE_HEBREW_1
                if (isRtl) {
                    ViewCompat.setLayoutDirection(clPersistentBottomSheet, ViewCompat.LAYOUT_DIRECTION_RTL)
                }
            }
            tvDeleteTrackingData.setOnClickListener {
                mActivity?.deleteTrackingDataDialog(object : DeleteTrackingDataInterface {
                    override fun deleteData(dialog: DialogInterface) {
                        mTrackingInterface?.getDeleteTrackingData()
                    }
                })
            }
            cardStartTracking.setOnClickListener {
                val mIdentityPoolId = mPreferenceManager?.getValue(
                    KEY_POOL_ID,
                    ""
                )
                val regionData = mPreferenceManager?.getValue(
                    KEY_USER_REGION,
                    ""
                )
                if (!validateIdentityPoolId(mIdentityPoolId, regionData)) {
                    mActivity?.getString(R.string.reconnect_with_aws_account)
                        ?.let { it1 -> activity.showError(it1) }
                    activity.restartAppWithClearData()
                    return@setOnClickListener
                }
                if (mIsLocationUpdateEnable) {
                    mActivity?.getColor(R.color.color_primary_green)
                        ?.let { it1 -> cardStartTracking.setCardBackgroundColor(it1) }
                    tvStopTracking.text = mActivity?.getText(R.string.label_start_tracking)
                    tvTrackingYourActivity.text =
                        mActivity?.getText(R.string.label_not_tracking_your_activity)
                    tvTrackingYourActivity.context?.let {
                        tvTrackingYourActivity.setTextColor(
                            ContextCompat.getColor(
                                it,
                                R.color.color_hint_text
                            )
                        )
                    }
                    mTrackingInterface?.removeUpdateBatch()
                    stopMqttManager()
                } else {
                    viewLoader.show()
                    tvStopTracking.hide()
                    cardStartTracking.isEnabled = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        startMqttManager()
                    }, 10)
                }
            }
            mBottomSheetTrackingBehavior?.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                if (!clEnableTracking.isVisible) {
                                    imgAmazonLogoTrackingSheet?.alpha = 1f
                                    ivAmazonInfoTrackingSheet?.alpha = 1f
                                }
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                imgAmazonLogoTrackingSheet?.alpha = 0f
                                ivAmazonInfoTrackingSheet?.alpha = 0f
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                                imgAmazonLogoTrackingSheet?.alpha = 1f
                                ivAmazonInfoTrackingSheet?.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_HIDDEN -> {}
                            BottomSheetBehavior.STATE_SETTLING -> {}
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }
                })
            initAdapter()
        }
    }

    private fun openSimulationWelcome() {
        val simulationBottomSheetFragment = SimulationBottomSheetFragment()
        (activity as MainActivity).supportFragmentManager.let {
            simulationBottomSheetFragment.show(
                it,
                WelcomeBottomSheetFragment::javaClass.name
            )
        }
    }

    private fun stopMqttManager() {
        mIsLocationUpdateEnable = false
        if (mqttManager != null) {
            try {
                mqttManager?.unsubscribeTopic("${mAWSLocationHelper.getCognitoCachingCredentialsProvider()?.identityId}/tracker")
            } catch (_: Exception) {
            }

            try {
                mqttManager?.disconnect()
            } catch (_: Exception) {
            }
            mqttManager = null
            val properties = listOf(
                Pair(AnalyticsAttribute.SCREEN_NAME, AnalyticsAttributeValue.TRACKERS)
            )
            (activity as MainActivity).analyticsHelper?.recordEvent(
                EventType.STOP_TRACKING,
                properties
            )
        }
    }

    private fun startMqttManager() {
        mIsLocationUpdateEnable = true
        if (mqttManager != null) stopMqttManager()
        val identityId: String? =
            mAWSLocationHelper.getCognitoCachingCredentialsProvider()?.identityId

        mqttManager =
            AWSIotMqttManager(identityId, mPreferenceManager?.getValue(WEB_SOCKET_URL, ""))
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
                            startTracking()
                            identityId.let {
                                if (it != null) {
                                    subscribeTopic(it)
                                }
                            }
                        }
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting -> {
                        }
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost -> {
                            throwable?.printStackTrace()
                            if (mIsLocationUpdateEnable) {
                                startTracking()
                            }
                            mBindingTracking?.apply {
                                viewLoader.hide()
                                tvStopTracking.show()
                                cardStartTracking.isEnabled = true
                            }
                        }
                        else -> {
                            mBindingTracking?.apply {
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
        mBindingTracking?.apply {
            mActivity?.getColor(R.color.color_red)
                ?.let { it1 -> cardStartTracking.setCardBackgroundColor(it1) }
            tvStopTracking.text =
                mActivity?.getText(R.string.label_stop_tracking)
            tvTrackingYourActivity.text =
                mActivity?.getText(R.string.label_tracking_your_activity)
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
        }
        val latLng = mMapHelper?.getLiveLocation()
        latLng?.let { updateLatLngOnMap(it) }
        mTrackingInterface?.updateBatch()
        val properties = listOf(
            Pair(AnalyticsAttribute.SCREEN_NAME, AnalyticsAttributeValue.TRACKERS)
        )
        (activity as MainActivity).analyticsHelper?.recordEvent(EventType.START_TRACKING, properties)
    }

    fun updateLatLngOnMap(latLng: LatLng) {
        mTrackingInterface?.updateBatch(latLng)
    }

    private fun subscribeTopic(identityId: String) {
        try {
            mqttManager?.subscribeToTopic(
                "$identityId/tracker",
                AWSIotMqttQos.QOS0
            ) { _, data ->

                val stringData = String(data)
                if (stringData.isNotEmpty()) {
                    val jsonObject = JsonParser.parseString(stringData).asJsonObject
                    val type = jsonObject.get("trackerEventType").asString
                    val geofenceName = jsonObject.get("geofenceId").asString
                    val subTitle = if (type.equals("ENTER", true)) {
                        val propertiesAws = listOf(
                            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.TRACKERS),
                            Pair(AnalyticsAttribute.GEOFENCE_ID, geofenceName),
                            Pair(AnalyticsAttribute.EVENT_TYPE, AnalyticsAttributeValue.ENTER)
                        )
                        (activity as MainActivity).analyticsHelper?.recordEvent(EventType.GEO_EVENT_TRIGGERED, propertiesAws)
                        "${mFragmentActivity?.getString(R.string.label_tracker_entered)} $geofenceName"
                    } else {
                        val propertiesAws = listOf(
                            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.TRACKERS),
                            Pair(AnalyticsAttribute.GEOFENCE_ID, geofenceName),
                            Pair(AnalyticsAttribute.EVENT_TYPE, AnalyticsAttributeValue.EXIT)
                        )
                        (activity as MainActivity).analyticsHelper?.recordEvent(EventType.GEO_EVENT_TRIGGERED, propertiesAws)
                        "${mFragmentActivity?.getString(R.string.label_tracker_exited)} $geofenceName"
                    }
                    runOnUiThread {
                        activity.messageDialog(
                            title = geofenceName,
                            subTitle = subTitle,
                            false,
                            object : MessageInterface {
                                override fun onMessageClick(dialog: DialogInterface) {
                                    dialog.dismiss()
                                }
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCurrentDateData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val dateEnd = calendar.time

        calendar.add(Calendar.YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val dateStart = calendar.time

        mTrackingInterface?.getLocationHistory(dateStart, dateEnd)
    }

    fun getTodayData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val dateStart = calendar.time
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val dateEnd = calendar.time

        mTrackingInterface?.getTodayLocationHistory(dateStart, dateEnd)
    }

    private fun checkAndSetDate(date: Date): String {
        val df = SimpleDateFormat(DateFormat.MMM_DD_YYYY, Locale.getDefault())
        val formattedDate: String = df.format(date)

        val isToday = DateUtils.isToday(date.time)
        return if (isToday) {
            buildString {
                append(mFragmentActivity?.getString(R.string.label_today))
                append(", ")
                append(formattedDate)
            }
        } else {
            formattedDate
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun hideTrackingBottomSheet() {
        mBottomSheetTrackingBehavior.let {
            it?.isHideable = true
            it?.state = BottomSheetBehavior.STATE_HIDDEN
            it?.isFitToContents = false
            mMapHelper?.clearMarker()
            mMapHelper?.removeLine()
            sourceIdsToRemove.let { list ->
                for (data in list) {
                    mMapHelper?.removeSource(data)
                }
            }
            headerIdsToRemove.let { list ->
                for (data in list) {
                    mMapHelper?.removeLayer(data)
                }
            }
            mGeofenceList.forEachIndexed { index, _ ->
                mMapboxMap?.style?.removeLayer(GeofenceCons.CIRCLE_CENTER_LAYER_ID + "$index")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + "$index")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index")
            }
            trackingHistoryData.clear()
            if (adapter != null) {
                adapter?.notifyDataSetChanged()
            }
            if (mIsLocationUpdateEnable) {
                mBindingTracking?.apply {
                    mActivity?.getColor(R.color.color_primary_green)
                        ?.let { it1 -> cardStartTracking.setCardBackgroundColor(it1) }
                    tvStopTracking.text = mActivity?.getText(R.string.label_start_tracking)
                }
                mTrackingInterface?.removeUpdateBatch()
                mIsLocationUpdateEnable = !mIsLocationUpdateEnable
            }
            stopMqttManager()
        }
    }

    fun manageGeofenceListUI(list: ArrayList<ListGeofenceResponseEntry>) {
        mGeofenceList.clear()
        mGeofenceList.addAll(list)
        val properties = listOf(
            Pair(AnalyticsAttribute.NUMBER_OF_TRACKER_POINTS, mGeofenceList.size.toString()),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.TRACKERS)
        )
        (activity as MainActivity).analyticsHelper?.recordEvent(EventType.TRACKER_SAVED, properties)
        if (mGeofenceList.isNotEmpty()) {
            val mLatLngList = ArrayList<LatLng>()
            mGeofenceList.forEachIndexed { index, data ->
                val latLng = LatLng(data.geometry.circle.center[1], data.geometry.circle.center[0])
                if (checkGeofenceInsideGrab(latLng, mPreferenceManager, mActivity?.applicationContext)) {
                    setDefaultIconWithGeofence(index)
                    mLatLngList.add(latLng)
                    drawGeofence(
                        fromLngLat(latLng.longitude, latLng.latitude),
                        data.geometry.circle.radius.toInt(),
                        index
                    )
                }
            }
            mMapHelper?.adjustMapBounds(
                mLatLngList,
                mActivity?.resources?.getDimension(R.dimen.dp_130)?.toInt()!!
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteTrackingData() {
        imageId = 0
        mMapHelper?.clearMarker()
        mMapHelper?.removeLine()
        sourceIdsToRemove.let {
            for (data in it) {
                mMapHelper?.removeSource(data)
            }
        }
        headerIdsToRemove.let {
            for (data in it) {
                mMapHelper?.removeLayer(data)
            }
        }
        trackingHistoryData.clear()
        if (adapter != null) {
            adapter?.notifyDataSetChanged()
        }
        mBindingTracking?.apply {
            rvTracking.hide()
            cardList.hide()
            tvDeleteTrackingData.hide()
            mBindingTracking?.layoutNoDataFound?.apply {
                ivSearchNoDataFound.setImageDrawable(
                    ContextCompat.getDrawable(
                        ivSearchNoDataFound.context,
                        R.drawable.ic_tracking
                    )
                )
                tvMakeSureSpelledCorrect.text = ""
                tvNoMatchingFound.text =
                    activity?.resources?.getString(R.string.no_tracking_history_found)
                groupNoSearchFound.show()
                root.show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun locationHistoryListUI(data: GetDevicePositionHistoryResult) {
        headerId = 0
        sourceIdsToRemove.clear()
        headerIdsToRemove.clear()
        if (!data.devicePositions.isNullOrEmpty()) {
            mBindingTracking?.tvDeleteTrackingData?.show()
            mBindingTracking?.clSearchLoaderSheetTracking?.root?.hide()
            mBindingTracking?.cardList?.show()
            mMapHelper?.clearMarker()
            mMapHelper?.removeLine()
            sourceIdsToRemove.let {
                for (t in it) {
                    mMapHelper?.removeSource(t)
                }
            }
            headerIdsToRemove.let {
                for (t in it) {
                    mMapHelper?.removeLayer(t)
                }
            }
            val coordinates = arrayListOf<Point>()
            val latLngList = arrayListOf<LatLng>()
            headerId++
            var lastDate = ""
            var firstData = true
            var lastData = false
            val devicePositionList = arrayListOf<DevicePosition>()
            devicePositionList.addAll(data.devicePositions)
            devicePositionList.reverse()
            devicePositionList.forEachIndexed { index, devicePositionData ->
                val isToday = DateUtils.isToday(devicePositionData.sampleTime.time)
                val dateString = checkAndSetDate(devicePositionData.sampleTime)
                val latLng =
                    fromLngLat(devicePositionData.position[0], devicePositionData.position[1])
                latLngList.add(
                    LatLng(
                        devicePositionData.position[1],
                        devicePositionData.position[0]
                    )
                )
                coordinates.add(latLng)
                mActivity?.let {
                    mMapHelper?.addMarkerTracker(
                        "tracker$imageId",
                        it,
                        MarkerEnum.TRACKER_ICON,
                        LatLng(
                            devicePositionData.position[1],
                            devicePositionData.position[0]
                        )
                    )
                }
                if (isToday) {
                    imageId++
                    if (devicePositionList.size > (index + 1)) {
                        val isNextToday =
                            DateUtils.isToday(devicePositionList[index + 1].sampleTime.time)
                        if (!isNextToday) {
                            lastData = true
                        }
                    } else {
                        lastData = true
                    }
                    if (lastData) {
                        headerIdsToRemove.add("layerId$headerId")
                        sourceIdsToRemove.add("sourceId$headerId")
                        mMapHelper?.addTrackerLine(
                            coordinates,
                            "layerId$headerId",
                            "sourceId$headerId",
                            R.color.color_primary_green
                        )
                        setCameraZoomLevel()
                        coordinates.clear()
                        latLngList.clear()
                    }
                } else {
                    if (lastDate != dateString) {
                        headerId++
                        firstData = true
                        lastData = false
                    }
                    if (devicePositionList.size > (index + 1)) {
                        val nextDateString =
                            checkAndSetDate(devicePositionList[index + 1].sampleTime)
                        if (nextDateString != dateString) {
                            lastData = true
                        }
                    } else {
                        lastData = true
                    }
                    if (lastData) {
                        headerIdsToRemove.add("layerId$headerId")
                        sourceIdsToRemove.add("sourceId$headerId")
                        mMapHelper?.addTrackerLine(
                            coordinates,
                            "layerId$headerId",
                            "sourceId$headerId",
                            R.color.color_primary_green
                        )
                        setCameraZoomLevel()
                        coordinates.clear()
                        latLngList.clear()
                    }
                    lastDate = dateString
                }
                if (firstData) {
                    mActivity?.getString(R.string.label_position_start)?.let {
                        TrackingHistoryData(
                            headerId.toString(),
                            dateString,
                            it,
                            devicePositionData
                        )
                    }?.let {
                        trackingHistoryData.add(
                            it
                        )
                    }
                    firstData = false
                } else if (lastData) {
                    mActivity?.getString(R.string.label_position_end)?.let {
                        TrackingHistoryData(
                            headerId.toString(),
                            dateString,
                            it,
                            devicePositionData
                        )
                    }?.let {
                        trackingHistoryData.add(
                            it
                        )
                    }
                    lastData = false
                    firstData = true
                } else {
                    mActivity?.getString(R.string.label_position_data)?.let {
                        TrackingHistoryData(
                            headerId.toString(),
                            dateString,
                            it,
                            devicePositionData
                        )
                    }?.let {
                        trackingHistoryData.add(
                            it
                        )
                    }
                    lastData = false
                    firstData = false
                }
            }

            checkAndRemoveLoaderData()
            mBindingTracking?.apply {
                clNoData.hide()
                layoutNoDataFound.root.hide()
                cardList.show()
                rvTracking.show()
                clDatabase.show()
                clSearchLoaderSheetTracking.root.hide()
            }
            submitList()
        } else {
            mBindingTracking?.apply {
                clSearchLoaderSheetTracking.root.hide()
            }
            mBindingTracking?.layoutNoDataFound?.apply {
                ivSearchNoDataFound.setImageDrawable(
                    ContextCompat.getDrawable(
                        ivSearchNoDataFound.context,
                        R.drawable.ic_tracking
                    )
                )
                tvMakeSureSpelledCorrect.text = ""
                tvNoMatchingFound.text =
                    activity?.resources?.getString(R.string.no_tracking_history_found)
                groupNoSearchFound.show()
                root.show()
            }
            isLoading = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun locationHistoryTodayListUI(data: GetDevicePositionHistoryResult) {
        if (!data.devicePositions.isNullOrEmpty()) {
            imageId = 0
            val date: Date = Calendar.getInstance().time
            val dateString = checkAndSetDate(date)

            trackingHistoryData.removeIf { it.headerString == dateString }
            mBindingTracking?.tvDeleteTrackingData?.show()
            mMapHelper?.clearMarker()
            mMapHelper?.removeLine()
            sourceIdsToRemove.let {
                mMapHelper?.removeSource("sourceId1")
            }
            headerIdsToRemove.let {
                mMapHelper?.removeLayer("layerId1")
            }
            val coordinates = arrayListOf<Point>()
            val latLngList = arrayListOf<LatLng>()
            val trackingData = arrayListOf<TrackingHistoryData>()
            val devicePositionList = arrayListOf<DevicePosition>()
            devicePositionList.addAll(data.devicePositions)
            devicePositionList.reverse()
            devicePositionList.forEachIndexed { index, devicePositionData ->
                when (index) {
                    0 -> {
                        mActivity?.getString(R.string.label_position_start)?.let {
                            TrackingHistoryData(
                                "1",
                                dateString,
                                it,
                                devicePositionData
                            )
                        }?.let {
                            trackingData.add(
                                it
                            )
                        }
                    }
                    data.devicePositions.size - 1 -> {
                        mActivity?.getString(R.string.label_position_end)?.let {
                            TrackingHistoryData(
                                "1",
                                dateString,
                                it,
                                devicePositionData
                            )
                        }?.let {
                            trackingData.add(
                                it
                            )
                        }
                    }
                    else -> {
                        mActivity?.getString(R.string.label_position_data)?.let {
                            TrackingHistoryData(
                                "1",
                                dateString,
                                it,
                                devicePositionData
                            )
                        }?.let {
                            trackingData.add(
                                it
                            )
                        }
                    }
                }
                val latLng =
                    fromLngLat(devicePositionData.position[0], devicePositionData.position[1])
                latLngList.add(
                    LatLng(
                        devicePositionData.position[1],
                        devicePositionData.position[0]
                    )
                )
                coordinates.add(latLng)
                mActivity?.let {
                    mMapHelper?.addMarkerTracker(
                        "tracker$imageId",
                        it,
                        MarkerEnum.TRACKER_ICON,
                        LatLng(
                            devicePositionData.position[1],
                            devicePositionData.position[0]
                        )
                    )
                }
                imageId++
            }
            if (coordinates.isEmpty()) {
                val liveLocation = mMapHelper?.getLiveLocation()
                liveLocation?.let {
                    mMapHelper?.moveCameraToCurrentLocation(
                        LatLng(
                            it.latitude,
                            it.longitude
                        )
                    )
                }
            } else {
                headerIdsToRemove.add("layerId1")
                sourceIdsToRemove.add("sourceId1")
                mMapHelper?.addTrackerLine(
                    coordinates,
                    "layerId1",
                    "sourceId1",
                    R.color.color_primary_green
                )
                setCameraZoomLevel()
            }

            mBindingTracking?.apply {
                clNoData.hide()
                layoutNoDataFound.root.hide()
                cardList.show()
                rvTracking.show()
                clDatabase.show()
                clSearchLoaderSheetTracking.root.hide()
            }
            checkAndRemoveLoaderData()
            val existingData = arrayListOf<TrackingHistoryData>()
            existingData.addAll(trackingHistoryData)
            trackingHistoryData.clear()
            trackingHistoryData.addAll(trackingData)
            trackingHistoryData.addAll(existingData)
            submitList()
        }
    }

    private fun setCameraZoomLevel() {
        val liveLocation = mMapHelper?.getLiveLocation()
        liveLocation?.let {
            mMapHelper?.moveCameraToLocationTracker(
                LatLng(
                    it.latitude,
                    it.longitude
                )
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun submitList() {
        adapter?.submitList(trackingHistoryData)
        adapter?.notifyDataSetChanged()
        mBindingTracking?.apply {
            clNoData.hide()
            rvTracking.show()
            clDatabase.show()
        }
        isLoading = false
    }

    private fun checkAndRemoveLoaderData(): Boolean {
        var indexOfLoader = -1
        trackingHistoryData.forEachIndexed { index, data ->
            if (data.headerData == mActivity?.getString(R.string.label_position_loader)) {
                indexOfLoader = index
            }
        }
        if (indexOfLoader != -1) {
            trackingHistoryData.removeAt(indexOfLoader)
            return true
        }
        return false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initAdapter() {
        val layoutManager = LinearLayoutManager(mActivity?.applicationContext)
        adapter = TrackingHistoryAdapter()
        mBindingTracking?.rvTracking?.adapter = adapter
        adapter?.let {
            mBindingTracking?.rvTracking?.addItemDecoration(StickyHeaderDecoration(it, null))
        }
        mBindingTracking?.rvTracking?.layoutManager = layoutManager
        mActivity?.getString(R.string.label_position_loader)?.let {
            TrackingHistoryData(
                headerId.toString(),
                it,
                it,
                null
            )
        }?.let {
            trackingHistoryData.add(
                it
            )
        }
        adapter?.submitList(trackingHistoryData)
        adapter?.notifyDataSetChanged()
    }

    private fun setDefaultIconWithGeofence(index: Int) {
        mMapboxMap?.getStyle { style ->
            if (style.getSource(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index") == null) {
                style.addSource(GeoJsonSource(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index"))
            }
            initPolygonCircleFillLayer(index)
        }
        mIsDefaultGeofence = true
    }

    private fun drawGeofence(mapPoint: Point, radius: Int, index: Int) {
        drawPolygonCircle(mapPoint, radius, index)
    }

    /**
     * Add a [FillLayer] to display a [Polygon] in a the shape of a circle.
     */
    private fun initPolygonCircleFillLayer(index: Int) {
        mMapboxMap?.getStyle { style ->
            val fillLayer = FillLayer(
                GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + "$index",
                GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index"
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

            if (style.getLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + "$index") == null) {
                style.addLayerBelow(fillLayer, GeofenceCons.CIRCLE_CENTER_LAYER_ID + "$index")
            }
        }
    }

    /**
     * Update the [FillLayer] based on the GeoJSON retrieved via
     * [.getTurfPolygon].
     *
     * @param circleCenter the center coordinate to be used in the Turf calculation.
     */
    private fun drawPolygonCircle(circleCenter: Point, radius: Int, index: Int) {
        mMapboxMap?.getStyle { style ->
            // Use Turf to calculate the Polygon's coordinates
            val polygonArea: Polygon = getTurfPolygon(circleCenter, radius.toDouble())
            val pointList = TurfMeta.coordAll(polygonArea, false)

            // Update the source's GeoJSON to draw a new fill circle
            val polygonCircleSource =
                style.getSourceAs<GeoJsonSource>(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index")
            polygonCircleSource?.setGeoJson(
                Polygon.fromOuterInner(
                    LineString.fromLngLats(pointList)
                )
            )

            // Adjust camera bounds to include entire circle
            val latLngList: MutableList<LatLng> = ArrayList(pointList.size)
            for (singlePoint in pointList) {
                latLngList.add(LatLng(singlePoint.latitude(), singlePoint.longitude()))
            }

            mMapboxMap?.easeCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds.Builder()
                        .includes(latLngList)
                        .build(),
                    Durations.CAMERA_TOP_RIGHT_LEFT_PADDING,
                    Durations.CAMERA_TOP_RIGHT_LEFT_PADDING,
                    Durations.CAMERA_TOP_RIGHT_LEFT_PADDING,
                    Durations.CAMERA_BOTTOM_PADDING
                ),
                Durations.CAMERA_DURATION_1500
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
}
