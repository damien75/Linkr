package sara.damien.app.requests;

/**
 * Created by Damien on 26/02/2014.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sara.damien.app.BundleParameters;
import sara.damien.app.Common;
import sara.damien.app.LinkrAPI;
import sara.damien.app.Meeting;
import sara.damien.app.Profile;
import sara.damien.app.R;
import sara.damien.app.chat.MessageActivity;
import sara.damien.app.utils.JSONParser;

public class DebateMeetingFragment extends ListFragment {
    private static String url ="http://www.golinkr.net";
    ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser(); //TODO: Why not store profiles as blobs?

    private static String currentID;


    JSONArray meetings = null;
    ArrayList<HashMap<String,String>> MeetingList;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        HashMap<String,String>meeting = MeetingList.get(position);
        String MeetingID = meeting.get(LinkrAPI.TAG_MEETING_ID);
        String IDu = meeting.get(LinkrAPI.TAG_ID);
        String First_Name = meeting.get(LinkrAPI.TAG_FIRST_NAME);
        String Last_Name = meeting.get(LinkrAPI.TAG_LAST_NAME);
        String subject = meeting.get(LinkrAPI.TAG_SUBJECT);
        String State = meeting.get(LinkrAPI.TAG_STATE);
        String Date_Meeting = meeting.get(LinkrAPI.TAG_DATE_MEETING);
        String MyStatus = meeting.get(LinkrAPI.TAG_MYSTATUS);

        Intent i = new Intent(getActivity(),MessageActivity.class);
//TODO : revoir le read parcelable, ameliorer la classe meeting pour ne pas avoir les put string
        Bundle b = new Bundle();
        Profile otherParticipant = new Profile(true,Last_Name,First_Name,"essai",10,0,0,"Thlassa",IDu,1,1);
        Meeting m = new Meeting(otherParticipant, MeetingID, subject,State, MyStatus, null, null, Date_Meeting,null);
        b.putParcelable(BundleParameters.MEETING_KEY,m);
        i.putExtras(b);
        startActivity(i);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_debate_meeting, container, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        currentID = prefs.getString("ID","0"); //TODO: replace with getID

        MeetingList = new ArrayList<HashMap<String, String>>();
        new GetMeetings().execute();
        return rootView;
    }
    private class GetMeetings extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... args) {
            // Creating service handler class instance
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION","getDebatingRequests"));
            params.add(new BasicNameValuePair("ID", currentID));
            String jsonStr = jsonParser.plainHttpRequest(url,"POST",params);

            // Making a request to url and getting response
            //String jsonStr = json.toString();
            Log.d("Response chat ",  jsonStr);

            //if (jsonStr != null) {
            try {
                meetings= new JSONArray(jsonStr);
                if (meetings.length()>0){  //FIXME: Pas besoin du if
                    for (int i = 0; i<meetings.length();i++){
                        JSONObject c = meetings.getJSONObject(i);
                        String idu = c.getString(LinkrAPI.TAG_ID);
                        String first_name = c.getString(LinkrAPI.TAG_FIRST_NAME);
                        String last_name = c.getString(LinkrAPI.TAG_LAST_NAME);
                        String name= first_name + " " + last_name + (Common.isDebugging() ? " - " + idu : "");
                        String date_accept = c.getString(LinkrAPI.TAG_DATE_ACCEPT);
                        String subject = c.getString(LinkrAPI.TAG_SUBJECT);
                        String idm = c.getString(LinkrAPI.TAG_MEETING_ID);
                        String mystatus = c.getString(LinkrAPI.TAG_MYSTATUS);
                        String state =c.getString(LinkrAPI.TAG_STATE);
                        String date_meeting = c.getString(LinkrAPI.TAG_DATE_MEETING);

                        HashMap<String,String> map = new HashMap<String, String>();
                        //TODO check : est-ce que name est utilisé quelque part ?
                        map.put("Name",name);
                        map.put(LinkrAPI.TAG_FIRST_NAME,first_name);
                        map.put(LinkrAPI.TAG_LAST_NAME,last_name);
                        map.put(LinkrAPI.TAG_SUBJECT,subject);
                        map.put(LinkrAPI.TAG_ID,idu);
                        map.put(LinkrAPI.TAG_MEETING_ID,idm);
                        map.put(LinkrAPI.TAG_MYSTATUS,mystatus);
                        map.put(LinkrAPI.TAG_DATE_ACCEPT,date_accept);
                        map.put(LinkrAPI.TAG_SUBJECT,subject);
                        map.put(LinkrAPI.TAG_DATE_MEETING,date_meeting);
                        map.put(LinkrAPI.TAG_STATE,state);
                        MeetingList.add(map);
                    }
                }

            }
            catch (JSONException e){
                e.printStackTrace();
            }
            return null;

        }
        @Override
        protected void onPostExecute(Void result) {
            pDialog.dismiss();
            ListAdapter adapter = new SimpleAdapter(
                    getActivity(), MeetingList,
                    R.layout.list_item_request,
                    new String[]{"Name", LinkrAPI.TAG_SUBJECT,  LinkrAPI.TAG_DATE_ACCEPT},
                    new int[]{R.id.name, R.id.subject, R.id.meeting_date});
            setListAdapter(adapter);
        }
    }
}