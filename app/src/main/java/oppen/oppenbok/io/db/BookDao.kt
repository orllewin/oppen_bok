package oppen.oppenbok.io.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookDao {

    @Query("SELECT * FROM pages")
    fun getAll(): List<ScreenPage>

    @Query("SELECT COUNT(*) FROM pages")
    fun getPageCount(): Int

    @Query("SELECT * FROM pages WHERE uid = :pageNumber")
    fun getPage(pageNumber: Int): ScreenPage

    @Insert
    fun insertAll(pages: List<ScreenPage>)

    @Insert
    fun insert(pages: ScreenPage)

    @Query("DELETE FROM pages")
    fun nukePages()

}