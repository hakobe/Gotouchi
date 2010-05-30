package jp.hakobe.android;

import java.util.ArrayList;

import jp.hakobe.android.model.Gotouchi;
import jp.hakobe.android.model.GotouchiDB;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GotouchiLauncher extends Activity {
    final static String TAG = "GotouchiLauncher"; 
    private GotouchiDB gotouchiDB;
    
    private String mProvider;
    private LocationManager mLocationManager;
    private GotouchiLocationListener mGotouchiLocationListener;
    private ProgressDialog mProgressDialog = null;
    
    final int MENU_RELOAD_LOCATION_ID = (Menu.FIRST + 1);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.gotouchiDB = new GotouchiDB();
        this.gotouchiDB.loadIndex(getResources().openRawResource(R.raw.gotouchi_index));
        //searchGotouches();
        
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        mGotouchiLocationListener = new GotouchiLocationListener(this);

    }
    
    private void setLocationManager(boolean fine) {
        if (mLocationManager == null) { return; }
        showLoadingDialog();
        
        Criteria criteria = new Criteria();
        if (fine) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        }
        
        mProvider = mLocationManager.getBestProvider(criteria, true);
        
        if (mProvider != null) {
            Log.d(TAG, "provider = " + mProvider);
            mLocationManager.requestLocationUpdates(
                mProvider,
                60000, // ms // 通知が発生するための最小時間
                10, // メートル // 通知が発生するための最小移動距離
                mGotouchiLocationListener
            );
            Location location = mLocationManager.getLastKnownLocation(mProvider);
            this.updateGotouchiInfo(location);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        this.setLocationManager(false);
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mGotouchiLocationListener);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) { mProgressDialog.dismiss(); }
    }
    
    protected void showLoadingDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }
        runOnUiThread(new Runnable() { public void run () {
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.show();
        } });
    }
    
    protected void hideLoadingDialog() {
        runOnUiThread(new Runnable() { public void run () {
            mProgressDialog.hide();
        } });
    }
    
    private ArrayList<Gotouchi> searchGotouches (double lat, double lon) {
        return this.gotouchiDB.searchByLatLon(lat, lon);
    }
    
    public void updateGotouchiInfo(Location location) {
        if (location == null ) { return; }
        
        final ArrayList<Gotouchi> gotouchis = this.searchGotouches(
                location.getLatitude(), location.getLongitude());
        
        final ArrayAdapter<Gotouchi> adapter = new ArrayAdapter<Gotouchi>(
                this, 
                R.layout.gotouchi_row, 
                R.id.gotouchi_name, 
                gotouchis
        ) {
            public View getView(int pos, View convertView, ViewGroup parent) {
                final View view = super.getView(pos, convertView, parent);
                final Gotouchi gotouchi = (Gotouchi) gotouchis.get(pos);
                
                ((TextView)view.findViewById(R.id.gotouchi_name)).setText(gotouchi.getName());
                ((TextView)view.findViewById(R.id.gotouchi_region)).setText(gotouchi.getRegion());
                
                final Uri uri = Uri.parse("http://www.google.com/m?q=" 
                        + gotouchi.getName() + " 舞台訪問|聖地巡礼|御当地訪問|ご当地訪問");
                view.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));                        
                    }
                });
                return view;
            }
        };
        runOnUiThread(new Runnable() {            
            public void run() {
                ListView spotsList = (ListView) findViewById(R.id.gotouchi_list);
                spotsList.setAdapter(adapter);
                hideLoadingDialog();
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {        
        menu.add(Menu.NONE, MENU_RELOAD_LOCATION_ID, Menu.NONE, R.string.menu_reload_location);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        
        Log.v(TAG, String.valueOf(item.getItemId()));   
        
        if (item.getItemId() == MENU_RELOAD_LOCATION_ID) {
            if (mLocationManager != null && mProvider != null) {

                mLocationManager.removeUpdates(mGotouchiLocationListener);
                this.setLocationManager(true);
            }
        }
        else {
            ret = super.onOptionsItemSelected(item);
        }
        return ret;
    }
}

class GotouchiLocationListener implements LocationListener {
    final String TAG = "GotouchiLocationListener";
    
    final GotouchiLauncher context;
    public GotouchiLocationListener(GotouchiLauncher context) {
        this.context = context;
    }
    
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    
    public void onProviderEnabled(String provider) {
    }
    
    public void onProviderDisabled(String provider) {
    }
    
    public void onLocationChanged(Location location) {
        this.context.updateGotouchiInfo(location);
    }
}