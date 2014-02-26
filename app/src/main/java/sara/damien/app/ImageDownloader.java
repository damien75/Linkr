package sara.damien.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;

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
        Bitmap image = null;

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("http://golinkr.net/get_picture.php?ID=" + id);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream is = httpEntity.getContent();
            image = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    protected void onPostExecute(Bitmap result) {
        parent.onImageReceived(result);
    }
}