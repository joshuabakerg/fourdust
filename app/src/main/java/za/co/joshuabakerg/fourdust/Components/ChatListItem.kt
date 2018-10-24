package za.co.joshuabakerg.fourdust.Components

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import za.co.joshuabakerg.fourdust.ChatDetails
import za.co.joshuabakerg.fourdust.R
import za.co.joshuabakerg.fourdust.setImageURL

class ChatListItem internal constructor(private val attachTo: ViewGroup, private val context: Context) {


    fun create(chatDetails: ChatDetails): Flowable<ChatDetails> {

        val handler = Handler(context.mainLooper)

        val image = chatDetails.userDetails[chatDetails.mainUser]?.image
        val imageView = ImageView(context)
        imageView.layoutParams = LinearLayout.LayoutParams(150, 150)
        imageView.setImageResource(R.drawable.unknown)
        imageView.setImageURL(image!!, true)
        //Name Container
        val nameLayout = LinearLayout(context)
        nameLayout.orientation = LinearLayout.VERTICAL
        nameLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        //Name
        val name = chatDetails.userDetails[chatDetails.mainUser]?.name
        val textView = TextView(context)
        textView.textSize = 20f
        textView.setPadding(20, 0, 0, 0)
        textView.text = name
        textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val available = chatDetails.userDetails[chatDetails.mainUser]?.available
        val availableView = TextView(context)
        availableView.textSize = 12f
        availableView.setPadding(20, 0, 0, 0)
        availableView.text = available
        availableView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        nameLayout.addView(textView)
        nameLayout.addView(availableView)

        //Container
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        linearLayout.setBackgroundColor(Color.parseColor("#FFE1E4CC"))

        linearLayout.addView(imageView)
        linearLayout.addView(nameLayout)
        handler.post {
            attachTo.addView(linearLayout)
        }
        return Flowable.create({
            val emitter = it
            linearLayout.setOnClickListener {
                emitter.onNext(chatDetails)
            }
        }, BackpressureStrategy.BUFFER)
    }

}