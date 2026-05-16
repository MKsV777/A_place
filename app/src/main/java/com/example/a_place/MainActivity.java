package com.example.a_place;

import android.os.Bundle;

import android.opengl.GLSurfaceView;


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