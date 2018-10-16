package za.co.joshuabakerg.fourdust.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import za.co.joshuabakerg.fourdust.UserSession

fun fetchImage(url: String) : Flowable<Bitmap>  {
    return Flowable.fromCallable<Bitmap> {
        val request = Request.Builder().url(url).header("Cookie", "sessionid="+UserSession.instance.sessionID).get().build()
        val response = OkHttpClient().newCall(request).execute()
        val body = response.body()
        BitmapFactory.decodeStream(body!!.byteStream())
    }
}

fun getHttp(url: String): Flowable<String>{
    return Flowable.fromCallable<String> {
        val request = Request.Builder()
                .url(url)
                .header("Cookie", "sessionid="+UserSession.instance.sessionID)
                .get()
                .build()
        val response = OkHttpClient().newCall(request).execute()
        val body = response.body()
        body!!.string()
    }
}

fun <T> getHttp(url: String, clazz: Class<T>): Flowable<T>{
    val objectMapper = ObjectMapper()
    return getHttp(url).map { objectMapper.readValue(it, clazz) }
}

fun <T> inBackground(sad: Flowable<T>): Flowable<T>{
    return Flowable.create({
        TestAsync(sad, it).execute()
    }, BackpressureStrategy.BUFFER)
}

class TestAsync <T> internal constructor(private val flowable: Flowable<T>, private val newFlowable: FlowableEmitter<T>) : AsyncTask<Void, Void, ArrayList<T>>() {


    override fun doInBackground(vararg params: Void?): ArrayList<T>? {
        var res = ArrayList<T>()
        flowable.subscribeOn(Schedulers.io())
                .blockingSubscribe{
                    res.add(it)
                }
        println("in doInBackground")
        return res
    }

    override fun onPostExecute(result: ArrayList<T>?) {
        println("in onPostExecute")
        result?.forEach {
            newFlowable.onNext(it)
        }
    }

}