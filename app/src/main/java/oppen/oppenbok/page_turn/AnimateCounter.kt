/*
 * Copyright (C) 2015 Hooked On Play
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package oppen.oppenbok.page_turn

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.Interpolator

/**
 *
 * Original Java source created by karacken on 18/11/16
 * https://github.com/karankalsi/PlayLikeCurl
 *
 * Converted to Kotlin and refactored by Oppenlab 2021
 *
 */
class AnimateCounter private constructor(builder: Builder) {

    private val duration: Long
    private val startValue: Float
    private val endValue: Float
    private val precision: Int
    private val interpolator: Interpolator?
    private var valueAnimator: ValueAnimator? = null

    private var listener: AnimateCounterListener? = null

    init {
        duration = builder.duration
        startValue = builder.startValue
        endValue = builder.endValue
        precision = builder.precision
        interpolator = builder.interpolator
    }

    fun execute() {
        valueAnimator = ValueAnimator.ofFloat(startValue, endValue)
        valueAnimator?.duration = duration
        valueAnimator?.interpolator = interpolator
        valueAnimator?.addUpdateListener { valueAnimator ->
            val current = java.lang.Float.valueOf(valueAnimator.animatedValue.toString())
            if (listener != null) listener!!.onValueUpdate(current)
        }
        valueAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (listener != null) {
                    listener!!.onAnimateCounterEnd()
                }
            }
        })
        valueAnimator?.start()
    }

    class Builder {
        var duration: Long = 2000
        var startValue = 0f
        var endValue = 10f
        var precision = 0
        var interpolator: Interpolator? = null

        fun setCount(start: Int, end: Int): Builder {
            require(start != end) { "Count start and end must be different" }
            startValue = start.toFloat()
            endValue = end.toFloat()
            precision = 0
            return this
        }

        fun setCount(start: Float, end: Float, precision: Int): Builder {
            require(Math.abs(start - end) >= 0.001) { "Count start and end must be different" }
            require(precision >= 0) { "Precision can't be negative" }
            startValue = start
            endValue = end
            this.precision = precision
            return this
        }

        fun setDuration(duration: Long): Builder {
            require(duration > 0) { "Duration must be positive value" }
            this.duration = duration
            return this
        }

        fun setInterpolator(interpolator: Interpolator?): Builder {
            this.interpolator = interpolator
            return this
        }

        fun build(): AnimateCounter {
            return AnimateCounter(this)
        }
    }

    fun stop() {
        if (valueAnimator?.isRunning == true) {
            valueAnimator?.cancel()
        }
    }

    fun setAnimateCounterListener(listener: AnimateCounterListener?) {
        this.listener = listener
    }

    interface AnimateCounterListener {
        fun onAnimateCounterEnd()
        fun onValueUpdate(value: Float)
    }
}