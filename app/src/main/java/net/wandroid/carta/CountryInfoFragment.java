package net.wandroid.carta;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.wandroid.carta.data.Country;

import java.util.HashMap;
import java.util.Map;

public class CountryInfoFragment extends Fragment {


    private TextView mNameTextView;
    private TextView mCapitalTextView;
    private TextView mRegionTextView;

    private Map<String, Country> mCountries = new HashMap<>();

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            handleSearch(query.toLowerCase().trim());
        }
    };


    public CountryInfoFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter filter = new IntentFilter(Intent.ACTION_SEARCH);
        manager.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_country_info, container, false);

        mNameTextView = (TextView) view.findViewById(R.id.country_name);
        mCapitalTextView = (TextView) view.findViewById(R.id.country_capital);
        mRegionTextView = (TextView) view.findViewById(R.id.country_region);
        return view;
    }

    private void handleSearch(String query) {

        Intent intent = new Intent(getContext(), DownloadCountriesService.class);
        intent.putExtra(DownloadCountriesService.KEY_COUNTRY_NAME, query);
        Activity activity = getActivity();
        if (activity != null) {
            activity.startService(intent);
        }
    }

    public void updateText(Country country) {
        if (country != null) {
            mNameTextView.setText(country.name);
            mCapitalTextView.setText(country.capital);
            mRegionTextView.setText(country.region);
        } else {
            mNameTextView.setText("No such country");
            mCapitalTextView.setText("");
            mRegionTextView.setText("");
        }
    }


}
