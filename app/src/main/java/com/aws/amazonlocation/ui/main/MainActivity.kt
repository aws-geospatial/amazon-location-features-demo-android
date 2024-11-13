package com.aws.amazonlocation.ui.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import aws.sdk.kotlin.services.cognitoidentity.model.Credentials
import aws.sdk.kotlin.services.iot.IotClient
import aws.sdk.kotlin.services.iot.model.AttachPolicyRequest
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
import com.aws.amazonlocation.ui.main.simulation.SimulationUtils
import com.aws.amazonlocation.ui.main.welcome.WelcomeBottomSheetFragment
import com.aws.amazonlocation.utils.ABOUT_FRAGMENT
import com.aws.amazonlocation.utils.AWS_CLOUD_INFORMATION_FRAGMENT
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.ConnectivityObserveInterface
import com.aws.amazonlocation.utils.DELAY_LANGUAGE_3000
import com.aws.amazonlocation.utils.Durations.DELAY_FOR_FRAGMENT_LOAD
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.IOT_POLICY
import com.aws.amazonlocation.utils.IOT_POLICY_UN_AUTH
import com.aws.amazonlocation.utils.IS_APP_FIRST_TIME_OPENED
import com.aws.amazonlocation.utils.IS_LOCATION_TRACKING_ENABLE
import com.aws.amazonlocation.utils.KEY_ACCESS_KEY
import com.aws.amazonlocation.utils.KEY_ACCESS_TOKEN
import com.aws.amazonlocation.utils.KEY_AUTH_EXPIRES_IN
import com.aws.amazonlocation.utils.KEY_AUTH_FETCH_TIME
import com.aws.amazonlocation.utils.KEY_AVOID_FERRIES
import com.aws.amazonlocation.utils.KEY_AVOID_TOLLS
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_CODE
import com.aws.amazonlocation.utils.KEY_EXPIRATION
import com.aws.amazonlocation.utils.KEY_ID_TOKEN
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_REFRESH_TOKEN
import com.aws.amazonlocation.utils.KEY_RE_START_APP
import com.aws.amazonlocation.utils.KEY_SECRET_KEY
import com.aws.amazonlocation.utils.KEY_SESSION_TOKEN
import com.aws.amazonlocation.utils.KEY_USER_DOMAIN
import com.aws.amazonlocation.utils.KEY_USER_POOL_CLIENT_ID
import com.aws.amazonlocation.utils.KEY_USER_REGION
import com.aws.amazonlocation.utils.KeyBoardUtils
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.NetworkConnectivityObserveInterface
import com.aws.amazonlocation.utils.SETTING_FRAGMENT
import com.aws.amazonlocation.utils.SIGN_IN
import com.aws.amazonlocation.utils.SIGN_OUT
import com.aws.amazonlocation.utils.Units.checkInternetConnection
import com.aws.amazonlocation.utils.VERSION_FRAGMENT
import com.aws.amazonlocation.utils.analytics.AnalyticsUtils
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.invisible
import com.aws.amazonlocation.utils.makeTransparentStatusBar
import com.aws.amazonlocation.utils.setLocale
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
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
    private var reStartApp: Boolean = false
    private var isSimulationPolicyAttached: Boolean = false
    private lateinit var mNavHostFragment: NavHostFragment
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private var mAuthStatus: String? = null
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
        if (mBinding.signInWebView.visibility == View.VISIBLE) {
            hideViews(mBinding.signInWebView, mBinding.ivBackMain, mBinding.viewBottom, mBinding.appCompatTextView)
            showViews(mBinding.bottomNavigationMain, mBinding.navHostFragment, mBinding.imgAmazonLogo, mBinding.ivAmazonInfo)
        }
        isSessionStarted = false
        hideProgress()
    }

    private fun checkSession() {
        if (!mLocationProvider.checkClientInitialize()) {
            val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
            if (mAuthStatus == AuthEnum.SIGNED_IN.name) {
                if (mLocationProvider.isAuthTokenExpired()) {
                    refreshToken()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        async { initMobileClient() }.await()
                        getTokenAndAttachPolicy()
                    }
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    async { initMobileClient() }.await()
                }
            }
        } else {
            mLocationProvider.checkSessionValid(this)
        }
    }

    private fun handleAuthorizationCode(
        method: String,
        authCode: String?,
    ) {
        if (method == SIGN_IN) {
            if (authCode != null) {
                mSignInViewModel.fetchTokensWithOkHttp(authCode)
            } else {
                showSignInFailed()
            }
        } else if (method == SIGN_OUT) {
            val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
            if (mAuthStatus == AuthEnum.SIGNED_IN.name) {
                clearUserInFo()
                mPreferenceManager.removeValue(KEY_ACCESS_TOKEN)
                mPreferenceManager.removeValue(KEY_ID_TOKEN)
                mPreferenceManager.removeValue(KEY_REFRESH_TOKEN)
                mPreferenceManager.removeValue(KEY_AUTH_EXPIRES_IN)
                mPreferenceManager.removeValue(KEY_AUTH_FETCH_TIME)
                mPreferenceManager.removeValue(KEY_ACCESS_KEY)
                mPreferenceManager.removeValue(KEY_SECRET_KEY)
                mPreferenceManager.removeValue(KEY_SESSION_TOKEN)
                mPreferenceManager.removeValue(KEY_EXPIRATION)
                mPreferenceManager.setValue(
                    KEY_CLOUD_FORMATION_STATUS,
                    AuthEnum.AWS_CONNECTED.name,
                )
                when (val fragment = mNavHostFragment.childFragmentManager.fragments[0]) {
                    is ExploreFragment -> {
                        fragment.refreshAfterSignOut()
                        val mapStyleNameDisplay =
                            mPreferenceManager.getValue(
                                KEY_MAP_STYLE_NAME,
                                getString(R.string.map_standard),
                            )
                                ?: getString(R.string.map_standard)
                        changeMapStyle(mapStyleNameDisplay)
                    }

                    is AWSCloudInformationFragment -> {
                        fragment.refreshAfterSignOut()
                    }

                    is SettingFragment -> {
                        fragment.refreshAfterSignOut()
                    }
                }
                CoroutineScope(Dispatchers.IO).launch {
                    async { initMobileClient() }.await()
                }
            } else {
                val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                val propertiesAws =
                    listOf(
                        Pair(
                            AnalyticsAttribute.TRIGGERED_BY,
                            if (fragment is ExploreFragment) AnalyticsAttributeValue.EXPLORER else AnalyticsAttributeValue.SETTINGS,
                        )
                    )
                analyticsUtils?.recordEvent(EventType.SIGN_OUT_FAILED, propertiesAws)
            }
        }
    }

    private fun showSignInFailed() {
        setBottomBar()
        hideProgress()
        showError("Sign in failed")
        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
        val propertiesAws =
            listOf(
                Pair(
                    AnalyticsAttribute.TRIGGERED_BY,
                    if (fragment is ExploreFragment) AnalyticsAttributeValue.EXPLORER else AnalyticsAttributeValue.SETTINGS,
                )
            )
        analyticsUtils?.recordEvent(EventType.SIGN_IN_FAILED, propertiesAws)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isSessionStarted = true
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN,
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
            },
        )
        lifecycleScope.launch {
            delay(DELAY_LANGUAGE_3000)
            val languageCode = getLanguageCode()
            languageCode?.let { setLocale(it, applicationContext) }
        }
        setSignInOutWebView()
        addBackPressCallBack()
    }

    private fun addBackPressCallBack() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                    if (mBinding.signInWebView.visibility == View.VISIBLE) {
                        hideViews(mBinding.signInWebView, mBinding.ivBackMain, mBinding.viewBottom, mBinding.appCompatTextView)
                        showViews(mBinding.bottomNavigationMain, mBinding.navHostFragment, mBinding.imgAmazonLogo, mBinding.ivAmazonInfo)
                        mBottomSheetDialog?.show()
                    } else if (mNavController.currentDestination?.label == AWS_CLOUD_INFORMATION_FRAGMENT) {
                        mNavController.popBackStack()
                    } else if (mNavController.currentDestination?.label == VERSION_FRAGMENT) {
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
                        mBottomSheetHelper.hideAttributeSheet()
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
                    } else if (mGeofenceUtils?.isGeofenceListExpandedOrHalfExpand() == true) {
                        mGeofenceUtils?.collapseGeofenceList()
                    } else if (mGeofenceUtils?.isAddGeofenceExpandedOrHalfExpand() == true) {
                        mGeofenceUtils?.collapseAddGeofence()
                    } else if (mTrackingUtils?.isTrackingExpandedOrHalfExpand() == true) {
                        mTrackingUtils?.collapseTracking()
                    } else {
                        if (isEnabled) {
                            isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            },
        )
    }

    private fun initObserver() {
        lifecycleScope.launch {
            mSignInViewModel.fetchTokenResponse.collect { handleResult ->
                handleResult
                    .onLoading {
                        showProgress()
                    }.onSuccess {
                        showProgress()
                        mIsUserLoggedIn = true
                        if (mPreferenceManager.getValue(
                                KEY_CLOUD_FORMATION_STATUS,
                                "",
                            ) != AuthEnum.SIGNED_IN.name
                        ) {
                            mLocationProvider.clearCredentials()
                        }
                        mPreferenceManager.setValue(
                            KEY_CLOUD_FORMATION_STATUS,
                            AuthEnum.SIGNED_IN.name,
                        )
                        mBottomSheetDialog?.dismiss()
                        async { mLocationProvider.generateNewAuthCredentials() }.await()
                        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                        getTokenAndAttachPolicy()
                        val propertiesAws =
                            listOf(
                                Pair(
                                    AnalyticsAttribute.TRIGGERED_BY,
                                    if (fragment is ExploreFragment) AnalyticsAttributeValue.EXPLORER else AnalyticsAttributeValue.SETTINGS,
                                ),
                            )
                        analyticsUtils?.recordEvent(EventType.SIGN_IN_SUCCESSFUL, propertiesAws)
                    }.onError { it ->
                        setBottomBar()
                        hideProgress()
                        it.messageResource?.let {
                            showError(it.toString())
                            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                            val propertiesAws =
                                listOf(
                                    Pair(
                                        AnalyticsAttribute.TRIGGERED_BY,
                                        if (fragment is ExploreFragment) AnalyticsAttributeValue.EXPLORER else AnalyticsAttributeValue.SETTINGS,
                                    ),
                                )
                            analyticsUtils?.recordEvent(EventType.SIGN_IN_FAILED, propertiesAws)
                        }
                    }
            }
        }
    }

    fun changeMapStyle(
        mapStyleNameDisplay: String,
    ) {
        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment is ExploreFragment) {
            fragment.mapStyleChange(
                mapStyleNameDisplay,
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setSignInOutWebView() {
        mBinding.signInWebView.settings.javaScriptEnabled = true
        mBinding.signInWebView.settings.allowFileAccess = false
        WebView.setWebContentsDebuggingEnabled(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBinding.signInWebView.settings.safeBrowsingEnabled = true
        }
        mBinding.signInWebView.webViewClient =
            object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?,
                ): Boolean {
                    when (request?.url?.host) {
                        SIGN_OUT -> {
                            handleAuthorizationCode(SIGN_OUT, null)
                            hideViews(mBinding.signInWebView, mBinding.ivBackMain, mBinding.viewBottom, mBinding.appCompatTextView)
                            showViews(mBinding.bottomNavigationMain, mBinding.navHostFragment, mBinding.imgAmazonLogo, mBinding.ivAmazonInfo)
                        }
                        SIGN_IN -> {
                            val authorizationCode = request.url?.getQueryParameter(KEY_CODE)
                            handleAuthorizationCode(SIGN_IN, authorizationCode)
                            hideViews(mBinding.signInWebView, mBinding.ivBackMain, mBinding.viewBottom, mBinding.appCompatTextView)
                            showViews(mBinding.bottomNavigationMain, mBinding.navHostFragment, mBinding.imgAmazonLogo, mBinding.ivAmazonInfo)
                        }
                        else -> {
                            mBinding.signInWebView.loadUrl(request?.url.toString())
                        }
                    }
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    hideProgress()
                }
            }
    }

    fun openSignIn() {
        showProgress()
        mBottomSheetDialog?.hide()
        showViews(mBinding.signInWebView, mBinding.ivBackMain, mBinding.viewBottom)
        hideViews(mBinding.bottomNavigationMain, mBinding.navHostFragment, mBinding.imgAmazonLogo, mBinding.ivAmazonInfo)
        mBinding.appCompatTextView.invisible()
        val mUserDomain = mPreferenceManager.getValue(KEY_USER_DOMAIN, "")
        val mUserPoolClientId = mPreferenceManager.getValue(KEY_USER_POOL_CLIENT_ID, "")
        val redirectUri = "${getString(R.string.AMAZON_LOCATION_SCHEMA)}://signin/"

        val signInUrl =
            "https://$mUserDomain/login?client_id=$mUserPoolClientId&response_type=code&identity_provider=COGNITO&redirect_uri=$redirectUri"

        mBinding.signInWebView.loadUrl(signInUrl)
    }

    fun openSignOut() {
        showProgress()
        showViews(mBinding.signInWebView, mBinding.ivBackMain, mBinding.viewBottom)
        hideViews(mBinding.bottomNavigationMain, mBinding.navHostFragment, mBinding.imgAmazonLogo, mBinding.ivAmazonInfo)
        mBinding.appCompatTextView.invisible()
        val mUserDomain = mPreferenceManager.getValue(KEY_USER_DOMAIN, "")
        val mUserPoolClientId = mPreferenceManager.getValue(KEY_USER_POOL_CLIENT_ID, "")
        val redirectUri = "${getString(R.string.AMAZON_LOCATION_SCHEMA)}://signout/"
        val signOutUrl =
            "https://$mUserDomain/logout?client_id=$mUserPoolClientId&logout_uri=$redirectUri"

        mBinding.signInWebView.loadUrl(signOutUrl)
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
                        ConstraintSet.END,
                    )
                    constraintSet.connect(
                        R.id.img_amazon_logo,
                        ConstraintSet.END,
                        R.id.bottom_navigation_main,
                        ConstraintSet.START,
                    )
                    constraintSet.connect(
                        R.id.iv_amazon_info,
                        ConstraintSet.END,
                        R.id.img_amazon_logo,
                        ConstraintSet.START,
                    )

                    constraintSet.applyTo(clMain)
                }
            }
        }
    }

    fun setSelectedScreen(screen: String) {
        currentPage = screen
        val properties = listOf(Pair(AnalyticsAttribute.SCREEN_NAME, screen),)
        analyticsUtils?.recordEvent(EventType.SCREEN_OPEN, properties)
    }

    fun exitScreen() {
        currentPage?.let {
            val properties = listOf(Pair(AnalyticsAttribute.SCREEN_NAME, it),)
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
                hideViews(mBinding.signInWebView, mBinding.ivBackMain, mBinding.viewBottom, mBinding.appCompatTextView)
                showViews(mBinding.bottomNavigationMain, mBinding.navHostFragment, mBinding.imgAmazonLogo, mBinding.ivAmazonInfo)
                mBottomSheetDialog?.show()
            }
        }
    }

    fun geofenceClick() {
        mBinding.bottomNavigationMain.selectedItemId = R.id.menu_geofence
    }

    fun getBottomNavHeight(): Int = mBinding.bottomNavigationMain.height

    fun getTokenAndAttachPolicy() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val identityId: String? = mLocationProvider.getIdentityId()
                val attachPolicyRequest =
                    AttachPolicyRequest {
                        policyName = IOT_POLICY
                        target = identityId
                    }
                var mRegion = mPreferenceManager.getValue(KEY_USER_REGION, "")

                if (mRegion.isNullOrEmpty()) {
                    mRegion = BuildConfig.DEFAULT_REGION
                }
                val iotClient =
                    IotClient {
                        region = mRegion
                        credentialsProvider =
                            createCredentialsProviderForPolicy(
                                mLocationProvider.getCredentials(),
                            )
                    }

                try {
                    iotClient.attachPolicy(attachPolicyRequest)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                if (fragment is AWSCloudInformationFragment) {
                    runOnUiThread {
                        fragment.refresh()
                    }
                }
                if (isTablet) {
                    if (fragment is SettingFragment) {
                        runOnUiThread {
                            fragment.refreshAfterSignIn()
                        }
                    }
                }
                hideProgressAndShowData()
            }
        } catch (e: Exception) {
            showError("Unable to attach policy")
            hideProgressAndShowData()
        }
    }

    fun refreshSettings() {
        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment is SettingFragment) {
            runOnUiThread {
                fragment.refreshAfterConnection()
            }
        }
    }

    private fun setSimulationIotPolicy() {
        val identityId = mLocationProvider.getIdentityId()
        CoroutineScope(Dispatchers.IO).launch {
            val attachPolicyRequest =
                AttachPolicyRequest {
                    policyName = IOT_POLICY_UN_AUTH
                    target = identityId
                }

            val iotClient =
                IotClient {
                    region = identityId?.split(":")?.get(0)
                    credentialsProvider =
                        createCredentialsProviderForPolicy(
                            mLocationProvider.getCredentials(),
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
        credentials: Credentials?,
    ): aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider {
        if (credentials?.accessKeyId == null || credentials.sessionToken == null || credentials.secretKey == null) {
            throw Exception(
                "Credentials not found",
            )
        }
        return aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider(
            aws.smithy.kotlin.runtime.auth.awscredentials.Credentials.invoke(
                accessKeyId = credentials.accessKeyId!!,
                secretAccessKey = credentials.secretKey!!,
                sessionToken = credentials.sessionToken,
            ),
        )
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
        }
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
                    mAuthStatus =
                        mPreferenceManager.getValue(
                            KEY_CLOUD_FORMATION_STATUS,
                            AuthEnum.DEFAULT.name,
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
                                    mSignInConnectInterface,
                                )
                            } else {
                                mGeofenceBottomSheetHelper.signInRequiredBottomSheet(
                                    mSignInRequiredInterface,
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
                                        mSignInConnectInterface,
                                    )
                                } else {
                                    mGeofenceBottomSheetHelper.signInRequiredBottomSheet(
                                        mSignInRequiredInterface,
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
        lifecycleScope.launch {
            if (fragment !is ExploreFragment) {
                delay(DELAY_FOR_FRAGMENT_LOAD) // Need delay for showing bottomsheet after fragment load
            }
            mBottomSheetHelper.hideSearchBottomSheet(true)
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
            mCloudFormationInterface,
        )
    }

    fun openCloudFormation() {
        mGeofenceBottomSheetHelper.cloudFormationBottomSheet(
            TabEnum.TAB_TRACKING,
            mCloudFormationInterface,
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
        if (!isTablet && fragment is ExploreFragment) {
            fragment.hideMapStyleSheet()
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

    private fun showGeofence() {
        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment is ExploreFragment) {
            if (!isTablet) {
                fragment.hideDirectionAndCurrentLocationIcon()
            }
        }
        lifecycleScope.launch {
            if (fragment !is ExploreFragment) {
                delay(DELAY_FOR_FRAGMENT_LOAD) // Need delay for showing bottomsheet after fragment load
            }
            mBottomSheetHelper.hideSearchBottomSheet(true)
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
            if (!isTablet) {
                fragment.hideMapStyleSheet()
            }
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
        showTracking()
        mBinding.bottomNavigationMain.menu
            .findItem(R.id.menu_tracking)
            .isChecked = true
        return false
    }

    private fun showTrackingBottomSheet() {
        if (mPreferenceManager.getValue(
                IS_LOCATION_TRACKING_ENABLE,
                false,
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

    private val mSignInConnectInterface =
        object :
            SignInConnectInterface {
            override fun signIn(dialog: Dialog?) {
                mBottomSheetDialog = dialog
                val propertiesAws = listOf(Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.EXPLORER),)
                analyticsUtils?.recordEvent(EventType.SIGN_IN_STARTED, propertiesAws)
                openSignIn()
            }

            override fun continueToExplore(dialog: Dialog?) {
                moveToExploreScreen()
                dialog?.dismiss()
            }
        }

    private val mSignInRequiredInterface =
        object : SignInRequiredInterface {
            override fun signInClick(dialog: Dialog?) {
                mBottomSheetDialog = dialog
                val propertiesAws = listOf(Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.EXPLORER),)
                analyticsUtils?.recordEvent(EventType.SIGN_IN_STARTED, propertiesAws)
                openSignIn()
            }

            override fun mayBeLaterClick(dialog: Dialog?) {
                moveToExploreScreen()
                dialog?.dismiss()
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

    private val mCloudFormationInterface =
        object : CloudFormationInterface {
            override fun dialogDismiss(dialog: Dialog?) {
                dialog?.dismiss()
            }
        }

    private fun showProgress() {
        if (alertDialog != null && alertDialog?.isShowing == true) {
            return
        }
        mBinding.apply {
            runOnUiThread {
                alertDialog = Dialog(this@MainActivity)
                alertDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                alertDialog?.setContentView(R.layout.dialog_progress)
                alertDialog?.setCancelable(false)
                alertDialog?.setCanceledOnTouchOutside(false)
                alertDialog?.window!!.setBackgroundDrawable(
                    ColorDrawable(
                        ContextCompat.getColor(this@MainActivity, android.R.color.transparent),
                    ),
                )
                alertDialog?.show()
            }
        }
    }

    private fun hideProgress() {
        runOnUiThread {
            alertDialog?.hide()
        }
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

    fun isAppNotFirstOpened(): Boolean = isAppNotFirstOpened

    override fun notifyAppCrash(message: String?) {
        if (!isDestroyed) {
            message?.let {
                val properties = listOf(Pair(AnalyticsAttribute.ERROR, it))
                analyticsUtils?.recordEvent(EventType.APPLICATION_ERROR, properties)
            }
        }
    }

    fun showSignInRequiredSheet() {
        reStartApp = true
        if (mGeofenceUtils?.isGeofenceListExpandedOrHalfExpand() == true) {
            mGeofenceUtils?.hideAllGeofenceBottomSheet()
            mBinding.bottomNavigationMain.selectedItemId = R.id.menu_geofence
        } else if (mTrackingUtils?.isTrackingExpandedOrHalfExpand() == true) {
            mTrackingUtils?.hideTrackingBottomSheet()
            mBinding.bottomNavigationMain.selectedItemId = R.id.menu_tracking
        }
    }

    fun initClient(isAfterSignOut:Boolean = false){
        if (!isAfterSignOut) {
            try {
                mLocationProvider.clearCredentials()
            } catch (_: Exception) { }
        }
        CoroutineScope(Dispatchers.IO).launch {
            async { initMobileClient() }.await()
        }
    }
}