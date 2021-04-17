package pers.nekogirlsaikou.ebooklib.epub

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.ParseSettings
import org.jsoup.parser.Tag
import java.lang.reflect.Field

open class EpubNcx : EpubItem(uid = "ncx", filePath = "toc.ncx", mediaType = "application/x-dtbncx+xml") {
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
                .setTagName("docTitle")
            //val navMap = ncx.appendElement("navMap")
            fun generateNavMap(navMap: Element, toc: List<Catalog>, depth: Int, depthmeta: Element) {
                for (item in toc) {
                    val navPoint = navMap.appendElement("navPoint")
                        .setTagName("navPoint")
                    navPoint.appendElement("navLabel")
                        .appendElement("text")
                        .text(item.title)
                        .setTagName("navLabel")
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
            generateNavMap(ncx.appendElement("navMap").setTagName("navMap"), toc, 1, depthMeta)

            doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
            val a = doc.toString()
            return doc.toString().encodeToByteArray()
        }
        set(value) {}
}

private fun Element.setTagName(newTagName:String):Element{
/*    val tag: Field = Element::class.java.getDeclaredField("tag")
    tag.isAccessible = true
    tag.set(this, Tag.valueOf(newTagName, ParseSettings.preserveCase))
    tag.isAccessible = false
    return this*/


/*    val tagName: Field = Tag::class.java.getDeclaredField("tagName") // Get the field which contains the tagname
    tagName.isAccessible = true // Set accessible to allow changes
    tagName.set(this.tag(),newTagName) // Set the tagname
    tagName.isAccessible = false // Revert to false

    val normalName: Field = Tag::class.java.getDeclaredField("normalName") // Get the field which contains the tagname
    normalName.isAccessible = true // Set accessible to allow changes
    normalName.set(this.tag(),newTagName) // Set the tagname
    normalName.isAccessible = false // Revert to false*/

    return this
}