package sara.damien.app.chat;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    JSONParser jsonParser = new JSONParser();
    private static String url ="http://www.golinkr.net";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ID1 = prefs.getString("ID","1");

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

        messages.add(new Message("Hello", false));
        messages.add(new Message("Hi!", true));
        messages.add(new Message("Wassup??", false));
        messages.add(new Message("nothing much, working on speech bubbles.", true));
        messages.add(new Message("you say!", true));
        messages.add(new Message("oh thats great. how are you showing them", false));


        adapter = new MessageAdapter(this, messages);
        setListAdapter(adapter);
        addNewMessage(new Message("mmm, well, using 9 patches png to show them.", true));
    }
    public void sendMessage(View v){
        String newMessage = text.getText().toString().trim();
        if(newMessage.length() > 0){
            text.setText("");
            //TODO ajouter qqch pour dire que le message n'a pas encore été envoyé
            addNewMessage(new Message(newMessage, true));
            SendMessage sendMessage = new SendMessage();
            sendMessage.message = newMessage;
            sendMessage.execute();
        }
    }
    private class SendMessage extends AsyncTask<Void, String, String>{
        private String message;
        @Override
        protected String doInBackground(Void... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION","addMessage"));
            params.add(new BasicNameValuePair("ID1", ID1));
            params.add(new BasicNameValuePair("ID2", ID2));
            params.add(new BasicNameValuePair("Message",message));
            String jsonStr = jsonParser.plainHttpRequest(url,"POST",params);

            try {
                Thread.sleep(2000); //simulate a network call
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.publishProgress(String.format("%s started writing", sender));
            try {
                Thread.sleep(2000); //simulate a network call
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.publishProgress(String.format("%s has entered text", sender));
            try {
                Thread.sleep(3000);//simulate a network call
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Utility.messages[rand.nextInt(Utility.messages.length-1)];

        }
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
        @Override
        protected void onPostExecute(String text) {
            if(messages.get(messages.size()-1).isStatusMessage)//check if there is any status message, now remove it.
            {
                messages.remove(messages.size()-1);
            }

            addNewMessage(new Message(text, false)); // add the orignal message from server.
        }
    }
    void addNewMessage(Message m)
    {
        messages.add(m);
        adapter.notifyDataSetChanged();
        getListView().setSelection(messages.size()-1);
    }

}
