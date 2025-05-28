package com.aws.amazonlocation.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.webView.WebViewActivity
import java.util.Locale
import java.util.regex.Pattern
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

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

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun getKeyboardHeight(activity: Activity, callback: (Int) -> Unit) {
    val rootView = activity.window.decorView.rootView
    rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        private var previousHeight = 0

        override fun onGlobalLayout() {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.height
            val visibleHeight = rect.height()
            val keyboardHeight = screenHeight - visibleHeight

            if (keyboardHeight > 0) {
                if (keyboardHeight != previousHeight) {
                    callback(keyboardHeight)
                    previousHeight = keyboardHeight
                }
            } else {
                previousHeight = 0
            }
        }
    })
}

@Suppress("DEPRECATION")
fun Activity.makeTransparentStatusBar() {
    if (Build.VERSION.SDK_INT in 21..29) {
        window.statusBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility =
            SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    } else if (Build.VERSION.SDK_INT >= 30) {
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Root ViewGroup of my activity
        val root = findViewById<ConstraintLayout>(R.id.cl_main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. Here the system is setting
            // only the bottom, left, and right dimensions, but apply whichever insets are
            // appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            view.layoutParams = (view.layoutParams as FrameLayout.LayoutParams).apply {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }
            // Return CONSUMED if you don't want want the window insets to keep being
            // passed down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
    }
}

@Suppress("Deprecation")
fun Activity.changeStatusBarColor(isLightStatusBar: Boolean, @ColorRes color: Int) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = isLightStatusBar
        window.statusBarColor = ContextCompat.getColor(this, color)
    } else {
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = isLightStatusBar
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, color))
        window.insetsController?.hide(android.view.WindowInsets.Type.statusBars())
        window.insetsController?.show(android.view.WindowInsets.Type.statusBars())
    }
}

@CheckResult
fun EditText.textChanges(): Flow<CharSequence?> {
    return callbackFlow {
        val listener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                trySend(s)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        }
        addTextChangedListener(listener)
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}

fun Activity.showKeyboard() {
    val imm: InputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) view = View(this)
    imm.showSoftInput(view, 0)
}

// hide the keyboard
fun Activity.hideKeyboard() {
    val imm: InputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) view = View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

@ExcludeFromJacocoGeneratedReport
fun changeTermsAndDescriptionFirstTextColor(termsOfUse: AppCompatTextView) {
    val context = termsOfUse.context
    val spannableString = SpannableString(
        termsOfUse.text.toString().replace(STRING_REPLACE_KEY, "")
    )
    if (termsOfUse.text.contains(STRING_REPLACE_KEY)) {
        val text = termsOfUse.text.split(STRING_REPLACE_KEY)[1]
        val condition =
            Pattern.compile(
                text.lowercase()
            )
        val clickHere = condition.matcher(
            termsOfUse.text.toString().replace(STRING_REPLACE_KEY, "").lowercase(Locale.ROOT)
        )
        while (clickHere.find()) {
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        context.startActivity(
                            Intent(
                                context,
                                WebViewActivity::class.java
                            ).putExtra(KEY_URL, BuildConfig.BASE_DOMAIN + BuildConfig.AWS_TERMS_URL)
                        )
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                    }
                },
                clickHere.start(),
                clickHere.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.color_primary_green
                    )
                ),
                clickHere.start(),
                clickHere.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                clickHere.start(),
                clickHere.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        termsOfUse.setText(
            spannableString,
            TextView.BufferType.SPANNABLE
        )
        termsOfUse.movementMethod = LinkMovementMethod.getInstance()
    }
}

fun changeTermsAndConditionColor(conditionPrivacy: AppCompatTextView) {
    val context = conditionPrivacy.context
    val spannableString = SpannableString(
        conditionPrivacy.text.toString().replace(STRING_REPLACE_KEY, "")
    )
    if (conditionPrivacy.text.contains(STRING_REPLACE_KEY)) {
        val text = conditionPrivacy.text.split(STRING_REPLACE_KEY)[1]
        val condition =
            Pattern.compile(
                text.lowercase()
            )
        val termsAndCondition =
            condition.matcher(
                conditionPrivacy.text.toString().replace(STRING_REPLACE_KEY, "")
                    .lowercase(Locale.ROOT)
            )
        while (termsAndCondition.find()) {
            spannableString.setSpan(
                ResourcesCompat.getFont(context, R.font.amazon_ember_bold)?.let {
                    FontSpan(
                        "",
                        it
                    )
                },
                termsAndCondition.start(),
                termsAndCondition.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        context.startActivity(
                            Intent(
                                context,
                                WebViewActivity::class.java
                            ).putExtra(KEY_URL, BuildConfig.BASE_DOMAIN + BuildConfig.AWS_TERMS_URL)
                        )
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                    }
                },
                termsAndCondition.start(),
                termsAndCondition.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.color_primary_green
                    )
                ),
                termsAndCondition.start(),
                termsAndCondition.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        conditionPrivacy.setText(
            spannableString,
            TextView.BufferType.SPANNABLE
        )
        conditionPrivacy.movementMethod = LinkMovementMethod.getInstance()
    }
}

fun Activity.restartApplication() {
    val intent = Intent(this, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    Runtime.getRuntime().exit(0)
}

fun Context.isInternetAvailable(): Boolean {
    var result = false
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    cm.run {
        cm.getNetworkCapabilities(cm.activeNetwork)?.run {
            result = when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
    }
    return result
}

fun hideKeyboard(activity: Activity, appCompatEditText: AppCompatEditText) {
    val imm: InputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(appCompatEditText.windowToken, 0)
}

fun copyTextToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}
