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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.wandroid.carta.data.Country;
import net.wandroid.carta.net.Async;

public class CountryInfoFragment extends Fragment {


    public static final String KEY_COUNTRY = "KEY_COUNTRY";
    public static final String KEY_HAS_COUNTRY = "KEY_HAS_COUNTRY";
    private TextView mNameTextView;
    private TextView mCapitalTextView;
    private TextView mRegionTextView;
    private ImageView mFlagImageView;

    private Country mCountry;

    private boolean mInvalidCountry;

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
    public void onSaveInstanceState(Bundle outState) {
        if (mCountry != null) {
            outState.putSerializable(KEY_COUNTRY, mCountry);
        }
        outState.putBoolean(KEY_HAS_COUNTRY, mInvalidCountry);

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_country_info, container, false);

        mNameTextView = (TextView) view.findViewById(R.id.country_name);
        mCapitalTextView = (TextView) view.findViewById(R.id.country_capital);
        mRegionTextView = (TextView) view.findViewById(R.id.country_region);
        mFlagImageView = (ImageView) view.findViewById(R.id.flag_view);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_COUNTRY)) {
                updateText((Country) savedInstanceState.getSerializable(KEY_COUNTRY));
            }
            mInvalidCountry = savedInstanceState.getBoolean(KEY_HAS_COUNTRY, false);
            if (mInvalidCountry) {
                noCountry();
            } else if (mCountry != null) {
                updateText(mCountry);
            }
        }
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

    @Async
    public void updateText(Country country) {
        mCountry = country;
        mInvalidCountry = false;
        mNameTextView.setText(country.name);
        mCapitalTextView.setText(country.capital);
        mRegionTextView.setText(country.region);
        Activity activity = getActivity();
        if (activity != null) {
            String imageName = country.alpha2Code.toLowerCase() + ".png";
            Picasso.with(activity).load("https://raw.githubusercontent.com/hjnilsson/country-flags/master/png250px/" + imageName).into(mFlagImageView);
        }
    }

    public void noCountry() {
        mCountry = null;
        mInvalidCountry = true;
        mNameTextView.setText("No such country");
        mCapitalTextView.setText("");
        mRegionTextView.setText("");
        mFlagImageView.setImageResource(R.drawable.error_ball);
    }


}
