package com.gsyt.noti2win;

import android.app.Notification;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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
       // Bitmap picture = getNotificationImage(sbn.getNotification().extras);
        Bitmap avatar = getWeChatAvatar(sbn.getNotification(), sbn.getNotification().extras);

        // Bitmap largeIcon =  sbn.getNotification().extras.getParcelable(Notification.EXTRA_LARGE_ICON);
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
    /**
     * 获取微信头像
     */
    private Bitmap getWeChatAvatar(Notification notification, Bundle extras) {
        // 可能的存储位置
        Bitmap avatar =null;
//        if (avatar == null) {
//            avatar = extras.getParcelable(Notification.EXTRA_SMALL_ICON);
//        }

        // 另一种方式：获取 RemoteViews 中的图片（复杂）
        if (avatar == null) {
            avatar = extractIconFromRemoteViews(notification);
        }
        return avatar;
    }

    /**
     * 解析 RemoteViews 获取头像（可能无效，微信 UI 可能变更）
     */
    private Bitmap extractIconFromRemoteViews(Notification notification) {
        try {
            RemoteViews views = notification.contentView;
            if (views == null) return null;

            Field field = views.getClass().getDeclaredField("mActions");
            field.setAccessible(true);
            List<Object> actions = (List<Object>) field.get(views);

            for (Object action : actions) {
                Field typeField = action.getClass().getDeclaredField("viewId");
                typeField.setAccessible(true);
                int viewId = typeField.getInt(action);

                Field valueField = action.getClass().getDeclaredField("value");
                valueField.setAccessible(true);
                Object value = valueField.get(action);

                if (value instanceof Bitmap) {
                    return (Bitmap) value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
