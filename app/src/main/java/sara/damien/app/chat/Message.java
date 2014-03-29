package sara.damien.app.chat;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sara.damien.app.Common;
import sara.damien.app.DB.DbHelper;
import sara.damien.app.LinkrAPI;
import sara.damien.app.utils.Utilities;

/**
 * Created by Sara-Fleur on 2/27/14.
 */
public class Message {
    String content;
    boolean isSent;
    String time;
    String sender, recipient;
    String ID;

    private Message() {
    }

    /**
     * Create an unsent message
     */
    public Message(String contents, String recipient) {
        this.ID = null;
        this.content = contents;
        this.sender = Common.getMyID();
        this.recipient = recipient;
        this.time = Utilities.getTimestamp();
        this.isSent = false;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isMine() {
        return this.sender.equals(Common.getMyID());
    }

    public void setID(String ID) { this.ID = ID; }

    public void setTime(String time) { this.time = time; }

    public boolean isSent() { return this.isSent; }

    public void setSent(boolean isSent) { this.isSent = isSent; }

    public String getTime() { return this.time; }

    public String getID() { return this.ID; }

    public String getSender() { return this.sender; }

    public String getRecipient() { return this.recipient; }

    public void send(MessageAdapter callback) {
        assert (this.sender.equals(Common.getMyID()));
        MessageSender sender = new MessageSender(callback);
        sender.execute();
    }

    public List<NameValuePair> serializeForLinkr() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("ID1", this.sender));
        params.add(new BasicNameValuePair("ID2", this.recipient));
        params.add(new BasicNameValuePair("Message", this.content));
        return params;
    }

    public static Message deserializeFromLinkr(JSONObject jsonMessage) throws JSONException {
        Message msg = new Message();

        msg.ID = jsonMessage.getString("IDmsg");
        msg.content = jsonMessage.getString("Message");
        msg.sender = jsonMessage.getString("from");
        msg.recipient = jsonMessage.getString("to");
        msg.time = jsonMessage.getString("timeStamp");
        msg.isSent = true;

        return msg;
    }

    public static interface DB {
        public static interface COLUMNS {
            String ID = "IDmsg";
            String SENDER = "ID1";
            String RECIPIENT = "ID2";
            String DATE = "Date";
            String CONTENTS = "Message";
            String VISIBILITY = "Visibility";//FIXME: What does VISIBILITY represent?
            String IS_SENT = "isSent";
            String[] PROJECTION = {ID, CONTENTS, SENDER, RECIPIENT, DATE, IS_SENT};
        }

        String NAME = "chat";

        String CREATE_QUERY =
                "CREATE TABLE " + NAME + " (" +
                        COLUMNS.ID + " INTEGER PRIMARY KEY," +
                        COLUMNS.SENDER + DbHelper.TEXT_TYPE + "," +
                        COLUMNS.RECIPIENT + DbHelper.TEXT_TYPE + "," +
                        COLUMNS.DATE + DbHelper.TEXT_TYPE + "," +
                        COLUMNS.CONTENTS + DbHelper.TEXT_TYPE + "," +
                        COLUMNS.VISIBILITY + DbHelper.TEXT_TYPE + "," +
                        COLUMNS.IS_SENT + DbHelper.TEXT_TYPE +
                        " )";
    }

    public ContentValues serializeForLocalDB() {
        ContentValues values = new ContentValues();
        values.put(DB.COLUMNS.ID, ID);
        values.put(DB.COLUMNS.DATE, time);
        values.put(DB.COLUMNS.SENDER, sender);
        values.put(DB.COLUMNS.RECIPIENT, recipient);
        values.put(DB.COLUMNS.CONTENTS, content);
        values.put(DB.COLUMNS.VISIBILITY, "1");
        values.put(DB.COLUMNS.IS_SENT, isSent);
        return values;
    }

    public static Message deserializeFromLocalDB(Cursor c) {
        Message msg = new Message();

        msg.ID = c.getString(c.getColumnIndex(DB.COLUMNS.ID));
        msg.content = c.getString(c.getColumnIndex(DB.COLUMNS.CONTENTS));
        msg.sender = c.getString(c.getColumnIndex(DB.COLUMNS.SENDER));
        msg.recipient = c.getString(c.getColumnIndex(DB.COLUMNS.RECIPIENT));
        msg.time = c.getString(c.getColumnIndex(DB.COLUMNS.DATE));
        msg.isSent = c.getInt(c.getColumnIndex(DB.COLUMNS.IS_SENT)) != 0; //FIXME: Check this

        return msg;
    }

    private class MessageSender extends AsyncTask<Void, Void, Void> {
        private MessageAdapter callback;

        public MessageSender(MessageAdapter callback) {
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... args) {
            LinkrAPI.SendMessage(Message.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void text) {
            this.callback.notifyDataSetChanged();
        }
    }
}
