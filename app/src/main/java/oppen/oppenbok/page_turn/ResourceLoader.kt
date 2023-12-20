package oppen.oppenbok.page_turn

import android.graphics.Bitmap

interface ResourceLoader {
    fun loadBitmap(pageNumber: Int, onBitmap: (Bitmap?) -> Unit)
}