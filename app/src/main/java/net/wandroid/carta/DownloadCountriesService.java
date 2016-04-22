package net.wandroid.carta;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.wandroid.carta.data.Country;
import net.wandroid.carta.net.GetCountries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by zabbat on 2016-04-22.
 */
public class DownloadCountriesService extends IntentService {

    public static final String KEY_COUNTRY_NAME = "KEY_COUNTRY_NAME";
    public static final String NET_WANDROID_CARTA_DOWNLOAD_RESULT = "net.wandroid.carta.DOWNLOAD_RESULT";
    public static final String KEY_COUNTRIES = "KEY_COUNTRIES";

    public DownloadCountriesService() {
        super(DownloadCountriesService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("EGG", "service");
        String name = intent.getStringExtra(KEY_COUNTRY_NAME);
        GetCountries get = new GetCountries();
        Country[] countries = new Country[0];
        try {
            countries = get.getCountries(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent resultIntent = new Intent(NET_WANDROID_CARTA_DOWNLOAD_RESULT);
        resultIntent.putExtra(KEY_COUNTRIES, new ArrayList<>(Arrays.asList(countries)));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resultIntent);

    }
}
