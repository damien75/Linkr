package sara.damien.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by Sara-Fleur on 2/25/14.
 */
public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
    Profile parent;
    public String error;

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

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            byte[] contents = buffer.toByteArray();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            image = BitmapFactory.decodeStream(new ByteArrayInputStream(contents), null, options);
            int ratio = Math.max(options.outHeight / 200, options.outWidth / 200);

            options.inJustDecodeBounds = false;
            options.inSampleSize = ratio;
            image = BitmapFactory.decodeStream(new ByteArrayInputStream(contents), null, options);
        } catch (Exception e) {
            Log.e("YADAYADA", e.getMessage());
            e.printStackTrace();
            error = e.getMessage();
        }

        error += "   ";
        return image;
    }

    protected void onPostExecute(Bitmap result) {
        parent.onImageReceived(result);
    }
}