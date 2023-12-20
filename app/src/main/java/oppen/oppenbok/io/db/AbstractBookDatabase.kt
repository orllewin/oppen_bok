package oppen.oppenbok.io.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ScreenPage::class], version = 1)
@TypeConverters(LinesConverter::class)
abstract class AbstractBookDatabase: RoomDatabase() {
    abstract fun bookDao(): BookDao
}