package com.aws.amazonlocation.utils

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetAttributionBinding
import com.aws.amazonlocation.databinding.BottomSheetDirectionSearchBinding
import com.aws.amazonlocation.databinding.BottomSheetNavigationBinding
import com.aws.amazonlocation.databinding.BottomSheetSearchBinding
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.explore.ExploreFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class BottomSheetHelper {

    private lateinit var mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mBottomSheetDirectionsSearch: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mBottomSheetDirections: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mNavigationBottomSheet: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mBottomSheetNavigationComplete: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mBottomSheetAttribution: BottomSheetBehavior<ConstraintLayout>
    private var exportFragment: ExploreFragment? = null
    var isSearchSheetOpen = false
    var context: Context? = null

    // set search bottom sheet
    fun setSearchBottomSheet(
        activity: FragmentActivity?,
        view: BottomSheetSearchBinding,
        mBaseActivity: BaseActivity?,
        fragment: ExploreFragment
    ) {
        this.exportFragment = fragment
        mBottomSheetSearchPlaces =
            BottomSheetBehavior.from(view.clSearchSheet)
        mBottomSheetSearchPlaces.isFitToContents = false
        context = view.clSearchSheet.context
        if (!(activity as MainActivity).isTablet) {
            mBottomSheetSearchPlaces.expandedOffset =
                view.clSearchSheet.context.resources.getDimension(R.dimen.dp_10).toInt()
        }
        view.edtSearchPlaces.clearFocus()
        activity.hideKeyboard()

        mBottomSheetSearchPlaces.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        mBaseActivity?.bottomNavigationVisibility(true)
                        isSearchSheetOpen = false
                        activity.hideKeyboard()
                        fragment.clearKeyboardFocus()
                        view.imgAmazonLogoSearchSheet.alpha = 1f
                        view.ivAmazonInfoSearchSheet.alpha = 1f
                        view.tvSearchCancel?.hide()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        mBaseActivity?.bottomNavigationVisibility(false)
                        view.imgAmazonLogoSearchSheet.alpha = 0f
                        view.ivAmazonInfoSearchSheet.alpha = 0f
                        isSearchSheetOpen = true
                        view.tvSearchCancel?.show()
                        view.edtSearchPlaces.requestFocus()
                        activity.showKeyboard()
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        mBaseActivity?.bottomNavigationVisibility(false)
                        view.imgAmazonLogoSearchSheet.alpha = 1f
                        view.ivAmazonInfoSearchSheet.alpha = 1f
                        view.tvSearchCancel?.show()
                        isSearchSheetOpen = true
                        activity.hideKeyboard()
                        fragment.clearKeyboardFocus()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        view.tvSearchCancel?.hide()
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    fun isSearchBottomSheetExpandedOrHalfExpand(): Boolean {
        return mBottomSheetSearchPlaces.state == BottomSheetBehavior.STATE_HALF_EXPANDED || mBottomSheetSearchPlaces.state == BottomSheetBehavior.STATE_EXPANDED
    }

    fun isAttributeExpandedOrHalfExpand(): Boolean {
        return mBottomSheetAttribution.state == BottomSheetBehavior.STATE_HALF_EXPANDED || mBottomSheetAttribution.state == BottomSheetBehavior.STATE_EXPANDED
    }

    fun isSearchBottomSheetHalfExpand(): Boolean {
        return mBottomSheetSearchPlaces.state == BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun isDirectionSearchExpandedOrHalfExpand(): Boolean {
        return mBottomSheetDirectionsSearch.state == BottomSheetBehavior.STATE_HALF_EXPANDED || mBottomSheetDirectionsSearch.state == BottomSheetBehavior.STATE_EXPANDED
    }

    fun isNavigationBottomSheetHalfExpand(): Boolean {
        return mNavigationBottomSheet.state == BottomSheetBehavior.STATE_HALF_EXPANDED || mNavigationBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED
    }

    fun isNavigationBottomSheetFullyExpand(): Boolean {
        return mNavigationBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED
    }

    // set direction bottom sheet
    fun setDirectionBottomSheet(view: ConstraintLayout) {
        mBottomSheetDirections =
            BottomSheetBehavior.from(view)
        mBottomSheetDirections.state = BottomSheetBehavior.STATE_HIDDEN
        mBottomSheetDirections.isDraggable = false
    }

    fun directionSheetDraggable(isDraggable: Boolean) {
        if (isDirectionSearchSheetVisible()) {
            mBottomSheetDirectionsSearch.isDraggable = isDraggable
        }
    }

    // set direction bottom sheet
    fun setDirectionSearchBottomSheet(
        view: BottomSheetDirectionSearchBinding,
        exploreFragment: ExploreFragment,
        mBaseActivity: BaseActivity?
    ) {
        mBottomSheetDirectionsSearch =
            BottomSheetBehavior.from(view.clDirectionSearchSheet)
        mBottomSheetDirectionsSearch.isHideable = true
        mBottomSheetDirectionsSearch.state = BottomSheetBehavior.STATE_HIDDEN
        mBottomSheetDirectionsSearch.isFitToContents = false
        mBaseActivity?.isTablet?.let {
            if (!it) {
                mBottomSheetDirectionsSearch.expandedOffset =
                    view.clDirectionSearchSheet.context.resources.getDimension(R.dimen.dp_15).toInt()
            }
        }
        mBottomSheetDirectionsSearch.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        mBaseActivity?.bottomNavigationVisibility(false)
                        setBottomSheetDirectionSearchData(view, mBaseActivity)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        mBaseActivity?.bottomNavigationVisibility(false)
                        view.imgAmazonLogoDirectionSearchSheet.alpha = 0f
                        view.ivAmazonInfoDirectionSearchSheet.alpha = 0f
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        mBaseActivity?.bottomNavigationVisibility(false)
                        exploreFragment.changeDirectionCardMargin(175.px)
                        mBottomSheetDirectionsSearch.isHideable = false
                        setBottomSheetDirectionSearchData(view, mBaseActivity)
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    private fun setBottomSheetDirectionSearchData(
        view: BottomSheetDirectionSearchBinding,
        mBaseActivity: BaseActivity?
    ) {
        view.imgAmazonLogoDirectionSearchSheet.alpha = 1f
        view.ivAmazonInfoDirectionSearchSheet.alpha = 1f
        mBaseActivity?.hideKeyboard()
        if (!view.rvSearchPlacesDirection.isVisible && !view.rvSearchPlacesSuggestionDirection.isVisible) {
            view.edtSearchDirection.clearFocus()
            view.edtSearchDest.clearFocus()
        }
    }

    fun setNavigationBottomSheet(view: BottomSheetNavigationBinding) {
        mNavigationBottomSheet =
            BottomSheetBehavior.from(view.clNavigationParent)
        mNavigationBottomSheet.isHideable = true
        mNavigationBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        mNavigationBottomSheet.isFitToContents = false
        mNavigationBottomSheet.expandedOffset =
            view.clNavigationParent.context.resources.getDimension(R.dimen.dp_50).toInt()

        mNavigationBottomSheet.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        view.cardNavigationLocation.alpha = 1f
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        view.cardNavigationLocation.alpha = 0f
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        view.cardNavigationLocation.alpha = 1f
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    // set direction bottom sheet
    fun setNavigationCompleteBottomSheet(view: ConstraintLayout) {
        mBottomSheetNavigationComplete =
            BottomSheetBehavior.from(view)
        mBottomSheetNavigationComplete.state = BottomSheetBehavior.STATE_HIDDEN
        mBottomSheetNavigationComplete.isDraggable = false
    }

    fun showNavigationSheet() {
        mNavigationBottomSheet.isHideable = false
        mNavigationBottomSheet.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun hideNavigationSheet() {
        mNavigationBottomSheet.isHideable = true
        mNavigationBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun hideDirectionSearchBottomSheet(exploreFragment: ExploreFragment) {
        exploreFragment.changeDirectionCardMargin(92.px)
        mBottomSheetDirectionsSearch.isHideable = true
        mBottomSheetDirectionsSearch.state = BottomSheetBehavior.STATE_HIDDEN
        exploreFragment.hideDirectionBottomSheet()
    }

    fun collapseDirectionSearch() {
        mBottomSheetDirectionsSearch.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun hideDirectionSearch(exploreFragment: ExploreFragment) {
        exploreFragment.changeDirectionCardMargin(92.px)
        mBottomSheetDirectionsSearch.isHideable = true
        mBottomSheetDirectionsSearch.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun collapseNavigatingSheet() {
        mNavigationBottomSheet.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun isDirectionSearchSheetVisible(): Boolean {
        return mBottomSheetDirectionsSearch.state != BottomSheetBehavior.STATE_HIDDEN
    }

    fun expandDirectionSearchSheet(exploreFragment: ExploreFragment) {
        exploreFragment.changeDirectionCardMargin(175.px)
        mBottomSheetDirectionsSearch.isHideable = false
        mBottomSheetDirectionsSearch.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomSheetDirectionsSearch.isDraggable = true
    }

    fun halfExpandDirectionSearchBottomSheet() {
        mBottomSheetDirectionsSearch.halfExpandedRatio = 0.5f
        mBottomSheetDirectionsSearch.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        mBottomSheetDirectionsSearch.isDraggable = true
    }

    fun expandDirectionSheet() {
        mBottomSheetDirections.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hideDirectionSheet() {
        mBottomSheetDirections.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun expandAttributeSheet() {
        mBottomSheetAttribution.isHideable = true
        mBottomSheetAttribution.isFitToContents = false
        mBottomSheetAttribution.halfExpandedRatio = 0.5f
        mBottomSheetAttribution.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hideAttributeSheet() {
        mBottomSheetAttribution.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun expandSearchBottomSheet() {
        mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun halfExpandBottomSheet() {
        mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun collapseSearchBottomSheet() {
        exportFragment?.setUserProfile()
        mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun isSearchPlaceSheetVisible() =
        mBottomSheetSearchPlaces.state == BottomSheetBehavior.STATE_HIDDEN

    fun isDirectionSheetVisible() =
        mBottomSheetDirections.state != BottomSheetBehavior.STATE_HIDDEN

    fun isNavigationSheetVisible() =
        mNavigationBottomSheet.state != BottomSheetBehavior.STATE_HIDDEN

    fun hideSearchBottomSheet(isHide: Boolean) {
        if (!isHide) {
            exportFragment?.setUserProfile()
            mBottomSheetSearchPlaces.isHideable = false
            if (context != null) {
                context?.let {
                    val isTablet = it.resources.getBoolean(R.bool.is_tablet)
                    if (isTablet) {
                        mBottomSheetSearchPlaces.peekHeight = it.resources.getDimensionPixelSize(R.dimen.dp_150)
                    } else {
                        mBottomSheetSearchPlaces.peekHeight = it.resources.getDimensionPixelSize(R.dimen.dp_98)
                    }
                }
            } else {
                context?.let {
                    mBottomSheetSearchPlaces.peekHeight = it.resources.getDimensionPixelSize(R.dimen.dp_98)
                }
            }
            mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            mBottomSheetSearchPlaces.isHideable = true
            mBottomSheetSearchPlaces.peekHeight = 0.px
            mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    fun expandNavigationCompleteSheet() {
        mBottomSheetNavigationComplete.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun hideNavigationCompleteSheet() {
        mBottomSheetNavigationComplete.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun setAttributeBottomSheet(view: BottomSheetAttributionBinding) {
        mBottomSheetAttribution =
            BottomSheetBehavior.from(view.clMain)
        mBottomSheetAttribution.isHideable = true
        mBottomSheetAttribution.isDraggable = false
        mBottomSheetAttribution.state = BottomSheetBehavior.STATE_HIDDEN
        mBottomSheetAttribution.isFitToContents = false
    }
}