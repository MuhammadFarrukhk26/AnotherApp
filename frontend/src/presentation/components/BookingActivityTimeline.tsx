import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

interface TimelineEvent {
  id: string;
  title: string;
  description: string;
  time: string;
  status: 'completed' | 'active' | 'pending';
  icon: string;
}

interface BookingActivityTimelineProps {
  currentStatus: string;
}

export const BookingActivityTimeline: React.FC<BookingActivityTimelineProps> = ({ currentStatus }) => {
  // Map booking status value to chronological indices for timeline progression
  const getStatusWeight = (status: string): number => {
    switch (status.toUpperCase()) {
      case 'PENDING':
        return 0;
      case 'ASSIGNED':
      case 'CONFIRMED':
        return 1;
      case 'EN_ROUTE':
      case 'TRAVELING':
        return 2;
      case 'ARRIVED':
      case 'IN_PROGRESS':
        return 3;
      case 'COMPLETED':
        return 4;
      default:
        return 1;
    }
  };

  const currentWeight = getStatusWeight(currentStatus);

  const timelineEvents: TimelineEvent[] = [
    {
      id: '1',
      title: 'Booking Confirmed',
      description: 'Your request was verified & secure payment hold initialized.',
      time: '02:00 PM',
      status: currentWeight >= 1 ? 'completed' : 'pending',
      icon: '✅',
    },
    {
      id: '2',
      title: 'Worker Assigned',
      description: 'Technician Ayaan Sheikh accepted and reserved your service slot.',
      time: '02:15 PM',
      status: currentWeight > 1 ? 'completed' : currentWeight === 1 ? 'active' : 'pending',
      icon: '👷',
    },
    {
      id: '3',
      title: 'Started Travel',
      description: 'Technician departed from regional hub. En route on ride.',
      time: '02:27 PM',
      status: currentWeight > 2 ? 'completed' : currentWeight === 2 ? 'active' : 'pending',
      icon: '🛵',
    },
    {
      id: '4',
      title: 'Technician Arrived',
      description: 'Technician reached destination coordinates and met customer.',
      time: '02:39 PM',
      status: currentWeight > 3 ? 'completed' : currentWeight === 3 ? 'active' : 'pending',
      icon: '📍',
    },
    {
      id: '5',
      title: 'Job Completed',
      description: 'Service successfully finalized and verified via dual-proof confirmation.',
      time: 'Pending',
      status: currentWeight >= 4 ? 'completed' : 'pending',
      icon: '🏆',
    },
  ];

  return (
    <View style={styles.card}>
      <Text style={styles.title} accessibilityRole="header">Recent Activities</Text>
      <Text style={styles.subtitle}>Real-time status changes & professional dispatch log</Text>

      <View style={styles.timelineContainer}>
        {timelineEvents.map((event, index) => {
          const isLast = index === timelineEvents.length - 1;
          
          return (
            <View key={event.id} style={styles.timelineRow}>
              {/* Left Column: Icon Indicator & Connected Vertical Path Line */}
              <View style={styles.leftColumn}>
                <View 
                  style={[
                    styles.nodeCircle, 
                    event.status === 'completed' && styles.nodeCompleted,
                    event.status === 'active' && styles.nodeActive,
                    event.status === 'pending' && styles.nodePending,
                  ]}
                >
                  <Text style={styles.nodeIcon}>{event.icon}</Text>
                </View>
                {!isLast && (
                  <View 
                    style={[
                      styles.connectorLine,
                      event.status === 'completed' ? styles.lineCompleted : styles.linePending,
                    ]} 
                  />
                )}
              </View>

              {/* Right Column: Dynamic Content Box */}
              <View style={styles.rightColumn}>
                <View style={styles.textHeaderRow}>
                  <Text 
                    style={[
                      styles.eventTitle,
                      event.status === 'active' && styles.titleActive,
                      event.status === 'pending' && styles.titlePending,
                    ]}
                  >
                    {event.title}
                  </Text>
                  <Text style={styles.eventTime}>{event.time}</Text>
                </View>
                <Text style={styles.eventDesc}>{event.description}</Text>
              </View>
            </View>
          );
        })}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 18,
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.04,
    shadowRadius: 12,
    elevation: 3,
    marginBottom: 16,
  },
  title: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
    letterSpacing: -0.3,
  },
  subtitle: {
    fontSize: 11.5,
    color: '#64748B',
    fontWeight: '550',
    marginTop: 2,
    marginBottom: 20,
  },
  timelineContainer: {
    paddingLeft: 4,
  },
  timelineRow: {
    flexDirection: 'row',
    minHeight: 68,
  },
  leftColumn: {
    alignItems: 'center',
    marginRight: 14,
  },
  nodeCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    backgroundColor: '#FFFFFF',
    zIndex: 10,
  },
  nodeCompleted: {
    borderColor: '#10B981',
    backgroundColor: '#ECFDF5',
  },
  nodeActive: {
    borderColor: '#4F46E5',
    backgroundColor: '#EEF2FF',
  },
  nodePending: {
    borderColor: '#CBD5E1',
    backgroundColor: '#F8FAFC',
  },
  nodeIcon: {
    fontSize: 14,
  },
  connectorLine: {
    width: 2.5,
    flex: 1,
    marginVertical: 4,
  },
  lineCompleted: {
    backgroundColor: '#10B981',
  },
  linePending: {
    backgroundColor: '#E2E8F0',
  },
  rightColumn: {
    flex: 1,
    paddingTop: 2,
    paddingBottom: 16,
  },
  textHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  eventTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#1E293B',
  },
  titleActive: {
    color: '#4F46E5',
  },
  titlePending: {
    color: '#64748B',
  },
  eventTime: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#64748B',
    backgroundColor: '#F1F5F9',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
    overflow: 'hidden',
  },
  eventDesc: {
    fontSize: 11.5,
    color: '#475569',
    lineHeight: 16,
    marginTop: 4,
    fontWeight: '500',
  },
});
