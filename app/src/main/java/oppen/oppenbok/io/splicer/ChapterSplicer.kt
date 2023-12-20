package oppen.oppenbok.io.splicer

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView

interface ChapterSplicer {
    fun splice(chapterNumber: Int, chapter: List<String>, onSpliced: (pages: List<Array<String>>) -> Unit)

    companion object {
        fun getDefault(context: Context, headingTemplate: AppCompatTextView, template: AppCompatTextView): ChapterSplicer {
            return ChapterSplicerImpl(context, headingTemplate, template)
        }
    }
}