package com.cgz.lib;

import com.sun.jndi.toolkit.url.UrlUtil;

import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

public class MutiDownloader {
    public static String path = "http://192.168.102.115:8080/Day10/WebStorm.dmg";
    public static final int TOAL_THREAD_COUNT = 3;

    public static void main(String[] args) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code == 200) {
                int length = conn.getContentLength();
                System.out.println("file legth" + length);
                RandomAccessFile raf = new RandomAccessFile(getDownloadFileName(path), "rw");
                // 创建一个空的文件并设置它的长度等于服务器上的文件的长度
                raf.setLength(length);
                raf.close();

                 int blockSize = length / TOAL_THREAD_COUNT;
                 System.out.println("every block size" + blockSize);



            } else {
                System.out.println("download error , code = "+ code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从网络路径获取文件名
     *
     * @param path
     *      网络路径
     * @return  文件名
     */
    private static String getDownloadFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
