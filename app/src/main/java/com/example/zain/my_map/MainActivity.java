package com.example.zain.my_map;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, PlaceSelectionListener {

    private static final String LOG_TAG = "PlaceSelectionListener";
    //private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(24, 61), new LatLng(37, 75.5));
    private static final int REQUEST_SELECT_PLACE = 1000;


    protected LocationManager locationManager;
    protected LocationListener locationListener;


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


        ImageView img_view = (ImageView) findViewById(R.id.img);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autocomplete_text);


        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {


// filter


                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS).build();

                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(MainActivity.this);


                    //autocomplete intent


                    //Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(MainActivity.this);

                    startActivityForResult(intent, REQUEST_SELECT_PLACE);


                } catch (GooglePlayServicesRepairableException |
                        GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }


        });


        addOnAutoCompleteTextViewItemClickedSubscriber(autoCompleteTextView);
        addOnAutoCompleteTextViewTextChangedObserver(autoCompleteTextView);


        //ImageView img_view = (ImageView) findViewById(R.id.img);

        img_view.setOnClickListener(new View.OnClickListener()
        {
            @Override

            public void onClick(View v)
            {


                //getMyLocation();


                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);




                android.location.Location userLocation = map.getMyLocation();




                LatLng myLocation = null;

//                LatLng latLng = new LatLng(dLat, dLong);

                if (userLocation != null)
                {
                    myLocation   = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

//                    Marker marker = map.addMarker(new MarkerOptions().position(myLocation).title("You are here"));
//                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, map.getMaxZoomLevel() - 5));


                    Marker marker = map.addMarker(new MarkerOptions().position(myLocation).title(myLocation.toString()));
                    marker.showInfoWindow();

                    CameraUpdate cameraupdate = CameraUpdateFactory.newLatLng(myLocation);
                    map.animateCamera(cameraupdate);

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 20));
                    KeyboardHelper.hideKeyboard(MainActivity.this);



                    //Marker marker = map.addMarker(new MarkerOptions().position(myLocation).title("You are here"));






                }


               }
            });

        }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    private void addOnAutoCompleteTextViewTextChangedObserver(final AutoCompleteTextView autoCompleteTextView) {
        Observable<PlaceAutocompleteResult> autocompleteResponseObservable = RxTextView.textChangeEvents(autoCompleteTextView).debounce(DELAY_IN_MILLIS, TimeUnit.MILLISECONDS)

                .map(new Func1<TextViewTextChangeEvent, String>() {
                    @Override
                    public String call(TextViewTextChangeEvent textViewTextChangeEvent) {
                        return textViewTextChangeEvent.text().toString();
                    }
                })

                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.length() >= 2;
                    }
                })

                .observeOn(Schedulers.io()).flatMap(new Func1<String, Observable<PlaceAutocompleteResult>>() {
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
            public void onError(Throwable e) {
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



                //Place listener methods


    @Override
    public void onPlaceSelected(Place place)

    {
        Log.i(LOG_TAG, "Place Selected: " + place.getName());
        locationView.setText(getString(R.string.formatted_place_data, place.getName(), place.getAddress(), place.getPhoneNumber(), place.getWebsiteUri(), place.getRating(), place.getId()));
        if (!TextUtils.isEmpty(place.getAttributions()))
        {
            attributeView.setText(Html.fromHtml(place.getAttributions().toString()));
        }

    }


    @Override
    public void onError(Status status)
    {

    }




    private static class NameAndPlaceId {
        final String name;
        final String placeId;

        NameAndPlaceId(String name, String placeId) {
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
























