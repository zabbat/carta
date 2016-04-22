package net.wandroid.carta;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import net.wandroid.carta.data.Country;

import java.io.IOException;
import java.io.InputStreamReader;

public class CountryInfoFragment extends Fragment {

    private TextView mNameTextView;
    private TextView mCapitalTextView;
    private TextView mRegionTextView;

    public CountryInfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

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
                    mNameTextView.setText(countries[0].name);
                    mCapitalTextView.setText(countries[0].capital);
                    mRegionTextView.setText(countries[0].region);
                }
            }
        }.execute();

        return view;
    }
}
