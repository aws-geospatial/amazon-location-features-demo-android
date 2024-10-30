package com.aws.amazonlocation.ui.main.web_view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.ActivityWebViewBinding
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.utils.KEY_URL

class WebViewActivity : BaseActivity() {
    private lateinit var mBinding: ActivityWebViewBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        intent?.getStringExtra(KEY_URL)?.let { mBinding.webView.loadUrl(it) }
        val webSettings = mBinding.webView.settings
        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true
        mBinding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
    }
}
