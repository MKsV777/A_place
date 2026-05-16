package com.example.a_place;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.Surface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// no te olvides de hacerle commit cada hora

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        glSurfaceView = new GLSurfaceView(this);

        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setEGLContextClientVersion(2);

        renderizado renderer = new renderizado();
        glSurfaceView.setRenderer(renderer);

        setContentView(glSurfaceView);

        iniciarpanelweb(renderer);

        glSurfaceView.setOnTouchListener((v, event) -> {
            int accion = event.getAction();
            float xd = event.getX(); // xdddddddd
            float yd = event.getY();
            switch (accion){
                case android.view.MotionEvent.ACTION_DOWN:
                case android.view.MotionEvent.ACTION_MOVE:
                    renderer.actualizartoque(xd, yd, true);

                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                        renderer.actualizartoque(0, 0, false);
                    break;
            }
            return true;

        });

        ViewCompat.setOnApplyWindowInsetsListener(glSurfaceView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void iniciarpanelweb(renderizado render) {
        runOnUiThread(() ->{
           WebView web = new WebView(this);
           web.getSettings().setJavaScriptEnabled(true);
           web.getSettings().setDomStorageEnabled(true);
           web.setWebViewClient(new WebViewClient());

            Runnable loopWeb = new Runnable() {
                @Override
                public void run() {
                    Surface superficie = render.obtenerSuperficie();

                    if (superficie != null && superficie.isValid()){
                        try {
                            Canvas canvas = superficie.lockCanvas(null);
                            web.draw(canvas);
                            superficie.unlockCanvasAndPost(canvas);
                        } catch (Exception ignored) {}
                    }
                    glSurfaceView.postDelayed(this, 33);
                }
            };


    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}