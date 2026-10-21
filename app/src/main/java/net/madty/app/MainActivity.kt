package net.madty.app

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

    // الصفحة الرئيسية المحلية داخل التطبيق
    private val homeUrl = "file:///android_asset/assets/index.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(13, 118, 124)
        window.navigationBarColor = Color.rgb(13, 118, 124)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        webView = WebView(this)

        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // إعدادات عرض الموقع
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

            // السماح بتحميل ملفات الواجهة المحلية
            allowFileAccess = true
            allowContentAccess = true

            // السماح بمحتوى HTTP داخل صفحات HTTPS عند الحاجة
            mixedContentMode =
                WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
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

        // معالجة روابط تنزيل الملفات
        webView.setDownloadListener(
            DownloadListener { url, _, _, mimeType, _ ->
                openDownload(url, mimeType)
            }
        )

        val rootLayout = FrameLayout(this).apply {
            addView(
                webView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        setContentView(rootLayout)

        if (savedInstanceState == null) {
            webView.loadUrl(homeUrl)
        } else {
            val restoredState = webView.restoreState(savedInstanceState)

            if (restoredState == null) {
                webView.loadUrl(homeUrl)
            }
        }
    }

    /**
     * إبقاء صفحات الموقع داخل التطبيق.
     */
    private fun handleNavigation(uri: Uri): Boolean {

        val scheme = uri.scheme.orEmpty().lowercase()
        val host = uri.host.orEmpty().lowercase()

        // روابط الموقع تفتح داخل WebView
        if (
            scheme == "https" &&
            (
                host == "madty.net" ||
                host == "www.madty.net" ||
                host.endsWith(".madty.net")
            )
        ) {
            return false
        }

        // فتح صفحات الموقع التي تستخدم HTTP داخل WebView
        if (
            scheme == "http" &&
            (
                host == "madty.net" ||
                host == "www.madty.net" ||
                host.endsWith(".madty.net")
            )
        ) {
            return false
        }

        // روابط الصفحات المحلية داخل التطبيق
        if (scheme == "file" && uri.path.orEmpty().contains("/android_asset/")) {
            return false
        }

        // روابط البريد والهاتف والرسائل والتطبيقات الخارجية
        if (
            scheme == "tel" ||
            scheme == "mailto" ||
            scheme == "sms" ||
            scheme == "geo"
        ) {
            return openExternalUri(uri)
        }

        // روابط التطبيقات التي تستخدم مخطط intent
        if (scheme == "intent") {
            return try {
                val intent = Intent.parseUri(
                    uri.toString(),
                    Intent.URI_INTENT_SCHEME
                )

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

        // الروابط الخارجية تفتح في المتصفح المناسب
        if (scheme == "https" || scheme == "http") {
            return openExternalUri(uri)
        }

        // منع محاولة فتح مخططات غير معروفة
        return true
    }

    private fun openExternalUri(uri: Uri): Boolean {
        return try {
            startActivity(
                Intent(Intent.ACTION_VIEW, uri)
            )
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

    /**
     * فتح رابط تنزيل الملف.
     */
    private fun openDownload(
        url: String,
        mimeType: String?
    ) {
        try {
            val uri = Uri.parse(url)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType ?: "*/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(intent)

        } catch (_: Exception) {

            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    )
                )
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
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()

            (webView.parent as? ViewGroup)?.removeView(webView)

            webView.destroy()
        }

        super.onDestroy()
    }
}
