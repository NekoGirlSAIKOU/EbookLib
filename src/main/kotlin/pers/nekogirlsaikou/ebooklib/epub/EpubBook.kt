package pers.nekogirlsaikou.ebooklib.epub

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import pers.nekogirlsaikou.ebooklib.VERSION
import pers.nekogirlsaikou.ebooklib.extends.writeDeflatedFile
import pers.nekogirlsaikou.ebooklib.extends.writeStoredFile
import java.io.*
import java.util.zip.ZipOutputStream
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

open class EpubBook constructor(language: String? = null, identifier: String? = null, title: String? = null, epubVersion: EpubVersion = EpubVersion.EPUB2) {
    var epubVersion: EpubVersion = epubVersion
    private val metas: MutableList<EpubMeta> = ArrayList()
    val spine: MutableList<EpubItem> = ArrayList()
    val toc: MutableList<Catalog> = ArrayList()
    var coverXMTHL: EpubHtml? = null
    open var language: String?
        get() {
            return getMetadata("dc","language")?.value!!
        }
        set(value) {
            if (value == null){
                removeMetadata("dc", "language")
            } else {
                setUniqueMetadata("dc", "language", value)
            }
        }
    open var title: String?
        get() {
            return getMetadata("dc", "title")?.value!!
        }
        set(value) {
            if (value == null){
                removeMetadata("dc", "title")
            } else {
                setUniqueMetadata("dc", "title", value)
            }
        }
    open var description:String?
        get() {
            return getMetadata("dc", "description")?.value!!
        }
        set(value) {
            if (value == null){
                removeMetadata("dc","description")
            } else {
                setUniqueMetadata("dc","description",value)
            }
        }

    private var items: MutableList<EpubItem> = ArrayList()


    init {
        addMetadata(
            null, "meta", null, mutableMapOf(
                "name" to "generator",
                "content" to "ebook-lib v$VERSION"
            )
        )
        this.language = language ?: "en"
        this.title = title
        identifier?.let { addIdentifier(it) }
    }

    constructor(ins: InputStream) : this() {
        // Read an ePub file
        // TODO: 2/11/21 read epub file
        metas.clear()
    }

    open fun addMetadata(namespace: String?=null, name: String, value: String?=null, others: MutableMap<String, String>?=null) {
        metas.add(EpubMeta(namespace, name, value, others))
    }

    open fun setUniqueMetadata(namespace: String?=null, name: String, value: String?=null, others: MutableMap<String, String>?=null){
        val meta = getMetadata(namespace,name)
        if (meta == null){
            addMetadata(namespace,name,value,others)
        } else {
            meta.value = value
            meta.others = others
        }
    }

    open fun removeMetadata(namespace: String?=null, name: String){
        metas.removeIf { it.namespace == namespace && it.name == name }
    }

    open fun getMetadata(namespace: String?=null, name: String):EpubMeta? {
        return metas.find { it.namespace == namespace && it.name == name }
    }

    open fun getMetadatas(namespace: String?=null, name: String):List<EpubMeta>{
        return metas.filter { it.namespace == namespace && it.name == name }
    }

    open fun addSubject(content: String) {
        addMetadata("dc", "subject", content)
    }

    open fun getSubjects():List<String>{
        val subjects:MutableList<String> = ArrayList()
        this.metas.filter { meta ->
            meta.namespace == "dc" && meta.name == "subject"
        }.forEach { meta ->
            subjects.add(meta.name)
        }
        return subjects
    }

    open fun getAuthor(): String? {
        return getMetadata("dc","creator")?.value!!
    }

    open fun getAuthors(): List<String> {
        val authors: MutableList<String> = ArrayList()
        metas.filter { meta ->meta.namespace == "dc" && meta.name == "creator" }
            .forEach { meta ->
                authors.add(meta.name)
            }
        return authors
    }

    open fun getAuthorMetas(): List<EpubMeta> {
        return metas.filter { it.namespace == "dc" && it.name == "creator" }
    }

