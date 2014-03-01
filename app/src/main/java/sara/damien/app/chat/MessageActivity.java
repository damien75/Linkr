package sara.damien.app.chat;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sara.damien.app.FeedReaderContract.FeedEntry;
import sara.damien.app.FeedReaderDbHelper;
import sara.damien.app.R;
import sara.damien.app.utils.JSONParser;

public class MessageActivity extends ListActivity {

    /** Called when the activity is first created. */

    ArrayList<Message> messages;
    MessageAdapter adapter;
    EditText text;
    static String sender;
    String IDm;
    String currentID;
    String myID;
    String First_Name;
    String Last_Name;
    String Subject;
    JSONParser jsonParser;
    private static String url ="http://www.golinkr.net";
    String latestTimeStamp;
    FeedReaderDbHelper mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myID = prefs.getString("ID","1");
        latestTimeStamp = prefs.getString("TimeStamp", "2014-02-28 16:27:40");

        text = (EditText) this.findViewById(R.id.messageEditor);

        mDbHelper = new FeedReaderDbHelper(getApplicationContext());

        Bundle bundle = getIntent().getExtras();
        currentID = bundle.getString("IDu");
        IDm = bundle.getString("IDm");
        First_Name = bundle.getString("First_Name");
        Last_Name = bundle.getString("Last_Name");
        Subject = bundle.getString("Subject");

        sender = First_Name+" "+Last_Name;
        this.setTitle(sender);

        messages = new ArrayList<Message>();

        messages.add(new Message("Hello", false,true));
        messages.add(new Message("Hi!", true,true));

        LocalCall lc = new LocalCall();
        lc.execute();
        adapter = new MessageAdapter(this, messages);
        setListAdapter(adapter);
        addNewMessage(new Message("mmm, well, using 9 patches png to show them.", true,true));

        callCheckNewMessages();

    }
    public void sendMessage(View v){
        String newMessage = text.getText().toString().trim();
        if(newMessage.length() > 0){
            text.setText("");
            //TODO ajouter qqch pour dire que le message n'a pas encore été envoyé
            addNewMessage(new Message(newMessage, true,false));
            SendMessage sendMessage = new SendMessage();
            sendMessage.message = newMessage;
            sendMessage.execute();
        }
    }

    private class SendMessage extends AsyncTask<Void, Void, Void>{
        private String message;
        @Override
        protected Void doInBackground(Void... args) {
            jsonParser = new JSONParser();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "addMessage"));
            params.add(new BasicNameValuePair("ID1", myID));
            params.add(new BasicNameValuePair("ID2", currentID));
            params.add(new BasicNameValuePair("Message",message));
            JSONObject json = jsonParser.makeHttpRequest(url,"POST",params);

            try {
                 boolean isSent = json.getBoolean("success");
                 if (isSent){
                     String IDmsg = json.getString("lastID");
                     messages.get(messages.size()-1).setSent();
                     // Gets the data repository in write mode
                     SQLiteDatabase db = mDbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
                     ContentValues values = new ContentValues();
                     values.put(FeedEntry._ID,IDmsg);
                     values.put(FeedEntry.COLUMN_NAME_DATE, "24-08");
                     values.put(FeedEntry.COLUMN_NAME_ID1, myID);
                     values.put(FeedEntry.COLUMN_NAME_ID2, currentID);
                     values.put(FeedEntry.COLUMN_NAME_MESSAGE, message);
                     values.put(FeedEntry.COLUMN_NAME_VISIBILITY, "1");

// Insert the new row, returning the primary key value of the new row
                     long newRowId;
                     newRowId = db.insert(
                             FeedEntry.TABLE_NAME,
                             null,
                             values);

                     SQLiteDatabase db1 = mDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
                     String[] projection = {
                             FeedEntry._ID,
                             FeedEntry.COLUMN_NAME_MESSAGE,
                             FeedEntry.COLUMN_NAME_DATE
                     };

                     String selection =
                             FeedEntry.COLUMN_NAME_ID1+"=?"
                     ;

                     String[] selectionArgs = {
                             "1"
                     };

// How you want the results sorted in the resulting Cursor
                     String sortOrder =
                             FeedEntry.COLUMN_NAME_DATE + " DESC";

                     Cursor c = db1.query(
                             FeedEntry.TABLE_NAME,  // The table to query
                             projection,                               // The columns to return
                             selection,                                // The columns for the WHERE clause
                             selectionArgs,                            // The values for the WHERE clause
                             null,                                     // don't group the rows
                             null,                                     // don't filter by row groups
                             sortOrder                                 // The sort order
                     );
                     c.moveToFirst();
                     while (!c.isAfterLast()){
                         Log.d("rowread", String.valueOf(c.getString(0)));
                         c.moveToNext();
                     }

                 }
                else{
                     //TODO send message again
                 }
            } catch (JSONException e) {
                e.printStackTrace();
            }
