package net.madty.app

import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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

        // إنشاء حاوية التطبيق
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
    }

    // تحديد طريقة فتح الروابط
    private fun handleUrl(uri: Uri): Boolean {

        val scheme = uri.scheme?.lowercase() ?: return false

        // السماح بفتح صفحات التطبيق والموقع داخلياً
        if (scheme == "file" || scheme == "about") {
            return false
        }

        if (scheme == "http" || scheme == "https") {

            val host = uri.host?.lowercase() ?: return false

            if (
                host == websiteHost ||
                host.endsWith(".$websiteHost")
            ) {
                return false
            }
        }

        // فتح الروابط الخارجية في المتصفح أو التطبيق المناسب
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "لا يوجد تطبيق لفتح هذا الرابط",
                Toast.LENGTH_SHORT
            ).show()
            true
        } catch (e: Exception) {
            true
        }
    }

    // تنزيل الملفات من الموقع
    private fun openDownload(url: String, mimeType: String?) {

        try {

            val fileName = URLUtil.guessFileName(
                url,
                null,
                mimeType
            )

            val request = DownloadManager.Request(Uri.parse(url))

            request.setTitle(fileName)
            request.setDescription("جاري تنزيل الملف...")
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )

            if (!mimeType.isNullOrEmpty()) {
                request.setMimeType(mimeType)
            }

            // تمرير ملفات تعريف الارتباط عند الحاجة
            val cookies = CookieManager.getInstance().getCookie(url)

            if (!cookies.isNullOrEmpty()) {
                request.addRequestHeader("Cookie", cookies)
            }

            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )

            val manager = getSystemService(
                Context.DOWNLOAD_SERVICE
            ) as DownloadManager

            manager.enqueue(request)

            Toast.makeText(
                this,
                "بدأ تنزيل الملف",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "تعذر تنزيل الملف: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // حفظ حالة الصفحة عند إعادة إنشاء النشاط
    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    // زر الرجوع
    @Deprecated("Deprecated in Java")
    override
