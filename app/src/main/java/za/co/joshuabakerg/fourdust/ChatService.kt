package za.co.joshuabakerg.fourdust

import android.graphics.Bitmap
import io.reactivex.Flowable
import za.co.joshuabakerg.fourdust.Exception.ServiceException
import za.co.joshuabakerg.fourdust.utils.fetchImageCache
import za.co.joshuabakerg.fourdust.utils.getHttp
import za.co.joshuabakerg.fourdust.utils.postHttp

class ChatService private constructor(){

    private var cacheDOB = System.currentTimeMillis()
    private var cachedChatDetails: List<ChatDetails>? = null

    fun getChatDetails(useCache: Boolean = false): Flowable<List<ChatDetails>> {
        val age = (System.currentTimeMillis() - cacheDOB)
        return if(useCache && cachedChatDetails != null &&  age < 1000000){
            println("Age of cache ${age}")
            Flowable.just(cachedChatDetails)
        }else {
            getChatDetailsInteral()
        }
    }

    fun getMessages(messageId: String): Flowable<List<ChatMessage>> {
        val url = "http://test.joshuabakerg.co.za/services/chat/messages/$messageId"
        return getHttp(url, ChatMessagesResponse::class.java)
                .map {
                    if (it.success!!) {
                        it.messages
                    } else {
                        throw ServiceException("Failed to get chat details [${it.message}]")
                    }
                }
    }

    fun sendMessage(convId: String, message: String): Flowable<java.util.LinkedHashMap<*, *>> {

        val req = object{
            val content = message
        }
        val url = "http://test.joshuabakerg.co.za/services/chat/conversation/$convId"
        return postHttp(url, req, LinkedHashMap::class.java)
    }

    private fun getChatDetailsInteral(): Flowable<List<ChatDetails>> {
        val url = "http://test.joshuabakerg.co.za/services/chat/"
        return getHttp(url, ChatDetailsResponse::class.java)
                .map {
                    if (it.success!!) {
                        cachedChatDetails = it.chat
                        cacheDOB = System.currentTimeMillis()
                        it.chat
                    } else {
                        throw ServiceException("Failed to get chat details [${it.message}]")
                    }
                }
    }

    init {  }

    private object Holder { val INSTANCE = ChatService() }

    companion object {
        val instance: ChatService by lazy { Holder.INSTANCE }
    }

}

open class BasicResponse {
    var success: Boolean? = null
    var message: String? = null
}

class ChatDetailsResponse : BasicResponse() {
    var chat: List<ChatDetails> = ArrayList()
}

class ChatDetails {

    var id: String? = null
    var messages: String? = null
    var users: List<String> = ArrayList()
    var mainUser: String? = null
    var userDetails: Map<String, UserDetails> = HashMap()

}

class UserDetails {
    var image: String? = null
    var name: String? = null
    var available: String? = null
    var convIds: Any? = null
    var status: String? = null

    var imageBitmap: Bitmap? = null

    fun getImageBitmap(): Flowable<Bitmap>? {
        if (image != null) {
            return if(imageBitmap == null) {
                fetchImageCache(image!!)
                        .map {
                            this.imageBitmap = it
                            it
                        }
            }else{
                Flowable.just(this.imageBitmap)
            }
        }else{
            return null
        }
    }
}

class ChatMessagesResponse : BasicResponse(){
    val messages: List<ChatMessage> = ArrayList()
}

class ChatMessage{
    var from: String? = null
    var content: String? = null
    var pic: String? = null
    var time: Long? = null
}