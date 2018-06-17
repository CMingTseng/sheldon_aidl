// Callback.aidl
package com.example.remoteserver;

// Declare any non-default types here with import statements

interface ITVCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onSuccess(String aString);

    /**
     * 手柄通知STB切换通道
     */
    boolean setTVSpeakerOn(boolean state);
    /**
     * 手柄通知STB接听来电
     */
    boolean answerTVCall();
    /**
     * 手柄通知STB挂断通话
     */
    boolean hangupTVCall();

    /**
     * 手柄通知STB发起呼叫
     */
    boolean dialTVCall(String phoneNumber);
    /**
     * 手柄上报DTMF键值
     */
    //boolean sendDtmf(int dtmf);

    void VoiceEvent(in byte[] data,in int datalen);

    void KeyEvent(in int keycode);
}
