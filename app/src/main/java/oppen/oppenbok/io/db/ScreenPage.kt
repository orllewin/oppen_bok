package oppen.oppenbok.io.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pages")
class ScreenPage (
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "lines") val lines: List<String>
)