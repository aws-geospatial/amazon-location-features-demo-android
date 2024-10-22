package com.aws.amazonlocation.ui.main.terms_condition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.databinding.FragmentTermsAndConditionBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.utils.changeTermsAndConditionColor

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class TermsAndConditionFragment : BaseFragment() {

    private lateinit var mBinding: FragmentTermsAndConditionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentTermsAndConditionBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        backPress()
    }

    private fun init() {
        mBinding.apply {
            changeTermsAndConditionColor(tvTermsDesc)
            ivBack?.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
