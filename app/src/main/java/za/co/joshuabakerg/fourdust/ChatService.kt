package za.co.joshuabakerg.fourdust

import io.reactivex.Flowable
import za.co.joshuabakerg.fourdust.Exception.ServiceException
import za.co.joshuabakerg.fourdust.utils.getHttp

class ChatService private constructor(){

    private var cachedChatDetails: List<ChatDetails>? = null

    fun getChatDetails(): Flowable<List<ChatDetails>> {
        val url = "http://test.joshuabakerg.co.za/services/chat/"
        return if(cachedChatDetails != null){
            Flowable.just(cachedChatDetails)
        }else {
            getHttp(url, ChatDetailsResponse::class.java)
                    .map {
                        if (it.success!!) {
                            cachedChatDetails = it.chat
                            it.chat
                        } else {
                            throw ServiceException("Falied to get chat details [${it.message}]")
                        }
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
}