package com.aws.amazonlocation.ui.main.attribution

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentAttributionBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.web_view.WebViewActivity
import com.aws.amazonlocation.utils.KEY_URL
import com.aws.amazonlocation.utils.MAP_STYLE_ATTRIBUTION
import com.aws.amazonlocation.utils.attributionPattern

class AttributionFragment : BaseFragment() {

    private lateinit var mBinding: FragmentAttributionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAttributionBinding.inflate(inflater, container, false)
        return mBinding.root
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val width = resources.getDimensionPixelSize(R.dimen.attribution_learn_more)
        mBinding.btnLearnMore.layoutParams.width = width
        mBinding.btnLearnMore.requestLayout()
        mBinding.btnLearnMoreSa.layoutParams.width = width
        mBinding.btnLearnMoreSa.requestLayout()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        backPress()
        initClick()
        init()
    }

    private fun init() {
        mBinding.apply {
            tvAttribution.text =
                mPreferenceManager.getValue(MAP_STYLE_ATTRIBUTION, "")
                    ?.replace(Regex(attributionPattern), "") ?: ""

        }
    }
    private fun initClick() {
        mBinding.apply {
            btnLearnMoreSa.setOnClickListener {
                startActivity(
                    Intent(
                        context,
                        WebViewActivity::class.java
                    ).putExtra(KEY_URL, BuildConfig.BASE_DOMAIN + BuildConfig.AWS_SOFTWARE_ATTRIBUTION_URL)
                )
            }
            btnLearnMore.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(BuildConfig.ATTRIBUTION_LEARN_MORE_HERE_URL)
                    )
                )
            }
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
