package com.goktuq.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Casper on 24.04.2016.
 */
public class CanliMapFragment extends Fragment implements OnMapReadyCallback{

    Timer timer;
    Handler handler;
    GoogleMap googleHarita;
    int focusSayac = 0;

    final static long TIME = 3000;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_canlimap, null, false);
        MapFragment mapFragment;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mapFragment = (MapFragment) this.getChildFragmentManager()
                    .findFragmentById(R.id.map_canli);
        }else {
            mapFragment = (MapFragment) this.getFragmentManager()
                    .findFragmentById(R.id.map_canli);
        }
        mapFragment.getMapAsync(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return view;
    }

    private void bilgiVer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                harita();
            }
        });
    }

    private void harita(){
        googleHarita.clear();
            if (googleHarita != null) {
                if(focusSayac == 0 && googleHarita.getMyLocation() != null){
                    googleHarita.moveCamera(CameraUpdateFactory.newLatLng(
                            new LatLng(googleHarita.getMyLocation().getLatitude(), googleHarita.getMyLocation().getLongitude())));
                    focusSayac++;
                }
                for (String ss : getForMarkers()) {
                    String[] ayri = ss.split("lok");
                    Marker marker;
                    if (ayri[3].equals("True")) {
                        marker = googleHarita.addMarker(new MarkerOptions()
                                .position(
                                        new LatLng(Double.parseDouble(ayri[1]),
                                                Double.parseDouble(ayri[2])))
                                .title(ayri[0])
                                .anchor(.5f, .5f)
                                .snippet("")
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.mipmap.araba)));
                    } else {
                        marker = googleHarita.addMarker(new MarkerOptions()
                                .position(
                                        new LatLng(Double.parseDouble(ayri[1]),
                                                Double.parseDouble(ayri[2])))
                                .title(ayri[0])
                                .anchor(.5f, .5f)
                                .snippet("")
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.mipmap.otostopcu)));
                    }
                    marker.showInfoWindow();
                }
            }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleHarita = googleMap;
        googleHarita.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(40.822861, 29.923878), 14.6f));// B kap�s�
        googleHarita.setMyLocationEnabled(true);
        googleHarita.getUiSettings().setMyLocationButtonEnabled(true);
        timer = new Timer();
        handler = new Handler();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                    bilgiVer();
            }
        }, 0, TIME);
    }

    public ArrayList<String> getForMarkers() {
        String METHOD_NAME = "getForMarkers";// Method ad�
        String NAMESPACE = "http://tempuri.org/";
        String SOAP_ACTION = "http://tempuri.org/getForMarkers";
        String URL = "http://www.goktugcancakmak.com/WebService.asmx?WSDL";
        // SOAP must be the same version as the webservice.
        int SOAP_VERSION = SoapEnvelope.VER11;
        List<String> acc = new ArrayList<String>();
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SOAP_VERSION);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.debug = true;
            androidHttpTransport.call(SOAP_ACTION, envelope);

            SoapObject obj1 = (SoapObject) envelope.bodyIn;

            SoapObject obj2 = (SoapObject) obj1.getProperty(0);

            for (int i = 0; i < obj2.getPropertyCount(); i++) {
                String id1 = obj2.getProperty(i).toString();

                if (id1 != "") {
                    acc.add(id1);
                }
            }
            return (ArrayList<String>) acc;
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    public void onDestroyView() {
            super.onDestroyView();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            FragmentManager fm = getActivity().getFragmentManager();
            Fragment fragment = (fm.findFragmentById(R.id.map_canli));
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }
        timer.cancel();
    }
}
