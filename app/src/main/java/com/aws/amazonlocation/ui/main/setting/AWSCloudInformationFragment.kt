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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.data.enum.TabEnum
import com.aws.amazonlocation.databinding.FragmentAwsCloudInformationBinding
import com.aws.amazonlocation.domain.`interface`.CloudFormationInterface
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.signin.CustomSpinnerAdapter
import com.aws.amazonlocation.ui.main.web_view.WebViewActivity
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.DisconnectAWSInterface
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.IS_LOCATION_TRACKING_ENABLE
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.KEY_RE_START_APP
import com.aws.amazonlocation.utils.KEY_RE_START_APP_WITH_AWS_DISCONNECT
import com.aws.amazonlocation.utils.KEY_TAB_ENUM
import com.aws.amazonlocation.utils.KEY_URL
import com.aws.amazonlocation.utils.KEY_USER_DOMAIN
import com.aws.amazonlocation.utils.KEY_USER_POOL_CLIENT_ID
import com.aws.amazonlocation.utils.KEY_USER_POOL_ID
import com.aws.amazonlocation.utils.KEY_USER_REGION
import com.aws.amazonlocation.utils.SignOutInterface
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.WEB_SOCKET_URL
import com.aws.amazonlocation.utils.changeClickHereColor
import com.aws.amazonlocation.utils.changeLearnMoreColor
import com.aws.amazonlocation.utils.changeTermsAndConditionColor
import com.aws.amazonlocation.utils.disconnectFromAWSDialog
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.regionMapList
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.signOutDialog
import com.aws.amazonlocation.utils.validateIdentityPoolId
import com.aws.amazonlocation.utils.validateUserPoolClientId
import com.aws.amazonlocation.utils.validateUserPoolId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class AWSCloudInformationFragment :
    BaseFragment(),
    SignOutInterface {
    private var isDisconnectFromAWSRequired: Boolean = false
    lateinit var mBinding: FragmentAwsCloudInformationBinding
    private var mAuthStatus: String? = null
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
        savedInstanceState: Bundle?,
    ): View {
        mBinding = FragmentAwsCloudInformationBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        init()
        clickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        val propertiesAws = listOf(
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsUtils?.recordEvent(EventType.AWS_ACCOUNT_CONNECTION_STOPPED, propertiesAws)
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

                    else -> {
                        showAwsConnect()
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
                            WebViewActivity::class.java,
                        ).putExtra(KEY_URL, url),
                    )
                }
            },
        )
        changeTermsAndConditionColor(mBinding.tvTermsCondition)
        setSpinnerData()
        isDisconnectFromAWSRequired = false
    }

    fun refreshAfterSignOut() {
        mBaseActivity?.clearUserInFo()
        if (isDisconnectFromAWSRequired) {
            mPreferenceManager.setDefaultConfig()
        }
        val propertiesAws =
            listOf(
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS),
            )
        (activity as MainActivity).analyticsUtils?.recordEvent(
            EventType.SIGN_OUT_SUCCESSFUL,
            propertiesAws,
        )
        init()
        showError(getString(R.string.sign_out_successfully))
    }

    private fun setSpinnerData() {
        mBinding.apply {
            val regionNameList = arrayListOf<String>()
            for (data in regionMapList.keys) {
                regionNameList.add(data)
            }
            val adapter = CustomSpinnerAdapter(requireContext(), regionNameList)
            spinnerRegion.adapter = adapter

            spinnerRegion.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        adapter.setSelection(position)
                        selectedRegion = regionMapList[parent.getItemAtPosition(position)]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Do something when nothing is selected
                    }
                }
            spinnerRegion.setSelection(2)
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
                if (!mAuthStatus.isNullOrEmpty()) {
                    when (mAuthStatus) {
                        AuthEnum.AWS_CONNECTED.name -> {
                            requireContext().disconnectFromAWSDialog(
                                disConnectFromAWS,
                                false
                            )
                        }

                        AuthEnum.SIGNED_IN.name -> {
                            requireContext().disconnectFromAWSDialog(
                                disConnectFromAWS,
                                true
                            )
                        }
                    }
                }
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
                mIdentityPoolId =
                    edtIdentityPoolId.text
                        .toString()
                        .trim()
                        .lowercase()
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
                (activity as MainActivity).analyticsUtils?.recordEvent(EventType.SIGN_IN_STARTED, propertiesAws)
                (activity as MainActivity).openSignIn()
            }

            btnLogout.setOnClickListener {
                requireContext().signOutDialog(this@AWSCloudInformationFragment)
            }
        }
    }

    private val disConnectFromAWS =
        object : DisconnectAWSInterface {
            override fun disconnectAWS(dialog: DialogInterface) {
                lifecycleScope.launch {
                    mPreferenceManager.setValue(KEY_RE_START_APP, true)
                    mPreferenceManager.setValue(KEY_RE_START_APP_WITH_AWS_DISCONNECT, true)
                    mPreferenceManager.setDefaultConfig()
                }
                (activity as MainActivity).refreshSettings()
                init()
                dialog.dismiss()
            }

            override fun logoutAndDisconnectAWS(dialog: DialogInterface) {
                this@AWSCloudInformationFragment.logout(dialog, true)
            }
        }

    private fun validateAWSAccountData() {
        CoroutineScope(Dispatchers.IO).launch {
            if (!validateIdentityPoolId(mIdentityPoolId, regionData)) {
                (activity as MainActivity).showError(getString(R.string.label_enter_identity_pool_id))
                awsConnectionFailed()
            } else if (mUserDomain.isNullOrEmpty()) {
                (activity as MainActivity).showError(getString(R.string.label_enter_domain))
                awsConnectionFailed()
            } else if (!validateUserPoolClientId(mUserPoolClientId)) {
                (activity as MainActivity).showError(getString(R.string.label_enter_user_pool_client_id))
                awsConnectionFailed()
            } else if (!validateUserPoolId(mUserPoolId)) {
                (activity as MainActivity).showError(getString(R.string.label_enter_user_pool_id))
                awsConnectionFailed()
            } else if (mWebSocketUrl.isNullOrEmpty()) {
                (activity as MainActivity).showError(getString(R.string.label_enter_web_socket_url))
                awsConnectionFailed()
            } else {
                storeDataAndRestartApp()
            }
        }
    }

    private fun awsConnectionFailed() {
        val propertiesAws = listOf(
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsUtils?.recordEvent(EventType.AWS_ACCOUNT_CONNECTION_FAILED, propertiesAws)
    }

    private fun storeDataAndRestartApp() {
        val propertiesAws = listOf(
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsUtils?.recordEvent(EventType.AWS_ACCOUNT_CONNECTION_SUCCESSFUL, propertiesAws)
        mPreferenceManager.setValue(IS_LOCATION_TRACKING_ENABLE, true)
        mPreferenceManager.setValue(
            KEY_CLOUD_FORMATION_STATUS,
            AuthEnum.AWS_CONNECTED.name,
        )

        mPreferenceManager.setValue(
            KEY_RE_START_APP,
            true,
        )

        mIdentityPoolId?.let { identityPId ->
            mPreferenceManager.setValue(
                KEY_POOL_ID,
                identityPId,
            )

            identityPId.split(":").let { splitStringList ->
                splitStringList[0].let { region ->
                    mPreferenceManager.setValue(
                        KEY_USER_REGION,
                        region,
                    )
                }
            }
        }
        mUserDomain?.let { uDomain ->
            val domainToSave = Units.sanitizeUrl(uDomain)
            mPreferenceManager.setValue(
                KEY_USER_DOMAIN,
                domainToSave,
            )
        }

        mUserPoolClientId?.let { uPoolClientId ->
            mPreferenceManager.setValue(
                KEY_USER_POOL_CLIENT_ID,
                uPoolClientId,
            )
        }
        mUserPoolId?.let { uPoolId ->
            mPreferenceManager.setValue(
                KEY_USER_POOL_ID,
                uPoolId,
            )
        }
        mWebSocketUrl?.let { webSocketUrl ->
            val webSocketUrlToSave = Units.sanitizeUrl(webSocketUrl)
            mPreferenceManager.setValue(
                WEB_SOCKET_URL,
                webSocketUrlToSave,
            )
        }
        mPreferenceManager.setValue(KEY_TAB_ENUM, TabEnum.TAB_EXPLORE.name)
        (activity as MainActivity).initClient()
        if ((activity as MainActivity).isTablet){
            (activity as MainActivity).refreshSettings()
        }
        (activity as MainActivity).runOnUiThread {
            init()
        }
    }

    private fun cloudFormationValidation() {
        mBinding.apply {
            if (edtIdentityPoolId.text
                    .toString()
                    .isNotEmpty() &&
                edtUserDomain.text
                    .toString()
                    .isNotEmpty() &&
                edtUserPoolClientId.text
                    .toString()
                    .isNotEmpty() &&
                edtUserPoolId.text
                    .toString()
                    .isNotEmpty() &&
                edtWebSocketUrl.text.toString().isNotEmpty()
            ) {
                btnConnect.alpha = 1f
                btnConnect.isEnabled = true
            } else {
                btnConnect.alpha = 0.4f
                btnConnect.isEnabled = false
            }
        }
    }

    private var clickHere =
        object : CloudFormationInterface {
            override fun clickHere(url: String) {
                val urlToPass = String.format(url, selectedRegion, selectedRegion)
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(urlToPass),
                    ),
                )
            }
        }

    fun refresh() {
        init()
    }

    fun hideKeyBoard() {
        mBinding.edtIdentityPoolId.clearFocus()
        mBinding.edtUserDomain.clearFocus()
        mBinding.edtUserPoolId.clearFocus()
        mBinding.edtWebSocketUrl.clearFocus()
        mBinding.edtUserPoolClientId.clearFocus()
    }

    override fun logout(
        dialog: DialogInterface,
        isDisconnectFromAWSRequired: Boolean,
    ) {
        this.isDisconnectFromAWSRequired = isDisconnectFromAWSRequired
        (activity as MainActivity).openSignOut()
    }
}
