package com.cgz.lib;

import com.sun.jndi.toolkit.url.UrlUtil;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

public class MutiDownloader {
    public static String path = "http://192.168.102.115:8080/Day10/WebStorm.dmg";

    public static void main(String[] args) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code == 200) {
                int length = conn.getContentLength();
                System.out.println("file legth" + length);
            } else {
                System.out.println("download error , code = "+ code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
