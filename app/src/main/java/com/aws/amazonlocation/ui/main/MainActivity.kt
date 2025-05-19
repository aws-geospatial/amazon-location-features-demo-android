package com.aws.amazonlocation.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import aws.sdk.kotlin.services.cognitoidentity.model.Credentials
import aws.sdk.kotlin.services.iot.IotClient
import aws.sdk.kotlin.services.iot.model.AttachPolicyRequest
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.ActivityMainBinding
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.explore.ExploreFragment
import com.aws.amazonlocation.ui.main.mapStyle.MapStyleFragment
import com.aws.amazonlocation.ui.main.simulation.SimulationUtils
import com.aws.amazonlocation.ui.main.welcome.WelcomeBottomSheetFragment
import com.aws.amazonlocation.utils.ABOUT_FRAGMENT
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.ConnectivityObserveInterface
import com.aws.amazonlocation.utils.Durations.DELAY_FOR_FRAGMENT_LOAD
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.IOT_POLICY_UN_AUTH
import com.aws.amazonlocation.utils.IS_APP_FIRST_TIME_OPENED
import com.aws.amazonlocation.utils.KEY_AVOID_DIRT_ROADS
import com.aws.amazonlocation.utils.KEY_AVOID_FERRIES
import com.aws.amazonlocation.utils.KEY_AVOID_TOLLS
import com.aws.amazonlocation.utils.KEY_AVOID_TUNNELS
import com.aws.amazonlocation.utils.KEY_AVOID_U_TURNS
import com.aws.amazonlocation.utils.KeyBoardUtils
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.NetworkConnectivityObserveInterface
import com.aws.amazonlocation.utils.SETTING_FRAGMENT
import com.aws.amazonlocation.utils.SimulationDialogInterface
import com.aws.amazonlocation.utils.Units.checkInternetConnection
import com.aws.amazonlocation.utils.VERSION_FRAGMENT
import com.aws.amazonlocation.utils.analytics.AnalyticsUtils
import com.aws.amazonlocation.utils.analyticsFields
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.invisible
import com.aws.amazonlocation.utils.makeTransparentStatusBar
import com.aws.amazonlocation.utils.requiredFields
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.simulationExit
import com.aws.amazonlocation.utils.simulationFields
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class MainActivity :
    BaseActivity(), CrashListener {
    private var isSessionStarted: Boolean = false
    private var isMapStyleChangeCalled: Boolean = false
    private var isAppNotFirstOpened: Boolean = false
    private var isSimulationPolicyAttached: Boolean = false
    private lateinit var mNavHostFragment: NavHostFragment
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private var mBottomSheetDialog: Dialog? = null
    private var alertDialog: Dialog? = null
    private var currentPage: String? = null
    private var connectivityObserver: ConnectivityObserveInterface? = null
    var analyticsUtils: AnalyticsUtils? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val width = resources.getDimensionPixelSize(R.dimen.screen_size)
        mBinding.bottomNavigationMain.layoutParams.width = width
        mBinding.bottomNavigationMain.requestLayout()
    }

    override fun onResume() {
        super.onResume()
        if (!isSessionStarted) {
            checkSession()
        }
        isSessionStarted = false
        hideProgress()
    }

    private fun checkSession() {
        if (!mLocationProvider.checkClientInitialize()) {
            CoroutineScope(Dispatchers.IO).launch {
                async { initMobileClient() }.await()
            }
        } else {
            mLocationProvider.checkSessionValid(this)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isSessionStarted = true
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
        isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        CoroutineScope(Dispatchers.IO).launch {
            analyticsUtils = AnalyticsUtils(mLocationProvider, mPreferenceManager)
            async { analyticsUtils?.initAnalytics() }.await()
            analyticsUtils?.startSession()
            setSelectedScreen(AnalyticsAttributeValue.EXPLORER)
        }
        checkRtl()
        makeTransparentStatusBar()
        checkInternetObserver()
        mNavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        mNavController = mNavHostFragment.navController
        mBinding.bottomNavigationMain.setupWithNavController(mNavController)
        if (mBottomSheetDialog == null) {
            setBottomBar()
        }

        isAppNotFirstOpened = mPreferenceManager.getValue(IS_APP_FIRST_TIME_OPENED, false)

        if (!isAppNotFirstOpened) {
            mPreferenceManager.setValue(KEY_AVOID_TOLLS, true)
            mPreferenceManager.setValue(KEY_AVOID_FERRIES, true)
            mPreferenceManager.setValue(KEY_AVOID_TUNNELS, true)
            mPreferenceManager.setValue(KEY_AVOID_DIRT_ROADS, true)
            mPreferenceManager.setValue(KEY_AVOID_U_TURNS, true)
        }
        val inflater = mNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph)
        if (!isAppNotFirstOpened) {
            val welcomeSheet = WelcomeBottomSheetFragment()
            this.supportFragmentManager.let {
                welcomeSheet.show(it, WelcomeBottomSheetFragment::javaClass.name)
            }
            if (isTablet) {
                mBinding.bottomNavigationMain.invisible()
            } else {
                mBinding.bottomNavigationMain.hide()
            }
        } else {
            graph.setStartDestination(R.id.explore_fragment)
            mNavHostFragment.navController.graph = graph
            if (mBottomSheetDialog == null) {
                setBottomBar()
            }
        }
        initClick()
        KeyBoardUtils.attachKeyboardListeners(
            mBinding.root,
            object : KeyBoardUtils.KeyBoardInterface {
                override fun showKeyBoard() {
                }

                override fun hideKeyBoard() {
                    val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                    when (fragment) {
                        is ExploreFragment -> {
                            fragment.hideKeyBoard()
                        }

                        is MapStyleFragment -> {
                            fragment.hideKeyBoard()
                        }
                    }
                }
            }
        )
        addBackPressCallBack()
    }

    private fun addBackPressCallBack() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                    if (mNavController.currentDestination?.label == VERSION_FRAGMENT) {
                        mNavController.popBackStack()
                    } else if (mNavController.currentDestination?.label == ABOUT_FRAGMENT) {
                        if (fragment !is ExploreFragment) {
                            mNavController.navigate(R.id.explore_fragment)
                        }
                        moveToExploreScreen()
                    } else if (mNavController.currentDestination?.label == SETTING_FRAGMENT) {
                        if (fragment !is ExploreFragment) {
                            mNavController.navigate(R.id.explore_fragment)
                        }
                        moveToExploreScreen()
                    } else if (mBottomSheetHelper.isAttributeExpandedOrHalfExpand()) {
                        if (fragment is ExploreFragment) {
                            fragment.hideAttribution()
                        } else {
                            mBottomSheetHelper.hideAttributeSheet()
                        }
                    } else if (mBottomSheetHelper.isSearchBottomSheetExpandedOrHalfExpand()) {
                        mBottomSheetHelper.collapseSearchBottomSheet()
                    } else if (fragment is ExploreFragment && fragment.isMapStyleExpandedOrHalfExpand()) {
                        fragment.hideMapStyleSheet()
                    } else if (mBottomSheetHelper.isNavigationBottomSheetHalfExpand()) {
                        if (fragment is ExploreFragment) {
                            fragment.navigationExit()
                        }
                    } else if (mBottomSheetHelper.isNavigationBottomSheetFullyExpand()) {
                        mBottomSheetHelper.collapseNavigatingSheet()
                    } else if (mBottomSheetHelper.isDirectionSearchExpandedOrHalfExpand()) {
                        mBottomSheetHelper.collapseDirectionSearch()
                    } else if (mTrackingUtils?.isTrackingExpandedOrHalfExpand() == true) {
                        mNavController.navigate(R.id.explore_fragment)
                        moveToExploreScreen()
                    } else if (mSimulationUtils?.isSimulationBottomSheetVisible() == true) {
                        simulationExit(
                            object : SimulationDialogInterface {
                                override fun onExitClick(dialog: DialogInterface) {
                                    if (fragment is ExploreFragment) {
                                        fragment.hideSimulationTop()
                                    }
                                    hideSimulationSheet()
                                }
                            }
                        )
                    } else {
                        if (isEnabled) {
                            isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        )
    }

    private fun checkInternetObserver() {
        connectivityObserver = NetworkConnectivityObserveInterface(applicationContext)
        connectivityObserver
            ?.observer()
            ?.onEach {
                when (it) {
                    ConnectivityObserveInterface.ConnectionStatus.Available -> {
                        if (!mLocationProvider.checkClientInitialize()) {
                            initMobileClient()
                        }
                    }

                    ConnectivityObserveInterface.ConnectionStatus.Lost -> {
                    }

                    ConnectivityObserveInterface.ConnectionStatus.Unavailable -> {
                    }

                    else -> {
                    }
                }
            }?.launchIn(CoroutineScope(Dispatchers.Main))
    }

    private fun checkRtl() {
        if (isTablet) {
            val languageCode = getLanguageCode(this)
            val isRtl =
                languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW || languageCode == LANGUAGE_CODE_HEBREW_1
            if (isRtl) {
                mBinding.apply {
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(clMain)

                    constraintSet.clear(R.id.bottom_navigation_main, ConstraintSet.START)
                    constraintSet.clear(R.id.img_amazon_logo, ConstraintSet.START)
                    constraintSet.clear(R.id.iv_amazon_info, ConstraintSet.START)
                    constraintSet.connect(
                        R.id.bottom_navigation_main,
                        ConstraintSet.END,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.END
                    )
                    constraintSet.connect(
                        R.id.img_amazon_logo,
                        ConstraintSet.END,
                        R.id.bottom_navigation_main,
                        ConstraintSet.START
                    )
                    constraintSet.connect(
                        R.id.iv_amazon_info,
                        ConstraintSet.END,
                        R.id.img_amazon_logo,
                        ConstraintSet.START
                    )

                    constraintSet.applyTo(clMain)
                }
            }
        }
    }

    fun setSelectedScreen(screen: String) {
        currentPage = screen
        val properties = listOf(Pair(AnalyticsAttribute.SCREEN_NAME, screen))
        analyticsUtils?.recordEvent(EventType.SCREEN_OPEN, properties)
    }

    fun exitScreen() {
        currentPage?.let {
            val properties = listOf(Pair(AnalyticsAttribute.SCREEN_NAME, it))
            analyticsUtils?.recordEvent(EventType.SCREEN_CLOSE, properties)
        }
    }

    override fun onDestroy() {
        KeyBoardUtils.detachKeyboardListeners(mBinding.root)
        analyticsUtils?.stopSession()
        super.onDestroy()
    }

    private fun initClick() {
        mBinding.apply {
            ivAmazonInfo?.setOnClickListener {
                val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                if (fragment is ExploreFragment) {
                    fragment.setAttributionDataAndExpandSheet()
                }
            }
            ivBackMain.setOnClickListener {
                hideViews(
                    mBinding.ivBackMain,
                    mBinding.viewBottom,
                    mBinding.appCompatTextView
                )
                showViews(
                    mBinding.bottomNavigationMain,
                    mBinding.navHostFragment,
                    mBinding.imgAmazonLogo,
                    mBinding.ivAmazonInfo
                )
                mBottomSheetDialog?.show()
            }
        }
    }

    fun trackingClick() {
        mBinding.bottomNavigationMain.selectedItemId = R.id.menu_tracking
    }

    private fun setSimulationIotPolicy() {
        val identityId = mLocationProvider.getIdentityId()
        if (identityId.isNullOrEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            val attachPolicyRequest =
                AttachPolicyRequest {
                    policyName = IOT_POLICY_UN_AUTH
                    target = identityId
                }

            val iotClient =
                IotClient {
                    region = identityId.split(":")[0]
                    credentialsProvider =
                        createCredentialsProviderForPolicy(
                            mLocationProvider.getCredentials()
                        )
                }

            try {
                iotClient.attachPolicy(attachPolicyRequest)
                isSimulationPolicyAttached = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createCredentialsProviderForPolicy(
        credentials: Credentials?
    ): aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider {
        if (credentials?.accessKeyId == null || credentials.sessionToken == null || credentials.secretKey == null) {
            throw Exception(
                "Credentials not found"
            )
        }
        return aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider(
            aws.smithy.kotlin.runtime.auth.awscredentials.Credentials.invoke(
                accessKeyId = credentials.accessKeyId!!,
                secretAccessKey = credentials.secretKey!!,
                sessionToken = credentials.sessionToken
            )
        )
    }

    private fun setBottomBar() {
        mBinding.bottomNavigationMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_explore -> {
                    setExplorer()
                    showAmazonLogo()
                    exitScreen()
                    setSelectedScreen(AnalyticsAttributeValue.EXPLORER)
                }

                R.id.menu_tracking -> {
                    val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                    if (fragment !is ExploreFragment) {
                        mNavController.navigate(R.id.explore_fragment)
                    }

                    hideSearchSheet(fragment)
                    if (mTrackingUtils?.isTrackingSheetHidden() == true) {
                        showTrackingPreview(fragment)
                    }
                    showAmazonLogo()
                }

                R.id.menu_settings -> {
                    mBottomSheetHelper.hideSearchBottomSheet(false)
                    mNavController.navigate(R.id.setting_fragment)
                    mTrackingUtils?.hideTrackingBottomSheet()
                    mSimulationUtils?.hideSimulationBottomSheet()
                    hideAmazonLogo()
                    exitScreen()
                    setSelectedScreen(AnalyticsAttributeValue.SETTINGS)
                }

                R.id.menu_more -> {
                    mBottomSheetHelper.hideSearchBottomSheet(false)
                    mNavController.navigate(R.id.about_fragment)
                    mTrackingUtils?.hideTrackingBottomSheet()
                    mSimulationUtils?.hideSimulationBottomSheet()
                    hideAmazonLogo()
                    exitScreen()
                    setSelectedScreen(AnalyticsAttributeValue.ABOUT)
                }
            }
            true
        }
    }

    private fun showTrackingPreview(fragment: Fragment?) {
        lifecycleScope.launch {
            if (fragment !is ExploreFragment) {
                delay(DELAY_FOR_FRAGMENT_LOAD) // Need delay for showing bottomsheet after fragment load
            }
            mBottomSheetHelper.hideSearchBottomSheet(true)
            mTrackingUtils?.showTrackingBottomSheet()
        }
    }

    private fun hideSearchSheet(fragment: Fragment?) {
        mBottomSheetHelper.hideSearchBottomSheet(true)
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            mBottomSheetHelper.hideDirectionSearchBottomSheet(fragment as ExploreFragment)
        }
    }

    private fun hideAmazonLogo() {
        mBinding.imgAmazonLogo?.hide()
        mBinding.ivAmazonInfo?.hide()
    }

    private fun showAmazonLogo() {
        mBinding.imgAmazonLogo?.show()
        mBinding.ivAmazonInfo?.show()
    }

    fun changeAmazonLogo(logoResId: Int) {
        mBinding.imgAmazonLogo?.setImageResource(logoResId)
    }

    private fun setExplorer() {
        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment !is ExploreFragment) {
            mNavController.navigate(R.id.explore_fragment)
        } else {
            fragment.showDirectionAndCurrentLocationIcon()
        }
        mBottomSheetHelper.hideSearchBottomSheet(false)
        if (!isTablet && fragment is ExploreFragment) {
            fragment.hideMapStyleSheet()
        }
        mTrackingUtils?.hideTrackingBottomSheet()
        mSimulationUtils?.hideSimulationBottomSheet()
    }

    fun showBottomBar() {
        mBinding.bottomNavigationMain.show()
    }

    fun hideSimulationSheet() {
        showBottomBar()
        mSimulationUtils?.hideSimulationBottomSheet()
        mBottomSheetHelper.hideSearchBottomSheet(false)
        mBinding.bottomNavigationMain.selectedItemId =
            R.id.menu_explore
        showNavigationIcon()
        showDirectionAndCurrentLocationIcon()
    }

    fun showSimulationSheet() {
        if (mSimulationUtils == null) {
            mSimulationUtils =
                SimulationUtils(mPreferenceManager, this@MainActivity, mLocationProvider)
            if (mNavHostFragment.childFragmentManager.fragments.isNotEmpty()) {
                val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                if (fragment is ExploreFragment) {
                    fragment.initSimulationView()
                    fragment.setMapBoxInSimulation()
                }
            }
        }
        mBottomSheetHelper.hideSearchBottomSheet(true)
        if (isTablet) {
            mBinding.bottomNavigationMain.invisible()
        } else {
            mBinding.bottomNavigationMain.hide()
        }
        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
        if (!isTablet && fragment is ExploreFragment) {
            fragment.hideMapStyleSheet()
        }
        showSimulationTop()
        if (checkInternetConnection(applicationContext)) {
            if (!isSimulationPolicyAttached) {
                lifecycleScope.launch {
                    setSimulationIotPolicy()
                }
            }
        }
        mTrackingUtils?.hideTrackingBottomSheet()
        mSimulationUtils?.showSimulationBottomSheet()
        if (mNavHostFragment.childFragmentManager.fragments.isNotEmpty()) {
            if (fragment is ExploreFragment) {
                if (isTablet) {
                    fragment.hideDirectionAndCurrentLocationIcon()
                }
            }
        }
    }

    fun reInitializeSimulation() {
        mSimulationUtils = null
    }

    fun showNavigationIcon() {
        if (mNavHostFragment.childFragmentManager.fragments.isNotEmpty()) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
            if (fragment is ExploreFragment) {
                fragment.showCurrentLocationIcon()
            }
        }
    }

    fun showDirectionAndCurrentLocationIcon() {
        if (mNavHostFragment.childFragmentManager.fragments.isNotEmpty()) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
            if (fragment is ExploreFragment) {
                fragment.showDirectionAndCurrentLocationIcon()
                fragment.showGeofence()
            }
        }
    }

    private fun showSimulationTop() {
        if (mNavHostFragment.childFragmentManager.fragments.isNotEmpty()) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
            if (fragment is ExploreFragment) {
                fragment.showSimulationTop()
                fragment.hideGeofence()
            }
        }
    }

    fun checkMapLoaded(): Boolean {
        if (mNavHostFragment.childFragmentManager.fragments.isNotEmpty()) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
            if (fragment is ExploreFragment) {
                return fragment.checkMapLoaded()
            }
        }
        return false
    }

    fun manageBottomNavigationVisibility(isVisibility: Boolean = true) {
        mBinding.apply {
            if (isVisibility) {
                bottomNavigationMain.animate().translationY(0f).start()
                bottomNavigationMain.show()
            } else {
                if (isTablet) {
                    mBinding.bottomNavigationMain.invisible()
                } else {
                    bottomNavigationMain.animate().translationY(100f).start()
                    bottomNavigationMain.hide()
                }
            }
        }
    }

    fun moveToExploreScreen() {
        if (!isMapStyleChangeCalled) {
            showAmazonLogo()
            mBinding.bottomNavigationMain.menu
                .findItem(R.id.menu_explore)
                .isChecked = true
        } else {
            isMapStyleChangeCalled = false
        }
    }

    private fun hideProgress() {
        runOnUiThread {
            alertDialog?.hide()
        }
    }

    fun checkPropertiesData() {
        val missingRequiredFields = requiredFields.filter { it.value == "null" }.keys
        val simulationMissingFields = simulationFields.filter { it.value == "null" }.keys
        val analyticsMissingFields = analyticsFields.filter { it.value == "null" }.keys

        if (missingRequiredFields.isNotEmpty() || simulationMissingFields.isNotEmpty() || analyticsMissingFields.isNotEmpty()) {
            val dialogMessage = buildString {
                when {
                    missingRequiredFields.isNotEmpty() -> {
                        append(getString(R.string.label_required_fields_missing))
                        append("\n")
                        missingRequiredFields.forEach { append("• $it\n") }
                        simulationMissingFields.forEach { append("• $it\n") }
                        analyticsMissingFields.forEach { append("• $it\n") }
                    }
                    simulationMissingFields.isNotEmpty() && analyticsMissingFields.isNotEmpty() -> {
                        append(getString(R.string.label_some_fields_missing))
                        append("\n")
                        simulationMissingFields.forEach { append("• $it\n") }
                        analyticsMissingFields.forEach { append("• $it\n") }
                    }
                    simulationMissingFields.isNotEmpty() -> {
                        append(getString(R.string.label_simulation_fields_missing))
                        append("\n")
                        simulationMissingFields.forEach { append("• $it\n") }
                    }
                    analyticsMissingFields.isNotEmpty() -> {
                        append(getString(R.string.label_analytics_fields_missing))
                        append("\n")
                        analyticsMissingFields.forEach { append("• $it\n") }
                    }
                }
            }

            val dialogTitle = getString(R.string.title_configuration_incomplete)
            val positiveButtonText = if (missingRequiredFields.isNotEmpty()) {
                getString(R.string.ok)
            } else {
                getString(R.string.label_continue)
            }

            AlertDialog
                .Builder(this)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setPositiveButton(positiveButtonText) { _, _ ->
                    if (missingRequiredFields.isNotEmpty()) {
                        finish()
                    } else {
                        setWelcomeToExplorer()
                    }
                }.setCancelable(false).show()
        } else {
            setWelcomeToExplorer()
        }
    }

    private fun setWelcomeToExplorer() {
        mPreferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        isAppNotFirstOpened = true
        val fragment =
            mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment is ExploreFragment) {
            fragment.checkPermissionFromWelcome()
        }
    }

    fun isAppNotFirstOpened(): Boolean = isAppNotFirstOpened

    override fun notifyAppCrash(message: String?) {
        if (!isDestroyed) {
            message?.let {
                val properties = listOf(Pair(AnalyticsAttribute.ERROR, it))
                analyticsUtils?.recordEvent(EventType.APPLICATION_ERROR, properties)
            }
        }
    }
}
