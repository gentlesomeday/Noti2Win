package com.gsyt.noti2win;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SCAN = 100;

    private int port = 0;
    private String ip = "";

    private AndroidExecutors executor;
    private ServiceAddr serviceAddr;
    TextView textView;
    AlertDialog loadingDialog;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        executor= AndroidExecutors.getInstance();
        serviceAddr=ServiceAddr.getInstance();
        textView = findViewById(R.id.tv_result);
        findViewById(R.id.btn_scan).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // 没有权限，请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                // 已经拥有权限，可以执行相机操作
                openScanCamera();
            }
        });
        findViewById(R.id.btn_start).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });
        loadNotiListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，可以执行相机操作
                openScanCamera();
            } else {
                // 权限被拒绝，显示提示
                Toast.makeText(this, "需要相机权限才能使用扫描功能", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openScanCamera() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            String scanResult = data.getStringExtra("SCAN_RESULT");
            Log.d("SCAN_RESULT", "result->" + scanResult);
            handler=new Handler(Looper.getMainLooper());
            // 处理扫描结果
            try {
                List<String> adds = Utils.decompressAddr(scanResult);
                if (adds != null && adds.size() > 1) {
                    port = Integer.parseInt(adds.get(0), 16);
                    String tempIp = null;
                    Message msg = new Message();
                    Gson gson = new Gson();
                    String msgJson = gson.toJson(msg);
                    for (int i = 1; i < adds.size(); i++) {
                        tempIp = "http://" + adds.get(i) + ":" + port;
                        testLink(tempIp,msgJson);
                        Log.d("SCAN_RESULT", "ip->" + tempIp);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(R.layout.dialog_loading); // 自定义加载视图
                    builder.setCancelable(false); // 禁止用户手动取消
                    loadingDialog = builder.create();
                    loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 透明背景
                    loadingDialog.show();

                    handler.postDelayed(()->{
                        if (port!=0) return;
                        if (loadingDialog != null&&loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }
                        textView.setText("未能连接到设备，请检查是网络环境");
                    }, 5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "出错了！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNotificationListenerEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                "enabled_notification_listeners");
        return flat != null && flat.contains(pkgName);
    }

    private void loadNotiListener(Context context) {
        if (isNotificationListenerEnabled(context)){
            ComponentName thisComponent = new ComponentName(this, NotificationService.class);
            PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(thisComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(thisComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            Log.d(TAG, "loadNotiListener!");
        }else{
            Log.d(TAG, "isNotificationListenerEnabled: false");
        }

    }

    private void testLink(String url,String msg) {
        executor.executeJob(() -> {
          if (Utils.sendMsg(url,msg)) {
             serviceAddr.setIpAddress(url);
              runOnUiThread(() -> {
                   if (loadingDialog != null&&loadingDialog.isShowing()) {
                       handler.postDelayed(()->{
                          loadingDialog.dismiss();
                          Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                           textView.setText("已连接到："+ url);
                       },1000);
               }
           });
        }
    });
    }
}