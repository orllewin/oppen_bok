package oppen.oppenbok.io.db

import android.content.Context
import androidx.room.Room

class BookDatabase(context: Context) {

    private val db: AbstractBookDatabase = Room.databaseBuilder(
        context,
        AbstractBookDatabase::class.java,
        "oppenbok_book_database_v1")
        .build()

    fun book(): OppenBokBook = OppenBokBook(db)
}