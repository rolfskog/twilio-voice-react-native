/**
 * Copyright © 2022 Twilio, Inc. All rights reserved. Licensed under the Twilio
 * license.
 *
 * See LICENSE in the project root for license information.
 */

import { Platform } from 'react-native';
import { requireNativeModule } from 'expo-modules-core';
import { NativeModule } from './common';
import type { NativeCallInfo } from './type/Call';
import type { CustomParameters } from './type/common';

/**
 * Expo Module for Twilio Voice React Native
 * This module provides access to the native Expo module for Android
 * and falls back to the React Native module for iOS
 */
class ExpoVoiceModule {
  private androidExpoNativeModule: any;

  constructor() {
    if (Platform.OS === 'android') {
      try {
        this.androidExpoNativeModule = requireNativeModule('TwilioVoiceExpo');
      } catch (e) {
        console.error('Failed to load Twilio Voice Expo module:', e);
      }
    }
  }

  /**
   * Connect to a Twilio Voice call
   * @param token - The access token for authentication
   * @param params - Custom parameters for the call
   * @param contactHandle - The contact handle for iOS (ignored on Android)
   * @param notificationDisplayName - The notification display name for Android (ignored on iOS)
   * @returns Promise resolving to call information
   */
  async connect(
    token: string,
    params: CustomParameters = {},
    contactHandle?: string,
    notificationDisplayName?: string
  ): Promise<NativeCallInfo> {
    if (Platform.OS === 'android') {
      if (this.androidExpoNativeModule) {
        return this.androidExpoNativeModule.voice_connect(
          token,
          params,
          notificationDisplayName
        );
      } else {
        return NativeModule.voice_connect_android(
          token,
          params,
          notificationDisplayName
        );
      }
    } else if (Platform.OS === 'ios') {
      return NativeModule.voice_connect_ios(token, params, contactHandle || '');
    } else {
      throw new Error(`Unsupported platform: ${Platform.OS}`);
    }
  }

  /**
   * Register for incoming calls
   * @param accessToken - The access token for authentication
   * @returns Promise resolving when registration is complete
   */
  async register(accessToken: string): Promise<void> {
    if (Platform.OS === 'android' && this.androidExpoNativeModule) {
      return this.androidExpoNativeModule.voice_register(accessToken);
    } else {
      return NativeModule.voice_register(accessToken);
    }
  }

  /**
   * Unregister for incoming calls
   * @param accessToken - The access token for authentication
   * @returns Promise resolving when unregistration is complete
   */
  async unregister(accessToken: string): Promise<void> {
    if (Platform.OS === 'android' && this.androidExpoNativeModule) {
      return this.androidExpoNativeModule.voice_unregister(accessToken);
    } else {
      return NativeModule.voice_unregister(accessToken);
    }
  }

  /**
   * Get the SDK version
   * @returns Promise resolving to the SDK version
   */
  async getVersion(): Promise<string> {
    if (Platform.OS === 'android' && this.androidExpoNativeModule) {
      return this.androidExpoNativeModule.voice_getVersion();
    } else {
      return NativeModule.voice_getVersion();
    }
  }

  /**
   * Handle a Firebase message for incoming calls
   * @param remoteMessage - The Firebase message
   * @returns Promise resolving to whether the message was handled
   */
  async handleEvent(remoteMessage: Record<string, string>): Promise<boolean> {
    if (Platform.OS === 'android' && this.androidExpoNativeModule) {
      return this.androidExpoNativeModule.voice_handleEvent(remoteMessage);
    } else {
      return NativeModule.voice_handleEvent(remoteMessage);
    }
  }
}

export const ExpoVoice = new ExpoVoiceModule();
