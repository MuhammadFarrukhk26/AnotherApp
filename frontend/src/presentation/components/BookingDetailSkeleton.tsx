import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, Animated, ScrollView } from 'react-native';

interface ShimmerProps {
  width?: number | string;
  height: number;
  borderRadius?: number;
  style?: any;
}

export const Shimmer: React.FC<ShimmerProps> = ({ width = '100%', height, borderRadius = 8, style }) => {
  const animatedValue = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.loop(
      Animated.timing(animatedValue, {
        toValue: 1,
        duration: 1500,
        useNativeDriver: true,
      })
    ).start();
  }, [animatedValue]);

  // Continuous sweeping animation across the component
  const translateX = animatedValue.interpolate({
    inputRange: [0, 1],
    outputRange: [-180, 420],
  });

  return (
    <View style={[styles.shimmerContainer, { width, height, borderRadius }, style]}>
      <Animated.View
        style={[
          styles.shimmerStreak,
          {
            transform: [{ translateX }, { skewX: '-20deg' }],
          },
        ]}
      />
    </View>
  );
};

export const BookingDetailSkeleton: React.FC = () => {
  return (
    <ScrollView 
      style={styles.container}
      contentContainerStyle={styles.scrollContent}
      showsVerticalScrollIndicator={false}
    >
      {/* 1. Header Detail Summary Card */}
      <View style={styles.card}>
        <View style={styles.statusHeaderRow}>
          <Shimmer width={140} height={20} borderRadius={6} />
          <Shimmer width={75} height={24} borderRadius={12} />
        </View>
        <Shimmer width={180} height={12} borderRadius={4} style={{ marginTop: 8 }} />
        
        <View style={styles.divider} />

        {/* Schedule Shimmer */}
        <View style={styles.detailRow}>
          <Shimmer width={80} height={14} borderRadius={4} />
          <Shimmer width={160} height={14} borderRadius={4} />
        </View>

        {/* Location Shimmer */}
        <View style={styles.detailRow}>
          <Shimmer width={80} height={14} borderRadius={4} />
          <Shimmer width={210} height={14} borderRadius={4} />
        </View>

        {/* Price Shimmer */}
        <View style={styles.detailRow}>
          <Shimmer width={100} height={14} borderRadius={4} />
          <Shimmer width={90} height={18} borderRadius={4} />
        </View>
      </View>

      {/* 2. Safety & Live Share Card */}
      <View style={styles.card}>
        <View style={styles.row}>
          <Shimmer width={160} height={16} borderRadius={6} />
        </View>
        <Shimmer width="100%" height={12} borderRadius={4} style={{ marginTop: 10 }} />
        <Shimmer width="90%" height={12} borderRadius={4} style={{ marginTop: 6 }} />
        <Shimmer width="100%" height={44} borderRadius={12} style={{ marginTop: 16 }} />
      </View>

      {/* 3. Tab Button Selector Shimmer */}
      <View style={styles.tabShimmerContainer}>
        <Shimmer width="48%" height={36} borderRadius={10} />
        <Shimmer width="48%" height={36} borderRadius={10} />
      </View>

      {/* 4. Assigned Provider & Tracking Segment */}
      <View style={styles.card}>
        <View style={styles.workerRow}>
          {/* Avatar Ring */}
          <Shimmer width={54} height={54} borderRadius={27} />
          <View style={styles.workerInfo}>
            <View style={styles.row}>
              <Shimmer width={120} height={16} borderRadius={4} />
              <Shimmer width={44} height={16} borderRadius={6} style={{ marginLeft: 8 }} />
            </View>
            <Shimmer width={180} height={11} borderRadius={4} style={{ marginTop: 6 }} />
            <Shimmer width={130} height={11} borderRadius={4} style={{ marginTop: 6 }} />
          </View>
        </View>
        
        <View style={styles.divider} />
        
        {/* Tracking Map Canvas Shimmer */}
        <View style={styles.row}>
          <Shimmer width="100%" height={220} borderRadius={14} />
        </View>

        {/* ETA Grid Shimmers */}
        <View style={styles.etaGrid}>
          <Shimmer width="48%" height={56} borderRadius={12} />
          <Shimmer width="48%" height={56} borderRadius={12} />
        </View>
      </View>

      {/* 5. Billing & Invoice Summary Card */}
      <View style={styles.card}>
        <Shimmer width={160} height={16} borderRadius={4} style={{ marginBottom: 16 }} />
        
        {[1, 2, 3, 4].map((i) => (
          <View key={i} style={[styles.row, { marginBottom: 12 }]}>
            <Shimmer width={i === 2 ? 150 : 110} height={12} borderRadius={4} />
            <Shimmer width={65} height={12} borderRadius={4} />
          </View>
        ))}
        
        <View style={styles.divider} />
        
        <View style={styles.row}>
          <Shimmer width={90} height={16} borderRadius={4} />
          <Shimmer width={100} height={20} borderRadius={4} />
        </View>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  scrollContent: {
    paddingBottom: 24,
  },
  shimmerContainer: {
    backgroundColor: '#E2E8F0', // Base card shimmer grey
    overflow: 'hidden',
    position: 'relative',
  },
  shimmerStreak: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    width: 120,
    backgroundColor: 'rgba(255, 255, 255, 0.45)', // Premium glossy highlight band
  },
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.02,
    shadowRadius: 6,
    elevation: 1,
  },
  statusHeaderRow: {
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
    marginVertical: 7,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  tabShimmerContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    backgroundColor: '#E2E8F0',
    borderRadius: 14,
    padding: 4,
    marginBottom: 16,
  },
  workerRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  workerInfo: {
    flex: 1,
    marginLeft: 16,
  },
  etaGrid: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 14,
  },
});
