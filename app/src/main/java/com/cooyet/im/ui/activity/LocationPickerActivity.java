package com.cooyet.im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.TranslateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.cooyet.im.R;
import com.cooyet.im.app.IMApplication;
import com.cooyet.im.imservice.event.SelectEvent;
import com.cooyet.im.ui.adapter.album.ImageItem;
import com.cooyet.im.utils.LocationUtil;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by user on 2018/5/9.
 */

public class LocationPickerActivity extends Activity implements AMap.OnMyLocationChangeListener, PoiSearch.OnPoiSearchListener {
    private MapView mapView;
    private AMap aMap;
    private Marker screenMarker = null;
    private UiSettings mapUiSetting;
    private ListView hotList;
    private PoiSearch.Query query = null;
    private PoiSearch poiSearch;
    private CommonAdapter adapter;
    private List aroundLocationList = new ArrayList();
    private String currentDetailLocationName;
    private String currentLocationName;
    private int selectedIndex = 0;
    private TextView btnSend;
    private LatLng latLngCur;

    /**
     * 对截图添加经纬度信息
     *
     * @param path
     */
    private void addExif(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, LocationUtil.degressToString(latLngCur.longitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
                    latLngCur.longitude > 0 ? "E" : "W");
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, LocationUtil.degressToString(latLngCur.latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
                    latLngCur.latitude > 0 ? "N" : "S");
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
        setContentView(R.layout.tt_activity_location_pick);
        btnSend = (TextView) this.findViewById(R.id.send);
        mapView = (MapView) this.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
           /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
        MapsInitializer.sdcardDir = IMApplication.mapOfflinePath;

        hotList = (ListView) findViewById(R.id.map_hot_list);
        adapter = new CommonAdapter(this, R.layout.item_acty_location_detail, aroundLocationList) {
            @Override
            protected void convert(ViewHolder viewHolder, Object item, int position) {

                TextView detailAddress = viewHolder.getView(R.id.location_detail_address);
                ImageView selectIcon = viewHolder.getView(R.id.location_select_icon);

                detailAddress.setVisibility(View.VISIBLE);
                PoiItem poiItem = (PoiItem) item;

                viewHolder.setText(R.id.location_address, poiItem.getTitle());
                viewHolder.setText(R.id.location_detail_address, poiItem.getSnippet());

                if (selectedIndex == position) {
                    selectIcon.setVisibility(View.VISIBLE);
                } else {
                    selectIcon.setVisibility(View.GONE);
                }
            }
        };

        hotList.setAdapter(adapter);

        hotList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (selectedIndex == i) {
                    return;
                }
                selectedIndex = i;

                PoiItem poiItem = (PoiItem) aroundLocationList.get(i);
                LatLonPoint point = poiItem.getLatLonPoint();
                latLngCur = new LatLng(point.getLatitude(), point.getLongitude());
                currentLocationName = poiItem.getTitle();
                currentDetailLocationName = poiItem.getSnippet();
                adapter.notifyDataSetChanged();

                changeCamera(
                        CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                latLngCur, 18, 30, 0)));
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
                    @Override
                    public void onMapScreenShot(Bitmap bitmap) {

                    }

                    @Override
                    public void onMapScreenShot(Bitmap bitmap, int i) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        if (null == bitmap) {
                            return;
                        }
                        try {
                            String path = Environment.getExternalStorageDirectory() + "/test_"
                                    + sdf.format(new Date()) + ".jpg";
                            FileOutputStream fos = new FileOutputStream(path);
                            boolean b = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            try {
                                fos.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (b) {
                                //经纬度信息添加到图片上
                                addExif(path);
                                //发送latLngCur，Bitmap
                                List<ImageItem> itemList = new ArrayList<>();
                                ImageItem item = new ImageItem();
//                                item.setImageId("longitude" + latLngCur.longitude + ";" + "latitude:" + latLngCur.latitude);
                                item.setImagePath(path);
                                item.setThumbnailPath(path);
                                itemList.add(item);
                                EventBus.getDefault().post(new SelectEvent(itemList));
                                LocationPickerActivity.this.setResult(RESULT_OK, null);
                                LocationPickerActivity.this.finish();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });


            }
        });
        init();
    }

    private void init() {

        if (aMap == null) {
            aMap = mapView.getMap();
            mapUiSetting = aMap.getUiSettings();
            setUpMap();
        }
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                addMarkerInScreenCenter();
            }
        });
        // 设置可视范围变化时的回调的接口方法
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition postion) {
                //屏幕中心的Marker跳动
                LatLng l = postion.target;
                latLngCur = l;
                poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(l.latitude,
                        l.longitude), 1000));//设置周边搜索的中心点以及半径
                poiSearch.searchPOIAsyn();
                startJumpAnimation();
            }
        });

        query = new PoiSearch.Query("", "", "");
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);

        mapUiSetting.setScaleControlsEnabled(true);//控制比例尺控件是否显示
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
//        设置SDK 自带定位消息监听
        aMap.setOnMyLocationChangeListener(this);
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update) {
        aMap.moveCamera(update);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {

        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle = myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
//        设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

    }

    //dip和px转换
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 屏幕中心marker 跳动
     */
    public void startJumpAnimation() {

        if (screenMarker != null) {
            //根据屏幕距离计算需要移动的目标点
            final LatLng latLng = screenMarker.getPosition();
            Point point = aMap.getProjection().toScreenLocation(latLng);
            point.y -= dip2px(this, 125);
            LatLng target = aMap.getProjection()
                    .fromScreenLocation(point);
            //使用TranslateAnimation,填写一个需要移动的目标点
            Animation animation = new TranslateAnimation(target);
            animation.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    // 模拟重加速度的interpolator
                    if (input <= 0.5) {
                        return (float) (0.5f - 2 * (0.5 - input) * (0.5 - input));
                    } else {
                        return (float) (0.5f - Math.sqrt((input - 0.5f) * (1.5f - input)));
                    }
                }
            });
            //整个移动所需要的时间
            animation.setDuration(600);
            //设置动画
            screenMarker.setAnimation(animation);
            //开始动画
            screenMarker.startAnimation();
        } else {
            Log.e("amap", "screenMarker is null");
        }
    }

    /**
     * 在屏幕中心添加一个Marker
     */
    private void addMarkerInScreenCenter() {
        LatLng latLng = aMap.getCameraPosition().target;
        Point screenPosition = aMap.getProjection().toScreenLocation(latLng);
        screenMarker = aMap.addMarker(new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_pin)));
        //设置Marker在屏幕上,不跟随地图移动
        screenMarker.setPositionByPixels(screenPosition.x, screenPosition.y);

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
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onMyLocationChange(Location location) {
        // 定位回调监听
        if (location != null) {
            Log.e("amap", "onMyLocationChange 定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude());
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
                String errorInfo = bundle.getString(MyLocationStyle.ERROR_INFO);
                // 定位类型，可能为GPS WIFI等，具体可以参考官网的定位SDK介绍
                int locationType = bundle.getInt(MyLocationStyle.LOCATION_TYPE);
                latLngCur = new LatLng(location.getLatitude(), location.getLongitude());
                /*
                errorCode
                errorInfo
                locationType
                */
                Log.e("amap", "定位信息， code: " + errorCode + " errorInfo: " + errorInfo + " locationType: " + locationType);
            }
        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        ArrayList<PoiItem> list = poiResult.getPois();
        for (int m = 0; m < list.size(); m++) {
        }
        ArrayList<PoiItem> poiItemList = poiResult.getPois();
//        currentDetailLocationName = poiItemList.get(0).getAdName();
//        currentLocationName = poiItemList.get(0).getAoiName();

        aroundLocationList.clear();
//        aroundLocationList.add(currentDetailLocationName);
        aroundLocationList.addAll(poiItemList);
        adapter.notifyDataSetChanged();
        selectedIndex = 0;
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
