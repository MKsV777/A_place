package com.example.a_place;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PanelWeb {
    public int textureId = -1;
    public float xOffset = 0f;
    public float yOffset = 0f;

    private final Context context;
    private final String url;
    private final int altura;
    private final int anchura;

    private WebView webView;
    private boolean isReady = false;
    private Bitmap pendingBitmap = null;
    private final Object bitmapLock = new Object();
    private boolean needsUpdate = false;

    public PanelWeb(Context context, String url, int width, int height) {
        this.context = context;
        this.url = url;
        this.altura = width;
        this.anchura = height;

        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView = new WebView(context);
                    webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                    webView.getSettings().setJavaScriptEnabled(true);
                    //webView.getSettings().setDomStorageEnabled(true); no creo que se ocupa la verdad
                    webView.getSettings().setUseWideViewPort(true);
                    webView.getSettings().setLoadWithOverviewMode(true);

                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String urlString) {
                            isReady = true;
                            Log.d("PanelWeb", "CARGO !!! " + urlString);
                            triggerSnapshot();
                        }
                    });

                    webView.layout(0, 0, width, height); // aver aca segun yo se pone en el ui talvez me quivoque
                    int specancho = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
                    int specalto = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                    webView.measure(specancho, specalto);

                    webView.loadUrl(url);
                }
            });
        }
    }

    public void iniciarTextura() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        textureId = tex[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        Log.d("PanelWeb", "GL_TEXTURE_2D inicio con ID: " + textureId);
    }

    private void triggerSnapshot() {
        if (!isReady || webView == null) return;

        try {
            Bitmap bmp = Bitmap.createBitmap(anchura, altura, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(Color.WHITE);
            webView.draw(canvas);

            synchronized (bitmapLock) {
                if (pendingBitmap != null) {
                    pendingBitmap.recycle();
                }
                pendingBitmap = bmp;
                needsUpdate = true;
            }
        } catch (Exception e) {
            Log.e("PanelWeb", "error al convertir el webview a bitmap: " + e.getMessage(), e);
        }
    }

    public void update() {
        if (isReady && !needsUpdate) {
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        triggerSnapshot();
                    }
                });
            }
        }
    }

    public void updateTexture() {
        if (textureId == -1) return;

        Bitmap bmp = null;
        synchronized (bitmapLock) {
            if (needsUpdate) {
                bmp = pendingBitmap;
                pendingBitmap = null;
                needsUpdate = false;
            }
        }

        if (bmp != null) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            bmp.recycle();
        }
    }
}