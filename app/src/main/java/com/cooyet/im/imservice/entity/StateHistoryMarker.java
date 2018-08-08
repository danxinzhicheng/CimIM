package com.cooyet.im.imservice.entity;

import com.amap.api.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2018/7/11.
 */

public class StateHistoryMarker implements Serializable {
    public int userId;
    public List<LatLngSeriz> carHistory = new ArrayList<>();
    public List<LatLngSeriz> personHistory = new ArrayList<>();
    public List<LatLngSeriz> penHistory = new ArrayList<>();
}
