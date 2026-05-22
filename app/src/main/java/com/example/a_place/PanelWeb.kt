package com.example.a_place

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.view.Surface
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient

class PanelWeb(val context: Context, val url: String, val width: Int, val height: Int) {
    var textureId: Int = -1
    var surfaceTexture: SurfaceTexture? = null
    var surface: Surface? = null
    var webView: WebView? = null
    var xOffset: Float = 0f
    var yOffset: Float = 0f

    init {
        (context as Activity).runOnUiThread {
            webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                layout(0, 0, width, height)
                measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
                webViewClient = WebViewClient()

                // FIX: Use the class property explicitly
                loadUrl(this@PanelWeb.url)
            }
        }
    }

    fun iniciarTextura() {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        textureId = tex[0]

        // GL_TEXTURE_EXTERNAL_OES (0x8D65)
        val type = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        GLES20.glBindTexture(type, textureId)

        // parametros para prevenir pantalla negra
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture?.setDefaultBufferSize(width, height)
        surface = Surface(surfaceTexture)
    }

    // Inside PanelWeb.kt
    var needsUpdate = false // Add this flag

    fun update() {
        (context as? Activity)?.runOnUiThread {
            try {
                val canvas = surface?.lockCanvas(null)
                if (canvas != null) {
                    webView?.draw(canvas)
                    surface?.unlockCanvasAndPost(canvas)
                    needsUpdate = true // Signal that new data is ready
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Add this function to be called in onDrawFrame
    fun updateTexture() {
        if (needsUpdate) {
            try {
                surfaceTexture?.updateTexImage()

                needsUpdate = false // Reset flag
            } catch (e: Exception) {}
        }
    }
}