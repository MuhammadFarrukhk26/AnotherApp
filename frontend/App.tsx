import React, { useState } from 'react';
import { StyleSheet, View, Text, TouchableOpacity, SafeAreaView } from 'react-native';
import { BookingDetailScreen } from './src/presentation/screens/BookingDetailScreen';

export default function App() {
  const [activeBookingId, setActiveBookingId] = useState<string | null>(null);

  return (
    <SafeAreaView style={styles.container}>
      {activeBookingId ? (
        <BookingDetailScreen
          bookingId={activeBookingId}
          onBack={() => setActiveBookingId(null)}
        />
      ) : (
        <View style={styles.welcomeContainer}>
          <Text style={styles.title}>Hazir On-Demand Services</Text>
          <Text style={styles.subtitle}>React Native Clean Architecture Frontend</Text>
          
          <TouchableOpacity
            style={styles.demoButton}
            onPress={() => setActiveBookingId('booking_8849491')}
          >
            <Text style={styles.buttonText}>Track Booking #8849491</Text>
          </TouchableOpacity>
        </View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  welcomeContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  title: {
    fontSize: 24,
    fontWeight: '800',
    color: '#0F172A',
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#64748B',
    textAlign: 'center',
    marginBottom: 32,
  },
  demoButton: {
    backgroundColor: '#10B981',
    paddingVertical: 16,
    paddingHorizontal: 32,
    borderRadius: 12,
    shadowColor: '#10B981',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 4,
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
  },
});
