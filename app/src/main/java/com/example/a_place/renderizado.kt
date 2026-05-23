package com.example.a_place

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import android.content.Context
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.Matrix

class renderizado(val context: Context) : GLSurfaceView.Renderer {
    private var buffervertices: FloatBuffer? = null
    private var bufferTexturas: FloatBuffer? = null
    private var ShaderPrograma = 0
    private var anchopantalla = 1920f
    private var altopantalla = 1080f

    private val modelMatrix = FloatArray(16)
    private val panel = floatArrayOf(
        -0.5f,  0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,

        -0.5f,  0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.5f,  0.5f, 0.0f
    )

    private val paneluv = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,

        0.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )

    // estamos usando Standard sampler2D
    private val codigoShader = """
        attribute vec4 vPosition;
        attribute vec2 vTexCoord;
        uniform mat4 uMatrix;
        varying vec2 vTexCoordOut;
        void main() {
            gl_Position = uMatrix * vPosition;
            vTexCoordOut = vTexCoord;
        }
    """.trimIndent()

    private val fragmentoshader = """
        precision mediump float;
        varying vec2 vTexCoordOut;
        uniform sampler2D sTexture;
        uniform vec4 uColor;
        void main() {
            gl_FragColor = texture2D(sTexture, vTexCoordOut) * uColor;
        }
    """.trimIndent()

    private var paneltoque = false
    private val paneles = mutableListOf<PanelWeb>()

    fun nuevopanel(url: String, x: Float, y: Float) {
        val panelnuevo = PanelWeb(context, url, 1280, 720)
        panelnuevo.xOffset = x
        panelnuevo.yOffset = y
        paneles.add(panelnuevo)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        buffervertices = ByteBuffer.allocateDirect(panel.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .also { it.put(panel); it.position(0) }

        bufferTexturas = ByteBuffer.allocateDirect(paneluv.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .also { it.put(paneluv); it.position(0) }

        val vertShader = cargashade(GLES20.GL_VERTEX_SHADER, codigoShader)
        val fragShader = cargashade(GLES20.GL_FRAGMENT_SHADER, fragmentoshader)

        ShaderPrograma = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertShader)
            GLES20.glAttachShader(it, fragShader)
            GLES20.glLinkProgram(it)

            val status = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, status, 0)
            if (status[0] == 0) {
                android.util.Log.e("renderizado", "fallo algo checa linea 95 renderizado: ${GLES20.glGetProgramInfoLog(it)}")
            }
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        anchopantalla = width.toFloat()
        altopantalla = height.toFloat()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(ShaderPrograma)

        val posicionman = GLES20.glGetAttribLocation(ShaderPrograma, "vPosition")
        val uvHandle    = GLES20.glGetAttribLocation(ShaderPrograma, "vTexCoord")
        val matrixman = GLES20.glGetUniformLocation(ShaderPrograma, "uMatrix")
        val colorman     = GLES20.glGetUniformLocation(ShaderPrograma, "uColor")
        val textureHandle = GLES20.glGetUniformLocation(ShaderPrograma, "sTexture")

        GLES20.glEnableVertexAttribArray(posicionman)
        GLES20.glVertexAttribPointer(posicionman, 3, GLES20.GL_FLOAT, false, 0, buffervertices)

        GLES20.glEnableVertexAttribArray(uvHandle)
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, bufferTexturas)

        paneles.forEach { panel ->
            if (panel.textureId == -1) panel.iniciarTextura()

            panel.update()
            panel.updateTexture()

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, panel.xOffset, panel.yOffset, 0f)
            Matrix.rotateM(modelMatrix, 0, panel.rotation, 0f, 0f, 1.0f)
            GLES20.glUniformMatrix4fv(matrixman, 1, false, modelMatrix, 0)
            GLES20.glUniform4f(colorman, 1.0f, 1.0f, 1.0f, 1.0f)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0) // activamos la textura)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, panel.textureId) // ponemos la textura acompañada
            GLES20.glUniform1i(textureHandle, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6) // se dibuja los arreglos de los vertices osea el cuadrado o eso creo
            /*panel.update()
            panel.updateTexture()

            GLES20.glUniform2f(offsetHandle, panel.xOffset, panel.yOffset)
            GLES20.glUniform4f(colorman, 1.0f, 1.0f, 1.0f, 1.0f)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0) // activamos la textura
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, panel.textureId) // ponemos la textura acompañada
            GLES20.glUniform1i(textureHandle, 0)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6) // se dibuja los arreglos de los vertices osea el cuadrado o eso creo*/
        }

        GLES20.glDisableVertexAttribArray(posicionman)
        GLES20.glDisableVertexAttribArray(uvHandle)
    }

    fun actualizartoque(x: Float, y: Float, toque: Boolean) {
        if (!paneltoque) { paneltoque = true; return }
        val normalglx = (x / anchopantalla) * 2.0f - 1.0f
        val normalgly = (y / altopantalla) * 2.0f - 1.0f

        paneltoque = normalglx >= -0.5f && normalglx <= 0.5f &&
                normalgly >= -0.5f && normalgly <= 0.5f // si cae dentro de el panel toque!!!
    }

    fun cargashade(tipo: Int, codigo: String): Int {
        val shader = GLES20.glCreateShader(tipo)
        GLES20.glShaderSource(shader, codigo)
        GLES20.glCompileShader(shader)

        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            android.util.Log.e("renderizado", "fallo algo checa linea 159 renderizado: ${GLES20.glGetShaderInfoLog(shader)}")
        }
        return shader
    }
}