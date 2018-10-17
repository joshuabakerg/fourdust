package za.co.joshuabakerg.fourdust

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.w3c.dom.Text
import za.co.joshuabakerg.fourdust.utils.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var chatService: ChatService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        chatService = ChatService.instance
        requestLogin()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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
                val start = System.currentTimeMillis()
                val progress = ProgressDialog(this)
                progress.setTitle("Loading")
                progress.setMessage("Wait while loading...")
                progress.setCancelable(false)
                progress.show()
                println("took ${System.currentTimeMillis() - start} to create progress bar")
                inBackground(chatService.getChatDetails().subscribeOn(Schedulers.io()))
                        .subscribe {
                            val imageLoads = ArrayList<Pair<String, ImageView>>()
                            ll.removeAllViews()
                            it.forEach {
                                //Image
                                val image = it.userDetails[it.mainUser]?.image
                                val imageView = ImageView(applicationContext)
                                imageView.layoutParams = LinearLayout.LayoutParams(150, 150)
                                imageView.setImageResource(R.drawable.unknown)
                                imageLoads.add(Pair(image!!, imageView))

                                //Name
                                val name = it.userDetails[it.mainUser]?.name
                                val textView = TextView(applicationContext)
                                textView.textSize = 20f
                                textView.text = name
                                textView.gravity = Gravity.CENTER_VERTICAL

                                //Container
                                val linearLayout = LinearLayout(applicationContext)
                                linearLayout.orientation = LinearLayout.HORIZONTAL

                                linearLayout.addView(imageView)
                                linearLayout.addView(textView)

                                ll.addView(linearLayout)
                            }
                            applyUrlToImages(imageLoads).subscribe {
                                progress.dismiss()
                                ImageHelper.roundImageView(it.first, 50)
                            }

                        }
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
            R.id.nav_logout -> {
                inBackground(getHttp("http://test.joshuabakerg.co.za/services/user/logout"))
                        .subscribe {
                            requestLogin()
                        }
            }
        }
        println("took ${System.currentTimeMillis() - start} to finish sidebar")
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onResume() {
        super.onResume()
        val user = UserSession.instance.user
        if (user !== null) {
            var name = traverse<String>(user, "name/first") + " " + traverse<String>(user, "name/last")
            var email = traverse<String>(user, "email")
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email)) {
                nameText.text = name
                textView.text = email
            }
            val imageUrl = traverse<String>(user, "picture/thumbnail")
            if (imageUrl != null) {
                applyUrlToImage(imageUrl, imageView)
                        .subscribe {
                            ImageHelper.roundImageView(it, 50)
                        }
            }
        }
    }

    private fun requestLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
