package za.co.joshuabakerg.fourdust.utils;

import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadImage extends AsyncTask<Void, Void, Bitmap> {

    OkHttpClient client = new OkHttpClient();

    private Callback<Bitmap> callback;
    private ImageView imageView;
    private final String url;

    public DownloadImage(String url, Callback<Bitmap> callback) {
        this.url = url;
        this.callback = callback;
    }

    public DownloadImage(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        try {
            long start = System.currentTimeMillis();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();
            Bitmap imageBitmap = BitmapFactory.decodeStream(body.byteStream());
            Log.i("TIMER", "took " + (System.currentTimeMillis() - start) + " to download " + url);
            return imageBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(final Bitmap success) {
        if (callback != null) {
            callback.call(success);
        }
        if (imageView != null) {
            imageView.setImageBitmap(success);
        }
    }
}