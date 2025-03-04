package com.gsyt.noti2win;

/**
 * @Author:
 * @Description：
 **/
public class ServiceAddr {
    private String ipAddress = null;
    private static ServiceAddr INSTANCE = null;

    private ServiceAddr() {}
    public static ServiceAddr getInstance() {
        if (INSTANCE == null) {
            synchronized (ServiceAddr.class) {
                //double check
                if (INSTANCE == null) {
                    INSTANCE = new ServiceAddr();
                }
            }
            INSTANCE = new ServiceAddr();
        }
        return INSTANCE;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
