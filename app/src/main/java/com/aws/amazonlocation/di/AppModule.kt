package com.aws.amazonlocation.di

import android.content.Context
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.AuthImp
import com.aws.amazonlocation.data.repository.GeofenceImp
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.domain.repository.AuthRepository
import com.aws.amazonlocation.domain.repository.GeofenceRepository
import com.aws.amazonlocation.domain.repository.LocationSearchRepository
import com.aws.amazonlocation.utils.providers.GeofenceProvider
import com.aws.amazonlocation.utils.providers.LocationProvider
import com.aws.amazonlocation.utils.providers.PlacesProvider
import com.aws.amazonlocation.utils.providers.RoutesProvider
import com.aws.amazonlocation.utils.providers.TrackingProvider
import com.aws.amazonlocation.utils.BottomSheetHelper
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * provide different types of data with[dagger.hilt]
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun getPreferenceManager(@ApplicationContext appContext: Context) =
        PreferenceManager(appContext)

    @Provides
    @Singleton
    fun getLocationProvider(mPreferenceManager: PreferenceManager) =
        LocationProvider(mPreferenceManager)

    @Provides
    @Singleton
    fun getBottomSheetUtils() =
        BottomSheetHelper()

    @Provides
    @Singleton
    fun getMapHelper(@ApplicationContext appContext: Context) =
        MapHelper(appContext)

    @Provides
    @Singleton
    fun getPlacesProvider(mMapHelper: MapHelper,mPreferenceManager: PreferenceManager) =
        PlacesProvider(mMapHelper, mPreferenceManager)

    @Provides
    @Singleton
    fun getRoutesProvider(mPreferenceManager: PreferenceManager) =
        RoutesProvider(mPreferenceManager)

    @Provides
    @Singleton
    fun getGeofenceProvider() = GeofenceProvider()

    @Provides
    @Singleton
    fun getTrackingProvider() = TrackingProvider()

    @Provides
    @Singleton
    fun providesCommonRepositoryImp(
        @ApplicationContext appContext: Context,
        mLocationHelper: LocationProvider,
        mPlacesProvider: PlacesProvider,
        mRoutesProvider: RoutesProvider,
        mGeofenceProvider: GeofenceProvider,
        mTrackingProvider: TrackingProvider,
    ): AuthRepository =
        AuthImp(RemoteDataSourceImpl(appContext, mLocationHelper, mPlacesProvider, mRoutesProvider, mGeofenceProvider, mTrackingProvider))

    @Provides
    @Singleton
    fun providesLocationSearchRepository(
        @ApplicationContext appContext: Context,
        mLocationProvider: LocationProvider,
        mPlacesProvider: PlacesProvider,
        mRoutesProvider: RoutesProvider,
        mGeofenceProvider: GeofenceProvider,
        mTrackingProvider: TrackingProvider,
    ): LocationSearchRepository =
        LocationSearchImp(RemoteDataSourceImpl(appContext, mLocationProvider, mPlacesProvider, mRoutesProvider, mGeofenceProvider, mTrackingProvider))

    @Provides
    @Singleton
    fun providesGeofenceRepository(
        @ApplicationContext appContext: Context,
        mLocationProvider: LocationProvider,
        mPlacesProvider: PlacesProvider,
        mRoutesProvider: RoutesProvider,
        mGeofenceProvider: GeofenceProvider,
        mTrackingProvider: TrackingProvider,
    ): GeofenceRepository =
        GeofenceImp(RemoteDataSourceImpl(appContext, mLocationProvider, mPlacesProvider, mRoutesProvider, mGeofenceProvider, mTrackingProvider))
}
