package pers.nekogirlsaikou.ebooklib.epub

data class Catalog(val title: String, var sub_catalog: MutableList<Catalog>? = null) {
    var id: String? = null
    var path: String? = null

    constructor(epubHtml: EpubHtml, sub_catalog: MutableList<Catalog>?) : this(
        epubHtml.title ?: "Untitled",
        sub_catalog
    ) {
        this.id = epubHtml.uid
        this.path = epubHtml.filePath
    }

    constructor(epubHtml: EpubHtml) : this(epubHtml, null) {
        this.id = epubHtml.uid
        this.path = epubHtml.filePath
    }

    constructor(title: String, id: String, path: String, sub_catalog: MutableList<Catalog>?) : this(
        title,
        sub_catalog
    ) {
        this.id = id
        this.path = path
    }

    constructor(title: String, id: String, path: String) : this(title, id, path, null) {
        this.id = id
        this.path = path
    }
}
