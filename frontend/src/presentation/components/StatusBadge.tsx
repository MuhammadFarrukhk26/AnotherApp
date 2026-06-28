import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

interface StatusBadgeProps {
  status: string;
}

interface StatusConfig {
  label: string;
  backgroundColor: string;
  textColor: string;
  borderColor: string;
  dotColor: string;
  icon: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const normalizedStatus = status?.toUpperCase() || 'PENDING';

  const getStatusConfig = (currentStatus: string): StatusConfig => {
    switch (currentStatus) {
      case 'PENDING':
        return {
          label: 'Pending Confirmation',
          backgroundColor: '#FFFBEB', // Amber 50
          textColor: '#B45309', // Amber 700
          borderColor: '#FDE68A', // Amber 200
          dotColor: '#F59E0B', // Amber 500
          icon: '⏳',
        };
      case 'ACCEPTED':
        return {
          label: 'Booking Confirmed',
          backgroundColor: '#EFF6FF', // Blue 50
          textColor: '#1D4ED8', // Blue 700
          borderColor: '#BFDBFE', // Blue 200
          dotColor: '#3B82F6', // Blue 500
          icon: '📅',
        };
      case 'EN_ROUTE':
      case 'ENROUTE':
        return {
          label: 'Provider En Route',
          backgroundColor: '#F0F9FF', // Sky 50
          textColor: '#0369A1', // Sky 700
          borderColor: '#BAE6FD', // Sky 200
          dotColor: '#0EA5E9', // Sky 500
          icon: '🛵',
        };
      case 'ARRIVED':
        return {
          label: 'Provider Arrived',
          backgroundColor: '#F0FDFA', // Teal 50
          textColor: '#0F766E', // Teal 700
          borderColor: '#99F6E4', // Teal 200
          dotColor: '#14B8A6', // Teal 500
          icon: '📍',
        };
      case 'IN_PROGRESS':
      case 'INPROGRESS':
        return {
          label: 'Service In Progress',
          backgroundColor: '#ECFDF5', // Emerald 50
          textColor: '#047857', // Emerald 700
          borderColor: '#A7F3D0', // Emerald 200
          dotColor: '#10B981', // Emerald 500
          icon: '⚡',
        };
      case 'COMPLETED':
        return {
          label: 'Service Completed',
          backgroundColor: '#F0FDF4', // Green 50
          textColor: '#15803D', // Green 700
          borderColor: '#BBF7D0', // Green 200
          dotColor: '#22C55E', // Green 500
          icon: '🎉',
        };
      default:
        return {
          label: currentStatus,
          backgroundColor: '#F8FAFC', // Slate 50
          textColor: '#475569', // Slate 700
          borderColor: '#E2E8F0', // Slate 200
          dotColor: '#64748B', // Slate 500
          icon: '📝',
        };
    }
  };

  const config = getStatusConfig(normalizedStatus);

  return (
    <View style={[styles.badge, { backgroundColor: config.backgroundColor, borderColor: config.borderColor }]}>
      <Text style={styles.icon}>{config.icon}</Text>
      <View style={[styles.dot, { backgroundColor: config.dotColor }]} />
      <Text style={[styles.label, { color: config.textColor }]}>
        {config.label}
      </Text>
    </View>
  );
};

const styles = StyleSheet.create({
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 5,
    paddingHorizontal: 12,
    borderRadius: 20,
    borderWidth: 1.2,
  },
  icon: {
    fontSize: 12,
    marginRight: 6,
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    marginRight: 6,
  },
  label: {
    fontSize: 11.5,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
});
