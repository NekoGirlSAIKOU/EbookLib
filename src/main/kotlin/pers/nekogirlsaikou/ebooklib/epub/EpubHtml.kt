package pers.nekogirlsaikou.ebooklib.epub

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import pers.nekogirlsaikou.ebooklib.extends.getRootElement

open class EpubHtml constructor(
    uid: String?=null,
    filePath: String,
    mediaType: String?=null,
    content: String?=null,
    language: String?=null,
    title: String?=null
) : EpubItem(uid=uid, filePath=filePath, mediaType=mediaType) {
    open lateinit var html: Document
    open var language: String = "en"
    open var title: String? = null
    open internal var linkedCSS: MutableList<EpubItem> = ArrayList()
    open override var content: ByteArray?
        get() {
            val html = this.html.getRootElement()

            html.attr("lang", language)
            html.attr("xml:lang", language)

            when (this.book?.epubVersion ?: EpubVersion.EPUB2) {
                EpubVersion.EPUB2 -> {
                    html.attr("xmlns", "http://www.w3.org/1999/xhtml")
                }
                EpubVersion.EPUB3 -> {
                    html.attr("xmlns", "http://www.w3.org/1999/xhtml")
                    html.attr("xmlns:epub", "http://www.idpf.org/2007/ops")
                }
            }

            this.linkedCSS.forEach { item ->
                this.html.head().appendElement("link")
                    .attr("href", item.filePath)
                    .attr("type", item.mediaType)
                    .attr("rel", "stylesheet")
            }

            if (this.title != null) {
                this.html.head().appendElement("title").text(this.title)
            }


            this.html.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
            this.html.outputSettings().escapeMode(Entities.EscapeMode.xhtml)
            this.html.outputSettings().charset("UTF-8")
            return ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<!DOCTYPE html>\n" + html.toString()).toByteArray()
        }
        set(value) {
            this.html = Jsoup.parse(value?.decodeToString() ?:"")
        }

    init {
        this.html = Jsoup.parse(content ?: "")
        this.language = language ?: "en"
        this.title = title
    }

    open fun getContent(): String {
        return this.html.html()
    }

    open fun setContent(content: String?): EpubHtml {
        this.html = Jsoup.parse(content ?: "")
        return this
    }

    open fun linkCSS(CSSItem: EpubItem): EpubHtml {
        this.linkedCSS.add(CSSItem)
        return this
    }

    open fun unlinkCSS(CSSItem: EpubItem) {
        this.linkedCSS.remove(CSSItem)
    }

    fun getLinkedCSS(): List<EpubItem> {
        return this.linkedCSS
    }
}