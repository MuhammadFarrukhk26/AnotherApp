import React, { useState, useEffect } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  TouchableOpacity, 
  Modal, 
  TextInput, 
  ScrollView, 
  Alert 
} from 'react-native';
import { useTheme } from '../state/ThemeContext';

interface BillingCardProps {
  totalPrice: number;
  paymentMethod?: string;
  extraPrice?: number;
}

const PROMO_CODES: { [key: string]: { discount: number; isPercentage: boolean; description: string } } = {
  'WELCOME300': { discount: 300, isPercentage: false, description: 'PKR 300 flat discount for new users' },
  'HAZIR20': { discount: 20, isPercentage: true, description: '20% off base service fee' },
  'SUPERDEAL': { discount: 500, isPercentage: false, description: 'PKR 500 premium discount' },
};

export const BillingCard: React.FC<BillingCardProps> = ({
  totalPrice,
  paymentMethod = 'Cash on Delivery',
  extraPrice = 0,
}) => {
  const { colors, isDark } = useTheme();
  const [showBreakdownModal, setShowBreakdownModal] = useState<boolean>(false);
  const [promoCodeInput, setPromoCodeInput] = useState<string>('');
  const [appliedPromo, setAppliedPromo] = useState<string | null>('WELCOME300'); // pre-applied for interactive richness
  const [errorMsg, setErrorMsg] = useState<string>('');
  const [successMsg, setSuccessMsg] = useState<string>('');
  const [selectedTip, setSelectedTip] = useState<number>(0); // preset percentage tips: 0%, 10%, 15%, 20%

  // Math variables
  const baseServiceFeeRaw = totalPrice - extraPrice;
  const baseServiceFee = Math.max(0, baseServiceFeeRaw * 0.85);
  const workExtensionFee = extraPrice * 0.85;

  // Active promo calculations
  const activePromo = appliedPromo ? PROMO_CODES[appliedPromo.toUpperCase()] : null;
  let discountAmount = 0;
  if (activePromo) {
    if (activePromo.isPercentage) {
      discountAmount = baseServiceFee * (activePromo.discount / 100);
    } else {
      discountAmount = activePromo.discount;
    }
  }
  // Cap the discount at base service fee
  discountAmount = Math.min(discountAmount, baseServiceFee);

  const subtotal = Math.max(0, baseServiceFee + workExtensionFee - discountAmount);
  
  // Platform fee (10% of grand total, which is 10/85 of subtotal)
  const platformFee = subtotal * (10 / 85);
  // Tax (5% of grand total, which is 5/85 of subtotal)
  const tax = subtotal * (5 / 85);
  
  // Tip calculation based on subtotal
  const tipAmount = subtotal * (selectedTip / 100);
  const computedGrandTotal = subtotal + platformFee + tax + tipAmount;

  const formatCurrency = (amount: number) => {
    return `PKR ${amount.toLocaleString(undefined, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  };

  const handleApplyPromo = (code: string) => {
    const formattedCode = code.trim().toUpperCase();
    if (!formattedCode) {
      setErrorMsg('Please enter a promo code.');
      setSuccessMsg('');
      return;
    }

    if (PROMO_CODES[formattedCode]) {
      setAppliedPromo(formattedCode);
      setSuccessMsg(`Promo "${formattedCode}" applied successfully!`);
      setErrorMsg('');
      setPromoCodeInput('');
    } else {
      setErrorMsg('Invalid code. Try WELCOME300, HAZIR20, or SUPERDEAL!');
      setSuccessMsg('');
    }
  };

  const handleRemovePromo = () => {
    setAppliedPromo(null);
    setSuccessMsg('');
    setErrorMsg('');
    setPromoCodeInput('');
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <Text style={[styles.title, { color: colors.text }]} accessibilityRole="header">Billing & Invoice Summary</Text>
      
      {/* Base service fee row */}
      <View style={styles.row} accessible={true} accessibilityLabel={`Base Service Fee: ${formatCurrency(baseServiceFee)}`}>
        <Text style={[styles.label, { color: colors.textMuted }]}>Base Service Fee</Text>
        <Text style={[styles.value, { color: colors.textSecondary }]}>{formatCurrency(baseServiceFee)}</Text>
      </View>

      {/* Extension row if any extra price is applied */}
      {extraPrice > 0 && (
        <View style={styles.row} accessible={true} accessibilityLabel={`Work Extension: ${formatCurrency(workExtensionFee)}`}>
          <Text style={[styles.label, { color: colors.textMuted }]}>Work Extension Fee</Text>
          <Text style={[styles.value, { color: colors.textSecondary }]}>{formatCurrency(workExtensionFee)}</Text>
        </View>
      )}

      {/* Applied Promo Code row if any */}
      {appliedPromo && (
        <View style={styles.row} accessible={true} accessibilityLabel={`Promo Discount for code ${appliedPromo}: minus ${formatCurrency(discountAmount)}`}>
          <Text style={styles.promoLabel}>🎁 Promo ({appliedPromo})</Text>
          <Text style={styles.promoValue}>-{formatCurrency(discountAmount)}</Text>
        </View>
      )}

      <View style={styles.row} accessible={true} accessibilityLabel={`Platform Commission: ${formatCurrency(platformFee)}`}>
        <Text style={[styles.label, { color: colors.textMuted }]}>Platform Commission (10%)</Text>
        <Text style={[styles.value, { color: colors.textSecondary }]}>{formatCurrency(platformFee)}</Text>
      </View>

      <View style={styles.row} accessible={true} accessibilityLabel={`Government services tax: ${formatCurrency(tax)}`}>
        <Text style={[styles.label, { color: colors.textMuted }]}>Govt. Services Tax (5%)</Text>
        <Text style={[styles.value, { color: colors.textSecondary }]}>{formatCurrency(tax)}</Text>
      </View>

      {/* Gratuity Selection Component */}
      <View style={[styles.gratuityContainer, { backgroundColor: isDark ? colors.background : '#F8FAFC', borderColor: colors.border }]}>
        <Text style={[styles.gratuityTitle, { color: colors.textSecondary }]}>Add a Tip for the Technician:</Text>
        <View style={styles.gratuityRow}>
          {[0, 10, 15, 20].map((pct) => {
            const isSelected = selectedTip === pct;
            return (
              <TouchableOpacity
                key={pct}
                style={[
                  styles.gratuityButton,
                  { backgroundColor: colors.card, borderColor: colors.border },
                  isSelected && styles.gratuityButtonActive
                ]}
                onPress={() => setSelectedTip(pct)}
                accessibilityLabel={pct === 0 ? "No tip" : `${pct}% tip`}
                accessibilityRole="button"
              >
                <Text style={[
                  styles.gratuityButtonText,
                  { color: colors.textSecondary },
                  isSelected && styles.gratuityButtonTextActive
                ]}>
                  {pct === 0 ? "No Tip" : `${pct}%`}
                </Text>
                {pct > 0 && (
                  <Text style={[
                    styles.gratuityValText,
                    isSelected && styles.gratuityValTextActive
                  ]}>
                    +{formatCurrency(subtotal * (pct / 100)).replace("PKR ", "")}
                  </Text>
                )}
              </TouchableOpacity>
            );
          })}
        </View>
      </View>

      {selectedTip > 0 && (
        <View style={styles.row} accessible={true} accessibilityLabel={`Technician Tip: ${formatCurrency(tipAmount)}`}>
          <Text style={styles.tipLabel}>✨ Technician Tip ({selectedTip}%)</Text>
          <Text style={styles.tipValue}>{formatCurrency(tipAmount)}</Text>
        </View>
      )}

      <View style={[styles.divider, { backgroundColor: colors.border }]} />

      {/* Interactive Grand Total Section */}
      <TouchableOpacity 
        style={[styles.row, styles.totalRow, { backgroundColor: isDark ? colors.background : '#F8FAFC', borderColor: colors.border }]}
        onPress={() => setShowBreakdownModal(true)}
        activeOpacity={0.7}
        accessible={true}
        accessibilityLabel={`Grand Total: ${formatCurrency(computedGrandTotal)}. Tap to view high-contrast price breakdown and promo options.`}
        accessibilityRole="button"
        accessibilityHint="Opens a detailed invoice breakdown screen."
      >
        <View style={styles.totalLabelContainer}>
          <Text style={[styles.totalLabel, { color: colors.text }]}>Grand Total</Text>
          <View style={styles.infoPill}>
            <Text style={styles.infoPillText}>Breakdown ℹ️</Text>
          </View>
        </View>
        <Text style={styles.totalValue}>{formatCurrency(computedGrandTotal)}</Text>
      </TouchableOpacity>

      {/* Payment Method Details */}
      <View style={[styles.paymentMethodContainer, { backgroundColor: isDark ? colors.background : '#F8FAFC', borderColor: colors.border }]}>
        <View style={styles.paymentRow}>
          <Text style={styles.paymentIcon}>💳</Text>
          <View style={styles.paymentDetails}>
            <Text style={styles.paymentTitle}>Payment Method</Text>
            <Text style={[styles.paymentSub, { color: colors.textSecondary }]}>{paymentMethod}</Text>
          </View>
          <View style={styles.securedBadge}>
            <Text style={styles.securedText}>SECURE</Text>
          </View>
        </View>
      </View>

      {/* Interactive Price Breakdown Modal */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={showBreakdownModal}
        onRequestClose={() => setShowBreakdownModal(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={[styles.modalContent, { backgroundColor: colors.card }]} accessible={true} accessibilityLabel="Detailed Price Breakdown and Invoice Summary">
            
            {/* Modal Header */}
            <View style={[styles.modalHeader, { borderBottomColor: colors.border }]}>
              <View style={styles.dragHandle} />
              <View style={styles.modalHeaderRow}>
                <Text style={[styles.modalTitle, { color: colors.text }]} accessibilityRole="header">Detailed Invoice</Text>
                <TouchableOpacity 
                  onPress={() => setShowBreakdownModal(false)}
                  style={[styles.closeButton, { backgroundColor: colors.border }]}
                  accessibilityLabel="Close detailed invoice modal"
                  accessibilityRole="button"
                >
                  <Text style={[styles.closeButtonText, { color: colors.textSecondary }]}>✕</Text>
                </TouchableOpacity>
              </View>
            </View>

            <ScrollView contentContainerStyle={styles.modalScrollContent}>
              {/* Receipt Visual Container */}
              <View style={[styles.receiptContainer, { backgroundColor: isDark ? colors.background : '#F8FAFC', borderColor: colors.border }]}>
                <Text style={styles.receiptMerchant}>HAZIR SERVICES LTD</Text>
                <Text style={[styles.receiptDate, { color: colors.textSecondary }]}>Booking Invoice Breakdown</Text>
                <View style={[styles.receiptDashedDivider, { borderColor: colors.border }]} />

                {/* Line Item: Base Service */}
                <View style={styles.receiptRow}>
                  <Text style={[styles.receiptLabel, { color: colors.textSecondary }]}>Base Service Fee</Text>
                  <Text style={[styles.receiptValue, { color: colors.text }]}>{formatCurrency(baseServiceFee)}</Text>
                </View>

                {/* Line Item: Extra extension if any */}
                {extraPrice > 0 && (
                  <View style={styles.receiptRow}>
                    <Text style={[styles.receiptLabel, { color: colors.textSecondary }]}>Work Extension Fee</Text>
                    <Text style={[styles.receiptValue, { color: colors.text }]}>{formatCurrency(workExtensionFee)}</Text>
                  </View>
                )}

                {/* Line Item: Promo Code Discount */}
                {appliedPromo && (
                  <View style={styles.receiptRow}>
                    <Text style={styles.receiptPromoLabel}>🎁 Applied Promo ({appliedPromo})</Text>
                    <Text style={styles.receiptPromoValue}>-{formatCurrency(discountAmount)}</Text>
                  </View>
                )}

                <View style={[styles.receiptDashedDivider, { borderColor: colors.border }]} />

                {/* Line Item: Subtotal */}
                <View style={styles.receiptRow}>
                  <Text style={[styles.receiptLabel, styles.semiboldText, { color: colors.text }]}>Net Subtotal</Text>
                  <Text style={[styles.receiptValue, styles.semiboldText, { color: colors.text }]}>{formatCurrency(subtotal)}</Text>
                </View>

                {/* Line Item: Platform Commission */}
                <View style={styles.receiptRow}>
                  <Text style={[styles.receiptLabel, { color: colors.textSecondary }]}>Platform Commission (10%)</Text>
                  <Text style={[styles.receiptValue, { color: colors.text }]}>{formatCurrency(platformFee)}</Text>
                </View>

                {/* Line Item: Govt Tax */}
                <View style={styles.receiptRow}>
                  <Text style={[styles.receiptLabel, { color: colors.textSecondary }]}>Govt. Services Tax (5%)</Text>
                  <Text style={[styles.receiptValue, { color: colors.text }]}>{formatCurrency(tax)}</Text>
                </View>

                {/* Line Item: Tip */}
                {selectedTip > 0 && (
                  <View style={styles.receiptRow}>
                    <Text style={styles.receiptTipLabel}>✨ Technician Tip ({selectedTip}%)</Text>
                    <Text style={styles.receiptTipValue}>{formatCurrency(tipAmount)}</Text>
                  </View>
                )}

                <View style={[styles.receiptDashedDivider, { borderColor: colors.border }]} />

                {/* Line Item: Grand Total */}
                <View style={styles.receiptRow}>
                  <Text style={[styles.receiptTotalLabel, { color: colors.text }]}>Total Due</Text>
                  <Text style={styles.receiptTotalValue}>{formatCurrency(computedGrandTotal)}</Text>
                </View>
              </View>

              {/* Promo Code Application Section */}
              <View style={styles.promoSection}>
                <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>Apply Voucher / Promo Code</Text>
                
                <View style={styles.promoInputRow}>
                  <TextInput
                    style={[styles.promoInput, { backgroundColor: colors.background, borderColor: colors.border, color: colors.text }]}
                    placeholder="Enter code (e.g. SUPERDEAL)"
                    placeholderTextColor={isDark ? '#64748B' : '#94A3B8'}
                    value={promoCodeInput}
                    onChangeText={(text) => {
                      setPromoCodeInput(text);
                      setErrorMsg('');
                    }}
                    autoCapitalize="characters"
                    accessibilityLabel="Promo Code input field"
                  />
                  <TouchableOpacity
                    style={styles.promoApplyButton}
                    onPress={() => handleApplyPromo(promoCodeInput)}
                    accessibilityLabel="Apply entered promo code"
                    accessibilityRole="button"
                  >
                    <Text style={styles.promoApplyText}>Apply</Text>
                  </TouchableOpacity>
                </View>

                {/* Message display */}
                {errorMsg ? <Text style={styles.errorText}>{errorMsg}</Text> : null}
                {successMsg ? <Text style={styles.successText}>{successMsg}</Text> : null}

                {/* Currently Applied Promo Tag */}
                {appliedPromo && (
                  <View style={styles.appliedPromoBadgeRow}>
                    <View style={styles.appliedPromoBadge}>
                      <Text style={styles.appliedPromoBadgeText}>🎉 Code "{appliedPromo}" Active</Text>
                    </View>
                    <TouchableOpacity 
                      style={styles.removePromoButton}
                      onPress={handleRemovePromo}
                      accessibilityLabel="Remove applied promo code"
                      accessibilityRole="button"
                    >
                      <Text style={styles.removePromoText}>Remove</Text>
                    </TouchableOpacity>
                  </View>
                )}

                {/* Suggested Promo Codes List */}
                <Text style={[styles.suggestedTitle, { color: colors.textMuted }]}>Suggested Deals (Tap to apply):</Text>
                <View style={styles.suggestedList}>
                  {Object.keys(PROMO_CODES).map((code) => {
                    const info = PROMO_CODES[code];
                    const isActive = appliedPromo === code;
                    return (
                      <TouchableOpacity
                        key={code}
                        style={[
                          styles.suggestedCard,
                          { backgroundColor: isDark ? colors.background : '#F8FAFC', borderColor: colors.border },
                          isActive && styles.suggestedCardActive
                        ]}
                        onPress={() => handleApplyPromo(code)}
                        accessibilityLabel={`Apply suggested code ${code}: ${info.description}`}
                        accessibilityRole="button"
                      >
                        <View style={styles.suggestedHeader}>
                          <Text style={[styles.suggestedCodeText, isActive && styles.suggestedCodeTextActive]}>{code}</Text>
                          {isActive && <Text style={styles.checkmarkIcon}>✓</Text>}
                        </View>
                        <Text style={[styles.suggestedDescText, { color: colors.textMuted }]}>{info.description}</Text>
                      </TouchableOpacity>
                    );
                  })}
                </View>
              </View>
            </ScrollView>

            {/* Bottom Modal Footer Action */}
            <TouchableOpacity
              style={styles.dismissButton}
              onPress={() => setShowBreakdownModal(false)}
              accessibilityLabel="Dismiss price details screen"
              accessibilityRole="button"
            >
              <Text style={styles.dismissButtonText}>Done & Return</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
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
  title: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 14,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 6,
  },
  label: {
    fontSize: 13,
    color: '#64748B',
  },
  value: {
    fontSize: 13,
    fontWeight: '500',
    color: '#334155',
  },
  promoLabel: {
    fontSize: 13,
    color: '#10B981',
    fontWeight: '600',
  },
  promoValue: {
    fontSize: 13,
    fontWeight: '700',
    color: '#10B981',
  },
  divider: {
    height: 1,
    backgroundColor: '#F1F5F9',
    marginVertical: 12,
  },
  totalRow: {
    marginVertical: 2,
    backgroundColor: '#F8FAFC',
    padding: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#EFF6FF',
  },
  totalLabelContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  totalLabel: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  infoPill: {
    backgroundColor: '#EFF6FF',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 6,
    borderWidth: 0.5,
    borderColor: '#BFDBFE',
  },
  infoPillText: {
    color: '#1E40AF',
    fontSize: 9,
    fontWeight: '700',
  },
  totalValue: {
    fontSize: 16,
    fontWeight: '800',
    color: '#10B981',
  },
  paymentMethodContainer: {
    marginTop: 16,
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#F1F5F9',
  },
  paymentRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  paymentIcon: {
    fontSize: 20,
    marginRight: 10,
  },
  paymentDetails: {
    flex: 1,
  },
  paymentTitle: {
    fontSize: 11,
    fontWeight: '600',
    color: '#94A3B8',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  paymentSub: {
    fontSize: 13,
    fontWeight: '600',
    color: '#334155',
    marginTop: 1,
  },
  securedBadge: {
    backgroundColor: '#ECFDF5',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#D1FAE5',
  },
  securedText: {
    color: '#059669',
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 0.5,
  },

  // Modal Styles
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.6)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    height: '80%',
    paddingBottom: 20,
  },
  modalHeader: {
    alignItems: 'center',
    paddingTop: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  dragHandle: {
    width: 40,
    height: 4,
    backgroundColor: '#CBD5E1',
    borderRadius: 2,
    marginBottom: 8,
  },
  modalHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    width: '100%',
    paddingHorizontal: 20,
    paddingBottom: 12,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
  },
  closeButton: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
  },
  closeButtonText: {
    color: '#64748B',
    fontSize: 12,
    fontWeight: '800',
  },
  modalScrollContent: {
    padding: 20,
  },

  // Receipt Styles
  receiptContainer: {
    backgroundColor: '#F8FAFC',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderStyle: 'dashed',
    marginBottom: 20,
  },
  receiptMerchant: {
    fontSize: 12,
    fontWeight: '800',
    color: '#64748B',
    textAlign: 'center',
    letterSpacing: 1.5,
    marginBottom: 2,
  },
  receiptDate: {
    fontSize: 13,
    fontWeight: '700',
    color: '#334155',
    textAlign: 'center',
    marginBottom: 12,
  },
  receiptDashedDivider: {
    height: 1,
    borderWidth: 1,
    borderColor: '#CBD5E1',
    borderStyle: 'dashed',
    marginVertical: 12,
  },
  receiptRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 4,
  },
  receiptLabel: {
    fontSize: 13,
    color: '#475569',
  },
  receiptValue: {
    fontSize: 13,
    color: '#0F172A',
    fontWeight: '500',
  },
  receiptPromoLabel: {
    fontSize: 13,
    color: '#10B981',
    fontWeight: '700',
  },
  receiptPromoValue: {
    fontSize: 13,
    color: '#10B981',
    fontWeight: '800',
  },
  semiboldText: {
    fontWeight: '700',
    color: '#0F172A',
  },
  receiptTotalLabel: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
  },
  receiptTotalValue: {
    fontSize: 18,
    fontWeight: '900',
    color: '#10B981',
  },

  // Promo Application styles
  promoSection: {
    marginTop: 10,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#334155',
    marginBottom: 10,
  },
  promoInputRow: {
    flexDirection: 'row',
    gap: 10,
  },
  promoInput: {
    flex: 1,
    backgroundColor: '#F8FAFC',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 10,
    fontSize: 14,
    color: '#0F172A',
    fontWeight: '600',
  },
  promoApplyButton: {
    backgroundColor: '#1E40AF',
    borderRadius: 12,
    paddingHorizontal: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  promoApplyText: {
    color: '#FFFFFF',
    fontWeight: '700',
    fontSize: 14,
  },
  errorText: {
    color: '#EF4444',
    fontSize: 12,
    fontWeight: '600',
    marginTop: 6,
  },
  successText: {
    color: '#10B981',
    fontSize: 12,
    fontWeight: '600',
    marginTop: 6,
  },
  appliedPromoBadgeRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#ECFDF5',
    borderWidth: 1,
    borderColor: '#A7F3D0',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginTop: 12,
  },
  appliedPromoBadge: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  appliedPromoBadgeText: {
    color: '#065F46',
    fontWeight: '700',
    fontSize: 13,
  },
  removePromoButton: {
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  removePromoText: {
    color: '#EF4444',
    fontWeight: '700',
    fontSize: 12,
  },

  // Suggested Promos Styles
  suggestedTitle: {
    fontSize: 12,
    fontWeight: '700',
    color: '#64748B',
    marginTop: 20,
    marginBottom: 8,
    letterSpacing: 0.5,
    textTransform: 'uppercase',
  },
  suggestedList: {
    gap: 10,
  },
  suggestedCard: {
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  suggestedCardActive: {
    backgroundColor: '#ECFDF5',
    borderColor: '#10B981',
  },
  suggestedHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  suggestedCodeText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#1E40AF',
  },
  suggestedCodeTextActive: {
    color: '#065F46',
  },
  checkmarkIcon: {
    fontSize: 14,
    color: '#10B981',
    fontWeight: '900',
  },
  suggestedDescText: {
    fontSize: 12,
    color: '#64748B',
    fontWeight: '500',
  },

  // Dismiss Bottom button
  dismissButton: {
    backgroundColor: '#0F172A',
    marginHorizontal: 20,
    borderRadius: 14,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 6,
    elevation: 4,
  },
  dismissButtonText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
  gratuityContainer: {
    marginTop: 12,
    marginBottom: 8,
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    padding: 10,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  gratuityTitle: {
    fontSize: 12,
    fontWeight: '700',
    color: '#475569',
    marginBottom: 8,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  gratuityRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 8,
  },
  gratuityButton: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 10,
    paddingVertical: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  gratuityButtonActive: {
    backgroundColor: '#FFFBEB',
    borderColor: '#D97706',
  },
  gratuityButtonText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#64748B',
  },
  gratuityButtonTextActive: {
    color: '#B45309',
  },
  gratuityValText: {
    fontSize: 9,
    fontWeight: '600',
    color: '#94A3B8',
    marginTop: 2,
  },
  gratuityValTextActive: {
    color: '#D97706',
  },
  tipLabel: {
    fontSize: 13,
    color: '#D97706',
    fontWeight: '600',
  },
  tipValue: {
    fontSize: 13,
    fontWeight: '700',
    color: '#D97706',
  },
  receiptTipLabel: {
    fontSize: 13,
    color: '#D97706',
    fontWeight: '700',
  },
  receiptTipValue: {
    fontSize: 13,
    color: '#D97706',
    fontWeight: '800',
  },
});
