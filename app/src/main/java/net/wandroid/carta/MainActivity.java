package net.wandroid.carta;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
    public static final String COL_COUNTRY_NAME = "countryName";
    public static final int MIN_SEARCH_LENGTH = 2;
    public static final int LOADER_ID = 0;
    public static final String KEY_UPDATE_FRAGMENT = "KEY_UPDATE_FRAGMENT";
    private LocalBroadcastManager mLocalBroadcastManager;
    private List<Country> mCountries;
    private CursorAdapter mSuggestionAdapter;
    private SearchView mSearchView;
    

    @Async
    private void updateCountryInfoFragment(Country country) {
        Log.d("EGG", "updateCountryInfoFragment " + (country == null ? "None" : country.name));
        CountryInfoFragment fragment = (CountryInfoFragment) getSupportFragmentManager().findFragmentById(R.id.country_info_fragment);

        //Is fragment valid or has it detached?
        if (fragment != null) {
            if (country != null) {
                fragment.updateText(country);
            } else {
                fragment.noCountry();
            }
            mCountries = null;
        } else {
            //Fragment is not valid (after onStop), save the downloaded countries
            //TODO: save country
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);


        mSuggestionAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                new String[]{COL_COUNTRY_NAME}, new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView =
                (SearchView) findViewById(R.id.search_view);
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSuggestionsAdapter(mSuggestionAdapter);

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Country country = mCountries.get(position);
                mSearchView.setQuery(country.name, false);
                mCountries = new ArrayList<>();
                mCountries.add(country);
                mSuggestionAdapter.changeCursor(null);
                updateCountryInfoFragment(country);
                return false;
            }
        });


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                startSearch(newText.trim(), false);
                return false;
            }
        });


        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void startSearch(String query, boolean updateFragment) {
        if (query.length() > MIN_SEARCH_LENGTH) {
            mSuggestionAdapter.changeCursor(null);
            Bundle args = new Bundle();
            args.putString(KEY_QUERY, query);
            args.putBoolean(KEY_UPDATE_FRAGMENT, updateFragment);
            getLoaderManager().restartLoader(LOADER_ID, args, MainActivity.this).forceLoad();
        } else {
            mSuggestionAdapter.changeCursor(null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            startSearch(intent.getStringExtra(SearchManager.QUERY).trim(), true);
        }

    }

    @Override
    public Loader<List<Country>> onCreateLoader(int id, Bundle args) {
        if (args == null) {
            args = new Bundle();
        }
        return new CountryLoader(getApplicationContext(), args.getString(KEY_QUERY, null), args.getBoolean(KEY_UPDATE_FRAGMENT, false));
    }

    @Async
    @Override
    public void onLoadFinished(Loader<List<Country>> loader, List<Country> data) {
        Log.d("EGG", "onLoadFinished. size:" + data.size());

        MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID, COL_COUNTRY_NAME});

        //Copy the content, since we don't have control of the data reference (order might rearange)
        mCountries = new ArrayList<>(data);
        CountryLoader countryLoader = (CountryLoader) loader;
        boolean update = countryLoader.mUpdateFragment;
        String query = countryLoader.mCountryName;
        if (update) {
            if (data.isEmpty() || data.size() > 1) {
                //No country or multiple results. This means there's no country with such name.
                updateCountryInfoFragment(null);
            } else {
                //is it a direct match? 'Swe' will return only one result 'Sweden', but 'Swe' is not a country
                Country country = data.get(0);
                if (country.name.equalsIgnoreCase(query)) {
                    updateCountryInfoFragment(data.get(0));
                } else {
                    updateCountryInfoFragment(null);
                }
            }
        } else {

            for (int i = 0; i < mCountries.size(); i++) {
                c.addRow(new Object[]{i, mCountries.get(i).name});
            }
            mSuggestionAdapter.changeCursor(c);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Country>> loader) {
        mSuggestionAdapter.changeCursor(null);
    }


    public static class CountryLoader extends AsyncTaskLoader<List<Country>> {

        public final String mCountryName;
        public final boolean mUpdateFragment;

        public CountryLoader(Context context, String countryName, boolean updateFragment) {
            super(context);
            mCountryName = countryName;
            mUpdateFragment = updateFragment;
        }

        @Override
        public List<Country> loadInBackground() {
            if (mCountryName == null || mCountryName.isEmpty()) {
                return new ArrayList<>();
            }
            GetCountries get = new GetCountries();
            try {
                Country[] country = get.getCountries(mCountryName);
                return Arrays.asList(country);
            } catch (IOException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

}
