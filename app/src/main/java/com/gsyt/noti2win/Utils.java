package com.gsyt.noti2win;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
/**
 * @Author: 付仁秀
 * @Description：
 **/
public class Utils {
    public static List<String> decompressAddr(String oriString) {
        if (oriString.isEmpty()||oriString.length()<5){
            return null;
        }else{
            List<String> result = new ArrayList<>();
            String compressedPort = oriString.substring(0, 4);
            String port = decompressHex(compressedPort);
            result.add(port);
            String compressedIp = oriString.substring(4,oriString.length());
            result.addAll(decompressIPs(compressedIp));
            return result;
        }
    }

    public static String decompressHex(String compressed) {
        StringBuilder sb = new StringBuilder();
        String input = "0123456789";
        String map = "zyxwvutsrq";
        for (char c : compressed.toCharArray()) {
            if (map.contains(Character.toString(c))) {
                int index = map.indexOf(c);
                sb.append(input.charAt(index));
            }else{
                sb.append(c);
            }
        }
        return sb.toString();
    }
    public static List<String> decompressIPs(String compressed) {
        if (compressed == null || compressed.isEmpty()) return new ArrayList<>();
        byte[] bytes = Base64.getDecoder().decode(compressed);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        List<Integer> deltas = new ArrayList<>();
        while (byteBuffer.hasRemaining()) {
            deltas.add(byteBuffer.getInt());
        }
        List<Integer> ipInts = new ArrayList<>();
        ipInts.add(deltas.get(0));
        for (int i = 1; i < deltas.size(); i++) {
            ipInts.add(ipInts.get(i - 1) + deltas.get(i));
        }
        List<String> result = new ArrayList<>();
        for (int ipInt : ipInts) {
            result.add(intToIP(ipInt));
        }
        return result;
    }
    public static String intToIP(int value) {
        return (value >> 24 & 0xFF) + "." +
                (value >> 16 & 0xFF) + "." +
                (value >> 8 & 0xFF) + "." +
                (value & 0xFF);
    }

    public static boolean sendMsg(String ipAddress, String msg) {
        try {
            URL url = new URL(ipAddress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(2000);  // 设置连接超时为 5 秒
            connection.setReadTimeout(2000);     // 设置读取超时为 5 秒
            connection.setRequestMethod("POST"); // 设置请求方法为 POST
            connection.setDoOutput(true);        // 允许输出流
            connection.setRequestProperty("Content-Type", "application/json"); // 设置请求头为 JSON 类型
            // 发送字符串数据
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = msg.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String content = readResponse(connection);
                if (content.equals("1")) {
                    return true;
                } else {
                    return false;
                }
            }
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;  // 如果请求失败，认为该 IP 地址不可用
        }
    }

    private static String readResponse(HttpURLConnection connection) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();  // 返回响应内容的字符串
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  // 如果读取失败，返回 null
    }
}
