package sara.damien.app.chat;

import android.app.Dialog;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
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

//LATER: Get a better chat implementation
public class MessageActivity extends ListActivity {
    List<Message> messages;
    public static String timeStampReceived;
    public static String timeStampSent;
    private String chateeID;
    Dialog dialog;

    Meeting meeting;
    String myStatus;
    String state;

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
        myStatus = meeting.getMyStatus();
        state = meeting.getState();
        dateTimePickerShowBtn();
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

    public void dialogDateTimePropose(View view)
    {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.datetimepicker);
        dialog.setCancelable(true);
        dialog.setTitle("Select a date and a time!");
        dialog.show();


        final TimePicker time_picker = (TimePicker) dialog.findViewById(R.id.timePicker);
        final DatePicker date_picker = (DatePicker) dialog.findViewById(R.id.datePicker);
        Button btn = (Button) dialog.findViewById(R.id.buttonDateTimePicker);
        btn.setOnClickListener(new View.OnClickListener()
                               {
                                   public void onClick(View arg0)
                                   {
                                       TextView txt = (TextView) findViewById(R.id.messageDatePicker);
                                       String date = date_picker.getYear() + "/" + (date_picker.getMonth() + 1) + "/"
                                               + date_picker.getDayOfMonth();
                                       String time = time_picker.getCurrentHour() + ":" + time_picker.getCurrentMinute();
                                       txt.setText("You selected " + date);
                                       txt.append("Time "+ time);
                                       Calendar calendar = Calendar.getInstance();
                                       calendar.set(date_picker.getYear(), date_picker.getMonth(), date_picker.getDayOfMonth(),
                                               time_picker.getCurrentHour(), time_picker.getCurrentMinute());
                                       SimpleDateFormat formatter =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                       meeting.setDateMeeting(formatter.format(calendar.getTime()));
                                       Log.d("meeting date",meeting.getDateMeeting());
                                       new MeetingDateProposition().execute();

                                       dialog.cancel();
                                   }
                               }

        );

    }

    private class MeetingDateProposition extends AsyncTask<Void, Void, Void>{
        String response;

        @Override
        protected Void doInBackground(Void... voids) {
            response = LinkrAPI.sendMeetingDateProposition(meeting);
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
        }
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

    void dateTimePickerShowBtn(){
        Button accept = (Button)findViewById(R.id.accept_date_button);
        Button choose = (Button)findViewById(R.id.choose_date_button);
        Button change = (Button)findViewById(R.id.change_date_button);
        Button changeYourProposition = (Button)findViewById(R.id.change_your_proposition_date_button);
        Button refuse = (Button)findViewById(R.id.refuse_date_button);
        //TODO: why not set them to Gone in XML directly?
        accept.setVisibility(View.GONE);
        choose.setVisibility(View.GONE);
        change.setVisibility(View.GONE);
        refuse.setVisibility(View.GONE);
        changeYourProposition.setVisibility(View.GONE);
        if (state.equals("1")){
            choose.setVisibility(View.VISIBLE);
        }
        else if (state.equals("2")){
            change.setVisibility(View.VISIBLE);
        }
        else if ((state.equals("3") && myStatus.equals("1")) || (state.equals("4") && myStatus.equals("2"))){
            changeYourProposition.setVisibility(View.VISIBLE);
        }
        else if ((state.equals("3") && myStatus.equals("2")) || (state.equals("4") && myStatus.equals("1"))){
            accept.setVisibility(View.VISIBLE);
            refuse.setVisibility(View.VISIBLE);
        }
    }
}