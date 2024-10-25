package com.aws.amazonlocation.utils.providers

import android.location.Location
import aws.sdk.kotlin.services.geoplaces.GeoPlacesClient
import aws.sdk.kotlin.services.geoplaces.model.Address
import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeRequest
import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeResponse
import aws.sdk.kotlin.services.geoplaces.model.SearchTextRequest
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
                    key = getApiKey(mPreferenceManager)
                    this.queryText = text
                    this.language = getLanguageCode()
                    this.maxResults = SEARCH_MAX_SUGGESTION_RESULT
                    this.biasPosition = listOf(lng ?: 0.0, lat ?: 0.0)
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
            if (validateLatLng(searchText) != null) {
                isLatLng = true
                val mLatLng = validateLatLng(searchText)
                reverseGeocodeResponse =
                    searchPlaceIndexForPosition(
                        lng = mLatLng?.longitude,
                        lat = mLatLng?.latitude,
                        mBaseActivity,
                        getPlaceClient,
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
            }

            val mList = ArrayList<SearchSuggestionData>()
            val response =
                SearchSuggestionResponse(
                    text = searchText,
                    maxResults =
                        reverseGeocodeResponse?.resultItems?.size
                            ?: suggestResponse?.resultItems?.size,
                )
            if (isLatLng) {
                addMarkerBasedOnLatLng(response, searchText, mList)
            }

            reverseGeocodeResponse?.resultItems?.forEach {
                val mSearchSuggestionData: SearchSuggestionData =
                    if (it.placeId.isNotEmpty()) {
                        val getSearchResult = getPlace(it.placeId, mBaseActivity, getPlaceClient)
                        SearchSuggestionData(
                            placeId = it.placeId,
                            text = getSearchResult?.address?.label,
                            amazonLocationPlace = getSearchResult?.address,
                            distance =
                                getDistance(
                                    liveLocation,
                                    getSearchResult?.position?.get(1)!!,
                                    getSearchResult.position?.get(0)!!,
                                ),
                            position =
                                listOf(
                                    getSearchResult.position?.get(0)!!,
                                    getSearchResult.position?.get(1)!!,
                                ),
                        )
                    } else {
                        SearchSuggestionData(text = it.address?.label)
                    }
                mList.add(mSearchSuggestionData)
            }
            suggestResponse?.resultItems?.forEach {
                val mSearchSuggestionData: SearchSuggestionData =
                    if (!it.place?.placeId.isNullOrEmpty()) {
                        val getSearchResult =
                            getPlace(it.place?.placeId!!, mBaseActivity, getPlaceClient)
                        SearchSuggestionData(
                            placeId = it.place!!.placeId,
                            searchText = searchText,
                            text = getSearchResult?.address?.label,
                            amazonLocationPlace = getSearchResult?.address,
                            distance =
                                getDistance(
                                    liveLocation,
                                    getSearchResult?.position?.get(1)!!,
                                    getSearchResult.position?.get(0)!!,
                                ),
                            position =
                                listOf(
                                    getSearchResult.position?.get(0)!!,
                                    getSearchResult.position?.get(1)!!,
                                ),
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
        val mLatLng = validateLatLng(searchText)
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
                    amazonLocationPlace = place,
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
                    this.key = getApiKey(mPreferenceManager)
                    if (mText != null) this.queryText = mText
                    if (queryId != null) this.queryId = queryId
                    if (mText != null) this.language = getLanguageCode()
                    if (mText != null) this.maxResults = SEARCH_MAX_RESULT
                    this.biasPosition = listOfNotNull(lng, lat)
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
            if (validateLatLng(text!!) != null && response?.resultItems?.isEmpty()!!) {
                addMarkerBasedOnLatLng(searchSuggestionResponse, text, mList)
            } else {
                response?.resultItems?.forEach { result ->
                    val placeData =
                        SearchSuggestionData(
                            searchText = text,
                            amazonLocationPlace = result.address,
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
                    this.key = getApiKey(mPreferenceManager)
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
                    this.key = getApiKey(mPreferenceManager)
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
