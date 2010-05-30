package jp.hakobe.android.util;

//Geohash.java
//Geohash library for Java
//ported from David Troy's Geohash library for Javascript
//- http://github.com/davetroy/geohash-js/tree/master
//(c) 2008 David Troy
//(c) 2008 Tom Carden
//Distributed under the MIT License

public class Geohash {

    public static int BITS[] = { 16, 8, 4, 2, 1 };

    public static String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    public static int RIGHT = 0;
    public static int LEFT = 1;
    public static int TOP = 2;
    public static int BOTTOM = 3;

    public static int EVEN = 0;
    public static int ODD = 1;

    public static String[][] NEIGHBORS;
    public static String[][] BORDERS;

    static {
        NEIGHBORS = new String[4][2];
        BORDERS = new String[4][2];

        NEIGHBORS[BOTTOM][EVEN] = "bc01fg45238967deuvhjyznpkmstqrwx";
        NEIGHBORS[TOP][EVEN] = "238967debc01fg45kmstqrwxuvhjyznp";
        NEIGHBORS[LEFT][EVEN] = "p0r21436x8zb9dcf5h7kjnmqesgutwvy";
        NEIGHBORS[RIGHT][EVEN] = "14365h7k9dcfesgujnmqp0r2twvyx8zb";

        BORDERS[BOTTOM][EVEN] = "bcfguvyz";
        BORDERS[TOP][EVEN] = "0145hjnp";
        BORDERS[LEFT][EVEN] = "prxz";
        BORDERS[RIGHT][EVEN] = "028b";

        NEIGHBORS[BOTTOM][ODD] = NEIGHBORS[LEFT][EVEN];
        NEIGHBORS[TOP][ODD] = NEIGHBORS[RIGHT][EVEN];
        NEIGHBORS[LEFT][ODD] = NEIGHBORS[BOTTOM][EVEN];
        NEIGHBORS[RIGHT][ODD] = NEIGHBORS[TOP][EVEN];

        BORDERS[BOTTOM][ODD] = BORDERS[LEFT][EVEN];
        BORDERS[TOP][ODD] = BORDERS[RIGHT][EVEN];
        BORDERS[LEFT][ODD] = BORDERS[BOTTOM][EVEN];
        BORDERS[RIGHT][ODD] = BORDERS[TOP][EVEN];
    }

    private static void refine_interval(double[] interval, int cd, int mask) {
        if ((cd & mask) > 0) {
            interval[0] = (interval[0] + interval[1]) / 2.0;
        }
        else {
            interval[1] = (interval[0] + interval[1]) / 2.0;
        }
    }

    public static String calculateAdjacent(String srcHash, int dir) {
        srcHash = srcHash.toLowerCase();
        char lastChr = srcHash.charAt(srcHash.length() - 1);
        int type = (srcHash.length() % 2) == 1 ? ODD : EVEN;
        String base = srcHash.substring(0, srcHash.length() - 1);
        if (BORDERS[dir][type].indexOf(lastChr) != -1) {
            base = calculateAdjacent(base, dir);
        }
        return base + BASE32.charAt(NEIGHBORS[dir][type].indexOf(lastChr));
    }

    public static double[][] decode(String geohash) {
        boolean is_even = true;
        double[] lat = new double[3];
        double[] lon = new double[3];

        lat[0] = -90.0;
        lat[1] = 90.0;
        lon[0] = -180.0;
        lon[1] = 180.0;
        double lat_err = 90.0;
        double lon_err = 180.0;

        for (int i = 0; i < geohash.length(); i++) {
            char c = geohash.charAt(i);
            int cd = BASE32.indexOf(c);
            for (int j = 0; j < BITS.length; j++) {
                int mask = BITS[j];
                if (is_even) {
                    lon_err /= 2.0;
                    refine_interval(lon, cd, mask);
                }
                else {
                    lat_err /= 2.0;
                    refine_interval(lat, cd, mask);
                }
                is_even = !is_even;
            }
        }
        lat[2] = (lat[0] + lat[1]) / 2.0;
        lon[2] = (lon[0] + lon[1]) / 2.0;

        return new double[][] { lat, lon };
    }

    public static String encode(double latitude, double longitude) {
        boolean is_even = true;
        double lat[] = new double[3];
        double lon[] = new double[3];
        int bit = 0;
        int ch = 0;
        int precision = 12;
        String geohash = "";

        lat[0] = -90.0;
        lat[1] = 90.0;
        lon[0] = -180.0;
        lon[1] = 180.0;

        while (geohash.length() < precision) {
            if (is_even) {
                double mid = (lon[0] + lon[1]) / 2.0;
                if (longitude > mid) {
                    ch |= BITS[bit];
                    lon[0] = mid;
                }
                else {
                    lon[1] = mid;
                }
            }
            else {
                double mid = (lat[0] + lat[1]) / 2.0;
                if (latitude > mid) {
                    ch |= BITS[bit];
                    lat[0] = mid;
                }
                else {
                    lat[1] = mid;
                }
            }
            is_even = !is_even;
            if (bit < 4) {
                bit++;
            }
            else {
                geohash += BASE32.charAt(ch);
                bit = 0;
                ch = 0;
            }
        }
        return geohash;
    }

    public static String[] geohash9(String geohash) {
        
        String[] results = new String[9];
        results[4] = geohash;
        results[1] = calculateAdjacent(results[4], TOP);
        results[5] = calculateAdjacent(results[4], RIGHT);
        results[7] = calculateAdjacent(results[4], BOTTOM);
        results[3] = calculateAdjacent(results[4], LEFT);
        results[0] = calculateAdjacent(results[1], LEFT);
        results[2] = calculateAdjacent(results[1], RIGHT);
        results[6] = calculateAdjacent(results[7], LEFT);
        results[8] = calculateAdjacent(results[7], RIGHT);
        
        return results;
    }

}