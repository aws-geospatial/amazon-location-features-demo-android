package com.aws.amazonlocation.ui.main.webView

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.aws.amazonlocation.databinding.ActivityWebViewBinding
import com.aws.amazonlocation.utils.KEY_URL
import com.aws.amazonlocation.utils.changeStatusBarColor

class WebViewActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityWebViewBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(isLightStatusBar = true, android.R.color.white)
        mBinding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
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
