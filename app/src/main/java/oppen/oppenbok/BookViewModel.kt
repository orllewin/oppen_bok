package oppen.oppenbok

import android.net.Uri
import androidx.lifecycle.ViewModel
import oppen.oppenbok.io.db.OppenBokBook
import oppen.oppenbok.io.db.ScreenPage
import oppen.oppenbok.io.file.FileDatastore
import oppen.oppenbok.io.splicer.ChapterSplicer
import oppen.oppenbok.io.xhtml.XhtmlParser
import java.io.File

class BookViewModel: ViewModel() {

    private lateinit var bookDb: OppenBokBook
    private lateinit var fileIO: FileDatastore
    private lateinit var chapterSplicer: ChapterSplicer
    private lateinit var listener: BookListener

    private var pageIndex = -1

    fun initialise(bookDb: OppenBokBook, fileIO: FileDatastore, chapterSplicer: ChapterSplicer, listener: BookListener){
        this.bookDb = bookDb
        this.fileIO = fileIO
        this.chapterSplicer = chapterSplicer
        this.listener = listener

        bookDb.getPageCount { screenPages ->
            listener.ready(screenPages)
        }
    }

    fun openBook() = fileIO.openBookChooser()

    fun importBook(uri: Uri){
        fileIO.cacheEPubFile(uri){ cachedFile ->
            extractBook(cachedFile)
        }
    }

    private fun extractBook(cachedFile: File){
        fileIO.extractEPubFiles(cachedFile){ cacheDirectory ->
            fileIO.logDirectory("BOK", cacheDirectory)
            findAndProcessOpfFile(cacheDirectory)
        }
    }

    private fun findAndProcessOpfFile(directory: File){
        fileIO.findOpfFile(directory){ opfFile ->
            fileIO.parseOpfFile(opfFile){ title, creator, entries ->
                listener.storeMetadata(title, creator)
                processXhtml(entries)
            }
        }
    }

    /**
     * This method iterates each xHtml file, splices the content into 'ScreenPages'
     * and saves each xHtml files-worth of content into the DB
     *
     */
    private fun processXhtml(entries: List<FileDatastore.Entry>){
        pageIndex = -1

        entries.forEachIndexed { index, entry ->
            if(entry.mediaType == "application/xhtml+xml"){
                XhtmlParser().parse(entry){ lines ->
                    spliceChapter(index, lines){ chapterPages ->
                        bookDb.addPagesSynchronous(chapterPages)
                    }
                }
            }
        }

        listener.ready(bookDb.getPageCountSynchronous())
    }

    private fun spliceChapter(chapter: Int, lines: List<String>, onSpliced: (List<ScreenPage>) -> Unit) {
        chapterSplicer.splice(chapter, lines){ screenPages ->
            //Convert to DB entity
            val mapped = screenPages.map { strings ->
                pageIndex++
                ScreenPage(pageIndex, strings.toList())
            }
            onSpliced(mapped)
        }
    }

    /**
     * Note: This method can only be called from the OpenGL SurfaceView context
     * The synchronous database read is carried out in the OpenGL thread
     */
    fun getPage(pageNumber: Int): Array<String>{
        val screenPage = bookDb.getPageSynchronous(pageNumber)
        return screenPage.lines.toTypedArray()
    }

    fun clearAndOpen() {
        bookDb.deleteBook {
            listener.ready(0)
        }
    }

    interface BookListener{
        fun ready(pageCount: Int)
        fun storeMetadata(title: String?, creator: String?)
    }
}