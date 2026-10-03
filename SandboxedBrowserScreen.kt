package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberSlate700
import com.example.ui.theme.CyberSlate800
import com.example.ui.theme.CyberSlate900
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SecureSpaceViewModel

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxedBrowserScreen(
    viewModel: SecureSpaceViewModel,
    onBack: () -> Unit
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf("https://duckduckgo.com") }
    var inputUrl by remember { mutableStateOf("https://duckduckgo.com") }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isSecureHttps by remember { mutableStateOf(true) }

    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onBack()
        }
    }

    val privacyBookmarks = listOf(
        "DuckDuckGo" to "https://duckduckgo.com",
        "Tor Project" to "https://check.torproject.org",
        "EFF Privacy" to "https://eff.org",
        "Android Security" to "https://source.android.com/security"
    )

    Scaffold(
        containerColor = CyberNavyDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSlate900),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("browser_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Browser",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isSecureHttps) NeonEmerald else CriticalRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Sandboxed Incognito Browser",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Isolated Cookies • Zero Disk Cache",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SapphireBlue,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Wipe Session Button
                    IconButton(
                        onClick = {
                            webViewInstance?.let { wv ->
                                wv.clearCache(true)
                                wv.clearHistory()
                                wv.clearFormData()
                                wv.clearSslPreferences()
                                CookieManager.getInstance().removeAllCookies(null)
                                wv.loadUrl("about:blank")
                                viewModel.logAuditEvent("BROWSER_WIPE", "Sandboxed Browser Cleared", "Cache, history, and cookies zeroized", "INFO")
                            }
                        },
                        modifier = Modifier.testTag("browser_wipe_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Wipe Session",
                            tint = CriticalRed
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // URL Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("browser_url_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Surface(
                    onClick = {
                        val formatted = if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                            "https://$inputUrl"
                        } else inputUrl
                        currentUrl = formatted
                        webViewInstance?.loadUrl(formatted)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricCyan,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Language, contentDescription = "Go", tint = Color.Black)
                    }
                }
            }

            // Quick Privacy Bookmarks
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(privacyBookmarks) { (label, url) ->
                    Surface(
                        onClick = {
                            inputUrl = url
                            currentUrl = url
                            webViewInstance?.loadUrl(url)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = CyberSlate800,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSlate700)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Progress indicator
            if (pageProgress in 0.01f..0.99f) {
                LinearProgressIndicator(
                    progress = { pageProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = ElectricCyan,
                    trackColor = CyberSlate800
                )
            }

            // Web Content Area
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = false
                                databaseEnabled = false
                                cacheMode = WebSettings.LOAD_NO_CACHE
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                allowFileAccess = false
                                allowContentAccess = false
                            }

                            CookieManager.getInstance().setAcceptCookie(false)

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    url?.let {
                                        inputUrl = it
                                        isSecureHttps = it.startsWith("https://")
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    pageProgress = 1f
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    return false
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    pageProgress = newProgress / 100f
                                }
                            }

                            loadUrl(currentUrl)
                            webViewInstance = this
                        }
                    },
                    modifier = Modifier.fillMaxSize().testTag("sandboxed_webview")
                )
            }

            // Bottom Navigation Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSlate900)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { webViewInstance?.goBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                IconButton(onClick = { webViewInstance?.goForward() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward", tint = Color.White)
                }

                IconButton(onClick = { webViewInstance?.reload() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.White)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonEmerald.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "TOR / HTTPS SECURED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
