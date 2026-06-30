import React, { createContext, useContext, useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Animated,
  TouchableOpacity,
  Dimensions,
  Image,
  PanResponder,
} from 'react-native';

export interface NotificationPayload {
  id: string;
  title: string;
  message: string;
  icon?: string;
  avatar?: string;
  onPress?: () => void;
}

interface NotificationContextType {
  showNotification: (title: string, message: string, options?: Omit<NotificationPayload, 'id' | 'title' | 'message'>) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};

const { width } = Dimensions.get('window');

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [notification, setNotification] = useState<NotificationPayload | null>(null);
  const slideAnim = useRef(new Animated.Value(-120)).current;
  const opacityAnim = useRef(new Animated.Value(0)).current;
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  const showNotification = (
    title: string,
    message: string,
    options?: Omit<NotificationPayload, 'id' | 'title' | 'message'>
  ) => {
    // Clear any existing timer
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    const id = Math.random().toString(36).substr(2, 9);
    setNotification({
      id,
      title,
      message,
      ...options,
    });

    // Animate In
    Animated.parallel([
      Animated.spring(slideAnim, {
        toValue: 12, // Gap from the top
        useNativeDriver: true,
        bounciness: 8,
      }),
      Animated.timing(opacityAnim, {
        toValue: 1,
        duration: 250,
        useNativeDriver: true,
      }),
    ]).start();

    // Auto Dismiss after 4.5 seconds
    timeoutRef.current = setTimeout(() => {
      dismissNotification();
    }, 4500);
  };

  const dismissNotification = () => {
    Animated.parallel([
      Animated.timing(slideAnim, {
        toValue: -120,
        duration: 250,
        useNativeDriver: true,
      }),
      Animated.timing(opacityAnim, {
        toValue: 0,
        duration: 200,
        useNativeDriver: true,
      }),
    ]).start(() => {
      setNotification(null);
    });
  };

  // Simple gesture response to swipe up to dismiss
  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onPanResponderMove: (evt, gestureState) => {
        if (gestureState.dy < -15) {
          dismissNotification();
        }
      },
    })
  ).current;

  return (
    <NotificationContext.Provider value={{ showNotification }}>
      {children}

      {notification && (
        <Animated.View
          style={[
            styles.notificationCard,
            {
              transform: [{ translateY: slideAnim }],
              opacity: opacityAnim,
            },
          ]}
          {...panResponder.panHandlers}
        >
          <TouchableOpacity
            activeOpacity={0.9}
            style={styles.cardContent}
            onPress={() => {
              if (notification.onPress) {
                notification.onPress();
              }
              dismissNotification();
            }}
          >
            {/* Header branding */}
            <View style={styles.brandingRow}>
              <View style={styles.brandBadge}>
                <Text style={styles.brandIcon}>⚡</Text>
                <Text style={styles.brandText}>HAZIR SMART SYSTEM</Text>
              </View>
              <Text style={styles.timeText}>Just now</Text>
            </View>

            {/* Inner row containing icon/avatar + title + description */}
            <View style={styles.bodyRow}>
              {notification.avatar ? (
                <Image source={{ uri: notification.avatar }} style={styles.avatarImage} />
              ) : (
                <View style={styles.iconCircle}>
                  <Text style={styles.emojiIcon}>{notification.icon || '🔔'}</Text>
                </View>
              )}

              <View style={styles.textContainer}>
                <Text style={styles.title} numberOfLines={1}>
                  {notification.title}
                </Text>
                <Text style={styles.message} numberOfLines={2}>
                  {notification.message}
                </Text>
              </View>

              <TouchableOpacity style={styles.closeButton} onPress={dismissNotification}>
                <Text style={styles.closeButtonText}>✕</Text>
              </TouchableOpacity>
            </View>
          </TouchableOpacity>
        </Animated.View>
      )}
    </NotificationContext.Provider>
  );
};

const styles = StyleSheet.create({
  notificationCard: {
    position: 'absolute',
    top: 30, // Positioned safely near screen top
    left: 12,
    right: 12,
    zIndex: 9999,
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 12,
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    // Elevated shadow styling complying with material standards
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 8,
  },
  cardContent: {
    flex: 1,
  },
  brandingRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  brandBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#EFF6FF',
    borderRadius: 6,
    paddingHorizontal: 6,
    paddingVertical: 2,
    gap: 4,
  },
  brandIcon: {
    fontSize: 9,
  },
  brandText: {
    fontSize: 9,
    fontWeight: '800',
    color: '#1D4ED8',
    letterSpacing: 0.5,
  },
  timeText: {
    fontSize: 9,
    color: '#94A3B8',
    fontWeight: '600',
  },
  bodyRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  avatarImage: {
    width: 42,
    height: 42,
    borderRadius: 21,
    borderWidth: 1,
    borderColor: '#BFDBFE',
  },
  iconCircle: {
    width: 42,
    height: 42,
    borderRadius: 21,
    backgroundColor: '#F8FAFC',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  emojiIcon: {
    fontSize: 20,
  },
  textContainer: {
    flex: 1,
    paddingRight: 4,
  },
  title: {
    fontSize: 13,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  message: {
    fontSize: 11,
    color: '#475569',
    lineHeight: 14,
    fontWeight: '500',
  },
  closeButton: {
    padding: 4,
  },
  closeButtonText: {
    fontSize: 11,
    fontWeight: 'bold',
    color: '#94A3B8',
  },
});
