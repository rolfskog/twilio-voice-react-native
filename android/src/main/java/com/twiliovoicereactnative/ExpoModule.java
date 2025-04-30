package com.twiliovoicereactnative;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.MessageListener;
import com.twilio.voice.Voice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Expo Module for Twilio Voice React Native
 * This module provides access to the native Twilio Voice SDK for Android
 */
public class ExpoModule extends ReactContextBaseJavaModule {
    private static final String TAG = "TwilioVoiceExpoModule";

    public ExpoModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "TwilioVoiceExpo";
    }

    /**
     * Connect to a Twilio Voice call with the provided access token and parameters
     * @param accessToken The access token for authentication
     * @param params Custom parameters for the call
     * @param notificationDisplayName The notification display name for Android
     * @param promise Promise to resolve with call information
     */
    @ReactMethod
    public void voice_connect(String accessToken, ReadableMap params, String notificationDisplayName, Promise promise) {
        try {
            Context context = getReactApplicationContext();
            if (context == null) {
                promise.reject("TWILIO_VOICE_ERROR", "React context is null");
                return;
            }

            VoiceApplicationProxy voiceApplicationProxy = VoiceApplicationProxy.getInstance();
            if (voiceApplicationProxy == null) {
                promise.reject("TWILIO_VOICE_ERROR", "Voice application proxy is null");
                return;
            }

            CallRecordDatabase callRecordDatabase = voiceApplicationProxy.getCallRecordDatabase();
            String uuid = UUID.randomUUID().toString();
            CallRecordDatabase.CallRecord callRecord = callRecordDatabase.create(uuid);

            // Convert ReadableMap to HashMap
            HashMap<String, String> twimlParams = new HashMap<>();
            ReadableMapUtils.toMap(params, twimlParams);

            ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(accessToken)
                    .params(twimlParams);

            if (notificationDisplayName != null && !notificationDisplayName.isEmpty()) {
                connectOptionsBuilder.displayName(notificationDisplayName);
            }

            ConnectOptions connectOptions = connectOptionsBuilder.build();
            Call call = Voice.connect(context, connectOptions, new Call.Listener() {
                @Override
                public void onConnectFailure(Call call, CallException callException) {
                    Log.e(TAG, "Connect failure: " + callException.getMessage());
                    callRecordDatabase.remove(uuid);
                }

                @Override
                public void onRinging(Call call) {
                    Log.d(TAG, "Ringing");
                    callRecord.setCallSid(call.getSid());
                    callRecord.setCall(call);
                }

                @Override
                public void onConnected(Call call) {
                    Log.d(TAG, "Connected");
                    callRecord.setCallSid(call.getSid());
                    callRecord.setCall(call);
                }

                @Override
                public void onReconnecting(Call call, CallException callException) {
                    Log.d(TAG, "Reconnecting: " + callException.getMessage());
                }

                @Override
                public void onReconnected(Call call) {
                    Log.d(TAG, "Reconnected");
                }

                @Override
                public void onDisconnected(Call call, CallException callException) {
                    Log.d(TAG, "Disconnected");
                    callRecordDatabase.remove(uuid);
                }
            });

            callRecord.setCall(call);

            // Return the call info in the same format as the React Native module
            WritableMap result = Arguments.createMap();
            result.putString("uuid", uuid);
            result.putString("sid", call.getSid() != null ? call.getSid() : "");
            result.putInt("state", 0); // CONNECTING state
            result.putString("from", "");
            result.putString("to", "");
            result.putBoolean("isOnHold", false);
            result.putBoolean("isMuted", false);
            
            promise.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "Error connecting: " + e.getMessage());
            promise.reject("TWILIO_VOICE_ERROR", e.getMessage(), e);
        }
    }

    /**
     * Register for incoming calls with the provided access token
     * @param accessToken The access token for authentication
     * @param promise Promise to resolve when registration is complete
     */
    @ReactMethod
    public void voice_register(String accessToken, Promise promise) {
        try {
            // Get the Firebase token and register for incoming calls
            VoiceApplicationProxy voiceApplicationProxy = VoiceApplicationProxy.getInstance();
            if (voiceApplicationProxy == null) {
                promise.reject("TWILIO_VOICE_ERROR", "Voice application proxy is null");
                return;
            }

            VoiceServiceApi voiceServiceApi = voiceApplicationProxy.getVoiceServiceApi();
            if (voiceServiceApi != null) {
                voiceServiceApi.register(accessToken);
                promise.resolve(null);
            } else {
                promise.reject("TWILIO_VOICE_ERROR", "Voice service not available");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering: " + e.getMessage());
            promise.reject("TWILIO_VOICE_ERROR", e.getMessage(), e);
        }
    }

    /**
     * Unregister for incoming calls with the provided access token
     * @param accessToken The access token for authentication
     * @param promise Promise to resolve when unregistration is complete
     */
    @ReactMethod
    public void voice_unregister(String accessToken, Promise promise) {
        try {
            // Unregister for incoming calls
            VoiceApplicationProxy voiceApplicationProxy = VoiceApplicationProxy.getInstance();
            if (voiceApplicationProxy == null) {
                promise.reject("TWILIO_VOICE_ERROR", "Voice application proxy is null");
                return;
            }

            VoiceServiceApi voiceServiceApi = voiceApplicationProxy.getVoiceServiceApi();
            if (voiceServiceApi != null) {
                voiceServiceApi.unregister(accessToken);
                promise.resolve(null);
            } else {
                promise.reject("TWILIO_VOICE_ERROR", "Voice service not available");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering: " + e.getMessage());
            promise.reject("TWILIO_VOICE_ERROR", e.getMessage(), e);
        }
    }

    /**
     * Get the SDK version
     * @param promise Promise to resolve with the SDK version
     */
    @ReactMethod
    public void voice_getVersion(Promise promise) {
        try {
            String version = Voice.getVersion();
            promise.resolve(version);
        } catch (Exception e) {
            promise.reject("TWILIO_VOICE_ERROR", e.getMessage(), e);
        }
    }

    /**
     * Handle a Firebase message for incoming calls
     * @param remoteMessage The Firebase message
     * @param promise Promise to resolve with whether the message was handled
     */
    @ReactMethod
    public void voice_handleEvent(ReadableMap remoteMessage, Promise promise) {
        try {
            Context context = getReactApplicationContext();
            if (context == null) {
                promise.reject("TWILIO_VOICE_ERROR", "React context is null");
                return;
            }

            // Convert ReadableMap to HashMap
            HashMap<String, String> messageMap = new HashMap<>();
            ReadableMapUtils.toMap(remoteMessage, messageMap);

            // Need to pass a MessageListener for the handleMessage call
            MessageListener messageListener = new MessageListener() {
                @Override
                public void onMessageReceived(String message, String channelSid, String messageSid) {
                    // Not used for FCM messages
                }
            };

            boolean valid = Voice.handleMessage(context, messageMap, messageListener);
            promise.resolve(valid);
        } catch (Exception e) {
            Log.e(TAG, "Error handling message: " + e.getMessage());
            promise.reject("TWILIO_VOICE_ERROR", e.getMessage(), e);
        }
    }

    // Helper class for ReadableMap conversion
    private static class ReadableMapUtils {
        public static void toMap(ReadableMap readableMap, Map<String, String> map) {
            if (readableMap == null) return;

            for (String key : readableMap.toHashMap().keySet()) {
                switch (readableMap.getType(key)) {
                    case String:
                        map.put(key, readableMap.getString(key));
                        break;
                    case Number:
                        map.put(key, String.valueOf(readableMap.getDouble(key)));
                        break;
                    case Boolean:
                        map.put(key, String.valueOf(readableMap.getBoolean(key)));
                        break;
                    case Null:
                        map.put(key, "null");
                        break;
                    default:
                        // Skip other types for simplicity
                        break;
                }
            }
        }
    }
}
