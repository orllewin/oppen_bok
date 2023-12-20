package oppen.oppenbok.io.file

import android.content.Context
import android.net.Uri
import java.io.File

interface FileDatastore {

    data class Entry(val id: String?, val mediaType: String?, val path: String?)

    fun openBookChooser()
    fun cacheEPubFile(uri: Uri, onFileCached: (file: File) -> Unit)
    fun extractEPubFiles(file: File, onFilesExtracted: (directory: File) -> Unit)
    fun logDirectory(tag: String, directory: File)
    fun findOpfFile(dir: File, onOpfFile:(opfFile: File) -> Unit)
    fun parseOpfFile(opfFile: File, onEntries: (title: String?, creator: String?, files: List<Entry>) -> Unit)

    companion object{
        fun getDefault(context: Context, onBookOpen: () -> Unit): FileDatastore{
            return FileDatastoreImpl(context){
                onBookOpen()
            }
        }
    }
}