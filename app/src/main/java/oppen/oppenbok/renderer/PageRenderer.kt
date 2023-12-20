package oppen.oppenbok.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import oppen.oppenbok.R


class PageRenderer(val context: Context, val width: Int, val height: Int) {

    private var backgroundColor = ColorDrawable(Color.parseColor("#ffffff"))
    private var foregroundColor = Color.parseColor("#1d1d1d")

    fun render(page: Array<String>): Bitmap{

        val pageView = View.inflate(context, R.layout.page, null) as LinearLayoutCompat

        pageView.background = backgroundColor

        populate(pageView, page)

        val specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
        val specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        pageView.measure(specWidth, specHeight)
        pageView.layout(0, 0, width, height)
        pageView.draw(canvas)

        return bitmap
    }

    private fun populate(viewGroup: ViewGroup, sections: Array<String>){
        sections.forEach { line ->
            if(line.startsWith("<CHAPTER_START>")){
                val textView = View.inflate(context, R.layout.text_header, null) as AppCompatTextView
                textView.text = line.removePrefix("<CHAPTER_START>")
                textView.setTextColor(foregroundColor)
                viewGroup.addView(textView)
            }else{
                val textView = View.inflate(context, R.layout.text, null) as AppCompatTextView
                textView.text = line
                textView.setTextColor(foregroundColor)
                viewGroup.addView(textView)
            }
        }
    }
}