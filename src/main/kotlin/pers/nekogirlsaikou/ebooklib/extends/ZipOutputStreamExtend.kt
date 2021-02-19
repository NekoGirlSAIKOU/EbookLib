package pers.nekogirlsaikou.ebooklib.extends

import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal fun ZipOutputStream.writeDeflatedFile(path:String,content:ByteArray?) {
    setMethod(ZipOutputStream.DEFLATED)
    val ze = ZipEntry(path)
    putNextEntry(ze)
    write(content ?: ByteArray(0))
    closeEntry()
}

internal fun ZipOutputStream.writeStoredFile(path:String,content: ByteArray?) {
    val content:ByteArray = content ?:ByteArray(0)
    setMethod(ZipOutputStream.STORED)
    val ze = ZipEntry(path)
    val crc32 = CRC32()
    crc32.update(content, 0, content.size)
    ze.size = content.size.toLong()
    ze.crc = crc32.value
    putNextEntry(ze)
    write(content)
    closeEntry()
}