import React, { useState, useEffect, useRef } from 'react';
import { View, Text, StyleSheet, Animated, TouchableOpacity, ActivityIndicator } from 'react-native';

interface BookingCountdownTimerProps {
  customerLat: number;
  customerLng: number;
  onArrived?: () => void;
  workerName?: string;
}

export const BookingCountdownTimer: React.FC<BookingCountdownTimerProps> = ({
  customerLat,
  customerLng,
  onArrived,
  workerName = 'Ayaan Sheikh',
}) => {
  // Initial ETA in minutes (simulating a standard 12-minute arrival duration)
  const [totalSeconds, setTotalSeconds] = useState<number>(720); // 12 minutes
  const [isActive, setIsActive] = useState<boolean>(true);
  const [simulationSpeed, setSimulationSpeed] = useState<number>(1); // multiplier
  const [currentSpeed, setCurrentSpeed] = useState<number>(35); // km/h
  const [trafficDelay, setTrafficDelay] = useState<number>(0); // in seconds
  const [distanceKm, setDistanceKm] = useState<number>(4.2); // starting distance
  const [currentMilestone, setCurrentMilestone] = useState<number>(1); // 0: Hub, 1: En Route, 2: Arrived

  const pulseAnim = useRef(new Animated.Value(1)).current;
  const progressAnim = useRef(new Animated.Value(0)).current;

  // Pulse animation for the countdown and active dots
  useEffect(() => {
    Animated.loop(
      Animated.sequence([
        Animated.timing(pulseAnim, {
          toValue: 1.12,
          duration: 1000,
          useNativeDriver: true,
        }),
        Animated.timing(pulseAnim, {
          toValue: 1,
          duration: 1000,
          useNativeDriver: true,
        }),
      ])
    ).start();
  }, [pulseAnim]);

  // Main countdown ticking and simulation updater
  useEffect(() => {
    if (!isActive || totalSeconds <= 0) {
      if (totalSeconds <= 0 && currentMilestone < 2) {
        setCurrentMilestone(2);
        setDistanceKm(0);
        setCurrentSpeed(0);
        if (onArrived) onArrived();
      }
      return;
    }

    const interval = setInterval(() => {
      setTotalSeconds((prev) => {
        const nextSeconds = prev - (1 * simulationSpeed);
        
        // Calculate dynamic values based on remaining time
        const progressFraction = (720 - nextSeconds) / 720;
        
        // Update distance remaining: starting 4.2 km down to 0 km
        const newDistance = Math.max(0, 4.2 * (1 - progressFraction));
        setDistanceKm(parseFloat(newDistance.toFixed(2)));

        // Speed fluctuation simulation (between 25 km/h and 45 km/h)
        const speedNoise = Math.floor(Math.sin(nextSeconds / 20) * 10);
        const baseSpeed = trafficDelay > 0 ? 15 : 35;
        setCurrentSpeed(Math.max(5, baseSpeed + speedNoise));

        if (nextSeconds <= 180 && currentMilestone === 1) {
          // Less than 3 minutes remaining: Near Arrival
          setCurrentMilestone(1.5); // transition milestone
        }

        if (nextSeconds <= 0) {
          clearInterval(interval);
          setCurrentMilestone(2);
          setDistanceKm(0);
          setCurrentSpeed(0);
          if (onArrived) onArrived();
          return 0;
        }

        return nextSeconds;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [isActive, totalSeconds, simulationSpeed, trafficDelay]);

  // Sync Animated value with remaining seconds for visual rendering
  useEffect(() => {
    const progressVal = (720 - totalSeconds) / 720;
    Animated.timing(progressAnim, {
      toValue: progressVal,
      duration: 500,
      useNativeDriver: false, // width/flex transforms do not support native driver
    }).start();
  }, [totalSeconds]);

  // Format total seconds into elegant MM:SS
  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  // User Actions: Simulate traffic congestion
  const handleTriggerTrafficDelay = () => {
    // Inject 2 minutes (120 seconds) of congestion delay
    setTotalSeconds((prev) => prev + 120);
    setTrafficDelay((prev) => prev + 120);
    setCurrentSpeed(10); // Traffic slows technician down
    
    // Auto-resolve traffic delay state after a visual period
    setTimeout(() => {
      setTrafficDelay(0);
    }, 10000);
  };

  // User Actions: Simulate driver speed-up
  const handleSpeedUpSimulation = () => {
    // Increase simulation clock to skip forward (x4 simulation speed)
    setSimulationSpeed((prev) => (prev === 1 ? 5 : 1));
  };

  const getMilestoneStyle = (step: number) => {
    if (step === 0) {
      // Step 0: Started / Hub
      return styles.milestonePassed;
    }
    if (step === 1) {
      // Step 1: En Route
      if (currentMilestone >= 1) return styles.milestoneActive;
      return styles.milestonePending;
    }
    if (step === 2) {
      // Step 2: Arrived
      if (currentMilestone >= 2) return styles.milestonePassed;
      if (currentMilestone > 1) return styles.milestoneActive;
      return styles.milestonePending;
    }
    return styles.milestonePending;
  };

  return (
    <View 
      style={styles.card}
      accessible={true}
      accessibilityLabel={`Technician ETA Countdown. Time remaining: ${Math.floor(totalSeconds / 60)} minutes.`}
    >
      {/* Card Header */}
      <View style={styles.headerRow}>
        <View>
          <Text style={styles.title} accessibilityRole="header">Live ETA Countdown</Text>
          <Text style={styles.subtitle}>Simulated Real-time Dispatch Sync</Text>
        </View>
        <View style={[styles.statusBadge, totalSeconds <= 0 ? styles.badgeArrived : styles.badgeEnroute]}>
          <Text style={styles.statusBadgeText}>
            {totalSeconds <= 0 ? 'ARRIVED' : 'EN ROUTE'}
          </Text>
        </View>
      </View>

      {/* Main Countdown Timer Core */}
      <View style={styles.timerGrid}>
        <View style={styles.timerCircle}>
          <Animated.View style={[styles.pulseInner, { transform: [{ scale: pulseAnim }] }]} />
          <View style={styles.timerNumberBox}>
            <Text style={styles.timerValue}>
              {totalSeconds <= 0 ? '00:00' : formatTime(totalSeconds)}
            </Text>
            <Text style={styles.timerLabel}>
              {totalSeconds <= 0 ? 'SERVICE ACTIVE' : 'MINS REMAINING'}
            </Text>
          </View>
        </View>

        {/* Real-time stats */}
        <View style={styles.statsWrapper}>
          <View style={styles.statPill}>
            <Text style={styles.statLabel}>📏 Distance Remaining</Text>
            <Text style={styles.statVal}>{totalSeconds <= 0 ? '0.00 km' : `${distanceKm} km`}</Text>
          </View>
          <View style={styles.statPill}>
            <Text style={styles.statLabel}>⚡ Estimated Speed</Text>
            <Text style={styles.statVal}>{totalSeconds <= 0 ? '0 km/h' : `${currentSpeed} km/h`}</Text>
          </View>
        </View>
      </View>

      {/* Interactive simulation progress indicator */}
      <View style={styles.progressSection}>
        <View style={styles.trackContainer}>
          <View style={styles.trackBackground} />
          <Animated.View 
            style={[
              styles.trackFill, 
              trafficDelay > 0 && styles.trackFillTraffic,
              {
                width: progressAnim.interpolate({
                  inputRange: [0, 1],
                  outputRange: ['0%', '100%'],
                })
              }
            ]} 
          />
          
          {/* Pulsing worker node along track */}
          <Animated.View 
            style={[
              styles.workerNodePosition, 
              {
                left: progressAnim.interpolate({
                  inputRange: [0, 1],
                  outputRange: ['0%', '92%'],
                })
              }
            ]}
          >
            <View style={[styles.workerPin, trafficDelay > 0 && styles.workerPinTraffic]}>
              <Text style={styles.workerPinIcon}>{trafficDelay > 0 ? '⚠️' : '🛵'}</Text>
            </View>
          </Animated.View>
        </View>

        {/* Custom landmarks */}
        <View style={styles.landmarksRow}>
          <View style={styles.landmark}>
            <Text style={styles.landmarkIcon}>🏢</Text>
            <Text style={styles.landmarkText}>Hub</Text>
          </View>
          <View style={styles.landmark}>
            <Text style={styles.landmarkIcon}>🚦</Text>
            <Text style={styles.landmarkText}>Main Rd</Text>
          </View>
          <View style={styles.landmark}>
            <Text style={styles.landmarkIcon}>🏠</Text>
            <Text style={styles.landmarkText}>Home</Text>
          </View>
        </View>
      </View>

      {/* Dynamic Simulation Event Banner */}
      <View style={[
        styles.eventBanner,
        totalSeconds <= 0 ? styles.bannerSuccess : (trafficDelay > 0 ? styles.bannerWarning : styles.bannerNormal)
      ]}>
        <Text style={[
          styles.eventText,
          totalSeconds <= 0 ? styles.textSuccess : (trafficDelay > 0 ? styles.textWarning : styles.textNormal)
        ]}>
          {totalSeconds <= 0 ? (
            `🎉 ${workerName} has successfully arrived! Confirm service completion below.`
          ) : trafficDelay > 0 ? (
            `⚠️ Road congestion alert: ${workerName} is delayed. Speed reduced to 10 km/h.`
          ) : simulationSpeed > 1 ? (
            `⏩ Fast-forward simulation active (${simulationSpeed}x speed).`
          ) : (
            `ℹ️ ${workerName} is moving steadily. Passing through commercial segments.`
          )}
        </Text>
      </View>

      {/* Simulator Sandbox Controls */}
      <View style={styles.sandboxContainer}>
        <Text style={styles.sandboxLabel}>SIMULATION CONTROLS</Text>
        <View style={styles.sandboxButtonsRow}>
          <TouchableOpacity 
            style={[styles.sandboxBtn, styles.delayBtn]} 
            onPress={handleTriggerTrafficDelay}
            disabled={totalSeconds <= 0}
          >
            <Text style={styles.delayBtnText}>🚦 Inject Congestion (+2m)</Text>
          </TouchableOpacity>

          <TouchableOpacity 
            style={[styles.sandboxBtn, styles.speedBtn, simulationSpeed > 1 && styles.speedBtnActive]} 
            onPress={handleSpeedUpSimulation}
            disabled={totalSeconds <= 0}
          >
            <Text style={[styles.speedBtnText, simulationSpeed > 1 && styles.speedBtnTextActive]}>
              {simulationSpeed > 1 ? '⏸️ Standard Speed (1x)' : '⏩ Fast Forward (5x)'}
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 16,
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.04,
    shadowRadius: 12,
    elevation: 3,
    marginBottom: 16,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 16,
  },
  title: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
    letterSpacing: -0.3,
  },
  subtitle: {
    fontSize: 11,
    color: '#64748B',
    fontWeight: '600',
    marginTop: 2,
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 8,
    borderWidth: 1,
  },
  badgeEnroute: {
    backgroundColor: '#EEF2FF',
    borderColor: '#818CF8',
  },
  badgeArrived: {
    backgroundColor: '#ECFDF5',
    borderColor: '#34D399',
  },
  statusBadgeText: {
    fontSize: 10,
    fontWeight: '800',
    color: '#4F46E5',
    letterSpacing: 0.5,
  },
  timerGrid: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
    marginBottom: 16,
    backgroundColor: '#F8FAFC',
    borderRadius: 16,
    padding: 14,
    borderWidth: 1,
    borderColor: '#F1F5F9',
  },
  timerCircle: {
    width: 90,
    height: 90,
    borderRadius: 45,
    backgroundColor: '#EEF2FF',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
    borderWidth: 2,
    borderColor: '#C7D2FE',
  },
  pulseInner: {
    position: 'absolute',
    width: 76,
    height: 76,
    borderRadius: 38,
    backgroundColor: 'rgba(199, 210, 254, 0.25)',
  },
  timerNumberBox: {
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 10,
  },
  timerValue: {
    fontSize: 22,
    fontWeight: '900',
    color: '#4F46E5',
    fontVariant: ['tabular-nums'],
  },
  timerLabel: {
    fontSize: 7.5,
    fontWeight: '800',
    color: '#6366F1',
    marginTop: 1,
    letterSpacing: 0.2,
  },
  statsWrapper: {
    flex: 1,
    gap: 8,
  },
  statPill: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 10,
    paddingVertical: 6,
    paddingHorizontal: 12,
  },
  statLabel: {
    fontSize: 9,
    fontWeight: '700',
    color: '#64748B',
  },
  statVal: {
    fontSize: 13,
    fontWeight: '800',
    color: '#0F172A',
    marginTop: 2,
  },
  progressSection: {
    marginBottom: 16,
  },
  trackContainer: {
    height: 24,
    justifyContent: 'center',
    position: 'relative',
    marginHorizontal: 8,
  },
  trackBackground: {
    height: 6,
    backgroundColor: '#E2E8F0',
    borderRadius: 3,
    width: '100%',
  },
  trackFill: {
    height: 6,
    backgroundColor: '#4F46E5',
    borderRadius: 3,
    position: 'absolute',
  },
  trackFillTraffic: {
    backgroundColor: '#F59E0B',
  },
  workerNodePosition: {
    position: 'absolute',
    transform: [{ translateX: -12 }],
  },
  workerPin: {
    width: 26,
    height: 26,
    borderRadius: 13,
    backgroundColor: '#4F46E5',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.5,
    borderColor: '#FFFFFF',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 1,
    elevation: 3,
  },
  workerPinTraffic: {
    backgroundColor: '#F59E0B',
  },
  workerPinIcon: {
    fontSize: 13,
  },
  landmarksRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 4,
    marginTop: 4,
  },
  landmark: {
    alignItems: 'center',
  },
  landmarkIcon: {
    fontSize: 12,
  },
  landmarkText: {
    fontSize: 9,
    fontWeight: '700',
    color: '#94A3B8',
    marginTop: 2,
  },
  eventBanner: {
    borderRadius: 12,
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderWidth: 1,
    marginBottom: 16,
  },
  bannerNormal: {
    backgroundColor: '#F0FDF4',
    borderColor: '#DCFCE7',
  },
  bannerWarning: {
    backgroundColor: '#FFFBEB',
    borderColor: '#FEF3C7',
  },
  bannerSuccess: {
    backgroundColor: '#ECFDF5',
    borderColor: '#D1FAE5',
  },
  eventText: {
    fontSize: 11,
    fontWeight: '700',
    lineHeight: 16,
  },
  textNormal: {
    color: '#16A34A',
  },
  textWarning: {
    color: '#D97706',
  },
  textSuccess: {
    color: '#059669',
  },
  sandboxContainer: {
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    padding: 10,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  sandboxLabel: {
    fontSize: 8.5,
    fontWeight: '800',
    color: '#94A3B8',
    letterSpacing: 1,
    marginBottom: 8,
  },
  sandboxButtonsRow: {
    flexDirection: 'row',
    gap: 8,
  },
  sandboxBtn: {
    flex: 1,
    paddingVertical: 8,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
  },
  delayBtn: {
    backgroundColor: '#FFFBEB',
    borderColor: '#FCD34D',
  },
  delayBtnText: {
    fontSize: 10,
    fontWeight: '700',
    color: '#B45309',
  },
  speedBtn: {
    backgroundColor: '#F1F5F9',
    borderColor: '#CBD5E1',
  },
  speedBtnActive: {
    backgroundColor: '#4F46E5',
    borderColor: '#4F46E5',
  },
  speedBtnText: {
    fontSize: 10,
    fontWeight: '700',
    color: '#475569',
  },
  speedBtnTextActive: {
    color: '#FFFFFF',
  },
});
