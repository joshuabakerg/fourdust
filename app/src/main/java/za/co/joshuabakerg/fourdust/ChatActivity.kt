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

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)

        val image = intent.extras.getString("image")
        val name = intent.extras.getString("name")
        val messaeId = intent.extras.getString("messagesId")


        title = name
        imageView3.setImageURL(image, true)

        ChatService.instance.getMessages(messaeId)
                .subscribeOn(Schedulers.io())
                .subscribeAndPost(applicationContext, {
                    it.forEach {
                        val textView = TextView(applicationContext)
                        textView.text = it.content?.removeSuffix("\n")
                        textView.textSize = 15f
                        textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        textView.setTextColor(Color.BLACK)
                        if (it.from != name) {
                            textView.gravity = Gravity.RIGHT
                            textView.setBackgroundColor(0xFFb3d389.toInt())
                        } else {
                            textView.setBackgroundColor(Color.BLUE)
                        }
                        messagesLayout.addView(textView)
                        val space = Space(applicationContext)
                        space.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20)
                        messagesLayout.addView(space)
                    }
                    chatScrollView.post {
                        chatScrollView.fullScroll(View.FOCUS_DOWN)
                    }
                }, {
                    Toast.makeText(applicationContext, "Failed to get messages with $name", Toast.LENGTH_LONG).show()
                    Log.e(javaClass.name, "Failed to get messages for messageId $messaeId", it)
                })

    }

    fun onBackClicked(view: View) {
        finish()
    }

}
