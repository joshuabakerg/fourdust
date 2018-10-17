package za.co.joshuabakerg.fourdust.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import za.co.joshuabakerg.fourdust.UserSession
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

val cachedImages = ConcurrentHashMap<String, Bitmap>()

fun applyUrlToImages(imagePairs: List<Pair<String, ImageView>>) :Flowable<Pair<ImageView, Bitmap>>{
    return Flowable.create({
        val emitter = it
        imagePairs.filter {
            cachedImages.containsKey(it.first)
        }.forEach {
            val bitmap = cachedImages[it.first]
            it.second.setImageBitmap(bitmap)
            emitter.onNext(Pair(it.second, bitmap!!))
        }
        val imageGets = imagePairs.filter {
            !cachedImages.containsKey(it.first)
        }.map {
            val pair = it
            fetchImage(pair.first).map {
                cachedImages[pair.first] = it
                Pair(pair.second, it)
            }
        }
        if(imageGets.isNotEmpty()) {
            inBackground(
                    Flowable.fromIterable(imageGets)
                            .flatMap { it.subscribeOn(Schedulers.io()) }
            ).subscribe {
                it.first.setImageBitmap(it.second)
                emitter.onNext(it)
            }
        }
    }, BackpressureStrategy.BUFFER)
}

fun applyUrlToImage(url: String, imageView: ImageView) : Flowable<ImageView>{
    return Flowable.create({
        val emitter = it
        if (cachedImages.containsKey(url)) {
            imageView.setImageBitmap(cachedImages[url])
            emitter.onNext(imageView)
            emitter.onComplete()
        } else {
            inBackground(fetchImage(url).subscribeOn(Schedulers.io()))
                    .subscribe {
                        cachedImages[url] = it
                        imageView.setImageBitmap(it)
                        emitter.onNext(imageView)
                        emitter.onComplete()
                    }
        }
    }, BackpressureStrategy.BUFFER)
}

fun fetchImage(url: String): Flowable<Bitmap> {
    return Flowable.fromCallable<Bitmap> {
        val start = System.currentTimeMillis()
        val request = Request.Builder().url(url).header("Cookie", "sessionid=" + UserSession.instance.sessionID).get().build()
        val response = OkHttpClient().newCall(request).execute()
        val body = response.body()
        Log.i("HttpUtils", "Took ${System.currentTimeMillis() - start}ms to download $url")
        BitmapFactory.decodeStream(body!!.byteStream())
    }
}

fun getHttp(url: String): Flowable<String> {
    return Flowable.fromCallable<String> {
        val start = System.currentTimeMillis()
        val request = Request.Builder()
                .url(url)
                .header("Cookie", "sessionid=" + UserSession.instance.sessionID)
                .get()
                .build()
        val response = OkHttpClient().newCall(request).execute()
        val body = response.body()
        Log.i("HttpUtils", "Took ${System.currentTimeMillis() - start}ms to download $url")
        body?.string()
    }
}

fun <T> getHttp(url: String, clazz: Class<T>): Flowable<T> {
    val objectMapper = ObjectMapper()
    return getHttp(url).map { objectMapper.readValue(it, clazz) }
}

fun <T> inBackground(sad: Flowable<T>): Flowable<T> {
    return Flowable.create({
        TestAsync(sad, it).execute()
    }, BackpressureStrategy.BUFFER)
}

class TestAsync<T> internal constructor(private val flowable: Flowable<T>, private val newFlowable: FlowableEmitter<T>) : AsyncTask<Void, Void, List<T>>() {


    override fun doInBackground(vararg params: Void?): List<T>? {
        var res = CopyOnWriteArrayList<T>()
        var start = System.currentTimeMillis()
        flowable.blockingSubscribe {
            res.add(it)
        }
        println("in doInBackground")
        return res
    }

    override fun onPostExecute(result: List<T>?) {
        println("in onPostExecute")
        result?.forEach {
            newFlowable.onNext(it)
        }
    }

}