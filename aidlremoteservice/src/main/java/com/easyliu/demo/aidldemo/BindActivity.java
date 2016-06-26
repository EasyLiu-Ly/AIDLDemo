package com.easyliu.demo.aidldemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.easyliu.demo.aidl.IRemoteService;
import com.easyliu.demo.aidl.IRemoteServiceCallback;
import com.easyliu.demo.aidl.ISecondary;

public class BindActivity extends AppCompatActivity {
    IRemoteService mService = null;
    ISecondary mSecondaryService = null;
    Button mKillButton;
    TextView mCallbackText;
    private boolean mIsBound;
    private static final int BUMP_MSG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_service_binding);
        Button button = (Button) findViewById(R.id.bind);
        button.setOnClickListener(mBindListener);
        button = (Button) findViewById(R.id.unbind);
        button.setOnClickListener(mUnbindListener);
        mKillButton = (Button) findViewById(R.id.kill);
        mKillButton.setOnClickListener(mKillListener);
        mKillButton.setEnabled(false);
        mCallbackText = (TextView) findViewById(R.id.callback);
        mCallbackText.setText("Not attached.");
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUMP_MSG:
                    mCallbackText.setText("Received from service: " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = IRemoteService.Stub.asInterface(service);
            mKillButton.setEnabled(true);
            mCallbackText.setText("Attached.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            Toast.makeText(BindActivity.this, R.string.remote_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mKillButton.setEnabled(false);
            mCallbackText.setText("Disconnected.");

            // As part of the sample, tell the user what happened.
            Toast.makeText(BindActivity.this, R.string.remote_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Class for interacting with the secondary interface of the service.
     */
    private ServiceConnection mSecondaryConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // Connecting to a secondary interface is the same as any
            // other interface.
            mSecondaryService = ISecondary.Stub.asInterface(service);
            mKillButton.setEnabled(true);
        }

        public void onServiceDisconnected(ComponentName className) {
            mSecondaryService = null;
            mKillButton.setEnabled(false);
        }
    };
    /**
     * 绑定
     */
    private View.OnClickListener mBindListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent=new Intent(IRemoteService.class.getName());
            intent.setPackage("com.easyliu.demo.aidldemo");
            //注意这里的Context.BIND_AUTO_CREATE，这意味这如果在绑定的过程中，
            //如果Service由于某种原因被Destroy了，Android还会自动重新启动被绑定的Service。
            // 你可以点击Kill Process 杀死Service看看结果
            bindService(intent,
                    mConnection, Context.BIND_AUTO_CREATE);
            intent=new Intent(ISecondary.class.getName());
            intent.setPackage("com.easyliu.demo.aidldemo");
            bindService(intent,
                    mSecondaryConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            mCallbackText.setText("Binding.");
        }
    };
    /**
     * 解除绑定
     */
    private View.OnClickListener mUnbindListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mIsBound) {
                if (mService != null) {
                    try {
                        mService.unregisterCallback(mCallback);
                    } catch (RemoteException e) {
                        // There is nothing special we need to do if the service
                        // has crashed.
                    }
                }
                // Detach our existing connection.
                unbindService(mConnection);
                unbindService(mSecondaryConnection);
                mKillButton.setEnabled(false);
                mIsBound = false;
                mCallbackText.setText("Unbinding.");
            }
        }
    };

    private View.OnClickListener mKillListener = new View.OnClickListener() {
        public void onClick(View v) {
            // To kill the process hosting our service, we need to know its
            // PID.  Conveniently our service has a call that will return
            // to us that information.
            if (mSecondaryService != null) {
                try {
                    int pid = mSecondaryService.getPid();
                    // Note that, though this API allows us to request to
                    // kill any process based on its PID, the kernel will
                    // still impose standard restrictions on which PIDs you
                    // are actually able to kill.  Typically this means only
                    // the process running your application and any additional
                    // processes created by that app as shown here; packages
                    // sharing a common UID will also be able to kill each
                    // other's processes.
                    Process.killProcess(pid);
                    mCallbackText.setText("Killed service process.");
                } catch (RemoteException ex) {
                    // Recover gracefully from the process hosting the
                    // server dying.
                    // Just for purposes of the sample, put up a notification.
                    Toast.makeText(BindActivity.this,
                            R.string.remote_call_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    /**
     * 远程回调接口实现
     */
    private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */
        public void valueChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0));
        }
    };
}
