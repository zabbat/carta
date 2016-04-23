package net.wandroid.carta.data;

import java.io.Serializable;

/**
 * class representing the java object of country json.
 * see: https://github.com/fayder/restcountries/wiki/API-1.x.x
 * only used members are implemented
 *
 * For performance this should implement parcable instead of serializable
 */
public class Country implements Serializable{
    public String name;
    public String capital;
    public String region;
    public String alpha2Code;
}
