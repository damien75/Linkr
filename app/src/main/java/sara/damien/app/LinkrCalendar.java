package sara.damien.app;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.*;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LinkrCalendar {

    private static long calendarID;
    public static final String[] EVENT_PROJECTION = new String[] {
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME,         // 2
            Calendars.OWNER_ACCOUNT                  // 3
    };
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    public static long getCalendarID(){return calendarID;}

    public LinkrCalendar (Context appContext){
        new calendarInitiator(appContext.getContentResolver());
    }

    public void insertMeetingToCalendar (Meeting meeting, ContentResolver cr){
        new calendarMeetingInserter(cr, meeting);
    }

    private class calendarInitiator extends AsyncQueryHandler {

        public calendarInitiator(ContentResolver cr) {
            super(cr);
            //TODO: select a calendar from every possible account type
            Uri uri = Calendars.CONTENT_URI;
            String selection = Calendars.ACCOUNT_TYPE + " = ?";
            String[] selectionArgs = new String[]{"com.google"};
            Cursor cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
            if (cur.moveToFirst()) {
                long calID = cur.getLong(PROJECTION_ID_INDEX);
                String displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
                String accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
                String ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
                calendarID = calID;
            }
        }
    }
    public class calendarMeetingInserter extends AsyncQueryHandler {
        Meeting meeting;

        public calendarMeetingInserter(ContentResolver cr, Meeting meeting) {
            super(cr);
            this.meeting = meeting;

            if (meeting != null) {
                long calID = getCalendarID();
                long startMillis = 0;
                long endMillis = 0;
                java.util.Calendar beginTime = java.util.Calendar.getInstance();
                Date beginning = null;
                try {
                    beginning = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(meeting.getDateMeeting());
                } catch (ParseException e) {
                    Log.e("Calendarr insertion", "error parsing date");
                    e.printStackTrace();
                }
                beginTime.setTime(beginning);
                startMillis = beginTime.getTimeInMillis();
                java.util.Calendar endTime = java.util.Calendar.getInstance();
                endTime.setTime(beginning);
                endTime.add(java.util.Calendar.HOUR_OF_DAY, 1);
                endMillis = endTime.getTimeInMillis();

                ContentValues values = new ContentValues();
                values.put(Events.DTSTART, startMillis);
                values.put(Events.DTEND, endMillis);
                values.put(Events.TITLE, "Meeting with " + meeting.getOtherParticipant().getName());
                values.put(Events.DESCRIPTION, "Meeting added from Linkr");
                values.put(Events.CALENDAR_ID, calID);
                values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().toString());
                Uri uri = cr.insert(Events.CONTENT_URI, values);

                long eventID = Long.parseLong(uri.getLastPathSegment());
                Log.d("eventID insertion",String.valueOf(eventID));
                meeting.setCalendarEventID(eventID);
                Common.getDB().updateMeeting(meeting);
            }
        }
    }

}
