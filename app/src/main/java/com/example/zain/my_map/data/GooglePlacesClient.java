package com.example.zain.my_map.data;

import retrofit2.http.GET;
import retrofit2.http.Query;
import com.example.zain.my_map.data.model.PlaceAutocompleteResult;
import com.example.zain.my_map.data.model.PlaceDetailsResult;
import rx.Observable;

public interface GooglePlacesClient {
    @GET("autocomplete/json?types=address&components=country:pk")
    Observable<PlaceAutocompleteResult> autocomplete(@Query("input") String str);

    @GET("details/json")
    Observable<PlaceDetailsResult> details(@Query("placeid") String placeId);
}





//https://maps.googleapis.com/maps/api/place/autocomplete/json?input=bhubaneshwar&components=country:in&sensor=false&key=your_key