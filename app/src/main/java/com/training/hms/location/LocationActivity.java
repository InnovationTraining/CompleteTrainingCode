package com.training.hms.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStates;
import com.huawei.hms.location.SettingsClient;
import com.training.hms.R;
import com.training.hms.util.CollectionUtil;

import java.util.List;

public class LocationActivity extends AppCompatActivity {
    private TextView mTvResult;
    // 声明fusedLocationProviderClient对象
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null) {
                onDealLocationInfo(locationResult);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        mTvResult = findViewById(R.id.text_result);
        mTvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        initClickListener();
        initPermission();
    }

    private void initClickListener() {
        findViewById(R.id.check_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckLocation();
            }
        });
        findViewById(R.id.get_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGetLocation();
            }
        });

    }

    private void initPermission() {
        // Android SDK<=28 所需权限动态申请
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this, strings, 1);
            }
        } else {
            // Android SDK>28 所需权限动态申请，需添加“android.permission.ACCESS_BACKGROUND_LOCATION”权限
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    "android.permission.ACCESS_BACKGROUND_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                String[] strings = {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        "android.permission.ACCESS_BACKGROUND_LOCATION"};
                ActivityCompat.requestPermissions(this, strings, 2);
            }
        }
    }

    private void onCheckLocation() {
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        LocationRequest mLocationRequest = new LocationRequest();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        // 检查设备定位设置
        mSettingsClient.checkLocationSettings(locationSettingsRequest)
                // 检查设备定位设置接口调用成功监听
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        LocationSettingsStates locationSettingsStates =
                                locationSettingsResponse.getLocationSettingsStates();
                        onInitText("定位是否打开：" + locationSettingsStates.isLocationUsable());
                    }
                })
                // 检查设备定位设置接口失败监听回调
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        onInitText("定位没有打开,请到设置中打开");
                    }
                });
    }

    private void onGetLocation() {
        // 实例化fusedLocationProviderClient对象
        if (null == mFusedLocationProviderClient) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        }
        // 定位模式选择
        LocationRequest mLocationRequest = new LocationRequest();
        // 设置定位类型
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // 设置回调次数为1
        // mLocationRequest.setNumUpdates(1);
        // 设置位置更新的间隔（单位：毫秒）
        mLocationRequest.setInterval(10000);

        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // TODO: 接口调用成功的处理
                        onInitText("启动定位成功，开始获取定位信息");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // TODO: 接口调用失败的处理
                        onInitText("启动定位失败");
                    }
                });

    }

    private void onDealLocationInfo(LocationResult locationResult) {
        if (null == locationResult) {
            onInitText("获取定位信息错误");
            return;
        }

        List<Location> locationList = locationResult.getLocations();
        if (CollectionUtil.isEmpty(locationList)) {
            onInitText("获取定位信息为空");
            return;
        }
        for (Location location : locationList) {
            onInitText("纬度:" + location.getLatitude());
            onInitText("经度:" + location.getLongitude());
        }
    }

    private void onInitText(String string) {
        mTvResult.append(string);
        mTvResult.append("\n");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mFusedLocationProviderClient) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                    // 停止位置更新成功监听回调
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    })
                    // 停止位置更新失败监听回调
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                        }
                    });
            mFusedLocationProviderClient.disableBackgroundLocation();
        }
    }
}