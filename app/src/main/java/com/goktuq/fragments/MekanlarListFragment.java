package com.goktuq.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.goktuq.MekanlarListAdaptor;
import com.goktuq.youtubedemo.R;

/**
 * Created by Casper on 06.05.2016.
 */
public class MekanlarListFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mekanlar, container,false);
        String[] mekanlar = {"Ev","İş","Okul"};
        ListAdapter adaptor = new MekanlarListAdaptor(this.getActivity(),mekanlar);
        ListView mekanListView = (ListView)view.findViewById(R.id.mekanList);
        mekanListView.setAdapter(adaptor);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
