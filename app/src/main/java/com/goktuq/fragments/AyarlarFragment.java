package com.goktuq.fragments;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.goktuq.KonumServisi;
import com.goktuq.youtubedemo.R;

/**
 * Created by Casper on 23.04.2016.
 */
public class AyarlarFragment extends Fragment {

    Switch mySwitch;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ayarlar, container, false);
        mySwitch = (Switch)view.findViewById(R.id.arkaCalis);
        if (servisCalisiyorMu()) {
            mySwitch.setChecked(true);
        } else {
            mySwitch.setChecked(false);
        }
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    getActivity().startService(new Intent(getActivity(), KonumServisi.class));
                } else {
                    getActivity().stopService(new Intent(getActivity(), KonumServisi.class));
                }
            }
        });
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private boolean servisCalisiyorMu() {
        ActivityManager servisYoneticisi = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo servis : servisYoneticisi
                .getRunningServices(Integer.MAX_VALUE)) {
            if (getActivity().getPackageName().equals(
                    servis.service.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}
