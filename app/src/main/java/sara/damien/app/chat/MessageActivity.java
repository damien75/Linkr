package sara.damien.app.chat;

import android.app.Dialog;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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

    MessageAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //TODO ; problem lors du refresh de l'activity: persist : special pour toi Clement !
        messages = Collections.synchronizedList(new ArrayList<Message>());

        Bundle bundle = getIntent().getExtras();
        meeting = bundle.getParcelable(BundleParameters.MEETING_KEY);
        myStatus = meeting.getMyStatus();
        dateTimePickerShowBtn();
        this.setTitle(meeting.getOtherParticipant().getName());
        chateeID = meeting.getOtherParticipant().getID();
        timeStampReceived = Common.getPrefs().getLastReceivedMessageTimeStamp(chateeID);
        timeStampSent = Common.getPrefs().getLastSentMessageTimeStamp(chateeID);

        adapter = new MessageAdapter(this, messages);
        setListAdapter(adapter);

        new LocalMessagesLoader().execute();
        scheduleNewUpdateChecking();
    }
    public void sendMessage(View v) {
        EditText text = (EditText) this.findViewById(R.id.messageEditor);
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
                                       new MeetingDateProposition().execute();

                                       dialog.cancel();
                                   }
                               }

        );

    }

    public void refuseDateMeeting(View view){
        new dateMeetingRefusal().execute();
    }

    private class dateMeetingRefusal extends AsyncTask<Void, Void, Void>{
        boolean successfullyRefused;
        @Override
        protected Void doInBackground(Void... voids) {
            successfullyRefused = LinkrAPI.refuseDateMeeting(meeting);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (successfullyRefused){
                meeting.setState("1");
                Common.getDB().updateStateMeeting(meeting);
                dateTimePickerShowBtn();
            }
            Toast.makeText(getApplicationContext(), successfullyRefused ? "You rejected this proposition" :
                    "We couldn't send your refuse to "+meeting.getOtherParticipant().getName(),Toast.LENGTH_SHORT).show();
        }
    }

    public void acceptProposition(View view){
        new dateMeetingAccepting().execute();
    }

    private class dateMeetingAccepting extends AsyncTask<Void, Void, Void>{
        boolean successfullyAccepted;
        boolean successfullyAddedToCalendar;
        @Override
        protected Void doInBackground(Void... voids) {
            successfullyAccepted = LinkrAPI.acceptDateMeeting(meeting);
            successfullyAddedToCalendar = LinkrAPI.updateCalendarEventID(meeting);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (successfullyAccepted){
                meeting.setState("2");
                Common.getCalendar().insertMeetingToCalendar(meeting, getContentResolver());
                Common.getDB().updateStateMeeting(meeting);//CHECKME : on update le meeting avant d'avoir recu l'eventID!
                dateTimePickerShowBtn();
            }
            Toast.makeText(getApplicationContext(), (successfullyAccepted ? "You accepted this proposition" :
                    "We couldn't send your accept to "+meeting.getOtherParticipant().getName()) +
                    (successfullyAddedToCalendar ? " Added to calendar" : "not added to calendar"),Toast.LENGTH_SHORT).show();
        }
    }

    private class MeetingDateProposition extends AsyncTask<Void, Void, Void>{
        boolean response;
        String message = "no connexion to server";

        @Override
        protected Void doInBackground(Void... voids) {
            response = LinkrAPI.sendMeetingDateProposition(meeting);
            if (response){
                Common.getDB().updateDateMeeting(meeting);
                message = "Meeting date successfully added locally and on server";
            }
            else {
                message = "Problem with concurrency on server";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            if (response) {
                ((TextView) findViewById(R.id.messageDatePicker)).setText("You suggested to meet on " + meeting.getDateMeeting());
            }
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

    private class checkNewUpdates extends AsyncTask<Void, Message, Void>{
        String dateMeeting="";
        String state="";
        long eventID = 0;
        @Override
        protected Void doInBackground(Void... voids) {
            //Messages Check
            List<Message> messages = LinkrAPI.getNewMessages(chateeID, timeStampReceived, timeStampSent);

            for (Message msg : messages) {
                Common.getDB().insertMessage(msg);
                publishProgress(msg);
            }

            //Meeting information check
            JSONObject jsonMeeting = LinkrAPI.fetchDateMeetingUpdate(meeting);
            try {
                dateMeeting = jsonMeeting.getString("Date_Meeting");
                state = jsonMeeting.getString("State");
                eventID = Long.valueOf(jsonMeeting.getString("calendarEventID"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onProgressUpdate(Message... newMessages) {
            for (Message msg : newMessages)
                addNewMessage(msg);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!(dateMeeting.equals(meeting.getDateMeeting()) && state.equals(meeting.getState()))){
                meeting.setDateMeeting(dateMeeting);
                meeting.setState(state);
                if (state.equals("2") && eventID>0 && eventID!=meeting.getCalendarEventID()){
                    meeting.setCalendarEventID(eventID);
                }
                Common.getDB().updateDateMeeting(meeting);
                dateTimePickerShowBtn();
            }
        }
    }

    public void scheduleNewUpdateChecking(){
        final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            new checkNewUpdates().execute();
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
        String state = meeting.getState();
        if (state.equals("1")){
            choose.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.messageDatePicker)).setText("Pick a date!");
        }
        else if (state.equals("2")){
            change.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.messageDatePicker)).setText("Your meeting is scheduled on "+ meeting.getDateMeeting());
        }
        else if ((state.equals("3") && myStatus.equals("1")) || (state.equals("4") && myStatus.equals("2"))){
            changeYourProposition.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.messageDatePicker)).setText("You suggested to meet on "+ meeting.getDateMeeting());
        }
        else if ((state.equals("3") && myStatus.equals("2")) || (state.equals("4") && myStatus.equals("1"))){
            accept.setVisibility(View.VISIBLE);
            refuse.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.messageDatePicker)).setText("You were asked to meet on "+ meeting.getDateMeeting());
        }
    }
}