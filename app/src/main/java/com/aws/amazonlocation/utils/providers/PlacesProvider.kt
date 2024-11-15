package com.aws.amazonlocation.utils.providers

import android.location.Location
import aws.sdk.kotlin.services.geoplaces.GeoPlacesClient
import aws.sdk.kotlin.services.geoplaces.model.Address
import aws.sdk.kotlin.services.geoplaces.model.GetPlaceAdditionalFeature
import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeRequest
import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeResponse
import aws.sdk.kotlin.services.geoplaces.model.SearchTextRequest
import aws.sdk.kotlin.services.geoplaces.model.SuggestAdditionalFeature
import aws.sdk.kotlin.services.geoplaces.model.SuggestRequest
import aws.sdk.kotlin.services.geoplaces.model.SuggestResponse
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.SEARCH_MAX_RESULT
import com.aws.amazonlocation.utils.SEARCH_MAX_SUGGESTION_RESULT
import com.aws.amazonlocation.utils.Units.getApiKey
import com.aws.amazonlocation.utils.Units.isMetric
import com.aws.amazonlocation.utils.Units.meterToFeet
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.validateLatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng

class PlacesProvider(
    private var mMapHelper: MapHelper,
    private var mPreferenceManager: PreferenceManager,
) {
    private var apiError = "Please try again later"

    private suspend fun searchPlaceIndexForSuggestions(
        lat: Double?,
        lng: Double?,
        text: String,
        mBaseActivity: BaseActivity?,
        getPlaceClient: GeoPlacesClient?,
    ): SuggestResponse? =
        try {
            val request =
                SuggestRequest {
                    this.queryText = text
                    this.language = getLanguageCode()
                    this.maxResults = SEARCH_MAX_SUGGESTION_RESULT
                    this.biasPosition = listOf(lng ?: 0.0, lat ?: 0.0)
                    this.additionalFeatures = listOf(SuggestAdditionalFeature.fromValue("Core"))
                }

            withContext(Dispatchers.IO) {
                getPlaceClient?.suggest(request)
            }
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            null
        }

    suspend fun searchPlaceSuggestion(
        lat: Double?,
        lng: Double?,
        searchText: String,
        mBaseActivity: BaseActivity?,
        getPlaceClient: GeoPlacesClient?,
    ): SearchSuggestionResponse {
        try {
            val liveLocation = mMapHelper.getLiveLocation()
            var isLatLng = false
            var suggestResponse: SuggestResponse? = null
            var reverseGeocodeResponse: ReverseGeocodeResponse? = null
            val response: SearchSuggestionResponse?
            if (validateLatLng(searchText.trim()) != null) {
                isLatLng = true
                val mLatLng = validateLatLng(searchText.trim())
                reverseGeocodeResponse =
                    searchPlaceIndexForPosition(
                        lng = mLatLng?.longitude,
                        lat = mLatLng?.latitude,
                        mBaseActivity,
                        getPlaceClient,
                    )
                response =
                    SearchSuggestionResponse(
                        text = mLatLng?.latitude.toString() + "," + mLatLng?.longitude.toString(),
                        maxResults = reverseGeocodeResponse?.resultItems?.size
                    )
            } else {
                suggestResponse =
                    searchPlaceIndexForSuggestions(
                        lat = lat,
                        lng = lng,
                        text = searchText,
                        mBaseActivity,
                        getPlaceClient,
                    )
                response =
                    SearchSuggestionResponse(
                        text = searchText,
                        maxResults = suggestResponse?.resultItems?.size,
                    )
            }

            val mList = ArrayList<SearchSuggestionData>()
            if (isLatLng) {
                addMarkerBasedOnLatLng(response, searchText, mList)
            }

            reverseGeocodeResponse?.resultItems?.forEach {
                val mSearchSuggestionData: SearchSuggestionData =
                    if (it.placeId.isNotEmpty()) {
                        SearchSuggestionData(
                            placeId = it.placeId,
                            text = it.address?.label,
                            amazonLocationAddress = it.address,
                            distance = it.distance.toDouble(),
                            position =
                            it.position?.let { doubles ->
                                listOf(
                                    doubles[0],
                                    doubles[1],
                                )
                            },
                        )
                    } else {
                        SearchSuggestionData(text = it.address?.label)
                    }
                mList.add(mSearchSuggestionData)
            }
            suggestResponse?.resultItems?.forEach {
                val mSearchSuggestionData: SearchSuggestionData =
                    if (!it.place?.placeId.isNullOrEmpty()) {
                        SearchSuggestionData(
                            placeId = it.place!!.placeId,
                            searchText = searchText,
                            text = it.place?.address?.label,
                            amazonLocationAddress = it.place?.address,
                            distance = it.place?.distance?.toDouble(),
                            position =
                            it.place?.position?.let { doubles ->
                                listOf(
                                    doubles[0],
                                    doubles[1],
                                )
                            },
                        )
                    } else {
                        SearchSuggestionData(text = it.title, queryId = it.query?.queryId)
                    }
                mList.add(mSearchSuggestionData)
            }
            response.data = mList
            return response
        } catch (e: Exception) {
            mBaseActivity?.handleException(e, apiError)
            return SearchSuggestionResponse(
                error = apiError,
            )
        }
    }

    private fun addMarkerBasedOnLatLng(
        mResponse: SearchSuggestionResponse,
        searchText: String,
        mList: ArrayList<SearchSuggestionData>,
    ) {
        val mLatLng = validateLatLng(searchText.trim())
        if (mLatLng != null) {
            val place =
                Address {
                    this.label = mResponse.text
                }
            val response =
                SearchSuggestionData(
                    searchText = mResponse.text,
                    placeId = mResponse.text,
                    text = mResponse.text,
                    isPlaceIndexForPosition = true,
                    amazonLocationAddress = place,
                    position = listOf(mLatLng.longitude, mLatLng.latitude),
                )
            mList.add(response)
        }
    }

    suspend fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        mText: String?,
        queryId: String?,
        mBaseActivity: BaseActivity?,
        getPlaceClient: GeoPlacesClient?,
    ): SearchSuggestionResponse? =
        try {
            val liveLocation = mMapHelper.getLiveLocation()
            val request =
                SearchTextRequest {
                    if (mText != null) this.queryText = mText
                    if (queryId != null) this.queryId = queryId
                    if (mText != null) this.language = getLanguageCode()
                    if (mText != null) this.maxResults = SEARCH_MAX_RESULT
                    if (queryId.isNullOrEmpty()) this.biasPosition = listOfNotNull(lng, lat)
                }

            val response =
                withContext(Dispatchers.IO) {
                    getPlaceClient?.searchText(request)
                }
            val text =
                mText ?: response
                    ?.resultItems
                    ?.get(0)
                    ?.categories
                    ?.get(0)
                    ?.name
            val searchSuggestionResponse =
                SearchSuggestionResponse(
                    text = text,
                    maxResults = response?.resultItems?.size,
                    error = null,
                )

            val mList = ArrayList<SearchSuggestionData>()
            if (validateLatLng(text!!.trim()) != null && response?.resultItems?.isEmpty()!!) {
                addMarkerBasedOnLatLng(searchSuggestionResponse, text, mList)
            } else {
                response?.resultItems?.forEach { result ->
                    val placeData =
                        SearchSuggestionData(
                            placeId = result.placeId,
                            searchText = text,
                            amazonLocationAddress = result.address,
                            text = result.title,
                            position =
                                listOf(
                                    result.position?.get(0) ?: 0.0,
                                    result.position?.get(1) ?: 0.0,
                                ),
                                distance =
                                getDistance(
                                    liveLocation,
                                    result.position
                                        ?.get(1) ?: 0.0,
                                    result.position
                                        ?.get(0) ?: 0.0,
                                ),
                        )
                    mList.add(placeData)
                }
            }
            searchSuggestionResponse.data = mList
            searchSuggestionResponse
        } catch (e: Exception) {
            mBaseActivity?.handleException(e, apiError)
            SearchSuggestionResponse(error = apiError)
        }

    suspend fun getPlace(
        placeId: String,
        mBaseActivity: BaseActivity?,
        getPlaceClient: GeoPlacesClient?,
    ): aws.sdk.kotlin.services.geoplaces.model.GetPlaceResponse? =
        try {
            val request =
                aws.sdk.kotlin.services.geoplaces.model.GetPlaceRequest {
                    this.additionalFeatures = listOf(GetPlaceAdditionalFeature.Contact)
                    this.key = getApiKey(mPreferenceManager)
                    this.placeId = placeId
                    this.language = getLanguageCode()
                }

            withContext(Dispatchers.IO) {
                getPlaceClient?.getPlace(request)
            }
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            null
        }

    suspend fun searchPlaceIndexForPosition(
        lng: Double?,
        lat: Double?,
        mBaseActivity: BaseActivity?,
        getPlaceClient: GeoPlacesClient?,
    ): ReverseGeocodeResponse? =
        try {
            val request =
                ReverseGeocodeRequest {
                    this.language = getLanguageCode()
                    this.queryPosition = listOfNotNull(lng, lat)
                    this.maxResults = SEARCH_MAX_SUGGESTION_RESULT
                }

            val response =
                withContext(Dispatchers.IO) {
                    getPlaceClient?.reverseGeocode(request)
                }
            response
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            null
        }

    suspend fun searchNavigationPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        mBaseActivity: BaseActivity?,
        getPlaceClient: GeoPlacesClient?,
    ): ReverseGeocodeResponse? =
        try {
            val request =
                ReverseGeocodeRequest {
                    this.language = getLanguageCode()
                    this.queryPosition = listOfNotNull(lng, lat)
                    this.maxResults = 1
                }

            withContext(Dispatchers.IO) {
                getPlaceClient?.reverseGeocode(request)
            }
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            null
        }

    fun getDistance(
        liveLocation: LatLng?,
        destinationLat: Double,
        destinationLng: Double,
    ): Double? {
        var distance: Double? = null
        if (liveLocation?.latitude != null) {
            val currentLocation = Location("currentLocation")
            currentLocation.latitude = liveLocation.latitude
            currentLocation.longitude = liveLocation.longitude

            val destinationLocation = Location("destinationLocation")
            destinationLocation.latitude = destinationLat
            destinationLocation.longitude = destinationLng
            val distanceMeters = currentLocation.distanceTo(destinationLocation).toDouble()
            distance =
                if (isMetric(
                        mPreferenceManager.getValue(
                            KEY_UNIT_SYSTEM,
                            "",
                        ),
                    )
                ) {
                    distanceMeters
                } else {
                    meterToFeet(distanceMeters)
                }
        }
        return distance
    }
}