/*
            this.publishProgress(String.format("%s started writing", sender));
            try {
                Thread.sleep(2000); //simulate a network call
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            return Utility.messages[rand.nextInt(Utility.messages.length-1)];
*/
            return null;
        }
        /*
        @Override
        public void onProgressUpdate(String... v) {

            if(messages.get(messages.size()-1).isStatusMessage)//check wether we have already added a status message
            {
                messages.get(messages.size()-1).setMessage(v[0]); //update the status for that
                adapter.notifyDataSetChanged();
                getListView().setSelection(messages.size()-1);
            }
            else{
                addNewMessage(new Message(true,v[0])); //add new message, if there is no existing status message
            }
        }
*/
        @Override
        protected void onPostExecute(Void text) {
            /*if(messages.get(messages.size()-1).isStatusMessage)//check if there is any status message, now remove it.
            {
                messages.remove(messages.size()-1);
            }

            addNewMessage(new Message(text, false,true)); // add the orignal message from server.*/
            notification();
        }
    }

    private class LocalCall extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... args) {
                    SQLiteDatabase db1 = mDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
                    String[] projection = {
                            FeedEntry._ID,
                            FeedEntry.COLUMN_NAME_MESSAGE,
                            FeedEntry.COLUMN_NAME_ID1
                    };


// How you want the results sorted in the resulting Cursor
                    String sortOrder =
                            FeedEntry.COLUMN_NAME_DATE + " DESC";

                    Cursor c = db1.query(
                            FeedEntry.TABLE_NAME,  // The table to query
                            projection,                               // The columns to return
                            "",                                // The columns for the WHERE clause
                            null,                              // The values for the WHERE clause
                            null,                                     // don't group the rows
                            null,                                     // don't filter by row groups
                            sortOrder                                 // The sort order
                    );
                    c.moveToFirst();
            Log.d("countcursor",String.valueOf(c.getColumnCount()));
                    while (!c.isAfterLast()){
                        Log.d("rowread", String.valueOf(c.getString(0)));
                        messages.add(new Message(c.getString(1), c.getString(2).equals(myID),true));
                        c.moveToNext();
                    }
            return null;
        }
    }

    private class checkNewMessage extends AsyncTask<Void, String, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            jsonParser = new JSONParser();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "getLastMessage"));
            params.add(new BasicNameValuePair("ID1", currentID));
            params.add(new BasicNameValuePair("ID2", myID));
            params.add(new BasicNameValuePair("Date",latestTimeStamp));
            String json = jsonParser.plainHttpRequest(url,"POST",params);
            try{
                JSONArray newMessages = new JSONArray(json);
                if (newMessages.length()>0){
                    for (int i=0; i<newMessages.length(); i++){
                        JSONObject message = newMessages.getJSONObject(i);
                        String messageTimeStamp = message.getString("Date");
                        String messageText = message.getString("Message");
                        this.publishProgress(messageText);
                    }
                    latestTimeStamp = newMessages.getJSONObject(newMessages.length()-1).getString("Date");
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("TimeStamp",latestTimeStamp);
                    editor.commit();
                }
                else {
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onProgressUpdate(String... v) {
            addNewMessage(new Message(v[0],false,true));
        }
    }

    public void callCheckNewMessages(){
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            new checkNewMessage().execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 50000 ms
    }

    void addNewMessage(Message m)
    {
        messages.add(m);
        adapter.notifyDataSetChanged();
        getListView().setSelection(messages.size()-1);
    }

    void notification(){
        adapter.notifyDataSetChanged();
    }

}