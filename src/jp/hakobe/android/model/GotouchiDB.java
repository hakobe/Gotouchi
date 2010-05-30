package jp.hakobe.android.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import jp.hakobe.android.util.Geohash;
import jp.hakobe.android.util.JSON;

public class GotouchiDB {
    final String TAG = "GotouchiDB";

    private ArrayList<String> geohashes = new ArrayList<String>();
    private JSONObject geohashMap = null;
    
    public GotouchiDB() {
    }
    
    public void loadIndex(InputStream indexIs) {
        JSONObject indexJson;
        try {
            indexJson = JSON.parseJSON(indexIs);
            JSONArray geohashesJson = indexJson.getJSONArray("geohashes");
            for (int i = 0; i < geohashesJson.length(); i++) {
                this.geohashes.add(geohashesJson.getString(i));
            }
            this.geohashMap = indexJson.getJSONObject("geohash_map");
        }
        catch (Exception e) {
            Log.v(TAG, e.getStackTrace().toString());
        }
    }
    
    private double distance(double x1, double y1, double x2, double y2) {
        double dx = Math.abs(x2 - x1);
        double dy = Math.abs(y2 - y1);        
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }
    
    private double calcluateDistance(String baseGeohash, String geohash) {
        double baselatlon[][] = Geohash.decode(baseGeohash);
        double latlon[][] = Geohash.decode(geohash);
        
        return distance(baselatlon[0][0], baselatlon[1][0], latlon[0][0], latlon[1][0]);
    }
    
    public ArrayList<Gotouchi> searchByLatLon(double lat, double lon) {
        int shortGeohashLen = 4;
        String baseGeohash = Geohash.encode(lat, lon);
        String baseGeohashShort = baseGeohash.substring(0,shortGeohashLen);
        String baseGeohashMoreShort = baseGeohash.substring(0,shortGeohashLen - 2);
        String[] baseGeohashShort9 = Geohash.geohash9(baseGeohashShort);
        
        Log.v(TAG, "base: "+ Geohash.encode(lat, lon));
        Log.v(TAG, "base: "+ baseGeohashShort);
        ArrayList<Gotouchi> results = new ArrayList<Gotouchi>();
        final HashMap<String, Double> matchLens = new HashMap<String, Double>();
        for (String geohash : this.geohashes) {
            String shortGeohashMore = geohash.substring(0,shortGeohashLen - 2);
            if (!shortGeohashMore.equals(baseGeohashMoreShort)) {
                continue;
            }
            String shortGeohash = geohash.substring(0,shortGeohashLen);
            for (String g9: baseGeohashShort9) {
                if (g9.equals(shortGeohash)) {
                    results.addAll(this.createGotouchis(geohash));
                    matchLens.put(geohash, calcluateDistance(baseGeohash, geohash));
                    break;
                }
            }
        }
        Comparator<Gotouchi> comparator = new Comparator<Gotouchi>() {
            public int compare(Gotouchi g1, Gotouchi g2) {
                if (matchLens.get(g1.getGeohash()) == matchLens.get(g2.getGeohash())) {
                    return 0;
                }
                else if (matchLens.get(g1.getGeohash()) > matchLens.get(g2.getGeohash())) {
                    return 1;
                }
                else if (matchLens.get(g1.getGeohash()) < matchLens.get(g2.getGeohash())) {
                    return -1;
                }
                return 0;
            }
        };
        Collections.sort(results, comparator);
        return results;
    }
    private ArrayList<Gotouchi> createGotouchis(String geohash) {
        ArrayList<Gotouchi> results = new ArrayList<Gotouchi>();
        
        try {
            JSONArray gotouchiJsons = this.geohashMap.getJSONArray(geohash);
            for (int i = 0; i < gotouchiJsons.length(); i++) {
                JSONObject gotouchiJson = gotouchiJsons.getJSONObject(i);
                
                String name = gotouchiJson.getString("name");
                String region = gotouchiJson.getString("region");

                Gotouchi gotouchi = new Gotouchi();
                gotouchi.setGeohash(geohash);
                gotouchi.setName(name);
                gotouchi.setRegion(region);
                results.add(gotouchi);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        
        return results;
    }
}
