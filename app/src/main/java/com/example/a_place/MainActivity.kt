package com.example.a_place

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// no te olvides de hacerle commit cada hora
class MainActivity : AppCompatActivity() {
    private var web: WebView? = null
    private var glSurfaceView: GLSurfaceView? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        glSurfaceView = GLSurfaceView(this)

        glSurfaceView!!.setEGLContextClientVersion(2)

        glSurfaceView!!.setEGLContextClientVersion(2)

        val renderer = renderizado()
        glSurfaceView!!.setRenderer(renderer)

        setContentView(glSurfaceView)

        iniciarpanelweb(renderer)

        glSurfaceView!!.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            val accion = event!!.getAction()
            val xd = event.getX() // xdddddddd
            val yd = event.getY()
            when (accion) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> renderer.actualizartoque(
                    xd,
                    yd,
                    true
                )

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> renderer.actualizartoque(
                    0f,
                    0f,
                    false
                )
            }
            true
        })

        ViewCompat.setOnApplyWindowInsetsListener(
            glSurfaceView!!,
            OnApplyWindowInsetsListener { v: View?, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }) // android studio gave me this thing
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected fun iniciarpanelweb(render: renderizado) {
        runOnUiThread(Runnable {
            web = WebView(this)

            val anchoSpec = View.MeasureSpec.makeMeasureSpec(1280, View.MeasureSpec.EXACTLY)
            val altoSpec = View.MeasureSpec.makeMeasureSpec(720, View.MeasureSpec.EXACTLY)
            web!!.measure(anchoSpec, altoSpec)
            web!!.layout(0, 0, 1280, 720)
            web!!.settings.javaScriptEnabled = true
            web!!.settings.domStorageEnabled = true
            web!!.webViewClient = WebViewClient()
            web!!.loadUrl("https://www.google.com")
            val loopWeb: Runnable = object : Runnable {
                override fun run() {
                    val superficie = render.obtenerSuperficie()

                    if (superficie != null && superficie.isValid) {
                        try {
                            val canvas = superficie.lockCanvas(null)
                            if (canvas != null) {
                                web!!.draw(canvas)
                                superficie.unlockCanvasAndPost(canvas)
                            }
                        } catch (ignored: Exception) {}
                    }
                    glSurfaceView!!.postDelayed(this, 33)
                }
            }
            glSurfaceView!!.postDelayed(loopWeb, 500)
        })
    }


    override fun onResume() {
        super.onResume()
        glSurfaceView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView!!.onPause()
    }
}