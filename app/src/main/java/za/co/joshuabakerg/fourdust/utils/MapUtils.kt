package za.co.joshuabakerg.fourdust.utils

import android.text.TextUtils

fun <T> traverse(map: Map<*, *>, path: String): T? {
    var currentValue: Any? = map
    val values = path.split("/")
    for (value in values) {
        if(!TextUtils.isEmpty(value)){
            if(currentValue is Map<*, *>){
                currentValue = currentValue[value]
            }else{
                return null
            }
        }
    }
    return currentValue as T
}