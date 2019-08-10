package com.example.mymap;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    public LocationClient mLocationClient;
    private MapView mapView;
    private TextView positionText;
    private BaiduMap baiduMap;
	private LocationClientOption mOption;
    private boolean isFirstLocate = true;


    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MylocationListerner());
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        positionText = (TextView)findViewById(R.id.position_text_view);
        mapView=(MapView)findViewById(R.id.bmapView);
        Button button = (Button)findViewById(R.id.check_button);
        //注册按钮监听
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText1 = (EditText)findViewById(R.id.weidu_edit);
                EditText editText2 = (EditText)findViewById(R.id.jingdu_edit);
                String weidu = editText1.getText().toString();
                String jingdu = editText2.getText().toString();

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
        });

        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        //检测权限
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
    //定位信息到达，处理地图
    private void navigateto(BDLocation location){
        if(isFirstLocate){//初次请求定位，将地图移动到我的位置
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
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
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }
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
            positionText.setText(currentPosition);

        }
    }
}