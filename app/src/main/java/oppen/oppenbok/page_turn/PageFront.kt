package oppen.oppenbok.page_turn

import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

/**
 *
 * Original Java source created by karacken on 18/11/16
 * https://github.com/karankalsi/PlayLikeCurl
 *
 * Converted to Kotlin and refactored by Oppenlab 2021
 *
 */
class PageFront(screen_width: Int) : Page(screen_width) {

    override val depth = -0.002f

    override fun calculateVerticesCoords() {
        super.calculateVerticesCoords()

        for (row in 0..GRID) for (col in 0..GRID) {
            val pos = 3 * (row * (GRID + 1) + col)
            if (!isActive()) vertices[pos + 2] = depth
            val perc = 1.0f - curlCirclePosition / GRID.toFloat()
            val dx = GRID - curlCirclePosition

            var calc_r = RADIUS
            var mov_x = 0f

            when {
                perc < 0.20f -> calc_r = RADIUS * perc * 5
            }
            when {
                perc > 0.05f -> mov_x = perc - 0.05f
            }

            //Asin(2pi/wav*x)
            when {
                isActive() -> vertices[pos + 2] = (calc_r * sin(3.14 / (GRID * 0.60f) * (col - dx)) + calc_r * 1.1f).toFloat()
            }

            val wHRatio = 1 - calc_r
            vertices[pos] = col.toFloat() / GRID.toFloat() * wHRatio - mov_x
            vertices[pos + 1] = row.toFloat() / GRID.toFloat() * hWRatio - hWCorrection
        }
        val byteBuf = ByteBuffer.allocateDirect(vertices.size * 4)
        byteBuf.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuf.asFloatBuffer()
        vertexBuffer?.put(vertices)
        vertexBuffer?.position(0)
    }
}