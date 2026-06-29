import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, LayoutAnimation, Platform, UIManager } from 'react-native';
import { Card } from './Card';

// Enable LayoutAnimation for Android
if (Platform.OS === 'android' && UIManager.setLayoutAnimationEnabledExperimental) {
  UIManager.setLayoutAnimationEnabledExperimental(true);
}

interface FAQItem {
  id: string;
  question: string;
  answer: string;
}

export const BookingFAQSection: React.FC = () => {
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const faqs: FAQItem[] = [
    {
      id: 'safety',
      question: 'What is Hazir’s customer safety policy?',
      answer: 'Your safety is our priority. Every Hazir technician undergoes background checks, biometric registration, and strict onboarding tests. Live sharing is available so family and friends can track your booking status and location in real-time.',
    },
    {
      id: 'cancel_refund',
      question: 'How do cancellation refunds work?',
      answer: 'Cancellations are completely free before a worker is assigned or accepts your job. If you cancel after a worker is en route, any online pre-payment is immediately refunded to your Hazir Wallet (within 10 minutes) or your bank card (within 3-5 business days).',
    },
    {
      id: 'extension',
      question: 'Why would a service duration be extended?',
      answer: 'For complex repairs, the technician might need more time for accurate diagnostics or assembly. Any extension requested must be explicitly approved by you on this screen before extra charges are added to your invoice.',
    },
    {
      id: 'pricing',
      question: 'Are there any hidden platform fees?',
      answer: 'No! All charges, including the base fee, technician tip, and any approved time extensions are clearly transparent. You only pay what you see on your final digital receipt.',
    }
  ];

  const toggleExpand = (id: string) => {
    LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);
    if (expandedId === id) {
      setExpandedId(null);
    } else {
      setExpandedId(id);
    }
  };

  return (
    <Card style={styles.container}>
      <Text style={styles.sectionTitle} accessibilityRole="header">🙋 Common Service FAQs</Text>
      <Text style={styles.sectionSubtitle}>Quick answers to safety policies and billing queries.</Text>

      <View style={styles.faqList}>
        {faqs.map((faq) => {
          const isExpanded = expandedId === faq.id;
          return (
            <View key={faq.id} style={styles.faqItemContainer}>
              <TouchableOpacity
                style={styles.questionRow}
                onPress={() => toggleExpand(faq.id)}
                activeOpacity={0.7}
                accessibilityRole="button"
                accessibilityLabel={faq.question}
                accessibilityState={{ expanded: isExpanded }}
              >
                <Text style={[styles.questionText, isExpanded && styles.questionTextExpanded]}>
                  {faq.question}
                </Text>
                <Text style={[styles.arrowIcon, isExpanded && styles.arrowIconExpanded]}>
                  {isExpanded ? '▲' : '▼'}
                </Text>
              </TouchableOpacity>

              {isExpanded && (
                <View style={styles.answerContainer}>
                  <Text style={styles.answerText}>{faq.answer}</Text>
                </View>
              )}
            </View>
          );
        })}
      </View>
    </Card>
  );
};

const styles = StyleSheet.create({
  container: {
    marginVertical: 12,
    backgroundColor: '#FFFFFF',
    borderColor: '#E2E8F0',
    borderWidth: 1,
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  sectionSubtitle: {
    fontSize: 12,
    color: '#64748B',
    marginBottom: 16,
  },
  faqList: {
    gap: 8,
  },
  faqItemContainer: {
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
    paddingBottom: 8,
  },
  questionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
  },
  questionText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#334155',
    flex: 1,
    paddingRight: 12,
    lineHeight: 18,
  },
  questionTextExpanded: {
    color: '#D97706', // Primary Accent
  },
  arrowIcon: {
    fontSize: 11,
    color: '#94A3B8',
    fontWeight: '700',
  },
  arrowIconExpanded: {
    color: '#D97706',
  },
  answerContainer: {
    marginTop: 4,
    marginBottom: 8,
    paddingHorizontal: 4,
  },
  answerText: {
    fontSize: 12,
    color: '#475569',
    lineHeight: 17,
  },
});
