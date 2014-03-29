package sara.damien.app.chat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.List;

import sara.damien.app.R;
import sara.damien.app.utils.Utilities;

/**
 * Created by Sara-Fleur on 2/27/14.
 */
public class MessageAdapter extends ArrayAdapter<Message> {
    public MessageAdapter(Context context, List objects) {
        super(context, -1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = super.getItem(position);

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_row, parent, false);

            holder = new ViewHolder();
            holder.message = (TextView) convertView.findViewById(R.id.message_text);
            holder.time = (TextView) convertView.findViewById(R.id.message_time);
            holder.global = (LinearLayout) convertView.findViewById(R.id.global);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.message.setText(message.getContent());
        holder.time.setText(Utilities.deserializeAndConvertToLocalTime(message.getTime()));
        holder.global.setBackgroundResource((message.isSent() && message.isMine()) ? R.drawable.speech_bubble_green : R.drawable.speech_bubble_orange);
        ((LayoutParams) holder.global.getLayoutParams()).gravity = message.isMine() ? Gravity.RIGHT : Gravity.LEFT;

        return convertView;
    }

    //Info on the ViewHolder pattern at http://developer.android.com/training/improving-layouts/smooth-scrolling.html
    private static class ViewHolder {
        TextView message;
        TextView time;
        LinearLayout global;
    }
}
