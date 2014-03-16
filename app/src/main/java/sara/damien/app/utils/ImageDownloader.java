package sara.damien.app.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;

import sara.damien.app.LinkrAPI;
import sara.damien.app.Profile;

/**
 * Created by Sara-Fleur on 2/25/14.
 */
public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
    Profile parent;

    public ImageDownloader(Profile parent) {
        this.parent = parent;
    }

    @Override
    protected Bitmap doInBackground(String... ids) {
        if (ids == null || ids.length < 1)
            return null;

        String id = ids[0];
        return LinkrAPI.downloadProfilePicture(id);
    }

    protected void onPostExecute(Bitmap result) {
        parent.onImageReceived(result);
    }
}