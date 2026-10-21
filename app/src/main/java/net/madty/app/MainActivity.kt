package net.madty.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView

    // استخدام المسار الآمن الذي يوفره WebViewAssetLoader
    private val homeUrl = "https://appassets.androidplatform.net/assets/index.html"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // إعداد WebViewAssetLoader لتحميل الملفات المحلية بأمان
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            // استخدام الدالة الأحدث (غير المهملة)
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return handleUrl(request.url)
            }
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // تعطيل الوصول للملفات المحلية لأنه لم يعد ضرورياً مع WebViewAssetLoader
            allowFileAccess = false
            allowContentAccess = false
            loadWithOverviewMode = true
            useWideViewPort = true
            // باقي الإعدادات...
        }

        // التعامل مع التحميلات باستخدام DownloadManager
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                request.setMimeType(mimetype)
                request.addRequestHeader("User-Agent", userAgent)
                request.setDescription("جاري التحميل...")
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(
                    android.os.Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimetype)
                )
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(applicationContext, "جاري التحميل...", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "فشل التحميل: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        val container = FrameLayout(this)
        container.addView(webView)
        setContentView(container)

        if (savedInstanceState == null) {
            webView.loadUrl(homeUrl)
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    // دالة للتعامل مع الروابط الخارجية
    private fun handleUrl(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase() ?: ""
        val host = uri.host?.lowercase() ?: ""

        // الروابط التي تبدأ بـ http أو https وليست من madty.net تفتح في المتصفح
        if (scheme == "http" || scheme == "https") {
            if (host == "madty.net" || host == "www.madty.net" || host.endsWith(".madty.net")) {
                return false // اترك WebView يتعامل معها
            }
            // فتح الروابط الخارجية في المتصفح
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                return true
            } catch (e: Exception) {
                return false
            }
        }

        // التعامل مع روابط الاتصال، البريد، الرسائل، إلخ.
        if (scheme == "tel" || scheme == "mailto" || scheme == "sms" || scheme == "geo") {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                return true
            } catch (e: Exception) {
                Toast.makeText(this, "لا يوجد تطبيق مناسب لهذا الرابط", Toast.LENGTH_SHORT).show()
                return true
            }
        }

        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
