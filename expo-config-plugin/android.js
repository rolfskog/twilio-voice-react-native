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
    const voiceAndroidVersion = '6.7.1'; // Use the same version as in the original build.gradle
    const audioSwitchVersion = '1.1.8';

    // Check if the Twilio dependencies already exist in the build.gradle
    const hasTwilioVoice = config.modResults.dependencies.some((dependency) =>
      dependency.includes('com.twilio:voice-android')
    );

    // Add Twilio Voice dependency if not already present
    if (!hasTwilioVoice) {
      config.modResults.dependencies.push({
        implementation: `'com.twilio:voice-android:${voiceAndroidVersion}'`,
      });

      // Add AudioSwitch dependency
      config.modResults.dependencies.push({
        implementation: `'com.twilio:audioswitch:${audioSwitchVersion}'`,
      });
    }

    return config;
  });

  // Add the necessary repositories to the project build.gradle
  config = withProjectBuildGradle(config, (config) => {
    // Check if we need to add the Google Maven repository
    const buildscriptRepositories =
      config.modResults.buildscript?.repositories || [];
    const projectRepositories =
      config.modResults.allprojects?.repositories || [];

    // Function to check if a repository list already has Google Maven
    const hasGoogleMaven = (repos) => {
      return repos.some((repo) => {
        return (
          repo.maven &&
          repo.maven.url &&
          (repo.maven.url.includes('maven.google.com') ||
            repo.maven.url.includes('google()'))
        );
      });
    };

    // Add Google Maven repository if not present
    if (
      !hasGoogleMaven(buildscriptRepositories) &&
      buildscriptRepositories.push
    ) {
      buildscriptRepositories.push({
        maven: { url: 'https://maven.google.com/' },
      });
    }

    if (!hasGoogleMaven(projectRepositories) && projectRepositories.push) {
      projectRepositories.push({
        maven: { url: 'https://maven.google.com/' },
      });
    }

    return config;
  });

  return config;
};

module.exports = withTwilioVoiceAndroid;
