package com.goktuq.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.goktuq.youtubedemo.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GmapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private GoogleMap mMap;
    private Map<String, String> zamanlarBaslangic = new HashMap<String, String>();
    ;
    private Map<String, String> zamanlarBitis = new HashMap<String, String>();
    MapFragment mapFragment;
    static String filename = "OtoFile";
    SharedPreferences sharedData;
    String kulId = "";
    long THREE_HOUR = 1000*60*60*3;

    public static GmapFragment newInstance() {
        GmapFragment fragment = new GmapFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gmaps, null, false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mapFragment = (MapFragment) this.getChildFragmentManager()
                    .findFragmentById(R.id.map_etiket);
        } else {
            mapFragment = (MapFragment) this.getFragmentManager()
                    .findFragmentById(R.id.map_etiket);
        }
        mapFragment.getMapAsync(this);
        sharedData = this.getActivity().getSharedPreferences(filename, Context.MODE_PRIVATE);
        kulId = sharedData.getString("kulId", "bulunamadi");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        GoogleMap googleHarita = googleMap;
        Double lat = 40.8211636937, longi = 29.9254438750;
        if (googleHarita != null) {
            googleHarita.setOnMarkerClickListener(this);
            for (String ss : getForMarkers()) {
                String[] ayri = ss.split("lok");
                Timestamp timestampIlk = new Timestamp(Long.parseLong(ayri[3])-THREE_HOUR);
                Timestamp timestamp = new Timestamp(Long.parseLong(ayri[4])-THREE_HOUR);
                lat = Double.parseDouble(ayri[0]);
                longi = Double.parseDouble(ayri[1]);
                Marker marker = googleHarita.addMarker(new MarkerOptions()
                        .position(
                                new LatLng(Double.parseDouble(ayri[0]),
                                        Double.parseDouble(ayri[1])))
                        .snippet(timestampIlk.getHours() + ":" + timestampIlk.getMinutes() + "-" + timestamp.getHours() + ":" + timestamp.getMinutes() + " Arası")
                        .title(ayri[5])
                        .anchor(.5f, .5f));
                marker.showInfoWindow();
                zamanlarBaslangic.put(marker.getId(), ayri[3]);
                zamanlarBitis.put(marker.getId(), ayri[4]);
            }
            googleHarita.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lat, longi), 14.6f));
            googleHarita.setMyLocationEnabled(true);
            googleHarita.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    public ArrayList<String> getForMarkers() {
        String METHOD_NAME = "gunlukEtiket";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/gunlukEtiket";
        String URL = "http://otostopaws.iv8wvcggmq.eu-central-1.elasticbeanstalk.com/services/DemoDagitik?wsdl";
        // SOAP must be the same version as the webservice.
        int SOAP_VERSION = SoapEnvelope.VER11;
        List<String> acc = new ArrayList<String>();
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("kulID", kulId);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SOAP_VERSION);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.debug = true;
            androidHttpTransport.call(SOAP_ACTION, envelope);
            KvmSerializable ks = (KvmSerializable) envelope.bodyIn;
            for (int i = 0; i < ks.getPropertyCount(); i++) {
                if (ks.getProperty(i) != null)
                    acc.add(ks.getProperty(i).toString());//Dizi elemanı
            }
            return (ArrayList<String>) acc;
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    public boolean onMarkerClick(final Marker arg0) {
        final Timestamp timeBas = new Timestamp(Long.parseLong(zamanlarBaslangic.get(arg0.getId()))-THREE_HOUR);
        final Timestamp timeBit = new Timestamp(Long.parseLong(zamanlarBitis.get(arg0.getId()))-THREE_HOUR);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        ScrollView scroll = new ScrollView(getActivity());

        final EditText titleBox = new EditText(getActivity());
        titleBox.setHint("Etiket Adı");
        layout.addView(titleBox);

        LinearLayout layoutDates = new LinearLayout(getActivity());
        layoutDates.setOrientation(LinearLayout.HORIZONTAL);
        final DatePicker datePickerBaslangic = new DatePicker(getActivity());
        datePickerBaslangic.setCalendarViewShown(false);
        try {
            Field f[] = datePickerBaslangic.getClass().getDeclaredFields();
            for (Field field : f) {
                if (field.getName().equals("mYearPicker")|| field.getName().equals("mYearSpinner")) {
                    field.setAccessible(true);
                    Object yearPicker = new Object();
                    yearPicker = field.get(datePickerBaslangic);
                    ((View) yearPicker).setVisibility(View.GONE);
                }
            }
        }
        catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH,timeBas.getDate());
        cal.set(Calendar.MONTH,timeBas.getMonth());
        cal.set(Calendar.YEAR,timeBas.getYear());
        datePickerBaslangic.setMinDate(cal.getTimeInMillis()-(1000*60*60*24));
        datePickerBaslangic.setMaxDate(cal.getTimeInMillis()+(1000*60*60*24));
        datePickerBaslangic.updateDate(timeBas.getYear(),timeBas.getMonth(),timeBas.getDate());
        layoutDates.addView(datePickerBaslangic);
        final DatePicker datePickerBitis = new DatePicker(getActivity());
        datePickerBitis.setCalendarViewShown(false);
        try {
            Field f[] = datePickerBitis.getClass().getDeclaredFields();
            for (Field field : f) {
                if (field.getName().equals("mYearPicker")|| field.getName().equals("mYearSpinner")) {
                    field.setAccessible(true);
                    Object yearPicker = new Object();
                    yearPicker = field.get(datePickerBitis);
                    ((View) yearPicker).setVisibility(View.GONE);
                }
            }
        }
        catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }
        cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH,timeBit.getDate());
        cal.set(Calendar.MONTH,timeBit.getMonth());
        cal.set(Calendar.YEAR,timeBit.getYear());
        datePickerBitis.setMinDate(cal.getTimeInMillis()-(1000*60*60*24));
        datePickerBitis.setMaxDate(cal.getTimeInMillis()+(1000*60*60*24));
        datePickerBitis.updateDate(timeBit.getYear(),timeBit.getMonth(),timeBit.getDate());
        layoutDates.addView(datePickerBitis);
        layout.addView(layoutDates);

        LinearLayout layoutTimes = new LinearLayout(getActivity());
        layoutTimes.setOrientation(LinearLayout.HORIZONTAL);
        final TimePicker pickerBaslangic = new TimePicker(getActivity());
        pickerBaslangic.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pickerBaslangic.setHour(timeBas.getHours());
            pickerBaslangic.setMinute(timeBas.getMinutes());
        }else{
            pickerBaslangic.setCurrentHour(timeBas.getHours());
            pickerBaslangic.setCurrentMinute(timeBas.getMinutes());
        }
        layoutTimes.addView(pickerBaslangic);
        final TimePicker pickerBitis = new TimePicker(getActivity());
        pickerBitis.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pickerBitis.setHour(timeBit.getHours());
            pickerBitis.setMinute(timeBit.getMinutes());
        }else{
            pickerBitis.setCurrentHour(timeBit.getHours());
            pickerBitis.setCurrentMinute(timeBit.getMinutes());
        }
        layoutTimes.addView(pickerBitis);
        layout.addView(layoutTimes);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Etiketi Düzenle");
        scroll.addView(layout);
        builder.setView(scroll);

        builder.setPositiveButton("Kaydet", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timeBas.setHours(pickerBaslangic.getHour());
                    timeBas.setMinutes(pickerBaslangic.getMinute());
                    timeBit.setHours(pickerBitis.getHour());
                    timeBit.setMinutes(pickerBitis.getMinute());
                }else{
                    timeBas.setHours(pickerBaslangic.getCurrentHour());
                    timeBas.setMinutes(pickerBaslangic.getCurrentMinute());
                    timeBit.setHours(pickerBitis.getCurrentHour());
                    timeBit.setMinutes(pickerBitis.getCurrentMinute());
                }
                timeBas.setDate(datePickerBaslangic.getDayOfMonth());
                timeBas.setMonth(datePickerBaslangic.getMonth());
                timeBit.setDate(datePickerBitis.getDayOfMonth());
                timeBit.setMonth(datePickerBitis.getMonth());
                timeBas.setTime(timeBas.getTime()+THREE_HOUR);
                timeBit.setTime(timeBit.getTime()+THREE_HOUR);
                mekanKayit(kulId, titleBox.getText().toString(), arg0.getPosition().latitude + "", arg0.getPosition().longitude + "", timeBas.getTime()+"", timeBit.getTime()+"");
            }
        });
        builder.setNegativeButton("Vazgeç", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do something
            }
        });
        /*builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mekanKayit("1", items[item].toString(), arg0.getPosition().latitude + "", arg0.getPosition().longitude + "", zamanlarBaslangic.get(arg0.getId()), zamanlarBitis.get(arg0.getId()));
            }
        });*/
        AlertDialog alert = builder.create();
        alert.show();
        return false;
    }

    public void mekanKayit(String kulID, String mekanAdi, String enlem,
                           String boylam, String baslangicZaman, String bitisZaman) {
        String METHOD_NAME = "mekanKayit";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/mekanKayit";
        String URL = "http://otostopaws.iv8wvcggmq.eu-central-1.elasticbeanstalk.com/services/DemoDagitik?wsdl";
        // SOAP must be the same version as the webservice.
        int SOAP_VERSION = SoapEnvelope.VER11;
        List<String> acc = new ArrayList<String>();
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("mekanAdi", mekanAdi);
            request.addProperty("kulID", kulID);
            request.addProperty("enlem", enlem);
            request.addProperty("boylam", boylam);
            request.addProperty("baslangicZaman", baslangicZaman);
            request.addProperty("bitisZaman", bitisZaman);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SOAP_VERSION);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.debug = true;
            androidHttpTransport.call(SOAP_ACTION, envelope);
            Object result = envelope.getResponse();
            String resultData = result.toString();
            Toast.makeText(getActivity(), resultData,
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            FragmentManager fm = getActivity().getFragmentManager();
            Fragment fragment = (fm.findFragmentById(R.id.map_etiket));
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }
    }
}
