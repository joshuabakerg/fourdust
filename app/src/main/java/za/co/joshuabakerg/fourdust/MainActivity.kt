package za.co.joshuabakerg.fourdust

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import za.co.joshuabakerg.fourdust.R.id.ll
import za.co.joshuabakerg.fourdust.utils.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (UserSession.instance.user === null) {
            requestLogin()
        }
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
        when (item.itemId) {
            R.id.nav_camera -> {

                var start = System.currentTimeMillis()
                inBackground(
                        getHttp("http://test.joshuabakerg.co.za/services/chat/", LinkedHashMap::class.java)
                ).map {
                    traverse<List<*>>(it, "chat")
                }.subscribe {
                    start = System.currentTimeMillis();
                    val flowables = it!!.map {
                        with(it as LinkedHashMap<String, Any>) {
                            val mainUser = this["mainUser"] as String
                            traverse<String>(this, "userDetails/$mainUser/image")
                        }
                    }.map {
                        println(it)
                        val url = it
                        fetchImage(url!!)
                                .map {
                                    object {
                                        val bitmap = it
                                        val url = url
                                    }
                                }
                    }
                    println(flowables)
                    inBackground(Flowable.fromIterable(flowables)
                            .flatMap {
                                it.subscribeOn(Schedulers.io())
                            }
                    ).subscribe {
                        println("Took ${System.currentTimeMillis()-start} to download all images")
                        val imageView = ImageView(applicationContext)
                        imageView.setImageBitmap(it.bitmap)
                        imageView.layoutParams = ViewGroup.LayoutParams(200, 200)
                        ll.addView(imageView)
                    }
                }
                println("after")
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
                UserSession.instance.user = null
                requestLogin()
            }
        }

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
                fetchImage(imageUrl)
                        .subscribeOn(Schedulers.io())
                        .blockingSubscribe {
                            val roundedCornerBitmap = ImageHelper.getRoundedCornerBitmap(it, 20)
                            imageView.setImageBitmap(roundedCornerBitmap)
                        }
            }
        }
    }

    fun requestLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
