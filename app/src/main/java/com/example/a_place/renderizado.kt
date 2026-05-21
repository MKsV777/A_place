package com.example.a_place;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.nio.FloatBuffer;



import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



public class renderizado implements GLSurfaceView.Renderer {
    private FloatBuffer buffervertices;
    private FloatBuffer bufferTexturas;


    private int idTexturaOES;
    private int ShaderPrograma;
    private android.graphics.SurfaceTexture mSurfaceTexture;
    private android.view.Surface mSurface;

    private android.graphics.SurfaceTexture surfaceTexture;


    private final float[] panel = {
            -0.5f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.5f,  0.5f, 0.0f
    };

    private final float[] paneluv = {
            0.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    public android.view.Surface obtenerSuperficie(){
        return mSurface;
    }

    private final String codigoShader =
            "attribute vec4 vPosition;" +
                    "atribute vec4 vTexCoord;"+ // aca por lo que tengo entendido tengo atribute vec4 almacena 4 cosas en una variable
                    "varying vec2 TexCoord;" +// lo mismo aqui
                    "void main() {" + // iniciamos main
                    "gl_Position = vPosition;" + // definimos la posicion
                    "TextCoord = vTexCoord"+ // definimos el stream que se enviara a el texture 2D
                    "}";

    private final String fragmentoshader =
            "#extension GL_OES_EGL_image_external : require\\n"+ // inicia la libreria creo de imagen externa
                    "precision mediump float;" +
                    "varying vec2 TexCoord;" +
                    "uniform samplerExternalOES sTexture;" + // inicia lo que al final le daremos a el panel para que se muestre como textura
                    "void main(){" +
                    "   gl_FragColor = texture2D(sTexture, TexCoord);" + // le damos la textura y el stream de datos que definimos anteriormente es para la imagen externa
                    "}";


    private boolean paneltoque = false;

    private float pantallaAncho = 1f;
    private float pantallaAlto = 1f;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        ByteBuffer bb = ByteBuffer.allocateDirect(panel.length * 4);

        bb.order(ByteOrder.nativeOrder());

        buffervertices = bb.asFloatBuffer();
        buffervertices.put(panel);
        buffervertices.position(0);

        ByteBuffer bbTex = ByteBuffer.allocateDirect(paneluv.length * 4);
        bbTex.order(ByteOrder.nativeOrder());
        bufferTexturas = bbTex.asFloatBuffer();
        bufferTexturas.put(paneluv);
        bufferTexturas.position(0);

        int[] texturas = new int[1];
        GLES20.glGenTextures(1, texturas, 0);
        idTexturaOES = texturas[0];

        GLES20.glBindTexture(0x8D65, idTexturaOES); // 0x8D65 es aparentemente GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        GLES20.glTexParameterf(0x8D65, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(0x8D65, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        mSurfaceTexture = new android.graphics.SurfaceTexture(idTexturaOES);
        mSurfaceTexture.setDefaultBufferSize(1280, 720);
        mSurface = new android.view.Surface(mSurfaceTexture);

        int shader = cargashade(GLES20.GL_VERTEX_SHADER, codigoShader);
        int fragmentoshaderman = cargashade(GLES20.GL_FRAGMENT_SHADER, fragmentoshader);

        ShaderPrograma = GLES20.glCreateProgram();

        GLES20.glAttachShader(ShaderPrograma, shader);
        GLES20.glAttachShader(ShaderPrograma, fragmentoshaderman);
        GLES20.glLinkProgram(ShaderPrograma);


    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mSurfaceTexture != null) {
            try { mSurfaceTexture.updateTexImage(); } catch (Exception ignored) {}
        }

        GLES20.glUseProgram(ShaderPrograma);


        int posicionman = GLES20.glGetAttribLocation(ShaderPrograma, "vPosition");
        GLES20.glEnableVertexAttribArray(posicionman);
        GLES20.glVertexAttribPointer(posicionman, 3, GLES20.GL_FLOAT, false, 0, buffervertices);

        int uvHandle = GLES20.glGetAttribLocation(ShaderPrograma, "vTexCoord");
        GLES20.glEnableVertexAttribArray(uvHandle);
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, bufferTexturas);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(0x8D65, idTexturaOES);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(ShaderPrograma, "sTexture"), 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        // you will see -man everywhere i short manejador to "-man" in spanish wich means handle

        int colorman = GLES20.glGetUniformLocation(ShaderPrograma, "uColor");
        if(paneltoque){
            GLES20.glUniform4f(colorman, 0.0f, 1.0f, 0.0f, 1.0f);
        }else {
            GLES20.glUniform4f(colorman, 0.0f, 1.0f, 0.88f, 1.0f);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        GLES20.glDisableVertexAttribArray(posicionman);
        GLES20.glDisableVertexAttribArray(uvHandle);
    }

    public void actualizartoque(float x, float y, boolean toque){
        if(!paneltoque){
            paneltoque = true;
            return;
        }
        float normalglx = (x / pantallaAncho) * 2.0f - 1.0f;
        float normalgly = (y / pantallaAlto) * 2.0f - 1.0f;

        if(normalglx >= -0.5f && normalglx <= 0.5f && normalgly >= -0.5f && normalgly <= 0.5f){
            paneltoque = true;

            float webviewX = (normalglx + 0.5f);
            float webviewY = (normalgly + 0.5f);

        }
        else{
            paneltoque = false;
        }

    }

    public int cargashade(int tipo, String codigo) {
        int shader = GLES20.glCreateShader(tipo);
        GLES20.glShaderSource(shader, codigo);
        GLES20.glCompileShader(shader);
        return shader;
    }

}
