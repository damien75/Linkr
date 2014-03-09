package sara.damien.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Sara-Fleur on 3/3/14.
 * More line-ending testing
 */
public class RequestsSentAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<RequestsSent> mRequests;

    public RequestsSentAdapter(Context context, ArrayList<RequestsSent> mRequests) {
        super();
        this.mContext = context;
        this.mRequests = mRequests;
    }
    @Override
    public int getCount() {
        return mRequests.size();
    }
    @Override
    public Object getItem(int position) {
        return mRequests.get(position);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RequestsSent request = (RequestsSent) this.getItem(position);

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

        holder.name.setText(request.getFirst_Name()+" "+request.getLast_Name());
        holder.subject.setText(request.getSubject());
        holder.date.setText(request.getDate_Request());

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

