package net.wandroid.carta;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;

import com.google.gson.Gson;

import net.wandroid.carta.data.Country;
import net.wandroid.carta.net.GetCountries;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Required to be run in debug flavour
 * These test should not be packed with the release apk
 */
public class RestDataTests extends AndroidTestCase {
    public static final int INDEX_BOTSWANA = 0;
    public static final String TEST_JSON_SW_JSON = "test/json/sw.json";
    public static final String NAME_SW = "sw";
    private Gson mGson;
    private AssetManager mAssetManager;
    private Country[] mCountries;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mGson = new Gson();
        mAssetManager = getContext().getAssets();
        InputStreamReader reader = new InputStreamReader(mAssetManager.open(TEST_JSON_SW_JSON));
        mCountries = mGson.fromJson(reader, Country[].class);
        reader.close();
    }

    public void test_json_to_java() throws IOException {
        assertEquals(4, mCountries.length);
    }

    public void test_country_members() {
        assertEquals("Botswana", mCountries[INDEX_BOTSWANA].name);
        assertEquals("Gaborone", mCountries[INDEX_BOTSWANA].capital);
        assertEquals("Africa", mCountries[INDEX_BOTSWANA].region);
        assertEquals("BW", mCountries[INDEX_BOTSWANA].alpha2Code);
    }

    public void test_get() throws IOException {
        GetCountries get = new GetCountries();
        Country[] countries = get.getCountries(NAME_SW);
        assertEquals("Botswana", countries[INDEX_BOTSWANA].name);
    }

}
