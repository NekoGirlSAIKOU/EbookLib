package pers.nekogirlsaikou.ebooklib.epub

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import pers.nekogirlsaikou.ebooklib.VERSION
import pers.nekogirlsaikou.ebooklib.extends.writeDeflatedFile
import pers.nekogirlsaikou.ebooklib.extends.writeStoredFile
import java.io.*
import java.lang.NullPointerException
import java.util.*
import java.util.zip.ZipOutputStream
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

open class EpubBook constructor(language: String? = null, identifier: String? = null, title: String? = null) {
    var epubVersion: EpubVersion = EpubVersion.EPUB2
    val metas: MutableList<EpubMeta> = ArrayList()
    val spine: MutableList<EpubItem> = ArrayList()
    val toc: MutableList<Catalog> = ArrayList()
    var coverXMTHL: EpubHtml? = null
    open var language: String
        get() {
            return metas.find { meta -> meta.name == "language" }!!.value!!
        }
        set(value) {
            val meta: EpubMeta? = metas.find { meta -> meta.name == "language" }
            if (meta == null) {
                metas.add(EpubMeta("dc", "language", value))
            } else {
                meta.value = value
            }
        }
    open var identifier: String
        get() {
            return metas.find { meta -> meta.name == "identifier" }!!.name
        }
        set(value) {
            val meta: EpubMeta? = metas.find { meta -> meta.name == "identifier" }
            if (meta == null) {
                val map: MutableMap<String, String> = HashMap()
                map["id"] = "BookId"
                metas.add(EpubMeta("dc", "identifier", value, map))
            } else {
                meta.value = value
            }
        }
    open var title: String?
        get() {
            return metas.find { meta -> meta.name == "title" }?.value!!
        }
        set(value) {
            metas.forEach { meta ->
                if (meta.name == "title") {
                    if (value == null) {
                        metas.remove(meta)
                    } else {
                        meta.value = value
                    }
                    return
                }
            }
            if (value == null) {
                return
            } else {
                metas.add(EpubMeta("dc", "title", value))
            }
        }

    var items: MutableList<EpubItem> = ArrayList()


    init {
        val map: MutableMap<String, String> = HashMap()
        map["name"] = "generator"
        map["content"] = "ebook-lib v$VERSION"
        this.metas.add(EpubMeta(null, "meta", null, map))
        this.language = language ?: "en"
        this.identifier = identifier ?: "urn:uuid:" + UUID.randomUUID().toString()
        this.title = title
    }

    constructor(ins: InputStream) : this() {
        // Read an ePub file
        // TODO: 2/11/21 read epub file
        metas.clear()
    }

    fun addMetadata(namespace: String?, name: String, value: String?, others: MutableMap<String, String>?) {
        metas.add(EpubMeta(namespace, name, value, others))
    }

    fun addMetadata(namespace: String?, name: String, value: String?) {
        metas.add(EpubMeta(namespace, name, value))
    }

    fun addSubject(content: String) {
        addMetadata("dc", "subject", content)
    }

    fun getAuthor(): String? {
        return metas.find { it.name == "creator" }?.value!!
    }

    fun getAuthors(): List<String> {
        val authors: MutableList<String> = ArrayList()
        metas.filter { meta -> meta.name == "creator" }
            .forEach { meta ->
                authors.add(meta.name)
            }
        return authors
    }

    fun getAuthorMetas(): List<EpubMeta> {
        return metas.filter { it.name == "creator" }
    }

    fun addAuthor(authorName: String) {
        val others: MutableMap<String, String> = HashMap()
        others["opf:role"] = "aut"
        addMetadata("dc", "creator", authorName, others)
    }

    fun addIllustrator(illustratorName: String) {
        var others: MutableMap<String, String> = HashMap()
        others["id"] = "illustrator"
        addMetadata("dc", "contributor", illustratorName, others)
        others = HashMap()
        others["property"] = "role"
        others["refines"] = "#illustrator"
        others["scheme"] = "marc:relators"
        addMetadata(null, "meta", "ill", others)
    }

    fun addItem(item: EpubItem): EpubItem {
        if (item in items) {
            return item
        }
        items.add(item)
        item.onAddedByBook(this)
        return item
    }

    fun addItem(uid: String? = null, filePath: String, mediaType: String? = null): EpubItem {
        return addItem(EpubItem(uid, filePath, mediaType))
    }

    fun addEpubHtml(uid: String? = null, filePath: String, title: String? = null, language: String? = null): EpubHtml {
        return addItem(
            EpubHtml(
                uid = uid,
                filePath = filePath,
                title = title,
                language = language,
                mediaType = null,
                content = null
            )
        ) as EpubHtml
    }

    fun addEpubCss(uid: String?=null, filePath: String, content: String? = null):EpubCss{
        return addItem(EpubCss(uid,filePath,content)) as EpubCss
    }

    fun removeItem(item: EpubItem) {
        items.remove(item)
    }

    fun findItemByFilePath(filePath: String): EpubItem? {
        return items.find { it.filePath == filePath }
    }

    fun findItemById(id: String): EpubItem? {
        return items.find { it.uid == id }
    }

    fun findAllImageItem(): List<EpubImage> {
        // If you use EpubItem instead of EpubImage as image it will work properly in ePub file but will not be founded by this method
        return items.filter { it is EpubImage } as List<EpubImage>
    }

    fun findAllHTMLItem(): List<EpubHtml> {
        return items.filter { it is EpubHtml } as List<EpubHtml>
    }

    fun findAllCssItem(): List<EpubCss>{
        return items.filter { it is EpubCss } as List<EpubCss>
    }

