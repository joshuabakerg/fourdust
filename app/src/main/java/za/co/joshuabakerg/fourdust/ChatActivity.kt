package za.co.joshuabakerg.fourdust

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.content_chat.*
import za.co.joshuabakerg.fourdust.utils.getExtra
import za.co.joshuabakerg.fourdust.utils.traverse

class ChatActivity : AppCompatActivity() {

    private lateinit var chatDetails: ChatDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)

        chatDetails = getExtra(intent, ChatDetails::class.java)
        val mainUser = chatDetails.userDetails[chatDetails.mainUser]!!

        title = mainUser.name
        imageView3.setImageURL(mainUser.image!!, true)

        ChatService.instance.getMessages(chatDetails.messages!!)
                .subscribeOn(Schedulers.io())
                .subscribeAndPost(applicationContext, {
                    it.forEach(::addMessage)
                    chatScrollView.post {
                        chatScrollView.fullScroll(View.FOCUS_DOWN)
                    }
                }, {
                    Toast.makeText(applicationContext, "Failed to get messages with ${chatDetails.messages}", Toast.LENGTH_LONG).show()
                    Log.e(javaClass.name, "Failed to get messages for messageId ${chatDetails.messages}", it)
                })

    }

    fun addMessage(it: ChatMessage) {
        val textView = TextView(applicationContext)
        textView.text = it.content?.removeSuffix("\n")
        textView.textSize = 15f
        textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.setTextColor(Color.BLACK)
        if (!it.from.equals(chatDetails.mainUser)) {
            textView.gravity = Gravity.RIGHT
            textView.setBackgroundColor(0xFFd8f4b5.toInt())
        } else {
            textView.gravity = Gravity.LEFT
            textView.setBackgroundColor(0xFFc6c6c6.toInt())
        }
        messagesLayout.addView(textView)
        val space = Space(applicationContext)
        space.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20)
        messagesLayout.addView(space)
    }

    fun onSendButtonClick(view: View) {
        val messageToSend = messageTextView.text.toString()
        messageTextView.text.clear()
        ChatService.instance.sendMessage(chatDetails.id!!, messageToSend)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    println("sent")
                }
        val chatMessage = ChatMessage()
        chatMessage.from = traverse(UserSession.instance.user!!, "login/username")
        chatMessage.content = messageToSend
        chatMessage.time = System.currentTimeMillis()
        addMessage(chatMessage)
        chatScrollView.post {
            chatScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    fun onBackClicked(view: View) {
        finish()
    }

}
