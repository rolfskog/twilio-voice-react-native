const {
  withAndroidManifest,
  withAppBuildGradle,
  withProjectBuildGradle,
} = require('@expo/config-plugins');

/**
 * Adds the required Android permissions and configurations for Twilio Voice to work with Expo
 * @param {object} config - The Expo config
 * @returns {object} - The modified Expo config
 */
const withTwilioVoiceAndroid = (config) => {
  // Add the necessary permissions to the Android manifest
  config = withAndroidManifest(config, (config) => {
    const androidManifest = config.modResults;
    const mainApplication = androidManifest.manifest.application[0];

    // Ensure permissions are added
    if (!androidManifest.manifest['uses-permission']) {
      androidManifest.manifest['uses-permission'] = [];
    }

    const permissions = [
      'android.permission.INTERNET',
      'android.permission.RECORD_AUDIO',
      'android.permission.MODIFY_AUDIO_SETTINGS',
      'android.permission.ACCESS_NETWORK_STATE',
      'android.permission.WAKE_LOCK',
    ];

    // Add Bluetooth permissions based on Android version
    permissions.push('android.permission.BLUETOOTH');
    permissions.push('android.permission.BLUETOOTH_ADMIN');
    permissions.push('android.permission.BLUETOOTH_CONNECT');

    // Add notification permission for Android 13+
    permissions.push('android.permission.POST_NOTIFICATIONS');

    // Add each permission if not already present
    permissions.forEach((permission) => {
      if (
        !androidManifest.manifest['uses-permission'].some(
          (p) => p.$['android:name'] === permission
        )
      ) {
        androidManifest.manifest['uses-permission'].push({
          $: {
            'android:name': permission,
          },
        });
      }
    });

    // Add the service for handling Firebase messages if not already present
    if (!mainApplication.service) {
      mainApplication.service = [];
    }

    const firebaseServiceExists = mainApplication.service.some(
      (service) =>
        service.$['android:name'] ===
        'com.twiliovoicereactnative.VoiceFirebaseMessagingService'
    );

    if (!firebaseServiceExists) {
      mainApplication.service.push({
        '$': {
          'android:name':
            'com.twiliovoicereactnative.VoiceFirebaseMessagingService',
          'android:exported': 'false',
        },
        'intent-filter': [
          {
            action: [
              {
                $: {
                  'android:name': 'com.google.firebase.MESSAGING_EVENT',
                },
              },
            ],
          },
        ],
      });
    }

    return config;
  });

  // Add the necessary dependencies to the app build.gradle
  config = withAppBuildGradle(config, (config) => {
    if (
      !config.modResults.includes("implementation 'com.twilio:voice-android:")
    ) {
      const voiceAndroidVersion = '6.7.1'; // Use the same version as in the original build.gradle
      const audioSwitchVersion = '1.1.8';

      // Add the Twilio Voice and AudioSwitch dependencies
      const pattern = /dependencies\s*{/;
      const twilioDependencies = `dependencies {
    implementation 'com.twilio:voice-android:${voiceAndroidVersion}'
    implementation 'com.twilio:audioswitch:${audioSwitchVersion}'`;

      config.modResults = config.modResults.replace(
        pattern,
        twilioDependencies
      );
    }

    return config;
  });

  // Add the necessary repositories to the project build.gradle
  config = withProjectBuildGradle(config, (config) => {
    if (
      !config.modResults.includes('maven { url "https://maven.google.com/" }')
    ) {
      const pattern = /allprojects\s*{[^}]*repositories\s*{/;
      const googleMavenRepo = `allprojects {
    repositories {
        maven { url "https://maven.google.com/" }`;

      config.modResults = config.modResults.replace(pattern, googleMavenRepo);
    }

    return config;
  });

  return config;
};

module.exports = withTwilioVoiceAndroid;
