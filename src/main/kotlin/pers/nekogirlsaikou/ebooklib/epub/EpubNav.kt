package pers.nekogirlsaikou.ebooklib.epub

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

open class EpubNav : EpubHtml("nav", "nav.xhtml", "application/xhtml+xml") {
    open val toc: MutableList<Catalog>?
        get() = book?.toc
    open override var language: String
        get() {
            return this.book?.language ?: "en"
        }
        set(value) {}
    open override var title: String?
        get() {
            return super.title ?: this.book?.title
        }
        set(value) {
            super.title = value
        }
    open override var html: Document
        get() {
            val doc = Jsoup.parse("")

            this.book?.let { book ->
                val body = doc.body()
                val nav = body.appendElement("nav")
                    .attr("id", "toc")
                    .attr("role", "doc-toc")
                if (book.epubVersion == EpubVersion.EPUB3) {
                    nav.attr("epub:type", "toc")
                }

                nav.appendElement("h2")
                    .text(this.title)

                fun generateTOCTable(root: Element, toc: List<Catalog>) {
                    val ol = root.appendElement("ol")
                    toc.forEach { catalog ->
                        val li = ol.appendElement("li")
                        if (catalog.path == null) {
                            li.appendElement("div")
                                .text(catalog.title)
                        } else {
                            li.appendElement("a")
                                .text(catalog.title)
                                .attr("href", catalog.path)
                        }
                        catalog.sub_catalog?.let {
                            generateTOCTable(li, it)
                        }
                    }
                }
                generateTOCTable(nav, book.toc)
            }
            return doc
        }
        set(value) {
            // TODO: 2/19/21 parse nav
        }
}