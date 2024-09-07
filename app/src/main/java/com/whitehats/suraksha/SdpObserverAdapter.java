package com.whitehats.suraksha;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import android.util.Log;

public class SdpObserverAdapter implements SdpObserver {

    private static final String TAG = "SdpObserverAdapter";

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, "onCreateSuccess: SDP creation successful");
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "onSetSuccess: SDP set successfully");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, "onCreateFailure: SDP creation failed. Error: " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e(TAG, "onSetFailure: SDP set failed. Error: " + s);
    }
}
