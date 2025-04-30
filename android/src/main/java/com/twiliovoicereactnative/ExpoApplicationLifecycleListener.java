package com.twiliovoicereactnative;

import android.app.Application;
import android.content.Context;

import expo.modules.core.interfaces.ReactApplicationLifecycleListener;

/**
 * Expo Application Lifecycle Listener for Twilio Voice React Native
 * This class hooks into the Android Application lifecycle events and delegates them to VoiceApplicationProxy
 */
public class ExpoApplicationLifecycleListener implements ReactApplicationLifecycleListener {
    private VoiceApplicationProxy voiceApplicationProxy;

    @Override
    public void onCreate(Application application) {
        // Initialize the VoiceApplicationProxy with the application context
        this.voiceApplicationProxy = new VoiceApplicationProxy(application);
        this.voiceApplicationProxy.onCreate();
    }

    @Override
    public void onDestroy() {
        // Clean up resources when the application is destroyed
        if (this.voiceApplicationProxy != null) {
            this.voiceApplicationProxy.onTerminate();
        }
    }
}
