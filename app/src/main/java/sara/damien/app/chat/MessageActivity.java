package sara.damien.app.chat;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import sara.damien.app.R;
import sara.damien.app.utils.JSONParser;

public class MessageActivity extends ListActivity {

    /** Called when the activity is first created. */

    ArrayList<Message> messages;
    MessageAdapter adapter;
    EditText text;
    static Random rand = new Random();
    static String sender;
    String IDm;
    String ID2;
    String ID1;
    String First_Name;
    String Last_Name;
    String Subject;
    JSONParser jsonParser;
    private static String url ="http://www.golinkr.net";
    String latestTimeStamp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ID1 = prefs.getString("ID","1");
        latestTimeStamp = prefs.getString("TimeStamp", "2014-02-28 16:27:40");

        text = (EditText) this.findViewById(R.id.messageEditor);

        Bundle bundle = getIntent().getExtras();
        ID2 = bundle.getString("IDu");
        IDm = bundle.getString("IDm");
        First_Name = bundle.getString("First_Name");
        Last_Name = bundle.getString("Last_Name");
        Subject = bundle.getString("Subject");

        sender = First_Name+" "+Last_Name;
        this.setTitle(sender);

        messages = new ArrayList<Message>();

        messages.add(new Message("Hello", false,true));
        messages.add(new Message("Hi!", true,true));
        messages.add(new Message("Wassup??", false,true));
        messages.add(new Message("nothing much, working on speech bubbles.", true,true));
        messages.add(new Message("you say!", true,true));
        messages.add(new Message("oh thats great. how are you showing them", false,true));


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
            params.add(new BasicNameValuePair("ID1", ID1));
            params.add(new BasicNameValuePair("ID2", ID2));
            params.add(new BasicNameValuePair("Message",message));
            JSONObject json = jsonParser.makeHttpRequest(url,"POST",params);

            try {
                 boolean isSent = json.getBoolean("success");
                 if (isSent){
                     messages.get(messages.size()-1).setSent();
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

    private class checkNewMessage extends AsyncTask<Void, String, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            jsonParser = new JSONParser();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "getLastMessage"));
            params.add(new BasicNameValuePair("ID1", ID2));
            params.add(new BasicNameValuePair("ID2", ID1));
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