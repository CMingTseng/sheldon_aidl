package com.example.remoteserver;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class RemoteService extends Service {
    public static final String TAG = "RemoteService";
    private List<Entity> data = new ArrayList<Entity>();
    boolean pthreadState = false;
    int mStartMode;       // indicates how to behave if the service is killed
    final RemoteCallbackList<ITVCallback> remoteCallbackList = new RemoteCallbackList<>();

    /*-----------------------------------------------------------------------------
     Function Name: onCreate
     Input		:
     Output		:
     Return 	:
     Describe	:
     -------------------------------------------------------------------------------*/
    public void onCreate() {
        // Used to load the 'native-lib' library on application startup.
        System.loadLibrary("RemoteServiceJNI");
        pthreadState = true;
        //DataThread datathread = new DataThread();
        //datathread.start();
        Nano_Printf("service onCreate");

        Nano_Printf(String.format("<%s>",stringFromJNI()));
        // mMyActivity = getActivity();
    }

    /*-----------------------------------------------------------------------------
     Function Name: onStartCommand
     Input		:
     Output		:
     Return 		:
     Describe		:
     -------------------------------------------------------------------------------*/
    public int onStartCommand(Intent intent, int flags, int startId) {
        Nano_Printf("service onStartCommand");
        return mStartMode;
    }

    /*-----------------------------------------------------------------------------
     Function Name: onBind
     Input		:
     Output		:
     Return 		:
     Describe		:
     -------------------------------------------------------------------------------*/
    public IBinder onBind(Intent intent) {
        Nano_Printf("service on bind,intent = %s",intent.toString());
        return binder;
    }

    /*-----------------------------------------------------------------------------
     Function Name: onDestroy
     Input		    :
     Output		    :
     Return 		:
     Describe		:
     -------------------------------------------------------------------------------*/
    public void onDestroy() {
        Nano_Printf("service onDestroy");
        pthreadState = false;
        // 取消掉所有的回调
        remoteCallbackList.kill();
    }

    private void Nano_Printf(String...args) {
        String str = "";
        for(int i = 0; i < args.length; i++){
            str +=  args[i];
            if( i != args.length - 1){
                str += ", ";
            }
        }
        Log.d(TAG, str);
    }

    /*-----------------------------------------------------------------------------
    Function Name : Nano_Notify
    Input		  :
    Output		  :
    Return 		  :
    Describe	  :
    -------------------------------------------------------------------------------*/
    private void Nano_Notify(String name)
    {
        if(remoteCallbackList == null) {
            Nano_Printf("remoteCallbackList is null");
            return;
        }
        final int len = remoteCallbackList.beginBroadcast();
        //Nano_Printf("client num "+len);
        for (int i = 0; i < len; i++) {
            try {
                remoteCallbackList.getBroadcastItem(i).onSuccess(name);    // 通知回调
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        remoteCallbackList.finishBroadcast();
    }

    /*-----------------------------------------------------------------------------
    Function Name:
    Input		:
    Output		:
    Return 		:
    Describe		:
    -------------------------------------------------------------------------------*/
    private void Nano_VoiceEvent(byte[] data,int datalen)
    {
        if(remoteCallbackList == null) {
            Nano_Printf("remoteCallbackList is null");
            return;
        }
        final int len = remoteCallbackList.beginBroadcast();
        //Nano_Printf("len "+len);
        for (int i = 0; i < len; i++) {
            try {
                remoteCallbackList.getBroadcastItem(i).VoiceEvent(data,datalen);    // 通知回调
                Nano_Printf(String.format(" voice event %d",datalen));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        remoteCallbackList.finishBroadcast();
    }

    /*-----------------------------------------------------------------------------
    Function Name:
    Input		:
    Output		:
    Return 		:
    Describe		:
    -------------------------------------------------------------------------------*/
    private void Nano_KeyEvent(int keycode)
    {
        if(remoteCallbackList == null) {
            Nano_Printf("remoteCallbackList is null");
            return;
        }
        final int len = remoteCallbackList.beginBroadcast();
        //Nano_Printf("len "+len);
        for (int i = 0; i < len; i++) {
            try {
                Nano_Printf(String.format(" key event "));
                remoteCallbackList.getBroadcastItem(i).KeyEvent(keycode);    // 通知回调
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        remoteCallbackList.finishBroadcast();
    }

    /**
     * 以小端模式将byte[]/char转成int
     */
    public static int bytesToIntLittle(byte[] src,int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }
    /**
     * 以大端模式将byte[]/char转成int
     */
    public static int bytesToIntBig(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }

    /*-----------------------------------------------------------------------------
    Function Name:   class DataThread
    Input		:
    Output		:
    Return 		:
    Describe	:创建数据线程，处理语音数据和按键事件
    -------------------------------------------------------------------------------*/
    private class DataThread extends Thread{  // 在线程的run()中进行处理
        @Override
        public void run() {
            Nano_Printf("service Run1");
            NanoOpen();

            try {
                while(pthreadState)
                {
                    int  type = -1;
                    byte appbuf[] = new byte[2048];
                    type = NanoPollEvent(appbuf,appbuf.length);
                    if(type == 4) {         /*按键事件*/
                        Nano_KeyEvent(bytesToIntLittle(appbuf,0));
                    }else if(type > 4) {   /*语音事件*/
                        Nano_VoiceEvent(appbuf,640);
                    }
                    Thread.sleep(1); /*毫秒单位*/
                }
                Nano_Printf("service Exit");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private final IRemoteService.Stub binder = new IRemoteService.Stub() {

        @Override
        public void doSomeThing(int anInt, String aString) throws RemoteException {
            Log.i(TAG, String.format("rcv:%s, %s", anInt, aString));
        }

        @Override
        public void addEntity(Entity entity) throws RemoteException {
            Log.i(TAG, String.format("rcv:entity = %s", entity));
            data.add(entity);
        }

        @Override
        public List<Entity> getEntity() throws RemoteException {
            Log.i(TAG, String.format("get:List<Entity> = %s", data));
            return data;
        }

        public void setEntity(int index, Entity entity) throws RemoteException {
            Log.i(TAG, String.format("set:entity[%d] = %s", index, entity));
            data.set(index, entity);
        }

        @Override
        public void asyncCallSomeone(String para, ITVCallback callback) throws RemoteException {

            Log.i(TAG, String.format("asyncCallSomeone..."));

            //remoteCallbackList.register(callback);

            final int len = remoteCallbackList.beginBroadcast();
            for (int i = 0; i < len; i++) {
                remoteCallbackList.getBroadcastItem(i).onSuccess(para + "_callbck");
            }
            remoteCallbackList.finishBroadcast();
        }

        /*提供registerActivity方法*/
        public void registerActivity() throws RemoteException {
            Nano_Printf("registerActivity");
            // mMyActivity = MyActivity;
        }

        /*提供registerCallBack方法*/
        public void registerCallBack(ITVCallback callback) throws RemoteException {
            Nano_Printf("registerCallBack");
            remoteCallbackList.register(callback);
        }

        /*提供unregisterCallBack方法*/
        public void unregisterCallBack(ITVCallback callback) throws RemoteException {
            Nano_Printf("unregisterCallBack");
            remoteCallbackList.unregister(callback);
        }

        public boolean setSpeakerOn(boolean state)    throws RemoteException {
            Nano_Printf("setSpeakerOn");
            NanosetSpeakerOn(state);
            return true;
        }

        public boolean dialCall(String phoneNumber)    throws RemoteException {
            Nano_Printf("dialCall");
            NanodialCall(phoneNumber);
            return true;
        }

        public boolean incomingCall(String phoneNumber)    throws RemoteException {
            Nano_Printf("incomingCall");
            NanoincomingCall(phoneNumber);
            return true;
        }

        public boolean answerCall(String phoneNumber)    throws RemoteException {
            Nano_Printf("answerCall");
            NanoanswerCall(phoneNumber);
            return true;
        }

        public boolean hangupCall(String phoneNumber)   throws RemoteException {
            Nano_Printf("hangupCall");
            NanohangupCall(phoneNumber);
            return true;
        }
    };


    /*nanosic : native interface*/
    public native String   stringFromJNI();
    public native int      NanoOpen();
    public native int      NanoPollEvent(byte[] buf,int size);
    public native boolean  NanosetSpeakerOn(boolean state);
    public native boolean  NanodialCall(String phoneNumber);
    public native boolean  NanoincomingCall(String phoneNumber);
    public native boolean  NanoanswerCall(String phoneNumber);
    public native boolean  NanohangupCall(String phoneNumber);
}
