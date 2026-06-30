import React, { useState } from 'react';
import { StyleSheet, SafeAreaView } from 'react-native';
import { BookingListScreen } from './src/presentation/screens/BookingListScreen';
import { BookingDetailScreen } from './src/presentation/screens/BookingDetailScreen';
import { NotificationProvider } from './src/presentation/components/NotificationManager';

export default function App() {
  const [activeBookingId, setActiveBookingId] = useState<string | null>(null);

  return (
    <NotificationProvider>
      <SafeAreaView style={styles.container}>
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
    </NotificationProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
});
