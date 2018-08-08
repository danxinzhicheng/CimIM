package com.cooyet.im.ui.fragment;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.cooyet.im.DB.entity.UserEntity;
import com.cooyet.im.R;
import com.cooyet.im.app.IMApplication;
import com.cooyet.im.imservice.entity.LatLngSeriz;
import com.cooyet.im.imservice.entity.StateHistoryMarker;
import com.cooyet.im.imservice.service.IMService;
import com.cooyet.im.imservice.support.IMServiceConnector;
import com.cooyet.im.ui.base.TTBaseFragment;
import com.cooyet.im.utils.PoiOverlay;
import com.cooyet.tools.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

public class StateFragment extends TTBaseFragment implements AMap.OnMyLocationChangeListener, View.OnClickListener, AMap.OnMapTouchListener, PoiSearch.OnPoiSearchListener, Inputtips.InputtipsListener {
    private View curView = null;
    private MapView mapView;
    private EditText etSearch;
    private AMap aMap;
    private UiSettings mapUiSetting;
    private TextView btnCar, btnPerson, btnPen, btnClearMaker;
    private Button btnSend;

    private Boolean isPenEnable = false;
    private Boolean isCarEnable = true;
    private Boolean isPersonEnable = false;

    private List<LatLng> listPts = new ArrayList<>();
    private Polyline polyline;

    private PoiSearch.Query query = null;
    private PoiSearch poiSearch;
    private ProgressDialog progDialog = null;// 搜索时进度条
    private String cityCode = "025";
    private PoiResult poiResult;

    private StateHistoryMarker mHistoryData = new StateHistoryMarker();

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(getContext());
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索...");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
    }

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {

        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            IMService imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }
            if (imService.getLoginManager().getLoginInfo() != null) {
                UserEntity mEntity = imService.getLoginManager().getLoginInfo();
                mHistoryData.userId = mEntity.getPeerId();
                StateHistoryMarker historyData = (StateHistoryMarker) SharedPreferencesHelper.getObject(getContext(), String.valueOf(mHistoryData.userId));
                if (historyData != null) {
                    mHistoryData = historyData;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadHistoryToMaker(mHistoryData);
                        }
                    }, 2000);
                }

            }
        }
    };

    private List<LatLng> convertLatLng(List<LatLngSeriz> list) {
        List<LatLng> list1 = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            LatLngSeriz latLngSeriz = list.get(i);
            LatLng latLng = new LatLng(latLngSeriz.latitude, latLngSeriz.longitude);
            list1.add(latLng);
        }
        return list1;
    }

    private void loadHistoryToMaker(StateHistoryMarker historyData) {
        if (historyData == null) return;
        List<LatLngSeriz> list1 = historyData.personHistory;
        List<LatLngSeriz> list2 = historyData.carHistory;
        List<LatLngSeriz> list3 = historyData.penHistory;

        if (list3 != null && list3.size() >= 0) {
            List<LatLng> list = convertLatLng(list3);
            drawLine(list);
        }

        if (list1 != null && list1.size() >= 0) {
            for (int i = 0; i < list1.size(); i++) {
                LatLng latLng = new LatLng(list1.get(i).latitude, list1.get(i).longitude);
                drawPersonMaker(latLng, list1.get(i));
            }
        }

        if (list2 != null && list2.size() >= 0) {
            for (int i = 0; i < list2.size(); i++) {
                LatLng latLng = new LatLng(list2.get(i).latitude, list2.get(i).longitude);
                drawCarMaker(latLng, list2.get(i));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_internal,
                topContentView);
        initRes();
        //设置离线地图路径
        MapsInitializer.sdcardDir = IMApplication.mapOfflinePath;
        mapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mapView.getMap();
            mapUiSetting = aMap.getUiSettings();
        }

        setUpMap();

        return curView;
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
        mapUiSetting.setScaleControlsEnabled(true);//控制比例尺控件是否显示
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
//        设置SDK 自带定位消息监听
        aMap.setOnMyLocationChangeListener(this);
        aMap.setOnMapTouchListener(this);

        // 绑定marker拖拽事件
        aMap.setOnMarkerDragListener(markerDragListener);
    }


    private static long currentTime = 0;
    private Boolean isInDragMarkerState() {

        if (System.currentTimeMillis() - currentTime >= 1000) {
            return false;
        } else {
            return true;
        }
    }

    // 定义 Marker拖拽的监听
    private AMap.OnMarkerDragListener markerDragListener = new AMap.OnMarkerDragListener() {

        // 当marker开始被拖动时回调此方法, 这个marker的位置可以通过getPosition()方法返回。
        // 这个位置可能与拖动的之前的marker位置不一样。
        // marker 被拖动的marker对象。
        @Override
        public void onMarkerDragStart(Marker marker) {

            LatLngSeriz latLngSeriz = (LatLngSeriz) marker.getObject();
            if (latLngSeriz.index == 0) {
                mHistoryData.carHistory.remove(mHistoryData.carHistory.indexOf(latLngSeriz));
            } else if (latLngSeriz.index == 1) {
                mHistoryData.personHistory.remove(mHistoryData.personHistory.indexOf(latLngSeriz));
            }

            currentTime = System.currentTimeMillis();

            marker.setVisible(true);
            marker.remove();
            marker.destroy();

        }

        // 在marker拖动完成后回调此方法, 这个marker的位置可以通过getPosition()方法返回。
        // 这个位置可能与拖动的之前的marker位置不一样。
        // marker 被拖动的marker对象。
        @Override
        public void onMarkerDragEnd(Marker marker) {
            Log.i("mmmm", "onMarkerDragEnd");

        }

        // 在marker拖动过程中回调此方法, 这个marker的位置可以通过getPosition()方法返回。
        // 这个位置可能与拖动的之前的marker位置不一样。
        // marker 被拖动的marker对象。
        @Override
        public void onMarkerDrag(Marker marker) {
            Log.i("mmmm", "onMarkerDrag");
        }
    };

