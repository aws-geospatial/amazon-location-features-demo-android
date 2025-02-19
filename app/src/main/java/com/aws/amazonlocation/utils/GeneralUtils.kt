package com.aws.amazonlocation.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import aws.sdk.kotlin.services.cognitoidentity.CognitoIdentityClient
import aws.sdk.kotlin.services.cognitoidentity.model.GetIdRequest
import aws.sdk.kotlin.services.cognitoidentity.model.ResourceNotFoundException
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.LanguageData
import com.aws.amazonlocation.data.response.LoginResponse
import com.aws.amazonlocation.data.response.PoliticalData
import com.aws.amazonlocation.utils.DateFormat.MM_DD_YYYY_HH_MM
import com.aws.amazonlocation.utils.DateFormat.YYYY_MM_DD_T_HH_MM_SS
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.maplibre.android.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
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


fun formatToDisplayTime(utcOffsetTime: String, outputDateFormat: String): String {
    val inputFormat = SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS, Locale.getDefault() ?: Locale.US)
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val date: Date = inputFormat.parse(utcOffsetTime) as Date

    val outputFormat = SimpleDateFormat(outputDateFormat, Locale.getDefault() ?: Locale.US)
    outputFormat.timeZone = TimeZone.getDefault()

    return outputFormat.format(date)
}

fun formatToISO8601(date: Date): String {
    val iso8601Format = SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS, Locale.getDefault() ?: Locale.US)
    return iso8601Format.format(date)
}

fun formatToDisplayDate(date: Date): String {
    val displayFormat = SimpleDateFormat(MM_DD_YYYY_HH_MM, Locale.getDefault() ?: Locale.US)
    return displayFormat.format(date)
}

fun getPoliticalData(context: Context): ArrayList<PoliticalData> {
    return arrayListOf(
        PoliticalData(
            countryName = context.getString(R.string.label_no_political_view),
            description = "",
            countryCode = "",
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_arg),
            description = context.getString(R.string.description_arg),
            countryCode = context.getString(R.string.flag_arg),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_cyp),
            description = context.getString(R.string.description_cyp),
            countryCode = context.getString(R.string.flag_cyp),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_egy),
            description = context.getString(R.string.description_egy),
            countryCode = context.getString(R.string.flag_egy),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_geo),
            description = context.getString(R.string.description_geo),
            countryCode = context.getString(R.string.flag_geo),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_grc),
            description = context.getString(R.string.description_grc),
            countryCode = context.getString(R.string.flag_grc),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_ind),
            description = context.getString(R.string.description_ind),
            countryCode = context.getString(R.string.flag_ind),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_ken),
            description = context.getString(R.string.description_ken),
            countryCode = context.getString(R.string.flag_ken),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_mar),
            description = context.getString(R.string.description_mar),
            countryCode = context.getString(R.string.flag_mar),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_ps),
            description = context.getString(R.string.description_ps),
            countryCode = context.getString(R.string.flag_ps),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_rus),
            description = context.getString(R.string.description_rus),
            countryCode = context.getString(R.string.flag_rus),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_sdn),
            description = context.getString(R.string.description_sdn),
            countryCode = context.getString(R.string.flag_sdn),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_srb),
            description = context.getString(R.string.description_srb),
            countryCode = context.getString(R.string.flag_srb),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_sur),
            description = context.getString(R.string.description_sur),
            countryCode = context.getString(R.string.flag_sur),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_syr),
            description = context.getString(R.string.description_syr),
            countryCode = context.getString(R.string.flag_syr),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_tur),
            description = context.getString(R.string.description_tur),
            countryCode = context.getString(R.string.flag_tur),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_tza),
            description = context.getString(R.string.description_tza),
            countryCode = context.getString(R.string.flag_tza),
        ),
        PoliticalData(
            countryName = context.getString(R.string.label_ury),
            description = context.getString(R.string.description_ury),
            countryCode = context.getString(R.string.flag_ury),
        ),
    )
}

