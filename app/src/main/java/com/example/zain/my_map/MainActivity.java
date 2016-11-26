package com.example.zain.my_map;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jakewharton.rxbinding.widget.AdapterViewItemClickEvent;
import com.jakewharton.rxbinding.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.zain.my_map.data.model.Location;
import com.example.zain.my_map.data.model.PlaceAutocompleteResult;
import com.example.zain.my_map.data.model.PlaceDetailsResult;
import com.example.zain.my_map.data.model.Prediction;
import com.example.zain.my_map.data.RestClient;
import com.example.zain.my_map.utils.KeyboardHelper;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final String LOG_TAG = "PlaceSelectionListener";
    //private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(24, 61), new LatLng(37, 75.5));
    private static final int REQUEST_SELECT_PLACE = 1000;
    private static final String TAG = "MainActivity";

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    Boolean IsGPS=false;

    private static final long DELAY_IN_MILLIS = 500;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private GoogleMap map;

    Marker currloc;

    protected double dLat;
    protected double dLong;
    private TextView locationView;
    private TextView attributeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(android.location.Location location)
            {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET
            }
                    ,10);
            return;
        }
        else{
            configure_button();
        }
        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autocomplete_text);
        addOnAutoCompleteTextViewItemClickedSubscriber(autoCompleteTextView);
        addOnAutoCompleteTextViewTextChangedObserver(autoCompleteTextView);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case 10:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    configure_button();
                    return;
                }
        }
    }

    private void configure_button()
    {
        ImageView img_view = (ImageView) findViewById(R.id.img);

        img_view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;}
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                android.location.Location gpslocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng latLng = new LatLng(gpslocation.getLatitude(), gpslocation.getLongitude());
                map.clear();
                Marker marker = map.addMarker(new MarkerOptions().position(latLng).title("Home Sweet Home"));
                marker.showInfoWindow();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
    }

    private void addOnAutoCompleteTextViewTextChangedObserver(final AutoCompleteTextView autoCompleteTextView)
    {
        Observable<PlaceAutocompleteResult> autocompleteResponseObservable = RxTextView.textChangeEvents(autoCompleteTextView).debounce(DELAY_IN_MILLIS, TimeUnit.MILLISECONDS)

                .map(new Func1<TextViewTextChangeEvent, String>()
                {
                    @Override
                    public String call(TextViewTextChangeEvent textViewTextChangeEvent)
                    {
                        return textViewTextChangeEvent.text().toString();
                    }
                })

                .filter(new Func1<String, Boolean>()
                {
                    @Override
                    public Boolean call(String s) {
                        return s.length() >= 2;
                    }
                })

                .observeOn(Schedulers.io()).flatMap(new Func1<String, Observable<PlaceAutocompleteResult>>()
                {
                    @Override
                    public Observable<PlaceAutocompleteResult> call(String s) {
                        return RestClient.INSTANCE.getGooglePlacesClient().autocomplete(s);
                    }
                })

                .observeOn(AndroidSchedulers.mainThread()).retry();


        compositeSubscription.add(autocompleteResponseObservable.subscribe(new Observer<PlaceAutocompleteResult>() {

            private static final String TAG = "PlaceAutocompleteResult";

            @Override
            public void onCompleted() {
                Log.i(TAG, "onCompleted");

            }

            @Override
            public void onError(Throwable e)
            {
                Log.e(TAG, "onError", e);
            }

            @Override
            public void onNext(PlaceAutocompleteResult placeAutocompleteResult) {
                Log.i(TAG, placeAutocompleteResult.toString());

                List<NameAndPlaceId> list = new ArrayList<>();
                for (Prediction prediction : placeAutocompleteResult.predictions) {
                    list.add(new NameAndPlaceId(prediction.description, prediction.placeId));
                }

                ArrayAdapter<NameAndPlaceId> itemsAdapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_list_item_1, list);
                autoCompleteTextView.setAdapter(itemsAdapter);
                String enteredText = autoCompleteTextView.getText().toString();
                if (list.size() >= 1 && enteredText.equals(list.get(0).name)) {
                    autoCompleteTextView.dismissDropDown();
                } else {
                    autoCompleteTextView.showDropDown();
                }
            }
        }));
    }


    private void addOnAutoCompleteTextViewItemClickedSubscriber(final AutoCompleteTextView autoCompleteTextView) {
        Observable<PlaceDetailsResult> adapterViewItemClickEventObservable = RxAutoCompleteTextView.itemClickEvents(autoCompleteTextView)

                .map(new Func1<AdapterViewItemClickEvent, String>() {
                    @Override
                    public String call(AdapterViewItemClickEvent adapterViewItemClickEvent) {
                        NameAndPlaceId item = (NameAndPlaceId) autoCompleteTextView.getAdapter()
                                .getItem(adapterViewItemClickEvent.position());
                        return item.placeId;
                    }
                })

                .observeOn(Schedulers.io()).flatMap(new Func1<String, Observable<PlaceDetailsResult>>() {
                    @Override
                    public Observable<PlaceDetailsResult> call(String placeId) {
                        return RestClient.INSTANCE.getGooglePlacesClient().details(placeId);
                    }
                })


                .observeOn(AndroidSchedulers.mainThread()).retry();

        compositeSubscription.add(adapterViewItemClickEventObservable.subscribe(new Observer<PlaceDetailsResult>() {

            private static final String TAG = "PlaceDetailsResult";

            @Override
            public void onCompleted() {
                Log.i(TAG, "onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError", e);
            }

            @Override
            public void onNext(PlaceDetailsResult placeDetailsResponse) {
                Log.i(TAG, placeDetailsResponse.toString());
                updateMap(placeDetailsResponse);
            }
        }));
    }


    private void updateMap(PlaceDetailsResult placeDetailsResponse) {
        if (map != null) {
            map.clear();
            Location location = placeDetailsResponse.result.geometry.location;
            LatLng latLng = new LatLng(location.lat, location.lng);
            Marker marker = map.addMarker(new MarkerOptions().position(latLng).title(placeDetailsResponse.result.name));
            marker.showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            KeyboardHelper.hideKeyboard(MainActivity.this);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }


    //Location Listener methods


    @Override
    public void onLocationChanged(android.location.Location location)
    {
//        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
//        currloc = map.addMarker(new MarkerOptions().position(loc));
//        if (map != null) {
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));

//        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));




    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }







    private static class NameAndPlaceId {
        final String name;
        final String placeId;

        NameAndPlaceId(String name, String placeId)
        {
            this.name = name;
            this.placeId = placeId;
        }

        @Override
        public String toString()

        {
            return name;
        }
    }
}
























