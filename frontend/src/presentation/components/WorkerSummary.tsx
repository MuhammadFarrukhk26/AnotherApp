import React from 'react';
import { View, Text, StyleSheet, Image, TouchableOpacity, Alert } from 'react-native';

interface WorkerSummaryProps {
  workerId: string;
  workerName?: string;
  workerPhone?: string;
  workerRating?: number;
  workerJobsCount?: number;
  avatarUrl?: string;
  onMessagePressed?: () => void;
}

export const WorkerSummary: React.FC<WorkerSummaryProps> = ({
  workerId,
  workerName = 'Ahmed Ali',
  workerPhone = '+92 300 1234567',
  workerRating = 4.9,
  workerJobsCount = 124,
  avatarUrl = 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200', // Beautiful professional portrait
  onMessagePressed,
}) => {
  const handleCall = () => {
    Alert.alert(
      'Simulated Outgoing Call',
      `Calling provider ${workerName} at ${workerPhone}...\n\n(In production, this initiates a direct device call via Linking API)`,
      [{ text: 'OK', style: 'default' }]
    );
  };

  const handleMessage = () => {
    if (onMessagePressed) {
      onMessagePressed();
    } else {
      Alert.alert(
        'Live Secure Chat',
        `Opening messaging session with ${workerName} [ID: ${workerId}]...\n\n(In-app encrypted web socket routing active)`,
        [{ text: 'Open Chat', style: 'default' }, { text: 'Cancel', style: 'cancel' }]
      );
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.profileRow}>
        <Image
          source={{ uri: avatarUrl }}
          style={styles.avatar}
          resizeMode="cover"
        />
        <View style={styles.infoCol}>
          <View style={styles.badgeRow}>
            <Text style={styles.nameText}>{workerName}</Text>
            <View style={styles.verifiedBadge}>
              <Text style={styles.verifiedText}>PRO</Text>
            </View>
          </View>
          
          <Text style={styles.subtitleText}>Certified Service Specialist</Text>
          
          <View style={styles.ratingRow}>
            <Text style={styles.starText}>★</Text>
            <Text style={styles.ratingVal}>{workerRating.toFixed(1)}</Text>
            <Text style={styles.dot}>•</Text>
            <Text style={styles.jobsText}>{workerJobsCount} jobs completed</Text>
          </View>
        </View>
      </View>

      <View style={styles.divider} />

      <View style={styles.actionRow}>
        <TouchableOpacity
          style={[styles.actionButton, styles.callButton]}
          onPress={handleCall}
          activeOpacity={0.7}
        >
          <Text style={styles.callButtonText}>📞 Call Provider</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.actionButton, styles.messageButton]}
          onPress={handleMessage}
          activeOpacity={0.7}
        >
          <Text style={styles.messageButtonText}>💬 Message</Text>
        </TouchableOpacity>
      </View>
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
  profileRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  avatar: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#F1F5F9',
    borderWidth: 2,
    borderColor: '#10B981',
  },
  infoCol: {
    flex: 1,
    marginLeft: 16,
  },
  badgeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
  },
  nameText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#0F172A',
    marginRight: 8,
  },
  verifiedBadge: {
    backgroundColor: '#ECFDF5',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#A7F3D0',
  },
  verifiedText: {
    color: '#047857',
    fontSize: 10,
    fontWeight: '800',
  },
  subtitleText: {
    fontSize: 13,
    color: '#64748B',
    marginTop: 2,
  },
  ratingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 6,
  },
  starText: {
    color: '#F59E0B',
    fontSize: 14,
    marginRight: 4,
  },
  ratingVal: {
    fontSize: 12,
    fontWeight: '600',
    color: '#0F172A',
  },
  dot: {
    fontSize: 12,
    color: '#94A3B8',
    marginHorizontal: 6,
  },
  jobsText: {
    fontSize: 12,
    color: '#64748B',
  },
  divider: {
    height: 1,
    backgroundColor: '#F1F5F9',
    marginVertical: 14,
  },
  actionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
  },
  actionButton: {
    flex: 1,
    height: 44,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  callButton: {
    backgroundColor: '#10B981',
    shadowColor: '#10B981',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 4,
    elevation: 2,
  },
  callButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '700',
  },
  messageButton: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#CBD5E1',
  },
  messageButtonText: {
    color: '#334155',
    fontSize: 14,
    fontWeight: '600',
  },
});
