package com.cgz.lib;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.nio.file.Path;


public class MultiDownloader {

    public static final int TOTAL_THREAD_COUNT = 3;
    public static String path = "http://192.168.102.115:8080/Day10/flash.dmg";

    public static void main(String[] args) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code == 200) {
                int length = conn.getContentLength();
                System.out.println("file length：" + length);
                RandomAccessFile raf = new RandomAccessFile(getDownloadFileName(path), "rw");
                // 创建一个空的文件并设置它的长度等于服务器上的文件的长度
                raf.setLength(length);
                raf.close();

                int blockSize = length / TOTAL_THREAD_COUNT;
                 System.out.println("every block size：" + blockSize);
                for (int threadId = 0; threadId < TOTAL_THREAD_COUNT; threadId++) {
                    int startPosition = threadId * blockSize;
                    int endPosition = (threadId + 1) * blockSize - 1;
                    if (threadId == (TOTAL_THREAD_COUNT - 1)) {
                        endPosition = length - 1;
                    }
                    new DownloadThread(threadId, startPosition, endPosition).start();
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
        /**
         * 当前线程的ID
         */
        private int threadId;
        /**
         * 当前线程下载的起始位置
         */
        private int startPosition;
        /**
         * 当前线程下载的终止位置
         */
        private int endPosition;

        public DownloadThread(int threadId, int startPosition, int endPosition) {
            this.threadId = threadId;
            this.startPosition = startPosition;
            this.endPosition = endPosition;

        }

        @Override
        public void run() {
            System.out.println("thread:"+ threadId+"begin working");

            try {
                File finfo = new File(TOTAL_THREAD_COUNT + getDownloadFileName(path) + threadId + ".txt");
                if (finfo.exists() && finfo.length() >0){
                    FileInputStream fis = new FileInputStream(finfo);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String lastposition = br.readLine();
                    startPosition = Integer.parseInt(lastposition);
                    fis.close();
                }

                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                System.out.println("begin and end:" + threadId + "range of download:" + startPosition +
                        "~~~" + endPosition);
                conn.setRequestProperty("Range", "bytes=" + startPosition + "-" + endPosition);
                int code = conn.getResponseCode();
                if (code == 206) {
                    InputStream is = conn.getInputStream();
                    RandomAccessFile raf = new RandomAccessFile(getDownloadFileName(path), "rw");

                    raf.seek(startPosition);

                    int len = 0;
                    byte[] buffer = new byte[1024];
                    int total = 0; // downloaded data of current thread in this time
                    while ((len = is.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);

                        total += len;
                        RandomAccessFile inforaf = new RandomAccessFile(TOTAL_THREAD_COUNT + getDownloadFileName(path) + threadId + ".txt", "rwd");
                        inforaf.write(String.valueOf(startPosition+total).getBytes());
                        inforaf.close();

                    }
                    is.close();
                    raf.close();
                    System.out.println("thread:" + threadId + "download complete.....");
                } else {
                    System.out.println("request download failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
