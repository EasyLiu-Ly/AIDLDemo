package com.easyliu.demo.aidlremotedemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG =MainActivity.class.getSimpleName() ;
    private AdditionServiceConnection mServiceConnection;
    private EditText et_mainActivity_inputA;
    private EditText et_mainActivity_inputB;
    private Button btn_mainActivity_add;
    private TextView tv_sum;
    private boolean mIsBound;
    private IRemoteService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
    }

    /**
     * init Views
     */
    private void initViews() {
        et_mainActivity_inputA = (EditText) findViewById(R.id.et_mainActivity_inputA);
        et_mainActivity_inputB = (EditText) findViewById(R.id.et_mainActivity_inputB);
        tv_sum = (TextView) findViewById(R.id.tv_sum);
        btn_mainActivity_add = (Button) findViewById(R.id.btn_mainActivity_add);
        btn_mainActivity_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mService != null) {
                        int sum = mService.add(Integer.parseInt(et_mainActivity_inputA.getText().toString()),
                                Integer.parseInt(et_mainActivity_inputB.getText().toString()));
                        tv_sum.setText(String.valueOf(sum));
                    } else {
                        tv_sum.setText("请先绑定服务!");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * bind service
     */
    private void doBindService() {
        mServiceConnection = new AdditionServiceConnection();
        Intent intent = new Intent(RemoteService.class.getName());
        intent.setPackage("com.easyliu.demo.aidlremotedemo");
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * unbind service
     */
    private void doUnbindService() {
        if (mIsBound) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
            mIsBound = false;
        }
    }

    /**
     * ServiceConection
     */
    class AdditionServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IRemoteService.Stub.asInterface((IBinder) service);
            mIsBound = true;
            try {
                //设置死亡代理
                service.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            tv_sum.setText("Servie Conected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mIsBound = false;
            tv_sum.setText("Servie DisConected!");
        }
    }

    /**
     * 监听Binder是否死亡
     */
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (mService == null) {
                return;
            }
            mService.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mService = null;
            //重新绑定
            doBindService();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
