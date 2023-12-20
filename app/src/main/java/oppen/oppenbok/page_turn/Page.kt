package oppen.oppenbok.page_turn

import android.content.Context
import android.content.res.Configuration
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL10

const val RADIUS = 0.18f
const val GRID = 25

/**
 *
 * Original Java source created by karacken on 18/11/16
 * https://github.com/karankalsi/PlayLikeCurl
 *
 * Converted to Kotlin and refactored by Oppenlab 2021
 *
 */
open class Page(screen_width: Int) {

    var curlCirclePosition = 25f
    private var bitmapRatio = 1.0f
    private var screenWidth = 0

    private var active = false
    var vertexBuffer: FloatBuffer? = null
    private val textureBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer

    private var resourceLoader: ResourceLoader? = null
    private var pageNumber = -1
    private var updateTexture = false

    private val textures = IntArray(1)
    var vertices = FloatArray((GRID + 1) * (GRID + 1) * 3)
    private val texture = FloatArray((GRID + 1) * (GRID + 1) * 2)
    private val indices = ShortArray(GRID * GRID * 6)

    var hWRatio = 0f
    var hWCorrection = 0f

    open val depth = 0f

    init {
        this.screenWidth = screen_width
        calculateFacesCoords()
        calculateTextureCoords()
        var byteBuf = ByteBuffer.allocateDirect(texture.size * 4)
        byteBuf.order(ByteOrder.nativeOrder())
        textureBuffer = byteBuf.asFloatBuffer()
        textureBuffer.put(texture)
        textureBuffer.position(0)
        byteBuf = ByteBuffer.allocateDirect(indices.size * 4)
        byteBuf.order(ByteOrder.nativeOrder())
        indexBuffer = byteBuf.asShortBuffer()
        indexBuffer.put(indices)
        indexBuffer.position(0)
    }

    fun setResourceId(pageNumber: Int, resourceLoader: ResourceLoader?) {
        this.pageNumber = pageNumber
        this.resourceLoader = resourceLoader
        updateTexture = true
    }

    fun isActive(): Boolean = active

    fun setActive(active: Boolean) {
        this.active = active
    }

    open fun calculateVerticesCoords() {
        hWRatio = bitmapRatio
        hWCorrection = (hWRatio - 1f) / 2f
        //println(">>__: hWRatio: $hWRatio bitmapRatio: $bitmapRatio hWCorrection: $hWCorrection")
    }

    private fun calculateFacesCoords() {
        for (row in 0 until GRID) for (col in 0 until GRID) {
            val pos = 6 * (row * GRID + col)
            indices[pos] = (row * (GRID + 1) + col).toShort()
            indices[pos + 1] = (row * (GRID + 1) + col + 1).toShort()
            indices[pos + 2] = ((row + 1) * (GRID + 1) + col).toShort()
            indices[pos + 3] = (row * (GRID + 1) + col + 1).toShort()
            indices[pos + 4] = ((row + 1) * (GRID + 1) + col + 1).toShort()
            indices[pos + 5] = ((row + 1) * (GRID + 1) + col).toShort()
        }
    }

    private fun calculateTextureCoords() {
        for (row in 0..GRID) for (col in 0..GRID) {
            val pos = 2 * (row * (GRID + 1) + col)
            texture[pos] = col / GRID.toFloat()
            texture[pos + 1] = 1 - row / GRID.toFloat()
        }
    }

    fun draw(gl: GL10, context: Context) {
        if (updateTexture) {
            updateTexture = false
            loadGLTexture(gl, context)
        }

        calculateVerticesCoords()
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glFrontFace(GL10.GL_CCW)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer)
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer)
        gl.glDrawElements(GL10.GL_TRIANGLES, indices.size, GL10.GL_UNSIGNED_SHORT, indexBuffer)
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
    }

    fun loadGLTexture(gl: GL10, context: Context) {
        if (pageNumber == -1) return

        resourceLoader?.loadBitmap(pageNumber) { bitmap ->
            if(bitmap == null) return@loadBitmap

            bitmapRatio = when (context.resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> bitmap.height.toFloat() / bitmap.width.toFloat()
                else -> bitmap.width.toFloat() / bitmap.height.toFloat()
            }

            gl.glGenTextures(1, textures, 0)
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT.toFloat())
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT.toFloat())
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)

            bitmap.recycle()

        }
    }
}