fun getLanguageData(context: Context): ArrayList<LanguageData> {
    return arrayListOf(
        LanguageData(value = context.getString(R.string.label_no_map_language), label = context.getString(R.string.label_no_map_language),),
        LanguageData(value = "en", label = "English"),
        LanguageData(value = "ar", label = "العربية"),
        LanguageData(value = "as", label = "অসমীয়া"),
        LanguageData(value = "az", label = "Azərbaycan dili"),
        LanguageData(value = "be", label = "Беларуская"),
        LanguageData(value = "bg", label = "Български"),
        LanguageData(value = "bn", label = "বাংলা"),
        LanguageData(value = "bs", label = "Bosanski"),
        LanguageData(value = "ca", label = "Català"),
        LanguageData(value = "cs", label = "Čeština"),
        LanguageData(value = "cy", label = "Cymraeg"),
        LanguageData(value = "da", label = "Dansk"),
        LanguageData(value = "de", label = "Deutsch"),
        LanguageData(value = "el", label = "Ελληνικά"),
        LanguageData(value = "es", label = "Español"),
        LanguageData(value = "et", label = "Eesti"),
        LanguageData(value = "eu", label = "Euskara"),
        LanguageData(value = "fi", label = "Suomi"),
        LanguageData(value = "fo", label = "Føroyskt"),
        LanguageData(value = "fr", label = "Français"),
        LanguageData(value = "ga", label = "Gaeilge"),
        LanguageData(value = "gl", label = "Galego"),
        LanguageData(value = "gn", label = "Avañe'ẽ"),
        LanguageData(value = "gu", label = "ગુજરાતી"),
        LanguageData(value = "he", label = "עברית"),
        LanguageData(value = "hi", label = "हिन्दी"),
        LanguageData(value = "hr", label = "Hrvatski"),
        LanguageData(value = "hu", label = "Magyar"),
        LanguageData(value = "hy", label = "Հայերեն"),
        LanguageData(value = "id", label = "Bahasa Indonesia"),
        LanguageData(value = "is", label = "Íslenska"),
        LanguageData(value = "it", label = "Italiano"),
        LanguageData(value = "ja", label = "日本語"),
        LanguageData(value = "ka", label = "ქართული"),
        LanguageData(value = "kk", label = "Қазақ тілі"),
        LanguageData(value = "km", label = "ខ្មែរ"),
        LanguageData(value = "kn", label = "ಕನ್ನಡ"),
        LanguageData(value = "ko", label = "한국어"),
        LanguageData(value = "ky", label = "Кыргызча"),
        LanguageData(value = "lt", label = "Lietuvių"),
        LanguageData(value = "lv", label = "Latviešu"),
        LanguageData(value = "mk", label = "Македонски"),
        LanguageData(value = "ml", label = "മലയാളം"),
        LanguageData(value = "mr", label = "मराठी"),
        LanguageData(value = "ms", label = "Bahasa Melayu"),
        LanguageData(value = "mt", label = "Malti"),
        LanguageData(value = "my", label = "မြန်မာစာ"),
        LanguageData(value = "nl", label = "Nederlands"),
        LanguageData(value = "no", label = "Norsk"),
        LanguageData(value = "or", label = "ଓଡ଼ିଆ"),
        LanguageData(value = "pa", label = "ਪੰਜਾਬੀ"),
        LanguageData(value = "pl", label = "Polski"),
        LanguageData(value = "pt", label = "Português"),
        LanguageData(value = "ro", label = "Română"),
        LanguageData(value = "ru", label = "Русский"),
        LanguageData(value = "sk", label = "Slovenčina"),
        LanguageData(value = "sl", label = "Slovenščina"),
        LanguageData(value = "sq", label = "Shqip"),
        LanguageData(value = "sr", label = "Српски"),
        LanguageData(value = "sv", label = "Svenska"),
        LanguageData(value = "ta", label = "தமிழ்"),
        LanguageData(value = "te", label = "తెలుగు"),
        LanguageData(value = "th", label = "ไทย"),
        LanguageData(value = "tr", label = "Türkçe"),
        LanguageData(value = "uk", label = "Українська"),
        LanguageData(value = "uz", label = "Oʻzbek"),
        LanguageData(value = "vi", label = "Tiếng Việt"),
        LanguageData(value = "zh", label = "简体中文"),
        LanguageData(value = "zh-Hant", label = "繁體中文"),
    )
}