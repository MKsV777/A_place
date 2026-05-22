package com.example.a_place

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// no te olvides de hacerle commit cada hora
class MainActivity : AppCompatActivity() {
    private var glSurfaceView: GLSurfaceView? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        glSurfaceView = GLSurfaceView(this)

        glSurfaceView!!.setEGLContextClientVersion(2)

        glSurfaceView!!.setEGLContextClientVersion(2)

        val renderer = renderizado(this)
        glSurfaceView!!.setRenderer(renderer)
        renderer.nuevopanel("https://www.google.com", 0f, 0f)

        setContentView(glSurfaceView)

        glSurfaceView!!.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            val accion = event!!.getAction()
            val xd = event.getX()
            val yd = event.getY()

            // ADD THIS LINE HERE MANUALLY:
            android.util.Log.d("TouchDebug", "Touch: $xd, $yd")

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


        ViewCompat.setOnApplyWindowInsetsListener(glSurfaceView!!) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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