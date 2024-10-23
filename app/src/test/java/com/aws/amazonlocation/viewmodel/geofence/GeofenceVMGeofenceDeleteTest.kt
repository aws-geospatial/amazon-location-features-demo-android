package com.aws.amazonlocation.viewmodel.geofence

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import aws.sdk.kotlin.services.location.model.Circle
import aws.sdk.kotlin.services.location.model.GeofenceGeometry
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import aws.smithy.kotlin.runtime.time.Instant
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.GeofenceImp
import com.aws.amazonlocation.data.response.DeleteGeofence
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import com.aws.amazonlocation.mock.NO_DATA_FOUND
import com.aws.amazonlocation.mock.TEST_DATA_7
import com.aws.amazonlocation.mock.TEST_DATA_8
import com.aws.amazonlocation.mock.TEST_DATA_LAT_1
import com.aws.amazonlocation.mock.TEST_DATA_LNG_1
import com.aws.amazonlocation.ui.main.geofence.GeofenceViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeofenceVMGeofenceDeleteTest : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var geofenceImp: GeofenceImp

    private lateinit var geofenceUseCase: GeofenceUseCase

    private lateinit var mGeofenceViewModel: GeofenceViewModel

    override fun setUp() {
        super.setUp()

        geofenceImp = GeofenceImp(mRemoteDataSourceImpl)
        geofenceUseCase = GeofenceUseCase(geofenceImp)
        mGeofenceViewModel = GeofenceViewModel(geofenceUseCase)
    }

    @Test
    fun geofenceAddSuccess() = runTest {
        Mockito.`when`(
            mRemoteDataSourceImpl.deleteGeofence(
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenAnswer {
            val callback: GeofenceAPIInterface = it.arguments[2] as GeofenceAPIInterface
            callback.deleteGeofence(
                DeleteGeofence(
                    position = 1,
                    data = ListGeofenceResponseEntry {
                        geofenceId = TEST_DATA_8
                        status = TEST_DATA_7
                        createTime = Instant.now()
                        updateTime = Instant.now()
                        geometry = GeofenceGeometry {
                            circle = Circle {
                                center = listOf(TEST_DATA_LAT_1, TEST_DATA_LNG_1)
                                radius = 100.0
                            }
                        }
                    }
                )
            )
        }

        mGeofenceViewModel.mDeleteGeofence.test {
            mGeofenceViewModel.deleteGeofence(
                1,
                ListGeofenceResponseEntry {
                    geofenceId = TEST_DATA_8
                    status = TEST_DATA_7
                    createTime = Instant.now()
                    updateTime = Instant.now()
                    geometry = GeofenceGeometry {
                        circle = Circle {
                            center = listOf(TEST_DATA_LAT_1, TEST_DATA_LNG_1)
                            radius = 100.0
                        }
                    }
                }
            )
            val result = awaitItem()
            assert(result is HandleResult.Success)
            assert((result as HandleResult.Success).response.data != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun geofenceDeleteFail() = runTest {
        Mockito.`when`(
            mRemoteDataSourceImpl.deleteGeofence(
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenAnswer {
            val callback: GeofenceAPIInterface = it.arguments[2] as GeofenceAPIInterface
            callback.deleteGeofence(
                DeleteGeofence(data = null, errorMessage = NO_DATA_FOUND)
            )
        }

        mGeofenceViewModel.mDeleteGeofence.test {
            mGeofenceViewModel.deleteGeofence(
                1,
                ListGeofenceResponseEntry {
                    geofenceId = TEST_DATA_8
                    status = TEST_DATA_7
                    createTime = Instant.now()
                    updateTime = Instant.now()
                    geometry = GeofenceGeometry {
                        circle = Circle {
                            center = listOf(TEST_DATA_LAT_1, TEST_DATA_LNG_1)
                            radius = 100.00
                        }
                    }
                }
            )
            val result = awaitItem()
            assert(result is HandleResult.Error)
            (result as HandleResult.Error).exception.messageResource?.equals(NO_DATA_FOUND)
                ?.let { assert(it) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
