package oppen.oppenbok.io.splicer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import oppen.oppenbok.R
import oppen.oppenbok.RuntimeParams


class ChapterSplicerImpl(
    private val context: Context,
    private val headingTemplate: AppCompatTextView,
    private val template: AppCompatTextView
): ChapterSplicer {
    var chapterNumber = -1
    var height = 0
    private val pages = arrayListOf<Array<String>>()

    private var _template: AppCompatTextView? = null
    private var _headerTemplate: AppCompatTextView? = null

    private var isNewChapter = false

    init {
        _template = LayoutInflater.from(context).inflate(R.layout.text, null) as AppCompatTextView?
        _template?.layoutParams = template.layoutParams

        _headerTemplate = LayoutInflater.from(context).inflate(R.layout.text_header, null) as AppCompatTextView?
        _headerTemplate?.layoutParams = headingTemplate.layoutParams
    }

    override fun splice(chapterNumber: Int, chapter: List<String>, onSpliced: (pages: List<Array<String>>) -> Unit) {
        this.chapterNumber = chapterNumber
        height = RuntimeParams.height - context.resources.getDimensionPixelSize(R.dimen.page_padding_vertical_total)
        pages.clear()

        isNewChapter = true

        buildScreenPages(chapter, onSpliced)
    }

    private fun buildScreenPages(
        lines: List<String>,
        onSpliced: (pages: List<Array<String>>) -> Unit
    ){

        var cumulativeHeight = 0
        var lineNumber = 0
        var linesAdded = 0
        var remainingHeight = 0
        val page = arrayListOf<String>()

        var lineHeight = 0

        while (lineHeight < height && cumulativeHeight < height && lineNumber < lines.size){

            var line = lines[lineNumber]

            if(isNewChapter && lineNumber == 0){
                isNewChapter = false
                _headerTemplate?.text = line
                lineHeight = getHeight(_headerTemplate)
                line = "<CHAPTER_START>$line"
                cumulativeHeight += lineHeight
            }else{
                _template?.text = line
                lineHeight = getHeight(_template)
                cumulativeHeight += lineHeight
            }

            if(cumulativeHeight < height){
                page.add(line)
                linesAdded++
                lineNumber++
            }else{
                remainingHeight = height - (cumulativeHeight - lineHeight)
            }
        }

        if(linesAdded == 0){
            //we didn't add any lines because the first one we tried was bigger than the screen
            fillRemainder(remainingHeight, lines, page, onSpliced)
        }else{
            val sublist = lines.subList(linesAdded, lines.size)
            if(sublist.isEmpty()){
                onSpliced(pages)
                return
            }else{
                fillRemainder(remainingHeight, sublist, page, onSpliced)
            }
        }
    }

    private fun fillRemainder(
        remainingHeight: Int,
        lines: List<String>,
        page: ArrayList<String>,
        onSpliced: (pages: List<Array<String>>) -> Unit
    ){

//        if(remainingHeight < context.resources.getDimensionPixelSize(R.dimen.min_remaining) ){
//            buildScreenPages(lines, onSpliced)
//            return
//        }

        val nextLine = lines.first()
        val words = nextLine.split(" ")

        var viewHeight = 0
        var wordIndex = 0

        val sb = StringBuilder()

        val wordCount = words.size

        while (viewHeight < remainingHeight && wordIndex < wordCount){
            sb.append(words[wordIndex] + " ")

            _template?.text = sb.toString()

            viewHeight = getHeight(_template)
            wordIndex++
        }

        var fitLine = ""

        for(i in 0 until wordIndex-1) fitLine += "${words[i]} "

        page.add(fitLine)

        var lineRemainder = ""

        try {
            lineRemainder = nextLine.substring(fitLine.length, nextLine.length)
        }catch (e: StringIndexOutOfBoundsException){
            println(e.toString())
        }

        pages.add(page.toTypedArray())

        val remainingLines = arrayListOf<String>()
        remainingLines.add(lineRemainder)
        remainingLines.addAll(lines.subList(1, lines.size))
        buildScreenPages(remainingLines, onSpliced)
    }

    private fun getHeight(t: AppCompatTextView?): Int {

        if(t == null) throw IllegalStateException("Cannot get height of a null view")

        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            RuntimeParams.width - context.resources.getDimensionPixelSize(R.dimen.page_padding_horizontal_total),
            View.MeasureSpec.AT_MOST
        )
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        t.measure(widthMeasureSpec, heightMeasureSpec)
        return t.measuredHeight
    }
}