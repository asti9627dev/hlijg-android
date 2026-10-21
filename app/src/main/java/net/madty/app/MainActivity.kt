package net.madty.app

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var webView: WebView

    // الصفحة الرئيسية داخل التطبيق
    private val homeUrl =
        "file:///android_asset/assets/index.html"

    // نطاق الموقع
    private val websiteHost = "madty.net"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(13, 118, 124)
        window.navigationBarColor = Color.rgb(13, 118, 124)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        // إنشاء WebView
        webView = WebView(this)

        webView.setBackgroundColor(Color.WHITE)

        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // إعدادات الموقع
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            javaScriptCanOpenWindowsAutomatically = true

            setSupportMultipleWindows(false)

            useWideViewPort = true
            loadWithOverviewMode = true

            builtInZoomControls = true
            displayZoomControls = false

            cacheMode = WebSettings.LOAD_DEFAULT

            allowFileAccess = true
            allowContentAccess = true

            mixedContentMode =
                WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        // ملفات تعريف الارتباط
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        // عرض النوافذ المنبثقة
        webView.webChromeClient = WebChromeClient()

        // التحكم في فتح الروابط
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return handleUrl(request.url)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(
                view: WebView,
                url: String
            ): Boolean {
                return handleUrl(Uri.parse(url))
            }
        }

        // التعامل مع روابط التنزيل
        webView.setDownloadListener(
            DownloadListener { url, _, _, mimeType, _ ->
                openDownload(url, mimeType)
            }
        )

        // إنشاء حاوية عرض التطبيق
        val container = FrameLayout(this)

        container.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(container)

        // استعادة الصفحة السابقة أو فتح الرئيسية
        val restored = if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            null
        }

        if (restored == null) {
    webView.loadUrl(homeUrl)
        }
