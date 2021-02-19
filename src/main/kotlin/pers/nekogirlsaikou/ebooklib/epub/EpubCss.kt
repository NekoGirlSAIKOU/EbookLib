package pers.nekogirlsaikou.ebooklib.epub

open class EpubCss constructor(uid: String?=null, filePath: String, content: String? = null) :
    EpubItem(uid, filePath, "text/css", content?.toByteArray()) {
    open var css: String?
        get() {
            return content?.decodeToString()
        }
        set(value) {
            content = value?.encodeToByteArray()
        }
}