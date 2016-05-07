package com.goktuq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.goktuq.youtubedemo.R;

/**
 * Created by Casper on 06.05.2016.
 */
public class MekanlarListAdaptor extends ArrayAdapter<String> {

    public MekanlarListAdaptor(Context context, String[] mekanlar) {
        super(context, R.layout.row_mekanlar, mekanlar);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mekanInflater = LayoutInflater.from(getContext());
        View customView = mekanInflater.inflate(R.layout.row_mekanlar,parent,false);
        String singleMekanItem = getItem(position);
        TextView mekanText = (TextView)customView.findViewById(R.id.mekanText);
        ImageView mekanImage = (ImageView)customView.findViewById(R.id.mekanImage);

        mekanText.setText(singleMekanItem);
        mekanImage.setImageResource(R.mipmap.araba);
        return customView;
    }
}
