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
        println("Age og cache ${age}")
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
                        throw ServiceException("Falied to get chat details [${it.message}]")
                    }
                }
    }

    init {  }

    private object Holder { val INSTANCE = ChatService() }

    companion object {
        val instance: ChatService by lazy { Holder.INSTANCE }
    }

}

class ChatDetailsResponse {
    var success: Boolean? = null
    var message: String? = null
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