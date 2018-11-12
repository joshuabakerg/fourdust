package za.co.joshuabakerg.fourdust

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import za.co.joshuabakerg.fourdust.Components.ChatListItem
import za.co.joshuabakerg.fourdust.utils.getHttp
import za.co.joshuabakerg.fourdust.utils.inBackground
import za.co.joshuabakerg.fourdust.utils.putExtra
import za.co.joshuabakerg.fourdust.utils.traverse

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    enum class AppSate {
        MAIN,
        IN_CHATS,
        IN_MESSAGE
    }

    private var currentState = AppSate.MAIN
    private var backFlow = HashMap<AppSate, AppSate>()
    private var methodForState = HashMap<AppSate, () -> Unit>()


    private lateinit var chatService: ChatService
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backFlow[AppSate.IN_MESSAGE] = AppSate.IN_CHATS
        backFlow[AppSate.IN_CHATS] = AppSate.MAIN
        methodForState[AppSate.IN_CHATS] = this::displayAllChats
        methodForState[AppSate.MAIN] = this::displayMain

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            displayAllChats()
            Snackbar.make(view, "Showing all chats", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        chatService = ChatService.instance
        handler = Handler(applicationContext.mainLooper)
        requestLogin()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            goBackAndRun()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val start = System.currentTimeMillis()
        when (item.itemId) {
            R.id.nav_camera -> {
                val intent = Intent(applicationContext, ChatActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {
                val messageSocket = getMessageSocket()
                val data = LinkedHashMap<String, Any>()
                data["convId"] = "conv1"
                messageSocket.emit("join-chat", data)
            }
            R.id.nav_send -> {
                displayAllChats()
            }
            R.id.nav_logout -> {
                inBackground(getHttp("http://test.joshuabakerg.co.za/services/user/logout"))
                        .subscribe {}
                val fourdustSharedPreferences = getSharedPreferences("fourdust", Context.MODE_PRIVATE)
                val edit = fourdustSharedPreferences.edit()
                edit.putString("sessionid", null)
                edit.commit()
                requestLogin()
            }
        }
        println("took ${System.currentTimeMillis() - start} to finish sidebar")
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun goBackAndRun() {
        val backState = backFlow[currentState]
        if (backState != null) {
            val function = methodForState[backState]
            if (function != null) {
                function()
            } else {
                Log.e(this::class.java.name, "There is no state command for $backState")
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun displayAllChats() {
        currentState = AppSate.IN_CHATS
        val start = System.currentTimeMillis()
        println("took ${System.currentTimeMillis() - start} to create progress bar")
        ll.removeAllViews()
        chatService.getChatDetails(true)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    it.forEach {
                        ChatListItem(ll, applicationContext)
                                .create(it)
                                .subscribe{
                                    val intent = Intent(applicationContext, ChatActivity::class.java)
                                    putExtra(intent, it)
                                    startActivity(intent)
                                }
                        val space = Space(applicationContext)
                        space.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10)
                        handler.post {
                            ll.addView(space)
                        }
                    }

                }
    }

    private fun onChatClicked(chatDetails: ChatDetails) {
        currentState = AppSate.IN_MESSAGE
        ll.removeAllViews()
        chatService.getMessages(chatDetails.messages!!)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    it.forEach {
                        val textView = TextView(applicationContext)
                        textView.text = "${it.from}:\t${it.content}".removeSuffix("\n")
                        handler.post {
                            ll.addView(textView)
                        }
                        println(it.content)
                    }
                    val editText = EditText(applicationContext)
                    editText.hint = "Type a message"
                    editText.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    val button = Button(applicationContext)
                    button.text = "Send"
                    button.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    button.setOnClickListener {
                        if (chatDetails.id != null) {
                            chatService.sendMessage(chatDetails.id!!, editText.text.toString())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe()
                            editText.text.clear()
                        }
                    }
                    handler.post {
                        ll.addView(editText)
                        ll.addView(button)
                    }
                }
    }

    private fun displayMain() {
        currentState = AppSate.MAIN
        ll.removeAllViews()
    }

    override fun onResume() {
        super.onResume()
        val userStorage = UserSession.instance.user
        if (userStorage !== null) {
            getHttp("http://test.joshuabakerg.co.za/services/user/", LinkedHashMap::class.java)
                    .subscribeOn(Schedulers.io())
                    .subscribeAndPost(applicationContext) {
                        val user = it
                        var name = traverse<String>(user, "name/first") + " " + traverse<String>(user, "name/last")
                        var email = traverse<String>(user, "email")

                        nameText.text = name
                        textView.text = email
                        val imageUrl = traverse<String>(user, "picture/thumbnail")
                        if (imageUrl != null) {
                            imageView.setImageURL(imageUrl, true)
                        }
                    }
        }
    }

    private fun requestLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}