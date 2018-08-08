package com.cooyet.im.imservice.entity;

import com.amap.api.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by user on 2018/7/11.
 */

public class LatLngSeriz  implements Serializable {
    public LatLngSeriz(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int index;//0 car .1 person
    public double latitude;
    public double longitude;
}