    open fun setCover(coverImageItem: EpubItem, create_page: Boolean = false) {
        addItem(coverImageItem)
        val others: MutableMap<String, String> = HashMap()
        others["name"] = "cover"
        others["content"] = coverImageItem.uid
        addMetadata(null, "meta", null, others)
        if (create_page) {
            // TODO: 2/11/21 Add create cover page support
            coverXMTHL = EpubHtml("coverPage", "coverPage.xhtml")

        }
    }

    open fun setCover(filePath: String?, content: ByteArray, create_page: Boolean = false) {
        // Default file name is cover.jpg
        val coverImageItem = EpubImage("coverImage", filePath ?: "cover.jpg", null, content)
        setCover(coverImageItem, create_page)
    }

    open fun setCover(content: ByteArray, create_page: Boolean = false) {
        setCover(null, content, create_page)
    }

    private fun generateContainerXML(): String {
        val doc = Document("")
        val container: Element = doc.appendElement("container")
            .attr("version", "1.0")
            .attr("xmlns", "urn:oasis:names:tc:opendocument:xmlns:container")
        val rootfile: Element = container.appendElement("rootfiles").appendElement("rootfile")
            .attr("full-path", "OEBPS/content.opf")
            .attr("media-type", "application/oebps-package+xml")

        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
        return doc.toString()
    }

    private fun generateContentOPF(): String {
        return when (epubVersion) {
            EpubVersion.EPUB2 -> generateContentOPF_epub2()
            EpubVersion.EPUB3 -> generateContentOPF_epub3()
        }
    }

    private fun generateContentOPF_epub2(): String {
        val doc = Document("")
        val package_e = doc.appendElement("package")
            .attr("version", "2.0")
            .attr("unique-identifier", "BookId")
            .attr("xmlns", "http://www.idpf.org/2007/opf")
        val metadata = package_e.appendElement("metadata")
            .attr("xmlns:opf", "http://www.idpf.org/2007/opf")
            .attr("xmlns:dc", "http://purl.org/dc/elements/1.1/")
        metas.forEach { meta -> meta.addToMetadata(metadata) }
        val manifest = package_e.appendElement("manifest")
        this.items.forEach { item ->
            val item_e = manifest.appendElement("item")
                .attr("id", item.uid)
                .attr("href", item.filePath)
                .attr("media-type", item.mediaType)
            if (item is EpubNav || item.uid == "nav") {
                item_e.attr("properties", "nav")
            }
        }
        val spine_e = package_e.appendElement("spine")
            .attr("toc", "ncx")
        if (this.spine.size == 0) {
            this.spine.addAll(findAllHTMLItem())    // Auto generate spine
        }
        this.spine.forEach { spine ->
            spine_e.appendElement("itemref")
                .attr("idref", spine.uid)
        }
        val guide: Element = package_e.appendElement("guide")
        if (coverXMTHL != null) {
            guide.appendElement("reference")
                .attr("type", "cover")
                .attr("title", coverXMTHL!!.title)
                .attr("href", coverXMTHL!!.filePath)
        }

        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
        return doc.toString()
    }

    private fun generateContentOPF_epub3(): String {
        val doc = Document("")
        val package_e = doc.appendElement("package")
            .attr("version", "3.0")
            .attr("unique-identifier", "BookId")
            .attr("xmlns", "http://www.idpf.org/2007/opf")
        val metadata = package_e.appendElement("metadata")
            .attr("xmlns:opf", "http://www.idpf.org/2007/opf")
            .attr("xmlns:dc", "http://purl.org/dc/elements/1.1/")
        metas.forEach { meta -> meta.addToMetadata(metadata) }
        val manifest = package_e.appendElement("manifest")
        this.items.forEach { item ->
            val item_e = manifest.appendElement("item")
                .attr("id", item.uid)
                .attr("href", item.filePath)
                .attr("media-type", item.mediaType)
            if (item is EpubNav || item.uid == "nav") {
                item_e.attr("properties", "nav")
            }
        }
        val spine_e = package_e.appendElement("spine")
            .attr("toc", "ncx")
        if (this.spine.size == 0) {
            this.spine.addAll(findAllHTMLItem())    // Auto generate spine
        }
        this.spine.forEach { item ->
            val itemref = spine_e.appendElement("itemref")
                .attr("idref", item.uid)
            if (item is EpubNav || item.uid == "nav") {
                itemref.attr("linear", "no")
            }
        }
        val guide: Element = package_e.appendElement("guide")
        if (coverXMTHL != null) {
            guide.appendElement("reference")
                .attr("type", "cover")
                .attr("title", coverXMTHL!!.title)
                .attr("href", coverXMTHL!!.filePath)
        }

        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
        return doc.toString()
    }

    fun write(os: OutputStream) {
        val zipOutputStream = ZipOutputStream(os)
        // Add mimetype
        zipOutputStream.writeStoredFile("mimetype", "application/epub+zip".encodeToByteArray())

        // Add META-INF/container.xml
        zipOutputStream.writeDeflatedFile("META-INF/container.xml", generateContainerXML().encodeToByteArray())

        // Add OEBPS/content.opf
        zipOutputStream.writeDeflatedFile("OEBPS/content.opf", generateContentOPF().encodeToByteArray())

        // Add items
        this.items.forEach { item ->
            item.onOutput(this.epubVersion)
            zipOutputStream.writeDeflatedFile("OEBPS/" + item.filePath, item.content)
        }

        zipOutputStream.close()
    }

    fun write(f: File) {
        write(FileOutputStream(f))
    }

    fun write(filePath: String) {
        write(FileOutputStream(filePath))
    }
}