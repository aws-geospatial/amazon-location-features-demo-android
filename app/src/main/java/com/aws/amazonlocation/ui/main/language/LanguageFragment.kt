package com.aws.amazonlocation.ui.main.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentLanguageBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_BR_PT
import com.aws.amazonlocation.utils.LANGUAGE_CODE_CH_CN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_CH_TW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ENGLISH
import com.aws.amazonlocation.utils.LANGUAGE_CODE_FRENCH
import com.aws.amazonlocation.utils.LANGUAGE_CODE_GERMAN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HINDI
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ITALIAN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_JAPANESE
import com.aws.amazonlocation.utils.LANGUAGE_CODE_KOREAN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_SPANISH
import com.aws.amazonlocation.utils.LocaleHelper
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.isRunningTest

class LanguageFragment : BaseFragment() {
    private lateinit var mBinding: FragmentLanguageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentLanguageBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        backPress()
        init()
    }

    private fun init() {
        val languageCode = getLanguageCode(requireContext())
        val isRtl =
            languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW || languageCode == LANGUAGE_CODE_HEBREW_1
        setSelectedLanguage(languageCode, isRtl)
        mBinding.apply {
            rgLanguage.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_german -> {
                        rbGerman.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_GERMAN)
                    }

                    R.id.rb_spanish -> {
                        rbSpanish.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_SPANISH)
                    }

                    R.id.rb_english -> {
                        rbEnglish.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_ENGLISH)
                    }

                    R.id.rb_french -> {
                        rbFrench.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_FRENCH)
                    }

                    R.id.rb_italian -> {
                        rbItalian.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_ITALIAN)
                    }

                    R.id.rb_brazilian_portuguese -> {
                        rbBrazilianPortuguese.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_BR_PT)
                    }

                    R.id.rb_simplified_chinese -> {
                        rbSimplifiedChinese.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_CH_CN)
                    }

                    R.id.rb_traditional_chinese -> {
                        rbTraditionalChinese.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_CH_TW)
                    }

                    R.id.rb_japanese -> {
                        rbJapanese.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_JAPANESE)
                    }

                    R.id.rb_korean -> {
                        rbKorean.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_KOREAN)
                    }

                    R.id.rb_arabic -> {
                        rbArabic.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_ARABIC)
                    }

                    R.id.rb_hebrew -> {
                        rbHebrew.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_HEBREW)
                    }

                    R.id.rb_hindi -> {
                        rbHindi.isChecked = true
                        changeAppLanguage(LANGUAGE_CODE_HINDI)
                    }
                }
            }
            ivLanguage?.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun setSelectedLanguage(
        languageCode: String?,
        isRtl: Boolean
    ) {
        mBinding.apply {
            setDeselectedRadioButtonIcon(rbGerman, isRtl)
            setDeselectedRadioButtonIcon(rbSpanish, isRtl)
            setDeselectedRadioButtonIcon(rbEnglish, isRtl)
            setDeselectedRadioButtonIcon(rbFrench, isRtl)
            setDeselectedRadioButtonIcon(rbItalian, isRtl)
            setDeselectedRadioButtonIcon(rbBrazilianPortuguese, isRtl)
            setDeselectedRadioButtonIcon(rbSimplifiedChinese, isRtl)
            setDeselectedRadioButtonIcon(rbTraditionalChinese, isRtl)
            setDeselectedRadioButtonIcon(rbJapanese, isRtl)
            setDeselectedRadioButtonIcon(rbKorean, isRtl)
            setDeselectedRadioButtonIcon(rbArabic, isRtl)
            setDeselectedRadioButtonIcon(rbHebrew, isRtl)
            setDeselectedRadioButtonIcon(rbHindi, isRtl)
            when (languageCode) {
                LANGUAGE_CODE_GERMAN -> {
                    setRadioButtonIcon(rbGerman, isRtl)
                }

                LANGUAGE_CODE_SPANISH -> {
                    setRadioButtonIcon(rbSpanish, isRtl)
                }

                LANGUAGE_CODE_ENGLISH -> {
                    setRadioButtonIcon(rbEnglish, isRtl)
                }

                LANGUAGE_CODE_FRENCH -> {
                    setRadioButtonIcon(rbFrench, isRtl)
                }

                LANGUAGE_CODE_ITALIAN -> {
                    setRadioButtonIcon(rbItalian, isRtl)
                }

                LANGUAGE_CODE_BR_PT -> {
                    setRadioButtonIcon(rbBrazilianPortuguese, isRtl)
                }

                LANGUAGE_CODE_CH_CN -> {
                    setRadioButtonIcon(rbSimplifiedChinese, isRtl)
                }

                LANGUAGE_CODE_CH_TW -> {
                    setRadioButtonIcon(rbTraditionalChinese, isRtl)
                }

                LANGUAGE_CODE_JAPANESE -> {
                    setRadioButtonIcon(rbJapanese, isRtl)
                }

                LANGUAGE_CODE_KOREAN -> {
                    setRadioButtonIcon(rbKorean, isRtl)
                }

                LANGUAGE_CODE_ARABIC -> {
                    setRadioButtonIcon(rbArabic, isRtl)
                }

                LANGUAGE_CODE_HEBREW, LANGUAGE_CODE_HEBREW_1 -> {
                    setRadioButtonIcon(rbHebrew, isRtl)
                }

                LANGUAGE_CODE_HINDI -> {
                    setRadioButtonIcon(rbHindi, isRtl)
                }

                else -> {
                    setRadioButtonIcon(rbEnglish, isRtl)
                }
            }
        }
    }

    private fun setRadioButtonIcon(
        rb: AppCompatRadioButton,
        isRtl: Boolean
    ) {
        rb.isChecked = true
        if (isRtl) {
            rb.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_checkmark,
                0,
                0,
                0
            )
        } else {
            rb.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.icon_checkmark,
                0
            )
        }
    }

    private fun setDeselectedRadioButtonIcon(
        rb: AppCompatRadioButton,
        isRtl: Boolean
    ) {
        rb.isChecked = false
        if (isRtl) {
            rb.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_radio_button_unchecked,
                0,
                0,
                0
            )
        } else {
            rb.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_radio_button_unchecked,
                0
            )
        }
    }

    private fun changeAppLanguage(languageCode: String) {
        val properties =
            listOf(
                Pair(AnalyticsAttribute.LANGUAGE, languageCode),
                Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
            )
        (activity as MainActivity).analyticsUtils?.recordEvent(
            EventType.LANGUAGE_CHANGED,
            properties
        )
        if (!isRunningTest) {
            LocaleHelper.setLocale(requireContext(), languageCode)
            (activity as MainActivity).recreate()
            val isRtl =
                languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW || languageCode == LANGUAGE_CODE_HEBREW_1
            (activity as MainActivity).window.decorView.layoutDirection = if (isRtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

            val navController = findNavController()
            val currentDestinationId = navController.currentDestination?.id

            currentDestinationId?.let {
                val navOptions =
                    NavOptions
                        .Builder()
                        .setPopUpTo(it, true)
                        .build()

                navController.navigate(it, null, navOptions)
            }
            init()
        }
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
