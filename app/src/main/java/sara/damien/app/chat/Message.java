package sara.damien.app.chat;

import android.os.AsyncTask;
import android.support.v7.appcompat.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import sara.damien.app.Common;
import sara.damien.app.utils.JSONParser;
import sara.damien.app.utils.Utilities;

/**
 * Created by Sara-Fleur on 2/27/14.
 */
public class Message {
    String message;
    boolean isStatusMessage;
    boolean isSent;
    String time;
    String sender, recipient;
    String ID;

    public Message(String ID, String message, String sender, String recipient, String time, boolean isStatusMessage, boolean isSent) {
        super();
        this.ID = ID;
        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
        this.time = time;
        this.isStatusMessage = isStatusMessage;
        this.isSent = isSent;
    }

    public Message(String message, String recipient) {
        this(null, message, Common.getMyID(), recipient, Utilities.getTimestamp(), false, false);
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isMine() {
        return this.sender.equals(Common.getMyID());
    }
    public boolean isStatusMessage() {
        return isStatusMessage;
    }
    public void setStatusMessage(boolean isStatusMessage) {
        this.isStatusMessage = isStatusMessage;
    }
    public boolean isSent(){return this.isSent;}
    public String getTime(){return this.time;}

    public String getID() { return this.ID; }
    public String getSender() { return this.sender; }
    public String getRecipient() { return this.recipient; }

    public static Message fromJSONMessage(String from, String to, JSONObject jsonMessage) throws JSONException {
        return new Message(jsonMessage.getString("IDmsg"), jsonMessage.getString("Message"), from, to, jsonMessage.getString("Date"), true, false);
    }

    private class SendMessage extends AsyncTask<Void, Void, Void> {
        private MessageAdapter callback;

        public SendMessage(MessageAdapter callback) {
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... args) {
            JSONParser jsonParser = new JSONParser();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "addMessage"));
            params.add(new BasicNameValuePair("ID1", Message.this.sender));
            params.add(new BasicNameValuePair("ID2", Message.this.recipient));
            params.add(new BasicNameValuePair("Message",Message.this.message));
            JSONObject json = jsonParser.makeHttpRequest(Utilities.API_URL,"POST",params);

            try {
                boolean isSent = json.getBoolean("success");
                if (isSent){
                    Message.this.ID = json.getString("lastID");
                    // TODO: No need to return the date from the server
                    Message.this.isSent = true;
                    Common.getDB().insertMessage(Message.this);
                } else {
                    //TODO send message again
                }
            } catch (JSONException e) {
                e.printStackTrace(); //TODO: Gérer les exceptions
            }
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
