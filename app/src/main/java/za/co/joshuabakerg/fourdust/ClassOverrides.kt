package za.co.joshuabakerg.fourdust

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import za.co.joshuabakerg.fourdust.utils.ImageHelper
import za.co.joshuabakerg.fourdust.utils.applyUrlToImage

fun <T> Flowable<T>.subscribeAndPost(applicationContext: Context, onSuccess: (T) -> Unit): Disposable? {
    return this.subscribeAndPost(applicationContext, onSuccess, null)
}

fun <T> Flowable<T>.subscribeAndPost(applicationContext: Context, onSuccess: (T) -> Unit, onError: ((Throwable) -> Unit)? = null): Disposable? {
    val handler = Handler(applicationContext.mainLooper)
    return this.subscribe({
        handler.post {
            onSuccess.invoke(it)
        }
    }, {
        if (onError != null) {
            onError.invoke(it)
        } else {
            Log.e(this::javaClass.name, "Default flowable error", it)
        }
    })
}

fun ImageView.roundImage(pixels: Int) {
    ImageHelper.roundImageView(this, pixels)
}

fun ImageView.setImageURL(imageUrl: String, roundImage: Boolean = false) {
    println("here")
    applyUrlToImage(imageUrl, this)
            .subscribeOn(Schedulers.io())
            .subscribeAndPost(this.context, {
                if (roundImage) {
                    this.roundImage(50)
                }
            }, {
                handler.post {
                    Toast.makeText(context, "Failed to get image $imageUrl", Toast.LENGTH_LONG).show()
                }
            })
}
