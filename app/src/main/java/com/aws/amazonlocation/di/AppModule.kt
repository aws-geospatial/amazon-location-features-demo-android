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
    fun getAWSLocationProvider(mPreferenceManager: PreferenceManager) =
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
    fun getAWSPlacesProvider(mMapHelper: MapHelper,mPreferenceManager: PreferenceManager) =
        PlacesProvider(mMapHelper, mPreferenceManager)

    @Provides
    @Singleton
    fun getAWSRoutesProvider(mPreferenceManager: PreferenceManager) =
        RoutesProvider(mPreferenceManager)

    @Provides
    @Singleton
    fun getAWSGeofenceProvider() = GeofenceProvider()

    @Provides
    @Singleton
    fun getAWSTrackingProvider() = TrackingProvider()

    @Provides
    @Singleton
    fun providesCommonRepositoryImp(
        @ApplicationContext appContext: Context,
        mAWSLocationHelper: LocationProvider,
        mAWSPlacesProvider: PlacesProvider,
        mAWSRoutesProvider: RoutesProvider,
        mAWSGeofenceProvider: GeofenceProvider,
        mAWSTrackingProvider: TrackingProvider,
    ): AuthRepository =
        AuthImp(RemoteDataSourceImpl(appContext, mAWSLocationHelper, mAWSPlacesProvider, mAWSRoutesProvider, mAWSGeofenceProvider, mAWSTrackingProvider))

    @Provides
    @Singleton
    fun providesLocationSearchRepository(
        @ApplicationContext appContext: Context,
        mAWSLocationHelper: LocationProvider,
        mAWSPlacesProvider: PlacesProvider,
        mAWSRoutesProvider: RoutesProvider,
        mAWSGeofenceProvider: GeofenceProvider,
        mAWSTrackingProvider: TrackingProvider,
    ): LocationSearchRepository =
        LocationSearchImp(RemoteDataSourceImpl(appContext, mAWSLocationHelper, mAWSPlacesProvider, mAWSRoutesProvider, mAWSGeofenceProvider, mAWSTrackingProvider))

    @Provides
    @Singleton
    fun providesGeofenceRepository(
        @ApplicationContext appContext: Context,
        mAWSLocationHelper: LocationProvider,
        mAWSPlacesProvider: PlacesProvider,
        mAWSRoutesProvider: RoutesProvider,
        mAWSGeofenceProvider: GeofenceProvider,
        mAWSTrackingProvider: TrackingProvider,
    ): GeofenceRepository =
        GeofenceImp(RemoteDataSourceImpl(appContext, mAWSLocationHelper, mAWSPlacesProvider, mAWSRoutesProvider, mAWSGeofenceProvider, mAWSTrackingProvider))
}
