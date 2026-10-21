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
import android.webkit.URLUtil
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

    // رابط موقعك
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

        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        webView.setBackgroundColor(Color.WHITE)

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

        // إدارة النوافذ المنبثقة
        webView.webChromeClient = WebChromeClient()

        // إدارة فتح الروابط
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

        // التعامل مع التنزيلات
        webView.setDownloadListener(
            DownloadListener { url, _, contentDisposition, mimeType, _ ->
                openDownload(
                    url,
                    mimeType,
                    contentDisposition
                )
            }
        )

        // إضافة WebView إلى الشاشة
        val container = FrameLayout(this)

        container.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(container)

        // استعادة الصفحة السابقة عند الإمكان
        if (savedInstanceState != null) {
            val restored = webView.restoreState(savedInstanceState)

            if (restored == null) {
                webView.loadUrl(homeUrl)
            }
        } else {
            webView.loadUrl(homeUrl)
        }
    }

    /**
     * تحديد طريقة فتح الرابط:
     * روابط الموقع داخليًا، والروابط الخارجية بالتطبيق المناسب.
     */
    private fun handleUrl(uri: Uri): Boolean {

        val scheme = uri.scheme.orEmpty().lowercase()
        val host = uri.host.orEmpty().lowercase()

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

        // فتح روابط الهاتف والبريد والرسائل
        if (
            scheme == "tel" ||
            scheme == "mailto" ||
            scheme == "sms" ||
            scheme == "smsto" ||
            scheme == "geo"
        ) {
            return openExternalUri(uri)
        }

        // فتح روابط التطبيقات الخارجية
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

        // منع المخططات غير المعروفة
        return true
    }

    /**
     * فتح الرابط باستخدام تطبيق مناسب على الجهاز.
     */
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

    /**
     * فتح الملفات التي يطلب الموقع تنزيلها.
     */
    private fun openDownload(
        url: String,
        mimeType: String?,
        contentDisposition: String?
    ) {
        try {
            val uri = Uri.parse(url)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    uri,
                    mimeType ?: "*/*"
                )

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(intent)

        } catch (_: Exception) {

            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )

                startActivity(intent)

            } catch (_: Exception) {

                Toast.makeText(
                    this,
                    "تعذر فتح رابط التنزيل. حاول فتحه من المتصفح.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * حفظ حالة الصفحة عند الحاجة.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        if (::webView.isInitialized) {
            webView.saveState(outState)
        }

        super.onSaveInstanceState(outState)
    }

    /**
     * زر الرجوع:
     * يرجع إلى الصفحة
