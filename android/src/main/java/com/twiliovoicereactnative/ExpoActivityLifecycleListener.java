package com.twiliovoicereactnative;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import expo.modules.core.interfaces.ReactActivityLifecycleListener;

/**
 * Expo Activity Lifecycle Listener for Twilio Voice React Native
 * This class hooks into the Android Activity lifecycle events and delegates them to VoiceActivityProxy
 */
public class ExpoActivityLifecycleListener implements ReactActivityLifecycleListener {
    private VoiceActivityProxy voiceActivityProxy;

    @Override
    public void onCreate(Activity activity, Bundle savedInstanceState) {
        // Initialize the VoiceActivityProxy with the activity context
        this.voiceActivityProxy = new VoiceActivityProxy(activity, permission -> {
            // Simple implementation of the permission rationale notifier
            // In a real app, you might want to show a dialog explaining why the permission is needed
        });
        this.voiceActivityProxy.onCreate(savedInstanceState);
    }

    @Override
    public boolean onNewIntent(Intent intent) {
        if (this.voiceActivityProxy != null) {
            this.voiceActivityProxy.onNewIntent(intent);
        }
        return false; // Return false to allow other listeners to process the intent
    }

    @Override
    public void onDestroy(Activity activity) {
        if (this.voiceActivityProxy != null) {
            this.voiceActivityProxy.onDestroy();
        }
    }
}
