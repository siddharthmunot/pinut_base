package org.wiflick.wiflickhome;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainViewAdapter extends BaseAdapter{
    private String TAG = "wiflick";

    private Context mContext;
    private final String[] titles;
    private final int[] Imageid;

    public MainViewAdapter(Context c, String[] titles, int[] Imageid) {
        mContext = c;
        this.Imageid = Imageid;
        this.titles = titles;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return titles.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Log.i(TAG, "position#" + position);
        grid = (View) convertView;

        if (convertView == null) {
            Log.i(TAG, "position#" + position);

            //grid = new View(mContext);
            grid = inflater.inflate(R.layout.main_grid_layout, null);
        }
            //TextView textView = (TextView) grid.findViewById(R.id.icon_text);
            ImageView imageView = (ImageView)grid.findViewById(R.id.icon_image);
            Log.i(TAG, "position#" + position);
        //textView.setText(titles[position]);
        Log.i(TAG, "titles = " + titles[position]);
        imageView.setImageResource(Imageid[position]);
        grid.setBackgroundColor(Color.rgb(212, 207, 207));
       // } else {
         //   grid = (View) convertView;
        //}

        return grid;
    }
}