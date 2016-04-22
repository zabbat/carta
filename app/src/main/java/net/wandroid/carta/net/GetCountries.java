package net.wandroid.carta.net;

import com.google.gson.Gson;

import net.wandroid.carta.data.Country;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zabbat on 2016-04-22.
 */
public class GetCountries {

    public static final String GET = "GET";
    public static final int TIMEOUT_MILLIS = 30 * 1000;
    public static final String HTTPS_RESTCOUNTRIES_EU_REST_V1_NAME = "https://restcountries.eu/rest/v1/name/";

    public Country[] getCountries(String name) throws IOException {
        String url = HTTPS_RESTCOUNTRIES_EU_REST_V1_NAME + name;
        return getData(url, Country[].class);
    }

    private <T> T getData(String address, Class<T> dataType) throws IOException {
        URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(GET);
        connection.setDoInput(true);
        connection.setConnectTimeout(TIMEOUT_MILLIS);


        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            Gson gson = new Gson();
            T data = gson.fromJson(reader, dataType);
            reader.close();
            return data;
        }

        throw new IOException("Could not read" + dataType.getCanonicalName() + "from url:" + url);
    }

}
