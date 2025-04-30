const {
  withAndroidManifest,
  withAppBuildGradle,
  withProjectBuildGradle,
  AndroidConfig,
} = require('@expo/config-plugins');

const { getMainApplicationOrThrow } = AndroidConfig.Manifest;

/**
 * Adds the required Android permissions and configurations for Twilio Voice to work with Expo
 * @param {object} config - The Expo config
 * @returns {object} - The modified Expo config
 */
const withTwilioVoiceAndroid = (config) => {
  // Add the necessary permissions to the Android manifest
  config = withAndroidManifest(config, (config) => {
    const androidManifest = config.modResults;
    if (!androidManifest) return config;

    const permissions = [
      'android.permission.INTERNET',
      'android.permission.RECORD_AUDIO',
      'android.permission.MODIFY_AUDIO_SETTINGS',
      'android.permission.ACCESS_NETWORK_STATE',
      'android.permission.ACCESS_WIFI_STATE',
      'android.permission.BLUETOOTH',
      'android.permission.WAKE_LOCK',
      'android.permission.FOREGROUND_SERVICE',
      'android.permission.READ_PHONE_STATE',
      'android.permission.CALL_PHONE',
      'android.permission.ANSWER_PHONE_CALLS',
      'android.permission.MANAGE_OWN_CALLS',
      'android.permission.USE_FULL_SCREEN_INTENT',
    ];

    // Ensure manifest has uses-permission elements
    if (!androidManifest.manifest['uses-permission']) {
      androidManifest.manifest['uses-permission'] = [];
    }

    // Add each permission to the manifest if not already present
    for (const permission of permissions) {
      const permissionExists = androidManifest.manifest['uses-permission'].some(
        (item) => item.$?.['android:name'] === permission
      );

      if (!permissionExists) {
        androidManifest.manifest['uses-permission'].push({
          $: { 'android:name': permission },
        });
      }
    }

    const mainApplication = getMainApplicationOrThrow(androidManifest);

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

    // Check if we're dealing with a string or an object
    if (typeof config.modResults === 'string') {
      // Add Twilio Voice and AudioSwitch dependencies
      if (!config.modResults.includes('com.twilio:voice-android')) {
        // Find the dependencies block
        const dependenciesBlockRegex = /(dependencies\s*{[^}]*)(})/;
        if (dependenciesBlockRegex.test(config.modResults)) {
          // Add dependencies inside the dependencies block
          const dependenciesToAdd = `
    // Twilio Voice SDK dependencies added by expo plugin
    implementation 'com.twilio:voice-android:${voiceAndroidVersion}'
    implementation 'com.twilio:audioswitch:${audioSwitchVersion}'
`;

          config.modResults = config.modResults.replace(
            dependenciesBlockRegex,
            `$1${dependenciesToAdd}$2`
          );
        } else {
          // If no dependencies block found, add it before the last closing bracket
          const lastClosingBracket = config.modResults.lastIndexOf('}');
          if (lastClosingBracket !== -1) {
            const dependenciesToAdd = `

dependencies {
    // Twilio Voice SDK dependencies added by expo plugin
    implementation 'com.twilio:voice-android:${voiceAndroidVersion}'
    implementation 'com.twilio:audioswitch:${audioSwitchVersion}'
}
`;

            config.modResults =
              config.modResults.substring(0, lastClosingBracket) +
              dependenciesToAdd +
              config.modResults.substring(lastClosingBracket);
          }
        }
      }
    } else if (config.modResults && typeof config.modResults === 'object') {
      // Handle object representation of build.gradle
      if (!config.modResults.dependencies) {
        config.modResults.dependencies = [];
      }

      // Check if Twilio Voice dependency already exists
      const hasTwilioVoice = config.modResults.dependencies.some(
        (dep) =>
          typeof dep === 'object' &&
          dep.implementation &&
          dep.implementation.includes('com.twilio:voice-android')
      );

      if (!hasTwilioVoice) {
        // Add Twilio Voice dependency
        config.modResults.dependencies.push({
          implementation: `'com.twilio:voice-android:${voiceAndroidVersion}'`,
        });

        // Add AudioSwitch dependency
        config.modResults.dependencies.push({
          implementation: `'com.twilio:audioswitch:${audioSwitchVersion}'`,
        });
      }
    }

    return config;
  });

  // Add the necessary repositories to the project build.gradle
  config = withProjectBuildGradle(config, (config) => {
    // Check if we're dealing with a string or an object
    if (typeof config.modResults === 'string') {
      // Check if Google Maven repository is already included
      if (!config.modResults.includes('maven.google.com')) {
        // Look for repositories section in allprojects block
        const allProjectsRepoRegex =
          /(allprojects\s*{[^}]*repositories\s*{[^}]*)(})/;
        if (allProjectsRepoRegex.test(config.modResults)) {
          // Add Google Maven repository to allprojects repositories
          config.modResults = config.modResults.replace(
            allProjectsRepoRegex,
            '$1        maven { url "https://maven.google.com/" }\n    $2'
          );
        } else {
          // If allprojects block doesn't have repositories, look for buildscript repositories
          const buildscriptRepoRegex =
            /(buildscript\s*{[^}]*repositories\s*{[^}]*)(})/;
          if (buildscriptRepoRegex.test(config.modResults)) {
            // Add Google Maven repository to buildscript repositories
            config.modResults = config.modResults.replace(
              buildscriptRepoRegex,
              '$1        maven { url "https://maven.google.com/" }\n    $2'
            );
          }
        }
      }
    } else if (config.modResults && typeof config.modResults === 'object') {
      // Handle object representation of build.gradle
      // Add to buildscript repositories if they exist
      if (
        config.modResults.buildscript &&
        config.modResults.buildscript.repositories
      ) {
        const hasGoogleMaven = config.modResults.buildscript.repositories.some(
          (repo) =>
            repo.maven &&
            repo.maven.url &&
            repo.maven.url.includes('maven.google.com')
        );

        if (!hasGoogleMaven) {
          config.modResults.buildscript.repositories.push({
            maven: { url: 'https://maven.google.com/' },
          });
        }
      }

      // Add to allprojects repositories if they exist
      if (
        config.modResults.allprojects &&
        config.modResults.allprojects.repositories
      ) {
        const hasGoogleMaven = config.modResults.allprojects.repositories.some(
          (repo) =>
            repo.maven &&
            repo.maven.url &&
            repo.maven.url.includes('maven.google.com')
        );

        if (!hasGoogleMaven) {
          config.modResults.allprojects.repositories.push({
            maven: { url: 'https://maven.google.com/' },
          });
        }
      }
    }

    return config;
  });

  return config;
};

module.exports = withTwilioVoiceAndroid;
