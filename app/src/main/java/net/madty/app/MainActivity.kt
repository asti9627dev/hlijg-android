package net.madty.app

import android.app.Activity
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.graphics.Color
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import android.content.Intent
import android.net.Uri

class MainActivity : Activity() {

    private lateinit var webView: WebView

    // الواجهة الرئيسية المحلية داخل التطبيق
    private val homeUrl =
        "file:///android_asset/assets/index.html"

    // نطاق الموقع
    private val websiteHost = "madty.net"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(13, 118, 77)
        window.navigationBarColor = Color.rgb(13, 118, 77)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        webView = WebView(this)

        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
            allowFileAccess = true
            allowContentAccess = true
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        CookieManager.getInstance().setAcceptCookie(true)

        CookieManager.getInstance()
            .setAcceptThirdPartyCookies(webView, true)

        webView.webChromeClient = WebChromeClient()

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

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: android.webkit.WebResourceError
            ) {
                super.onReceivedError(view, request, error)

                if (request.isForMainFrame) {
                    Toast.makeText(
                        this@MainActivity,
                        "تعذر تحميل الصفحة. تحقق من اتصال الإنترنت.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // فتح روابط التنزيل
        webView.setDownloadListener(
            DownloadListener { url, _, _, _, _ ->
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    )
                    startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(
                        this,
                        "تعذر فتح رابط التنزيل",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        setContentView(
            FrameLayout(this).apply {
                addView(
                    webView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }
        )

        if (savedInstanceState == null) {
            webView.loadUrl(homeUrl)
        } else {
            val restored = webView.restoreState(savedInstanceState)

            if (restored == null) {
                webView.loadUrl(homeUrl)
            }
        }
    }

    private fun handleUrl(uri: Uri): Boolean {

        val scheme = uri.scheme.orEmpty().lowercase()
        val host = uri.host.orEmpty().lowercase()

        // السماح بفتح الواجهة المحلية ومواردها
        if (scheme == "file") {
            return false
        }

        // إبقاء صفحات الموقع داخل التطبيق
        if (
            (scheme == "https" || scheme == "http") &&
            (
                host == websiteHost ||
                host == "www.$websiteHost" ||
                host.endsWith(".$websiteHost")
            )
        ) {
            return false
        }

        // فتح الروابط الخارجية في المتصفح
        return try {
            startActivity(
                Intent(Intent.ACTION_VIEW, uri)
            )
            true
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "تعذر فتح الرابط",
                Toast.LENGTH_SHORT
            ).show()
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }
}
