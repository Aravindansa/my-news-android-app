package com.sa.mynews.feature_news.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import androidx.core.view.isVisible
import com.sa.mynews.databinding.ActivityNewsDetailBinding

class NewsDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityNewsDetailBinding
    private var url:String?=null
    companion object{
        const val URL="URL"
        const val TITLE="TITLE"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        url=intent.getStringExtra(URL)
        binding.tvTitle.text=intent.getStringExtra(TITLE)
        binding.webView.apply {
            webViewClient=WebViewClient()
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
        }
        if (savedInstanceState == null)
            url?.let { binding.webView.loadUrl(it) }
        binding.imgClose.setOnClickListener { finish() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState);
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.webView.restoreState(savedInstanceState);
    }
    inner class WebViewClient : android.webkit.WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return false
        }
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            binding.linearProgressIndicator.isVisible = false
        }

    }
}