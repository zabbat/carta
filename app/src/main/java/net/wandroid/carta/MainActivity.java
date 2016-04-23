package net.wandroid.carta;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import net.wandroid.carta.data.Country;
import net.wandroid.carta.net.Async;
import net.wandroid.carta.net.GetCountries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Country>> {

    public static final String KEY_COUNTRIES = "KEY_COUNTRIES";
    public static final String KEY_QUERY = "KEY_QUERY";
    private LocalBroadcastManager mLocalBroadcastManager;
    private List<Country> mCountries;
    private CursorAdapter mSuggestionAdapter;
    private SearchView mSearchView;

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

        mSuggestionAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                new String[]{"countryName"}, new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        getLoaderManager().initLoader(0, null, this);
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
        mSearchView =
                (SearchView) findViewById(R.id.search_view);
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSuggestionsAdapter(mSuggestionAdapter);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.length() > 1) {
                    Bundle args = new Bundle();
                    args.putString(KEY_QUERY, newText);
                    getLoaderManager().restartLoader(0, args, MainActivity.this).forceLoad();
                } else {
                    mSuggestionAdapter.changeCursor(null);
                }

                return false;
            }
        });

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

    @Override
    public Loader<List<Country>> onCreateLoader(int id, Bundle args) {
        if (args == null) {
            args = new Bundle();
        }
        return new CountryLoader(getApplicationContext(), args.getString(KEY_QUERY, null));
    }

    @Async
    @Override
    public void onLoadFinished(Loader<List<Country>> loader, List<Country> data) {
        MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID, "countryName"});

        for (int i = 0; i < data.size(); i++) {
            c.addRow(new Object[]{i, data.get(i).name});
        }
        mSuggestionAdapter.changeCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<List<Country>> loader) {
        mSuggestionAdapter.changeCursor(null);
    }


    public static class CountryLoader extends AsyncTaskLoader<List<Country>> {

        private final String mUrl;

        public CountryLoader(Context context, String url) {
            super(context);
            mUrl = url;
        }

        @Override
        public List<Country> loadInBackground() {
            if (mUrl == null || mUrl.isEmpty()) {
                return new ArrayList<>();
            }
            GetCountries get = new GetCountries();
            try {
                Country[] country = get.getCountries(mUrl);
                return Arrays.asList(country);
            } catch (IOException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

}
