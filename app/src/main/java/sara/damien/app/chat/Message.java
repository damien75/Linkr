package sara.damien.app.chat;

import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sara.damien.app.Common;
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

    public Message(String ID, String content, String sender, String recipient, String time, boolean isSent) {
        super();
        this.ID = ID;
        this.content = content;
        this.sender = sender;
        this.recipient = recipient;
        this.time = time;
        this.isSent = isSent;
    }

    public Message(String content, String recipient) {
        this(null, content, Common.getMyID(), recipient, Utilities.getTimestamp(), false);
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

    public boolean isSent() { return this.isSent; }

    public void setSent(boolean isSent) { this.isSent = isSent; }

    public String getTime() { return this.time; }

    public String getID() { return this.ID; }

    public String getSender() { return this.sender; }

    public String getRecipient() { return this.recipient; }

    public static Message fromJSONMessage(String from, String to, JSONObject jsonMessage) throws JSONException {
        return new Message(jsonMessage.getString("IDmsg"), jsonMessage.getString("Message"), from, to, jsonMessage.getString("Date"), true);
    }

    public List<NameValuePair> serializeForLinkr() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("ID1", this.sender));
        params.add(new BasicNameValuePair("ID2", this.recipient));
        params.add(new BasicNameValuePair("Message",this.content));
        return params;
    }

    private class SendMessage extends AsyncTask<Void, Void, Void> {
        private MessageAdapter callback;

        public SendMessage(MessageAdapter callback) {
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

    public void send(MessageAdapter callback) {
        assert(this.sender.equals(Common.getMyID()));
        SendMessage sendMessage = new SendMessage(callback);
        sendMessage.execute();
    }
}
