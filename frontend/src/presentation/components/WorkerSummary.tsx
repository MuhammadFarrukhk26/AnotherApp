import React, { useState } from 'react';
import { View, Text, StyleSheet, Image, TouchableOpacity, Alert, Modal, Linking } from 'react-native';

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
  workerName = 'Ayaan Sheikh',
  workerPhone = '+92 321 4567890',
  workerRating = 4.9,
  workerJobsCount = 186,
  avatarUrl = 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200',
  onMessagePressed,
}) => {
  const [showContactModal, setShowContactModal] = useState(false);

  const handleCall = () => {
    setShowContactModal(false);
    Alert.alert(
      'Simulated Outgoing Call',
      `Calling provider ${workerName} at ${workerPhone}...\n\n(In production, this initiates a direct device call via Linking API)`,
      [{ text: 'OK', style: 'default' }]
    );
  };

  const handleMessage = () => {
    setShowContactModal(false);
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
      {/* Active status bar */}
      <View style={styles.statusBar}>
        <View style={styles.statusDot} />
        <Text style={styles.statusText}>ASSIGNED SPECIALIST • ONLINE & EN ROUTE</Text>
      </View>

      <View style={styles.profileRow}>
        <View style={styles.avatarContainer}>
          <Image
            source={{ uri: avatarUrl }}
            style={styles.avatar}
            resizeMode="cover"
          />
          <View style={styles.ratingBadge}>
            <Text style={styles.starText}>★</Text>
            <Text style={styles.ratingVal}>{workerRating.toFixed(1)}</Text>
          </View>
        </View>

        <View style={styles.infoCol}>
          <View style={styles.badgeRow}>
            <Text style={styles.nameText}>{workerName}</Text>
            <View style={styles.verifiedBadge}>
              <Text style={styles.verifiedText}>PRO VERIFIED</Text>
            </View>
          </View>
          
          <Text style={styles.subtitleText}>Elite Service Partner</Text>
          
          <View style={styles.statsRow}>
            <View style={styles.statItem}>
              <Text style={styles.statNumber}>{workerJobsCount}</Text>
              <Text style={styles.statLabel}>Jobs Done</Text>
            </View>
            <View style={styles.statDivider} />
            <View style={styles.statItem}>
              <Text style={styles.statNumber}>100%</Text>
              <Text style={styles.statLabel}>On-time Rate</Text>
            </View>
          </View>
        </View>
      </View>

      <View style={styles.divider} />

      {/* Direct Contact Button */}
      <TouchableOpacity
        style={styles.contactButton}
        onPress={() => setShowContactModal(true)}
        activeOpacity={0.8}
        testID={`contact_worker_button_${workerId}`}
      >
        <Text style={styles.contactButtonText}>📞 Contact {workerName.split(' ')[0]}</Text>
      </TouchableOpacity>

      {/* Quick Badges */}
      <View style={styles.perksContainer}>
        <Text style={styles.perkItem}>🛡️ Background Checked</Text>
        <Text style={styles.perkItem}>⭐ Top Rated</Text>
        <Text style={styles.perkItem}>⚡ Instant Response</Text>
      </View>

      {/* Contact Options Modal */}
      <Modal
        visible={showContactModal}
        transparent={true}
        animationType="fade"
        onRequestClose={() => setShowContactModal(false)}
      >
        <TouchableOpacity
          style={styles.modalOverlay}
          activeOpacity={1}
          onPress={() => setShowContactModal(false)}
        >
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Contact {workerName}</Text>
            <Text style={styles.modalSubtitle}>How would you like to reach your service partner?</Text>

            <TouchableOpacity style={styles.modalActionRow} onPress={handleMessage} activeOpacity={0.7}>
              <View style={[styles.modalActionIconContainer, { backgroundColor: '#EEF2FF' }]}>
                <Text style={styles.modalActionIcon}>💬</Text>
              </View>
              <View style={styles.modalActionTextContainer}>
                <Text style={styles.modalActionTitle}>In-App Secure Chat</Text>
                <Text style={styles.modalActionDesc}>Chat live with end-to-end status tracking</Text>
              </View>
            </TouchableOpacity>

            <TouchableOpacity style={styles.modalActionRow} onPress={handleCall} activeOpacity={0.7}>
              <View style={[styles.modalActionIconContainer, { backgroundColor: '#ECFDF5' }]}>
                <Text style={styles.modalActionIcon}>📞</Text>
              </View>
              <View style={styles.modalActionTextContainer}>
                <Text style={styles.modalActionTitle}>Direct Voice Call</Text>
                <Text style={styles.modalActionDesc}>Call via mobile carrier line</Text>
              </View>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.modalCloseButton}
              onPress={() => setShowContactModal(false)}
              activeOpacity={0.7}
            >
              <Text style={styles.modalCloseButtonText}>Cancel</Text>
            </TouchableOpacity>
          </View>
        </TouchableOpacity>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 18,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.06,
    shadowRadius: 12,
    elevation: 3,
    marginTop: 16,
    overflow: 'hidden',
  },
  statusBar: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
    alignSelf: 'flex-start',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 20,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  statusDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#10B981',
    marginRight: 6,
  },
  statusText: {
    fontSize: 9,
    fontWeight: '800',
    color: '#475569',
    letterSpacing: 0.5,
  },
  profileRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  avatarContainer: {
    position: 'relative',
  },
  avatar: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: '#F1F5F9',
    borderWidth: 3,
    borderColor: '#E2E8F0',
  },
  ratingBadge: {
    position: 'absolute',
    bottom: -4,
    right: -4,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#0F172A',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 10,
    borderWidth: 1.5,
    borderColor: '#FFFFFF',
  },
  starText: {
    color: '#F59E0B',
    fontSize: 10,
    marginRight: 2,
    fontWeight: 'bold',
  },
  ratingVal: {
    fontSize: 10,
    fontWeight: '800',
    color: '#FFFFFF',
  },
  infoCol: {
    flex: 1,
    marginLeft: 18,
  },
  badgeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
  },
  nameText: {
    fontSize: 18,
    fontWeight: '900',
    color: '#0F172A',
    marginRight: 8,
    letterSpacing: -0.3,
  },
  verifiedBadge: {
    backgroundColor: '#F0FDF4',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#BBF7D0',
  },
  verifiedText: {
    color: '#15803D',
    fontSize: 8,
    fontWeight: '900',
    letterSpacing: 0.3,
  },
  subtitleText: {
    fontSize: 13,
    color: '#64748B',
    marginTop: 2,
    fontWeight: '500',
  },
  statsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 10,
  },
  statItem: {
    alignItems: 'flex-start',
  },
  statNumber: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
  },
  statLabel: {
    fontSize: 10,
    color: '#64748B',
    fontWeight: '500',
    marginTop: 1,
  },
  statDivider: {
    width: 1,
    height: 20,
    backgroundColor: '#E2E8F0',
    marginHorizontal: 16,
  },
  divider: {
    height: 1,
    backgroundColor: '#F1F5F9',
    marginVertical: 16,
  },
  contactButton: {
    backgroundColor: '#10B981',
    borderRadius: 12,
    height: 48,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#10B981',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 4,
  },
  contactButtonText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '800',
  },
  perksContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 14,
    paddingHorizontal: 4,
  },
  perkItem: {
    fontSize: 10,
    fontWeight: '600',
    color: '#64748B',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.4)',
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  modalContent: {
    width: '100%',
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingHorizontal: 24,
    paddingTop: 28,
    paddingBottom: 36,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: -10 },
    shadowOpacity: 0.1,
    shadowRadius: 16,
    elevation: 10,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: '900',
    color: '#0F172A',
    marginBottom: 6,
  },
  modalSubtitle: {
    fontSize: 13,
    color: '#64748B',
    lineHeight: 18,
    marginBottom: 24,
  },
  modalActionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  modalActionIconContainer: {
    width: 44,
    height: 44,
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 16,
  },
  modalActionIcon: {
    fontSize: 20,
  },
  modalActionTextContainer: {
    flex: 1,
  },
  modalActionTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  modalActionDesc: {
    fontSize: 12,
    color: '#64748B',
    marginTop: 2,
  },
  modalCloseButton: {
    marginTop: 24,
    backgroundColor: '#F1F5F9',
    borderRadius: 12,
    height: 48,
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalCloseButtonText: {
    color: '#475569',
    fontSize: 14,
    fontWeight: '700',
  },
});
