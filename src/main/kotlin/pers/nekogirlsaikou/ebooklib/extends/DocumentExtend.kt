package pers.nekogirlsaikou.ebooklib.extends

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

internal fun Document.getRootElement(): Element {
    return getElementsByTag("html")[0]
}

internal fun Document.setAttribute(attributeKey:String,attributeValue:String){
    getRootElement().attr(attributeKey,attributeValue)
}

internal fun Document.setAttribute(attributeKey:String,attributeValue:Boolean){
    getRootElement().attr(attributeKey,attributeValue)
}

internal fun Document.getAttribute(attributeKey:String):String{
    return getRootElement().attr(attributeKey)
}