package com.aws.amazonlocation.utils

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.amplifyframework.geo.location.models.AmazonLocationPlace
import com.amplifyframework.geo.maplibre.util.toJsonElement
import com.amplifyframework.geo.maplibre.view.MapLibreView
import com.amplifyframework.geo.models.MapStyle
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.MarkerEnum
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.domain.`interface`.MarkerClickInterface
import com.aws.amazonlocation.domain.`interface`.UpdateRouteInterface
import com.aws.amazonlocation.domain.`interface`.UpdateTrackingInterface
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.map_style.MapStyleChangeListener
import com.aws.amazonlocation.utils.Distance.DISTANCE_IN_METER_20
import com.aws.amazonlocation.utils.Distance.DISTANCE_IN_METER_30
import com.aws.amazonlocation.utils.Durations.CAMERA_DURATION_1000
import com.aws.amazonlocation.utils.Durations.CAMERA_DURATION_1500
import com.aws.amazonlocation.utils.Durations.DEFAULT_INTERVAL_IN_MILLISECONDS
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_DRAGGABLE_INVISIBLE_ICON_ID
import com.aws.amazonlocation.utils.MapCameraZoom.DEFAULT_CAMERA_ZOOM
import com.aws.amazonlocation.utils.MapCameraZoom.NAVIGATION_CAMERA_ZOOM
import com.aws.amazonlocation.utils.MapCameraZoom.TRACKING_CAMERA_ZOOM
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.constants.MapboxConstants
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.engine.*
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import org.json.JSONObject
import java.util.*

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class MapHelper(private val appContext: Context) {

    var isGrabSelectedAndOutsideBound: Boolean = false
    private val mSourceId: String = "line-source"
    private val mDotSourceId: String = "dot-source"
    private val mDotDestinationSourceId: String = "dot-destination-source"
    private var mLayerId: String = "linelayer"
    private var mDotLayerId: String = "dot-layer"
    private var mDotDestinationLayerId: String = "dot-destination-layer"
    private val mDefaultLatLng = LatLng(49.281174, -123.116823)
    val mDefaultLatLngGrab = LatLng(1.2840123, 103.8487542)
    var mSymbolManager: SymbolManager? = null
    private var mSymbolManagerWithClick: SymbolManager? = null
    private var mSymbolManagerTracker: SymbolManager? = null
    private var mMapboxMap: MapboxMap? = null
    private var mLocationEngine: LocationEngine? = null
    private var mLocationTrackingEngine: LocationEngine? = null
    private var mLastStoreLocation: Location? = null
    private var mLastStoreTrackingLocation: Location? = null
    private var mRouteInterface: UpdateRouteInterface? = null
    private var mTrackingInterface: UpdateTrackingInterface? = null
    private var mOriginSymbol: Symbol? = null
    private var mGeofenceSM: SymbolManager? = null
    private var mGeofenceDragSM: SymbolManager? = null
    var mSymbolOptionList = ArrayList<Symbol>()
    private var mMapLibreView: MapLibreView? = null
    private var mapStyleChangeListener: MapStyleChangeListener? = null
    private var mPreferenceManager: PreferenceManager? = null
    private val MAX_BUSES = notificationData.size
    private val geoJsonSources: Array<GeoJsonSource?> = Array(MAX_BUSES) { null }
    private val animators: Array<ValueAnimator?> = Array(MAX_BUSES) { null }
    private val currentPositions: Array<LatLng?> = Array(MAX_BUSES) { null }

    fun initSymbolManager(
        mapView: MapLibreView,
        mapboxMap: MapboxMap?,
        mapStyle: String,
        style: String,
        isMapLoadedInterface: IsMapLoadedInterface,
        mapStyleChangedListener: MapStyleChangeListener,
        activity: FragmentActivity?,
        mPreferenceManager: PreferenceManager
    ) {
        this.mPreferenceManager = mPreferenceManager
        mapboxMap?.let {
            mMapLibreView = mapView
            this.mMapboxMap = it
            if (!it.locationComponent.isLocationComponentActivated) {
                if (isGrabMapSelected(mPreferenceManager, appContext)) {
                    moveCameraToLocation(mDefaultLatLngGrab)
                } else {
                    moveCameraToLocation(mDefaultLatLng)
                }
            }
            mapView.setStyle(MapStyle(mapStyle, style)) { style ->
                updateZoomRange(style)
                enableLocationComponent()
                if (activity?.checkLocationPermission() == true) {
                    initLocationEngine(true)
                }
                mSymbolManager = SymbolManager(mapView, mapboxMap, style)
                mSymbolManagerWithClick = SymbolManager(mapView, mapboxMap, style)
                mSymbolManagerTracker = SymbolManager(mapView, mapboxMap, style)
                mGeofenceSM = SymbolManager(mapView, mapboxMap, style)
                mGeofenceDragSM = SymbolManager(mapView, mapboxMap, style)
                mapboxMap.uiSettings.isAttributionEnabled = false
                isMapLoadedInterface.mapLoadedSuccess()
                mapStyleChangedListener.onMapStyleChanged(mapStyle)
                mapStyleChangeListener = mapStyleChangedListener
                mPreferenceManager.setValue(MAP_STYLE_ATTRIBUTION, style.sources.first().attribution)
            }
        }
    }

    fun updateStyle(mapView: MapLibreView, mapStyle: String, style: String) {
        mapView.setStyle(MapStyle(mapStyle, style)) {
            mapStyleChangeListener?.onMapStyleChanged(mapStyle)
            mPreferenceManager?.setValue(MAP_STYLE_ATTRIBUTION, it.sources.first().attribution)
            updateZoomRange(it)
        }
    }

    fun updateMapStyle(mapStyle: String, style: String) {
        mMapLibreView?.setStyle(MapStyle(mapStyle, style)) {
            mapStyleChangeListener?.onMapStyleChanged(mapStyle)
            mPreferenceManager?.setValue(MAP_STYLE_ATTRIBUTION, it.sources.first().attribution)
            updateZoomRange(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine(setCurrentLocation: Boolean = false) {
        mLocationEngine = LocationEngineProvider.getBestLocationEngine(appContext)
        val request =
            LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS).setMaxWaitTime(1000)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).build()

        if (setCurrentLocation) {
            try {
                mLocationEngine?.getLastLocation(initialLocationListener)
            } catch (_: Exception) {}
        } else {
            mLocationEngine?.requestLocationUpdates(request, locationListener, Looper.getMainLooper())
            mLocationEngine?.getLastLocation(locationListener)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initTrackingLocationEngine() {
        mLocationTrackingEngine = LocationEngineProvider.getBestLocationEngine(appContext)
        val request =
            LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS).setMaxWaitTime(1000)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).build()
        mLocationTrackingEngine?.requestLocationUpdates(
            request,
            locationTrackingListener,
            Looper.getMainLooper()
        )
        mLocationTrackingEngine?.getLastLocation(locationTrackingListener)
    }

    fun setInitialLocation() {
        initLocationEngine(true)
    }

    fun setUpdateRoute(routeInterface: UpdateRouteInterface?) {
        initLocationEngine()
        this.mRouteInterface = routeInterface
    }

    fun setTrackingUpdateRoute(updateTrackingInterface: UpdateTrackingInterface?) {
        initTrackingLocationEngine()
        this.mTrackingInterface = updateTrackingInterface
    }

    fun removeLocationListener() {
        addLiveLocationMarker(false)
        mLocationEngine?.removeLocationUpdates(locationListener)
    }

    fun removeTrackingLocationListener() {
        // addLiveLocationMarker(false)
        mLocationTrackingEngine?.removeLocationUpdates(locationTrackingListener)
    }

    private val locationListener = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            if (mLastStoreLocation == null) {
                mLastStoreLocation = result?.lastLocation
            } else {
                mLastStoreLocation?.let {
                    val distance = result?.lastLocation?.let { it1 -> it.distanceTo(it1) }
                    if (distance != null) {
                        if (distance > DISTANCE_IN_METER_20) {
                            mLastStoreLocation = result.lastLocation
                            mRouteInterface?.updateRoute(it, result.lastLocation?.bearing)
                        }
                    }
                }
            }
        }

        override fun onFailure(exception: Exception) {
        }
    }

    private val locationTrackingListener = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            try {
                mMapboxMap?.locationComponent?.forceLocationUpdate(result?.lastLocation)
                if (mLastStoreTrackingLocation == null) {
                    mLastStoreTrackingLocation = result?.lastLocation
                } else {
                    mLastStoreTrackingLocation?.let {
                        val distance = result?.lastLocation?.let { it1 -> it.distanceTo(it1) }
                        if (distance != null) {
                            if (distance > DISTANCE_IN_METER_30) {
                                mLastStoreTrackingLocation = result.lastLocation
                                mTrackingInterface?.updateRoute(it, result.lastLocation?.bearing)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onFailure(exception: Exception) {
        }
    }

    private val initialLocationListener = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            mMapboxMap?.getStyle {
                mMapboxMap?.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().zoom(DEFAULT_CAMERA_ZOOM).padding(
                            appContext.resources.getDimension(R.dimen.dp_130).toDouble(),
                            appContext.resources.getDimension(R.dimen.dp_130).toDouble(),
                            appContext.resources.getDimension(R.dimen.dp_130).toDouble(),
                            appContext.resources.getDimension(R.dimen.dp_130).toDouble()
                        ).target(
                            result?.lastLocation?.let {
                                LatLng(
                                    it.latitude,
                                    it.longitude
                                )
                            } ?: mDefaultLatLng
                        ).build()
                    )
                )
                try {
                    mMapboxMap?.locationComponent?.forceLocationUpdate(result?.lastLocation)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onFailure(exception: Exception) {
        }
    }

    fun addMarker(
        activity: Activity,
        markerType: MarkerEnum,
        currentPlace: SearchSuggestionData,
        originPlace: SearchSuggestionData? = null
    ) {
        mMapboxMap?.getStyle { style ->
            val list = ArrayList<LatLng>()
            currentPlace.amazonLocationPlace?.let { amazonLocationPlace ->
                setMarkerData(
                    amazonLocationPlace,
                    style,
                    activity,
                    markerType,
                    currentPlace,
                    list,
                    false
                )

                if (markerType == MarkerEnum.DIRECTION_ICON) {
                    if (originPlace != null) {
                        originPlace.amazonLocationPlace?.let {
                            list.add(
                                LatLng(
                                    it.coordinates.latitude,
                                    it.coordinates.longitude
                                )
                            )
                        }
                    } else {
                        getLiveLocation()?.let { list.add(it) }
                    }
                    adjustMapBounds(list, appContext.resources.getDimension(R.dimen.dp_90).toInt())
                } else {
                    adjustMapBounds(list, appContext.resources.getDimension(R.dimen.dp_130).toInt())
                }
            }
        }
    }

    fun setDirectionMarker(
        originLatLng: LatLng,
        latitude: Double,
        longitude: Double,
        activity: Activity,
        markerType: MarkerEnum,
        name: String
    ) {
        val list = ArrayList<LatLng>()
        val latLng = LatLng(
            latitude,
            longitude
        )
        mMapboxMap?.getStyle { style ->
            ContextCompat.getDrawable(activity.baseContext, R.drawable.ic_direction_marker)?.let {
                style.addImage(
                    name,
                    it
                )
            }
            mSymbolManager?.textAllowOverlap = false
            mSymbolManager?.iconAllowOverlap = true
            mSymbolManager?.iconIgnorePlacement = false
            val symbolOptions = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(
                    name
                )
                .withIconAnchor(Property.ICON_ANCHOR_CENTER)

            if (markerType == MarkerEnum.ORIGIN_ICON) {
                mOriginSymbol = mSymbolManager?.create(symbolOptions)
            } else {
                mSymbolManager?.create(symbolOptions)
            }

            list.add(originLatLng)
            list.add(
                LatLng(
                    latitude,
                    longitude
                )
            )
            adjustMapBounds(list, appContext.resources.getDimension(R.dimen.dp_90).toInt())
        }
    }

    fun addDirectionMarker(
        activity: Activity,
        markerType: MarkerEnum,
        currentPlace: SearchSuggestionData?,
        isFromMapClick: Boolean
    ) {
        mMapboxMap?.getStyle { style ->
            val list = ArrayList<LatLng>()
            currentPlace?.amazonLocationPlace?.let { amazonLocationPlace ->
                setMarkerData(amazonLocationPlace, style, activity, markerType, currentPlace, list, isFromMapClick)
                adjustMapBounds(list, appContext.resources.getDimension(R.dimen.dp_90).toInt())
            }
        }
    }

    private fun setMarkerData(
        amazonLocationPlace: AmazonLocationPlace,
        style: Style,
        activity: Activity,
        markerType: MarkerEnum,
        currentPlace: SearchSuggestionData,
        list: ArrayList<LatLng>,
        isFromMapClick: Boolean
    ) {
        val latLng = LatLng(
            amazonLocationPlace.coordinates.latitude,
            amazonLocationPlace.coordinates.longitude
        )
        style.addImage(
            amazonLocationPlace.label.toString(),
            convertLayoutToBitmap(activity, markerType, currentPlace, isFromMapClick = isFromMapClick)
        )
        mSymbolManager?.textAllowOverlap = false
        mSymbolManager?.iconAllowOverlap = true
        mSymbolManager?.iconIgnorePlacement = false
        val symbolOptions = SymbolOptions()
            .withLatLng(latLng)
            .withData(amazonLocationPlace.toJsonElement())
            .withIconImage(
                amazonLocationPlace.label.toString()
            )
            .withIconAnchor(Property.ICON_ANCHOR_LEFT)

        if (markerType == MarkerEnum.ORIGIN_ICON) {
            mOriginSymbol = mSymbolManager?.create(symbolOptions)
        } else {
            mSymbolManager?.create(symbolOptions)
        }

        list.add(
            LatLng(
                amazonLocationPlace.coordinates.latitude,
                amazonLocationPlace.coordinates.longitude
            )
        )
    }
    fun setMarker(
        latitude: Double,
        longitude: Double,
        activity: Activity,
        markerType: MarkerEnum,
        name: String
    ) {
        val list = ArrayList<LatLng>()
        val latLng = LatLng(
            latitude,
            longitude
        )
        mMapboxMap?.getStyle { style ->
            style.addImage(
                name,
                convertLayoutToBitmap(activity, markerType, null, name)
            )
            mSymbolManager?.textAllowOverlap = false
            mSymbolManager?.iconAllowOverlap = true
            mSymbolManager?.iconIgnorePlacement = false
            val symbolOptions = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(
                    name
                )
                .withIconAnchor(Property.ICON_ANCHOR_LEFT)

            if (markerType == MarkerEnum.ORIGIN_ICON) {
                mOriginSymbol = mSymbolManager?.create(symbolOptions)
            } else {
                mSymbolManager?.create(symbolOptions)
            }

            list.add(
                LatLng(
                    latitude,
                    longitude
                )
            )
            adjustMapBounds(list, appContext.resources.getDimension(R.dimen.dp_90).toInt())
        }
    }

    fun addLiveLocationMarker(isLocationIconDefault: Boolean = false) {
        if (mMapboxMap?.locationComponent?.isLocationComponentActivated == true) {
            val locationComponentOptions = LocationComponentOptions.builder(appContext)
            if (PermissionsManager.areLocationPermissionsGranted(appContext)) {
                locationComponentOptions.apply {
                    mMapboxMap?.getStyle { style ->
                        if (style.getLayer(mLayerId) != null) {
                            layerAbove(mLayerId)
                        }
                    }
                    if (isLocationIconDefault) {
                        accuracyColor(
                            ContextCompat.getColor(
                                appContext,
                                android.R.color.transparent
                            )
                        )
                        foregroundTintColor(
                            ContextCompat.getColor(
                                appContext,
                                android.R.color.transparent
                            )
                        )

                        backgroundDrawable(R.drawable.ic_navigation_icon)
                            .bearingTintColor(
                                ContextCompat.getColor(
                                    appContext,
                                    android.R.color.transparent
                                )
                            )
                    } else {
                        accuracyColor(
                            ContextCompat.getColor(
                                appContext,
                                R.color.color_bn_selected
                            )
                        ).foregroundTintColor(
                            ContextCompat.getColor(
                                appContext,
                                R.color.color_bn_selected
                            )
                        ).bearingTintColor(
                            ContextCompat.getColor(
                                appContext,
                                R.color.color_bn_selected
                            )
                        ).bearingDrawable(R.drawable.ic_bearing)
                    }
                }

                mMapboxMap?.locationComponent?.apply {
                    applyStyle(locationComponentOptions.build())
                    renderMode = if (isLocationIconDefault) RenderMode.GPS else RenderMode.COMPASS
                }
            }
        }
    }

    fun addLine(
        coordinates: List<Point>,
        isWalk: Boolean
    ) {
        mMapboxMap?.getStyle { style ->
            style.removeLayer(mLayerId)
            style.removeSource(mSourceId)
            mLayerId = UUID.randomUUID().toString()
            val lineString: LineString = LineString.fromLngLats(coordinates)
            val feature: Feature = Feature.fromGeometry(lineString)

            val featureCollection = FeatureCollection.fromFeature(feature)
            val geoJsonSource = GeoJsonSource(mSourceId, featureCollection)
            style.addSource(geoJsonSource)

            mSymbolManager?.layerId?.let {
                style.addLayerBelow(
                    LineLayer(mLayerId, mSourceId).withProperties(
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        if (isWalk) {
                            PropertyFactory.lineDasharray(arrayOf(0f, 2f))
                        } else {
                            PropertyFactory.lineDasharray(arrayOf())
                        },
                        PropertyFactory.lineWidth(6f),
                        PropertyFactory.lineColor(
                            ContextCompat.getColor(
                                appContext,
                                R.color.color_primary_green
                            )
                        )
                    ),
                    it
                )
            }
        }
    }

    fun addStartDot(
        coordinates: List<Point>
    ) {
        mMapboxMap?.getStyle { style ->
            if (style.getLayer(mDotLayerId) != null) {
                style.removeLayer(mDotLayerId)
                style.removeSource(mDotSourceId)
            }
            mDotLayerId = UUID.randomUUID().toString()
            val lineString: LineString = LineString.fromLngLats(coordinates)
            val feature: Feature = Feature.fromGeometry(lineString)

            val featureCollection = FeatureCollection.fromFeature(feature)
            val geoJsonSource = GeoJsonSource(mDotSourceId, featureCollection)
            style.addSource(geoJsonSource)

            style.addLayerBelow(
                LineLayer(mDotLayerId, mDotSourceId).withProperties(
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineDasharray(arrayOf(0f, 2f)),
                    PropertyFactory.lineWidth(6f),
                    PropertyFactory.lineColor(
                        ContextCompat.getColor(
                            appContext,
                            R.color.color_hint_text
                        )
                    )
                ),
                mLayerId
            )
        }
    }

    fun removeStartDot() {
        mMapboxMap?.getStyle { style ->
            style.removeLayer(mDotLayerId)
            style.removeSource(mDotSourceId)
        }
    }

    fun addDotDestination(
        coordinates: List<Point>
    ) {
        mMapboxMap?.getStyle { style ->
            if (style.getLayer(mDotDestinationLayerId) != null) {
                style.removeLayer(mDotDestinationLayerId)
                style.removeSource(mDotDestinationSourceId)
            }
            mDotDestinationLayerId = UUID.randomUUID().toString()
            val lineString: LineString = LineString.fromLngLats(coordinates)
            val feature: Feature = Feature.fromGeometry(lineString)

            val featureCollection = FeatureCollection.fromFeature(feature)
            val geoJsonSource = GeoJsonSource(mDotDestinationSourceId, featureCollection)
            style.addSource(geoJsonSource)

            style.addLayerBelow(
                LineLayer(mDotDestinationLayerId, mDotDestinationSourceId).withProperties(
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineDasharray(arrayOf(0f, 2f)),
                    PropertyFactory.lineWidth(6f),
                    PropertyFactory.lineColor(
                        ContextCompat.getColor(
                            appContext,
                            R.color.color_hint_text
                        )
                    )
                ),
                mLayerId
            )
        }
    }

    private fun removeDestinationDot() {
        mMapboxMap?.getStyle { style ->
            style.removeLayer(mDotDestinationLayerId)
            style.removeSource(mDotDestinationSourceId)
        }
    }

    fun addTrackerLine(
        coordinates: List<Point>,
        mLayerId: String,
        mSourceId: String,
        color: Int
    ) {
        val rColor = ContextCompat.getColor(
            appContext,
            color
        )
        mMapboxMap?.getStyle { style ->
            style.removeLayer(mLayerId)
            style.removeSource(mSourceId)

            val features: MutableList<Feature> = mutableListOf()
            for (point in coordinates) {
                val feature: Feature = Feature.fromGeometry(point)
                features.add(feature)
            }

            val featureCollection = FeatureCollection.fromFeatures(features)
            val geoJsonSource = GeoJsonSource(mSourceId, featureCollection)
            style.addSource(geoJsonSource)

            // Add larger circles with green outline and white inner color
            mSymbolManagerTracker?.layerId?.let {
                style.addLayerBelow(
                    CircleLayer(mLayerId, mSourceId).withProperties(
                        PropertyFactory.circleRadius(6f),
                        PropertyFactory.circleColor(Color.WHITE),
                        PropertyFactory.circleStrokeColor(rColor),
                        PropertyFactory.circleStrokeWidth(3f)
                    ),
                    it
                )
            }

            // Add smaller green circles in between the larger circles
            val inBetweenPoints = getInBetweenPoints(coordinates)
            val inBetweenFeatures: MutableList<Feature> = mutableListOf()
            for (point in inBetweenPoints) {
                val feature: Feature = Feature.fromGeometry(point)
                inBetweenFeatures.add(feature)
            }
            val sourceIDInBetween = mSourceId + LABEL_IN_BETWEEN
            val layerIDInBetween = mLayerId + LABEL_IN_BETWEEN

            style.removeLayer(layerIDInBetween)
            style.removeSource(sourceIDInBetween)

            val inBetweenFeatureCollection = FeatureCollection.fromFeatures(inBetweenFeatures)
            val inBetweenGeoJsonSource = GeoJsonSource(sourceIDInBetween, inBetweenFeatureCollection)
            style.addSource(inBetweenGeoJsonSource)

            mSymbolManagerTracker?.layerId?.let {
                style.addLayerBelow(
                    CircleLayer(mLayerId + LABEL_IN_BETWEEN, mSourceId + LABEL_IN_BETWEEN).withProperties(
                        PropertyFactory.circleRadius(3f),
                        PropertyFactory.circleColor(rColor)
                    ),
                    it
                )
            }
        }
    }
    private fun getInBetweenPoints(coordinates: List<Point>): List<Point> {
        val inBetweenPoints: MutableList<Point> = mutableListOf()
        for (i in 0 until coordinates.size - 1) {
            val start = coordinates[i]
            val end = coordinates[i + 1]
            val distance = calculateDistance(start, end) // in meters
            val numPoints = (distance / 80).toInt() // add a point every 10 meters
            for (j in 0 until numPoints) {
                val fraction = (j + 1).toDouble() / (numPoints + 1)
                val lng = start.longitude() + fraction * (end.longitude() - start.longitude())
                val lat = start.latitude() + fraction * (end.latitude() - start.latitude())
                val midPoint = Point.fromLngLat(lng, lat)
                inBetweenPoints.add(midPoint)
            }
        }
        return inBetweenPoints
    }

    private fun calculateDistance(start: Point, end: Point): Double {
        val R = 6371e3 // radius of the Earth in meters
        val lat1 = Math.toRadians(start.latitude())
        val lat2 = Math.toRadians(end.latitude())
        val dLat = Math.toRadians(end.latitude() - start.latitude())
        val dLng = Math.toRadians(end.longitude() - start.longitude())
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    fun updateLine(coordinates: List<Point>) {
        mMapboxMap?.getStyle { style ->
            val lineString: LineString = LineString.fromLngLats(coordinates)
            val feature: Feature = Feature.fromGeometry(lineString)
            val featureCollection = FeatureCollection.fromFeature(feature)
            style.getSourceAs<GeoJsonSource>(mSourceId)?.setGeoJson(featureCollection)
        }
    }

    fun removeLine() {
        mMapboxMap?.getStyle { style ->
            style.removeLayer(mLayerId)
            style.removeSource(mSourceId)
        }
        removeStartDot()
        removeDestinationDot()
    }

    fun removeLayer(lineId: String) {
        mMapboxMap?.getStyle { style ->
            style.removeLayer(lineId)
        }
    }

    fun removeSource(sourceId: String) {
        mMapboxMap?.getStyle { style ->
            style.removeSource(sourceId)
        }
    }

    fun removeMarkerAndLine() {
        removeLine()
        clearMarker()
    }

    fun removeGeoJsonSourceData(index: Int) {
        mMapboxMap?.getStyle { style ->
            if (index in 0 until MAX_BUSES) {
                animators[index]?.cancel()
                animators[index] = null
                currentPositions[index] = null
                geoJsonSources[index]?.let { source ->
                    if (style.getSource(source.id) != null) {
                        style.removeSource(source)
                    }
                }
                geoJsonSources[index] = null
            }
        }
    }

    fun removeSimulationData() {
        mMapboxMap?.getStyle { style ->
            for (index in 0 until MAX_BUSES) {
                geoJsonSources[index]?.let { style.removeSource(it) }
            }
        }
        geoJsonSources.fill(null)
        animators.fill(null)
        currentPositions.fill(null)
    }

    fun setGeoJsonSourceEmpty() {
        geoJsonSources.fill(null)
    }

    fun addMultipleMarker(
        activity: Activity,
        markerType: MarkerEnum,
        placeList: ArrayList<SearchSuggestionData>,
        mMarkerClickInterface: MarkerClickInterface
    ) {
        clearMarker()
        val list = ArrayList<LatLng>()
        placeList.forEach { searchPlace ->
            if (!searchPlace.isPlaceIndexForPosition) {
                searchPlace.amazonLocationPlace?.let { amazonPlace ->
                    addMarkerWithClick(
                        activity,
                        markerType,
                        searchPlace,
                        mMarkerClickInterface
                    )
                    list.add(
                        LatLng(
                            amazonPlace.coordinates.latitude,
                            amazonPlace.coordinates.longitude
                        )
                    )
                }
            }
        }
        adjustMapBounds(list, appContext.resources.getDimension(R.dimen.dp_130).toInt())
    }

    private fun addMarkerWithClick(
        activity: Activity,
        markerType: MarkerEnum,
        currentPlace: SearchSuggestionData,
        mMarkerClickInterface: MarkerClickInterface
    ) {
        mMapboxMap?.getStyle { style ->
            currentPlace.amazonLocationPlace?.let { amazonLocationPlace ->
                val latLng =
                    LatLng(
                        amazonLocationPlace.coordinates.latitude,
                        amazonLocationPlace.coordinates.longitude
                    )
                style.addImage(
                    amazonLocationPlace.label.toString(),
                    convertLayoutToBitmap(activity, markerType, currentPlace)
                )
                mSymbolManagerWithClick?.textAllowOverlap = true
                mSymbolManagerWithClick?.iconAllowOverlap = true
                mSymbolManagerWithClick?.iconIgnorePlacement = false
                val symbolOptions = SymbolOptions()
                    .withLatLng(latLng)
                    .withData(amazonLocationPlace.toJsonElement())
                    .withIconImage(
                        amazonLocationPlace.label.toString()
                    )
                    .withIconAnchor(Property.ICON_ANCHOR_LEFT)
                mSymbolManagerWithClick?.create(symbolOptions)
                mSymbolManagerWithClick?.addClickListener {
                    mMarkerClickInterface.markerClick(it.iconImage)
                    true
                }
            }
        }
    }

    fun addMarkerTracker(
        trackerImageName: String,
        activity: Activity,
        markerType: MarkerEnum,
        currentPlace: LatLng
    ) {
        mMapboxMap?.getStyle { style ->
            BitmapUtils.getBitmapFromDrawable(
                ContextCompat.getDrawable(
                    activity,
                    if (markerType.name == MarkerEnum.ORIGIN_ICON.name) R.drawable.ic_geofence_marker_1 else R.drawable.ic_tracker
                )
            )?.let {
                style.addImage(trackerImageName, it)
            }
            mSymbolManagerTracker?.textAllowOverlap = true
            mSymbolManagerTracker?.iconAllowOverlap = true
            mSymbolManagerTracker?.iconIgnorePlacement = false
            val symbolOptions = SymbolOptions()
                .withLatLng(currentPlace)
                .withIconImage(
                    trackerImageName
                )
                .withIconAnchor(if (markerType.name == MarkerEnum.GEOFENCE_ICON.name) Property.ICON_ANCHOR_CENTER else Property.ICON_ANCHOR_CENTER)
            mSymbolManagerTracker?.create(symbolOptions)
        }
    }

    fun addMarkerSimulation(
        trackerImageName: String,
        activity: Activity,
        currentPlace: LatLng,
        index: Int
    ) {
        mMapboxMap?.getStyle { style ->
            if (style.getLayer("$LAYER_SIMULATION_ICON$trackerImageName") != null) {
                style.removeLayer("$LAYER_SIMULATION_ICON$trackerImageName")
                style.removeSource("$SOURCE_SIMULATION_ICON$trackerImageName")
            }
            BitmapUtils.getBitmapFromDrawable(
                ContextCompat.getDrawable(
                    activity,
                    R.drawable.ic_simulation_my_location
                )
            )?.let {
                style.addImage("$SOURCE_SIMULATION_ICON$trackerImageName", it)
            }

            if (index in 0 until MAX_BUSES) {
                currentPositions[index] = currentPlace
                if (geoJsonSources[index] == null) {
                    val source = GeoJsonSource(
                        "$SOURCE_SIMULATION_ICON$trackerImageName",
                        Feature.fromGeometry(Point.fromLngLat(currentPlace.longitude, currentPlace.latitude))
                    )
                    geoJsonSources[index] = source
                    style.addSource(source)
                }
            }

            style.addLayer(
                SymbolLayer("$LAYER_SIMULATION_ICON$trackerImageName", "$SOURCE_SIMULATION_ICON$trackerImageName")
                    .withProperties(
                        PropertyFactory.iconImage("$SOURCE_SIMULATION_ICON$trackerImageName"),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                    )
            )
        }
    }

    fun startAnimation(point: LatLng, index: Int) {
        if (index in 0 until MAX_BUSES) {
            animators[index]?.let { animator ->
                if (animator.isStarted) {
                    currentPositions[index] = animator.animatedValue as LatLng
                    animator.cancel()
                }
            }

            animators[index] = ObjectAnimator
                .ofObject(latLngEvaluators[index], currentPositions[index], point)
                .setDuration(DELAY_SIMULATION_2000)
            animators[index]?.addUpdateListener { valueAnimator ->
                val animatedPosition = valueAnimator.animatedValue as LatLng
                geoJsonSources[index]?.setGeoJson(Point.fromLngLat(animatedPosition.longitude, animatedPosition.latitude))
            }
            animators[index]?.start()
            currentPositions[index] = point
        }
    }
    fun getLiveLocation(): LatLng? {
        isGrabSelectedAndOutsideBound = false
        var mLatLng: LatLng? = null
        if (mMapboxMap?.locationComponent?.isLocationComponentActivated == true) {
            mMapboxMap?.locationComponent?.lastKnownLocation?.apply {
                mLatLng = LatLng(
                    latitude,
                    longitude
                )
            }
        }
        mPreferenceManager?.let { preferenceManager ->
            if (isGrabMapSelected(preferenceManager, appContext)) {
                if (mLatLng != null) {
                    mLatLng?.let {
                        if (!(it.latitude in latSouth..latNorth && it.longitude in lonWest..lonEast)) {
                            isGrabSelectedAndOutsideBound = true
                            return mDefaultLatLngGrab
                        }
                    }
                } else {
                    isGrabSelectedAndOutsideBound = true
                    return mDefaultLatLngGrab
                }
            }
        }
        return if (mLatLng == null) {
            mPreferenceManager?.let {
                if (isGrabMapSelected(it, appContext)) {
                    mDefaultLatLngGrab
                } else {
                    mDefaultLatLng
                }
            }
        } else {
            mLatLng
        }
    }

    fun clearMarker() {
        mGeofenceSM?.deleteAll()
        mSymbolManager?.deleteAll()
        mSymbolManagerWithClick?.deleteAll()
        mSymbolManagerTracker?.deleteAll()
    }

    fun clearOriginMarker() {
        mOriginSymbol?.let {
            mSymbolManager?.delete(it)
        }
    }

    fun adjustMapBounds(latLngList: ArrayList<LatLng>, padding: Int) {
        adjustMapBounds(latLngList, padding, null)
    }
    fun adjustMapBounds(latLngList: ArrayList<LatLng>, padding: Int, topPadding: Int? = null) {
        if (latLngList.size > 1) {
            val latLngBounds = LatLngBounds.Builder().includes(latLngList).build()
            mMapboxMap?.easeCamera(
                CameraUpdateFactory.newLatLngBounds(
                    latLngBounds,
                    padding,
                    topPadding ?: appContext.resources.getDimension(R.dimen.dp_80).toInt(),
                    padding,
                    appContext.resources.getDimension(R.dimen.dp_350).toInt()
                ),
                CAMERA_DURATION_1500
            )
        } else {
            if (latLngList.isNotEmpty()) {
                moveCameraToLocation(latLngList[0])
            }
        }
    }

    // move camera to  location
    fun moveCameraToLocationTracker(latLng: LatLng) {
        mMapboxMap?.getStyle { style ->
            if (style.isFullyLoaded) {
                mMapboxMap?.easeCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().zoom(DEFAULT_CAMERA_ZOOM).padding(
                            appContext.resources.getDimension(R.dimen.dp_80).toDouble(),
                            appContext.resources.getDimension(R.dimen.dp_80).toDouble(),
                            appContext.resources.getDimension(R.dimen.dp_80).toDouble(),
                            appContext.resources.getDimension(R.dimen.dp_80).toDouble()
                        ).target(
                            latLng
                        ).build()
                    ),
                    CAMERA_DURATION_1500
                )
            }
        }
    }

    // move camera to  location
    fun moveCameraToLocation(latLng: LatLng) {
        mMapboxMap?.getStyle { style ->
            if (style.isFullyLoaded) {
                mMapboxMap?.easeCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().zoom(DEFAULT_CAMERA_ZOOM).padding(
                            appContext.resources.getDimension(R.dimen.dp_130).toDouble(),
                            appContext.resources.getDimension(R.dimen.dp_130).toDouble(),
                            appContext.resources.getDimension(R.dimen.dp_130).toDouble(),
                            appContext.resources.getDimension(R.dimen.dp_130).toDouble()
                        ).target(
                            latLng
                        ).build()
                    ),
                    CAMERA_DURATION_1500
                )
            }
        }
    }

    // move camera to  location
    fun moveCameraToCurrentLocation(latLng: LatLng) {
        mMapboxMap?.easeCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().zoom(TRACKING_CAMERA_ZOOM).padding(
                    appContext.resources.getDimension(R.dimen.dp_210).toDouble(),
                    appContext.resources.getDimension(R.dimen.dp_80).toDouble(),
                    appContext.resources.getDimension(R.dimen.dp_210).toDouble(),
                    appContext.resources.getDimension(R.dimen.dp_350).toDouble()
                ).target(
                    latLng
                ).build()
            ),
            CAMERA_DURATION_1500
        )
    }

    fun bearingCamera(bearing: Float, latLng: Location) {
        mMapboxMap?.easeCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().zoom(NAVIGATION_CAMERA_ZOOM).target(LatLng(latLng))
                    .bearing(bearing.toDouble()).build()
            ),
            CAMERA_DURATION_1000
        )
    }

    fun navigationZoomCamera(latLng: LatLng, isZooming: Boolean) {
        if (!isZooming) {
            mMapboxMap?.easeCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().zoom(NAVIGATION_CAMERA_ZOOM).target(latLng)
                        .build()
                ),
                CAMERA_DURATION_1000
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun enableLocationComponent() {
        mMapboxMap?.style?.let { loadedMapStyle ->
            if (PermissionsManager.areLocationPermissionsGranted(appContext)) {
                val locationComponentOptions: LocationComponentOptions =
                    LocationComponentOptions.builder(appContext).accuracyColor(
                        ContextCompat.getColor(
                            appContext,
                            R.color.color_bn_selected
                        )
                    ).foregroundTintColor(
                        ContextCompat.getColor(
                            appContext,
                            R.color.color_bn_selected
                        )
                    ).bearingTintColor(
                        ContextCompat.getColor(
                            appContext,
                            R.color.color_bn_selected
                        )
                    ).bearingDrawable(R.drawable.ic_bearing)
                        .build()

                val locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(appContext, loadedMapStyle)
                        .locationComponentOptions(locationComponentOptions)
                        .build()

                mMapboxMap?.locationComponent?.apply {
                    activateLocationComponent(locationComponentActivationOptions)
                    isLocationComponentEnabled = true
                    cameraMode = CameraMode.TRACKING
                    zoomWhileTracking(DEFAULT_CAMERA_ZOOM)
                    renderMode = RenderMode.COMPASS
                }
            }
        }
    }

    // check map box location component enable or not for get live location
    fun checkLocationComponentEnable(mBaseActivity: BaseActivity?, isLiveLocationClicked: Boolean) {
        mMapboxMap?.let {
            if (it.locationComponent.isLocationComponentActivated) {
                getLiveLocation()
                    ?.let { LatLng ->
                        if (isLiveLocationClicked) {
                            if (LatLng.longitude == mDefaultLatLngGrab.longitude && LatLng.latitude == mDefaultLatLngGrab.latitude) {
                                mBaseActivity?.showError(mBaseActivity.getString(R.string.your_location_not_supported_by_grab))
                            }
                        }
                        moveCameraToLocation(LatLng)
                    }
            } else {
                enableLocationComponent()
            }
        }
    }

    fun addGeofenceMarker(
        activity: Activity,
        data: ListGeofenceResponseEntry,
        markerClick: MarkerClickInterface
    ) {
        mMapboxMap?.getStyle { style ->
            val location = data.geometry.circle.center
            convertGeofenceLayoutToBitmap(activity, data).let { bitmap ->
                style.addImage(
                    data.geofenceId,
                    bitmap
                )
            }
            mGeofenceSM?.textAllowOverlap = true
            mGeofenceSM?.iconAllowOverlap = true
            mGeofenceSM?.iconIgnorePlacement = false
            val symbolOptions = SymbolOptions()
                .withLatLng(LatLng(location[1], location[0]))
                .withIconImage(data.geofenceId)
                .withIconAnchor(Property.ICON_ANCHOR_LEFT)
            mGeofenceSM?.create(symbolOptions)?.let { symbol ->
                mSymbolOptionList.add(symbol)
            }
            mGeofenceSM?.addClickListener {
                markerClick.markerClick(it.iconImage)
                true
            }
        }
    }

    fun addGeofenceInvisibleDraggableMarker(activity: Activity?, latLng: LatLng, listener: OnSymbolDragListener) {
        mGeofenceDragSM?.iconAllowOverlap = true
        mGeofenceDragSM?.iconIgnorePlacement = true
        mMapboxMap?.getStyle { style ->
            activity?.let {
                convertLayoutToGeofenceInvisibleDragBitmap(activity).let { bitmap ->
                    style.addImage(
                        CIRCLE_DRAGGABLE_INVISIBLE_ICON_ID,
                        bitmap
                    )
                }
            }

            val symbolOptions = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(CIRCLE_DRAGGABLE_INVISIBLE_ICON_ID)
                .withIconAnchor(Property.ICON_ANCHOR_CENTER)
                .withDraggable(true)

            mGeofenceDragSM?.addDragListener(listener)

            mGeofenceDragSM?.let {
                it.create(symbolOptions)?.let { symbol ->
                    mSymbolOptionList.add(symbol)
                }
            }
        }
    }

    fun updateGeofenceInvisibleDraggableMarker(latLng: LatLng) {
        mSymbolOptionList.firstOrNull { it.iconImage == CIRCLE_DRAGGABLE_INVISIBLE_ICON_ID }?.let { symbol ->
            symbol.latLng = latLng
            mGeofenceDragSM?.update(symbol)
        }
    }

    fun deleteGeofenceInvisibleDraggableMarker(listener: OnSymbolDragListener) {
        mGeofenceDragSM?.removeDragListener(listener)
        val temp = mSymbolOptionList.filter { it.isDraggable && it.iconImage == CIRCLE_DRAGGABLE_INVISIBLE_ICON_ID }
        temp.forEach {
            mGeofenceDragSM?.delete(it)
            mSymbolOptionList.remove(it)
        }
    }

    fun deleteGeofenceMarker(position: Int) {
        mGeofenceSM?.delete(mSymbolOptionList[position])
        mSymbolOptionList.removeAt(position)
    }

    fun deleteAllGeofenceMarker() {
        mGeofenceSM?.deleteAll()
    }

    // convert layout to marker
    private fun convertLayoutToBitmap(
        context: Activity,
        markerEnum: MarkerEnum = MarkerEnum.NONE,
        data: SearchSuggestionData? = null,
        markerName: String? = null,
        isFromMapClick: Boolean = false
    ): Bitmap {
        val viewGroup: ViewGroup? = null
        val view = context.layoutInflater.inflate(R.layout.layout_map_custom_marker, viewGroup)
        val llMain: ConstraintLayout = view.findViewById(R.id.ll_custom_marker)
        val tvAddress: OutLineTextView = view.findViewById(R.id.tv_address)
        val ivSearch: AppCompatImageView = view.findViewById(R.id.iv_search)
        val ivDirection: AppCompatImageView = view.findViewById(R.id.iv_direction)
        val ivOrigin: AppCompatImageView = view.findViewById(R.id.iv_origin)
        val ivTracker: AppCompatImageView = view.findViewById(R.id.iv_tracker)
        when (markerEnum) {
            MarkerEnum.NONE -> {
                ivSearch.show()
                ivDirection.hide()
                ivOrigin.hide()
                ivTracker.hide()
            }
            MarkerEnum.DIRECTION_ICON -> {
                ivSearch.hide()
                ivOrigin.hide()
                ivTracker.hide()
                ivDirection.show()
            }
            MarkerEnum.ORIGIN_ICON -> {
                ivSearch.hide()
                ivDirection.hide()
                ivTracker.hide()
                ivOrigin.show()
            }
            MarkerEnum.TRACKER_ICON -> {
                ivSearch.hide()
                ivDirection.hide()
                ivTracker.show()
                ivOrigin.hide()
            }
            else -> {}
        }
        if (!isFromMapClick) {
            data?.let { searchData ->
                if (searchData.isPlaceIndexForPosition) {
                    searchData.text?.let { tvAddress.setText(it) }
                } else {
                    (searchData.text?.split(",")?.toTypedArray()?.get(0) ?: data.text)?.let {
                        tvAddress.setText(
                            it
                        )
                    }
                }
            }
        } else {
            data?.let { searchData -> searchData.text?.let { tvAddress.setText(it) } }
        }
        markerName?.let {
            tvAddress.setText(it)
        }
        llMain.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        llMain.layout(0, 0, llMain.measuredWidth, llMain.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            llMain.measuredWidth,
            llMain.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        llMain.draw(canvas)
        return bitmap
    }

    private fun convertLayoutToGeofenceInvisibleDragBitmap(
        context: Activity
    ): Bitmap {
        val viewGroup: ViewGroup? = null
        val view = context.layoutInflater.inflate(R.layout.layout_geofence_draggable_marker, viewGroup)
        val llMain: ConstraintLayout = view.findViewById(R.id.ll_geofence_draggable_marker)
        val ivGeofenceDragMarker: AppCompatImageView = view.findViewById(R.id.iv_geofence_draggable_marker)

        ivGeofenceDragMarker.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN)

        llMain.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        llMain.layout(0, 0, llMain.measuredWidth, llMain.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            llMain.measuredWidth,
            llMain.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        llMain.draw(canvas)
        return bitmap
    }

    // convert layout to marker
    private fun convertGeofenceLayoutToBitmap(
        context: Activity,
        data: ListGeofenceResponseEntry?
    ): Bitmap {
        val viewGroup: ViewGroup? = null
        val view = context.layoutInflater.inflate(R.layout.layout_geofence_marker, viewGroup)
        val clMain: ConstraintLayout = view.findViewById(R.id.cl_geofence_marker)
        val tvGeofenceName: OutLineTextView = view.findViewById(R.id.tv_geofence_name)
        data?.geofenceId?.let { tvGeofenceName.setText(it) }
        clMain.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        clMain.layout(0, 0, clMain.measuredWidth, clMain.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            clMain.measuredWidth,
            clMain.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        clMain.draw(canvas)
        return bitmap
    }
    interface IsMapLoadedInterface {
        fun mapLoadedSuccess()
    }

    private fun setStyleLanguage(style: Style) {
        val r = this.appContext.resources
        val mapName = mPreferenceManager?.getValue(KEY_MAP_NAME, r.getString(R.string.esri))
        var expression: Expression? = null
        val languageCode = getLanguageCode()

        if (mapName == r.getString(R.string.here) || mapName == r.getString(R.string.grab)) {
            expression = Expression.coalesce(
                Expression.get("name:$languageCode"),
                Expression.get("name")
            )
        } else {
            if (mapName == r.getString(R.string.esri)) {
                expression = Expression.coalesce(
                    Expression.coalesce(
                        Expression.get("_name_$languageCode"),
                        Expression.get("_name_global")
                    ),
                    Expression.get("_name")
                )
            }
        }
        for (layer in style.layers) {
            if (layer is SymbolLayer) {
                val textField = textField(expression)
                layer.setProperties(textField)
            }
        }
    }

    fun updateZoomRange(style: Style) {
        mMapboxMap?.getStyle {
            val r = this.appContext.resources
            val mapName = mPreferenceManager?.getValue(KEY_MAP_NAME, r.getString(R.string.esri))
            if (mapName != r.getString(R.string.open_data)) {
                setStyleLanguage(style)
            }
            val cameraPosition = mMapboxMap?.cameraPosition
            val zoom = cameraPosition?.zoom
            val minZoom = minZoomLevel(style)
            val maxZoom = MapCameraZoom.MAX_ZOOM
            if (zoom != null) {
                if (zoom < minZoom) {
                    mMapboxMap?.cameraPosition = CameraPosition.Builder()
                        .zoom(minZoom)
                        .build()
                } else if (zoom > maxZoom) {
                    mMapboxMap?.cameraPosition = CameraPosition.Builder()
                        .zoom(maxZoom)
                        .build()
                }
            }
            mMapboxMap?.setMinZoomPreference(minZoom)
            mMapboxMap?.setMaxZoomPreference(maxZoom)
        }
    }

    private fun minZoomLevel(style: Style): Double {
        return try {
            val sources = JSONObject(style.json).getJSONObject(JSON_KEY_STYLE_SOURCES)
            when {
                sources.has(JSON_KEY_STYLE_ESRI) -> {
                    sources.getJSONObject(JSON_KEY_STYLE_ESRI).optDouble(JSON_KEY_STYLE_MINZOOM, MapboxConstants.MINIMUM_ZOOM.toDouble())
                }
                sources.has(JSON_KEY_STYLE_HERE) -> {
                    sources.getJSONObject(JSON_KEY_STYLE_HERE).optDouble(JSON_KEY_STYLE_MINZOOM, MapboxConstants.MINIMUM_ZOOM.toDouble())
                }
                sources.has(JSON_KEY_STYLE_RASTER) -> {
                    sources.getJSONObject(JSON_KEY_STYLE_RASTER).optDouble(JSON_KEY_STYLE_MINZOOM, MapboxConstants.MINIMUM_ZOOM.toDouble())
                }
                else -> {
                    MapboxConstants.MINIMUM_ZOOM.toDouble()
                }
            }
        } catch (e: Exception) {
            MapboxConstants.MINIMUM_ZOOM.toDouble()
        }
    }

    // Class is used to interpolate the marker animation.
    private val latLngEvaluators: Array<TypeEvaluator<LatLng>> = Array(MAX_BUSES) {
        object : TypeEvaluator<LatLng> {
            private val latLng = LatLng()
            override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
                latLng.latitude = startValue.latitude + (endValue.latitude - startValue.latitude) * fraction
                latLng.longitude = startValue.longitude + (endValue.longitude - startValue.longitude) * fraction
                return latLng
            }
        }
    }
}
