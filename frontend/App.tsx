import React, { useState } from 'react';
import { StyleSheet, SafeAreaView, StatusBar } from 'react-native';
import { BookingListScreen } from './src/presentation/screens/BookingListScreen';
import { BookingDetailScreen } from './src/presentation/screens/BookingDetailScreen';
import { NotificationProvider } from './src/presentation/components/NotificationManager';
import { ThemeProvider, useTheme } from './src/presentation/state/ThemeContext';

function MainApp() {
  const [activeBookingId, setActiveBookingId] = useState<string | null>(null);
  const { colors, isDark } = useTheme();

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
      <StatusBar barStyle={isDark ? 'light-content' : 'dark-content'} backgroundColor={colors.background} />
      {activeBookingId ? (
        <BookingDetailScreen
          bookingId={activeBookingId}
          onBack={() => setActiveBookingId(null)}
        />
      ) : (
        <BookingListScreen
          onSelectBooking={(id) => setActiveBookingId(id)}
        />
      )}
    </SafeAreaView>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <NotificationProvider>
        <MainApp />
      </NotificationProvider>
    </ThemeProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
});
