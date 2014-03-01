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

import java.util.ArrayList;

import sara.damien.app.R;

/**
 * Created by Sara-Fleur on 2/27/14.
 */
public class MessageAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<Message> mMessages;



    public MessageAdapter(Context context, ArrayList<Message> messages) {
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
        if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_row, parent, false);
            holder.message = (TextView) convertView.findViewById(R.id.message_text);
            holder.time = (TextView) convertView.findViewById(R.id.message_time);
            holder.global = (LinearLayout) convertView.findViewById(R.id.global);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        holder.message.setText(message.getMessage());
        holder.time.setText(message.getTime());

        LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();
        LayoutParams lglobal = (LayoutParams) holder.global.getLayoutParams();
        //check if it is a status message then remove background, and change text color.
        if(message.isStatusMessage())
        {
            holder.message.setBackground(null);
            lglobal.gravity = Gravity.LEFT;
            holder.message.setTextColor(R.color.textFieldColor);
        }
        else
        {
            //Check whether message is mine to show green background and align to right
            if(message.isMine())
            {
                if (message.isSent()){
                    holder.global.setBackgroundResource(R.drawable.speech_bubble_green);
                    lglobal.gravity = Gravity.RIGHT;
                }
                else{
                holder.global.setBackgroundResource(R.drawable.speech_bubble_orange);
                lglobal.gravity = Gravity.RIGHT;
                }
            }
            //If not mine then it is from sender to show orange background and align to left
            else
            {
                holder.global.setBackgroundResource(R.drawable.speech_bubble_orange);
                lglobal.gravity = Gravity.LEFT;
            }
            holder.message.setLayoutParams(lp);
            holder.message.setTextColor(R.color.textColor);
        }
        return convertView;
    }
    private static class ViewHolder
    {
        TextView message;
        TextView time;
        LinearLayout global;
    }

    @Override
    public long getItemId(int position) {
        //Unimplemented, because we aren't using Sqlite.
        return position;
    }


}
