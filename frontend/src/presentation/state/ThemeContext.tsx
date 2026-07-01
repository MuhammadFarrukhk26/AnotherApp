import React, { createContext, useContext, useState, useEffect } from 'react';
import { useColorScheme, Platform } from 'react-native';

export type ThemeType = 'light' | 'dark';

export interface ThemeColors {
  background: string;
  card: string;
  text: string;
  textSecondary: string;
  textMuted: string;
  border: string;
  primary: string;
  primaryLight: string;
  accent: string;
  success: string;
  warning: string;
  danger: string;
  surface: string;
  shadow: string;
  statusBar: 'light-content' | 'dark-content';
}

const lightColors: ThemeColors = {
  background: '#F8FAFC',
  card: '#FFFFFF',
  text: '#0F172A',
  textSecondary: '#334155',
  textMuted: '#64748B',
  border: '#E2E8F0',
  primary: '#4F46E5',
  primaryLight: '#EEF2FF',
  accent: '#FF007F',
  success: '#10B981',
  warning: '#F59E0B',
  danger: '#EF4444',
  surface: '#FFFFFF',
  shadow: '#0F172A',
  statusBar: 'dark-content',
};

const darkColors: ThemeColors = {
  background: '#0F172A',
  card: '#1E293B',
  text: '#F8FAFC',
  textSecondary: '#E2E8F0',
  textMuted: '#94A3B8',
  border: '#334155',
  primary: '#6366F1',
  primaryLight: '#2E2D56',
  accent: '#FF007F',
  success: '#34D399',
  warning: '#FBBF24',
  danger: '#F87171',
  surface: '#1E293B',
  shadow: '#000000',
  statusBar: 'light-content',
};

interface ThemeContextProps {
  theme: ThemeType;
  isDark: boolean;
  colors: ThemeColors;
  toggleTheme: () => void;
  setTheme: (theme: ThemeType) => void;
}

const ThemeContext = createContext<ThemeContextProps | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const systemColorScheme = useColorScheme();
  
  // Initialize from localStorage if on Web, otherwise fallback to system theme
  const [theme, setThemeState] = useState<ThemeType>(() => {
    if (Platform.OS === 'web') {
      try {
        const stored = localStorage.getItem('hazir_theme');
        if (stored === 'light' || stored === 'dark') {
          return stored;
        }
      } catch (e) {
        console.warn('Failed to retrieve stored theme:', e);
      }
    }
    return systemColorScheme === 'dark' ? 'dark' : 'light';
  });

  // Track system theme changes as a secondary fallback (only if user hasn't set one or if system changes)
  useEffect(() => {
    if (systemColorScheme && Platform.OS !== 'web') {
      setThemeState(systemColorScheme);
    }
  }, [systemColorScheme]);

  const toggleTheme = () => {
    setThemeState((prev) => {
      const nextTheme = prev === 'light' ? 'dark' : 'light';
      if (Platform.OS === 'web') {
        try {
          localStorage.setItem('hazir_theme', nextTheme);
        } catch (e) {
          console.warn('Failed to store theme preference:', e);
        }
      }
      return nextTheme;
    });
  };

  const setTheme = (newTheme: ThemeType) => {
    setThemeState(newTheme);
    if (Platform.OS === 'web') {
      try {
        localStorage.setItem('hazir_theme', newTheme);
      } catch (e) {
        console.warn('Failed to store theme preference:', e);
      }
    }
  };

  const isDark = theme === 'dark';
  const colors = isDark ? darkColors : lightColors;

  return (
    <ThemeContext.Provider value={{ theme, isDark, colors, toggleTheme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};
