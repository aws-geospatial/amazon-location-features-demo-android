package com.aws.amazonlocation.ui.main.signin

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetSigninRequiredBinding
import com.aws.amazonlocation.domain.`interface`.SignInRequiredInterface
import com.aws.amazonlocation.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
class SignInRequiredBottomSheetFragment(private var mSignInRequiredInterface: SignInRequiredInterface) : BottomSheetDialogFragment() {

    private lateinit var mBinding: BottomSheetSigninRequiredBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.CustomBottomSheetDialogTheme
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        if ((activity as MainActivity).isTablet) {
            dialog.setOnShowListener {
                val bottomSheetDialog = it as BottomSheetDialog
                val parentLayout =
                    bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                parentLayout?.let { layout ->
                    val behaviour = BottomSheetBehavior.from(layout)
                    behaviour.isDraggable = false
                    dialog.setCancelable(false)
                    behaviour.isHideable = false
                    behaviour.isFitToContents = false
                    dialog.setCanceledOnTouchOutside(false)
                    setupFullHeight(layout)
                }
            }
        }
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = BottomSheetSigninRequiredBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mSignInRequiredInterface.mayBeLaterClick(dialog)
            }
            true
        }
        clickListener()
    }

    private fun clickListener() {
        mBinding.apply {
            btnSignIn.setOnClickListener {
                mSignInRequiredInterface.signInClick(dialog)
            }

            tvMayBeLater.setOnClickListener {
                mSignInRequiredInterface.mayBeLaterClick(dialog)
            }

            ivSignInRequiredClose?.setOnClickListener {
                mSignInRequiredInterface.mayBeLaterClick(dialog)
            }
        }
    }
}
