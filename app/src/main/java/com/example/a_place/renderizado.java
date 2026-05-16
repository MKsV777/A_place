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
    private int ShaderPrograma;

    private final float[] panel = {
            -0.5f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.5f,  0.5f, 0.0f
    };

    private final String codigoShader =
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "gl_Position = vPosition;" +
                    "}";

    private final String fragmentoshader =
                    "precision mediump float;" +
                    "uniform vec4 uColor;" +
                    "void main(){" +
                    "   gl_fragmentrColor = uColor;" +
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

        GLES20.glUseProgram(ShaderPrograma);

        int posicionman = GLES20.glGetAttribLocation(ShaderPrograma, "vPosition");
        GLES20.glEnableVertexAttribArray(posicionman);
        GLES20.glVertexAttribPointer(posicionman, 3, GLES20.GL_FLOAT, false, 0, buffervertices);

        // you will see -man everywhere i short manejador to "-man" in spanish wich means handle

        int colorman = GLES20.glGetUniformLocation(ShaderPrograma, "uColor");
        if(paneltoque){
            GLES20.glUniform4f(colorman, 0.0f, 1.0f, 0.0f, 1.0f);
        }else {
            GLES20.glUniform4f(colorman, 0.0f, 1.0f, 0.88f, 1.0f);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisableVertexAttribArray(posicionman);
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
