package com.twiliovoicereactnative;

import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice

import expo.modules.kotlin.Promise
import expo.modules.kotlin.jni.JavaScriptObject
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

import java.util.UUID

class ExpoModule : Module() {
  Function("voice_connect") {
    accessToken: String ->

    val context = appContext.reactContext
    if (context == null) {
      return@Function
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
      CallRecord.Direction.Outgoing,
      "Display Name" // provide a mechanism for determining the notification display name of the callee
    )
    VoiceApplicationProxy.getCallRecordDatabase.add(callRecord)
  }
}