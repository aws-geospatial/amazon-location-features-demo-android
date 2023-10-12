package com.aws.amazonlocation.ui.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.Tokens
import com.amazonaws.mobileconnectors.cognitoauth.AuthClient
import com.amazonaws.regions.Region
import com.amazonaws.services.iot.AWSIotClient
import com.amazonaws.services.iot.model.AttachPolicyRequest
import com.aws.amazonlocation.AmazonLocationApp
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.onError
import com.aws.amazonlocation.data.common.onLoading
import com.aws.amazonlocation.data.common.onSuccess
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.data.enum.TabEnum
import com.aws.amazonlocation.data.enum.TrackingEnum
import com.aws.amazonlocation.databinding.ActivityMainBinding
import com.aws.amazonlocation.domain.`interface`.CloudFormationInterface
import com.aws.amazonlocation.domain.`interface`.SignInConnectInterface
import com.aws.amazonlocation.domain.`interface`.SignInRequiredInterface
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.explore.ExploreFragment
import com.aws.amazonlocation.ui.main.map_style.MapStyleFragment
import com.aws.amazonlocation.ui.main.setting.AWSCloudInformationFragment
import com.aws.amazonlocation.ui.main.setting.SettingFragment
import com.aws.amazonlocation.ui.main.signin.SignInViewModel
import com.aws.amazonlocation.ui.main.simulation.SimulationUtils
import com.aws.amazonlocation.ui.main.welcome.WelcomeBottomSheetFragment
import com.aws.amazonlocation.utils.*
import com.aws.amazonlocation.utils.Durations.DELAY_FOR_FRAGMENT_LOAD
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class MainActivity : BaseActivity(), CrashListener {

    var analyticsHelper: AnalyticsHelper? = null
    private var isAppNotFirstOpened: Boolean = false
    private var reStartApp: Boolean = false
    private var isSimulationPolicyAttached: Boolean = false
    private lateinit var mNavHostFragment: NavHostFragment
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private var mAuthStatus: String? = null
    private val mSignInViewModel: SignInViewModel by viewModels()
    private var mBottomSheetDialog: Dialog? = null
    private var alertDialog: Dialog? = null
    private var currentPage: String? = null
    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkMap()
        }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val width = resources.getDimensionPixelSize(R.dimen.screen_size)
        mBinding.bottomNavigationMain.layoutParams.width = width
        mBinding.bottomNavigationMain.requestLayout()
    }
    override fun onResume() {
        super.onResume()
        // check session expired
        val credentialsProvider = mAWSLocationHelper.getCognitoCachingCredentialsProvider()
        val sessionExpiration: Date? = credentialsProvider?.sessionCredentialsExpiration

        if (sessionExpiration != null) {
            val currentDate = Date()
            if (currentDate.after(sessionExpiration)) {
                // Session has expired
                credentialsProvider.refresh()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
        if (!isRunningTest) {
            (application as AmazonLocationApp).setCrashListener(this)
        }
        isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        analyticsHelper =
            AnalyticsHelper(this@MainActivity, mAWSLocationHelper, mPreferenceManager)
        analyticsHelper?.initAnalytics()
        analyticsHelper?.startSession()
        checkRtl()
        makeTransparentStatusBar()
        reStartApp = mPreferenceManager.getBooleanValue(KEY_RE_START_APP, false)
        val mTab = mPreferenceManager.getValue(KEY_TAB_ENUM, "")
        mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        initObserver()
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
            graph.startDestination = R.id.explore_fragment
            mNavHostFragment.navController.graph = graph
            if (mBottomSheetDialog == null) {
                setBottomBar()
            }
        }
        if (reStartApp) {
            if (!mTab.isNullOrEmpty()) {
                when (mTab) {
                    TabEnum.TAB_EXPLORE.name -> {
                        mAuthStatus = mPreferenceManager.getValue(
                            KEY_CLOUD_FORMATION_STATUS,
                            AuthEnum.DEFAULT.name
                        )
                        when (mAuthStatus) {
                            AuthEnum.AWS_CONNECTED.name -> {
                                if (reStartApp) {
                                    reStartApp = false
                                    mPreferenceManager.removeValue(KEY_RE_START_APP)
                                    mGeofenceBottomSheetHelper.signInConnectedBottomSheet(
                                        mSignInConnectInterface
                                    )
                                }
                            }
                        }
                        setSelectedScreen(AnalyticsAttributeValue.EXPLORER)
                    }
                    TabEnum.TAB_TRACKING.name -> {
                        mBinding.bottomNavigationMain.selectedItemId =
                            R.id.menu_tracking
                        setSelectedScreen(AnalyticsAttributeValue.TRACKERS)
                    }
                    TabEnum.TAB_GEOFENCE.name -> {
                        mBinding.bottomNavigationMain.selectedItemId =
                            R.id.menu_geofence
                        setSelectedScreen(AnalyticsAttributeValue.GEOFENCES)
                    }
                }
            } else {
                setSelectedScreen(AnalyticsAttributeValue.EXPLORER)
            }
        } else {
            setSelectedScreen(AnalyticsAttributeValue.EXPLORER)
        }
        initClick()
        KeyBoardUtils.attachKeyboardListeners(
            mBinding.root,
            object : KeyBoardUtils.KeyBoardInterface {
                override fun showKeyBoard() {
                    val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                    if (fragment is ExploreFragment) {
                        if (!mGeofenceBottomSheetHelper.isCloudFormationBottomSheetVisible()) {
                            fragment.showKeyBoard()
                        }
                    }
                }

                override fun hideKeyBoard() {
                    val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                    if (fragment is ExploreFragment) {
                        if (mGeofenceBottomSheetHelper.isCloudFormationBottomSheetVisible()) {
                            mGeofenceBottomSheetHelper.cloudFormationBottomSheetHideKeyboard()
                        } else {
                            fragment.hideKeyBoard()
                        }
                    } else if (fragment is AWSCloudInformationFragment) {
                        fragment.hideKeyBoard()
                    } else if (fragment is MapStyleFragment) {
                        fragment.hideKeyBoard()
                    } else if (fragment is SettingFragment) {
                        fragment.hideKeyBoard()
                    }
                }
            }
        )
        lifecycleScope.launch {
            delay(DELAY_LANGUAGE_3000)
            val languageCode = getLanguageCode()
            languageCode?.let { setLocale(it, applicationContext) }
        }
        val isAppCrashed = mPreferenceManager.getBooleanValue(KEY_APP_CRASH, false)
        if (isAppCrashed) {
            mPreferenceManager.setValue(KEY_APP_CRASH, false)
            appCrashedDialog()
        }
    }

    // check rtl layout
    private fun checkRtl() {
        if (isTablet) {
            val languageCode = getLanguageCode()
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
        val properties = listOf(
            Pair(AnalyticsAttribute.SCREEN_NAME, screen)
        )
        analyticsHelper?.recordEvent(EventType.SCREEN_OPEN, properties)
    }

    fun exitScreen() {
        currentPage?.let {
            val properties = listOf(
                Pair(AnalyticsAttribute.SCREEN_NAME, it)
            )
            analyticsHelper?.recordEvent(EventType.SCREEN_CLOSE, properties)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        KeyBoardUtils.detachKeyboardListeners(mBinding.root)
        analyticsHelper?.stopSession()
    }

    private fun initClick() {
        mBinding.apply {
            ivAmazonInfo?.setOnClickListener {
                val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                if (fragment is ExploreFragment) {
                    fragment.setAttributionDataAndExpandSheet()
                }
            }
        }
    }

    fun geofenceClick() {
        mBinding.bottomNavigationMain.selectedItemId = R.id.menu_geofence
    }

    fun getBottomNavHeight(): Int {
        return mBinding.bottomNavigationMain.height
    }

    private fun initObserver() {
        lifecycleScope.launchWhenStarted {
            mSignInViewModel.mSignInResponse.collect { handleResult ->
                handleResult.onLoading {
                }.onSuccess {
                    mIsUserLoggedIn = true
                    mPreferenceManager.setValue(
                        KEY_CLOUD_FORMATION_STATUS,
                        AuthEnum.SIGNED_IN.name
                    )
                    getTokenAndAttachPolicy(it)
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.EXPLORER)
                    )
                    analyticsHelper?.recordEvent(EventType.SIGN_IN_SUCCESSFUL, propertiesAws)
                }.onError { it ->
                    setBottomBar()
                    hideProgress()
                    it.messageResource?.let {
                        showError(it.toString())
                    }
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.EXPLORER)
                    )
                    analyticsHelper?.recordEvent(EventType.SIGN_IN_FAILED, propertiesAws)
                }
            }
        }
    }

    fun getTokenAndAttachPolicy(it: String) {
        AWSMobileClient.getInstance().getTokens(object : Callback<Tokens> {
            override fun onResult(result: Tokens?) {
                result?.accessToken?.tokenString?.let { accessToken ->
                    mPreferenceManager.setValue(
                        KEY_ACCESS_TOKEN,
                        accessToken
                    )
                }
                result?.refreshToken?.tokenString?.let { refreshToken ->
                    mPreferenceManager.setValue(
                        KEY_REFRESH_TOKEN,
                        refreshToken
                    )
                }

                val mCognitoCredentialsProvider: CognitoCredentialsProvider? =
                    mAWSLocationHelper.initCognitoCachingCredentialsProvider()

                val identityId: String? =
                    mCognitoCredentialsProvider?.identityId

                // Initialize the AWSIotMqttManager with the configuration
                val attachPolicyReq =
                    AttachPolicyRequest().withPolicyName(IOT_POLICY)
                        .withTarget(identityId)
                val mIotAndroidClient =
                    AWSIotClient(mCognitoCredentialsProvider)
                var region = mPreferenceManager.getValue(KEY_USER_REGION, "")

                if (region.isNullOrEmpty()) {
                    region = BuildConfig.DEFAULT_REGION
                }
                mIotAndroidClient.setRegion(Region.getRegion(region))
                mIotAndroidClient.attachPolicy(attachPolicyReq)
                showError(it)
                hideProgressAndShowData()
            }

            override fun onError(e: Exception?) {
                hideProgressAndShowData()
            }
        })
    }

    private fun setSimulationIotPolicy() {
        val mCognitoCredentialsProvider: CognitoCredentialsProvider? =
            mAWSLocationHelper.initCognitoCachingCredentialsProvider()

        val identityId: String? =
            mCognitoCredentialsProvider?.identityId

        // Initialize the AWSIotMqttManager with the configuration
        val attachPolicyReq =
            AttachPolicyRequest().withPolicyName(IOT_POLICY_UN_AUTH)
                .withTarget(identityId)
        val mIotAndroidClient =
            AWSIotClient(mCognitoCredentialsProvider)
        var region = mPreferenceManager.getValue(KEY_USER_REGION, "")

        if (region.isNullOrEmpty()) {
            if (identityId != null) {
                region = identityId.split(":")[0]
            }
        }
        mIotAndroidClient.setRegion(Region.getRegion(region))
        mIotAndroidClient.attachPolicy(attachPolicyReq)
        isSimulationPolicyAttached = true
    }

    private fun hideProgressAndShowData() {
        hideProgress()
        runOnUiThread {
            if (mBinding.bottomNavigationMain.selectedItemId == R.id.menu_geofence) {
                mGeofenceUtils?.showGeofenceListBottomSheet(this@MainActivity)
                mBottomSheetHelper.hideSearchBottomSheet(true)
            } else if (mBinding.bottomNavigationMain.selectedItemId == R.id.menu_tracking) {
                if (checkMap()) {
                    mBottomSheetHelper.hideSearchBottomSheet(true)
                    showTracking()
                }
            }
            setBottomBar()
        }
        mBottomSheetDialog?.dismiss()
        mBottomSheetDialog = null
    }

    private fun setBottomBar() {
        mBinding.bottomNavigationMain.setOnItemSelectedListener { item ->
            mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
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

                    mGeofenceUtils?.hideAllGeofenceBottomSheet()
                    mAuthStatus = mPreferenceManager.getValue(
                        KEY_CLOUD_FORMATION_STATUS,
                        AuthEnum.DEFAULT.name
                    )
                    when (mAuthStatus) {
                        AuthEnum.DEFAULT.name -> {
                            hideSearchSheet(fragment)
                            if (mTrackingUtils?.isTrackingSheetHidden() == true) {
                                showTrackingPreview(fragment)
                            }
                        }
                        AuthEnum.AWS_CONNECTED.name -> {
                            if (reStartApp) {
                                reStartApp = false
                                mPreferenceManager.removeValue(KEY_RE_START_APP)
                                mGeofenceBottomSheetHelper.signInConnectedBottomSheet(
                                    mSignInConnectInterface
                                )
                            } else {
                                mGeofenceBottomSheetHelper.signInRequiredBottomSheet(
                                    mSignInRequiredInterface
                                )
                            }
                        }
                        AuthEnum.SIGNED_IN.name -> {
                            hideSearchSheet(fragment)
                            if (mTrackingUtils?.isTrackingSheetHidden() == true) {
                                if (checkMap()) {
                                    showTracking()
                                }
                            }
                        }
                        else -> {
                            hideSearchSheet(fragment)
                            if (mTrackingUtils?.isTrackingSheetHidden() == true) {
                                showTrackingPreview(fragment)
                            }
                        }
                    }
                    showAmazonLogo()
                }
                R.id.menu_geofence -> {
                    val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                    if (fragment !is ExploreFragment) {
                        mNavController.navigate(R.id.explore_fragment)
                    }
                    mTrackingUtils?.hideTrackingBottomSheet()
                    mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
                    if (!mAuthStatus.isNullOrEmpty()) {
                        when (mAuthStatus) {
                            AuthEnum.DEFAULT.name -> {
                                hideSearchAndShowGeofence(fragment)
                            }
                            AuthEnum.AWS_CONNECTED.name -> {
                                if (reStartApp) {
                                    reStartApp = false
                                    mPreferenceManager.removeValue(KEY_RE_START_APP)
                                    mGeofenceBottomSheetHelper.signInConnectedBottomSheet(
                                        mSignInConnectInterface
                                    )
                                } else {
                                    mGeofenceBottomSheetHelper.signInRequiredBottomSheet(
                                        mSignInRequiredInterface
                                    )
                                }
                            }
                            AuthEnum.SIGNED_IN.name -> {
                                showGeofence()
                            }
                            else -> {
                                hideSearchAndShowGeofence(fragment)
                            }
                        }
                    } else {
                        hideSearchAndShowGeofence(fragment)
                    }
                    showAmazonLogo()
                }
                R.id.menu_settings -> {
                    mBottomSheetHelper.hideSearchBottomSheet(false)
                    mNavController.navigate(R.id.setting_fragment)
                    mGeofenceUtils?.hideAllGeofenceBottomSheet()
                    mTrackingUtils?.hideTrackingBottomSheet()
                    mSimulationUtils?.hideSimulationBottomSheet()
                    hideAmazonLogo()
                    exitScreen()
                    setSelectedScreen(AnalyticsAttributeValue.SETTINGS)
                }
                R.id.menu_more -> {
                    mBottomSheetHelper.hideSearchBottomSheet(false)
                    mNavController.navigate(R.id.about_fragment)
                    mGeofenceUtils?.hideAllGeofenceBottomSheet()
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
            mTrackingUtils?.showTrackingBottomSheet(TrackingEnum.ENABLE_TRACKING)
        }
    }

    private fun hideSearchAndShowGeofence(fragment: Fragment) {
        mBottomSheetHelper.hideSearchBottomSheet(true)
        lifecycleScope.launch {
            if (fragment !is ExploreFragment) {
                delay(DELAY_FOR_FRAGMENT_LOAD) // Need delay for showing bottomsheet after fragment load
            }
            mGeofenceUtils?.showGeofenceBeforeLogin()
        }
    }

    private fun hideSearchSheet(fragment: Fragment?) {
        mBottomSheetHelper.hideSearchBottomSheet(true)
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            mBottomSheetHelper.hideDirectionSearchBottomSheet(fragment as ExploreFragment)
        }
    }

    fun showGeofenceCloudFormation() {
        mGeofenceBottomSheetHelper.cloudFormationBottomSheet(
            TabEnum.TAB_GEOFENCE,
            mCloudFormationInterface
        )
    }

    fun openCloudFormation() {
        mGeofenceBottomSheetHelper.cloudFormationBottomSheet(
            TabEnum.TAB_TRACKING,
            mCloudFormationInterface
        )
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

    fun setExplorer() {
        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment !is ExploreFragment) {
            mNavController.navigate(R.id.explore_fragment)
        } else {
            fragment.showDirectionAndCurrentLocationIcon()
        }
        mBottomSheetHelper.hideSearchBottomSheet(false)
        if (!isTablet) {
            mBottomSheetHelper.hideMapStyleSheet()
        }
        mGeofenceUtils?.hideAllGeofenceBottomSheet()
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
            mSimulationUtils = SimulationUtils(mPreferenceManager, this@MainActivity, mAWSLocationHelper)
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
        if (!isTablet) {
            mBottomSheetHelper.hideMapStyleSheet()
        }
        showSimulationTop()
        if (!isSimulationPolicyAttached) {
            lifecycleScope.launch {
                setSimulationIotPolicy()
            }
        }
        mGeofenceUtils?.hideAllGeofenceBottomSheet()
        mTrackingUtils?.hideTrackingBottomSheet()
        mSimulationUtils?.showSimulationBottomSheet()
        if (mNavHostFragment.childFragmentManager.fragments.isNotEmpty()) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
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

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthClient.CUSTOM_TABS_ACTIVITY_CODE &&
            resultCode == RESULT_CANCELED
        ) {
            val propertiesAws = listOf(
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.EXPLORER)
            )
            analyticsHelper?.recordEvent(EventType.SIGN_IN_FAILED, propertiesAws)
            hideProgress()
        }
    }

    private fun showGeofence() {
        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment is ExploreFragment) {
            if (!isTablet) {
                fragment.hideDirectionAndCurrentLocationIcon()
            }
        }
        mBottomSheetHelper.hideSearchBottomSheet(true)
        lifecycleScope.launch {
            if (fragment !is ExploreFragment) {
                delay(DELAY_FOR_FRAGMENT_LOAD) // Need delay for showing bottomsheet after fragment load
            }
            mGeofenceUtils?.showGeofenceListBottomSheet(this@MainActivity)
        }
        exitScreen()
        setSelectedScreen(AnalyticsAttributeValue.GEOFENCES)
    }

    private fun showTracking() {
        val fragment =
            mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment is ExploreFragment) {
            fragment.showDirectionAndCurrentLocationIcon()
        }
        if (!isTablet) {
            mBottomSheetHelper.hideMapStyleSheet()
        }
        lifecycleScope.launch {
            if (fragment !is ExploreFragment) {
                delay(DELAY_FOR_FRAGMENT_LOAD) // Need delay for showing bottomsheet after fragment load
            }
            mBottomSheetHelper.hideSearchBottomSheet(true)
            showTrackingBottomSheet()
        }
        exitScreen()
        setSelectedScreen(AnalyticsAttributeValue.TRACKERS)
    }

    private fun checkMap(): Boolean {
        val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
        if (mapName == getString(R.string.map_esri)) {
            enableTrackingDialog()
            return false
        }
        return true
    }

    private fun showTrackingBottomSheet() {
        if (mPreferenceManager.getValue(
                IS_LOCATION_TRACKING_ENABLE,
                false
            )
        ) {
            mTrackingUtils?.showTrackingBottomSheet(TrackingEnum.TRACKING_HISTORY)
        } else {
            mTrackingUtils?.showTrackingBottomSheet(TrackingEnum.ENABLE_TRACKING)
        }
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

    private val mSignInConnectInterface = object :
        SignInConnectInterface {
        override fun signIn(dialog: Dialog?) {
            mBottomSheetDialog = dialog
            val propertiesAws = listOf(
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.EXPLORER)
            )
            analyticsHelper?.recordEvent(EventType.SIGN_IN_STARTED, propertiesAws)
            mSignInViewModel.signInWithAmazon(this@MainActivity)
        }

        override fun continueToExplore(dialog: Dialog?) {
            moveToExploreScreen()
            dialog?.dismiss()
        }
    }

    private val mSignInRequiredInterface = object : SignInRequiredInterface {
        override fun signInClick(dialog: Dialog?) {
            mBottomSheetDialog = dialog
            val propertiesAws = listOf(
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.EXPLORER)
            )
            analyticsHelper?.recordEvent(EventType.SIGN_IN_STARTED, propertiesAws)
            mSignInViewModel.signInWithAmazon(this@MainActivity)
        }

        override fun mayBeLaterClick(dialog: Dialog?) {
            moveToExploreScreen()
            dialog?.dismiss()
        }
    }

    fun moveToExploreScreen() {
        showAmazonLogo()
        mBinding.bottomNavigationMain.menu.findItem(R.id.menu_explore).isChecked = true
    }

    private val mCloudFormationInterface = object : CloudFormationInterface {
        override fun dialogDismiss(dialog: Dialog?) {
            dialog?.dismiss()
        }
    }

    override fun onBackPressed() {
        if (mNavController.currentDestination?.label == AWS_CLOUD_INFORMATION_FRAGMENT) {
            mNavController.popBackStack()
        } else if (mNavController.currentDestination?.label == VERSION_FRAGMENT) {
            mNavController.popBackStack()
        } else if (mNavController.currentDestination?.label == ABOUT_FRAGMENT) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
            if (fragment !is ExploreFragment) {
                mNavController.navigate(R.id.explore_fragment)
            }
            moveToExploreScreen()
        } else if (mNavController.currentDestination?.label == SETTING_FRAGMENT) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
            if (fragment !is ExploreFragment) {
                mNavController.navigate(R.id.explore_fragment)
            }
            moveToExploreScreen()
        } else if (mBottomSheetHelper.isAttributeExpandedOrHalfExpand()) {
            mBottomSheetHelper.hideAttributeSheet()
        } else if (mBottomSheetHelper.isSearchBottomSheetExpandedOrHalfExpand()) {
            mBottomSheetHelper.collapseSearchBottomSheet()
        } else if (mBottomSheetHelper.isMapStyleExpandedOrHalfExpand()) {
            mBottomSheetHelper.hideMapStyleSheet()
        } else if (mBottomSheetHelper.isNavigationBottomSheetHalfExpand()) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
            if (fragment is ExploreFragment) {
                fragment.navigationExit()
            }
        } else if (mBottomSheetHelper.isNavigationBottomSheetFullyExpand()) {
            mBottomSheetHelper.collapseNavigatingSheet()
        } else if (mBottomSheetHelper.isDirectionSearchExpandedOrHalfExpand()) {
            mBottomSheetHelper.collapseDirectionSearch()
        } else if (mGeofenceUtils?.isGeofenceListExpandedOrHalfExpand() == true) {
            mGeofenceUtils?.collapseGeofenceList()
        } else if (mGeofenceUtils?.isAddGeofenceExpandedOrHalfExpand() == true) {
            mGeofenceUtils?.collapseAddGeofence()
        } else if (mTrackingUtils?.isTrackingExpandedOrHalfExpand() == true) {
            mTrackingUtils?.collapseTracking()
        } else {
            super.onBackPressed()
        }
    }

    fun showProgress() {
        mBinding.apply {
            runOnUiThread {
                alertDialog = Dialog(this@MainActivity)
                alertDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                alertDialog?.setContentView(R.layout.dialog_progress)
                alertDialog?.setCancelable(false)
                alertDialog?.setCanceledOnTouchOutside(false)
                alertDialog?.window!!.setBackgroundDrawable(
                    ColorDrawable(
                        ContextCompat.getColor(this@MainActivity, android.R.color.transparent)
                    )
                )
                alertDialog?.show()
            }
        }
    }

    fun hideProgress() {
        runOnUiThread {
            if (alertDialog?.isShowing == true) {
                alertDialog?.hide()
            }
        }
    }

    private fun enableTrackingDialog() {
        enableTracker(
            object : EnableTrackerInterface {
                override fun continueToTracker(dialog: DialogInterface) {
                    showTracking()
                    mBinding.bottomNavigationMain.menu.findItem(R.id.menu_tracking).isChecked = true
                }

                override fun cancel() {
                    mBinding.bottomNavigationMain.menu.findItem(R.id.menu_explore).isChecked = true
                    setExplorer()
                }

                override fun viewTermsAndCondition(dialog: DialogInterface) {
                    resultLauncher.launch(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(BuildConfig.BASE_DOMAIN + BuildConfig.AWS_TERMS_URL)
                        )
                    )
                }
            }
        )
    }

    fun setWelcomeToExplorer() {
        mPreferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        isAppNotFirstOpened = true
        val fragment =
            mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment is ExploreFragment) {
            fragment.checkPermissionFromWelcome()
        }
    }

    fun isAppNotFirstOpened(): Boolean {
        return isAppNotFirstOpened
    }

    override fun notifyAppCrash(message: String?) {
        if (!isDestroyed) {
            message?.let {
                val properties = listOf(
                    Pair(AnalyticsAttribute.ERROR, it)
                )
                analyticsHelper?.recordEvent(EventType.APPLICATION_ERROR, properties)
            }
            restartApplication()
        }
    }
}
