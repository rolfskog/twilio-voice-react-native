package com.twiliovoicereactnative;

import android.view.View;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import expo.modules.core.interfaces.Package;
import expo.modules.core.interfaces.ReactNativeHostInterface;

import java.util.Collections;
import java.util.List;

/**
 * Expo Package for Twilio Voice React Native
 * This class registers the Expo Module and Lifecycle Listeners
 */
public class ExpoPackage implements Package, ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Collections.singletonList(new ExpoModule(reactContext));
    }
    
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @Override
    public List<expo.modules.core.interfaces.ReactActivityLifecycleListener> createReactActivityLifecycleListeners() {
        return Collections.singletonList(new ExpoActivityLifecycleListener());
    }

    @Override
    public List<expo.modules.core.interfaces.ReactApplicationLifecycleListener> createReactApplicationLifecycleListeners() {
        return Collections.singletonList(new ExpoApplicationLifecycleListener());
    }
}
