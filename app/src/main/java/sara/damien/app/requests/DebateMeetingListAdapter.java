package sara.damien.app.requests;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import sara.damien.app.Meeting;
import sara.damien.app.R;

/**
 * Created by Sara-Fleur on 3/17/14.
 */
public class DebateMeetingListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Meeting> mMeetings;

    public DebateMeetingListAdapter(Context context, ArrayList<Meeting> mMeetings) {
        super();
        this.mContext = context;
        this.mMeetings = mMeetings;
    }
    @Override
    public int getCount() {
        return mMeetings.size();
    }
    @Override
    public Object getItem(int position) {
        return mMeetings.get(position);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Meeting request = (Meeting) this.getItem(position);

        ViewHolder holder;
        if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.requests_sent_row, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.subject = (TextView) convertView.findViewById(R.id.subject);
            holder.date = (TextView) convertView.findViewById (R.id.sent_date);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        holder.name.setText(request.getOtherParticipant().getFirst_Name()+" "+request.getOtherParticipant().getLast_Name());
        holder.subject.setText(request.getSubject());
        holder.date.setText(request.getDateRequest());

        return convertView;
    }
    private static class ViewHolder
    {
        TextView name;
        TextView subject;
        TextView date;
    }

    @Override
    public long getItemId(int position) {
        //Unimplemented, because we aren't using Sqlite.
        return position;
    }

}

