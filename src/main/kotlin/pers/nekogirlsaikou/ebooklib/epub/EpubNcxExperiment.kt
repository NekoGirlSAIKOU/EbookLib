package pers.nekogirlsaikou.ebooklib.epub

import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException
import kotlin.Throws
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.DocumentBuilder
import javax.xml.transform.TransformerFactory
import javax.xml.transform.OutputKeys
import java.io.ByteArrayOutputStream
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import pers.nekogirlsaikou.ebooklib.epub.EpubHtml
import java.nio.charset.StandardCharsets
import java.util.HashMap

/**
 * EpubNcx seems have some bugs.
 * This may fix those bugs and may be the default EpubNcx in the future.
 */
class EpubNcxExperiment : EpubNcx() {
    override var content: ByteArray?
        get() {
            if (book == null) {
                return null
            }

            return generateToc(
                book!!.toc,
                book!!.title,
                book!!.language,
                this.uid
            ).toByteArray(StandardCharsets.UTF_8)
        }
        set(value) {}

    companion object {
        @Throws(ParserConfigurationException::class, TransformerException::class)
        fun generateToc(toc: List<Catalog>?, novel_title: String?, language: String?, uid: String?): String {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.newDocument()
            document.xmlStandalone = true
            val ncx = document.createElement("ncx")
            document.appendChild(ncx)
            ncx.setAttribute("xmlns", "http://www.daisy.org/z3986/2005/ncx/")
            ncx.setAttribute("version", "2005-1")
            ncx.setAttribute("xml:lang", language)
            val head = document.createElement("head")
            ncx.appendChild(head)
            var meta = document.createElement("meta")
            head.appendChild(meta)
            meta.setAttribute("name", "dtb:uid")
            meta.setAttribute("content", uid)
            val depthmeta = document.createElement("meta")
            head.appendChild(depthmeta)
            depthmeta.setAttribute("name", "dtb:depth")
            depthmeta.setAttribute("content", "0")
            meta = document.createElement("meta")
            head.appendChild(meta)
            meta.setAttribute("name", "dtb:totalPageCount")
            meta.setAttribute("content", "0")
            meta = document.createElement("meta")
            head.appendChild(meta)
            meta.setAttribute("name", "dtb:maxPageNumber")
            meta.setAttribute("content", "0")
            val docTitle = document.createElement("docTitle")
            ncx.appendChild(docTitle)
            val text = document.createElement("text")
            text.textContent = novel_title
            docTitle.appendChild(text)
            val navMap = document.createElement("navMap")
            ncx.appendChild(navMap)
            generateNavMap(document, navMap, toc!!, 1, depthmeta)
            val tff = TransformerFactory.newInstance()
            val tf = tff.newTransformer()
            tf.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//NISO//DTD ncx 2005-1//EN")
            tf.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd")
            tf.setOutputProperty(OutputKeys.INDENT, "yes") //for debug purpose
            val xml_os = ByteArrayOutputStream()
            tf.transform(DOMSource(document), StreamResult(xml_os))
            return xml_os.toString()
        }

        private fun generateNavMap(document: Document, navMap: Element, toc: List<Catalog>, depth: Int, depthmeta: Element) {
            for (item in toc) {
                val navPoint = document.createElement("navPoint")
                navMap.appendChild(navPoint)
                val navLabel = document.createElement("navLabel")
                navPoint.appendChild(navLabel)
                val text = document.createElement("text")
                navLabel.appendChild(text)
                text.textContent = item.title
                if (item.path != null) {
                    val content = document.createElement("content")
                    navPoint.appendChild(content)
                    content.setAttribute("src", item.path as String?)
                }
                if (item.sub_catalog != null) {
                    generateNavMap(document, navPoint, item.sub_catalog!!, depth + 1, depthmeta)
                }
                val current_depth = depthmeta.getAttribute("content").toInt()
                if (current_depth < depth) {
                    depthmeta.setAttribute("content", depth.toString())
                }
            }
        }
    }
}