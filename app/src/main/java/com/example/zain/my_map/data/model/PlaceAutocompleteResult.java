package com.example.zain.my_map.data.model;

import java.util.List;

public class PlaceAutocompleteResult {
    public String status;
    public List<Prediction> predictions;

    @Override
    public String toString() {
        return "PlaceAutocompleteResult{" + "status='" + status + '\'' + ", predictions=" + predictions + '}';
    }
}
