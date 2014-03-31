package sara.damien.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sara.damien.app.R;
import sara.damien.app.model.NavDrawerItem;

/**
 * Adapter for slide-in menu items
 */
public class NavDrawerAdapter extends ArrayAdapter<NavDrawerItem> {
    public NavDrawerAdapter(Context context, List<NavDrawerItem> objects) {
        super(context, -1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) super.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        TextView txtSubtitle = (TextView) convertView.findViewById(R.id.counter);

        NavDrawerItem item = super.getItem(position);
        imgIcon.setImageResource(item.getIcon());
        txtTitle.setText(item.getTitle());
        txtSubtitle.setText(item.getSubtitle());
        txtSubtitle.setVisibility(item.isSubtitleVisible() ? View.VISIBLE : View.GONE);

        return convertView;
    }
}