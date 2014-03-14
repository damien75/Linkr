package sara.damien.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

// LATER: Keep a local copy of each parameters
public class LinkrPreferences {
    private static SharedPreferences prefs;

    public LinkrPreferences(Context appContext) {
        prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    public boolean getConnected() {
        return prefs.getBoolean("Connected", false);
    }

    public synchronized void setConnected(boolean connected) { //TODO: Use everywhere
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("Connected", connected);
        editor.apply();
    }

    public String getID() {
        String id = prefs.getString("ID", null);
        return id; //TODO pour SF: Throw an exception if id == null;
    }

    public synchronized void setID(String id) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ID", id);
        editor.apply();
    }

    public void setLinkedInAuthTokens(String token, String secret) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", token);
        editor.putString("secret",secret);
        editor.apply();
    }

    public String getLastMessageTimeStamp() {
        return prefs.getString("TimeStamp", "2014-02-28 16:27:40");
    }

    public void setLastMessageTimeStamp(String latestTimeStamp) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("TimeStamp", latestTimeStamp);
        editor.apply();
    }

    /* FIXME: Je pense vraiment que le blocage devrait être fait côté serveur
    public Set<String> getBlockedIDs() {
        return prefs.getStringSet("blockedIDs", new HashSet<String>());
    }
     */
}
