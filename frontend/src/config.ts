import { Platform } from 'react-native';

// Centralized API configuration for Hazir App.
// Fallback to production API URL.
let detectedUrl = 'https://api.hazir-app.com/api/v1';

if (Platform.OS === 'web' && typeof window !== 'undefined') {
  const host = window.location.hostname;
  if (
    host.includes('localhost') ||
    host.includes('127.0.0.1') ||
    host.includes('ais-dev') ||
    host.includes('ais-pre') ||
    host.includes('run.app')
  ) {
    // In local development or AI Studio preview containers, point to the current origin
    detectedUrl = `${window.location.origin}/api/v1`;
  }
}

export const API_BASE_URL = detectedUrl;
