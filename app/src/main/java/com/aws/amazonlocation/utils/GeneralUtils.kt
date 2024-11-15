package com.aws.amazonlocation.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
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
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import aws.sdk.kotlin.services.cognitoidentity.CognitoIdentityClient
import aws.sdk.kotlin.services.cognitoidentity.model.GetIdRequest
import aws.sdk.kotlin.services.cognitoidentity.model.ResourceNotFoundException
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.LoginResponse
import com.aws.amazonlocation.domain.`interface`.CloudFormationInterface
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.web_view.WebViewActivity
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import org.maplibre.android.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val isRunningTest: Boolean by lazy {
    try {
        val testProperty = System.getProperty("robolectric") != null ||
                System.getProperty("android.injected.invoked.from.ide") == "true"
        testProperty
    } catch (e: ClassNotFoundException) {
        false
    }
}

val isRunningTestLiveLocation: Boolean by lazy {
    try {
        Class.forName("com.aws.amazonlocation.ui.main.ExploreFragmentLiveNavigationTest")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}
val isRunningTest2LiveLocation: Boolean by lazy {
    try {
        Class.forName("com.aws.amazonlocation.ui.main.CheckGoButtonClickLiveNavigationTest")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}
val isRunningTest3LiveLocation: Boolean by lazy {
    try {
        Class.forName("com.aws.amazonlocation.ui.main.ExploreFragmentMapFunctionWithoutAwsLoginTest")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}

val isRunningRemoteDataSourceImplTest: Boolean by lazy {
    try {
        Class.forName("com.aws.amazonlocation.data.datasource.RemoteDataSourceImplTest")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}

@Suppress("DEPRECATION")
@ExcludeFromJacocoGeneratedReport
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

@ExcludeFromJacocoGeneratedReport
fun changeConditionPrivacyColor(conditionPrivacy: AppCompatTextView) {
    val mContext = conditionPrivacy.context
    val mSpannableString = SpannableString(conditionPrivacy.text.toString())
    val mCondition =
        Pattern.compile(
            mContext.resources.getString(R.string.condition_of_use).lowercase(Locale.ROOT)
        )
    val mPrivacy =
        Pattern.compile(
            mContext.resources.getString(R.string.privacy_notice).lowercase(Locale.ROOT)
        )
    val mConditionOfUse =
        mCondition.matcher(conditionPrivacy.text.toString().lowercase(Locale.ROOT))
    val mPrivacyNotice = mPrivacy.matcher(conditionPrivacy.text.toString().lowercase(Locale.ROOT))
    while (mConditionOfUse.find()) {
        mSpannableString.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    mContext,
                    R.color.color_primary_green
                )
            ),
            mConditionOfUse.start(),
            mConditionOfUse.end(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    while (mPrivacyNotice.find()) {
        mSpannableString.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    mContext,
                    R.color.color_primary_green
                )
            ),
            mPrivacyNotice.start(),
            mPrivacyNotice.end(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    conditionPrivacy.setText(
        mSpannableString,
        TextView.BufferType.SPANNABLE
    )
    conditionPrivacy.movementMethod = LinkMovementMethod.getInstance()
}

@ExcludeFromJacocoGeneratedReport
@ExperimentalCoroutinesApi
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

@ExcludeFromJacocoGeneratedReport
fun Activity.showKeyboard() {
    val imm: InputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) view = View(this)
    imm.showSoftInput(view, 0)
}

// hide the keyboard
@ExcludeFromJacocoGeneratedReport
fun Activity.hideKeyboard() {
    val imm: InputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) view = View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun validateLatLng(searchText: String): LatLng? {
    val pattern = Pattern.compile(LAT_LNG_REGEX_PATTERN)
    return if (pattern.matcher(searchText).matches()) {
        val latLng = searchText.split(",").toTypedArray()
        LatLng(latLng[0].trim().toDouble(), latLng[1].trim().toDouble())
    } else {
        null
    }
}

fun getUserName(mLoginResponse: LoginResponse?): String? {
    return if (mLoginResponse != null) {
        val nameArray = mLoginResponse.name?.split(" ")?.toTypedArray()
        if (nameArray != null) {
            if (nameArray.size >= 2) {
                val firstName = nameArray[0].first().uppercase()
                val lastName = nameArray[1].first().uppercase()
                "$firstName$lastName"
            } else {
                nameArray[0].first().uppercase()
            }
        } else {
            null
        }
    } else {
        null
    }
}

fun getRegion(region: String?, subRegion: String?, country: String?): String {
    var mRegion = ""
    mRegion += if (!region.isNullOrEmpty()) {
        "$region, $country"
    } else if (!subRegion.isNullOrEmpty()) {
        "$subRegion, $country"
    } else if (!country.isNullOrEmpty()) {
        country
    } else {
        ""
    }
    return mRegion
}

@ExcludeFromJacocoGeneratedReport
fun changeTermsAndDescriptionFirstTextColor(termsOfUse: AppCompatTextView) {
    val context = termsOfUse.context
    val spannableString = SpannableString(termsOfUse.text.toString().replace(STRING_REPLACE_KEY, ""))
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

@ExcludeFromJacocoGeneratedReport
fun changeTermsAndConditionColor(conditionPrivacy: AppCompatTextView) {
    val context = conditionPrivacy.context
    val spannableString = SpannableString(conditionPrivacy.text.toString().replace(STRING_REPLACE_KEY, ""))
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

@ExcludeFromJacocoGeneratedReport
fun changeClickHereColor(
    conditionPrivacy: AppCompatTextView,
    mCloudFormationClickHereInterface: CloudFormationInterface
) {
    val context = conditionPrivacy.context
    val spannableString = SpannableString(conditionPrivacy.text.toString().replace(STRING_REPLACE_KEY, ""))
    if (conditionPrivacy.text.contains(STRING_REPLACE_KEY)) {
        val text = conditionPrivacy.text.split(STRING_REPLACE_KEY)[1]
        val condition =
            Pattern.compile(
                text.lowercase()
            )
        val clickHere = condition.matcher(
            conditionPrivacy.text.toString().replace(STRING_REPLACE_KEY, "").lowercase(Locale.ROOT)
        )
        while (clickHere.find()) {
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        when (conditionPrivacy.text.toString()) {
                            context.resources.getString(R.string.how_to_connect_1_1).replace(STRING_REPLACE_KEY, "") -> {
                                mCloudFormationClickHereInterface.clickHere(BuildConfig.CLOUD_FORMATION_URL)
                            }
                            context.resources.getString(R.string.label_connected_title_1).replace(STRING_REPLACE_KEY, "") -> {
                                mCloudFormationClickHereInterface.clickHere(Credentials.CLOUD_FORMATION_REMOVE_URL)
                            }
                        }
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
        }
        conditionPrivacy.setText(
            spannableString,
            TextView.BufferType.SPANNABLE
        )
        conditionPrivacy.movementMethod = LinkMovementMethod.getInstance()
    }
}

@ExcludeFromJacocoGeneratedReport
fun changeLearnMoreColor(
    learnMore: AppCompatTextView,
    mCloudFormationClickHereInterface: CloudFormationInterface
) {
    val context = learnMore.context
    val spannableString = SpannableString(learnMore.text.toString().replace(STRING_REPLACE_KEY, ""))
    if (learnMore.text.contains(STRING_REPLACE_KEY)) {
        val text = learnMore.text.split(STRING_REPLACE_KEY)[1]
        val condition =
            Pattern.compile(
                text.lowercase()
            )
        val clickHere = condition.matcher(
            learnMore.text.toString().replace(STRING_REPLACE_KEY, "").lowercase(Locale.ROOT)
        )
        while (clickHere.find()) {
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        mCloudFormationClickHereInterface.clickHere(Credentials.CLOUD_INFORMATION_LEARN_MORE)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = true
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
        learnMore.setText(
            spannableString,
            TextView.BufferType.SPANNABLE
        )
        learnMore.movementMethod = LinkMovementMethod.getInstance()
    }
}

@ExcludeFromJacocoGeneratedReport
fun Activity.restartApplication() {
    val intent = Intent(this, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    Runtime.getRuntime().exit(0)
}

@ExcludeFromJacocoGeneratedReport
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

@ExcludeFromJacocoGeneratedReport
suspend fun validateIdentityPoolId(mIdentityPoolId: String?, region: String?): Boolean {
    if (region.isNullOrEmpty() || mIdentityPoolId.isNullOrEmpty()) return false
    val pattern = Regex(regionPattern)
    if (!pattern.matches(region)) return false

    try {
        val cognitoClient = CognitoIdentityClient {
            this.region = region
        }

        val request = GetIdRequest {
            identityPoolId = mIdentityPoolId
        }

        cognitoClient.use {
            it.getId(request)
        }

    } catch (e: ResourceNotFoundException) {
        return false
    } catch (e: IllegalArgumentException) {
        return false
    } catch (e: Exception) {
        return false
    }

    return true
}

fun validateUserPoolId(mUserPoolId: String?): Boolean {
    if (mUserPoolId == null) return false
    val pattern: Pattern = Pattern.compile(userPoolIdPattern)
    val matcher: Matcher = pattern.matcher(mUserPoolId)
    return matcher.matches()
}

fun validateUserPoolClientId(mUserPoolClientId: String?): Boolean {
    if (mUserPoolClientId == null) return false
    val pattern: Pattern = Pattern.compile(userPoolClientId)
    val matcher: Matcher = pattern.matcher(mUserPoolClientId)
    return matcher.matches()
}

fun setLocale(languageCode: String, context: Context) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config: Configuration = context.resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale)
}

fun getLanguageCode(): String? {
    val appLanguage = AppCompatDelegate.getApplicationLocales()
    val languageCode = appLanguage.toLanguageTags().ifEmpty {
        Locale.getDefault().language
    }
    return languageCode
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