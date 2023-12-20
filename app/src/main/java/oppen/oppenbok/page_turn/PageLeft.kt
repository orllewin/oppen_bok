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
class PageLeft(screen_width: Int) : Page(screen_width) {

    override val depth = -0.001f

    override fun calculateVerticesCoords() {
        super.calculateVerticesCoords()

        for (row in 0..GRID) for (col in 0..GRID) {
            val pos = 3 * (row * (GRID + 1) + col)
            if (!isActive()) vertices[pos + 2] = depth
            var perc = 1.0f - curlCirclePosition / GRID.toFloat()
            perc *= 0.75f
            val dx = GRID - curlCirclePosition

            var calc_r = RADIUS

            if (perc < 0.20f) calc_r = RADIUS * perc * 5
            var mov_x = perc

            if (isActive()) vertices[pos + 2] = (calc_r * Math.sin(3.14 / (GRID * 0.50f) * (col - dx)) + calc_r * 1.1f).toFloat() //Asin(2pi/wav*x)
            val w_h_ratio = 1 - calc_r
            vertices[pos] = col.toFloat() / GRID.toFloat() * w_h_ratio - mov_x
            vertices[pos + 1] = row.toFloat() / GRID.toFloat() * hWRatio - hWCorrection
        }
        val byteBuf = ByteBuffer.allocateDirect(vertices.size * 4)
        byteBuf.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuf.asFloatBuffer()
        vertexBuffer?.put(vertices)
        vertexBuffer?.position(0)
    }
}