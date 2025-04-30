const { withInfoPlist } = require('@expo/config-plugins');

/**
 * Adds the required iOS permissions and capabilities for Twilio Voice to work with Expo
 * @param {object} config - The Expo config
 * @returns {object} - The modified Expo config
 */
const withTwilioVoiceIOS = (config) => {
  return withInfoPlist(config, (config) => {
    // Add background modes for audio and VoIP
    if (!config.modResults.UIBackgroundModes) {
      config.modResults.UIBackgroundModes = [];
    }

    // Add Audio, AirPlay, and Picture in Picture background mode if not already present
    if (!config.modResults.UIBackgroundModes.includes('audio')) {
      config.modResults.UIBackgroundModes.push('audio');
    }

    // Add Voice over IP background mode if not already present
    if (!config.modResults.UIBackgroundModes.includes('voip')) {
      config.modResults.UIBackgroundModes.push('voip');
    }

    // Add remote-notification background mode for push notifications if not already present
    if (!config.modResults.UIBackgroundModes.includes('remote-notification')) {
      config.modResults.UIBackgroundModes.push('remote-notification');
    }

    // Add microphone usage description if not present
    if (!config.modResults.NSMicrophoneUsageDescription) {
      config.modResults.NSMicrophoneUsageDescription =
        'This app needs microphone access to make and receive voice calls.';
    }

    return config;
  });
};

module.exports = withTwilioVoiceIOS;
