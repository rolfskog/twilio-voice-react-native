package com.twiliovoicereactnative

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Promise
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice
import expo.modules.core.ExportedModule
import expo.modules.core.Promise as ExpoPromise
import java.util.HashMap
import java.util.UUID

class ExpoModule(reactContext: ReactApplicationContext) : ExportedModule(reactContext) {
  private val TAG = "TwilioVoiceExpoModule"
  private val mainHandler = Handler(Looper.getMainLooper())

  override fun getName(): String {
    return "TwilioVoiceExpo"
  }

  /**
   * Connect to a Twilio Voice call with the provided access token and parameters
   * This is analogous to the voice_connect_android method in the React Native module
   */
  @ReactMethod
  fun voice_connect(accessToken: String, twimlParams: ReadableMap, notificationDisplayName: String?, promise: Promise) {
    try {
      val callRecordDatabase = VoiceApplicationProxy.getCallRecordDatabase()
      val uuid = UUID.randomUUID().toString()
      val callRecord = callRecordDatabase.create(uuid)

      val connectOptionsBuilder = ConnectOptions.Builder(accessToken)
        .params(twimlParams.toHashMap() as HashMap<String, String>)

      if (notificationDisplayName != null && notificationDisplayName.isNotEmpty()) {
        connectOptionsBuilder.displayName(notificationDisplayName)
      }

      val connectOptions = connectOptionsBuilder.build()
      val call = Voice.connect(reactApplicationContext as Context, connectOptions, object : Call.Listener {
          override fun onConnectFailure(call: Call, callException: CallException) {
            Log.e(TAG, "Connect failure: ${callException.message}")
            callRecordDatabase.remove(uuid)
          }

          override fun onRinging(call: Call) {
            Log.d(TAG, "Ringing")
            callRecord.setCallSid(call.sid)
            callRecord.setCall(call)
          }

          override fun onConnected(call: Call) {
            Log.d(TAG, "Connected")
            callRecord.setCallSid(call.sid)
            callRecord.setCall(call)
          }

          override fun onReconnecting(call: Call, callException: CallException) {
            Log.d(TAG, "Reconnecting: ${callException.message}")
          }

          override fun onReconnected(call: Call) {
            Log.d(TAG, "Reconnected")
          }

          override fun onDisconnected(call: Call, callException: CallException?) {
            Log.d(TAG, "Disconnected")
            callRecordDatabase.remove(uuid)
          }
        })

        callRecord.setCall(call)

        // Return the call info in the same format as the React Native module
        val result = HashMap<String, Any>()
        result["uuid"] = uuid
        result["sid"] = (call.sid ?: "")
        result["state"] = 0 // CONNECTING state
        result["from"] = ""
        result["to"] = ""
        result["isOnHold"] = false
        result["isMuted"] = false
        
        promise.resolve(result)
      } catch (e: Exception) {
        Log.e(TAG, "Error connecting: ${e.message}")
        promise.reject("TWILIO_VOICE_ERROR", e.message, e)
      }
    }

    /**
     * Register for incoming calls with the provided access token
     * This is analogous to the voice_register method in the React Native module
     */
    @ReactMethod
    fun voice_register(accessToken: String, promise: Promise) {
      try {
        // Get the Firebase token and register for incoming calls
        val voiceServiceApi = VoiceApplicationProxy.getVoiceServiceApi()
        if (voiceServiceApi != null) {
          voiceServiceApi.register(accessToken)
          promise.resolve(null)
        } else {
          promise.reject("TWILIO_VOICE_ERROR", "Voice service not available")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error registering: ${e.message}")
        promise.reject("TWILIO_VOICE_ERROR", e.message, e)
      }
    }

    /**
     * Unregister for incoming calls with the provided access token
     * This is analogous to the voice_unregister method in the React Native module
     */
    @ReactMethod
    fun voice_unregister(accessToken: String, promise: Promise) {
      try {
        // Unregister for incoming calls
        val voiceServiceApi = VoiceApplicationProxy.getVoiceServiceApi()
        if (voiceServiceApi != null) {
          voiceServiceApi.unregister(accessToken)
          promise.resolve(null)
        } else {
          promise.reject("TWILIO_VOICE_ERROR", "Voice service not available")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error unregistering: ${e.message}")
        promise.reject("TWILIO_VOICE_ERROR", e.message, e)
      }
    }

    /**
     * Get the SDK version
     * This is analogous to the voice_getVersion method in the React Native module
     */
    @ReactMethod
    fun voice_getVersion(promise: Promise) {
      try {
        val version = Voice.getVersion()
        promise.resolve(version)
      } catch (e: Exception) {
        promise.reject("TWILIO_VOICE_ERROR", e.message, e)
      }
    }

    /**
     * Handle a Firebase message for incoming calls
     * This is analogous to the voice_handleEvent method in the React Native module
     */
    @ReactMethod
    fun voice_handleEvent(remoteMessage: ReadableMap, promise: Promise) {
      try {
        val messageMap = remoteMessage.toHashMap() as Map<String, String>
        val valid = Voice.handleMessage(reactApplicationContext as Context, messageMap)
        promise.resolve(valid)
      } catch (e: Exception) {
        Log.e(TAG, "Error handling message: ${e.message}")
        promise.reject("TWILIO_VOICE_ERROR", e.message, e)
      }
    }
    
    // Helper method to convert ReadableMap to HashMap
    private fun ReadableMap.toHashMap(): HashMap<String, Any> {
      val result = HashMap<String, Any>()
      val iterator = this.keySetIterator()
      while (iterator.hasNextKey()) {
        val key = iterator.nextKey()
        when (this.getType(key)) {
          com.facebook.react.bridge.ReadableType.Null -> result[key] = "null"
          com.facebook.react.bridge.ReadableType.Boolean -> result[key] = this.getBoolean(key)
          com.facebook.react.bridge.ReadableType.Number -> result[key] = this.getDouble(key)
          com.facebook.react.bridge.ReadableType.String -> result[key] = this.getString(key) ?: ""
          com.facebook.react.bridge.ReadableType.Map -> result[key] = this.getMap(key)?.toHashMap() ?: HashMap<String, Any>()
          else -> result[key] = ""
        }
      }
      return result
    }
}
