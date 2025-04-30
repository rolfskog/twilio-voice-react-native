package com.twiliovoicereactnative;

import android.view.View;

import expo.modules.kotlin.AppContext;
import expo.modules.kotlin.views.ExpoView;
import expo.modules.core.interfaces.Package;

import java.util.Collections;
import java.util.List;

/**
 * Expo Package for Twilio Voice React Native
 * This class registers the Expo Module and Lifecycle Listeners
 */
public class ExpoPackage implements Package {
    @Override
    public List<Class> createExportedModules() {
        return Collections.singletonList(ExpoModule.class);
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
