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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sara.damien.app.BundleParameters;
import sara.damien.app.Common;
import sara.damien.app.DB.DbHelper;
import sara.damien.app.LinkrAPI;
import sara.damien.app.Meeting;
import sara.damien.app.R;
import sara.damien.app.utils.Utilities;

//LATER: Get a better chat implementation
public class MessageActivity extends ListActivity {
    List<Message> messages;
    Meeting meeting;

    MessageAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //FIXME: problem lors du refresh de l'activity: persist : special pour toi Clement !
        messages = Collections.synchronizedList(new ArrayList<Message>());

        Bundle bundle = getIntent().getExtras();
        meeting = bundle.getParcelable(BundleParameters.MEETING_KEY);

        this.setTitle(meeting.getOtherParticipant().getName());
        toggleDateTimePicker();

        adapter = new MessageAdapter(this, messages);
        setListAdapter(adapter);

        new LocalMessagesLoader().execute();
        scheduleNewUpdateChecking();
    }

    public void sendMessage(View v) {
        EditText text = (EditText) this.findViewById(R.id.messageEditor);
        String message_text = text.getText().toString().trim(); //TODO: Disable send button unless getText() != ""

        if (!message_text.isEmpty()) {
            text.setText("");
            //TODO : ajouter les messages à la base locale même s'ils ne sont pas encore envoyés
            Message message = new Message(message_text, meeting.getOtherParticipant().getID());
            addNewMessage(message);
            message.send(adapter);
        }
    }

    public void dialogDateTimePropose(View view) {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.datetimepicker);
        dialog.setCancelable(true);
        dialog.setTitle("Select a date and a time!");
        dialog.show();

        final TimePicker time_picker = (TimePicker) dialog.findViewById(R.id.timePicker);
        final DatePicker date_picker = (DatePicker) dialog.findViewById(R.id.datePicker);
        Button btn = (Button) dialog.findViewById(R.id.buttonDateTimePicker);

        btn.setOnClickListener(
            new View.OnClickListener() {
               public void onClick(View arg0) {
                   Calendar calendar = Utilities.calendarFromDateAndTimePickers(date_picker, time_picker);

                   TextView txt = (TextView) findViewById(R.id.messageDatePicker);
                   txt.setText("You selected " + Utilities.formatDateTime(calendar));

                   meeting.setDateMeeting(Utilities.serializeDateTime(calendar));
                   new MeetingDatePropositionSender().execute();

                   dialog.dismiss();
               }
           }
        );
    }

    public void refuseMeetingDate(View view) {
        new MeetingDateRefuser().execute();
    }

    private class MeetingDateRefuser extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return LinkrAPI.refuseMeetingDate(meeting);
        }

        @Override
        protected void onPostExecute(Boolean successfullyRefused) {
            if (successfullyRefused) {
                meeting.setState("1");
                Common.getDB().updateStateMeeting(meeting);
                toggleDateTimePicker();
            }

            //FIXME: Devise a better error recovery strategy
            Toast.makeText(Common.getAppContext(), successfullyRefused ? "You rejected this proposition" :
                    "We couldn't send your refusal to " + meeting.getOtherParticipant().getName(), Toast.LENGTH_SHORT).show();
        }
    }

    public void acceptProposition(View view) {
        new MeetingDateAccepter().execute();
    }

    private class MeetingDateAccepter extends AsyncTask<Void, Void, Void> {
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
            if (successfullyAccepted) {
                meeting.setState("2");
                Common.getCalendar().insertMeetingToCalendar(meeting, getContentResolver());
                Common.getDB().updateStateMeeting(meeting);//FIXME : on update le meeting avant d'avoir recu l'eventID!
                toggleDateTimePicker();
            }

            Toast.makeText(Common.getAppContext(), (successfullyAccepted ? "You accepted this proposition" :
                    "We couldn't send your accept to " + meeting.getOtherParticipant().getName()) +
                    (successfullyAddedToCalendar ? " Added to calendar" : "not added to calendar"), Toast.LENGTH_SHORT).show();
        }
    }

    private class MeetingDatePropositionSender extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return LinkrAPI.sendMeetingDateProposition(meeting);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            String message;

            if (success) {
                message = "Proposition sent";
                Common.getDB().updateDateMeeting(meeting);
                ((TextView) findViewById(R.id.messageDatePicker)).setText("You suggested to meet on " + meeting.getDateMeeting());
            } else {
                message = "Could not send this proposition";
            }

            Toast.makeText(Common.getAppContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private class LocalMessagesLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            messages.addAll(Common.getDB().readAllLocalMessage(meeting.getOtherParticipant().getID()));
            return null;
        }

        @Override
        protected void onPostExecute(Void text) {
            adapter.notifyDataSetChanged();
        }
    }

    private class checkNewUpdates extends AsyncTask<Void, Message, Void> {
        String dateMeeting = "";
        String state = "";
        long eventID = 0;

        @Override
        protected Void doInBackground(Void... voids) {
            List<Message> messages = LinkrAPI.getNewMessages(meeting.getOtherParticipant().getID());

            DbHelper DB = Common.getDB();
            for (Message msg : messages) {
                DB.insertMessage(msg);
                publishProgress(msg);
            }

            //Meeting information check
			//FIXME: Cleanup
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
        protected void onPostExecute(Void aVoid) { //FIXME: CLEANUP
            if (!(dateMeeting.equals(meeting.getDateMeeting()) && state.equals(meeting.getState()))) {
                meeting.setDateMeeting(dateMeeting);
                meeting.setState(state);
                if (state.equals("2") && eventID > 0 && eventID != meeting.getCalendarEventID()) {
                    meeting.setCalendarEventID(eventID);
                }
                Common.getDB().updateDateMeeting(meeting);
                toggleDateTimePicker();
            }
        }
    }

    public void scheduleNewUpdateChecking() {
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

    void toggleDateTimePicker() {
        Button accept = (Button) findViewById(R.id.accept_date_button);
        Button choose = (Button) findViewById(R.id.choose_date_button);
        Button change = (Button) findViewById(R.id.change_date_button);
        Button changeYourProposition = (Button) findViewById(R.id.change_your_proposition_date_button);
        Button refuse = (Button) findViewById(R.id.refuse_date_button);
        //TODO: why not set them to Gone in XML directly?
        accept.setVisibility(View.GONE);
        choose.setVisibility(View.GONE);
        change.setVisibility(View.GONE);
        refuse.setVisibility(View.GONE);
        changeYourProposition.setVisibility(View.GONE);
        String state = meeting.getState();
        String myStatus = meeting.getMyStatus();

        if (state.equals("1")) {
            choose.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.messageDatePicker)).setText("Pick a date!");
        } else if (state.equals("2")) {
            change.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.messageDatePicker)).setText("Your meeting is scheduled on " + meeting.getDateMeeting());
        } else if ((state.equals("3") && myStatus.equals("1")) || (state.equals("4") && myStatus.equals("2"))) {
            changeYourProposition.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.messageDatePicker)).setText("You suggested to meet on " + meeting.getDateMeeting());
        } else if ((state.equals("3") && myStatus.equals("2")) || (state.equals("4") && myStatus.equals("1"))) {
            accept.setVisibility(View.VISIBLE);
            refuse.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.messageDatePicker)).setText("You were asked to meet on " + meeting.getDateMeeting());
        }
    }
}