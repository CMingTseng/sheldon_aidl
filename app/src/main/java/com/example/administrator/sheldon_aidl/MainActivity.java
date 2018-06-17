package com.example.administrator.sheldon_aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.nfc.Tag;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.remoteserver.Entity;
import com.example.remoteserver.ITVCallback;
import com.example.remoteserver.IRemoteService;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean mBound = false;
    private IRemoteService iRemoteService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         *注册回调
         */
        findViewById(R.id.registerCallback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) {
                    alert("未连接到远程服务");
                    return;
                }
                try {
                    Entity entity = new Entity(100, "sheldon");
                    if (iRemoteService != null){
                        iRemoteService.addEntity(entity);

                        iRemoteService.registerCallBack(mCallback);
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         *注销回调
         */
        findViewById(R.id.unregisterCallback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) {
                    alert("未连接到远程服务");
                    return;
                }
                if (iRemoteService != null) {
                    try {
                        List<Entity> entityList = iRemoteService.getEntity();

                        StringBuilder sb = new StringBuilder("当前数量:" + entityList.size() + "\r\n");
                        for (int i = 0; i < entityList.size(); i++) {
                            sb.append(i + ": ");
                            sb.append(entityList.get(i) == null ? "" : entityList.get(i).toString());
                            sb.append("\n");
                        }
                        alert(sb.toString());

                        iRemoteService.unregisterCallBack(mCallback);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /**
         *通知手柄切换通道按钮
         */
        findViewById(R.id.setSpeakerOn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) {
                    alert("未连接到远程服务");
                    return;
                }

                if (iRemoteService != null) {
                    try {
                        //test------
                        List<Entity> entityList = iRemoteService.getEntity();
                        int pos = 1;
                        if(entityList.size()>pos){
                            entityList.get(pos).setAge(1314);
                            entityList.get(pos).setName("li");
                            iRemoteService.setEntity(pos,entityList.get(pos));
                        }
                        //---------

                        iRemoteService.setSpeakerOn(true);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /**
         *通知手柄发起呼叫按钮
         */
        findViewById(R.id.dialCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) {
                    alert("未连接到远程服务");
                    return;
                }

                if (iRemoteService != null) {
                    try {
                        //test------
                        final String para = "canshu";
                        iRemoteService.asyncCallSomeone(para, mCallback);
                        //-------

                        iRemoteService.dialCall("11111111111");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /**
         *通知手柄来电振铃
         */
        findViewById(R.id.incomingCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) {
                    alert("未连接到远程服务");
                    return;
                }
                if (iRemoteService != null) {
                    try {
                        iRemoteService.incomingCall("22222222222");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        /**
         *通知手柄接听来电
         */
        findViewById(R.id.answerCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) {
                    alert("未连接到远程服务");
                    return;
                }
                if (iRemoteService != null) {
                    try {
                        iRemoteService.answerCall("33333333333");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        /**
         *通知手柄挂断通话
         */
        findViewById(R.id.hangupCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) {
                    alert("未连接到远程服务");
                    return;
                }
                if (iRemoteService != null) {
                    try {
                        iRemoteService.hangupCall("44444444444");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }

            }
        });
    }

    private void alert(String str) {
        //解决在子线程中调用Toast的异常情况处理(还是有异常)
        //Looper.prepare();
        Toast.makeText(this, str, 0).show();
        //Looper.loop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
            attemptToBindService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    /**
     * 尝试与服务端建立连接
     */
    private void attemptToBindService() {
        Intent intent = new Intent();
        intent.setAction("com.example.REMOTE.myserver");
        intent.setPackage("com.example.remoteserver");
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(getLocalClassName(), "service connected");
            iRemoteService = IRemoteService.Stub.asInterface(service);

            mBound = true;

            if (iRemoteService != null) {
                try {
                    iRemoteService.doSomeThing(0, "anything string");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(getLocalClassName(), "service disconnected");
            mBound = false;
        }
    };

    /**
     * 添加回调到服务端
     */
    private ITVCallback mCallback = new ITVCallback.Stub() {
        public boolean setTVSpeakerOn(boolean state) throws RemoteException {
            Log.d("nano-client ", String.format("setSpeakerOnCallback"));
            return state;
        }

        public boolean answerTVCall() throws RemoteException {
            Log.d("nano-client ", String.format("answerCallCallback"));
            return true;
        }

        public boolean hangupTVCall() throws RemoteException {
            Log.d("nano-client ", String.format("hangupCallCallback"));
            return true;
        }

        public boolean dialTVCall(String phoneNumber) throws RemoteException {
            Log.d("nano-client ", String.format("dialCallCallback"));
            return true;
        }

        @Override
        public void onSuccess(String aString) throws RemoteException {
            Log.d("nano-client ", String.format("service arrived %s",aString));
            alert(String.format("回调: %s", aString));
        }
        /**
         * 语音数据回调函数
         */
        public void VoiceEvent(byte[] data,int datalen) throws RemoteException
        {

            //Nano_Printf(String.format("got voice len %d", datalen));
            //Log.d("nano-client ", String.format("%s", PrintHexString(data)));
            //wav_save(data,0,datalen);
            //textView1.setText(String.format("%s", PrintHexString(data)));

        }
        /**
         * 按键事件函数
         */
        public void KeyEvent(int keycode) throws RemoteException {
            Log.i("nano-client ",String.format("got keycode %x", keycode));
            //textView1.setText(String.format("got keycode %x", keycode));
        }
    };
}
