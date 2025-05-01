package com.twiliovoicereactnative

import android.util.Log
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.CallInvite
import com.twilio.voice.ConnectOptions
import com.twilio.voice.MessageListener
import com.twilio.voice.Voice

import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

import java.util.HashMap
import java.util.UUID

class ExpoModule : Module() {
  private val TAG = "TwilioVoiceExpoModule"
  
  override fun definition() = ModuleDefinition {
    Name("TwilioVoiceExpo")
    
    Function("voice_connect") { accessToken: String ->
      try {
        val context = appContext.reactContext
        if (context == null) {
          return@Function null
        }

        val connectOptions = ConnectOptions.Builder(accessToken).build()
        val uuid = UUID.randomUUID()
        val callListenerProxy = CallListenerProxy(uuid, context)

        val callRecord = CallRecordDatabase.CallRecord(
          uuid,
          VoiceApplicationProxy.getVoiceServiceApi().connect(
            connectOptions,
            callListenerProxy
          ),
          "Callee", // provide a mechanism for determining the name of the callee
          HashMap(), // provide a mechanism for passing custom TwiML parameters
          CallRecordDatabase.CallRecord.Direction.OUTGOING,
          "Display Name" // provide a mechanism for determining the notification display name of the callee
        )
        VoiceApplicationProxy.getCallRecordDatabase().add(callRecord)
        
        // Return the call info
        mapOf(
          "uuid" to uuid.toString(),
          "sid" to "",
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
  }
}