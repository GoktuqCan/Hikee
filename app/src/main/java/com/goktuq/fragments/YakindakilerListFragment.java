package com.goktuq.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.goktuq.YakindakilerListAdaptor;
import com.goktuq.youtubedemo.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casper on 19.05.2016.
 */
public class YakindakilerListFragment extends Fragment {

    String kulId = "";
    static String filename = "OtoFile";
    SharedPreferences sharedData;
    final ArrayList<String> yakinList = null;
    ListAdapter adaptor;
    ListView yakinListView;
    String[] yakinlar;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_yakindakiler, container,false);
        sharedData = this.getActivity().getSharedPreferences(filename, Context.MODE_PRIVATE);
        kulId = sharedData.getString("kulId", "bulunamadi");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final ArrayList<String> yakinList = getForMarkers();
        final String[] yakindakiler = new String[yakinList.size()];
        int sayac = 0;
        for(String s : yakinList) {
            yakindakiler[sayac] = s;
            sayac++;
        }
        adaptor = new YakindakilerListAdaptor(this.getActivity(),yakindakiler);
        yakinListView = (ListView)view.findViewById(R.id.yakinList);
        yakinListView.setAdapter(adaptor);
        return view;
    }


    public ArrayList<String> getForMarkers() {
        String METHOD_NAME = "getYakindakiler";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/getYakindakiler";
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
