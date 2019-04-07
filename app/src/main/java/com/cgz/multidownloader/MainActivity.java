package com.cgz.multidownloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public EditText mEtPath;
    public EditText mEtThreadCount;
    public LinearLayout mLLContainer;
    public Button mBtSelf;
    public Button mBtOther;

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


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_self:
                downloadBySelf();
                break;
            case R.id.bt_other:

                break;


        }
    }

    private void downloadBySelf() {

    }
}
