package com.goktuq;

import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.goktuq.fragments.AyarlarFragment;
import com.goktuq.fragments.CanliMapFragment;
import com.goktuq.fragments.GmapFragment;
import com.goktuq.fragments.MekanlarListFragment;
import com.goktuq.fragments.YakindakilerListFragment;
import com.goktuq.youtubedemo.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    int idOnceki = -1;
    static String filename = "OtoFile";
    SharedPreferences sharedData;
    String kulId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        String mesaj = "";
        if (extras != null)
            if (extras.getString("EtiketBildirim") != null) {
                mesaj = extras.getString("EtiketBildirim");
            }

        if (mesaj.equals("Harita")) {
            navigationView.setCheckedItem(R.id.nav_gallery);
            idOnceki = R.id.nav_gallery;
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.content_frame, new GmapFragment()).commit();
        } else {
            navigationView.setCheckedItem(R.id.nav_camara);
            idOnceki = R.id.nav_camara;
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.content_frame, new YakindakilerListFragment()).commit();
        }


        sharedData = this.getSharedPreferences(filename, Context.MODE_PRIVATE);
        kulId = sharedData.getString("kulId", "bulunamadi");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        View header = navigationView.getHeaderView(0);
        TextView isim = (TextView) header.findViewById(R.id.txtIsim);
        TextView email = (TextView) header.findViewById(R.id.textView);
        String[] cevap = kullaniciBilgi(kulId);
        isim.setText(cevap[0]);
        email.setText(cevap[1]);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.content_frame, new AyarlarFragment()).commit();
        }
        idOnceki = R.id.nav_share;
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_share);
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();

        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            if (idOnceki != id)
                fm.beginTransaction().replace(R.id.content_frame, new YakindakilerListFragment()).commit();
        } else if (id == R.id.nav_gallery) {
            if (idOnceki != id)
                fm.beginTransaction().replace(R.id.content_frame, new GmapFragment()).commit();
        } else if (id == R.id.nav_slideshow) {
            if (idOnceki != id)
                fm.beginTransaction().replace(R.id.content_frame, new MekanlarListFragment()).commit();
        } else if (id == R.id.nav_manage) {
            if (idOnceki != id)
                fm.beginTransaction().replace(R.id.content_frame, new CanliMapFragment()).commit();
        } else if (id == R.id.nav_share) {
            if (idOnceki != id)
                fm.beginTransaction().replace(R.id.content_frame, new AyarlarFragment()).commit();
        } else if (id == R.id.nav_send) {
            if (idOnceki != id) {
                servisiKapat();
                startActivity(new Intent(MainActivity.this, Giris.class));
            }
        }
        idOnceki = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void servisiKapat() {
        ActivityManager servisYoneticisi = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo servis : servisYoneticisi
                .getRunningServices(Integer.MAX_VALUE)) {
            if (this.getPackageName().equals(servis.service.getPackageName())) {
                this.stopService(new Intent(this, KonumServisi.class));
            }
        }
    }

    private String[] kullaniciBilgi(String kulId) {
        String METHOD_NAME = "kullaniciBilgi";// Method ad�
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/kullaniciBilgi";
        String URL = "http://otostopaws.iv8wvcggmq.eu-central-1.elasticbeanstalk.com/services/DemoDagitik?wsdl";
        int SOAP_VERSION = SoapEnvelope.VER11;
        String[] cevap = new String[2];
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
                cevap[i] = ks.getProperty(i).toString();
            }
            return cevap;
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

}
