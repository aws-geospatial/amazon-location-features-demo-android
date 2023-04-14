package com.aws.amazonlocation.utils

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetAttributionBinding
import com.aws.amazonlocation.databinding.BottomSheetDirectionSearchBinding
import com.aws.amazonlocation.databinding.BottomSheetMapStyleBinding
import com.aws.amazonlocation.databinding.BottomSheetNavigationBinding
import com.aws.amazonlocation.databinding.BottomSheetSearchBinding
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.explore.ExploreFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class BottomSheetHelper {

    private lateinit var mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mBottomSheetMapStyle: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mBottomSheetDirectionsSearch: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mBottomSheetDirections: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mNavigationBottomSheet: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mBottomSheetNavigationComplete: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mBottomSheetAttribution: BottomSheetBehavior<ConstraintLayout>
    private var exportFragment: ExploreFragment? = null
    var isSearchSheetOpen = false

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
        mBottomSheetSearchPlaces.expandedOffset =
            view.clSearchSheet.context.resources.getDimension(R.dimen.dp_50).toInt()

        mBottomSheetSearchPlaces.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            mBaseActivity?.bottomNavigationVisibility(true)
                            isSearchSheetOpen = false
                            activity?.hideKeyboard()
                            fragment.clearKeyboardFocus()
                            view.imgAmazonLogoSearchSheet.alpha = 1f
                            view.ivAmazonInfoSearchSheet.alpha = 1f
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            mBaseActivity?.bottomNavigationVisibility(false)
                            view.imgAmazonLogoSearchSheet.alpha = 0f
                            view.ivAmazonInfoSearchSheet.alpha = 0f
                            isSearchSheetOpen = true
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            mBaseActivity?.bottomNavigationVisibility(false)
                            view.imgAmazonLogoSearchSheet.alpha = 1f
                            view.ivAmazonInfoSearchSheet.alpha = 1f
                            isSearchSheetOpen = true
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {}
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
    fun isMapStyleExpandedOrHalfExpand(): Boolean {
        return mBottomSheetMapStyle.state == BottomSheetBehavior.STATE_HALF_EXPANDED || mBottomSheetMapStyle.state == BottomSheetBehavior.STATE_EXPANDED
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

    // set map style bottom sheet
    fun setMapStyleBottomSheet(view: BottomSheetMapStyleBinding) {
        mBottomSheetMapStyle =
            BottomSheetBehavior.from(view.clMapStyleBottomSheet)
        mBottomSheetMapStyle.isHideable = true
        mBottomSheetMapStyle.state = BottomSheetBehavior.STATE_HIDDEN
        mBottomSheetMapStyle.isFitToContents = false
        mBottomSheetMapStyle.expandedOffset =
            view.clMapStyleBottomSheet.context.resources.getDimension(R.dimen.dp_50).toInt()

        mBottomSheetMapStyle.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            view.imgAmazonLogoMapStyle.alpha = 1f
                            view.ivAmazonInfoMapStyle.alpha = 1f
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            view.imgAmazonLogoMapStyle.alpha = 0f
                            view.ivAmazonInfoMapStyle.alpha = 0f
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            view.imgAmazonLogoMapStyle.alpha = 1f
                            view.ivAmazonInfoMapStyle.alpha = 1f
                            if (isMapStyleExpandedOrHalfExpand() && isDirectionSearchSheetVisible()) {
                                collapseDirectionSearch()
                            }
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            if (exportFragment?.mBaseActivity?.mTrackingUtils?.isTrackingSheetCollapsed() != null) {
                                exportFragment?.mBaseActivity?.mTrackingUtils?.isTrackingSheetCollapsed()
                                    ?.let {
                                        if (!it) {
                                            if (exportFragment?.mBaseActivity?.mGeofenceUtils?.isGeofenceSheetCollapsed() != null) {
                                                exportFragment?.mBaseActivity?.mGeofenceUtils?.isGeofenceSheetCollapsed()
                                                    ?.let { it1 ->
                                                        if (!it1) {
                                                            hideSearchBottomSheet(false)
                                                        }
                                                    }
                                            }
                                        }
                                    }
                            } else {
                                hideSearchBottomSheet(false)
                            }
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
        mBottomSheetDirectionsSearch.expandedOffset =
            view.clDirectionSearchSheet.context.resources.getDimension(R.dimen.dp_50).toInt()

        mBottomSheetDirectionsSearch.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            mBaseActivity?.bottomNavigationVisibility(true)
                            view.imgAmazonLogoDirectionSearchSheet.alpha = 1f
                            view.ivAmazonInfoDirectionSearchSheet.alpha = 1f
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
                            view.imgAmazonLogoDirectionSearchSheet.alpha = 1f
                            view.ivAmazonInfoDirectionSearchSheet.alpha = 1f
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
    }

    fun halfExpandDirectionSearchBottomSheet() {
        mBottomSheetDirectionsSearch.halfExpandedRatio = 0.5f
        mBottomSheetDirectionsSearch.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun expandDirectionSheet() {
        mBottomSheetDirections.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun hideDirectionSheet() {
        mBottomSheetDirections.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun expandMapStyleSheet() {
        mBottomSheetMapStyle.halfExpandedRatio = 0.5f
        mBottomSheetMapStyle.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        hideSearchBottomSheet(true)
    }

    fun hideMapStyleSheet() {
        mBottomSheetMapStyle.state = BottomSheetBehavior.STATE_HIDDEN
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
            mBottomSheetSearchPlaces.peekHeight = 98.px
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
