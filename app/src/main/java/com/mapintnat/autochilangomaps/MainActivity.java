package com.mapintnat.autochilangomaps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.AddressDetail;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.CoordinateBounds;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.Poi;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    RecyclerView recyclerView;

    SearchResultListener searchResultListener = new SearchResultListener<NearbySearchResponse>() {
        @Override
        public void onSearchResult(NearbySearchResponse results) {
            StringBuilder stringBuilder = new StringBuilder();
            if (results != null) {
                List<Site> sites = results.getSites();
                if (sites != null && sites.size() > 0) {
                    recyclerView = findViewById(R.id.recycler_view_map);
                    MapSearchListAdapter adapter = new MapSearchListAdapter(sites);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(adapter);
                    int count = 1;
                    for (Site site : sites) {
                        AddressDetail addressDetail = site.getAddress();
                        Coordinate location = site.getLocation();
                        Poi poi = site.getPoi();
                        CoordinateBounds viewport = site.getViewport();
                        stringBuilder.append(String.format(
                                "name: %s, formatAddress: %s \n\n",
                                site.getName(), site.getFormatAddress()));
                    }
                } else {
                    stringBuilder.append("0 results");
                }
            }
            showSuccessResult(stringBuilder.toString());

            Log.i(TAG, ": -------------------- Search Listener Success");
        }

        @Override
        public void onSearchError(SearchStatus status) {
            showFailResult("", status.getErrorCode(), status.getErrorMessage());
        }
    };

    TextView NearbySearchResults;
    EditText origin;
    EditText destination;
    String language = "es";

    double latitude;
    double longitude;

    Integer radiusValue = 5000; //in meters


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

        origin = (EditText)findViewById(R.id.edt_origin);
        destination = (EditText)findViewById(R.id.edt_destination);

        origin.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s){}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                nearbySearch(String.valueOf(s));
            }
        });

        destination.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                nearbySearch(String.valueOf(s));
                Log.i(TAG, ": -------------------- Text Changed");
            }
        });


    }

    private void nearbySearch(String queryString) {
        NearbySearchRequest request = new NearbySearchRequest();
        request.setLocation(new Coordinate(latitude, longitude));
        request.setQuery(queryString);
        request.setLanguage("es");
        request.setRadius(radiusValue);
        request.setPageIndex(1);
        request.setPageSize(6);
        searchService.nearbySearch(request, searchResultListener);
        Log.i(TAG, ": -------------------- Nearby Search Done");
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
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    LatLng latLng1 = new LatLng(latitude, longitude);
                    CameraUpdate cameraUpdateLocation = CameraUpdateFactory.newLatLng(latLng1);
                    float zoom = 15.0f;
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

    private void showSuccessResult(String result) {
        //((TextView) findViewById(R.id.nearby_search_result_text)).setText(result);
    }

    private void showFailResult(String result, String errorCode, String errorMessage) {
        //((TextView) findViewById(R.id.nearby_search_result_text)).setText(result);
    }
}
