import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import axios from 'axios';

interface ETADisplayProps {
  customerLat: number;
  customerLng: number;
  workerId?: string;
  onArrived?: () => void;
}

export const ETADisplay: React.FC<ETADisplayProps> = ({
  customerLat,
  customerLng,
  workerId,
  onArrived,
}) => {
  const [eta, setEta] = useState<string>('Calculating...');
  const [distance, setDistance] = useState<string>('Calculating...');
  const [loading, setLoading] = useState<boolean>(true);
  const [workerCoords, setWorkerCoords] = useState<{ lat: number; lng: number } | null>(null);
  const [progress, setProgress] = useState<number>(0); // 0 to 1
  const [timeLeftSeconds, setTimeLeftSeconds] = useState<number>(0);

  // Sync remaining seconds with actual ETA
  useEffect(() => {
    const minsMatch = eta.match(/(\d+)/);
    const mins = minsMatch ? parseInt(minsMatch[1], 10) : 15;
    if (!isNaN(mins)) {
      const targetSeconds = mins * 60;
      // Adjust if we are not ticking yet, or if there is a big drift
      if (Math.abs(timeLeftSeconds - targetSeconds) > 30 || timeLeftSeconds === 0) {
        setTimeLeftSeconds(targetSeconds);
      }
    } else if (eta.toLowerCase().includes('arrived') || progress >= 1) {
      setTimeLeftSeconds(0);
    }
  }, [eta, progress]);

  // Live ticking countdown countdown
  useEffect(() => {
    if (timeLeftSeconds <= 0) return;
    const timer = setInterval(() => {
      setTimeLeftSeconds((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [timeLeftSeconds > 0]);

  const formatTime = (totalSeconds: number) => {
    if (totalSeconds <= 0) return '00:00';
    const m = Math.floor(totalSeconds / 60);
    const s = totalSeconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  // Set the worker's starting position slightly offset from the customer's coordinates
  useEffect(() => {
    if (customerLat && customerLng) {
      setWorkerCoords({
        lat: customerLat + 0.015, // Roughly 1.5 - 2 km away
        lng: customerLng - 0.012,
      });
      setProgress(0);
      setLoading(true);
    }
  }, [customerLat, customerLng]);

  // Simulate real-time worker movement closer to the destination
  useEffect(() => {
    if (!workerCoords) return;

    const interval = setInterval(() => {
      setProgress((prev) => {
        const next = prev + 0.08; // Increment movement
        if (next >= 1) {
          clearInterval(interval);
          setWorkerCoords({ lat: customerLat, lng: customerLng });
          if (onArrived) onArrived();
          return 1;
        }

        // Interpolate position
        const latOffset = (customerLat - (customerLat + 0.015)) * next;
        const lngOffset = (customerLng - (customerLng - 0.012)) * next;
        setWorkerCoords({
          lat: customerLat + 0.015 + latOffset,
          lng: customerLng - 0.012 + lngOffset,
        });

        return next;
      });
    }, 5000); // Update every 5 seconds

    return () => clearInterval(interval);
  }, [workerCoords === null]);

  // Fetch real Google Maps Distance Matrix API or calculate accurate geodesic fallback
  useEffect(() => {
    if (!workerCoords) return;

    let isMounted = true;
    const fetchDistanceMatrix = async () => {
      // API Key can be populated securely via BuildConfig / process.env / Secrets Panel
      const apiKey = ''; // User should configure this in AI Studio Secrets

      if (apiKey) {
        try {
          const url = `https://maps.googleapis.com/maps/api/distancematrix/json?origins=${workerCoords.lat},${workerCoords.lng}&destinations=${customerLat},${customerLng}&key=${apiKey}`;
          const response = await axios.get(url);
          
          if (isMounted && response.data.rows?.[0]?.elements?.[0]) {
            const element = response.data.rows[0].elements[0];
            if (element.status === 'OK') {
              setEta(element.duration.text);
              setDistance(element.distance.text);
              setLoading(false);
              return;
            }
          }
        } catch (error) {
          console.error('[Distance Matrix API Error]:', error);
        }
      }

      // High-fidelity fallback / simulated realistic route calculations based on geodesic distance
      if (isMounted) {
        const R = 6371; // Earth radius in km
        const dLat = ((customerLat - workerCoords.lat) * Math.PI) / 180;
        const dLng = ((customerLng - workerCoords.lng) * Math.PI) / 180;
        const a =
          Math.sin(dLat / 2) * Math.sin(dLat / 2) +
          Math.cos((workerCoords.lat * Math.PI) / 180) *
            Math.cos((customerLat * Math.PI) / 180) *
            Math.sin(dLng / 2) *
            Math.sin(dLng / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        const directKm = R * c;

        // Add 35% extra for routing detours
        const routingKm = directKm * 1.35;
        // Average city speed of 30 km/h (0.5 km per minute)
        const durationMins = Math.max(1, Math.round(routingKm / 0.5));

        if (progress >= 1) {
          setEta('Arrived');
          setDistance('0 km');
        } else {
          setEta(`${durationMins} mins`);
          setDistance(`${routingKm.toFixed(1)} km`);
        }
        setLoading(false);
      }
    };

    fetchDistanceMatrix();

    return () => {
      isMounted = false;
    };
  }, [workerCoords, customerLat, customerLng, progress]);

  return (
    <View style={styles.container}>
      <View style={styles.headerRow}>
        <Text style={styles.title}>Live ETA Tracking</Text>
        <View style={styles.liveBadge}>
          <View style={styles.pulseDot} />
          <Text style={styles.liveText}>LIVE</Text>
        </View>
      </View>

      <View style={styles.dashboardRow}>
        {/* Left Side: Circular Progress Countdown Timer */}
        <View style={styles.circularTimerContainer}>
          <View style={styles.circularTrackContainer}>
            {/* Background Circular Path Track */}
            <View style={styles.circularTrack} />
            
            {/* Destination Pinned at 12 o'clock */}
            <View style={styles.circularHomeIndicator}>
              <Text style={styles.circularHomeIcon}>🏠</Text>
            </View>

            {/* Dynamic Rotating Worker */}
            {(() => {
              const size = 110;
              const radius = 46;
              const centerX = size / 2;
              const centerY = size / 2;
              
              // 270-degree clockwise sweep from left (9 o'clock) to top (12 o'clock)
              const angle = Math.PI - progress * (1.5 * Math.PI);
              
              const left = centerX + radius * Math.cos(angle) - 12; // 12 is half of indicator size (24)
              const top = centerY + radius * Math.sin(angle) - 12;
              
              return (
                <View style={[styles.circularWorkerIndicator, { left, top }]}>
                  <Text style={styles.circularWorkerIcon}>🛵</Text>
                </View>
              );
            })()}

            {/* Central Timer readings */}
            <View style={styles.circularInnerContainer}>
              <Text style={styles.circularTimerLabel}>ETA TIMER</Text>
              {loading ? (
                <ActivityIndicator size="small" color="#10B981" />
              ) : (
                <Text style={styles.circularTimerValue}>
                  {progress >= 1 ? '00:00' : formatTime(timeLeftSeconds)}
                </Text>
              )}
              <Text style={styles.circularTimerUnit}>
                {progress >= 1 ? 'ARRIVED' : 'REMAINING'}
              </Text>
            </View>
          </View>
        </View>

        {/* Right Side: Key stats card stack */}
        <View style={styles.statsColumn}>
          <View style={styles.statItemCard}>
            <Text style={styles.statItemLabel}>⏱️ ESTIMATED ARRIVAL</Text>
            {loading ? (
              <ActivityIndicator size="small" color="#10B981" style={styles.statLoader} />
            ) : (
              <Text style={styles.statItemValue}>{eta}</Text>
            )}
          </View>
          
          <View style={styles.statItemCard}>
            <Text style={styles.statItemLabel}>📍 DISTANCE TO YOU</Text>
            {loading ? (
              <ActivityIndicator size="small" color="#10B981" style={styles.statLoader} />
            ) : (
              <Text style={styles.statItemValue}>{distance}</Text>
            )}
          </View>
        </View>
      </View>

      {/* Modern custom visual progress track */}
      <View style={styles.progressContainer}>
        <View style={styles.track} />
        <View style={[styles.filledTrack, { width: `${progress * 100}%` }]} />
        <View style={[styles.workerIndicator, { left: `${progress * 100}%` }]}>
          <Text style={styles.workerIcon}>🛵</Text>
        </View>
        <View style={styles.destinationIndicator}>
          <Text style={styles.homeIcon}>🏠</Text>
        </View>
      </View>

      <Text style={styles.statusDescription}>
        {progress >= 1
          ? 'Your provider has arrived at your location!'
          : 'Ahmed is driving towards your specified service location.'}
      </Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.05,
    shadowRadius: 10,
    elevation: 2,
    marginTop: 16,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  title: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  liveBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FEF2F2',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#FCA5A5',
  },
  pulseDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#EF4444',
    marginRight: 6,
  },
  liveText: {
    color: '#EF4444',
    fontSize: 10,
    fontWeight: '800',
  },
  dashboardRow: {
    flexDirection: 'row',
    gap: 16,
    alignItems: 'center',
    marginBottom: 18,
    backgroundColor: '#F8FAFC',
    borderRadius: 16,
    padding: 12,
    borderWidth: 1,
    borderColor: '#EFF6FF',
  },
  circularTimerContainer: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  circularTrackContainer: {
    width: 110,
    height: 110,
    position: 'relative',
  },
  circularTrack: {
    width: 110,
    height: 110,
    borderRadius: 55,
    borderWidth: 4,
    borderColor: '#E2E8F0',
    position: 'absolute',
  },
  circularHomeIndicator: {
    position: 'absolute',
    top: -4,
    alignSelf: 'center',
    zIndex: 5,
  },
  circularHomeIcon: {
    fontSize: 14,
  },
  circularWorkerIndicator: {
    position: 'absolute',
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#10B981',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.5,
    borderColor: '#FFFFFF',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 1,
    elevation: 3,
    zIndex: 5,
  },
  circularWorkerIcon: {
    fontSize: 12,
  },
  circularInnerContainer: {
    position: 'absolute',
    top: 6,
    left: 6,
    width: 98,
    height: 98,
    borderRadius: 49,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.03,
    shadowRadius: 2,
    elevation: 1,
  },
  circularTimerLabel: {
    fontSize: 8,
    fontWeight: '700',
    color: '#94A3B8',
    letterSpacing: 0.5,
    marginBottom: 2,
  },
  circularTimerValue: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
  },
  circularTimerUnit: {
    fontSize: 8,
    fontWeight: '800',
    color: '#10B981',
    letterSpacing: 0.5,
    marginTop: 2,
  },
  statsColumn: {
    flex: 1,
    gap: 10,
  },
  statItemCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.02,
    shadowRadius: 2,
    elevation: 1,
  },
  statItemLabel: {
    fontSize: 9,
    fontWeight: '700',
    color: '#64748B',
    letterSpacing: 0.5,
    marginBottom: 4,
  },
  statItemValue: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
  },
  statLoader: {
    alignSelf: 'flex-start',
    marginVertical: 2,
  },
  progressContainer: {
    height: 24,
    justifyContent: 'center',
    position: 'relative',
    marginHorizontal: 12,
    marginBottom: 12,
  },
  track: {
    height: 6,
    backgroundColor: '#E2E8F0',
    borderRadius: 3,
    width: '100%',
  },
  filledTrack: {
    height: 6,
    backgroundColor: '#10B981',
    borderRadius: 3,
    position: 'absolute',
    left: 0,
  },
  workerIndicator: {
    position: 'absolute',
    transform: [{ translateX: -12 }],
    justifyContent: 'center',
    alignItems: 'center',
  },
  workerIcon: {
    fontSize: 18,
  },
  destinationIndicator: {
    position: 'absolute',
    right: -10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  homeIcon: {
    fontSize: 18,
  },
  statusDescription: {
    fontSize: 12,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 18,
  },
});
