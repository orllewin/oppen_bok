package oppen.oppenbok.io.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_tracker")
class ProgressTracker(
        @PrimaryKey val name: String,
        val pageNumber: Int
)