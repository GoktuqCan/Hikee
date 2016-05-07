package com.goktuq;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.goktuq.youtubedemo.R;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;


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
        String[] properties = singleMekanItem.split("lok");

        Timestamp baslangicTime = Timestamp.valueOf(properties[1]);
        Timestamp bitisTime = Timestamp.valueOf(properties[2]);

        TextView mekanText = (TextView)customView.findViewById(R.id.mekanText);
        ImageView mekanImage = (ImageView)customView.findViewById(R.id.mekanImage);

        mekanText.setText(properties[0]+":"+dayOfWeekNameFromNumber(baslangicTime.getDay())+" "+baslangicTime.getHours()+":"
                +baslangicTime.getMinutes()+"-"+dayOfWeekNameFromNumber(bitisTime.getDay())+" "+bitisTime.getHours()+":"
                +bitisTime.getMinutes());
        mekanImage.setImageBitmap(getGoogleMapThumbnail(Double.parseDouble(properties[3]),Double.parseDouble(properties[4])));
        return customView;
    }

    public static Bitmap getGoogleMapThumbnail(double lati, double longi){
        String URL = "http://maps.google.com/maps/api/staticmap?center=" +lati + "," + longi + "&zoom=17&size=720x300&sensor=false&maptype=hybrid&markers=color:red%7Clabel:S%7C"+lati+","+longi+"";
        Bitmap bmp = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet(URL);

        InputStream in = null;
        try {
            in = httpclient.execute(request).getEntity().getContent();
            bmp = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bmp;
    }

    private String dayOfWeekNameFromNumber(int sayi){
        switch (sayi) {
            case 0:
                return "Pazar";
            case 1:
                return "Pazartesi";
            case 2:
                return "Salı";
            case 3:
                return "Çarşamba";
            case 4:
                return "Perşembe";
            case 5:
                return "Cuma";
            case 6:
                return "Cumartesi";
            default:
                return "Gün";
                
        }
    }
}