    open fun getIdentifiers():List<String>{
        val identifiers:MutableList<String> = ArrayList()
        this.metas.filter { meta ->
            meta.namespace == "dc" && meta.name == "identifier"
        }.forEach { meta ->
            identifiers.add(meta.name)
        }
        return identifiers
    }

    open fun getIdentifier():String?{
        return getMetadata("dc","identifier")?.value
    }

    open fun addIdentifier(identifier: String) {
        addMetadata("dc", "identifier", identifier,mutableMapOf<String,String>("id" to "BookId"))
    }

    open fun addAuthor(authorName: String) {
        addMetadata("dc", "creator", authorName, mutableMapOf<String,String>("opf:role" to "aut"))
    }

    open fun addIllustrator(illustratorName: String,id:String?=null) {
        val illustratorId = if (id == null){
            val count = metas.count {
                it.namespace == null
                        && it.name == "meta"
                        && it.others?.get("property") == "role"
                        && it.others?.get("scheme") == "marc:relators"
                        && it.others?.get("refines")?.contains("#illustrator") ?: false
            }
            "illustrator${count + 1}"
        } else {
            id
        }

        addMetadata("dc", "contributor", illustratorName, mutableMapOf("id" to "illustrator"))
        addMetadata(null, "meta", "ill", mutableMapOf(
            "property" to "role",
            "refines" to  "#${illustratorId}",
            "scheme" to  "marc:relators"
        ))
    }

    open fun addItem(item: EpubItem): EpubItem {
        if (item in items) {
            return item
        }
        items.add(item)
        item.onAddedByBook(this)
        return item
    }

    open fun addItem(uid: String? = null, filePath: String, mediaType: String? = null,content:ByteArray? = null): EpubItem {
        return addItem(EpubItem(uid = uid, filePath = filePath, mediaType = mediaType,content = content))
    }

    open fun addEpubHtml(uid: String? = null, filePath: String, title: String? = null, language: String? = null): EpubHtml {
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

    open fun addEpubCss(uid: String? = null, filePath: String, content: String? = null): EpubCss {
        return addItem(EpubCss(uid = uid,filePath = filePath,content = content)) as EpubCss
    }

    open fun removeItem(item: EpubItem) {
        items.remove(item)
    }

    open fun findItem(predicate: (EpubItem) -> Boolean):EpubItem?{
        return items.find(predicate)
    }

    open fun findItems(predicate: (EpubItem) -> Boolean):List<EpubItem>{
        return items.filter(predicate)
    }

    open fun findItemByFilePath(filePath: String): EpubItem? {
        return items.find { it.filePath == filePath }
    }

    open fun findItemById(id: String): EpubItem? {
        return items.find { it.uid == id }
    }

    open fun findAllImageItem(): List<EpubImage> {
        // If you use EpubItem instead of EpubImage as image it will work properly in ePub file but will not be founded by this method
        return items.filter { it is EpubImage } as List<EpubImage>
    }

    open fun findAllHTMLItem(): List<EpubHtml> {
        return items.filter { it is EpubHtml } as List<EpubHtml>
    }

    open fun findAllCssItem(): List<EpubCss> {
        return items.filter { it is EpubCss } as List<EpubCss>
    }

    open fun setCover(coverImageItem: EpubItem, create_page: Boolean = false): EpubBook {
        addItem(coverImageItem)
        val others: MutableMap<String, String> = HashMap()
        others["name"] = "cover"
        others["content"] = coverImageItem.uid
        addMetadata(null, "meta", null, others)
        if (create_page) {
            // TODO: 2/11/21 Add create cover page support
            //coverXMTHL = EpubHtml("coverPage", "coverPage.xhtml")

        }
        return this
    }

    open fun setCover(content: ByteArray,filePath: String?=null,create_page: Boolean = false): EpubBook {
        // Default file name is cover.jpg
        val coverImageItem = EpubImage("coverImage", filePath ?: "cover.jpg", content = content)
        return setCover(coverImageItem, create_page)
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