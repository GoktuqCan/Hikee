package com.goktuq;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.goktuq.youtubedemo.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Casper on 23.04.2016.
 */
public class KonumServisi extends Service {


    Timer timer;
    Handler handler;
    Location location1;
    String kulId = "";
    static String filename = "OtoFile";
    SharedPreferences sharedData;
    Long onceki, sonraki;
    public static Long bildirimSure = (long) (10 * 1000);
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int TEN_MINUTES = 1000 * 60 * 10;

    private long oncekiEtiketSure = 0;

    final static long TIME = 10000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        onceki = null;
        sonraki = null;
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
                //if (isBetterLocation(loc, location1)) {
                location1 = new Location(loc);
                //}
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
        sharedData = getSharedPreferences(filename, MODE_PRIVATE);
        kulId = sharedData.getString("kulId", "bulunamadi");
        timer = new Timer();
        handler = new Handler();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (location1 != null) {
                    bilgiVer();

                }
            }
        }, 0, TIME);

    }

    private void bilgiVer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (location1 != null) {
                    new backgroundNetwork().execute(location1);
                }
            }
        });
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be
            // worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void lokasyonGuncelle(String kulId, double latitude, double longitude) {
        String METHOD_NAME = "lokasyonGuncelle";
        String NAMESPACE = "http://controller";
        String SOAP_ACTION = "http://controller/lokasyonGuncelle";
        String URL = "http://otostopaws.iv8wvcggmq.eu-central-1.elasticbeanstalk.com/services/DemoDagitik?wsdl";
        int SOAP_VERSION = SoapEnvelope.VER11;
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("kulID", kulId);
            request.addProperty("latitude", new Double(latitude).toString());
            request.addProperty("longitude", new Double(longitude).toString());
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SOAP_VERSION);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.debug = true;
            androidHttpTransport.call(SOAP_ACTION, envelope);
            Object result = envelope.getResponse();
            String resultData = result.toString();
            if (!resultData.equals("basarili")) {
                String[] bilgiler = resultData.split("loc");
                if (Double.parseDouble(bilgiler[1]) <= 200) {
                    bildirimSure = (long) (5 * 1000);
                } else {
                    bildirimSure = (long) (10 * 1000);
                }
                bildirimGoster(bilgiler[0], bilgiler[1]);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    class backgroundNetwork extends AsyncTask<Location, Void, Void> {

        private Exception exception;

        protected Void doInBackground(Location... urls) {
            sharedData = getSharedPreferences(filename, MODE_PRIVATE);
            kulId = sharedData.getString("kulId", "bulunamadi");
            String ID = sharedData.getString("kulId", "bulunamadi");
            if ((System.currentTimeMillis() - oncekiEtiketSure) >= TEN_MINUTES) {
                gunlukEtiket();
                oncekiEtiketSure = System.currentTimeMillis();
            }
            lokasyonGuncelle(ID, location1.getLatitude(),
                    location1.getLongitude());
            return null;
        }

        protected void onPostExecute(Void feed) {
            Toast.makeText(KonumServisi.this,
                    location1.getLatitude() + " " + location1.getLongitude(),
                    Toast.LENGTH_SHORT).show();
        }

        public void onDismiss() {
            backgroundNetwork.this.cancel(true);
        }
    }

    public void gunlukEtiket() {
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
            if (acc.size() > 0)
                bildirimEtiket();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void bildirimEtiket() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            NotificationManager mNotificationManager;
            mNotificationManager = (NotificationManager) getApplicationContext()
                    .getApplicationContext().getSystemService(
                            Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(getApplicationContext(),
                    MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(
                    getApplicationContext(), 0, intent, 0);
            Intent intentBilgi = new Intent(getApplicationContext(),
                    MainActivity.class);
            intentBilgi.putExtra("EtiketBildirim","Harita");
            intentBilgi.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pIntentBilgi = PendingIntent.getActivity(
                    getApplicationContext(), 0, intentBilgi, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
                    getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Etiketlemeniz Gereken Yerler Var")
                    .setContentText("Harita Üzerinde Görmek İçin Tıklayın")
                    .setTicker("Kaldığınız yerleri etiketleyin")
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setLights(Color.GREEN, 2000, 3000);
            mNotificationManager.notify(1001, nBuilder.build());
        } else {
            Toast.makeText(getApplicationContext(),
                    "You need a higher version", Toast.LENGTH_LONG).show();
        }
    }

    public void bildirimGoster(String kisi, String uzaklik) {
        if (onceki == null) {
            onceki = System.currentTimeMillis();
        } else {
            sonraki = System.currentTimeMillis();
        }
        if (sonraki == null || sonraki - onceki >= bildirimSure) {
            if (sonraki != null)
                onceki = new Long(sonraki);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                NotificationManager mNotificationManager;
                mNotificationManager = (NotificationManager) getApplicationContext()
                        .getApplicationContext().getSystemService(
                                Context.NOTIFICATION_SERVICE);
                Intent intent = new Intent(getApplicationContext(),
                        MainActivity.class);
                PendingIntent pIntent = PendingIntent.getActivity(
                        getApplicationContext(), 0, intent, 0);
                Intent intentBilgi = new Intent(getApplicationContext(),
                        MainActivity.class);
                intentBilgi.putExtra( "EtiketBildirim", "Harita");
                intentBilgi.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pIntentBilgi = PendingIntent.getActivity(
                        getApplicationContext(), 0, intentBilgi, 0);
                NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
                        getApplicationContext())
                        .setSmallIcon(R.mipmap.araba)
                        .setContentTitle(kisi + " - " + uzaklik + " metre")
                        .setContentText("İstek göndermek için aşağı çekin.")
                        .setTicker("Yolcu Var!")
                        .addAction(R.mipmap.accepttwo, "Gönder", pIntentBilgi)
                        .addAction(R.mipmap.canceltwo, "Reddet", pIntent)
                        .setAutoCancel(true)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setLights(Color.GREEN, 2000, 3000);
                mNotificationManager.notify(1001, nBuilder.build());
            } else {
                Toast.makeText(getApplicationContext(),
                        "You need a higher version", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
