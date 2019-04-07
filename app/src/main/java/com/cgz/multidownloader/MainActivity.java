package com.cgz.multidownloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public EditText mEtPath;
    public EditText mEtThreadCount;
    public LinearLayout mLLContainer;
    public Button mBtSelf;
    public Button mBtOther;
    /**
     * 文件的下载地址
     */
    public  String path = "http://192.168.102.115:8080/Day10/flash.dmg";
    /**
     * 运行状态线程数
     */
    private static int runningThreadCount = 0;
    /**
     * 总的线程数
     */
    public int totalThreadCount = 3;
    /**
     * ProgressBar的集合
     */
    private ArrayList<ProgressBar> mPbs;
    /**
     * 当前app的缓存目录
     */
    private String CACHE_DIR;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 1.找到控件
        // 2.在button点击事件当中下载文件
        // 3.下载的时候在界面显示下载的进度
        mEtPath = findViewById(R.id.et_path);
        mEtThreadCount = findViewById(R.id.et_threadcount);
        mLLContainer = findViewById(R.id.ll_container);
        mBtSelf =  findViewById(R.id.bt_self);
        mBtOther =  findViewById(R.id.bt_other);

        mBtOther.setOnClickListener(this);
        mBtSelf.setOnClickListener(this);

        // 初始化缓存目录路径
        CACHE_DIR = this.getCacheDir().getAbsoluteFile() + "/";// 注意加上斜杠，否则创建载包名下
    }

    @Override
    public void onClick(View v) {
        // 判断点击方式
        switch (v.getId()) {
            case R.id.bt_self:
                downloadBySelf();
                break;
            case R.id.bt_other:

                break;


        }
    }

    /**
     * 通过自己自定义的多线程代码下载网络文件
     */
    private void downloadBySelf() {
        // 设定界面参数
        path = mEtPath.getText().toString().trim();
        totalThreadCount = Integer.valueOf(mEtThreadCount.getText().toString().trim());
        mLLContainer.removeAllViews();// 移除上次添加的view
        mPbs = new ArrayList<>(); //保存ProgressBar集合
        for (int i = 0; i < totalThreadCount; i++) {
            // 有几个线程就添加几个progressbar
            ProgressBar pb = (ProgressBar) View.inflate(this, R.layout.pb, null);
            mLLContainer.addView(pb);
            mPbs.add(pb);
        }

        // 子线程网络访问
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        int length = conn.getContentLength();
                        System.out.println("file length：" + length);
                        RandomAccessFile raf = new RandomAccessFile(CACHE_DIR + getDownloadFileName(path), "rw");
                        // 创建一个空的文件并设置它的长度等于服务器上的文件的长度
                        raf.setLength(length);
                        raf.close();

                        int blockSize = length / totalThreadCount;
                        System.out.println("every block size：" + blockSize);

                        runningThreadCount = totalThreadCount;

                        for (int threadId = 0; threadId < totalThreadCount; threadId++) {
                            int startPosition = threadId * blockSize;
                            int endPosition = (threadId + 1) * blockSize - 1;
                            if (threadId == (totalThreadCount - 1)) {
                                endPosition = length - 1;
                            }
                            new DownloadThread(threadId, startPosition, endPosition).start();
                        }


                    } else {
                        System.out.println("download error , code = " + code);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();


    }

    /**
     * 从网络路径获取文件名
     *
     * @param path 网络路径
     * @return 文件名
     */
    private static String getDownloadFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    /**
     * 下载文件线程
     */

    private class DownloadThread extends Thread {
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
        /**
         * 当前线程需要下载的总共字节数
         */
        private int threadTotal;
        /**
         * 上次该线程下载了多少个字节数
         */
        private int lastDownloadTotalSize;

        public DownloadThread(int threadId, int startPosition, int endPosition) {
            this.threadId = threadId;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.threadTotal = endPosition - startPosition;
            mPbs.get(threadId).setMax(threadTotal);// 设置进度条下载最大字节数

        }

        @Override
        public void run() {
            System.out.println("thread:" + threadId + "begin working");

            try {
                File finfo = new File(CACHE_DIR + totalThreadCount + getDownloadFileName(path) + threadId + ".txt");
                if (finfo.exists() && finfo.length() > 0) {
                    FileInputStream fis = new FileInputStream(finfo);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String lastPosition = br.readLine();
                    // 这里计算出来的是表示上次改线程下载了多少个字节总数
                    lastDownloadTotalSize = Integer.parseInt(lastPosition) - startPosition;
                    startPosition = Integer.parseInt(lastPosition);
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
                    RandomAccessFile raf = new RandomAccessFile(CACHE_DIR + getDownloadFileName(path), "rw");

                    raf.seek(startPosition);

                    int len = 0;
                    byte[] buffer = new byte[1024];
                    int total = 0; // downloaded data of current thread in this time
                    while ((len = is.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);

                        total += len;
                        RandomAccessFile inforaf = new RandomAccessFile(CACHE_DIR + totalThreadCount + getDownloadFileName(path) + threadId + ".txt", "rwd");
                        inforaf.write(String.valueOf(startPosition + total).getBytes());
                        inforaf.close();
                        mPbs.get(threadId).setProgress(total + lastDownloadTotalSize);//设置进度条下载进度

                    }
                    is.close();
                    raf.close();
                    System.out.println("thread:" + threadId + "download complete.....");
                } else {
                    System.out.println("request download failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                synchronized (MainActivity.class) {
                    runningThreadCount--;
                    if (runningThreadCount <= 0) {
                        System.out.println("multi thread download complete...");
                        for (int i = 0; i < totalThreadCount; i++) {
                            File finfo = new File(CACHE_DIR + totalThreadCount + getDownloadFileName(path) + i + ".txt");
                            System.out.println(finfo.delete()); //下载完成后删除中间记录文件
                        }
                    }
                }
            }
        }
    }
}
