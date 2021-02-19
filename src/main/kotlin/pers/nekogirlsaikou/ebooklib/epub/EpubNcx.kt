package pers.nekogirlsaikou.ebooklib.epub

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

open class EpubNcx : EpubItem("ncx", "toc.ncx", "application/x-dtbncx+xml") {
    open val toc: MutableList<Catalog>?
        get() = book?.toc
    open override var content: ByteArray?
        get() {
            val book: EpubBook
            val toc: List<Catalog>
            try {
                book = this.book!!
                toc = book.toc
            } catch (e: NullPointerException) {
                return null
            }
            val doc = Document("")
            val ncx = doc.appendElement("ncx")
                .attr("xmlns", "http://www.daisy.org/z3986/2005/ncx/")
                .attr("version", "2005-1")
                .attr("xml:lang", book.language)
            val head = ncx.appendElement("head")
            head.appendElement("meta")
                .attr("name", "dtb:uid")
                .attr("content", uid)
            val depthMeta = head.appendElement("meta")
                .attr("name", "dtb:depth")
                .attr("content", "0")
            head.appendElement("meta")
                .attr("name", "dtb:totalPageCount")
                .attr("content", "0")
            head.appendElement("meta")
                .attr("name", "dtb:maxPageNumber")
                .attr("content", "0")
            ncx.appendElement("docTitle")
                .appendElement("text")
                .text(book.title ?: "Untitled")
            //val navMap = ncx.appendElement("navMap")
            fun generateNavMap(navMap: Element, toc: List<Catalog>, depth: Int, depthmeta: Element) {
                for (item in toc) {
                    val navPoint = navMap.appendElement("navPoint")
                    navPoint.appendElement("navLabel")
                        .appendElement("text")
                        .text(item.title)
                    if (item.path != null) {
                        navPoint.appendElement("content")
                            .attr("src", item.path)
                    }
                    item.sub_catalog?.let {
                        generateNavMap(navPoint, it, depth + 1, depthmeta)
                    }
                    if (depthmeta.attr("content").toInt() < depth) {
                        depthmeta.attr("content", depth.toString())
                    }
                }
            }
            generateNavMap(ncx.appendElement("navMap"), toc, 1, depthMeta)

            doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
            val a = doc.toString()
            return doc.toString().encodeToByteArray()
        }
        set(value) {}
}