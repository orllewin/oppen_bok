package oppen.oppenbok.page_turn

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.opengl.GLSurfaceView
import android.opengl.GLU
import oppen.oppenbok.RuntimeParams
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.atan

/**
 *
 * Original Java source created by karacken on 18/11/16
 * https://github.com/karankalsi/PlayLikeCurl
 *
 * Converted to Kotlin and refactored by Oppenlab 2021
 *
 */
class PageRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private var leftPage: Page
    private var frontPage: Page
    private var rightPage: Page

    private val renderDepth = -2f

    enum class PAGE { LEFT, RIGHT, CURRENT }
    var activePage: PAGE? = null

    init {
        val width = Resources.getSystem().displayMetrics.widthPixels
        leftPage = PageLeft(width)
        frontPage = PageFront(width)
        rightPage = PageRight(width)
        togglePageActive(PAGE.CURRENT)
        leftPage.curlCirclePosition = GRID * (PAGE_RGHT.toFloat() / 100f)
        rightPage.curlCirclePosition = GRID.toFloat()
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        frontPage.loadGLTexture(gl, context)
        rightPage.loadGLTexture(gl, context)
        leftPage.loadGLTexture(gl, context)
        gl.glEnable(GL10.GL_TEXTURE_2D)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glClearColor(1f, 1f, 1f, 1f)
        gl.glClearDepthf(1.0f)
        gl.glEnable(GL10.GL_DEPTH_TEST)
        gl.glDepthFunc(GL10.GL_LEQUAL)
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST)

        println("OPENGLL SURFACE CREATED")
    }

    override fun onDrawFrame(gl: GL10) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl.glLoadIdentity()
        gl.glPushMatrix()
        gl.glTranslatef(0.0f, 0.0f, renderDepth)
        gl.glTranslatef(-0.5f, -0.5f, 0.0f)
        leftPage.draw(gl, context)
        gl.glPopMatrix()
        gl.glPushMatrix()
        gl.glTranslatef(0.0f, 0.0f, renderDepth)
        gl.glTranslatef(-0.5f, -0.5f, 0.0f)
        frontPage.draw(gl, context)
        gl.glPopMatrix()
        gl.glPushMatrix()
        gl.glTranslatef(0.0f, 0.0f, renderDepth)
        gl.glTranslatef(-0.5f, -0.5f, 0.0f)
        rightPage.draw(gl, context)
        gl.glPopMatrix()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, _height: Int) {
        var height = _height
        if (height == 0) {
            height = 1
        }
        gl.glViewport(0, 0, width, height)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()

        val hwRatio: Float
        val aspect: Float
        when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                hwRatio = height.toFloat() / width.toFloat()
                aspect = width.toFloat() / height.toFloat()
            }
            else -> {
                hwRatio = width.toFloat() / height.toFloat()
                aspect = height.toFloat() / width.toFloat()
            }
        }

        /*
            The relationship between the FOV angle, focal length (or depth), and height of the
            view is expressed by the formula given here:
            https://en.wikipedia.org/wiki/Angle_of_view#Calculating_a_camera's_angle_of_view

            In this case, `gluPerspective()` works in the Y axis, so we need the height.

            The depth is mostly constant within this class as `renderDepth`, though there's a
            tiny factor on each `Page` subclass to get the correct layering.

            The height is actually determined in `PageFront.calculateVerticesCoords()`,
            where the width is set to `1.0f`, and the height comes from `Page.hWRatio`, which
            is ultimately derived from the `Page.bitmapRatio` via `Page.loadGLTexture()` and then
            `Page.calculateVerticesCoords()`.

            While that bitmap work is async, we don't actually need to get the ratio from there,
            since it's derived from the same window dimensions that we already have here.
            We can effectively determine the height using local calculations.

            The following code should set the `fovY` to precisely fit the page bitmap on the screen.
            (If we had some reason to keep the FOV constant, we could also vary the depth to
            get the same effect.)
         */

        val depth = abs(frontPage.depth + renderDepth)
        val fovYRadians = atan(hwRatio / (2 * depth)) * 2
        val fovYDegrees = fovYRadians * (180f / Math.PI.toFloat())

        GLU.gluPerspective(gl, fovYDegrees, aspect, 0.1f, 100.0f)

        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()
    }

    fun updatePageRes(leftPageNumber: Int, frontPageNumber: Int, rightPageNumber: Int, resourceLoader: ResourceLoader?) {
        leftPage.setResourceId(leftPageNumber, resourceLoader)
        frontPage.setResourceId(frontPageNumber, resourceLoader)
        rightPage.setResourceId(rightPageNumber, resourceLoader)
    }

    fun togglePageActive(page: PAGE) {
        if (activePage == null || activePage != page) {
            activePage = page
            when (activePage) {
                PAGE.LEFT -> {
                    leftPage.setActive(true)
                    frontPage.setActive(false)
                    rightPage.setActive(false)
                }
                PAGE.RIGHT -> {
                    leftPage.setActive(false)
                    frontPage.setActive(false)
                    rightPage.setActive(true)
                }
                PAGE.CURRENT -> {
                    leftPage.setActive(false)
                    frontPage.setActive(true)
                    rightPage.setActive(false)
                }
            }
        }
    }

    fun updateCurlPosition(value: Float) {
        when (activePage) {
            PAGE.LEFT -> leftPage.curlCirclePosition = value
            PAGE.RIGHT -> rightPage.curlCirclePosition = value
            PAGE.CURRENT -> frontPage.curlCirclePosition = value
        }
    }

    val currentPagePerc: Int
        get() = when (activePage) {
            PAGE.LEFT -> (leftPage.curlCirclePosition / GRID * 100).toInt()
            PAGE.RIGHT -> (rightPage.curlCirclePosition / GRID * 100).toInt()
            PAGE.CURRENT -> (frontPage.curlCirclePosition / GRID * 100).toInt()
            else -> (frontPage.curlCirclePosition / GRID * 100).toInt()
        }
    val currentPageValue: Float
        get() = when (activePage) {
            PAGE.LEFT -> leftPage.curlCirclePosition
            PAGE.RIGHT -> rightPage.curlCirclePosition
            PAGE.CURRENT -> frontPage.curlCirclePosition
            else -> frontPage.curlCirclePosition
        }

    fun resetPages() {
        leftPage.curlCirclePosition = GRID * (PAGE_RGHT.toFloat() / 100f)
        rightPage.curlCirclePosition = GRID.toFloat()
        frontPage.curlCirclePosition = GRID.toFloat()
        togglePageActive(PAGE.CURRENT)
    }

    companion object {
        const val PAGE_LEFT = 100
        const val PAGE_RGHT = -5
    }
}