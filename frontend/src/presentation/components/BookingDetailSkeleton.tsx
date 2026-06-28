import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, Animated } from 'react-native';

export const BookingDetailSkeleton: React.FC = () => {
  const pulseAnim = useRef(new Animated.Value(0.3)).current;

  useEffect(() => {
    Animated.loop(
      Animated.sequence([
        Animated.timing(pulseAnim, {
          toValue: 0.7,
          duration: 1000,
          useNativeDriver: true,
        }),
        Animated.timing(pulseAnim, {
          toValue: 0.3,
          duration: 1000,
          useNativeDriver: true,
        }),
      ])
    ).start();
  }, [pulseAnim]);

  return (
    <View style={styles.container}>
      {/* Main Details Card skeleton */}
      <View style={styles.card}>
        <View style={styles.statusRow}>
          <Animated.View style={[styles.shimmer, { width: 120, height: 20, borderRadius: 6, opacity: pulseAnim }]} />
          <Animated.View style={[styles.shimmer, { width: 80, height: 24, borderRadius: 12, opacity: pulseAnim }]} />
        </View>
        <Animated.View style={[styles.shimmer, { width: 150, height: 14, borderRadius: 4, marginTop: 12, opacity: pulseAnim }]} />
        
        <View style={styles.divider} />

        <View style={styles.detailRow}>
          <Animated.View style={[styles.shimmer, { width: 70, height: 14, borderRadius: 4, opacity: pulseAnim }]} />
          <Animated.View style={[styles.shimmer, { width: 160, height: 14, borderRadius: 4, opacity: pulseAnim }]} />
        </View>

        <View style={styles.detailRow}>
          <Animated.View style={[styles.shimmer, { width: 70, height: 14, borderRadius: 4, opacity: pulseAnim }]} />
          <Animated.View style={[styles.shimmer, { width: 200, height: 14, borderRadius: 4, opacity: pulseAnim }]} />
        </View>

        <View style={styles.detailRow}>
          <Animated.View style={[styles.shimmer, { width: 90, height: 14, borderRadius: 4, opacity: pulseAnim }]} />
          <Animated.View style={[styles.shimmer, { width: 80, height: 18, borderRadius: 4, opacity: pulseAnim }]} />
        </View>
      </View>

      {/* Worker Card skeleton */}
      <View style={styles.card}>
        <View style={styles.workerRow}>
          <Animated.View style={[styles.shimmer, { width: 60, height: 60, borderRadius: 30, opacity: pulseAnim }]} />
          <View style={styles.workerInfo}>
            <View style={styles.row}>
              <Animated.View style={[styles.shimmer, { width: 100, height: 16, borderRadius: 4, opacity: pulseAnim }]} />
              <Animated.View style={[styles.shimmer, { width: 40, height: 16, borderRadius: 6, marginLeft: 8, opacity: pulseAnim }]} />
            </View>
            <Animated.View style={[styles.shimmer, { width: 160, height: 12, borderRadius: 4, marginTop: 8, opacity: pulseAnim }]} />
            <Animated.View style={[styles.shimmer, { width: 120, height: 12, borderRadius: 4, marginTop: 8, opacity: pulseAnim }]} />
          </View>
        </View>
        <View style={styles.divider} />
        <View style={styles.row}>
          <Animated.View style={[styles.shimmer, { flex: 1, height: 40, borderRadius: 10, opacity: pulseAnim }]} />
          <View style={{ width: 12 }} />
          <Animated.View style={[styles.shimmer, { flex: 1, height: 40, borderRadius: 10, opacity: pulseAnim }]} />
        </View>
      </View>

      {/* Tracking Card skeleton */}
      <View style={styles.card}>
        <View style={styles.statusRow}>
          <Animated.View style={[styles.shimmer, { width: 130, height: 16, borderRadius: 4, opacity: pulseAnim }]} />
          <Animated.View style={[styles.shimmer, { width: 60, height: 18, borderRadius: 6, opacity: pulseAnim }]} />
        </View>
        <Animated.View style={[styles.shimmer, { width: '100%', height: 180, borderRadius: 12, marginTop: 12, opacity: pulseAnim }]} />
      </View>

      {/* ETA Card skeleton */}
      <View style={styles.card}>
        <View style={styles.row}>
          <Animated.View style={[styles.shimmer, { flex: 1, height: 50, borderRadius: 10, opacity: pulseAnim }]} />
          <View style={{ width: 12 }} />
          <Animated.View style={[styles.shimmer, { flex: 1, height: 50, borderRadius: 10, opacity: pulseAnim }]} />
        </View>
      </View>

      {/* Billing Card skeleton */}
      <View style={styles.card}>
        <Animated.View style={[styles.shimmer, { width: 150, height: 16, borderRadius: 4, marginBottom: 16, opacity: pulseAnim }]} />
        {[1, 2, 3].map((i) => (
          <View key={i} style={[styles.row, { marginBottom: 12 }]}>
            <Animated.View style={[styles.shimmer, { width: i === 2 ? 140 : 100, height: 12, borderRadius: 4, opacity: pulseAnim }]} />
            <Animated.View style={[styles.shimmer, { width: 60, height: 12, borderRadius: 4, opacity: pulseAnim }]} />
          </View>
        ))}
        <View style={styles.divider} />
        <View style={styles.row}>
          <Animated.View style={[styles.shimmer, { width: 80, height: 16, borderRadius: 4, opacity: pulseAnim }]} />
          <Animated.View style={[styles.shimmer, { width: 90, height: 18, borderRadius: 4, opacity: pulseAnim }]} />
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    paddingVertical: 8,
  },
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
  },
  shimmer: {
    backgroundColor: '#E2E8F0',
  },
  statusRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  divider: {
    height: 1,
    backgroundColor: '#F1F5F9',
    marginVertical: 14,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 6,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  workerRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  workerInfo: {
    flex: 1,
    marginLeft: 16,
  },
});
