package com.aws.amazonlocation.ui.main.welcome

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetWelcomeBinding
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.changeTermsAndDescriptionFirstTextColor
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class WelcomeBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var mBinding: BottomSheetWelcomeBinding
    private lateinit var dialog: BottomSheetDialog

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val height = resources.getDimensionPixelSize(R.dimen.welcome_screen_height)
        mBinding.clWelcome?.layoutParams?.height = height
        mBinding.clWelcome?.requestLayout()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.CustomBottomSheetDialogTheme
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = BottomSheetDialog(requireContext(), theme)
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
                if (activity != null) {
                    if (!(activity as MainActivity).isTablet) {
                        behaviour.expandedOffset = requireContext().resources.getDimension(R.dimen.dp_50).toInt()
                    }
                } else {
                    activity?.let{ fragmentActivity ->
                        behaviour.expandedOffset = fragmentActivity.resources.getDimension(R.dimen.dp_50).toInt()
                    }
                }
                dialog.setCanceledOnTouchOutside(false)
                setupFullHeight(layout)
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
        mBinding = BottomSheetWelcomeBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        clickListener()
    }

    private fun clickListener() {
        mBinding.apply {
            btnContinueToApp.setOnClickListener {
                (activity as MainActivity).checkPropertiesData()
                dialog.dismiss()
            }
        }
    }

    private fun init() {
        mBinding.apply {
            changeTermsAndDescriptionFirstTextColor(tvTermsDesc)
        }
    }
}
