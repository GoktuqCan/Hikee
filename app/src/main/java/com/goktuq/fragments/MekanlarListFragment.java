package com.goktuq.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.goktuq.MekanlarListAdaptor;
import com.goktuq.youtubedemo.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casper on 06.05.2016.
 */
public class MekanlarListFragment extends Fragment{


    String kulId = "";
    static String filename = "OtoFile";
    SharedPreferences sharedData;
    final ArrayList<String> mekanList = null;
    ListAdapter adaptor;
    ListView mekanListView;
    String[] mekanlar;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mekanlar, container,false);
        sharedData = this.getActivity().getSharedPreferences(filename, Context.MODE_PRIVATE);
        kulId = sharedData.getString("kulId", "bulunamadi");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final ArrayList<String> mekanList = getForMekanlar();
        final String[] mekanlar = new String[mekanList.size()];
        int sayac = 0;
        for(String s : mekanList) {
            mekanlar[sayac] = s;
            sayac++;
        }
        adaptor = new MekanlarListAdaptor(this.getActivity(),mekanlar);
        mekanListView = (ListView)view.findViewById(R.id.mekanList);
        mekanListView.setAdapter(adaptor);
        mekanListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Seçenekler");
                String[] items = {"Düzenle","Sil"};
                AlertDialog.Builder builder1 = builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if(item==0){
                            mekanDuzeltDialog(mekanlar[position]);
                        }
                        if (item == 1) {
                            mekanSil(mekanlar[position].split("lok")[5]);
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return false;
            }
        });
        return view;
    }

    private void mekanDuzeltDialog(final String mekan){
        final Timestamp timeBas = Timestamp.valueOf(mekan.split("lok")[1]);
        final Timestamp timeBit = Timestamp.valueOf(mekan.split("lok")[2]);
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(getActivity());
        final EditText titleBox = new EditText(getActivity());
        titleBox.setHint("Etiket Adı");
        titleBox.setText(mekan.split("lok")[0]);
        layout.addView(titleBox);

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
        builder.setTitle("Düzelt");
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
                mekanDuzeltService(mekan.split("lok")[5],titleBox.getText().toString(),timeBas.toString(),timeBit.toString());
            }
        });
        builder.setNegativeButton("Vazgeç", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do something
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void mekanDuzeltService(String id,String mekanAdi,String timeBas,String timeBit){
        String METHOD_NAME = "mekanDuzelt";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/mekanDuzelt";
        String URL = "http://otostopaws.iv8wvcggmq.eu-central-1.elasticbeanstalk.com/services/DemoDagitik?wsdl";
        // SOAP must be the same version as the webservice.
        int SOAP_VERSION = SoapEnvelope.VER11;
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("id", id);
            request.addProperty("mekanAdi", mekanAdi);
            request.addProperty("timeBas", timeBas);
            request.addProperty("timeBit", timeBit);
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
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new MekanlarListFragment()).commit();
    }

    private void mekanSil(String id){
        String METHOD_NAME = "mekanSil";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/mekanSil";
        String URL = "http://otostopaws.iv8wvcggmq.eu-central-1.elasticbeanstalk.com/services/DemoDagitik?wsdl";
        // SOAP must be the same version as the webservice.
        int SOAP_VERSION = SoapEnvelope.VER11;
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("id", id);
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
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new MekanlarListFragment()).commit();
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public ArrayList<String> getForMekanlar() {
        String METHOD_NAME = "mekanlarByKulId";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/mekanlarByKulId";
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
}
