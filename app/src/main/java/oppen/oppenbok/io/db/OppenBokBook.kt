package oppen.oppenbok.io.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OppenBokBook(private val db: AbstractBookDatabase) {

    fun getAllPages(onPages: (List<ScreenPage>) -> Unit){
        GlobalScope.launch(Dispatchers.IO){
            val pages = db.bookDao().getAll()
            onPages(pages)
        }
    }

    fun getPageCount(onPageCount: (Int) -> Unit){
        GlobalScope.launch(Dispatchers.IO){
            val pageCount = db.bookDao().getPageCount()
            onPageCount(pageCount)
        }
    }

    fun getPageCountSynchronous(): Int = db.bookDao().getPageCount()

    fun getPage(pageNumber: Int, onPage: (ScreenPage) -> Unit){
        GlobalScope.launch(Dispatchers.IO){
            val page = db.bookDao().getPage(pageNumber)
            onPage(page)
        }
    }

    fun getPageSynchronous(pageNumber: Int): ScreenPage = db.bookDao().getPage(pageNumber)

    fun addPages(pages: List<ScreenPage>, onAdded: () -> Unit){
        GlobalScope.launch(Dispatchers.IO){
            db.bookDao().insertAll(pages)
            onAdded()
        }
    }

    fun addPagesSynchronous(pages: List<ScreenPage>){
        db.bookDao().insertAll(pages)
    }

    fun addPage(page: ScreenPage, onAdded: () -> Unit){
        GlobalScope.launch(Dispatchers.IO){
            db.bookDao().insert(page)
            onAdded()
        }
    }

    fun deleteBook(onNuked: () -> Unit){
        GlobalScope.launch(Dispatchers.IO){
            db.bookDao().nukePages()
            onNuked()
        }
    }

}