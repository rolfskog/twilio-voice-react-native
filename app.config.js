const withTwilioVoiceIOS = require('./expo-config-plugin/ios');
const withTwilioVoiceAndroid = require('./expo-config-plugin/android');

/**
 * Expo configuration for Twilio Voice React Native
 * This file exports a function that applies the iOS and Android plugins
 */
module.exports = function withTwilioVoice(config) {
  // Apply the iOS plugin
  config = withTwilioVoiceIOS(config);
  
  // Apply the Android plugin
  config = withTwilioVoiceAndroid(config);
  
  return config;
};
