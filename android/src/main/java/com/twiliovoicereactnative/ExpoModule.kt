
import android.content.Context
import android.util.Log
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice

import expo.modules.kotlin.Promise
import expo.modules.kotlin.jni.JavaScriptObject
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

import java.util.HashMap
import java.util.UUID

class ExpoModule : Module() {
  private val TAG = "TwilioVoiceExpoModule"

  override fun definition() = ModuleDefinition {
    Name("TwilioVoiceExpo")

    /**
     * Connect to a Twilio Voice call with the provided access token and parameters
     * This is analogous to the voice_connect method in the ExpoModule.ts
     */
    Function("voice_connect") { accessToken: String, params: Map<String, Any>, notificationDisplayName: String? ->
      try {
        val context = appContext.reactContext
        if (context == null) {
          throw Exception("React context is null")
        }

        val callRecordDatabase = VoiceApplicationProxy.getCallRecordDatabase()
        val uuid = UUID.randomUUID().toString()
        val callRecord = callRecordDatabase.create(uuid)

        val connectOptionsBuilder = ConnectOptions.Builder(accessToken)
          .params(params as HashMap<String, String>)

        if (notificationDisplayName != null && notificationDisplayName.isNotEmpty()) {
          connectOptionsBuilder.displayName(notificationDisplayName)
        }

        val connectOptions = connectOptionsBuilder.build()
        val call = Voice.connect(context as Context, connectOptions, object : Call.Listener {
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
        return@Function mapOf(
          "uuid" to uuid,
          "sid" to (call.sid ?: ""),
          "state" to 0, // CONNECTING state
          "from" to "",
          "to" to "",
          "isOnHold" to false,
          "isMuted" to false
        )
      } catch (e: Exception) {
        Log.e(TAG, "Error connecting: ${e.message}")
        throw e
      }
    }

    /**
     * Register for incoming calls with the provided access token
     * This is analogous to the register method in the ExpoModule.ts
     */
    Function("voice_register") { accessToken: String ->
      try {
        // Get the Firebase token and register for incoming calls
        val voiceServiceApi = VoiceApplicationProxy.getVoiceServiceApi()
        if (voiceServiceApi != null) {
          voiceServiceApi.register(accessToken)
          return@Function
        } else {
          throw Exception("Voice service not available")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error registering: ${e.message}")
        throw e
      }
    }

    /**
     * Unregister for incoming calls with the provided access token
     * This is analogous to the unregister method in the ExpoModule.ts
     */
    Function("voice_unregister") { accessToken: String ->
      try {
        // Unregister for incoming calls
        val voiceServiceApi = VoiceApplicationProxy.getVoiceServiceApi()
        if (voiceServiceApi != null) {
          voiceServiceApi.unregister(accessToken)
          return@Function
        } else {
          throw Exception("Voice service not available")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error unregistering: ${e.message}")
        throw e
      }
    }

    /**
     * Get the SDK version
     * This is analogous to the getVersion method in the ExpoModule.ts
     */
    Function("voice_getVersion") {
      return@Function Voice.getVersion()
    }

    /**
     * Handle a Firebase message for incoming calls
     * This is analogous to the handleEvent method in the ExpoModule.ts
     */
    Function("voice_handleEvent") { remoteMessage: Map<String, String> ->
      try {
        val valid = Voice.handleMessage(appContext.reactContext as Context, remoteMessage as Map<String, String>)
        return@Function valid
      } catch (e: Exception) {
        Log.e(TAG, "Error handling message: ${e.message}")
        throw e
      }
    }
  }
}