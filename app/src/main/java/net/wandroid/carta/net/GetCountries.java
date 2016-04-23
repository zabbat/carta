package net.wandroid.carta.net;

import com.google.gson.Gson;

import net.wandroid.carta.data.Country;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Class for a Rest GET for countries
 */
public class GetCountries {

    public static final String GET = "GET";
    public static final int TIMEOUT_MILLIS = 30 * 1000;
    public static final String HTTPS_RESTCOUNTRIES_EU_REST_V1_NAME = "https://restcountries.eu/rest/v1/name/";
    public static final String UTF_8 = "UTF-8";
    public static final String URLENCODE_SPACE = "+";
    public static final String REST_SPACE = "%20";

    /**
     * Returns an array of matching countries
     * This method use network and should ot be called on the main thread
     *
     * @param name part of the name of countries
     * @return array of matching countries.
     * @throws IOException
     */
    public Country[] getCountries(String name) throws IOException {
        String url = HTTPS_RESTCOUNTRIES_EU_REST_V1_NAME + URLEncoder.encode(name, UTF_8).replace(URLENCODE_SPACE, REST_SPACE);

        return getData(url, Country[].class);
    }

    /**
     * A generic json to data method. Will expect the result to be json text and convert it to a java class.
     * This method use network and should not be called on the main thread
     *
     * @param address  url to where the data is
     * @param dataType the java data type
     * @param <T>      the type to return
     * @return a java object of the json response.
     * @throws IOException
     */
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
