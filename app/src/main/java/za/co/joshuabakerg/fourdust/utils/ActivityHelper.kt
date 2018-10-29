package za.co.joshuabakerg.fourdust.utils

import android.content.Intent
import com.fasterxml.jackson.databind.ObjectMapper

val objectMapper = ObjectMapper()

fun putExtra(intent: Intent, obj: Any) {
    val stringObj = objectMapper.writeValueAsString(obj)
    intent.putExtra(obj.javaClass.name, stringObj)
}

fun <T> getExtra(intent: Intent, clazz: Class<T>) : T {
    val stringObj = intent.extras.getString(clazz.name)
    return objectMapper.readValue(stringObj, clazz)
}
