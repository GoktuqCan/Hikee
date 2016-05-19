package com.goktuq.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
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

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

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

    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;
    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static GmapFragment newInstance() {
        GmapFragment fragment = new GmapFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gmaps, null, false);
        if (canAccessLocation()) {
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(getActivity(),perm));
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
            googleHarita.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
        titleBox.setHint("name");
        layout.addView(titleBox);

        LinearLayout layoutDates = new LinearLayout(getActivity());
        layoutDates.setOrientation(LinearLayout.HORIZONTAL);
        final DatePicker datePickerBaslangic = initDatePicker();
        datePickerBaslangic.setCalendarViewShown(false);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH,timeBas.getDate());
        cal.set(Calendar.MONTH,timeBas.getMonth());
        cal.set(Calendar.YEAR,timeBas.getYear());
        datePickerBaslangic.setMinDate(cal.getTimeInMillis()-(1000*60*60*24));
        datePickerBaslangic.setMaxDate(cal.getTimeInMillis()+(1000*60*60*24));
        datePickerBaslangic.updateDate(timeBas.getYear(),timeBas.getMonth(),timeBas.getDate());
        layoutDates.addView(datePickerBaslangic);
        final DatePicker datePickerBitis = initDatePicker();
        datePickerBitis.setCalendarViewShown(false);
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
        final TimePicker pickerBaslangic = new TimePicker(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar));
        pickerBaslangic.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pickerBaslangic.setHour(timeBas.getHours());
            pickerBaslangic.setMinute(timeBas.getMinutes());
        }else{
            pickerBaslangic.setCurrentHour(timeBas.getHours());
            pickerBaslangic.setCurrentMinute(timeBas.getMinutes());
        }
        layoutTimes.addView(pickerBaslangic);
        final TimePicker pickerBitis = new TimePicker(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar));
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
        builder.setTitle("Düzenle");
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
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    public DatePicker initDatePicker(){
        DatePicker dp_mes = new DatePicker(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar));

        int year    = dp_mes.getYear();
        int month   = dp_mes.getMonth();
        int day     = dp_mes.getDayOfMonth();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int daySpinnerId = Resources.getSystem().getIdentifier("day", "id", "android");
            if (daySpinnerId != 0)
            {
                View daySpinner = dp_mes.findViewById(daySpinnerId);
                if (daySpinner != null)
                {
                    daySpinner.setVisibility(View.VISIBLE);
                }
            }

            int monthSpinnerId = Resources.getSystem().getIdentifier("month", "id", "android");
            if (monthSpinnerId != 0)
            {
                View monthSpinner = dp_mes.findViewById(monthSpinnerId);
                if (monthSpinner != null)
                {
                    monthSpinner.setVisibility(View.VISIBLE);
                }
            }

            int yearSpinnerId = Resources.getSystem().getIdentifier("year", "id", "android");
            if (yearSpinnerId != 0)
            {
                View yearSpinner = dp_mes.findViewById(yearSpinnerId);
                if (yearSpinner != null)
                {
                    yearSpinner.setVisibility(View.GONE);
                }
            }
        } else { //Older SDK versions
            Field f[] = dp_mes.getClass().getDeclaredFields();
            for (Field field : f)
            {
                if(field.getName().equals("mDayPicker") || field.getName().equals("mDaySpinner"))
                {
                    field.setAccessible(true);
                    Object dayPicker = null;
                    try {
                        dayPicker = field.get(dp_mes);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ((View) dayPicker).setVisibility(View.VISIBLE);
                }

                if(field.getName().equals("mMonthPicker") || field.getName().equals("mMonthSpinner"))
                {
                    field.setAccessible(true);
                    Object monthPicker = null;
                    try {
                        monthPicker = field.get(dp_mes);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ((View) monthPicker).setVisibility(View.VISIBLE);
                }

                if(field.getName().equals("mYearPicker") || field.getName().equals("mYearSpinner"))
                {
                    field.setAccessible(true);
                    Object yearPicker = null;
                    try {
                        yearPicker = field.get(dp_mes);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ((View) yearPicker).setVisibility(View.GONE);
                }
            }
        }
        return dp_mes;
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
