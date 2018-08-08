package com.cooyet.im.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.cooyet.im.R;
import com.cooyet.im.config.IntentConstant;

/**
 * Created by user on 2018/5/15.
 */

public class PreviewMessageLocationActivity extends Activity implements GeocodeSearch.OnGeocodeSearchListener {
    private MapView mapView;
    private AMap aMap;
    private GeocodeSearch geocoderSearch;
    private LatLng curLocation;
    private LatLonPoint latLonPoint;
    private TextView tvAddress;
    private Marker regeoMarker;

    //latitude:118.73977  longitude:31.959375
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_message_location);
        mapView = (MapView) this.findViewById(R.id.map);
        tvAddress = (TextView) this.findViewById(R.id.location_address);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();

        String latitude = this.getIntent().getStringExtra(IntentConstant.CUR_MESSAGE_TAG_GPS_LATITUDE);
        String longitude = this.getIntent().getStringExtra(IntentConstant.CUR_MESSAGE_TAG_GPS_LONGITUDE);

        Log.i("kkkk", "latitude:" + latitude);
        Log.i("kkkk", "longitude:" + longitude);

        curLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        latLonPoint = new LatLonPoint(Double.parseDouble(latitude), Double.parseDouble(longitude));
        changeCamera(
                CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        curLocation, 18, 30, 30)));
        aMap.clear();
        regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        getAddress(latLonPoint);
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update) {

        aMap.moveCamera(update);

    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
//        aMap.getUiSettings().setMyLocationButtonEnabled(true); // 显示默认的定位按钮
//        aMap.setMyLocationEnabled(true);// 可触发定位并显示定位层
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                String addressName = result.getRegeocodeAddress().getFormatAddress();
                tvAddress.setText(addressName);
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        curLocation, 15));
                regeoMarker.setPosition(curLocation);

            }
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {


    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(LatLonPoint latLonPoint) {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 400,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
    }
}