//    // 定义 Marker 点击事件监听
//    private AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
//        // marker 对象被点击时回调的接口
//        // 返回 true 则表示接口已响应事件，否则返回false
//        @Override
//        public boolean onMarkerClick(Marker marker) {
//            return true;
//        }
//    };

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private void initRes() {
        // 设置顶部标题栏
        setTopTitle(getActivity().getString(R.string.main_innernet));
        mapView = (MapView) curView.findViewById(R.id.map);
        etSearch = (EditText) curView.findViewById(R.id.input_edittext);
        btnCar = (TextView) curView.findViewById(R.id.btn_car);
        btnPerson = (TextView) curView.findViewById(R.id.btn_person);
        btnPen = (TextView) curView.findViewById(R.id.btn_edit);
        btnSend = (Button) curView.findViewById(R.id.btn_send);
        btnClearMaker = (TextView) curView.findViewById(R.id.btn_clear);
        btnCar.setOnClickListener(this);
        btnPerson.setOnClickListener(this);
        btnPen.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnClearMaker.setOnClickListener(this);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString().trim();
                if (!TextUtils.isEmpty(newText)) {
                    InputtipsQuery inputquery = new InputtipsQuery(newText, cityCode);
                    Inputtips inputTips = new Inputtips(getContext(), inputquery);
                    inputTips.setInputtipsListener(StateFragment.this);
                    inputTips.requestInputtipsAsyn();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String keyWord = etSearch.getText().toString();
                    if (TextUtils.isEmpty(keyWord)) {
                        Toast.makeText(getContext(), "请输入搜索关键字", Toast.LENGTH_SHORT).show();
                    } else {
                        doSearchQuery(keyWord);
                    }
                    return true;
                }
                return false;
            }
        });

        btnSend.setVisibility(View.GONE);//todo 先隐藏

    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(String keyWord) {
        showProgressDialog();// 显示进度框
        int currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", cityCode);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(getContext(), query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    private void setCarState() {
//        aMap.clear();
        btnCar.setBackgroundResource(R.drawable.state_car_press);
        btnPerson.setBackgroundResource(R.drawable.state_person);
        btnPen.setBackgroundResource(R.drawable.state_pen);
        isPenEnable = false;
        isCarEnable = true;
        isPersonEnable = false;
        mapUiSetting.setScrollGesturesEnabled(true);
    }

    private void setPersonState() {
//        aMap.clear();
        btnCar.setBackgroundResource(R.drawable.state_car);
        btnPerson.setBackgroundResource(R.drawable.state_person_press);
        btnPen.setBackgroundResource(R.drawable.state_pen);
        isPenEnable = false;
        isCarEnable = false;
        isPersonEnable = true;
        mapUiSetting.setScrollGesturesEnabled(true);
    }

    private void setEditState() {
//        aMap.clear();
        btnCar.setBackgroundResource(R.drawable.state_car);
        btnPerson.setBackgroundResource(R.drawable.state_person);
        btnPen.setBackgroundResource(R.drawable.state_pen_press);
        isPenEnable = true;
        isPersonEnable = false;
        isCarEnable = false;
        listPts.clear();
        mapUiSetting.setScrollGesturesEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_car:
                setCarState();
                break;
            case R.id.btn_person:
                setPersonState();
                break;
            case R.id.btn_edit:
                setEditState();
                break;
            case R.id.btn_send:
                break;
            case R.id.btn_clear:
                //清除轨迹
                if (polyline != null) {
                    polyline.setVisible(false);
                    polyline.remove();
                    listPts.clear();
                    mHistoryData.penHistory.clear();
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferencesHelper.putObject(getContext(), String.valueOf(mHistoryData.userId), mHistoryData);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void initHandler() {
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
                int locationType = bundle.getInt(MyLocationStyle.LOCATION_TYPE);
//                mLatLng1 = new LatLng(location.getLatitude(), location.getLongitude());
            }
        }
    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                Log.i("mmmm", "ACTION_UP");
                if (isInDragMarkerState()) {
                    return;
                }
                if (isCarEnable) {
                    Point touchpt1 = new Point();
                    touchpt1.x = (int) motionEvent.getX();
                    touchpt1.y = (int) motionEvent.getY();
                    LatLng latlng = aMap.getProjection()
                            .fromScreenLocation(touchpt1);

                    LatLngSeriz latLngSeriz = new LatLngSeriz(latlng.latitude, latlng.longitude);
                    latLngSeriz.index = 0;
                    mHistoryData.carHistory.add(latLngSeriz);

                    drawCarMaker(latlng, latLngSeriz);

                } else if (isPersonEnable) {
                    Point touchpt1 = new Point();
                    touchpt1.x = (int) motionEvent.getX();
                    touchpt1.y = (int) motionEvent.getY();
                    LatLng latlng = aMap.getProjection()
                            .fromScreenLocation(touchpt1);

                    LatLngSeriz latLngSeriz = new LatLngSeriz(latlng.latitude, latlng.longitude);
                    latLngSeriz.index = 1;
                    mHistoryData.personHistory.add(latLngSeriz);

                    drawPersonMaker(latlng, latLngSeriz);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isPenEnable) {
                    Point touchpt = new Point();
                    touchpt.x = (int) motionEvent.getX();
                    touchpt.y = (int) motionEvent.getY();
                    LatLng latlng = aMap.getProjection()
                            .fromScreenLocation(touchpt);
                    listPts.add(latlng);

                    LatLngSeriz latLngSeriz = new LatLngSeriz(latlng.latitude, latlng.longitude);
                    mHistoryData.penHistory.add(latLngSeriz);

                    // 地图上绘制
                    drawLine(listPts);
                }
                break;
            case MotionEvent.ACTION_DOWN:
                break;
        }
    }

    private void drawPersonMaker(LatLng latLng, LatLngSeriz latLngSeriz) {
        Marker mMarker = aMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.drawable.state_mark_person)))
                .draggable(true));
        mMarker.setObject(latLngSeriz);
    }

    private void drawCarMaker(LatLng latLng, LatLngSeriz latLngSeriz) {
        Marker mMarker = aMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.drawable.state_mark_car)))
                .draggable(true));
        mMarker.setObject(latLngSeriz);
    }

    /**
     * 地图上画线
     *
     * @param pts
     */
    private void drawLine(List<LatLng> pts) {
        if (pts.size() >= 2) {
            if (pts.size() == 2) {
                polyline = aMap.addPolyline((new PolylineOptions()).addAll(pts)
                        .width(10).setDottedLine(true).geodesic(true)
                        .color(Color.RED));
            } else {
                if (polyline == null) {
                    polyline = aMap.addPolyline((new PolylineOptions()).addAll(pts)
                            .width(10).setDottedLine(true).geodesic(true)
                            .color(Color.RED));
                }
                polyline.setPoints(pts);
            }

        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();// 隐藏对话框
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        aMap.clear();// 清理之前的图标
                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else {
                        Toast.makeText(getContext(), "对不起，没有搜索到相关数据", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getContext(), "对不起，没有搜索到相关数据", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "对不起，没有搜索到相关数据", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
//            ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
//                    getApplicationContext(),
//                    R.layout.route_inputs, listString);
//            searchText.setAdapter(aAdapter);
//            aAdapter.notifyDataSetChanged();
        }
    }
}
