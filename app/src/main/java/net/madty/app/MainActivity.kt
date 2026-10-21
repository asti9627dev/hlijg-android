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

    // الصفحة الرئيسية المحلية داخل التطبيق
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

        // إنشاء نافذة عرض الموقع
        webView = WebView(this)

        webView.setBackgroundColor(Color.WHITE)

        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // إعدادات WebView
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

        // تفعيل ملفات تعريف الارتباط
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webChromeClient = WebChromeClient()

        // التحكم في التنقل بين الصفحات
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

        // التعامل مع روابط تنزيل الملفات
        webView.setDownloadListener(
            DownloadListener { url, _, _, mimeType, _ ->
                openDownload(url, mimeType)
            }
        )

        // عرض WebView داخل التطبيق
        val container = FrameLayout(this)

        container.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(container)

        // تحميل الصفحة الرئيسية أو استعادة الصفحة السابقة
        val restored = if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            null
        }

        if (restored == null) {
            webView.loadUrl(homeUrl)
        }
    }

    // تحديد طريقة فتح الروابط
    private fun handleUrl(uri: Uri): Boolean {

        val scheme = uri.scheme.orEmpty().lowercase()
        val host = uri.host.orEmpty().lowercase()

        // صفحات موقع madty.net تفتح داخل التطبيق
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

        // روابط الهاتف والبريد والرسائل والموقع الجغرافي
        if (
            scheme == "tel" ||
            scheme == "mailto" ||
            scheme == "sms" ||
            scheme == "smsto" ||
            scheme == "geo"
        ) {
            return openExternalUri(uri)
        }

        // روابط التطبيقات الخارجية
        if (
            scheme == "intent" ||
            scheme == "market" ||
            scheme == "whatsapp"
        ) {
            return openExternalUri(uri)
        }

        // الروابط الخارجية تفتح في التطبيق المناسب
        if (scheme == "https" || scheme == "http") {
            return openExternalUri(uri)
        }

        // تجاهل المخططات غير المدعومة
        return true
    }

    // فتح الروابط الخارجية
    private fun openExternalUri(uri: Uri): Boolean {

        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            true

        } catch (_: ActivityNotFoundException) {

            Toast.makeText(
                this,
                "لا يوجد تطبيق مناسب لفتح هذا الرابط",
                Toast.LENGTH_SHORT
            ).show()

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

    // فتح روابط تنزيل الملفات
    private fun openDownload(
        url: String,
        mimeType: String?
    ) {

        try {
            val uri = Uri.parse(url)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType ?: "*/*")
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
                    "تعذر فتح الملف. حاول تنزيله من المتصفح.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // حفظ حالة الصفحة
    override fun onSaveInstanceState(outState: Bundle) {

        if (::webView.isInitialized) {
            webView.saveState(outState)
        }

        super.onSaveInstanceState(outState)
    }

    // الرجوع إلى الصفحة السابقة
    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {

        if (
            ::webView.isInitialized &&
            webView.canGoBack()
        ) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    // تحرير موارد WebView
    override fun onDestroy() {

        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.webChromeClient = null
            webView.webViewClient = null

            (webView.parent as? ViewGroup)?.removeView(webView)

            webView.destroy()
        }

        super.onDestroy()
    }
}
