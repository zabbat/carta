package net.wandroid.carta;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import net.wandroid.carta.data.Country;

import java.io.IOException;
import java.io.InputStreamReader;
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

        /**
         * Load debug flavour json data until we have implemented real rest fetching
         */
        new AsyncTask<Void, Void, Country[]>() {
            @Override
            protected Country[] doInBackground(Void... params) {
                Country[] countries = new Country[0];
                Activity activity = getActivity();
                if (activity != null) {
                    AssetManager assetManager = activity.getAssets();
                    try {
                        InputStreamReader reader = new InputStreamReader(assetManager.open("test/json/sw.json"));
                        Gson gson = new Gson();
                        countries = gson.fromJson(reader, Country[].class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    cancel(false);
                }
                return countries;
            }

            @Override
            protected void onPostExecute(Country[] countries) {
                super.onPostExecute(countries);
                if (countries.length == 0) {
                    mNameTextView.setText("No such country :(");
                } else {
                    for (Country c : countries) {
                        mCountries.put(c.name.toLowerCase(), c);
                    }
                }
            }
        }.execute();

        return view;
    }

    private void handleSearch(String query) {
        if (mCountries.containsKey(query)) {
            Country country = mCountries.get(query);
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
