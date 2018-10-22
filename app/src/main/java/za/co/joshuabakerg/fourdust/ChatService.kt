package za.co.joshuabakerg.fourdust

import android.graphics.Bitmap
import io.reactivex.Flowable
import za.co.joshuabakerg.fourdust.Exception.ServiceException
import za.co.joshuabakerg.fourdust.utils.fetchImageCache
import za.co.joshuabakerg.fourdust.utils.getHttp

class ChatService private constructor(){

    private var cacheDOB = System.currentTimeMillis()
    private var cachedChatDetails: List<ChatDetails>? = null

    fun getChatDetailsCache(): Flowable<List<ChatDetails>> {
        val age = (System.currentTimeMillis() - cacheDOB)
        println("Age of cache ${age}")
        return if(cachedChatDetails != null &&  age < 1000000){
            Flowable.just(cachedChatDetails)
        }else {
            getChatDetails()
        }
    }

    fun getChatDetails(): Flowable<List<ChatDetails>> {
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
    val from: String? = null
    val content: String? = null
    val pic: String? = null
    val time: Long? = null
}