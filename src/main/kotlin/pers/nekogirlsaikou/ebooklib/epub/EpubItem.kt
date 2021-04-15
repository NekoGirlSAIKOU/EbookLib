package pers.nekogirlsaikou.ebooklib.epub

import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

open class EpubItem constructor(uid: String?, filePath: String, mediaType: String?, content: ByteArray?) {
    var uid: String = uid ?: UUID.randomUUID().toString()
    var filePath: String = filePath
    var mediaType: String = mediaType ?: getMimeFromPath(filePath)
    internal open var content: ByteArray? = content
    var book: EpubBook? = null

    constructor(uid: String?, filePath: String, mediaType: String?) : this(uid, filePath, mediaType, null)

    constructor(uid: String?, filePath: String) : this(uid, filePath, null, null)

    constructor(filePath: String) : this(null, filePath, null, null)

    open fun addToSpine(spine: MutableList<EpubItem>): EpubItem {
        spine.add(this)
        return this
    }

    internal open fun onAddedByBook(book: EpubBook) {
        this.book = book
    }

    internal open fun onRemoveedByBook(book: EpubBook) {
        this.book = null
    }

    internal open fun onOutput(epubVersion: EpubVersion) {
    }

    private fun getMimeFromPath(filePath:String):String{
        return when (filePath.substring(filePath.lastIndexOf("."))){
            ".xhtml"->"application/xhtml+xml"
            ".jpg"->"image/jpeg"
            ".jpeg"->"image/jpeg"
            ".png"->"image/png"
            ".gif"->"image/gif"
            ".bmp"->"image/bmp"
            else -> Files.probeContentType(Paths.get(filePath))
        }
    }
}