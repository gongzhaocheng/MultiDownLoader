package com.cgz.lib;

import com.sun.jndi.toolkit.url.UrlUtil;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;
import java.nio.file.Path;

import sun.security.util.Length;

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
                System.out.println("file legth：" + length);
                RandomAccessFile raf = new RandomAccessFile(getDownloadFileName(path), "rw");
                // 创建一个空的文件并设置它的长度等于服务器上的文件的长度
                raf.setLength(length);
                raf.close();

                 int blockSize = length / TOAL_THREAD_COUNT;
                 System.out.println("every block size：" + blockSize);
                    for (int threaId = 0; threaId < TOAL_THREAD_COUNT; threaId++) {
                        int startPosition = threaId * blockSize;
                        int endPosition = (threaId + 1) * blockSize -1;
                        if (threaId == (TOAL_THREAD_COUNT - 1)){
                            endPosition = length - 1;
                        }
                        new DownloadThread(threaId,startPosition,endPosition).start();
                    }


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

    private static class DownloadThread extends Thread{

        private final int threaId;
        private final int startPosition;
        private final int endPosition;

        public DownloadThread(int threaId, int startPosition, int endPosition) {
            this.threaId = threaId;
            this.startPosition = startPosition;
            this.endPosition = endPosition;

        }

        @Override
        public void run() {
            try {
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                System.out.println("begin and end:" + threaId +
                        "range of download:" + startPosition +
                        "~~~" + endPosition);
                conn.setRequestProperty("Range", "bytes" +
                        startPosition + "-" + endPosition);
                int code = conn.getResponseCode();
                if (code == 206) {
                    InputStream is = conn.getInputStream();
                    RandomAccessFile raf = new RandomAccessFile(getDownloadFileName(path), "rw");

                    raf.seek(startPosition);

                    int len = 0;
                    byte[] buffer = new byte[1024];
                    int total = 0; // downloaded data of current thread in this times
                    while ((len = is.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        total += len;

                    }
                    is.close();
                    raf.close();
                    System.out.println("thread:" + threaId +
                            "download complete.....");
                } else {
                    System.out.println("request download failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
