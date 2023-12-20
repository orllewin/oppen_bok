package oppen.oppenbok.io.xhtml

import android.os.Build
import android.text.Html
import oppen.oppenbok.io.file.FileDatastore
import java.io.BufferedReader
import java.io.File

class XhtmlParser() {

    fun parse(entry: FileDatastore.Entry, onProcessed: (lines: List<String>) -> Unit){
        val file = File("${entry.path}")

        if(!file.exists()){
            throw IllegalStateException("Somehow, a file from the zip is inaccessible")
        }

        val content = file.inputStream().bufferedReader().use(BufferedReader::readText)
        val spannedContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(content, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        } else {
            Html.fromHtml(content)
        }

        val lines = spannedContent.split("\n")
        onProcessed(lines)
    }
}