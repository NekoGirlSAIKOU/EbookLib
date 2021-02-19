package pers.nekogirlsaikou.ebooklib.epub

import org.jsoup.nodes.Element

data class EpubMeta(var namespace:String?,var name:String, var value:String?,var others:MutableMap<String,String>?=null){

    internal fun addToMetadata(metadata:Element):Element{
        val meta:Element = if (namespace == null){
            metadata.appendElement(name)
        } else {
            metadata.appendElement("$namespace:$name")
        }
        if (value != null){
            meta.text(value)
        }
        if (others != null){
            for (key:String in others!!.keys){
                meta.attr(key, others!![key])
            }
        }
        return meta
    }
}
