package sara.damien.app.chat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.text.ParseException;
import java.util.List;

import sara.damien.app.Common;
import sara.damien.app.R;

/**
 * Created by Sara-Fleur on 2/27/14.
 */
public class MessageAdapter extends BaseAdapter {
    private Context mContext;
    private List<Message> mMessages;

    public MessageAdapter(Context context, List<Message> messages) {
        super();
        this.mContext = context;
        this.mMessages = messages;
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = (Message) this.getItem(position);

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_row, parent, false);
            holder.message = (TextView) convertView.findViewById(R.id.message_text);
            holder.time = (TextView) convertView.findViewById(R.id.message_time);
            holder.global = (LinearLayout) convertView.findViewById(R.id.global);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.message.setText(message.getContent());
        try {
            holder.time.setText(Common.setDateToLocalTimezone(message.getTime()));
        } catch (ParseException e) {
            //TODO: incompatible date format
            e.printStackTrace();
        }

        LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();
        LayoutParams lglobal = (LayoutParams) holder.global.getLayoutParams();


        if (message.isMine()) {
            if (message.isSent()) {
                holder.global.setBackgroundResource(R.drawable.speech_bubble_green);
                lglobal.gravity = Gravity.RIGHT;
            } else {
                holder.global.setBackgroundResource(R.drawable.speech_bubble_orange);
                lglobal.gravity = Gravity.RIGHT;
            }
        } else {
            holder.global.setBackgroundResource(R.drawable.speech_bubble_orange);
            lglobal.gravity = Gravity.LEFT;
        }

        holder.message.setLayoutParams(lp);
        holder.message.setTextColor(R.color.textColor); //FIXME: GetColor

        return convertView;
    }

    private static class ViewHolder {
        TextView message;
        TextView time;
        LinearLayout global;
    }

    @Override
    public long getItemId(int position) { //TODO: Check
        //throw new UnsupportedOperationException();
        //tring ID = mMessages.get(position).getID();
        return position;
        //return Long.valueOf(mMessages.get(position).getID());
    }
}
