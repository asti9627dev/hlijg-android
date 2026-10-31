package net.madty.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
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
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var mainContainer: FrameLayout
    private lateinit var nativeHomeLayout: LinearLayout
    private lateinit var contentContainer: LinearLayout

    private val websiteHost = "madtiy.net"
    private val primaryColor = Color.rgb(13, 118, 124)
    private val imageExecutor = Executors.newFixedThreadPool(4)

    private data class Grade(
        val name: String,
        val imageUrl: String,
        val link: String
    )

    private data class Category(
        val name: String,
        val query: String,
        val imageUrl: String
    )

    private var currentSemester = 1
    private var currentGrade: Grade? = null

    private val dataTerm1 = listOf(
        Grade("الصف الاول الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/07/اول-ابتدائي-الفصل-الدراسي-الاول.jpg", "https://www.madtiy.net/fd1/sf1/"),
        Grade("الصف الثاني الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/07/ثاني-ابتدائي-الفصل-الدراسي-الاول.jpg", "https://www.madtiy.net/fd1/sf2/"),
        Grade("الصف الثالث الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/12/ثالث-ابتدائي-الفصل-الدراسي-الاول.jpg", "https://www.madtiy.net/fd1/sf3/"),
        Grade("الصف الرابع الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/07/رابع-ابتدائي-الفصل-الدراسي-الاول-ف1.jpg", "https://www.madtiy.net/fd1/sf4/"),
        Grade("الصف الخامس الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/07/خامس-ابتدائي-الفصل-الدراسي-الاول-ف1.jpg", "https://www.madtiy.net/fd1/sf5/"),
        Grade("الصف السادس الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/07/سادس-ابتدائي-الفصل-الدراسي-الاول-ف1.jpg", "https://www.madtiy.net/fd1/sf6/"),
        Grade("الصف الاول المتوسط", "https://www.madtiy.net/wp-content/uploads/2023/07/اول-متوسط-الفصل-الدراسي-الاول-ف1.jpg", "https://www.madtiy.net/fd1/sf7/"),
        Grade("الصف الثاني المتوسط", "https://www.madtiy.net/wp-content/uploads/2023/07/ثاني-متوسط-الفصل-الدراسي-الاول-ف1.jpg", "https://www.madtiy.net/fd1/sf8/"),
        Grade("الصف الثالث المتوسط", "https://www.madtiy.net/wp-content/uploads/2023/07/ثالث-متوسط-الفصل-الدراسي-الاول-ف1.jpg", "https://www.madtiy.net/fd1/sf9/"),
        Grade("الصف الاول الثانوي", "https://www.madtiy.net/wp-content/uploads/2023/07/اول-ثانوي-مسارات-الفصل-الدراسي-الاول-ف1.jpg", "https://www.madtiy.net/fd1/%d8%a7%d9%84%d8%b5%d9%81-%d8%a7%d9%84%d8%a7%d9%88%d9%84-%d8%a7%d9%84%d8%ab%d8%a7%d9%86%d9%88%d9%8a/"),
        Grade("الصف الثاني الثانوي", "https://www.madtiy.net/wp-content/uploads/2023/07/ثاني-ثانوي-مسارات-الفصل-الدراسي-الاول-ف1.jpg", "https://www.madtiy.net/fd1/%d8%a7%d9%84%d8%b5%d9%81-%d8%a7%d9%84%d8%ab%d8%a7%d9%86%d9%8a-%d8%a7%d9%84%d8%ab%d8%a7%d9%86%d9%88%d9%8a/"),
        Grade("الصف الثالث الثانوي", "https://www.madtiy.net/wp-content/uploads/2023/08/ثالث-ثانوي-مسارات-الفصل-الدراسي-الاول-ف1.jpg", "https://www.madtiy.net/fd1/sf12/")
    )

    private val dataTerm2 = listOf(
        Grade("الصف الاول الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/11/اول-ابتدائي-الفصل-الدراسي-الثاني.jpg", "https://www.madtiy.net/fd2/sf01/"),
        Grade("الصف الثاني الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/11/ثاني-ابتدائي-الفصل-الدراسي-الثاني.jpg", "https://www.madtiy.net/fd2/sf02/"),
        Grade("الصف الثالث الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/12/ثالث-ابتدائي-الفصل-الدراسي-الثاني.jpg", "https://www.madtiy.net/fd2/sf03/"),
        Grade("الصف الرابع الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/11/رابع-ابتدائي-الفصل-الدراسي-الثاني-ف2.jpg", "https://www.madtiy.net/fd2/sf04/"),
        Grade("الصف الخامس الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/11/خامس-ابتدائي-الفصل-الدراسي-الثاني-ف2.jpg", "https://www.madtiy.net/fd2/sf05/"),
        Grade("الصف السادس الابتدائي", "https://www.madtiy.net/wp-content/uploads/2023/11/سادس-ابتدائي-الفصل-الدراسي-الثاني-ف2.jpg", "https://www.madtiy.net/fd2/sf06/"),
        Grade("الصف الاول المتوسط", "https://www.madtiy.net/wp-content/uploads/2023/11/اول-متوسط-الفصل-الدراسي-الثاني-ف2.jpg", "https://www.madtiy.net/fd2/sf07/"),
        Grade("الصف الثاني المتوسط", "https://www.madtiy.net/wp-content/uploads/2023/11/ثاني-متوسط-الفصل-الدراسي-الثاني-ف2.jpg", "https://www.madtiy.net/fd2/sf08/"),
        Grade("الصف الثالث المتوسط", "https://www.madtiy.net/wp-content/uploads/2023/11/ثالث-متوسط-الفصل-الدراسي-الثاني-ف2.jpg", "https://www.madtiy.net/fd2/sf09/"),
        Grade("الصف الاول الثانوي", "https://www.madtiy.net/wp-content/uploads/2023/07/اول-ثانوي-مسارات-الفصل-الدراسي-الثاني-ف2.jpg", "https://www.madtiy.net/fd2/sf010/"),
        Grade("الصف الثاني الثانوي", "https://www.madtiy.net/wp-content/uploads/2023/07/ثاني-ثانوي-مسارات-الفصل-الدراسي-الثاني-ف2.jpg", "https://www.madtiy.net/fd2/sf012/"),
        Grade("الصف الثالث الثانوي", "https://www.madtiy.net/wp-content/uploads/2023/11/ثالث-ثانوي-مسارات-الفصل-الدراسي-الثاني-ف2.jpg", "https://www.madtiy.net/fd2/sf011/")
    )

    private val subjectImageUrls = mapOf(
        "الرياضيات" to "https://www.madtiy.net/wp-content/uploads/2023/07/الرياضيات.jpg",
        "لغتي" to "https://www.madtiy.net/wp-content/uploads/2023/07/لغتي.jpg",
        "لغتي الخالدة" to "https://www.madtiy.net/wp-content/uploads/2023/07/لغتي.jpg",
        "العلوم" to "https://www.madtiy.net/wp-content/uploads/2023/07/العلوم.jpg",
        "الدراسات الإسلامية" to "https://www.madtiy.net/wp-content/uploads/2023/07/الدراسات.jpg"
    )

    private val firstGradeCategories = listOf(
        Category("لغتي", "لغتي", "https://www.madtiy.net/wp-content/uploads/2025/08/لغتي-اول-ابتدائي-الجزء-الاول.jpg"),
        Category("الرياضيات", "رياضيات", "https://www.madtiy.net/wp-content/uploads/2025/08/رياضيات-اول-ابتدائي-الجزء-الاول.jpg"),
        Category("العلوم", "علوم", "https://www.madtiy.net/wp-content/uploads/2025/08/مادة-العلوم-اول-ابتدائي-الجزء-الاول.jpg"),
        Category("الإنجليزي", "انجليزي", "https://www.madtiy.net/wp-content/uploads/2025/08/مادة-الانجليزي-اول-ابتدائي-الجزء-الاول.jpg"),
        Category("الدراسات الإسلامية", "دراسات اسلامية", "https://www.madtiy.net/wp-content/uploads/2025/08/الدراسات-الاسلاميه-اول-ابتدائي-الجزء-الاول.jpg"),
        Category("المهارات الحياتية والأسرية", "مهارات حياتية وأسرية", "https://www.madtiy.net/wp-content/uploads/2025/08/المهارات-الحياتيه-والاسريه-اول-ابتدائي-الجزء-الاول.jpg"),
        Category("التربية الفنية", "تربية فنية", "https://www.madtiy.net/wp-content/uploads/2025/08/مادة-التربية-الفنية-اول-ابتدائي-الجزء-الاول.jpg"),
        Category("التربية البدنية", "تربية بدنية", "https://www.madtiy.net/wp-content/uploads/2023/08/مادة-التربية-البدنية-اول-ابتدائي-الفصل-الاول.jpg"),
        Category("نماذج اختبارات", "نماذج اختبارات", "https://www.madtiy.net/wp-content/uploads/2023/10/نماذج-اختبارات-اول-ابتدائي-الفصل-الدراسي-الاول.jpg")
    )

    private fun categoriesForGrade(grade: Grade): List<Category> {
        if (grade.name == "الصف الاول الابتدائي") return firstGradeCategories
        val names = listOf(
            "لغتي", "الرياضيات", "العلوم", "الدراسات الإسلامية"
        )
        return names.map { name ->
            Category(name, name, subjectImageUrls[name] ?: subjectImageUrls["العلوم"]!!)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = primaryColor
        window.navigationBarColor = primaryColor
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        webView = WebView(this).apply {
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
                allowFileAccess = true
                allowContentAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        mainContainer = FrameLayout(this)
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 8, Gravity.TOP
            )
            visibility = View.GONE
            progressTintList = android.content.res.ColorStateList.valueOf(primaryColor)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return handleUrl(request.url)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return handleUrl(Uri.parse(url))
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
            }
        }

        webView.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            openDownload(url, userAgent, contentDisposition, mimeType)
        })

        buildNativeHomeScreen()
        mainContainer.addView(webView)
        mainContainer.addView(nativeHomeLayout)
        mainContainer.addView(progressBar)
        setContentView(mainContainer)

        if (savedInstanceState != null) {
            val restored = webView.restoreState(savedInstanceState)
            if (restored != null && webView.url != null && webView.url != "about:blank") {
                showWebview()
            } else {
                showNativeHome()
                webView.loadUrl("https://www.madtiy.net")
            }
        } else {
            showNativeHome()
            webView.loadUrl("https://www.madtiy.net")
        }
    }

    private fun buildNativeHomeScreen() {
        nativeHomeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F7F9F8"))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(primaryColor)
            setPadding(22, 24, 22, 24)
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "مادتي"
            setTextColor(Color.WHITE)
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        val shareButton = TextView(this).apply {
            text = "مشاركة"
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(16, 10, 16, 10)
            setOnClickListener {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "https://www.madtiy.net")
                }
                startActivity(Intent.createChooser(sendIntent, "مشاركة موقع مادتي"))
            }
        }
        header.addView(title)
        header.addView(shareButton)

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(16, 12, 16, 12)
            gravity = Gravity.CENTER
        }

        val term1Button = makeTermButton("الفصل الأول", true)
        val term2Button = makeTermButton("الفصل الثاني", false)
        term1Button.setOnClickListener {
            currentSemester = 1
            updateTermButtonStyles(term1Button, term2Button)
            showGrades(dataTerm1)
        }
        term2Button.setOnClickListener {
            currentSemester = 2
            updateTermButtonStyles(term2Button, term1Button)
            showGrades(dataTerm2)
        }
        tabs.addView(term1Button)
        tabs.addView(term2Button)

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            isFillViewport = true
        }

        contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 18, 16, 28)
        }
        scroll.addView(contentContainer)

        nativeHomeLayout.addView(header)
        nativeHomeLayout.addView(tabs)
        nativeHomeLayout.addView(scroll)

        showGrades(dataTerm1)
    }

    private fun makeTermButton(label: String, selected: Boolean): Button {
        return Button(this).apply {
            text = label
            setTextColor(if (selected) Color.WHITE else Color.BLACK)
            setBackgroundColor(if (selected) primaryColor else Color.LTGRAY)
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply {
                setMargins(5, 0, 5, 0)
            }
        }
    }

    private fun updateTermButtonStyles(selected: Button, other: Button) {
        selected.setBackgroundColor(primaryColor)
        selected.setTextColor(Color.WHITE)
        other.setBackgroundColor(Color.LTGRAY)
        other.setTextColor(Color.BLACK)
    }

    private fun showGrades(data: List<Grade>) {
        currentGrade = null
        contentContainer.removeAllViews()
        addSectionHeading("اختر الصف الدراسي", "حدد صفك للوصول إلى التصنيفات والمواد")
        for (i in data.indices step 2) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                    setMargins(0, 0, 0, 16)
                }
            }
            row.addView(createImageCard(data[i].name, data[i].imageUrl) {
                showCategories(data[i])
            })
            if (i + 1 < data.size) {
                row.addView(createImageCard(data[i + 1].name, data[i + 1].imageUrl) {
                    showCategories(data[i + 1])
                })
            } else {
                row.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
            }
            contentContainer.addView(row)
        }
    }

    private fun showCategories(grade: Grade) {
        currentGrade = grade
        contentContainer.removeAllViews()

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, 0, 0, 14)
            }
        }
        val back = TextView(this).apply {
            text = "‹"
            textSize = 32f
            gravity = Gravity.CENTER
            setTextColor(primaryColor)
            layoutParams = LinearLayout.LayoutParams(48, 48)
            setOnClickListener { showGrades(if (currentSemester == 1) dataTerm1 else dataTerm2) }
        }
        val heading = TextView(this).apply {
            text = grade.name
            textSize = 19f
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }
        topRow.addView(back)
        topRow.addView(heading)
        contentContainer.addView(topRow)
        addSectionHeading(
            if (currentSemester == 1) "تصنيفات الفصل الأول" else "تصنيفات الفصل الثاني",
            "اختر المادة لعرض ملفاتها التعليمية"
        )

        val categories = categoriesForGrade(grade)
        for (i in categories.indices step 2) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                    setMargins(0, 0, 0, 16)
                }
            }
            row.addView(createImageCard(categories[i].name, categories[i].imageUrl) {
                openCategory(grade, categories[i])
            })
            if (i + 1 < categories.size) {
                row.addView(createImageCard(categories[i + 1].name, categories[i + 1].imageUrl) {
                    openCategory(grade, categories[i + 1])
                })
            } else {
                row.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
            }
            contentContainer.addView(row)
        }
    }

    private fun addSectionHeading(title: String, subtitle: String) {
        val heading = TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.parseColor("#252525"))
            setTypeface(null, Typeface.BOLD)
        }
        val sub = TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(Color.GRAY)
            setPadding(0, 5, 0, 16)
        }
        contentContainer.addView(heading)
        contentContainer.addView(sub)
    }

    private fun createImageCard(
        title: String,
        imageUrl: String,
        onClick: () -> Unit
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply {
                setMargins(7, 0, 7, 0)
            }
            setPadding(5, 5, 5, 12)
            elevation = 3f
            setOnClickListener { onClick() }
        }

        val imageFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(118))
            setBackgroundColor(Color.parseColor("#F1F4F3"))
        }

        val imageView = ImageView(this@MainActivity).apply {
            layoutParams = FrameLayout.LayoutParams(-1, -1)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = title
        }
        imageFrame.addView(imageView)
        loadImageFromUrl(imageUrl, imageView)

        val label = TextView(this).apply {
            text = title
            textSize = 13f
            setTextColor(Color.parseColor("#333333"))
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setPadding(2, 10, 2, 0)
        }

        card.addView(imageFrame)
        card.addView(label)
        return card
    }

    private fun loadImageFromUrl(imageUrl: String, imageView: ImageView) {
        imageExecutor.execute {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(imageUrl.replace(" ", "%20")).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 12000
                    readTimeout = 12000
                    doInput = true
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                    connect()
                }
                val bitmap = connection.inputStream.use { BitmapFactory.decodeStream(it) }
                runOnUiThread {
                    if (bitmap != null && !isFinishing) imageView.setImageBitmap(bitmap)
                    else imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } catch (_: Exception) {
                runOnUiThread {
                    if (!isFinishing) imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun openCategory(grade: Grade, category: Category) {
        val term = if (currentSemester == 1) "الفصل الدراسي الأول" else "الفصل الدراسي الثاني"
        val query = "${category.query} ${grade.name} $term"
        val encodedQuery = Uri.encode(query)
        showWebview()
        webView.loadUrl("https://www.madtiy.net/?s=$encodedQuery")
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun showNativeHome() {
        nativeHomeLayout.visibility = View.VISIBLE
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showWebview() {
        nativeHomeLayout.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }

    private fun handleUrl(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase()
        val host = uri.host?.lowercase()

        if (host != null && (host == websiteHost || host.endsWith(".$websiteHost"))) {
            showWebview()
            return false
        }

        if (scheme == "file" || scheme == "javascript") return true

        return try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "لا يوجد تطبيق لفتح هذا الرابط", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun openDownload(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String?
    ) {
        try {
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                if (!mimeType.isNullOrBlank()) setMimeType(mimeType)
                addRequestHeader("User-Agent", userAgent)
                addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url) ?: "")
                setTitle(fileName)
                setDescription("جاري تنزيل الملف...")
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(this, "بدأ التحميل...", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, "حدث خطأ أثناء التحميل", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        outState.putInt("currentSemester", currentSemester)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.visibility == View.VISIBLE) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                showNativeHome()
            }
        } else if (currentGrade != null) {
            showGrades(if (currentSemester == 1) dataTerm1 else dataTerm2)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        imageExecutor.shutdownNow()
        super.onDestroy()
    }
}
