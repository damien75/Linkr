package sara.damien.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import sara.damien.app.Meeting;
import sara.damien.app.R;

/**
 * Created by Sara-Fleur on 3/3/14.
 */
public class SentRequestsAdapter extends ArrayAdapter<Meeting> {
    public SentRequestsAdapter(Context context, List<Meeting> objects) {
        super(context, -1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Meeting request = super.getItem(position);

        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(super.getContext()).inflate(R.layout.requests_sent_row, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.subject = (TextView) convertView.findViewById(R.id.subject);
            holder.date = (TextView) convertView.findViewById (R.id.sent_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(request.getOtherParticipant().getFirst_Name()+" "+request.getOtherParticipant().getLast_Name());
        holder.subject.setText(request.getSubject());
        holder.date.setText(request.getDateRequest());

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView subject;
        TextView date;
    }
}

