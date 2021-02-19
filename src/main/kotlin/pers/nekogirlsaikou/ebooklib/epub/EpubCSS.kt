package pers.nekogirlsaikou.ebooklib.epub

open class EpubCSS constructor(uid: String?, filePath: String, content: String) :
    EpubItem(uid, filePath, "text/css", content.toByteArray()) {
    var css: String?
        get() {
            return content?.decodeToString()
        }
        set(value) {
            content = value?.encodeToByteArray()
        }
}