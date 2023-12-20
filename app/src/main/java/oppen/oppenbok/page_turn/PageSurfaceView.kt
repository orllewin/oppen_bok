package oppen.oppenbok.page_turn

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import kotlin.math.abs

/**
 *
 * Original Java source created by karacken on 18/11/16
 * https://github.com/karankalsi/PlayLikeCurl
 *
 * Converted to Kotlin and refactored by Oppenlab 2021
 *
 */
class PageSurfaceView(context: Context?, val onLongPress: () -> Unit) : GLSurfaceView(context), GestureDetector.OnGestureListener {

    private val gesturedDetector = GestureDetector(context, this)
    private val renderer: PageRenderer = PageRenderer(context!!)
    private var onPageChangeListener: OnPageChangeListener? = null
    private var pageCurlAdapter: PageCurlAdapter? = null

    private var currentPosition = 0
    private var canSwipeLeft = false
    private var canSwipeRight = false

    var x1 = 0f
    var pos = 0f

    init {
        setRenderer(renderer)
    }

    fun onPageTouchEvent(event: MotionEvent): Boolean {
        if (gesturedDetector.onTouchEvent(event)) return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                renderer.togglePageActive(PageRenderer.PAGE.CURRENT)
                pos = renderer.currentPageValue
            }
            MotionEvent.ACTION_MOVE -> {
                val perc_move = (event.x - x1) / width.toFloat()
                if (event.x - x1 > 0) { // page_left
                    if (pos >= GRID && canSwipeLeft) {
                        renderer.togglePageActive(PageRenderer.PAGE.LEFT)
                        pos = renderer.currentPageValue
                    }
                    val value = pos + perc_move * GRID
                    if (value <= GRID) renderer.updateCurlPosition(value)
                } else if (event.x - x1 < 0) { //  page_right
                    val value = (1 - abs(perc_move)) * GRID - (GRID - pos)
                    if (canSwipeRight) renderer.updateCurlPosition(value)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (renderer.activePage == PageRenderer.PAGE.CURRENT) {
                    animatePagetoDefault(PageRenderer.PAGE_LEFT, false, AccelerateDecelerateInterpolator())
                } else {
                    animatePagetoDefault(PageRenderer.PAGE_RGHT, false, AccelerateDecelerateInterpolator())
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false

    override fun onLongPress(e: MotionEvent) = onLongPress()

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (abs(e1.y - e2.y) > SWIPE_MAX_OFF_PATH) {
            return false
        } else {
            if (abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {
                return false
            }
            if (e1.x - e2.x > SWIPE_MIN_DISTANCE && canSwipeRight) {
                animatePagetoDefault(PageRenderer.PAGE_RGHT, true, DecelerateInterpolator())
                return true
            } else if (e2.x - e1.x > SWIPE_MIN_DISTANCE && canSwipeLeft) {
                animatePagetoDefault(PageRenderer.PAGE_LEFT, true, DecelerateInterpolator())
                return true
            }
        }
        return false
    }

    private fun animatePagetoDefault(end_perc: Int, ispagechanged: Boolean, interpolator: Interpolator) {
        val start_per = renderer.currentPagePerc
        if (start_per == end_perc) {
            renderer.resetPages()
            if (ispagechanged) {
                processPageChange(end_perc)
            }
            return
        }

        val animateCounter = AnimateCounter.Builder()
            .setCount(start_per, end_perc)
            .setDuration(300)
            .setInterpolator(interpolator)
            .build()

        animateCounter.setAnimateCounterListener(object : AnimateCounter.AnimateCounterListener {
            override fun onAnimateCounterEnd() {
                renderer.resetPages()
                if (ispagechanged) {
                    processPageChange(end_perc)
                }
            }

            override fun onValueUpdate(value: Float) {
                renderer.updateCurlPosition(GRID * value / 100f)
            }
        })
        animateCounter.execute()
    }

    private fun processPageChange(page_type: Int) {
        when (page_type) {
            PageRenderer.PAGE_LEFT -> currentPosition--
            else -> currentPosition++
        }
        if (onPageChangeListener != null) onPageChangeListener!!.onPageChanged(currentPosition)
        processPage()
    }

    private fun processPage() {
        when (currentPosition) {
            0 -> {
                renderer.updatePageRes(
                    leftPageNumber = currentPosition,
                    frontPageNumber = currentPosition,
                    rightPageNumber =  currentPosition + 1,
                    resourceLoader = pageCurlAdapter?.resourceLoader
                )
                canSwipeLeft = false
                canSwipeRight = true
            }
            pageCurlAdapter!!.pagesCount - 1 -> {
                renderer.updatePageRes(
                    leftPageNumber = currentPosition - 1,
                    frontPageNumber = currentPosition,
                    rightPageNumber =  currentPosition,
                    resourceLoader = pageCurlAdapter!!.resourceLoader
                )
                canSwipeLeft = true
                canSwipeRight = false
            }
            else -> {
                renderer.updatePageRes(
                    leftPageNumber = currentPosition - 1,
                    frontPageNumber = currentPosition,
                    rightPageNumber = currentPosition + 1,
                    resourceLoader = pageCurlAdapter!!.resourceLoader
                )
                canSwipeLeft = true
                canSwipeRight = true
            }
        }
    }

    fun setPageCurlAdapter(pageCurlAdapter: PageCurlAdapter?) {
        this.pageCurlAdapter = pageCurlAdapter
        if (pageCurlAdapter?.pagesCount ?: 0 > 0) processPage()
    }

    fun setCurrentPosition(position: Int) {
        if (position >= 0 && position < pageCurlAdapter!!.pagesCount) {
            currentPosition = position
            processPage()
        }
    }

    interface OnPageChangeListener {
        fun onPageChanged(position: Int)
    }



    companion object {
        const val SWIPE_MIN_DISTANCE = 120
        const val SWIPE_MAX_OFF_PATH = 250
        const val SWIPE_THRESHOLD_VELOCITY = 200
    }
}