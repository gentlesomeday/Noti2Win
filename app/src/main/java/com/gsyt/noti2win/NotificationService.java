package com.gsyt.noti2win;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationService extends NotificationListenerService {

    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";

    private static final String MOBILEQQ_PACKAGE_NAME = "com.tencent.mobileqq";

    private ServiceAddr serviceAddr = ServiceAddr.getInstance();
    private AndroidExecutors executors=AndroidExecutors.getInstance();
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        //通知来源包名
        String packageName = sbn.getPackageName();
        String notificationTitle = sbn.getNotification().extras.getString("android.title");
        String notificationText = sbn.getNotification().extras.getString("android.text");
        // 判断是否为微信的通知
        Message message = null;
        if (WECHAT_PACKAGE_NAME.equals(packageName)) {
            // 获取通知的标题和内容
            message = new Message(0, notificationTitle, notificationText);
        } else if (MOBILEQQ_PACKAGE_NAME.equals(packageName)) {
            message = new Message(1, notificationTitle, notificationText);
        }
        if (message == null) return;
        Gson gson = new Gson();
        String msgJson = gson.toJson(message);
        String ipAddress = serviceAddr.getIpAddress();
        if (ipAddress == null) {
            Toast.makeText(this, "请先设置服务器地址", Toast.LENGTH_LONG).show();
            return;
        }
        executors.executeJob(() -> {
            Utils.sendMsg(ipAddress, msgJson);
        });
        Log.d("TAGAAA", "onNotificationPosted->"+notificationTitle);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    @Override
    public StatusBarNotification[] getActiveNotifications() {
        return super.getActiveNotifications();
    }
}
