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
 * Created by Casper on 19.05.2016.
 */
public class YakindakilerListAdaptor extends ArrayAdapter<String> {

    public YakindakilerListAdaptor(Context context, String[] yakindakiler) {
        super(context, R.layout.row_yakindakiler, yakindakiler);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mekanInflater = LayoutInflater.from(getContext());
        View customView = mekanInflater.inflate(R.layout.row_yakindakiler, parent, false);
        String singleMekanItem = getItem(position);
        String[] properties = singleMekanItem.split("lok");

        TextView yakindakiText = (TextView) customView.findViewById(R.id.yakindakiText);
        ImageView yakindakiImage = (ImageView) customView.findViewById(R.id.yakindakiImage);

        yakindakiText.setText(properties[0] + " " +properties[1] + " metre");
        String ek = properties[2];
        if(ek.equals("true")) {
            yakindakiImage.setImageResource(R.mipmap.araba);
        }else {
            yakindakiImage.setImageResource(R.mipmap.otostopcu);
        }
        return customView;
    }
}
