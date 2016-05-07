package com.goktuq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.goktuq.youtubedemo.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by Casper on 24.04.2016.
 */
public class UyeOl extends Activity {

    Location location1;
    double enlem = 0, boylam = 0;
    boolean araba;
    String ad, soyad, email, sifre, ogrno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uyeol);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void kayitYap(View v) {
        LocationManager konumYoneticisi = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener konumDinleyicisi = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onLocationChanged(Location loc) {
                location1 = new Location(loc);
            }
        };
        boolean gps_enabled = konumYoneticisi
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = konumYoneticisi
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (gps_enabled) {
            konumYoneticisi.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, konumDinleyicisi);
        } else if (network_enabled) {
            konumYoneticisi.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, konumDinleyicisi);
        }
        if (location1 != null) {
            enlem = location1.getLatitude();
            boylam = location1.getLongitude();
        }
        araba = ((Switch) findViewById(R.id.kayitSwitch)).isChecked();
        ad = ((EditText) findViewById(R.id.ad)).getText().toString();
        soyad = ((EditText) findViewById(R.id.soyad)).getText().toString();
        email = ((EditText) findViewById(R.id.email)).getText().toString();
        sifre = ((EditText) findViewById(R.id.sifreUyeOl)).getText().toString();
        ogrno = ((EditText) findViewById(R.id.ogrNo)).getText().toString();
        kayitGonder();
        startActivity(new Intent(UyeOl.this, Giris.class));
    }

    private void kayitGonder() {
        String METHOD_NAME = "uyeOl";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/uyeOl";
        String URL = "http://otostopaws.iv8wvcggmq.eu-central-1.elasticbeanstalk.com/services/DemoDagitik?wsdl";
        // SOAP must be the same version as the webservice.
        int SOAP_VERSION = SoapEnvelope.VER11;
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("email", email);
            request.addProperty("sifre", sifre);
            request.addProperty("ad", ad);
            request.addProperty("soyad", soyad);
            request.addProperty("ogrno", ogrno);
            request.addProperty("araba", new Boolean(araba).toString());
            request.addProperty("latitude", new Double(enlem).toString());
            request.addProperty("longitude", new Double(boylam).toString());
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SOAP_VERSION);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.debug = true;
            androidHttpTransport.call(SOAP_ACTION, envelope);
            Object result = envelope.getResponse();
            String resultData = result.toString();
            Toast.makeText(getApplicationContext(), resultData,
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

}
