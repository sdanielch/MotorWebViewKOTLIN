package es.isa.mallorcamoves

import android.Manifest
import android.Manifest.permission.*
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.webkit.*
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebChromeClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build
import android.webkit.PermissionRequest
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import android.net.http.SslError
import android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE
import android.webkit.SslErrorHandler
import java.security.Permission
import kotlin.arrayOf as arrayOf1


class MainActivity : AppCompatActivity() {

    private val BASE_URL = "https://h5p.org/audio-recorder"
    private val USER_AGENT =
        "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36"
    private val permisos = arrayOf1(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        RECORD_AUDIO,
        Manifest.permission.ACCESS_MEDIA_LOCATION,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.CAPTURE_AUDIO_OUTPUT
    )

    //private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        // Preguntamos por los permisos si no los tenemos
        ActivityCompat.requestPermissions(this, permisos, 0);

    }

    private var myRequest: PermissionRequest? = null

    override fun onStart() {
        super.onStart()
        if (hasNoPermissions()) {
            // Le decimos al usuario que faltan permisos y que debe activarlos
            Toast.makeText(
                applicationContext,
                "Faltan permisos en la APP activa todos los permisos de la APP",
                Toast.LENGTH_SHORT
            ).show()
            requestPermission()
        } else {
            // Le decimos al usuario que los permisos ya están listos y configurados
            Toast.makeText(applicationContext, ";)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        webView.webChromeClient = object : WebChromeClient() {


            override fun onPermissionRequest(request: PermissionRequest) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.resources)
                }
            }


        }


        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }


            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.proceed() // Ignore SSL certificate errors
            }


        }

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.allowFileAccessFromFileURLs = true
        //settings.setAppCacheEnabled(false)
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setAppCachePath(cacheDir.path)
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.allowUniversalAccessFromFileURLs = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.loadWithOverviewMode = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.blockNetworkLoads = false
        settings.setUserAgentString(USER_AGENT)
        settings.safeBrowsingEnabled = true
        webView.loadUrl(BASE_URL)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true  // api 26
        }

        settings.setGeolocationEnabled(true)


        // TODA ESTA PARTE DE AQUI ES PARA PODER SUBIR FICHEROS AL SERVIDOR
        webView.setWebChromeClient(object : WebChromeClient() {


            //PARTE DE CHROME

            override fun onPermissionRequest(request: PermissionRequest?) {
                if (request?.resources!!.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE) || request?.resources!!.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)){
                    request.grant(request.resources)
                }
            }



            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                //var mFilePathCallback = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setType("*/*")
                val PICKFILE_REQUEST_CODE = 100
                startActivityForResult(intent, PICKFILE_REQUEST_CODE)
                return true
            }


        })


        // ESTO ES POR SI EN ALGUN MOMENTO EL USUARIO DEBE DESCARGAR UN FICHERO
        webView.setDownloadListener(object : DownloadListener {
            override fun onDownloadStart(
                url: String, userAgent: String,
                contentDisposition: String, mimetype: String,
                contentLength: Long
            ) {
                val request = DownloadManager.Request(Uri.parse(url))
                request.allowScanningByMediaScanner()

                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mimetype)
                val webview = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                webview.enqueue(request)
                Toast.makeText(getApplicationContext(), "Descargando fichero...", Toast.LENGTH_LONG)
                    .show()
            }
        })


    }

    // Esto es para que si pulsamos para atrás no se nos salga de la app
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }


}


