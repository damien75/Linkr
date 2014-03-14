package sara.damien.app.chat;

import android.app.Dialog;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sara.damien.app.BundleParameters;
import sara.damien.app.Common;
import sara.damien.app.LinkrAPI;
import sara.damien.app.Meeting;
import sara.damien.app.R;
import sara.damien.app.utils.DateTimePicker;

//LATER: Get a better chat implementation
public class MessageActivity extends ListActivity {
    List<Message> messages;
    public static String timeStampReceived;
    public static String timeStampSent;
    private String chateeID;

    Meeting meeting;

    MessageAdapter adapter;
    EditText text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //TODO ; problem lors du refresh de l'activity: persist : special pour toi Clement !
        messages = Collections.synchronizedList(new ArrayList<Message>());

        Bundle bundle = getIntent().getExtras();
        meeting = bundle.getParcelable(BundleParameters.MEETING_KEY);
        Calendar c = Calendar.getInstance();
        Log.e("calendar",c.toString());
        this.setTitle(meeting.getOtherParticipant().getName());
        chateeID = meeting.getOtherParticipant().getID();
        timeStampReceived = Common.getPrefs().getLastReceivedMessageTimeStamp(chateeID);
        timeStampSent = Common.getPrefs().getLastSentMessageTimeStamp(chateeID);

        text = (EditText) this.findViewById(R.id.messageEditor);
        adapter = new MessageAdapter(this, messages);
        setListAdapter(adapter);

        new LocalMessagesLoader().execute();
        scheduleNewMessageChecking();
    }
    public void sendMessage(View v) {
            String message_text = text.getText().toString().trim(); //TODO: Disable send button unless getText() != ""

            if (!message_text.isEmpty()){
                text.setText("");
                //TODO : ajouter les messages à la base locale même s'ils ne sont pas encore envoyés
                Message message = new Message(message_text, chateeID);
                addNewMessage(message);
                message.send(adapter);
        }
    }

    public void button_click(View view){
        final Dialog mDateTimeDialog = new Dialog(this);
        final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.datetimedialog, null);
        final DateTimePicker mDateTimePicker = (DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);

        ((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((TextView) findViewById(R.id.messageDatePicker)).setText(mDateTimePicker.getDateString());
                mDateTimePicker.clearFocus(); //TODO: Est-ce que clearFocus est n�cessaire ?
                mDateTimeDialog.dismiss();
            }
        });

        ((Button) mDateTimeDialogView.findViewById(R.id.CancelDialog)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDateTimeDialog.cancel();
            }
        });

        ((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mDateTimePicker.reset();
            }
        });

        mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDateTimeDialog.setContentView(mDateTimeDialogView);
        mDateTimeDialog.show();
    }

    private class LocalMessagesLoader extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... args) {
            messages.addAll(Common.getDB().readAllLocalMessage(chateeID));
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
            List<Message> messages = LinkrAPI.getNewMessages(chateeID, timeStampReceived, timeStampSent);

            for (Message msg : messages) {
                Common.getDB().insertMessage(msg);
                publishProgress(msg);
            }

            if (messages.size() > 0) {
                timeStampReceived = messages.get(messages.size() - 1).getTime();
                Common.getPrefs().setLastReceivedMessageTimeStamp(timeStampReceived, chateeID);
            }

            return null;
        }

        @Override
        public void onProgressUpdate(Message... newMessages) {
            for (Message msg : newMessages)
                addNewMessage(msg);
        }
    }

    public void scheduleNewMessageChecking(){
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
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        timer.schedule(doAsynchronousTask, 0, 5000);
    }

    void addNewMessage(Message m) {
        messages.add(m);
        adapter.notifyDataSetChanged();
        getListView().setSelection(messages.size() - 1);
    }
}