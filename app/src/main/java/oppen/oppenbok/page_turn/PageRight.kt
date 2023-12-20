package oppen.oppenbok.page_turn

import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 *
 * Original Java source created by karacken on 18/11/16
 * https://github.com/karankalsi/PlayLikeCurl
 *
 * Converted to Kotlin and refactored by Oppenlab 2021
 *
 */
class PageRight(screen_width: Int) : Page(screen_width) {

    override val depth = -0.003f

    override fun calculateVerticesCoords() {
        super.calculateVerticesCoords()

        for (row in 0..GRID) for (col in 0..GRID) {
            val pos = 3 * (row * (GRID + 1) + col)
            if (!isActive()) vertices[pos + 2] = depth
            vertices[pos] = col.toFloat() / GRID.toFloat()
            vertices[pos + 1] = row.toFloat() / GRID.toFloat() * hWRatio - hWCorrection
        }
        val byteBuf = ByteBuffer.allocateDirect(vertices.size * 4)
        byteBuf.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuf.asFloatBuffer()
        vertexBuffer?.put(vertices)
        vertexBuffer?.position(0)
    }
}