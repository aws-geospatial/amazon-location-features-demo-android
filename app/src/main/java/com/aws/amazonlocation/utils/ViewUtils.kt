package com.aws.amazonlocation.utils

import android.view.View

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun showViews(vararg view: View?) {
    view.forEach {
        it?.show()
    }
}

fun hideViews(vararg view: View?) {
    view.forEach {
        it?.hide()
    }
}
