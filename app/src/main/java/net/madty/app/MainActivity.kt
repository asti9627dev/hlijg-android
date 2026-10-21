package net.madty.app

import android.annotation.SuppressLint
import android.app.Activity
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

    // الواجهة المحلية الحالية داخل assets
    private val homeUrl = "file:///android_asset/assets/index.html"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(13, 118, 124)
        window.navigationBarColor = Color.rgb(13, 118, 124)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)

            settings.apply {
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

                // السماح بقراءة ملفات الواجهة المحلية ومواردها.
                // لا تستخدم هذه الإعدادات لفتح ملفات غير موثوقة.
                allowFileAccess = true
                allowContentAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return handleNavigation(request.url)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(
                view: WebView,
                url: String
            ): Boolean {
                return handleNavigation(Uri.parse(url))
            }
        }

        // تمرير روابط التنزيل إلى تطبيق قادر على فتح الملف أو تنزيله.
        webView.setDownloadListener(
            DownloadListener { url, _, _, mimeType, _ ->
                openDownload(url, mimeType)
            }
        )

        setContentView(
            FrameLayout(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addView(webView)
            }
        )

        if (savedInstanceState == null) {
            webView.loadUrl(homeUrl)
        } else {
            val restored = webView.restoreState(savedInstanceState)
            if (restored == null) webView.loadUrl(homeUrl)
        }
    }

    /**
     * يبقي صفحات الموقع داخل WebView.
     * روابط التطبيقات الخارجية مثل الهاتف والبريد تفتح في تطبيقاتها.
     */
    private fun handleNavigation(uri: Uri): Boolean {
        val scheme = uri.scheme.orEmpty().lowercase()
        val host = uri.host.orEmpty().lowercase()

        if (scheme == "http" || scheme == "https") {
            // موقع madty.net ونطاقاته الفرعية تبقى داخل التطبيق.
            if (
                host == "madty.net" ||
                host == "www.madty.net" ||
                host.endsWith(".madty.net")
            ) {
                return false
            }

            // تبقى روابط الويب الأخرى داخل WebView كذلك، حتى تعمل
            // التحويلات وروابط الصور وملفات PDF داخل عرض التطبيق.
            return false
        }

        if (
            scheme == "tel" ||
            scheme == "mailto" ||
            scheme == "sms" ||
            scheme == "geo"
        ) {
            return try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                true
            } catch (_: Exception) {
                Toast.makeText(
                    this,
                    "لا يوجد تطبيق مناسب لفتح هذا الرابط",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
        }

        if (scheme == "intent") {
            return try {
                val intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                startActivity(intent)
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

        // السماح لـ WebView بمعالجة المخططات الأخرى افتراضيًا.
        return false
    }

    private fun openDownload(url: String, mimeType: String?) {
        try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType ?: "*/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: Exception) {
                Toast.makeText(
                    this,
                    "تعذر فتح رابط التنزيل. تحقق من وجود تطبيق مناسب للملف.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::webView.isInitialized) {
            webView.saveState(outState)
        }
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Android API, retained for back navigation")
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
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
        super.onDestroy()
    }
}
