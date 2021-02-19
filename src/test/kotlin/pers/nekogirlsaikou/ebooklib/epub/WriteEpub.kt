package pers.nekogirlsaikou.ebooklib.epub

import org.junit.Test

class WriteEpub {
    @Test
    fun writeEpub2() {
        val book = EpubBook(title = "test epub2")
        book.epubVersion = EpubVersion.EPUB2
        book.addAuthor("NekoGirlSAIKOU")
        book.addSubject("epub2")

        book.addEpubHtml(title = "volume1", filePath = "volume1.xhtml")
            .setContent("<p>volume1 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc, true)

        book.addEpubHtml(title = "chater1", filePath = "chapter1.xhtml")
            .setContent("<p>chapter1 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc[0].sub_catalog!!)

        book.addEpubHtml(title = "chater2", filePath = "chapter2.xhtml")
            .setContent("<p>chapter2 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc[0].sub_catalog!!)

        book.addEpubHtml(title = "volume2", filePath = "volume2.xhtml")
            .setContent("<p>volume2 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc, true)

        book.addEpubHtml(title = "chater3", filePath = "chapter3.xhtml")
            .setContent("<p>chapter3 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc[1].sub_catalog!!)

        book.addEpubHtml(title = "chater4", filePath = "chapter4.xhtml")
            .setContent("<p>chapter4 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc[1].sub_catalog!!)

        book.addItem(EpubNcx())

        book.write("/tmp/test epub2.epub")
    }

    @Test
    fun writeEpub3() {
        val book = EpubBook(title = "test epub3")
        book.epubVersion = EpubVersion.EPUB3
        book.addAuthor("NekoGirlSAIKOU")
        book.addSubject("epub3")

        book.addEpubHtml(title = "volume1", filePath = "volume1.xhtml")
            .setContent("<p>volume1 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc, true)

        book.addEpubHtml(title = "chapter1", filePath = "chapter1.xhtml")
            .setContent("<p>chapter1 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc[0].sub_catalog!!)

        book.addEpubHtml(title = "chapter2", filePath = "chapter2.xhtml")
            .setContent("<p>chapter2 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc[0].sub_catalog!!)

        book.addEpubHtml(title = "volume2", filePath = "volume2.xhtml")
            .setContent("<p>volume2 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc, true)

        book.addEpubHtml(title = "chapter3", filePath = "chapter3.xhtml")
            .setContent("<p>chapter3 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc[1].sub_catalog!!)

        book.addEpubHtml(title = "chapter4", filePath = "chapter4.xhtml")
            .setContent("<p>chapter4 content</p>")
            .addToSpine(book.spine)
            .addToToc(book.toc[1].sub_catalog!!)

        book.addItem(EpubNcx())
        book.addItem(EpubNav())
            .addToSpine(book.spine)

        book.write("/tmp/test epub3.epub")
    }
}