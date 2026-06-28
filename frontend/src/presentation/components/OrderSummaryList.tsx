import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

interface OrderSummaryListProps {
  bookingId: string;
  serviceType: string;
  scheduledTime: string;
  address: string;
  price: number;
}

export const OrderSummaryList: React.FC<OrderSummaryListProps> = ({
  bookingId,
  serviceType,
  scheduledTime,
  address,
  price,
}) => {
  const formattedDate = new Date(scheduledTime).toLocaleString(undefined, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <View style={styles.container}>
      {/* Service Milestones Tracker */}
      <Text style={styles.sectionTitle}>Order Progress Milestones</Text>
      <View style={styles.milestoneCard}>
        <View style={styles.milestoneRow}>
          <View style={[styles.milestoneDot, styles.dotCompleted]} />
          <View style={styles.milestoneContent}>
            <Text style={styles.milestoneTitle}>Order Placed & Confirmed</Text>
            <Text style={styles.milestoneDesc}>Your requested slot has been verified on the schedule.</Text>
          </View>
        </View>

        <View style={styles.lineConnectorCompleted} />

        <View style={styles.milestoneRow}>
          <View style={[styles.milestoneDot, styles.dotCompleted]} />
          <View style={styles.milestoneContent}>
            <Text style={styles.milestoneTitle}>Professional Provider Assigned</Text>
            <Text style={styles.milestoneDesc}>Ahmed is assigned to handle your {serviceType.toLowerCase()} needs.</Text>
          </View>
        </View>

        <View style={styles.lineConnectorCompleted} />

        <View style={styles.milestoneRow}>
          <View style={[styles.milestoneDot, styles.dotActive]} />
          <View style={styles.milestoneContent}>
            <Text style={[styles.milestoneTitle, styles.titleActive]}>Provider En Route</Text>
            <Text style={styles.milestoneDesc}>Live GPS coordination of provider's motorcycle has started.</Text>
          </View>
        </View>

        <View style={styles.lineConnectorUpcoming} />

        <View style={styles.milestoneRow}>
          <View style={[styles.milestoneDot, styles.dotUpcoming]} />
          <View style={styles.milestoneContent}>
            <Text style={[styles.milestoneTitle, styles.titleUpcoming]}>Service Commenced</Text>
            <Text style={styles.milestoneDesc}>In-person service checks and work will begin upon OTP validation.</Text>
          </View>
        </View>
      </View>

      {/* Service Scope Checklist */}
      <Text style={styles.sectionTitle}>Service Details & Included Scope</Text>
      <View style={styles.detailsCard}>
        <View style={styles.scopeItem}>
          <Text style={styles.scopeBullet}>✓</Text>
          <Text style={styles.scopeText}>Fully vetted & background-checked service professional.</Text>
        </View>
        <View style={styles.scopeItem}>
          <Text style={styles.scopeBullet}>✓</Text>
          <Text style={styles.scopeText}>All essential industrial equipment & supplies included in fee.</Text>
        </View>
        <View style={styles.scopeItem}>
          <Text style={styles.scopeBullet}>✓</Text>
          <Text style={styles.scopeText}>Post-service visual walkthrough and client confirmation signoff.</Text>
        </View>
        <View style={styles.scopeItem}>
          <Text style={styles.scopeBullet}>✓</Text>
          <Text style={styles.scopeText}>100% Satisfaction Guarantee: Free re-clean if unsatisfied within 24 hours.</Text>
        </View>
      </View>

      {/* Special Instructions & Safety Policy */}
      <Text style={styles.sectionTitle}>Special Instructions</Text>
      <View style={styles.notesCard}>
        <View style={styles.notesRow}>
          <Text style={styles.notesIcon}>📝</Text>
          <View style={styles.notesTextContainer}>
            <Text style={styles.notesSubtitle}>Customer Note</Text>
            <Text style={styles.notesBody}>
              "Please knock on the side door if the main bell isn't answered. Keep an eye out for the friendly pet cat near the porch!"
            </Text>
          </View>
        </View>
        <View style={styles.notesDivider} />
        <View style={styles.notesRow}>
          <Text style={styles.notesIcon}>🛡️</Text>
          <View style={styles.notesTextContainer}>
            <Text style={styles.notesSubtitle}>No-Contact Safety protocol</Text>
            <Text style={styles.notesBody}>
              All providers wear sanitized protective gear. Digital invoices will be emailed instantly.
            </Text>
          </View>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginTop: 16,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    marginBottom: 10,
    marginTop: 6,
  },
  milestoneCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
  },
  milestoneRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  milestoneDot: {
    width: 14,
    height: 14,
    borderRadius: 7,
    marginTop: 3,
    borderWidth: 2,
  },
  dotCompleted: {
    backgroundColor: '#10B981',
    borderColor: '#A7F3D0',
  },
  dotActive: {
    backgroundColor: '#3B82F6',
    borderColor: '#BFDBFE',
  },
  dotUpcoming: {
    backgroundColor: '#FFFFFF',
    borderColor: '#CBD5E1',
  },
  milestoneContent: {
    flex: 1,
    marginLeft: 14,
  },
  milestoneTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#334155',
  },
  titleActive: {
    color: '#1D4ED8',
  },
  titleUpcoming: {
    color: '#64748B',
  },
  milestoneDesc: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 2,
    lineHeight: 15,
  },
  lineConnectorCompleted: {
    width: 2,
    height: 20,
    backgroundColor: '#10B981',
    marginLeft: 6,
    marginVertical: 4,
  },
  lineConnectorUpcoming: {
    width: 2,
    height: 20,
    backgroundColor: '#E2E8F0',
    marginLeft: 6,
    marginVertical: 4,
  },
  detailsCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
  },
  scopeItem: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginVertical: 4,
  },
  scopeBullet: {
    color: '#10B981',
    fontWeight: '800',
    fontSize: 14,
    marginRight: 8,
  },
  scopeText: {
    flex: 1,
    fontSize: 12,
    color: '#475569',
    lineHeight: 17,
  },
  notesCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
  },
  notesRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  notesIcon: {
    fontSize: 20,
    marginRight: 12,
    marginTop: 2,
  },
  notesTextContainer: {
    flex: 1,
  },
  notesSubtitle: {
    fontSize: 11,
    fontWeight: '700',
    color: '#94A3B8',
    textTransform: 'uppercase',
  },
  notesBody: {
    fontSize: 12,
    color: '#475569',
    lineHeight: 17,
    marginTop: 2,
  },
  notesDivider: {
    height: 1,
    backgroundColor: '#F1F5F9',
    marginVertical: 12,
  },
});
