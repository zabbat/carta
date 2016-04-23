package net.wandroid.carta;

import android.app.Activity;
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

/**
 * Fragment that displays info and flag of a country
 */
public class CountryInfoFragment extends Fragment {

    public static final String KEY_COUNTRY = "KEY_COUNTRY";
    public static final String KEY_HAS_COUNTRY = "KEY_HAS_COUNTRY";
    public static final String HTTPS_RAW_GITHUBUSERCONTENT_COM_HJNILSSON_COUNTRY_FLAGS_MASTER_PNG250PX = "https://raw.githubusercontent.com/hjnilsson/country-flags/master/png250px/";
    public static final String FLAG_IMAGE_ENDING = ".png";
    /**
     * Intent action for update the country
     */
    public static final String ACTION_UPDATE = "net.wandroid.carta.UPDATE";
    private TextView mNameTextView;
    private TextView mCapitalTextView;
    private TextView mRegionTextView;
    private ImageView mFlagImageView;
    private BroadcastReceiver mUpdateBroadcastReceiver;

    /**
     * The country to display
     */
    private Country mCountry;

    /**
     * True if there is no valid country to show, otherwise false.
     * mCountry will be ignored if set to true
     */
    private boolean mInvalidCountry;

    public CountryInfoFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        mUpdateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Country country = (Country) intent.getSerializableExtra(KEY_COUNTRY);
                if (country != null) {
                    updateText(country);
                } else {
                    noCountry();
                }
            }
        };
        IntentFilter filter = new IntentFilter(ACTION_UPDATE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUpdateBroadcastReceiver, filter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUpdateBroadcastReceiver);
        mUpdateBroadcastReceiver = null;
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

    /**
     * Updates the info of the country and displays the flag.
     *
     * @param country
     */
    @Async
    public void updateText(Country country) {
        mCountry = country;
        mInvalidCountry = false;
        mNameTextView.setText(country.name);
        mCapitalTextView.setText(getString(R.string.capital_txt, country.capital));
        mRegionTextView.setText(getString(R.string.region_txt, country.region));
        Activity activity = getActivity();
        if (activity != null) { //Method might be called async, make sure there is an activity
            String imageName = country.alpha2Code.toLowerCase() + FLAG_IMAGE_ENDING;
            //Use picasso library to handle downloading, caching and errors
            Picasso.with(activity).load(HTTPS_RAW_GITHUBUSERCONTENT_COM_HJNILSSON_COUNTRY_FLAGS_MASTER_PNG250PX + imageName).error(R.drawable.error_ball).into(mFlagImageView);
        }
    }

    /**
     * hides info of the country and displays an error message and shows an error image
     */
    public void noCountry() {
        mCountry = null;
        mInvalidCountry = true;
        mNameTextView.setText(R.string.no_country);
        mCapitalTextView.setText("");
        mRegionTextView.setText("");
        mFlagImageView.setImageResource(R.drawable.error_ball);
    }


}
