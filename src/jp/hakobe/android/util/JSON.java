package jp.hakobe.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

public class JSON {
    public JSON() {
    }
    public static JSONObject parseJSON(InputStream is) throws JSONException, IOException {
        final StringBuilder body = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            body.append(line).append("\n");
        }
        br.close();

        return new JSONObject(body.toString());
    }
}
