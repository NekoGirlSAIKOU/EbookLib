package pers.nekogirlsaikou.ebooklib.epub

import org.junit.Test

class WriteEpub {
    @Test
    fun writeEpub2() {
        val book = EpubBook(title = "test epub2")
        book.addIdentifier("123");
        book.epubVersion = EpubVersion.EPUB2
        book.addAuthor("NekoGirlSAIKOU")
        book.addSubject("epub2")

        book.addTestItems()

        book.addItem(EpubNcxExperiment())

        book.write("/tmp/test epub2.epub")
    }

    @Test
    fun writeEpub3() {
        val book = EpubBook(title = "test epub3")
        book.addIdentifier("123");
        book.epubVersion = EpubVersion.EPUB3
        book.addAuthor("NekoGirlSAIKOU")
        book.addSubject("epub3")

        book.addTestItems()

        book.addItem(EpubNcxExperiment())
        book.addItem(EpubNav().apply { book.spine.add(this) })

        book.write("/tmp/test epub3.epub")
    }
}

fun EpubBook.addTestItems() {
    val book = this

    val css = book.addEpubCss("test_css", "test_css.css")
    css.css = "p {color:red}"

    book.addEpubHtml(title = "volume1", filePath = "volume1.xhtml")
        .let {
            it.setContent("<p>volume1 content</p>")
            book.spine.add(it)
            book.toc.add(Catalog(it).apply { this.sub_catalog = ArrayList() })
        }

    book.addEpubHtml(title = "chapter1", filePath = "chapter1.xhtml")
        .let {
            it.setContent("<p>chapter1 content</p>")
            book.spine.add(it)
            book.toc[0].sub_catalog!!.add(Catalog(it))
        }

    book.addEpubHtml(title = "chapter2", filePath = "chapter2.xhtml")
        .let {
            it.setContent("<p>chapter2 content</p>")
            book.spine.add(it)
            book.toc[0].sub_catalog!!.add(Catalog(it))
        }

    book.addEpubHtml(title = "volume2", filePath = "volume2.xhtml")
        .let {
            it.setContent("<p>volume2 content</p>")
            book.spine.add(it)
            book.toc.add(Catalog(it).apply { this.sub_catalog = ArrayList() })
        }

    book.addEpubHtml(title = "chapter3", filePath = "chapter3.xhtml")
        .let {
            it.setContent("<p>chapter3 content</p>")
            book.spine.add(it)
            book.toc[1].sub_catalog!!.add(Catalog(it))
        }

    book.addEpubHtml(title = "chapter4", filePath = "chapter4.xhtml")
        .let {
            it.setContent("<p>chapter4 content</p>")
            book.spine.add(it)
            book.toc[1].sub_catalog!!.add(Catalog(it))
        }
}