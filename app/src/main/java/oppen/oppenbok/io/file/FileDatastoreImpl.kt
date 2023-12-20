package oppen.oppenbok.io.file

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Xml
import oppen.oppenbok.OPEN_EPUB
import org.xmlpull.v1.XmlPullParser
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class FileDatastoreImpl(val context: Context, val onBookOpen: () -> Unit): FileDatastore {
    override fun openBookChooser() {
        onBookOpen()
    }

    override fun cacheEPubFile(uri: Uri, onFileCached: (file: File) -> Unit) {
        //we have the .ePub uri, save as-is to cache directory:

        val file = File(context.cacheDir, "cached.epub")

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val outputStream = FileOutputStream(file)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = inputStream.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }

        onFileCached(file)

    }

    override fun extractEPubFiles(file: File, onFilesExtracted: (file: File) -> Unit){
        FileInputStream(file).use { fileInputStream ->
            ZipInputStream(BufferedInputStream(fileInputStream)).use { zipInputStream ->
                var zipEntry: ZipEntry?
                while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                    val name = zipEntry!!.name

                    val archiveFile = File(context.cacheDir, name)

                    archiveFile.run {
                        parentFile?.mkdirs()
                    }

                    if(zipEntry!!.isDirectory){
                        archiveFile.mkdir()
                    }else {
                        val fileOutputStream = FileOutputStream(archiveFile)
                        val bufferedInputStream = BufferedInputStream(zipInputStream)
                        val bufferedOutputStream = BufferedOutputStream(fileOutputStream)
                        bufferedInputStream.copyTo(bufferedOutputStream)

                        bufferedOutputStream.close()
                        fileOutputStream.close()
                    }
                }
            }
        }

        onFilesExtracted(context.cacheDir)
    }

    @SuppressLint("DefaultLocale")
    override fun findOpfFile(dir: File, onOpfFile:(opfFile: File) -> Unit) {
        if (dir.exists()) {
            val files = dir.listFiles()
            for (i in files!!.indices) {
                val file = files[i]
                when {
                    file.isDirectory -> {
                        findOpfFile(file, onOpfFile)
                    }
                    else -> {
                        if(file.name.toLowerCase().endsWith(".opf")){
                            onOpfFile(file)
                            break
                        }
                    }
                }
            }
        }
    }

    override fun parseOpfFile(opfFile: File, onEntries: (title: String?, creator: String?, entries: List<FileDatastore.Entry>) -> Unit) {
        opfFile.inputStream().use { fileInputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            parser.setInput(fileInputStream, null)
            parser.nextTag()

            val entries = arrayListOf<FileDatastore.Entry>()
            var title: String? = null
            var creator: String? = null

            loop@ while (parser.next() != XmlPullParser.END_TAG) {
                when {
                    parser.eventType != XmlPullParser.START_TAG -> continue@loop
                }
                when (parser.name) {
                    "metadata" -> readMetadata(parser){ _title, _creator ->
                        title = _title
                        creator = _creator
                    }
                    "manifest" -> readManifest(opfFile, parser){ _entries ->
                        entries.addAll(_entries)
                    }
                    else -> skip(parser)
                }
            }

            onEntries(title, creator, entries)
        }
    }

    override fun logDirectory(tag: String, directory: File) {
        if (directory.exists()) {
            val files = directory.listFiles()
            for (i in files!!.indices) {
                val file = files[i]
                when {
                    file.isDirectory -> {
                        println("$tag: ${file.path}/")
                        logDirectory(tag, file)
                    }
                    else -> println("$tag: -${file.path}")
                }
            }
        }
    }

    private fun readMetadata(parser: XmlPullParser, onMetadata: (title: String?, creator: String?) -> Unit){

        var title: String? = null
        var creator: String? = null

        loop@ while (parser.next() != XmlPullParser.END_TAG) {
            when {
                parser.eventType != XmlPullParser.START_TAG -> continue@loop
            }
            when (parser.name) {
                "title" -> title = parser.nextText()
                "creator" -> creator = parser.nextText()
                else -> skip(parser)
            }
        }

        onMetadata(title, creator)
    }

    //Xml Pull Parser
    private fun readManifest(opfFile: File, parser: XmlPullParser, onEntries: (entries: List<FileDatastore.Entry>) -> Unit){
        Log.d("FND", "READING MANIFEST")
        val entries = mutableListOf<FileDatastore.Entry>()

        loop@ while (parser.next() != XmlPullParser.END_TAG) {
            when {
                parser.eventType != XmlPullParser.START_TAG -> continue@loop
            }
            when (parser.name) {
                "item" -> entries.add(readItem(opfFile, parser))
                else -> skip(parser)
            }
        }

        onEntries(entries)
    }

    private fun readItem(opfFile: File, parser: XmlPullParser): FileDatastore.Entry {
        parser.require(XmlPullParser.START_TAG, null, "item")
        val id: String? = parser.getAttributeValue(null, "id")
        val mediaType: String? = parser.getAttributeValue(null, "media-type")
        val link: String? = parser.getAttributeValue(null, "href")

        val path = File(opfFile.parentFile, link).path


        loop@ while (parser.next() != XmlPullParser.END_TAG) {
            when {
                parser.eventType != XmlPullParser.START_TAG -> continue@loop
            }
        }
        return FileDatastore.Entry(id, mediaType, path)
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}