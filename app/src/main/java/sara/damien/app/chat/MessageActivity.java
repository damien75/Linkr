package sara.damien.app.chat;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sara.damien.app.Common;
import sara.damien.app.DB.DbHelper;
import sara.damien.app.R;
import sara.damien.app.utils.DateTimePicker;
import sara.damien.app.utils.JSONParser;
import sara.damien.app.utils.Utilities;

public class MessageActivity extends ListActivity {
    ArrayList<Message> messages;
    MessageAdapter adapter;
    EditText text;
    static String sender;
    String IDm; //TODO: Shouldn't we pass a full profile, instead of just small bits?
    String currentID;
    String First_Name;
    String Last_Name;
    String Subject;
    JSONParser jsonParser;
    String latestTimeStamp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        latestTimeStamp = Common.getPrefs().getString("TimeStamp", "2014-02-28 16:27:40");

        text = (EditText) this.findViewById(R.id.messageEditor);

        Bundle bundle = getIntent().getExtras();
        currentID = bundle.getString("IDu");
        IDm = bundle.getString("IDm");
        First_Name = bundle.getString("First_Name");
        Last_Name = bundle.getString("Last_Name");
        Subject = bundle.getString("Subject");

        sender = First_Name+" "+Last_Name;
        this.setTitle(sender);

        messages = new ArrayList<Message>();

        LocalCall lc = new LocalCall();
        lc.execute();
        adapter = new MessageAdapter(this, messages);
        setListAdapter(adapter);
        //addNewMessage(new Message("testemessage",true,true,"24/08"));
        callCheckNewMessages();

    }
    public void sendMessage(View v){
        String message_text = text.getText().toString().trim(); //TODO: Disable send button unless getText() != ""

        if (!message_text.isEmpty()){
            text.setText("");
            Message message = new Message(message_text, currentID);
            addNewMessage(message);
            message.send(adapter);
        }
    }

    public void button_click(View view){
        // Create the dialog
        final Dialog mDateTimeDialog = new Dialog(this);
        // Inflate the root layout
        final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.datetimedialog, null);
        // Grab widget instance
        final DateTimePicker mDateTimePicker = (DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);

        //mDateTimePicker.setDateChangedListener(this);

        // Update demo edittext when the "OK" button is clicked
        ((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDateTimePicker.clearFocus();
                // TODO Auto-generated method stub
                String result_string = mDateTimePicker.getMonth() + "/" + String.valueOf(mDateTimePicker.getDay()) + "/" + String.valueOf(mDateTimePicker.getYear())
                        + "  " + String.valueOf(mDateTimePicker.getHour()) + ":" + String.valueOf(mDateTimePicker.getMinute());
                ((TextView) findViewById(R.id.messageDatePicker)).setText(result_string);
                mDateTimeDialog.dismiss();
            }
        });

        // Cancel the dialog when the "Cancel" button is clicked
        ((Button) mDateTimeDialogView.findViewById(R.id.CancelDialog)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDateTimeDialog.cancel();
            }
        });

        // Reset Date and Time pickers when the "Reset" button is clicked

        ((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDateTimePicker.reset();
            }
        });

        // Setup TimePicker
        // No title on the dialog window
        mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Set the dialog content view
        mDateTimeDialog.setContentView(mDateTimeDialogView);
        // Display the dialog
        mDateTimeDialog.show();
    }

    private class LocalCall extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... args) {
            messages.addAll(Common.getDB().readAllLocalMessage());
            return null;
        }

        @Override
        protected void onPostExecute(Void text){
            adapter.notifyDataSetChanged();
        }
    }

    private class checkNewMessage extends AsyncTask<Void, Message, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            jsonParser = new JSONParser();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "getLastMessage")); //FIXME: Since only the last message is returned, receiving multiple messages at once seems impossible.
            params.add(new BasicNameValuePair("ID1", currentID));
            params.add(new BasicNameValuePair("ID2", Common.getMyID()));
            params.add(new BasicNameValuePair("Date",latestTimeStamp));
            String json = jsonParser.plainHttpRequest(Utilities.API_URL,"POST",params);
            try{
                JSONArray newMessages = new JSONArray(json);
                if (newMessages.length()>0){
                    for (int i=0; i<newMessages.length(); i++) {
                        Message message = Message.fromJSONMessage(Common.getMyID(), currentID, newMessages.getJSONObject(i));
                        this.publishProgress(message);
                        Common.getDB().insertMessage(message);
                    }
                    latestTimeStamp = newMessages.getJSONObject(newMessages.length()-1).getString("Date");
                    SharedPreferences.Editor editor = Common.getPrefs().edit();
                    editor.putString("TimeStamp", latestTimeStamp);
                    editor.commit();
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onProgressUpdate(Message... newMessages) {
            for (Message msg : newMessages)
                addNewMessage(msg);
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