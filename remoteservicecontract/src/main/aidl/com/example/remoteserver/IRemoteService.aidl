// IMyAidlInterface.aidl
package com.example.remoteserver;
import com.example.remoteserver.Entity;
import com.example.remoteserver.ITVCallback;

// Declare any non-default types here with import statements

interface IRemoteService {

    void doSomeThing(int anInt,String aString);

    void addEntity(in Entity entity);

    void setEntity(int index,in Entity entity);

    List<Entity> getEntity();

    void asyncCallSomeone( String para, ITVCallback callback);

    /**
     * 通知手柄切换通道
     */
    boolean setSpeakerOn(boolean state);
    /**
     * 通知手柄发起呼叫
     */
    boolean dialCall(String phoneNumber);
    /**
     * 通知手柄来电振铃
     */
    boolean incomingCall(String phoneNumber);
    /**
     * 通知手柄接听来电
     */
    boolean answerCall(String phoneNumber);
    /**
     * 通知手柄挂断通话
     */
    boolean hangupCall(String phoneNumber);
    /**
     * 注册呼叫相关的回调接口.
     *
     * @param telephoneCallback 呼叫相关的回调接口
     */
    void registerCallBack( ITVCallback callback);
    /**
     * 解除注册呼叫相关的回调接口.
     *
     * @param telephoneCallback 呼叫相关的回调接口
     */
    void unregisterCallBack(ITVCallback telephoneCallback);
    /**
     * 保存Activity对象.
     */
    void registerActivity();
}
