package pers.nekogirlsaikou.ebooklib.epub

open class EpubImage constructor(uid: String?=null, filePath: String, mediaType: String?=null, content: ByteArray?=null) :
    EpubItem(uid=uid, filePath=filePath, mediaType=mediaType, content=content) {
    public open override var content: ByteArray?
        get() = super.content
        set(value) {
            super.content = value
        }

    constructor(epubItem: EpubItem) : this(epubItem.uid, epubItem.filePath, epubItem.mediaType, epubItem.content) {
        // convert EpubItem to EpubImage
        this.book = epubItem.book
    }
}