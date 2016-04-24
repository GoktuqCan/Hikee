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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GmapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private GoogleMap mMap;
    private Map<String, String> zamanlar = new HashMap<String, String>();
    MapFragment mapFragment;
    static String filename = "OtoFile";
    SharedPreferences sharedData;
    String kulId = "";

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
        }else {
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
        Double lat = 45.0,longi=29.0;
        if (googleHarita != null) {
            for (String ss : getForMarkers()) {
                String[] ayri = ss.split("lok");
                lat = Double.parseDouble(ayri[0]);
                longi = Double.parseDouble(ayri[1]);
                googleHarita.setOnMarkerClickListener(this);
                Marker marker = googleHarita.addMarker(new MarkerOptions()
                        .position(
                                new LatLng(Double.parseDouble(ayri[0]),
                                        Double.parseDouble(ayri[1])))
                        .snippet(ayri[2])
                        .title(ayri[4])
                        .anchor(.5f, .5f));
                marker.showInfoWindow();
                zamanlar.put(marker.getId(), ayri[3]);
            }
            googleHarita.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lat,longi), 14.6f));
            googleHarita.setMyLocationEnabled(true);
            googleHarita.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    public ArrayList<String> getForMarkers() {
        String METHOD_NAME = "besDakika";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/besDakika";
        String URL = "http://gggaws-txbam3mqqn.elasticbeanstalk.com/services/DemoDagitik?wsdl";
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
                acc.add(ks.getProperty(i).toString());
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
        final CharSequence[] items = { "Ev", "İş", "Okul" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Etiket Seçiniz");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mekanKayit("1", items[item].toString(), arg0.getPosition().latitude+"", arg0.getPosition().longitude+"",zamanlar.get(arg0.getId()));
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        return false;
    }

    public void mekanKayit(String kulID, String mekanAdi, String enlem,
                           String boylam,String zaman) {
        String METHOD_NAME = "mekanKayit";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/mekanKayit";
        String URL = "http://gggaws-txbam3mqqn.elasticbeanstalk.com/services/DemoDagitik?wsdl";
        // SOAP must be the same version as the webservice.
        int SOAP_VERSION = SoapEnvelope.VER11;
        List<String> acc = new ArrayList<String>();
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("mekanAdi", mekanAdi);
            request.addProperty("kulID", kulID);
            request.addProperty("enlem", enlem);
            request.addProperty("boylam", boylam);
            request.addProperty("zaman", zaman);
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
        FragmentManager fm = getActivity().getFragmentManager();
        Fragment fragment = (fm.findFragmentById(R.id.map_etiket));
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }
}
