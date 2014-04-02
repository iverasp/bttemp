package com.iegget.temp;

/**
 * Created by iver on 4/2/14.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyArrayAdapter extends ArrayAdapter<Food> {
    private final Context context;
    private final ArrayList<Food> values;

    public MyArrayAdapter(Context context, ArrayList<Food> values) {
        super(context, R.layout.list, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list, parent, false);
        TextView nameView = (TextView) rowView.findViewById(R.id.name);
        TextView descriptionView = (TextView) rowView.findViewById(R.id.description);
        TextView temperatureView = (TextView) rowView.findViewById(R.id.temperature);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        nameView.setText(values.get(position).getName());
        descriptionView.setText(values.get(position).getDescription());
        temperatureView.setText(values.get(position).getTemperature());
        imageView.setImageResource(values.get(position).getIcon());

        return rowView;
    }
}
