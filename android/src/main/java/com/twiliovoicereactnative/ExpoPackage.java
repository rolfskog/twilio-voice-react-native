
package com.twiliovoicereactnative;

import android.content.Context;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import expo.modules.core.interfaces.Package;
import expo.modules.core.interfaces.ReactActivityLifecycleListener;
import expo.modules.core.interfaces.ApplicationLifecycleListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExpoPackage implements Package, ReactPackage {
  @Override
  public List<? extends ReactActivityLifecycleListener> createReactActivityLifecycleListeners(Context activityContext) {
    return Collections.singletonList(new ExpoActivityLifecycleListener());
  }

  @Override
  public List<? extends ApplicationLifecycleListener> createApplicationLifecycleListeners(Context applicationContext) {
    return Collections.singletonList(new ExpoApplicationLifecycleListener());
  }
  
  @Override
  public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<>();
    modules.add(new ExpoModule(reactContext));
    return modules;
  }

  @Override
  public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
    return Collections.emptyList();
  }
}