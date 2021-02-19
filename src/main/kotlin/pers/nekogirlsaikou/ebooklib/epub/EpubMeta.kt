package pers.nekogirlsaikou.ebooklib.epub

import org.jsoup.nodes.Element

data class EpubMeta(
    var namespace: String?,
    var name: String,
    var value: String?,
    var others: MutableMap<String, String>? = null
) {

    fun attr (key:String,value: String):EpubMeta{
        if (others == null){
            others = HashMap()
        }
        others!!.set(key,value)
        return this
    }

    fun attr (key:String):String?{
        return others?.get(key)
    }

    internal fun addToMetadata(metadata: Element): Element {
        val meta: Element = if (namespace == null) {
            metadata.appendElement(name)
        } else {
            metadata.appendElement("$namespace:$name")
        }
        if (value != null) {
            meta.text(value)
        }
        others?.keys?.forEach { key ->
            meta.attr(key, others!![key])
        }
        return meta
    }
}
