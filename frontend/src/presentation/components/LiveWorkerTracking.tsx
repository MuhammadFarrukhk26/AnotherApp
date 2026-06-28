import React, { useState, useEffect, useRef } from 'react';
import { View, Text, StyleSheet, Dimensions, Animated, ActivityIndicator, TouchableOpacity } from 'react-native';

// High-contrast map styles complying with WCAG standards for clear visibility
const highContrastMapStyle = [
  {
    elementType: 'geometry',
    stylers: [{ color: '#f8fafc' }],
  },
  {
    elementType: 'labels.icon',
    stylers: [{ visibility: 'on' }],
  },
  {
    elementType: 'labels.text.fill',
    stylers: [{ color: '#0f172a' }],
  },
  {
    elementType: 'labels.text.stroke',
    stylers: [{ color: '#ffffff' }, { weight: 4 }],
  },
  {
    featureType: 'administrative',
    elementType: 'geometry',
    stylers: [{ color: '#334155' }, { weight: 1.5 }],
  },
  {
    featureType: 'poi',
    elementType: 'geometry',
    stylers: [{ color: '#e2e8f0' }],
  },
  {
    featureType: 'road',
    elementType: 'geometry',
    stylers: [{ color: '#ffffff' }],
  },
  {
    featureType: 'road.highway',
    elementType: 'geometry',
    stylers: [{ color: '#cbd5e1' }, { weight: 3 }],
  },
  {
    featureType: 'transit',
    stylers: [{ visibility: 'off' }],
  },
  {
    featureType: 'water',
    elementType: 'geometry',
    stylers: [{ color: '#93c5fd' }],
  },
];

const getMapStyle = (showTransit: boolean) => {
  return highContrastMapStyle.map(style => {
    if (style.featureType === 'transit') {
      return {
        ...style,
        stylers: [{ visibility: showTransit ? 'on' : 'off' }, { color: '#8b5cf6' }]
      };
    }
    return style;
  });
};

// Safely try to import react-native-maps to avoid crashes if native packages are not linked
let MapView: any = null;
let Marker: any = null;
let Polyline: any = null;

try {
  const Maps = require('react-native-maps');
  MapView = Maps.default || Maps.MapView;
  Marker = Maps.Marker;
  Polyline = Maps.Polyline;
} catch (e) {
  console.log('[LiveWorkerTracking] react-native-maps not loaded, using beautiful vector fallback');
}

interface LiveWorkerTrackingProps {
  customerLat: number;
  customerLng: number;
  workerId?: string;
}

