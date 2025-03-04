package com.gsyt.noti2win;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author：
 * @Description： 线程池任务 主线程任务
 * Thread safe
 **/
public class AndroidExecutors {
    private ExecutorService threadPool = Executors.newFixedThreadPool(8);

    private AndroidExecutors() {

    }
    public static AndroidExecutors INSTANCE;
    public static AndroidExecutors getInstance() {
        if (INSTANCE == null) {
            synchronized (AndroidExecutors.class) {
                //double check
                if (INSTANCE == null) {
                    INSTANCE = new AndroidExecutors();
                }
            }
            INSTANCE = new AndroidExecutors();
        }
        return INSTANCE;
    }


    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void executeJob(Runnable job) {
        this.INSTANCE.getThreadPool().execute(job);

    }


}
