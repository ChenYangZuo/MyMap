package com.example.mymap;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    public LocationClient mLocationClient;
    public LatLng stLoc;
    public LatLng enLoc;
    public LatLng tempLoc;
    public double x1;
    public double x2;
    public double y1;
    public double y2;
    public int z = 0;
    public WalkingRouteOverlay overlay;
    private String City;
    private ScrollView scrollView;
    private MapContainer map_container;
    private MapView mapView;
    private TextView positionText;
    private BaiduMap baiduMap;
    private PoiCitySearchOption citySearchOption;
    private PoiSearch poiSearch;
    private RoutePlanSearch mSearch;
	private LocationClientOption mOption;
    private boolean isFirstLocate = true;
//    public int i = 0;
//    private double mlatitude;
//    private double mlongitude;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MylocationListerner());
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        positionText = (TextView)findViewById(R.id.position_text_view);
        mapView=(MapView)findViewById(R.id.bmapView);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        map_container = (MapContainer) findViewById(R.id.map_container);
        map_container.setScrollView(scrollView);
        Button button1 = (Button)findViewById(R.id.check_button);
        Button button2 = (Button)findViewById(R.id.daohang_button);
        Button button3 = (Button)findViewById(R.id.qingchu_button);
        //注册按钮监听
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yingjichangkong(view);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qujingzheyue(view);
            }
        });

        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                overlay.removeFromMap();
            }
        });
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        quanxiangou();

    }
    //定位信息到达，处理地图
    private void navigateto(BDLocation location){
        if(isFirstLocate){//初次请求定位，将地图移动到我的位置
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(18f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }
    //请求定位
    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }
    //设置请求参数
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");//使用百度经纬度，默认为国家测绘局坐标，放入百度地图会有误差
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setScanSpan(5000);//定位间隔
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }
    //省电措施
    @Override
    public void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
        mLocationClient.stop();
        baiduMap.setMyLocationEnabled(false);
    }
    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
        mapView.onPause();
    }
    //检测权限
    private  void quanxiangou(){
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
        else{
            requestLocation();//开始请求定位
        }
    }
    //未获取权限报错
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    //位置信息监听
    public class MylocationListerner implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location){
            if(location.getLocType() == BDLocation.TypeGpsLocation||location.getLocType() == BDLocation.TypeNetWorkLocation){
                navigateto(location);
            }
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
            currentPosition.append("经度：").append(location.getLongitude()).append("\n");
            currentPosition.append("国家：").append(location.getCountry()).append("\n");
            currentPosition.append("省：").append(location.getProvince()).append("\n");
            currentPosition.append("市：").append(location.getCity()).append("\n");
            currentPosition.append("区：").append(location.getDistrict()).append("\n");
            currentPosition.append("街道：").append(location.getStreet()).append("\n");

            currentPosition.append("定位方式：");
            if(location.getLocType()==BDLocation.TypeGpsLocation){
                currentPosition.append("GPS");
            }else if(location.getLocType()==BDLocation.TypeNetWorkLocation){
                currentPosition.append("网络");
            }
            City = location.getCity();
//            mlatitude = location.getLatitude();
//            mlongitude = location.getLongitude();
            positionText.setText(currentPosition);

        }
    }
    //经纬度定位
    public void yingjichangkong(View view){
        EditText editText1 = (EditText)findViewById(R.id.weidu_edit);
        EditText editText2 = (EditText)findViewById(R.id.jingdu_edit);
        String weidu = editText1.getText().toString();
        String jingdu = editText2.getText().toString();



        if(weidu.isEmpty() == true || jingdu.isEmpty() == true){
            Snackbar.make(view,"参数不能为空哦！",Snackbar.LENGTH_SHORT).show();
            return;
        }

        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();

        locationBuilder.latitude(Double.valueOf(weidu));
        locationBuilder.longitude(Double.valueOf(jingdu));
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);

        LatLng ll = new LatLng(Double.valueOf(weidu),Double.valueOf(jingdu));
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(16f);
        baiduMap.animateMapStatus(update);
    }
    //路线规划
    public void qujingzheyue(final View view){
        EditText editText1 = (EditText)findViewById(R.id.chufa_edit);
        EditText editText2 = (EditText)findViewById(R.id.daoda_edit);
        String chufa = editText1.getText().toString();
        String daoda = editText2.getText().toString();

        if(chufa.isEmpty() == true || daoda.isEmpty() == true){
            Snackbar.make(view,"参数不能为空哦！",Snackbar.LENGTH_SHORT).show();
            return;
        }

        mSearch = RoutePlanSearch.newInstance();
        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                overlay = new WalkingRouteOverlay(baiduMap);
                if(walkingRouteResult.getRouteLines()==null){
                    Snackbar.make(view,"哦噢地图卖光了o(╥﹏╥)o",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (walkingRouteResult.getRouteLines().size() > 0) {
                    overlay.setData(walkingRouteResult.getRouteLines().get(0));
                    overlay.addToMap();
                }
            }
            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }
            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }
            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }
            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
            }
            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
            }
        };
        mSearch.setOnGetRoutePlanResultListener(listener);

        Log.d("MainActivity",""+chufa);
        Log.d("MainActivity",""+daoda);

        Citysearch(chufa,daoda);

        stLoc = new LatLng(x1,y1);
        enLoc = new LatLng(x2,y2);

        PlanNode stNode = PlanNode.withLocation(stLoc);
        PlanNode enNode = PlanNode.withLocation(enLoc);

        mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
        mSearch.destroy();

    }
    //获取所述地点经纬度
    private void Citysearch(String chufa,String daoda){
        OnGetPoiSearchResultListener onGetPoiSearchResultListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                tempLoc = poiResult.getAllPoi().get(0).getLocation();
                if(z == 0){
                    x1 = tempLoc.latitude;
                    y1 = tempLoc.longitude;
                    Log.d("MainActivity","出发：");
                    Log.d("MainActivity","x:"+x1);
                    Log.d("MainActivity","y:"+y1);
                    z = 1;
                }
                else if(z == 1){
                    x2 = tempLoc.latitude;
                    y2 = tempLoc.longitude;
                    Log.d("MainActivity","到达：");
                    Log.d("MainActivity","x:"+x2);
                    Log.d("MainActivity","y:"+y2);
                    z = 0;
                }

                return;
            }
            @Override
            public void onGetPoiDetailResult(PoiDetailResult arg0) {
                // TODO Auto-generated method stub

            }
            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }
            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        };
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(onGetPoiSearchResultListener);
        citySearchOption = new PoiCitySearchOption();
        citySearchOption.city(City);
        citySearchOption.keyword(chufa);
        poiSearch.searchInCity(citySearchOption);
        citySearchOption.keyword(daoda);
        poiSearch.searchInCity(citySearchOption);
    }
}