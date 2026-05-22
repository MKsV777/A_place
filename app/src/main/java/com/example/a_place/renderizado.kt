package com.example.a_place

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.Surface
import java.nio.ByteBuffer
import android.content.Context
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class renderizado(val context: Context) : GLSurfaceView.Renderer {
    private var buffervertices: FloatBuffer? = null
    private var bufferTexturas: FloatBuffer? = null


    private var ShaderPrograma = 0





    private val panel = floatArrayOf(
        -0.5f,  0.5f, 0.0f, // Top Left
        -0.5f, -0.5f, 0.0f, // Bottom Left
        0.5f, -0.5f, 0.0f, // Bottom Right

        -0.5f,  0.5f, 0.0f, // Top Left
        0.5f, -0.5f, 0.0f, // Bottom Right
        0.5f,  0.5f, 0.0f  // Top Right
    )

    private val paneluv = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,

        0.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )

    fun nuevopanel(url: String, x: Float, y: Float){
        val panelnuevo = PanelWeb(context, url, 1280, 720)
        panelnuevo.xOffset = x
        panelnuevo.yOffset = y
        paneles.add(panelnuevo)
    }


    private val codigoShader = """
        attribute vec4 vPosition;
        attribute vec2 vTexCoord;
        uniform vec2 uOffset;
        varying vec2 vTexCoordOut;
        void main() {
            // Apply the offset to the position
            gl_Position = vec4(vPosition.x + uOffset.x, vPosition.y + uOffset.y, vPosition.z, vPosition.w);
            vTexCoordOut = vTexCoord;
        }
    """.trimIndent()

    private val fragmentoshader = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTexCoordOut; // MUST match the output name exactly
        uniform samplerExternalOES sTexture;
        uniform vec4 uColor;
        
        void main() {
           gl_FragColor = texture2D(sTexture, vTexCoordOut) * uColor;
        }
    """.trimIndent()


    private var paneltoque = false
    private val paneles = mutableListOf<PanelWeb>()
    private val pantallaAncho = 1f
    private val pantallaAlto = 1f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        val bb = ByteBuffer.allocateDirect(panel.size * 4)

        bb.order(ByteOrder.nativeOrder())

        buffervertices = bb.asFloatBuffer()
        buffervertices!!.put(panel)
        buffervertices!!.position(0)

        val bbTex = ByteBuffer.allocateDirect(paneluv.size * 4)
        bbTex.order(ByteOrder.nativeOrder())
        bufferTexturas = bbTex.asFloatBuffer()
        bufferTexturas!!.put(paneluv)
        bufferTexturas!!.position(0)

        val shader = cargashade(GLES20.GL_VERTEX_SHADER, codigoShader)
        val fragmentoshaderman = cargashade(GLES20.GL_FRAGMENT_SHADER, fragmentoshader)

        ShaderPrograma = GLES20.glCreateProgram()

        GLES20.glAttachShader(ShaderPrograma, shader)
        GLES20.glAttachShader(ShaderPrograma, fragmentoshaderman)
        GLES20.glLinkProgram(ShaderPrograma)
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(ShaderPrograma)


        val posicionman = GLES20.glGetAttribLocation(ShaderPrograma, "vPosition")
        GLES20.glEnableVertexAttribArray(posicionman)
        GLES20.glVertexAttribPointer(posicionman, 3, GLES20.GL_FLOAT, false, 0, buffervertices)

        val uvHandle = GLES20.glGetAttribLocation(ShaderPrograma, "vTexCoord")
        GLES20.glEnableVertexAttribArray(uvHandle)
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, bufferTexturas)

        val offsetHandle = GLES20.glGetUniformLocation(ShaderPrograma, "uOffset")
        val colorman = GLES20.glGetUniformLocation(ShaderPrograma, "uColor")


        paneles.forEach { panel ->
            if (panel.textureId == -1) {
                panel.iniciarTextura()
            }

            panel.update()
            panel.updateTexture()

            GLES20.glUniform2f(offsetHandle, panel.xOffset, panel.yOffset)
            GLES20.glUniform4f(colorman, 1.0f, 1.0f, 1.0f, 1.0f)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(0x8D65, panel.textureId)
            GLES20.glUniform1i(GLES20.glGetUniformLocation(ShaderPrograma, "sTexture"), 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        }

        GLES20.glDisableVertexAttribArray(posicionman)
        GLES20.glDisableVertexAttribArray(uvHandle)
    }

    fun actualizartoque(x: Float, y: Float, toque: Boolean) {
        if (!paneltoque) {
            paneltoque = true
            return
        }
        val normalglx = (x / pantallaAncho) * 2.0f - 1.0f
        val normalgly = (y / pantallaAlto) * 2.0f - 1.0f

        if (normalglx >= -0.5f && normalglx <= 0.5f && normalgly >= -0.5f && normalgly <= 0.5f) {
            paneltoque = true

            val webviewX = (normalglx + 0.5f)
            val webviewY = (normalgly + 0.5f)
        } else {
            paneltoque = false
        }
    }

    fun cargashade(tipo: Int, codigo: String): Int {
        val shader = GLES20.glCreateShader(tipo)
        GLES20.glShaderSource(shader, codigo)
        GLES20.glCompileShader(shader)
        return shader
    }
}