export const LiveWorkerTracking: React.FC<LiveWorkerTrackingProps> = ({
  customerLat,
  customerLng,
  workerId,
}) => {
  const [hasNativeMaps, setHasNativeMaps] = useState<boolean>(!!MapView);
  const [workerCoords, setWorkerCoords] = useState<{ lat: number; lng: number }>({
    lat: customerLat + 0.008,
    lng: customerLng - 0.006,
  });
  const [progress, setProgress] = useState<number>(0);
  const [showTraffic, setShowTraffic] = useState<boolean>(false);
  const [showTransit, setShowTransit] = useState<boolean>(false);
  const routeAnim = useRef(new Animated.Value(0)).current;
  const mapRef = useRef<any>(null);

  // Automatically fit map bounds to include both customer and worker coordinates with appropriate padding
  useEffect(() => {
    if (hasNativeMaps && mapRef.current) {
      try {
        mapRef.current.fitToCoordinates(
          [
            { latitude: customerLat, longitude: customerLng },
            { latitude: workerCoords.lat, longitude: workerCoords.lng },
          ],
          {
            edgePadding: { top: 80, right: 80, bottom: 80, left: 80 },
            animated: true,
          }
        );
      } catch (error) {
        console.log('[LiveWorkerTracking] fitToCoordinates failed:', error);
      }
    }
  }, [customerLat, customerLng, workerCoords.lat, workerCoords.lng, hasNativeMaps]);

  // Move worker closer to customer over time
  useEffect(() => {
    const interval = setInterval(() => {
      setProgress((prev) => {
        const next = Math.min(prev + 0.05, 1);
        
        // Calculate current position between start and end
        const startLat = customerLat + 0.008;
        const startLng = customerLng - 0.006;
        
        const currentLat = startLat + (customerLat - startLat) * next;
        const currentLng = startLng + (customerLng - startLng) * next;
        
        setWorkerCoords({ lat: currentLat, lng: currentLng });
        
        if (next >= 1) {
          clearInterval(interval);
        }
        return next;
      });
    }, 4000);

    Animated.loop(
      Animated.timing(routeAnim, {
        toValue: 1,
        duration: 3000,
        useNativeDriver: true,
      })
    ).start();

    return () => clearInterval(interval);
  }, [customerLat, customerLng]);

  // If react-native-maps is available, render the actual interactive map
  if (hasNativeMaps && MapView && Marker) {
    const initialRegion = {
      latitude: (customerLat + workerCoords.lat) / 2,
      longitude: (customerLng + workerCoords.lng) / 2,
      latitudeDelta: 0.02,
      longitudeDelta: 0.02,
    };

    return (
      <View 
        style={styles.container}
        accessible={true}
        accessibilityLabel="Live tracking map container"
      >
        <View style={styles.mapHeaderRow}>
          <Text style={styles.title} accessibilityRole="header">Live Routing Map</Text>
          <View style={styles.layerControls}>
            <TouchableOpacity
              style={[styles.layerButton, showTraffic && styles.layerButtonActive]}
              onPress={() => setShowTraffic(!showTraffic)}
              accessible={true}
              accessibilityLabel={`Toggle Traffic Congestion Overlay. Currently ${showTraffic ? 'on' : 'off'}.`}
              accessibilityRole="checkbox"
              accessibilityState={{ checked: showTraffic }}
            >
              <Text style={[styles.layerButtonText, showTraffic && styles.layerButtonTextActive]}>🚦 Traffic</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.layerButton, showTransit && styles.layerButtonActive]}
              onPress={() => setShowTransit(!showTransit)}
              accessible={true}
              accessibilityLabel={`Toggle Transit Routes. Currently ${showTransit ? 'on' : 'off'}.`}
              accessibilityRole="checkbox"
              accessibilityState={{ checked: showTransit }}
            >
              <Text style={[styles.layerButtonText, showTransit && styles.layerButtonTextActive]}>🚇 Transit</Text>
            </TouchableOpacity>
          </View>
        </View>

        <View style={styles.mapWrapper}>
          <MapView 
            ref={mapRef}
            style={styles.map} 
            initialRegion={initialRegion}
            customMapStyle={getMapStyle(showTransit)}
            showsTraffic={showTraffic}
            accessible={true}
            accessibilityLabel="High contrast interactive tracking map detailing en-route progress of the technician to your home."
            accessibilityRole="image"
            onMapReady={() => {
              if (mapRef.current) {
                try {
                  mapRef.current.fitToCoordinates(
                    [
                      { latitude: customerLat, longitude: customerLng },
                      { latitude: workerCoords.lat, longitude: workerCoords.lng },
                    ],
                    {
                      edgePadding: { top: 80, right: 80, bottom: 80, left: 80 },
                      animated: false,
                    }
                  );
                } catch (e) {
                  console.log('[LiveWorkerTracking] initial fitToCoordinates failed:', e);
                }
              }
            }}
          >
            <Marker
              coordinate={{ latitude: customerLat, longitude: customerLng }}
              title="Your Location"
              description="Service Destination"
              accessible={true}
              accessibilityLabel="Destination point. Your home address."
              accessibilityRole="image"
              accessibilityHint="Double-tap to focus destination info."
            >
              <View style={styles.customUserMarkerContainer}>
                <View style={styles.pulseRing} />
                <View style={styles.userMarkerPin}>
                  <Text style={styles.markerEmoji}>🏠</Text>
                </View>
              </View>
            </Marker>
            <Marker
              coordinate={{ latitude: workerCoords.lat, longitude: workerCoords.lng }}
              title="Provider (Ahmed)"
              description="En Route"
              accessible={true}
              accessibilityLabel={`Technician Ahmed en route to your home. Current location is latitude ${workerCoords.lat.toFixed(4)}, longitude ${workerCoords.lng.toFixed(4)}.`}
              accessibilityRole="image"
              accessibilityHint="Double-tap to announce technician route details."
            >
              <View style={styles.customWorkerMarkerContainer}>
                <View style={styles.pulseRingWorker} />
                <View style={styles.workerMarkerPin}>
                  <Text style={styles.markerEmoji}>🛵</Text>
                </View>
              </View>
            </Marker>
            {Polyline && (
              <Polyline
                coordinates={[
                  { latitude: workerCoords.lat, longitude: workerCoords.lng },
                  { latitude: customerLat, longitude: customerLng },
                ]}
                strokeColor="#10B981"
                strokeWidth={4}
              />
            )}
          </MapView>
          
          {showTraffic && (
            <View 
              style={styles.trafficBanner}
              accessible={true}
              accessibilityLabel="Traffic congestion warning. Minor delays on Shahrah-e-Faisal route."
              accessibilityRole="alert"
            >
              <Text style={styles.trafficBannerText}>⚠️ Moderate congestion on route. Potential delay +4 mins.</Text>
            </View>
          )}

          {showTransit && (
            <View 
              style={styles.transitBanner}
              accessible={true}
              accessibilityLabel="Transit overlay active. Displaying subway and shuttle routes."
              accessibilityRole="alert"
            >
              <Text style={styles.transitBannerText}>🚇 Rapid Transit lanes active nearby. Alternative roads clear.</Text>
            </View>
          )}
        </View>
      </View>
    );
  }

  // Beautiful visual dynamic vector route-tracking fallback
  return (
    <View 
      style={styles.container}
      accessible={true}
      accessibilityLabel="Live Route Tracking fall-back dashboard"
    >
      <View style={styles.headerRow}>
        <View style={{ flex: 1 }}>
          <Text style={styles.title} accessibilityRole="header">Live Route Tracking Map</Text>
          <View style={[styles.fallbackBadge, { alignSelf: 'flex-start', marginTop: 4 }]}>
            <Text style={styles.fallbackText}>INTERACTIVE VECTOR</Text>
          </View>
        </View>
        
        <View style={styles.layerControls}>
          <TouchableOpacity
            style={[styles.layerButton, showTraffic && styles.layerButtonActive]}
            onPress={() => setShowTraffic(!showTraffic)}
            accessible={true}
            accessibilityLabel={`Toggle Traffic Congestion Overlay. Currently ${showTraffic ? 'on' : 'off'}.`}
            accessibilityRole="checkbox"
            accessibilityState={{ checked: showTraffic }}
          >
            <Text style={[styles.layerButtonText, showTraffic && styles.layerButtonTextActive]}>🚦 Traffic</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.layerButton, showTransit && styles.layerButtonActive]}
            onPress={() => setShowTransit(!showTransit)}
            accessible={true}
            accessibilityLabel={`Toggle Transit Routes. Currently ${showTransit ? 'on' : 'off'}.`}
            accessibilityRole="checkbox"
            accessibilityState={{ checked: showTransit }}
          >
            <Text style={[styles.layerButtonText, showTransit && styles.layerButtonTextActive]}>🚇 Transit</Text>
          </TouchableOpacity>
        </View>
      </View>

      <View 
        style={styles.mapFallbackContainer}
        accessible={true}
        accessibilityLabel={`Visual Route Tracking Track. Progress is ${Math.round(progress * 100)} percent.`}
        accessibilityRole="image"
      >
        {/* Simple mock street network background */}
        <View style={styles.mockStreetV1} />
        <View style={styles.mockStreetV2} />
        <View style={styles.mockStreetH1} />
        <View style={styles.mockStreetH2} />

        {/* Traffic Overlays */}
        {showTraffic && (
          <View style={styles.vectorTrafficOverlay}>
            <Text style={styles.vectorTrafficEmoji}>🚗🚙🚗🚙🚗</Text>
          </View>
        )}

        {/* Transit Overlays */}
        {showTransit && (
          <View style={styles.vectorTransitOverlay}>
            <Text style={styles.vectorTransitEmoji}>🚏 Transit Line 42</Text>
          </View>
        )}

        {/* Start / Hub point */}
        <View 
          style={[styles.node, styles.startNode]}
          accessible={true}
          accessibilityLabel="Service Hub office starting point."
          accessibilityRole="text"
        >
          <View style={styles.hubMarkerPin}>
            <Text style={styles.hubMarkerEmoji}>🏢</Text>
          </View>
          <Text style={styles.nodeLabel}>Service Hub</Text>
        </View>

        {/* Animated route indicator */}
        <View style={styles.pathLine}>
          <View style={[styles.pathLineFilled, showTraffic && styles.pathLineFilledTraffic, { width: `${progress * 100}%` }]} />
        </View>

        {/* Animated worker node */}
        <View 
          style={[styles.node, { left: `${14 + progress * 62}%`, zIndex: 10 }]}
          accessible={true}
          accessibilityLabel={`Technician Ahmed en route on scooter. ${progress >= 1 ? 'Arrived at your home.' : Math.round((1 - progress) * 100) + ' percent remaining.'}`}
          accessibilityRole="text"
        >
          <View style={styles.customWorkerMarkerContainer}>
            <View style={styles.pulseRingWorker} />
            <View style={styles.workerMarkerPin}>
              <Text style={styles.markerEmoji}>🛵</Text>
            </View>
          </View>
          <Text style={styles.nodeLabelMoving}>Technician</Text>
        </View>

        {/* Destination point */}
        <View 
          style={[styles.node, styles.destNode]}
          accessible={true}
          accessibilityLabel="Your home destination point."
          accessibilityRole="text"
        >
          <View style={styles.customUserMarkerContainer}>
            <View style={styles.pulseRing} />
            <View style={styles.userMarkerPin}>
              <Text style={styles.markerEmoji}>🏠</Text>
            </View>
          </View>
          <Text style={styles.nodeLabel}>You</Text>
        </View>

        {/* Floating status display overlay */}
        <View 
          style={styles.floatingStatus}
          accessible={true}
          accessibilityLabel={`Status update: ${progress >= 1 ? 'Worker Arrived' : 'En Route: ' + Math.round((1 - progress) * 100) + ' percent remaining'}`}
          accessibilityRole="alert"
        >
          <Text style={styles.statusText}>
            {progress >= 1 ? '🎉 Worker Arrived' : `📍 En Route: ${Math.round((1 - progress) * 100)}% remaining`}
          </Text>
        </View>
      </View>

      {showTraffic && (
        <View 
          style={styles.vectorAlertBanner}
          accessible={true}
          accessibilityLabel="Traffic congestion alert. Potential delay +4 minutes on route segments."
          accessibilityRole="alert"
        >
          <Text style={styles.vectorAlertText}>⚠️ Traffic Alert: Delay detected on connecting segments. +4 min delay expected.</Text>
        </View>
      )}

      {showTransit && (
        <View 
          style={[styles.vectorAlertBanner, { backgroundColor: '#F5F3FF', borderColor: '#DDD6FE' }]}
          accessible={true}
          accessibilityLabel="Transit routing information active."
          accessibilityRole="alert"
        >
          <Text style={[styles.vectorAlertText, { color: '#6D28D9' }]}>🚇 Transit Info: Technician travels via Rapid Transit Bus (RTB) corridors.</Text>
        </View>
      )}
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
  mapHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  layerControls: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  layerButton: {
    backgroundColor: '#F1F5F9',
    paddingVertical: 4,
    paddingHorizontal: 10,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  layerButtonActive: {
    backgroundColor: '#0F172A',
    borderColor: '#0F172A',
  },
  layerButtonText: {
    fontSize: 11,
    fontWeight: '700',
    color: '#475569',
  },
  layerButtonTextActive: {
    color: '#FFFFFF',
  },
  mapWrapper: {
    position: 'relative',
    marginTop: 8,
  },
  trafficBanner: {
    position: 'absolute',
    bottom: 12,
    left: 12,
    right: 12,
    backgroundColor: 'rgba(254, 243, 199, 0.95)',
    borderWidth: 1,
    borderColor: '#FCD34D',
    borderRadius: 8,
    paddingVertical: 6,
    paddingHorizontal: 12,
    zIndex: 99,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
    elevation: 2,
  },
  trafficBannerText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#92400E',
    textAlign: 'center',
  },
  transitBanner: {
    position: 'absolute',
    top: 12,
    left: 12,
    right: 12,
    backgroundColor: 'rgba(245, 243, 255, 0.95)',
    borderWidth: 1,
    borderColor: '#DDD6FE',
    borderRadius: 8,
    paddingVertical: 6,
    paddingHorizontal: 12,
    zIndex: 99,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
    elevation: 2,
  },
  transitBannerText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#6D28D9',
    textAlign: 'center',
  },
  vectorTrafficOverlay: {
    position: 'absolute',
    width: '100%',
    height: 30,
    top: '25%',
    backgroundColor: 'rgba(239, 68, 68, 0.15)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(239, 68, 68, 0.25)',
  },
  vectorTrafficEmoji: {
    fontSize: 12,
    letterSpacing: 4,
    opacity: 0.8,
  },
  vectorTransitOverlay: {
    position: 'absolute',
    width: '100%',
    height: 24,
    bottom: '15%',
    backgroundColor: 'rgba(139, 92, 246, 0.15)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(139, 92, 246, 0.25)',
  },
  vectorTransitEmoji: {
    fontSize: 10,
    fontWeight: '800',
    color: '#6D28D9',
    opacity: 0.9,
  },
  pathLineFilledTraffic: {
    backgroundColor: '#EF4444',
  },
  vectorAlertBanner: {
    backgroundColor: '#FEF3C7',
    borderWidth: 1,
    borderColor: '#FCD34D',
    borderRadius: 10,
    padding: 10,
    marginTop: 10,
  },
  vectorAlertText: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#92400E',
    lineHeight: 16,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  title: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  fallbackBadge: {
    backgroundColor: '#EFF6FF',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#BFDBFE',
  },
  fallbackText: {
    color: '#2563EB',
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  map: {
    width: '100%',
    height: 180,
    borderRadius: 12,
    marginTop: 8,
  },
  mapFallbackContainer: {
    width: '100%',
    height: 180,
    backgroundColor: '#F1F5F9',
    borderRadius: 12,
    position: 'relative',
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    justifyContent: 'center',
    alignItems: 'center',
  },
  mockStreetV1: {
    position: 'absolute',
    width: 24,
    height: '100%',
    backgroundColor: '#E2E8F0',
    left: '30%',
  },
  mockStreetV2: {
    position: 'absolute',
    width: 24,
    height: '100%',
    backgroundColor: '#E2E8F0',
    right: '25%',
  },
  mockStreetH1: {
    position: 'absolute',
    width: '100%',
    height: 24,
    backgroundColor: '#E2E8F0',
    top: '35%',
  },
  mockStreetH2: {
    position: 'absolute',
    width: '100%',
    height: 24,
    backgroundColor: '#E2E8F0',
    bottom: '25%',
  },
  pathLine: {
    width: '65%',
    height: 6,
    backgroundColor: '#CBD5E1',
    borderRadius: 3,
    position: 'absolute',
    top: '50%',
    left: '18%',
  },
  pathLineFilled: {
    height: '100%',
    backgroundColor: '#10B981',
    borderRadius: 3,
  },
  node: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
    top: '40%',
  },
  startNode: {
    left: '10%',
  },
  destNode: {
    right: '10%',
  },
  nodeLabel: {
    fontSize: 9.5,
    fontWeight: '800',
    color: '#64748B',
    marginTop: 6,
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
    borderWidth: 0.5,
    borderColor: '#E2E8F0',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 1,
    elevation: 1,
  },
  nodeLabelMoving: {
    fontSize: 9.5,
    fontWeight: '800',
    color: '#10B981',
    marginTop: 6,
    backgroundColor: '#ECFDF5',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
    borderWidth: 0.5,
    borderColor: '#A7F3D0',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 1,
    elevation: 1,
  },
  customUserMarkerContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 48,
    height: 48,
  },
  customWorkerMarkerContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 48,
    height: 48,
  },
  userMarkerPin: {
    backgroundColor: '#1E40AF', // Navy
    borderRadius: 20,
    width: 36,
    height: 36,
    borderWidth: 2,
    borderColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  workerMarkerPin: {
    backgroundColor: '#10B981', // Emerald Green
    borderRadius: 20,
    width: 36,
    height: 36,
    borderWidth: 2,
    borderColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  hubMarkerPin: {
    backgroundColor: '#64748B', // Slate
    borderRadius: 20,
    width: 36,
    height: 36,
    borderWidth: 2,
    borderColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  pulseRing: {
    position: 'absolute',
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: 'rgba(30, 64, 175, 0.15)',
    borderWidth: 1,
    borderColor: 'rgba(30, 64, 175, 0.3)',
  },
  pulseRingWorker: {
    position: 'absolute',
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: 'rgba(16, 185, 129, 0.15)',
    borderWidth: 1,
    borderColor: 'rgba(16, 185, 129, 0.3)',
  },
  markerEmoji: {
    fontSize: 16,
  },
  hubMarkerEmoji: {
    fontSize: 16,
  },
  floatingStatus: {
    position: 'absolute',
    bottom: 12,
    backgroundColor: 'rgba(15, 23, 42, 0.85)',
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 20,
    alignSelf: 'center',
  },
  statusText: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: '700',
  },
});
