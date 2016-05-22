package com.goktuq;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.goktuq.youtubedemo.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by Casper on 24.04.2016.
 */
public class Giris extends Activity {

    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int INITIAL_REQUEST=1337;

    static String filename = "OtoFile";
    SharedPreferences sharedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedData = getSharedPreferences(filename, MODE_PRIVATE);
        String donenogrNo = sharedData.getString("ogrNo", "bulunamadi");
        String donenSifre = sharedData.getString("sifre", "bulunamadi");

        if (donenogrNo != "bulunamadi" && donenSifre != "bulunamadi") {
            EditText ogrNo = (EditText) findViewById(R.id.ogrNoGiris);
            EditText sifre = (EditText) findViewById(R.id.sifreGiris);
            ogrNo.setText(donenogrNo);
            sifre.setText(donenSifre);
        }
        if (!canAccessLocation()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }
    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
        }
        return true;
    }

    public void kayitSayfasi(View v) {
        startActivity(new Intent(Giris.this, UyeOl.class));
    }

    public void giris(View v) {
        beniHatirlaFonk();
        startActivity(new Intent(Giris.this, MainActivity.class));
    }

    private void beniHatirlaFonk(){
        CheckBox beniHatirla = (CheckBox) findViewById(R.id.beniHatirla);
        EditText ogrNo = (EditText) findViewById(R.id.ogrNoGiris);
        EditText sifre = (EditText) findViewById(R.id.sifreGiris);
        sharedData = getSharedPreferences(filename, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedData.edit();
        if (beniHatirla.isChecked()) {
            editor.putString("kulId", idGetir(ogrNo.getText().toString()));
            editor.putString("ogrNo", ogrNo.getText().toString());
            editor.putString("sifre", sifre.getText().toString());
            editor.commit();
        } else {
            editor.remove("kulId");
            editor.remove("ogrNo");
            editor.remove("sifre");
            editor.commit();
        }
    }

    public String idGetir(String ogrNo) {
        String METHOD_NAME = "findIdByOgrNo";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/findIdByOgrNo";
        String URL = "http://otostopaws.iv8wvcggmq.eu-central-1.elasticbeanstalk.com/services/DemoDagitik?wsd";
        // SOAP must be the same version as the webservice.
        int SOAP_VERSION = SoapEnvelope.VER11;
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("ogrNo", ogrNo);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SOAP_VERSION);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            // androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            androidHttpTransport.debug = true;
            androidHttpTransport.call(SOAP_ACTION, envelope);
            Object result = envelope.getResponse();
            String resultData = result.toString();
            return resultData;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            return "";
        }
    }

}
