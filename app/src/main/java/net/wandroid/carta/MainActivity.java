package net.wandroid.carta;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import net.wandroid.carta.data.Country;
import net.wandroid.carta.net.Async;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_COUNTRIES = "KEY_COUNTRIES";
    private LocalBroadcastManager mLocalBroadcastManager;
    private List<Country> mCountries;

    private BroadcastReceiver mDownloadCompleteBroadcastReceiver = new BroadcastReceiver() {
        @Async
        @Override
        public void onReceive(Context context, Intent intent) {
            //Download complete. Try to update fragment or save for later.
            List<Country> countries = (ArrayList<Country>) intent.getSerializableExtra(DownloadCountriesService.KEY_COUNTRIES);
            updateCountryInfoFragment(countries);
        }
    };


    @Async
    private void updateCountryInfoFragment(List<Country> countries) {


        CountryInfoFragment fragment = (CountryInfoFragment) getSupportFragmentManager().findFragmentById(R.id.country_info_fragment);

        //Is fragment valid or has it detached?
        if (fragment != null) {
            if (countries.size() > 0) {
                fragment.updateText(countries.get(0));
            } else {
                fragment.noCountry();
            }
            mCountries = null;
        } else {
            //Fragment is not valid (after onStop), save the downloaded countries
            mCountries = countries;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mCountries != null) {
            outState.putSerializable(KEY_COUNTRIES, new ArrayList<>(mCountries));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(DownloadCountriesService.NET_WANDROID_CARTA_DOWNLOAD_RESULT);
        mLocalBroadcastManager.registerReceiver(mDownloadCompleteBroadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        mLocalBroadcastManager.unregisterReceiver(mDownloadCompleteBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_COUNTRIES)) {
                mCountries = (List<Country>) savedInstanceState.getSerializable(KEY_COUNTRIES);
                updateCountryInfoFragment(mCountries);
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mLocalBroadcastManager.sendBroadcast(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) findViewById(R.id.search_view);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
