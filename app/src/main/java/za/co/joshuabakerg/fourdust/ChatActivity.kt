package za.co.joshuabakerg.fourdust

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)

        val image = intent.extras.getString("image")
        val name = intent.extras.getString("name")
        title = name
        imageView3.setImageURL(image, true)

    }

}
