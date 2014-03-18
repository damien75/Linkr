package sara.damien.app;

import android.app.Application;

/* NOTE: The doc (http://developer.android.com/reference/android/app/Application.html) suggests that
 * subclassing Application is generally unneeded, and instead points to static singleton classes.
 * The apporach implemented here is indeed to use singleton classes, but instead of passing an
 * explicit Context every time the Common singleton instance is retrieved, we initialize it on app
 * launch. http://stackoverflow.com/questions/987072/using-application-context-everywhere suggests
 * this is fine.
 */
public class LinkrApplication extends Application {
    @Override
    public void onCreate () {
        super.onCreate();
        Common.Init(this);

    }
}
