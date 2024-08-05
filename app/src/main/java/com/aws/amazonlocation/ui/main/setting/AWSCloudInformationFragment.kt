package com.aws.amazonlocation.ui.main.setting

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.navigation.fragment.findNavController
import com.amazonaws.mobile.client.AWSMobileClient
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.onError
import com.aws.amazonlocation.data.common.onLoading
import com.aws.amazonlocation.data.common.onSuccess
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.data.enum.TabEnum
import com.aws.amazonlocation.databinding.FragmentAwsCloudInformationBinding
import com.aws.amazonlocation.domain.`interface`.CloudFormationInterface
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.signin.CustomSpinnerAdapter
import com.aws.amazonlocation.ui.main.signin.SignInViewModel
import com.aws.amazonlocation.ui.main.web_view.WebViewActivity
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.DisconnectAWSInterface
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.IS_LOCATION_TRACKING_ENABLE
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_ID_TOKEN
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.KEY_PROVIDER
import com.aws.amazonlocation.utils.KEY_RE_START_APP
import com.aws.amazonlocation.utils.KEY_RE_START_APP_WITH_AWS_DISCONNECT
import com.aws.amazonlocation.utils.KEY_TAB_ENUM
import com.aws.amazonlocation.utils.KEY_URL
import com.aws.amazonlocation.utils.KEY_USER_DOMAIN
import com.aws.amazonlocation.utils.KEY_USER_POOL_CLIENT_ID
import com.aws.amazonlocation.utils.KEY_USER_POOL_ID
import com.aws.amazonlocation.utils.KEY_USER_REGION
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.SE_REGION_LIST
import com.aws.amazonlocation.utils.SignOutInterface
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.WEB_SOCKET_URL
import com.aws.amazonlocation.utils.changeClickHereColor
import com.aws.amazonlocation.utils.changeLearnMoreColor
import com.aws.amazonlocation.utils.changeTermsAndConditionColor
import com.aws.amazonlocation.utils.disconnectFromAWSDialog
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.isGrabMapSelected
import com.aws.amazonlocation.utils.isRunningTest
import com.aws.amazonlocation.utils.regionMapList
import com.aws.amazonlocation.utils.restartApplication
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.signOutDialog
import com.aws.amazonlocation.utils.validateIdentityPoolId
import com.aws.amazonlocation.utils.validateUserPoolClientId
import com.aws.amazonlocation.utils.validateUserPoolId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class AWSCloudInformationFragment : BaseFragment(), SignOutInterface {
    lateinit var mBinding: FragmentAwsCloudInformationBinding
    private var mAuthStatus: String? = null
    private val mSignInViewModel: SignInViewModel by viewModels()
    private var mIdentityPoolId: String? = null

    private var mUserDomain: String? = null
    private var mUserPoolClientId: String? = null
    private var mUserPoolId: String? = null
    private var mWebSocketUrl: String? = null
    private var regionData: String? = null
    private var selectedRegion = regionMapList[regionMapList.keys.first()]

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAwsCloudInformationBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initObserver()
        clickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        val propertiesAws = listOf(
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsHelper?.recordEvent(EventType.AWS_ACCOUNT_CONNECTION_STOPPED, propertiesAws)
    }
    private fun init() {
        mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        mBinding.apply {
            if (!mAuthStatus.isNullOrEmpty()) {
                when (mAuthStatus) {
                    AuthEnum.AWS_CONNECTED.name -> {
                        showAwsDisconnected()
                    }

                    AuthEnum.SIGNED_IN.name -> {
                        showAwsDisconnected()
                        btnSignIn.hide()
                        btnLogout.show()
                    }
                }
            } else {
                showAwsConnect()
            }
        }
        changeClickHereColor(mBinding.tvTitle11, clickHere)
        changeLearnMoreColor(
            mBinding.tvTvTitle22,
            object : CloudFormationInterface {
                override fun clickHere(url: String) {
                    startActivity(
                        Intent(
                            context,
                            WebViewActivity::class.java
                        ).putExtra(KEY_URL, url)
                    )
                }
            }
        )
        changeTermsAndConditionColor(mBinding.tvTermsCondition)
        setSpinnerData()
    }

    private fun initObserver() {
        lifecycleScope.launch {
            withStarted { }
            mSignInViewModel.mSignInResponse.collect { handleResult ->
                handleResult.onLoading {
                }.onSuccess {
                    mBaseActivity?.mIsUserLoggedIn = true
                    showError(it)
                    mPreferenceManager.setValue(
                        KEY_CLOUD_FORMATION_STATUS,
                        AuthEnum.SIGNED_IN.name
                    )
                    (activity as MainActivity).getTokenAndAttachPolicy(it)
                    init()
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
                    )
                    (activity as MainActivity).analyticsHelper?.recordEvent(EventType.SIGN_IN_SUCCESSFUL, propertiesAws)
                }.onError { it ->
                    (activity as MainActivity).hideProgress()
                    it.messageResource?.let {
                        showError(it.toString())
                    }
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
                    )
                    (activity as MainActivity).analyticsHelper?.recordEvent(EventType.SIGN_IN_FAILED, propertiesAws)
                }
            }
        }

        lifecycleScope.launch {
            withStarted { }
            mSignInViewModel.mSignOutResponse.collect { handleResult ->
                handleResult.onLoading {
                }.onSuccess {
                    mBaseActivity?.clearUserInFo()
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
                    )
                    (activity as MainActivity).analyticsHelper?.recordEvent(EventType.SIGN_OUT_SUCCESSFUL, propertiesAws)
                    if (it.isDisconnectFromAWSRequired) {
                        mPreferenceManager.setValue(KEY_RE_START_APP, true)
                        mPreferenceManager.setValue(KEY_RE_START_APP_WITH_AWS_DISCONNECT, true)
                        mPreferenceManager.setDefaultConfig()
                    } else {
                        mBaseActivity?.mPreferenceManager?.setValue(
                            KEY_CLOUD_FORMATION_STATUS,
                            AuthEnum.AWS_CONNECTED.name
                        )
                        init()
                    }
                    mPreferenceManager.removeValue(KEY_ID_TOKEN)
                    mPreferenceManager.removeValue(KEY_PROVIDER)
                    showError(it.message.toString())
                    if (!isRunningTest) {
                        delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                        activity?.restartApplication()
                    }
                }.onError { it ->
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
                    )
                    (activity as MainActivity).analyticsHelper?.recordEvent(EventType.SIGN_OUT_FAILED, propertiesAws)
                    it.messageResource?.let {
                        showError(it.toString())
                    }
                }
            }
        }
    }

    private fun setSpinnerData() {
        mBinding.apply {
            val regionNameList = arrayListOf<String>()
            for (data in regionMapList.keys) {
                regionNameList.add(data)
            }
            val adapter = CustomSpinnerAdapter(requireContext(), regionNameList)
            spinnerRegion.adapter = adapter

            spinnerRegion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    adapter.setSelection(position)
                    selectedRegion = regionMapList[parent.getItemAtPosition(position)]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do something when nothing is selected
                }
            }
            if (isGrabMapSelected(mPreferenceManager, requireContext())) {
                spinnerRegion.setSelection(3)
            } else {
                spinnerRegion.setSelection(2)
            }
        }
    }

    private fun FragmentAwsCloudInformationBinding.showAwsDisconnected() {
        hideViews(groupAwsCloud, btnConnect, btnLogout)
        showViews(btnDisconnect, btnSignIn)
        tvAwsCloudFormationRequired.text = getString(R.string.aws_cloud_formation)
        tvSignInRequired.text = getString(R.string.label_connected)
        tvSignInDescription.text = getString(R.string.label_connected_description)
        tvHowToConnect.text = getString(R.string.label_how_to_remove)
        tvTitle11.text = getString(R.string.label_connected_title_1)
        tvTitle12.text = getString(R.string.label_connected_title_2)
        tvTitle21.text = getString(R.string.label_connected_title_3)
        tvTvTitle22.text = getString(R.string.label_connected_title_4)
    }

    private fun FragmentAwsCloudInformationBinding.showAwsConnect() {
        tvAwsCloudFormationRequired.text = getString(R.string.connect_to_aws_account)
        tvSignInRequired.text = getString(R.string.aws_cloudformation_required)
        tvTitle11.text = getString(R.string.how_to_connect_1_1)
        tvTitle12.text = getString(R.string.how_to_connect_1_2)
        tvTitle21.text = getString(R.string.how_to_connect_2_1)
        tvTvTitle22.text = getString(R.string.how_to_connect_2_2)
        showViews(groupAwsCloud, btnConnect, tvTitle12)
        hideViews(btnDisconnect, btnSignIn, btnLogout)
    }

    private fun clickListener() {
        mBinding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            btnDisconnect.setOnClickListener {
                requireContext().disconnectFromAWSDialog(
                    disConnectFromAWS,
                    AWSMobileClient.getInstance().isSignedIn
                )
            }
            edtIdentityPoolId.doOnTextChanged { _, _, _, _ ->
                cloudFormationValidation()
            }
            edtUserDomain.doOnTextChanged { _, _, _, _ ->
                cloudFormationValidation()
            }
            edtUserPoolClientId.doOnTextChanged { _, _, _, _ ->
                cloudFormationValidation()
            }
            edtUserPoolId.doOnTextChanged { _, _, _, _ ->
                cloudFormationValidation()
            }
            edtWebSocketUrl.doOnTextChanged { _, _, _, _ ->
                cloudFormationValidation()
            }
            btnConnect.setOnClickListener {
                mIdentityPoolId = edtIdentityPoolId.text.toString().trim().lowercase()
                mUserDomain = edtUserDomain.text.toString().trim()
                mUserPoolClientId = edtUserPoolClientId.text.toString().trim()
                mUserPoolId = edtUserPoolId.text.toString().trim()
                mWebSocketUrl = edtWebSocketUrl.text.toString().trim()

                mIdentityPoolId?.let { identityPId ->
                    identityPId.split(":").let { splitStringList ->
                        splitStringList[0].let { region ->
                            regionData = region
                        }
                    }
                }
                regionData?.let { _ -> validateAWSAccountData() }
            }

            btnSignIn.setOnClickListener {
                val propertiesAws = listOf(
                    Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
                )
                (activity as MainActivity).analyticsHelper?.recordEvent(EventType.SIGN_IN_STARTED, propertiesAws)
                mSignInViewModel.signInWithAmazon(requireActivity())
            }

            btnLogout.setOnClickListener {
                requireContext().signOutDialog(this@AWSCloudInformationFragment)
            }
        }
    }

    private val disConnectFromAWS = object : DisconnectAWSInterface {
        override fun disconnectAWS(dialog: DialogInterface) {
            lifecycleScope.launch {
                mPreferenceManager.setValue(KEY_RE_START_APP, true)
                mPreferenceManager.setValue(KEY_RE_START_APP_WITH_AWS_DISCONNECT, true)
                mPreferenceManager.setDefaultConfig()
                if (!isRunningTest) {
                    delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                    activity?.restartApplication()
                }
            }
            init()
            dialog.dismiss()
        }

        override fun logoutAndDisconnectAWS(dialog: DialogInterface) {
            this@AWSCloudInformationFragment.logout(dialog, true)
        }
    }

    private fun validateAWSAccountData() {
        if (!validateIdentityPoolId(mIdentityPoolId, regionData)) {
            (activity as MainActivity).showError(getString(R.string.label_enter_identity_pool_id))
        } else if (mUserDomain.isNullOrEmpty()) {
            (activity as MainActivity).showError(getString(R.string.label_enter_domain))
        } else if (!validateUserPoolClientId(mUserPoolClientId)) {
            (activity as MainActivity).showError(getString(R.string.label_enter_user_pool_client_id))
        } else if (!validateUserPoolId(mUserPoolId)) {
            (activity as MainActivity).showError(getString(R.string.label_enter_user_pool_id))
        } else if (mWebSocketUrl.isNullOrEmpty()) {
            (activity as MainActivity).showError(getString(R.string.label_enter_web_socket_url))
        } else {
            storeDataAndRestartApp()
        }
    }

    private fun storeDataAndRestartApp() {
        val propertiesAws = listOf(
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsHelper?.recordEvent(EventType.AWS_ACCOUNT_CONNECTION_SUCCESSFUL, propertiesAws)
        mPreferenceManager.setValue(IS_LOCATION_TRACKING_ENABLE, true)
        mPreferenceManager.setValue(
            KEY_CLOUD_FORMATION_STATUS,
            AuthEnum.AWS_CONNECTED.name
        )

        mPreferenceManager.setValue(
            KEY_RE_START_APP,
            true
        )

        mIdentityPoolId?.let { identityPId ->
            mPreferenceManager.setValue(
                KEY_POOL_ID,
                identityPId
            )

            identityPId.split(":").let { splitStringList ->
                splitStringList[0].let { region ->
                    mPreferenceManager.setValue(
                        KEY_USER_REGION,
                        region
                    )
                }
            }
        }
        mUserDomain?.let { uDomain ->
            val domainToSave = Units.sanitizeUrl(uDomain)
            mPreferenceManager.setValue(
                KEY_USER_DOMAIN,
                domainToSave
            )
        }

        mUserPoolClientId?.let { uPoolClientId ->
            mPreferenceManager.setValue(
                KEY_USER_POOL_CLIENT_ID,
                uPoolClientId
            )
        }
        mUserPoolId?.let { uPoolId ->
            mPreferenceManager.setValue(
                KEY_USER_POOL_ID,
                uPoolId
            )
        }
        mWebSocketUrl?.let { webSocketUrl ->
            val webSocketUrlToSave = Units.sanitizeUrl(webSocketUrl)
            mPreferenceManager.setValue(
                WEB_SOCKET_URL,
                webSocketUrlToSave
            )
        }
        mPreferenceManager.setValue(KEY_TAB_ENUM, TabEnum.TAB_EXPLORE.name)
        if (isGrabMapSelected(mPreferenceManager, requireContext())) {
            if (!SE_REGION_LIST.contains(regionData)) {
                mPreferenceManager.setValue(
                    KEY_MAP_STYLE_NAME,
                    resources.getString(R.string.map_light)
                )
                mPreferenceManager.setValue(KEY_MAP_NAME, resources.getString(R.string.esri))
            }
        }
        if (!isRunningTest) {
            lifecycleScope.launch {
                delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                activity?.restartApplication()
            }
        }
    }

    private fun cloudFormationValidation() {
        mBinding.apply {
            if (edtIdentityPoolId.text.toString()
                .isNotEmpty() && edtUserDomain.text.toString()
                    .isNotEmpty() && edtUserPoolClientId.text.toString()
                    .isNotEmpty() && edtUserPoolId.text.toString()
                    .isNotEmpty() && edtWebSocketUrl.text.toString().isNotEmpty()
            ) {
                btnConnect.alpha = 1f
                btnConnect.isEnabled = true
            } else {
                btnConnect.alpha = 0.4f
                btnConnect.isEnabled = false
            }
        }
    }

    private var clickHere = object : CloudFormationInterface {
        override fun clickHere(url: String) {
            val urlToPass = String.format(url, selectedRegion, selectedRegion)
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(urlToPass)
                )
            )
        }
    }

    fun hideKeyBoard() {
        mBinding.edtIdentityPoolId.clearFocus()
        mBinding.edtUserDomain.clearFocus()
        mBinding.edtUserPoolId.clearFocus()
        mBinding.edtWebSocketUrl.clearFocus()
        mBinding.edtUserPoolClientId.clearFocus()
    }
    override fun logout(dialog: DialogInterface, isDisconnectFromAWSRequired: Boolean) {
        mSignInViewModel.signOutWithAmazon(requireContext(), isDisconnectFromAWSRequired)
    }
}
