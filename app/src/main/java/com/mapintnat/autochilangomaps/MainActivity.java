package com.mapintnat.autochilangomaps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapFragment;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapFragmentDemoActivity";

    private HuaweiMap hMap;
    private static final int REQUEST_CODE = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private MapFragment mMapFragment;
    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
    };

    //Declare a SearchService object.
    private SearchService searchService;
    // Instantiate the SearchService object.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_map);

        searchService = SearchServiceFactory.create(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (!hasPermissions(this, RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE);
        }

        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mMapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(HuaweiMap map) {
        Log.d(TAG, "onMapReady: ");
        hMap = map;
        hMap.setMyLocationEnabled(true);// Enable the my-location overlay.
        hMap.getUiSettings().setMyLocationButtonEnabled(true);
        setLastLocation();
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setLastLocation() {
        try {
            Task<Location> lastLocation = mFusedLocationProviderClient.getLastLocation();
            lastLocation.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location == null) {
                        Log.i(TAG, "setLastLocation got null location");
                        return;
                    }
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latLng1 = new LatLng(latitude, longitude);
                    CameraUpdate cameraUpdateLocation = CameraUpdateFactory.newLatLng(latLng1);
                    float zoom = 12.0f;
                    CameraUpdate cameraUpdateZoom = CameraUpdateFactory.zoomTo(zoom);
                    hMap.moveCamera(cameraUpdateLocation);
                    hMap.moveCamera(cameraUpdateZoom);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "setLastLocation onFailure:" + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "setLastLocation exception:" + e.getMessage());
        }
    }
}
