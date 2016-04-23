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

    /**
     * Column with the country name in the adaptor cursor
     */
    public static final String COL_COUNTRY_NAME = "countryName";
    /**
     * Min length of search string that should trigger search suggestions.
     * This is used to avoid getting large data as short strings matches many countries.
     */
    public static final int MIN_SEARCH_LENGTH = 3;
    /**
     * Loader ID
     */
    public static final int LOADER_ID = 0;
    /**
     * Argument for loader. Will contain the search string
     */
    private static final String ARG_QUERY = "ARG_QUERY";
    /**
     * Argument for loader. True if the result should be used. false if the result should be search suggestions
     */
    private static final String ARG_UPDATE_FRAGMENT = "ARG_UPDATE_FRAGMENT";

    private LocalBroadcastManager mLocalBroadcastManager;

    /**
     * Result countries after a search. Used with the search suggestions
     */
    private List<Country> mCountries;
    /**
     * Adapter for the search suggestions
     */
    private CursorAdapter mSuggestionAdapter;
    /**
     * The search view
     */
    private SearchView mSearchView;


    /**
     * Updates the Fragment with country info.
     * This method might be called async.
     *
     * @param country The country to display, or null if there is no matching country
     */
    @Async
    private void updateCountryInfoFragment(Country country) {
        //Note that it exist a condition where the fragment might not be updated because of the async.
        // If the loader returns after the fragment's onStop the broadcast will not be received.
        // If this condition is important then there are some suggestions for improvement:
        // -make the intent sticky (resource heavy)
        // -create a full async handler with a content provider to store the data
        // -make the fragment do a callback once it is updated and have the activity resend the data until the callback is received
        Intent intent = new Intent(CountryInfoFragment.ACTION_UPDATE);
        intent.putExtra(CountryInfoFragment.KEY_COUNTRY, country);
        mLocalBroadcastManager.sendBroadcast(intent);

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

        // Register the search view. As there are no other activities in this project this is not needed but should be added for completeness
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
                //Clear the suggestions
                mCountries = new ArrayList<>();
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
                //Search for suggestions. Could be optimized by doing postDelayed with a threshold to avoid spamming.
                startSearch(newText.trim(), false);
                return false;
            }
        });


        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Start to search for countries. Will clear old suggestions
     *
     * @param query          the string to search for. If less than MIN_SEARCH_LENGTH then the suggestions
     *                       will be removed and no search is performed. This is to avoid searching for many countries.
     * @param updateFragment true if the result is a search, false if it is for suggestions
     */
    private void startSearch(String query, boolean updateFragment) {
        mSuggestionAdapter.changeCursor(null); // clear old suggestions
        if (query.length() >= MIN_SEARCH_LENGTH) {
            Bundle args = new Bundle();
            args.putString(ARG_QUERY, query);
            args.putBoolean(ARG_UPDATE_FRAGMENT, updateFragment);
            getLoaderManager().restartLoader(LOADER_ID, args, MainActivity.this).forceLoad();
        }
    }

    /**
     * This will be called when the user presses the search button since the activity is singleTop.
     *
     * @param intent the intent
     */
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
        return new CountryLoader(getApplicationContext(), args.getString(ARG_QUERY, null), args.getBoolean(ARG_UPDATE_FRAGMENT, false));
    }

    @Async
    @Override
    public void onLoadFinished(Loader<List<Country>> loader, List<Country> data) {

        MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID, COL_COUNTRY_NAME});

        //Copy the content, since we don't have control of the data reference (order might rearrange)
        mCountries = new ArrayList<>(data);

        CountryLoader countryLoader = (CountryLoader) loader;
        if (countryLoader.mUpdateFragment) { //Loader called in update mode. Update fragment with a country
            if (data.isEmpty() || data.size() > 1) {
                //No country or multiple results. This means there's no country with such name.
                updateCountryInfoFragment(null);
            } else {
                //is it a match? 'Swe' will return only one result 'Sweden', but 'Swe' is not a country
                Country country = data.get(0);
                if (country.name.equalsIgnoreCase(countryLoader.mCountryName)) {
                    updateCountryInfoFragment(country);
                } else {
                    updateCountryInfoFragment(null);
                }
            }
        } else { // Loader called for finding suggestions
            //Update the adapter
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

    /**
     * Loader class. Will load countries in the background. useful for auto complete and suggestions
     */
    public static class CountryLoader extends AsyncTaskLoader<List<Country>> {

        /**
         * part of the name of the country to search for
         */
        public final String mCountryName;
        /**
         * The mode the load is performed in. If true then it's a single country search.
         * If false then it should find multiple suggestions
         */
        public final boolean mUpdateFragment;

        /**
         * Constructor
         *
         * @param context        the context
         * @param countryName    part of the country name to search for
         * @param updateFragment search mode. If true should try to find exact match, if false then multiple suggestions
         */
        public CountryLoader(Context context, String countryName, boolean updateFragment) {
            super(context);
            mCountryName = countryName;
            mUpdateFragment = updateFragment;
        }

        @Override
        public List<Country> loadInBackground() {
            if (mCountryName == null || mCountryName.isEmpty()) {
                //null or no name should not be searched
                return new ArrayList<>();
            }
            GetCountries get = new GetCountries();
            try {
                Country[] country = get.getCountries(mCountryName);
                return Arrays.asList(country);
            } catch (IOException e) {
                e.printStackTrace();
                //Error. Return empty list
                return new ArrayList<>();
            }
        }
    }

}
