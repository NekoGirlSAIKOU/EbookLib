package pers.nekogirlsaikou.ebooklib.epub

open class EpubImage constructor(uid:String?,filePath:String,mediaType:String?,content:ByteArray?):EpubItem(uid,filePath,mediaType,content) {
    public override var content: ByteArray?
        get() = super.content
        set(value) {super.content=value}

    constructor(epubItem: EpubItem):this(epubItem.uid,epubItem.filePath,epubItem.mediaType,epubItem.content){
        // convert EpubItem to EpubImage
        this.book = epubItem.book
    }
}