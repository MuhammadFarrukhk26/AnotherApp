import React, { useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ActivityIndicator,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  Animated as RNAnimated,
  Image,
  Share,
  Clipboard,
} from 'react-native';
import { useBookingStore } from '../state/bookingStore';
import { Card } from '../components/Card';
import { WorkerSummary } from '../components/WorkerSummary';
import { RatingModal } from '../components/RatingModal';
import { ETADisplay } from '../components/ETADisplay';
import { BillingCard } from '../components/BillingCard';
import { LiveWorkerTracking } from '../components/LiveWorkerTracking';
import { BookingDetailSkeleton } from '../components/BookingDetailSkeleton';
import { OrderSummaryList } from '../components/OrderSummaryList';
import { StatusBadge } from '../components/StatusBadge';
import { BookingDetailErrorBoundary } from '../components/BookingDetailErrorBoundary';

// Try to dynamically load react-native-reanimated for layout transitions
let Reanimated: any = null;
let AnimatedView: any = null;
let FadeIn: any = null;
let FadeOut: any = null;

try {
  Reanimated = require('react-native-reanimated');
  AnimatedView = Reanimated.default?.View || Reanimated.Animated?.View || Reanimated.default;
  FadeIn = Reanimated.FadeIn;
  FadeOut = Reanimated.FadeOut;
} catch (e) {
  console.log('[Reanimated] react-native-reanimated fallback initialized.');
}

const TransitionWrapper: React.FC<{
  visible: boolean;
  children: React.ReactNode;
}> = ({ visible, children }) => {
  const [shouldRender, setShouldRender] = useState(visible);
  const fadeAnim = useRef(new RNAnimated.Value(visible ? 1 : 0)).current;
  const slideAnim = useRef(new RNAnimated.Value(visible ? 0 : 15)).current;

  useEffect(() => {
    if (visible) {
      setShouldRender(true);
      RNAnimated.parallel([
        RNAnimated.timing(fadeAnim, {
          toValue: 1,
          duration: 350,
          useNativeDriver: true,
        }),
        RNAnimated.timing(slideAnim, {
          toValue: 0,
          duration: 350,
          useNativeDriver: true,
        }),
      ]).start();
    } else {
      RNAnimated.parallel([
        RNAnimated.timing(fadeAnim, {
          toValue: 0,
          duration: 300,
          useNativeDriver: true,
        }),
        RNAnimated.timing(slideAnim, {
          toValue: 15,
          duration: 300,
          useNativeDriver: true,
        }),
      ]).start(() => {
        setShouldRender(false);
      });
    }
  }, [visible]);

  if (!shouldRender) return null;

  if (AnimatedView && FadeIn && FadeOut) {
    return (
      <AnimatedView
        entering={FadeIn.duration(400)}
        exiting={FadeOut.duration(300)}
        style={{ width: '100%' }}
      >
        {children}
      </AnimatedView>
    );
  }

  return (
    <RNAnimated.View
      style={{
        opacity: fadeAnim,
        transform: [{ translateY: slideAnim }],
        width: '100%',
      }}
    >
      {children}
    </RNAnimated.View>
  );
};

interface BookingDetailScreenProps {
  bookingId: string;
  onBack: () => void;
}

export const BookingDetailScreen: React.FC<BookingDetailScreenProps> = ({
  bookingId,
  onBack,
}) => {
  const { currentBooking, loading, error, fetchBookingDetails } = useBookingStore();
  const [localStatus, setLocalStatus] = useState<string | null>(null);
  const [showRatingModal, setShowRatingModal] = useState(false);
  const [showMap, setShowMap] = useState(true);

  // Job Duration Extension State
  const [extraPrice, setExtraPrice] = useState(0);
  const [extensionMinutes, setExtensionMinutes] = useState(0);
  const [extensionApproved, setExtensionApproved] = useState(false);
  const [selectedOption, setSelectedOption] = useState<{ mins: number; price: number } | null>({ mins: 30, price: 500 });
  const [workerRequestActive, setWorkerRequestActive] = useState(true);

  // Share Live Status State
  const [isGenerating, setIsGenerating] = useState(false);
  const [shareLinkGenerated, setShareLinkGenerated] = useState(false);
  const [generatedLink, setGeneratedLink] = useState('');
  const [copied, setCopied] = useState(false);

  const handleGenerateLink = () => {
    setIsGenerating(true);
    setTimeout(() => {
      const tempToken = Math.random().toString(36).substring(2, 10).toUpperCase();
      const link = `https://hazir-app.com/track/share/${bookingId}?token=SH_${tempToken}`;
      setGeneratedLink(link);
      setShareLinkGenerated(true);
      setIsGenerating(false);
    }, 700);
  };

  const handleCopyToClipboard = () => {
    try {
      Clipboard.setString(generatedLink);
    } catch (e) {
      console.log('Clipboard copy failed', e);
    }
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleNativeShare = async () => {
    try {
      const serviceName = currentBooking ? currentBooking.serviceType : 'service';
      const statusText = activeStatus.replace('_', ' ').toLowerCase();
      const message = `Assalaamu alaikum! I am sharing my live tracking link for my Hazir ${serviceName} booking. The technician is currently on status: "${statusText}". You can track their live location and status update here: ${generatedLink}`;
      
      await Share.share({
        message,
        title: 'Hazir Live Tracking Share',
      });
    } catch (error) {
      console.log('Error sharing tracking link:', error);
    }
  };

  useEffect(() => {
    fetchBookingDetails(bookingId);
  }, [bookingId]);

  // Synchronize local simulated status when remote booking details load
  useEffect(() => {
    if (currentBooking) {
      setLocalStatus(currentBooking.status);
    }
  }, [currentBooking]);

  const activeStatus = localStatus || (currentBooking ? currentBooking.status : 'PENDING');

  const finalPrice = currentBooking ? currentBooking.price + extraPrice : 0;
  const formattedPrice = currentBooking
    ? `PKR ${finalPrice.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
    : '';

  return (
    <BookingDetailErrorBoundary
      bookingId={bookingId}
      onBack={onBack}
      onRetry={() => fetchBookingDetails(bookingId)}
    >
      <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={onBack} style={styles.backButton}>
          <Text style={styles.backText}>← Back</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Booking Tracker</Text>
        <View style={styles.headerPlaceholder} />
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent}>
        {loading && <BookingDetailSkeleton />}

        {error && (
          <View style={styles.centerContainer}>
            <Text style={styles.errorText}>Error: {error}</Text>
            <TouchableOpacity
              style={styles.retryButton}
              onPress={() => fetchBookingDetails(bookingId)}
            >
              <Text style={styles.retryText}>Retry</Text>
            </TouchableOpacity>
          </View>
        )}

        {!loading && !error && currentBooking && (
          <View>
            <Card>
              <View style={styles.statusRow}>
                <Text style={styles.serviceLabel}>{currentBooking.serviceType}</Text>
                <StatusBadge status={activeStatus} />
              </View>
              
              <Text style={styles.bookingId}>ID: {currentBooking.id}</Text>
              <View style={styles.divider} />

              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Schedule:</Text>
                <Text style={styles.detailValue}>
                  {new Date(currentBooking.scheduledTime).toLocaleString()}
                </Text>
              </View>

              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Location:</Text>
                <Text style={styles.detailValue}>{currentBooking.address}</Text>
              </View>

              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Estimated Price:</Text>
                <Text style={styles.priceValue}>{formattedPrice}</Text>
              </View>
            </Card>

            {/* Share Live Status Component */}
            <Card style={styles.shareCard}>
              <View style={styles.shareHeaderRow}>
                <Text style={styles.shareTitle}>🛡️ Safety & Live Share</Text>
              </View>
              <Text style={styles.shareSubtitle}>
                Let trusted contacts track your service status and live technician location in real-time.
              </Text>
              
              {shareLinkGenerated ? (
                <View style={styles.shareSuccessContainer}>
                  <Text style={styles.shareSuccessTitle}>✅ Secure Tracking Link Active</Text>
                  <Text style={styles.shareSuccessText} numberOfLines={1} ellipsizeMode="middle">
                    {generatedLink}
                  </Text>
                  <View style={styles.shareActionButtons}>
                    <TouchableOpacity 
                      style={styles.shareButtonSecondary}
                      onPress={handleCopyToClipboard}
                    >
                      <Text style={styles.shareButtonSecondaryText}>{copied ? 'Copied!' : '📋 Copy Link'}</Text>
                    </TouchableOpacity>
                    <TouchableOpacity 
                      style={styles.shareButtonPrimary}
                      onPress={handleNativeShare}
                    >
                      <Text style={styles.shareButtonPrimaryText}>📤 Send Link</Text>
                    </TouchableOpacity>
                  </View>
                </View>
              ) : (
                <TouchableOpacity
                  style={styles.generateLinkButton}
                  onPress={handleGenerateLink}
                  activeOpacity={0.8}
                >
                  {isGenerating ? (
                    <ActivityIndicator size="small" color="#FFFFFF" />
                  ) : (
                    <Text style={styles.generateLinkButtonText}>🔗 Generate Temporary Live Tracking Link</Text>
                  )}
                </TouchableOpacity>
              )}
            </Card>

            {/* View Switcher Segmented Control */}
            <View style={styles.tabContainer}>
              <TouchableOpacity
                style={[styles.tabButton, showMap && styles.activeTabButton]}
                onPress={() => setShowMap(true)}
                activeOpacity={0.8}
              >
                <Text style={[styles.tabButtonText, showMap && styles.activeTabButtonText]}>
                  📍 Live Tracking Map
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.tabButton, !showMap && styles.activeTabButton]}
                onPress={() => setShowMap(false)}
                activeOpacity={0.8}
              >
                <Text style={[styles.tabButtonText, !showMap && styles.activeTabButtonText]}>
                  📄 Order Summary List
                </Text>
              </TouchableOpacity>
            </View>

            <TransitionWrapper visible={showMap}>
              <View>
                {currentBooking.workerId ? (
                  <View>
                    <WorkerSummary
                      workerId={currentBooking.workerId}
                      workerName="Ayaan Sheikh"
                      workerPhone="+92 321 4567890"
                      workerRating={4.9}
                      workerJobsCount={186}
                      avatarUrl="https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200"
                    />

                    <LiveWorkerTracking
                      customerLat={currentBooking.latitude}
                      customerLng={currentBooking.longitude}
                      workerId={currentBooking.workerId}
                    />
                    
                    <ETADisplay
                      customerLat={currentBooking.latitude}
                      customerLng={currentBooking.longitude}
                      workerId={currentBooking.workerId}
                      onArrived={() => {
                        console.log('Worker has arrived at service destination');
                      }}
                    />
                  </View>
                ) : (
                  <Card style={styles.workerCard}>
                    <Text style={styles.workerTitle}>Assigned Provider</Text>
                    <Text style={styles.noWorkerText}>Searching for matching nearby professionals...</Text>
                  </Card>
                )}
              </View>
            </TransitionWrapper>

            <TransitionWrapper visible={!showMap}>
              <OrderSummaryList
                bookingId={currentBooking.id}
                serviceType={currentBooking.serviceType}
                scheduledTime={currentBooking.scheduledTime}
                address={currentBooking.address}
                price={currentBooking.price}
              />
            </TransitionWrapper>

            {/* Job Duration Extension Component */}
            {activeStatus !== 'COMPLETED' && (
              <View style={styles.extensionContainer}>
                <View style={styles.extensionHeaderRow}>
                  <Text style={styles.extensionSectionTitle}>⏱️ Job Duration Extension</Text>
                  {workerRequestActive && !extensionApproved && (
                    <View style={styles.requestAlertBadge}>
                      <Text style={styles.requestAlertBadgeText}>Worker Requested</Text>
                    </View>
                  )}
                </View>

                {!extensionApproved ? (
                  <View style={styles.extensionActiveBox}>
                    <View style={styles.workerRequestPrompt}>
                      <View style={styles.workerMiniAvatar}>
                        <Text style={styles.workerAvatarEmoji}>👷</Text>
                      </View>
                      <View style={styles.workerMessageBubble}>
                        <Text style={styles.workerNameTitle}>Ayaan Sheikh (Provider)</Text>
                        <Text style={styles.workerRequestText}>
                          "Assalaamu alaikum! The {currentBooking.serviceType.toLowerCase()} is taking a bit more complexity than estimated due to technical diagnostics. Can we extend the duration to finish properly?"
                        </Text>
                      </View>
                    </View>

                    <Text style={styles.selectOptionLabel}>Select Extension Duration:</Text>
                    <View style={styles.optionsRow}>
                      {[
                        { mins: 30, price: 500, label: '+30 Mins' },
                        { mins: 60, price: 900, label: '+60 Mins' },
                        { mins: 90, price: 1300, label: '+90 Mins' },
                      ].map((opt) => {
                        const isSelected = selectedOption?.mins === opt.mins;
                        return (
                          <TouchableOpacity
                            key={opt.mins}
                            style={[
                              styles.optionCard,
                              isSelected && styles.optionCardSelected,
                            ]}
                            onPress={() => setSelectedOption({ mins: opt.mins, price: opt.price })}
                            activeOpacity={0.8}
                          >
                            <Text style={[styles.optionLabel, isSelected && styles.optionLabelSelected]}>
                              {opt.label}
                            </Text>
                            <Text style={[styles.optionPrice, isSelected && styles.optionPriceSelected]}>
                              PKR {opt.price}
                            </Text>
                          </TouchableOpacity>
                        );
                      })}
                    </View>

                    <View style={styles.extensionActionRow}>
                      <TouchableOpacity
                        style={styles.declineButton}
                        onPress={() => {
                          setWorkerRequestActive(false);
                          setSelectedOption(null);
                        }}
                        activeOpacity={0.8}
                      >
                        <Text style={styles.declineButtonText}>Decline</Text>
                      </TouchableOpacity>
                      <TouchableOpacity
                        style={styles.approveButton}
                        onPress={() => {
                          if (selectedOption) {
                            setExtraPrice(selectedOption.price);
                            setExtensionMinutes(selectedOption.mins);
                            setExtensionApproved(true);
                          }
                        }}
                        activeOpacity={0.8}
                        disabled={!selectedOption}
                      >
                        <Text style={styles.approveButtonText}>Approve & Extend</Text>
                      </TouchableOpacity>
                    </View>
                  </View>
                ) : (
                  <View style={styles.extensionSuccessBox}>
                    <View style={styles.successIconContainer}>
                      <Text style={styles.successEmoji}>✅</Text>
                    </View>
                    <View style={styles.successContent}>
                      <Text style={styles.successTitle}>Extension Authorized</Text>
                      <Text style={styles.successDesc}>
                        An additional {extensionMinutes} minutes of service has been successfully added to your order.
                      </Text>
                      <View style={styles.addedPricePill}>
                        <Text style={styles.addedPriceText}>+ PKR {extraPrice.toLocaleString()} added to Invoice</Text>
                      </View>
                    </View>
                    <TouchableOpacity
                      style={styles.changeExtensionButton}
                      onPress={() => {
                        setExtensionApproved(false);
                        setExtraPrice(0);
                        setExtensionMinutes(0);
                        setSelectedOption({ mins: 30, price: 500 });
                      }}
                      activeOpacity={0.8}
                    >
                      <Text style={styles.changeExtensionButtonText}>Modify</Text>
                    </TouchableOpacity>
                  </View>
                )}

                {/* Proactive customer addition section when there is no active worker request and no approval */}
                {!workerRequestActive && !extensionApproved && (
                  <TouchableOpacity
                    style={styles.reopenRequestButton}
                    onPress={() => {
                      setWorkerRequestActive(true);
                      setSelectedOption({ mins: 30, price: 500 });
                    }}
                    activeOpacity={0.8}
                  >
                    <Text style={styles.reopenRequestButtonText}>
                      ➕ Need to manually request a Duration Extension?
                    </Text>
                  </TouchableOpacity>
                )}
              </View>
            )}

            {/* Service Proof Photo Verification Component */}
            {activeStatus === 'COMPLETED' && (
              <View style={styles.proofContainer}>
                <View style={styles.proofHeaderRow}>
                  <Text style={styles.proofSectionTitle}>🛡️ Service Proof Verification</Text>
                  <View style={styles.verifiedBadge}>
                    <Text style={styles.verifiedBadgeText}>Quality Verified</Text>
                  </View>
                </View>

                <Text style={styles.proofDescription}>
                  Your technician has captured and uploaded these 'Before' and 'After' photos of the job site to guarantee service standards:
                </Text>

                <View style={styles.proofImagesRow}>
                  <View style={styles.proofImageWrapper}>
                    <Image
                      source={{ uri: currentBooking?.beforePhoto || 'https://images.unsplash.com/photo-1581094288338-2314dddb7eed?q=80&w=600' }}
                      style={styles.proofImage}
                      resizeMode="cover"
                    />
                    <Text style={styles.proofImageTag}>BEFORE WORK</Text>
                  </View>

                  <View style={styles.proofImageWrapper}>
                    <Image
                      source={{ uri: currentBooking?.afterPhoto || 'https://images.unsplash.com/photo-1504307651254-35680f356dfd?q=80&w=600' }}
                      style={styles.proofImage}
                      resizeMode="cover"
                    />
                    <Text style={[styles.proofImageTag, styles.proofImageTagSuccess]}>AFTER WORK</Text>
                  </View>
                </View>
              </View>
            )}

            <BillingCard totalPrice={finalPrice} extraPrice={extraPrice} />

            {/* Quick Action to Complete Job and Rate */}
            {activeStatus !== 'COMPLETED' ? (
              <TouchableOpacity
                style={styles.completeJobButton}
                onPress={() => {
                  setLocalStatus('COMPLETED');
                  setShowRatingModal(true);
                }}
                activeOpacity={0.8}
              >
                <Text style={styles.completeJobButtonText}>✓ Complete Service & Review</Text>
              </TouchableOpacity>
            ) : (
              <TouchableOpacity
                style={styles.reviewButton}
                onPress={() => setShowRatingModal(true)}
                activeOpacity={0.8}
              >
                <Text style={styles.reviewButtonText}>★ Leave Another Star Review</Text>
              </TouchableOpacity>
            )}

            <RatingModal
              visible={showRatingModal}
              onClose={() => setShowRatingModal(false)}
              onSubmit={(rating, review) => {
                console.log(`Submitted review for Ayaan Sheikh: Rating ${rating}, ${review}`);
              }}
              workerName="Ayaan Sheikh"
            />
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
    </BookingDetailErrorBoundary>
  );
};

function getStatusColor(status: string): string {
  switch (status) {
    case 'PENDING':
      return '#F59E0B';
    case 'ACCEPTED':
      return '#3B82F6';
    case 'IN_PROGRESS':
      return '#10B981';
    case 'COMPLETED':
      return '#059669';
    default:
      return '#EF4444';
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  tabContainer: {
    flexDirection: 'row',
    backgroundColor: '#E2E8F0',
    borderRadius: 14,
    padding: 4,
    marginTop: 16,
    marginBottom: 8,
  },
  tabButton: {
    flex: 1,
    paddingVertical: 10,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 10,
  },
  activeTabButton: {
    backgroundColor: '#FFFFFF',
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 1,
  },
  tabButtonText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#64748B',
  },
  activeTabButtonText: {
    color: '#0F172A',
    fontWeight: '700',
  },
  header: {
    height: 56,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderColor: '#E2E8F0',
  },
  backButton: {
    paddingVertical: 8,
    paddingHorizontal: 12,
  },
  backText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#0F172A',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#0F172A',
  },
  headerPlaceholder: {
    width: 60,
  },
  scrollContent: {
    padding: 16,
  },
  centerContainer: {
    marginTop: 100,
    alignItems: 'center',
    justifyContent: 'center',
  },
  loadingText: {
    marginTop: 12,
    fontSize: 14,
    color: '#64748B',
  },
  errorText: {
    fontSize: 14,
    color: '#EF4444',
    textAlign: 'center',
    marginBottom: 16,
  },
  retryButton: {
    backgroundColor: '#0F172A',
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 8,
  },
  retryText: {
    color: '#FFFFFF',
    fontWeight: '600',
  },
  statusRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  serviceLabel: {
    fontSize: 20,
    fontWeight: '700',
    color: '#0F172A',
  },
  statusBadge: {
    paddingVertical: 4,
    paddingHorizontal: 10,
    borderRadius: 9999,
  },
  statusText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '600',
  },
  bookingId: {
    fontSize: 12,
    color: '#64748B',
    marginBottom: 16,
  },
  divider: {
    height: 1,
    backgroundColor: '#E2E8F0',
    marginBottom: 16,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  detailLabel: {
    fontSize: 14,
    color: '#64748B',
    width: '35%',
  },
  detailValue: {
    fontSize: 14,
    fontWeight: '500',
    color: '#0F172A',
    width: '65%',
    textAlign: 'right',
  },
  priceValue: {
    fontSize: 16,
    fontWeight: '700',
    color: '#10B981',
  },
  workerCard: {
    marginTop: 16,
    borderLeftWidth: 4,
    borderLeftColor: '#10B981',
  },
  workerTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 8,
  },
  workerName: {
    fontSize: 14,
    fontWeight: '600',
    color: '#334155',
  },
  workerDetails: {
    fontSize: 12,
    color: '#64748B',
    marginTop: 4,
  },
  noWorkerText: {
    fontSize: 14,
    color: '#64748B',
    fontStyle: 'italic',
  },
  completeJobButton: {
    backgroundColor: '#F59E0B',
    borderRadius: 14,
    height: 52,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 20,
    shadowColor: '#F59E0B',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 3,
  },
  completeJobButtonText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
  reviewButton: {
    backgroundColor: '#0F172A',
    borderRadius: 14,
    height: 52,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 20,
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 3,
  },
  reviewButtonText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
  extensionContainer: {
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
  extensionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  extensionSectionTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  requestAlertBadge: {
    backgroundColor: '#FFF3CD',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#FFEBAA',
  },
  requestAlertBadgeText: {
    color: '#856404',
    fontSize: 10,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  extensionActiveBox: {
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#F1F5F9',
  },
  workerRequestPrompt: {
    flexDirection: 'row',
    marginBottom: 12,
  },
  workerMiniAvatar: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#E2E8F0',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  workerAvatarEmoji: {
    fontSize: 18,
  },
  workerMessageBubble: {
    flex: 1,
  },
  workerNameTitle: {
    fontSize: 12,
    fontWeight: '700',
    color: '#475569',
    marginBottom: 2,
  },
  workerRequestText: {
    fontSize: 12.5,
    color: '#334155',
    lineHeight: 18,
    fontStyle: 'italic',
  },
  selectOptionLabel: {
    fontSize: 12,
    fontWeight: '700',
    color: '#64748B',
    marginBottom: 8,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  optionsRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 14,
    gap: 8,
  },
  optionCard: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    paddingVertical: 8,
    paddingHorizontal: 6,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
  },
  optionCardSelected: {
    borderColor: '#3B82F6',
    backgroundColor: '#EFF6FF',
  },
  optionLabel: {
    fontSize: 12,
    fontWeight: '700',
    color: '#475569',
  },
  optionLabelSelected: {
    color: '#1D4ED8',
  },
  optionPrice: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 2,
  },
  optionPriceSelected: {
    color: '#2563EB',
    fontWeight: '600',
  },
  extensionActionRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: 8,
  },
  declineButton: {
    paddingVertical: 8,
    paddingHorizontal: 14,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#CBD5E1',
    backgroundColor: '#FFFFFF',
  },
  declineButtonText: {
    color: '#64748B',
    fontSize: 12.5,
    fontWeight: '600',
  },
  approveButton: {
    paddingVertical: 8,
    paddingHorizontal: 14,
    borderRadius: 8,
    backgroundColor: '#3B82F6',
  },
  approveButtonText: {
    color: '#FFFFFF',
    fontSize: 12.5,
    fontWeight: '700',
  },
  extensionSuccessBox: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#ECFDF5',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#D1FAE5',
  },
  successIconContainer: {
    marginRight: 10,
  },
  successEmoji: {
    fontSize: 22,
  },
  successContent: {
    flex: 1,
  },
  successTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#065F46',
  },
  successDesc: {
    fontSize: 11.5,
    color: '#047857',
    marginTop: 1,
  },
  addedPricePill: {
    alignSelf: 'flex-start',
    backgroundColor: '#D1FAE5',
    borderRadius: 6,
    paddingHorizontal: 6,
    paddingVertical: 2,
    marginTop: 6,
  },
  addedPriceText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#065F46',
  },
  changeExtensionButton: {
    paddingVertical: 6,
    paddingHorizontal: 10,
    backgroundColor: '#FFFFFF',
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#A7F3D0',
  },
  changeExtensionButtonText: {
    color: '#047857',
    fontSize: 11,
    fontWeight: '600',
  },
  reopenRequestButton: {
    paddingVertical: 10,
    backgroundColor: '#F8FAFC',
    borderRadius: 10,
    borderWidth: 1,
    borderStyle: 'dashed',
    borderColor: '#CBD5E1',
    alignItems: 'center',
    justifyContent: 'center',
  },
  reopenRequestButtonText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#64748B',
  },
  proofContainer: {
    backgroundColor: '#F0FDF4',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#DCFCE7',
    marginTop: 16,
    marginBottom: 8,
  },
  proofHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  proofSectionTitle: {
    fontSize: 14.5,
    fontWeight: '700',
    color: '#166534',
  },
  verifiedBadge: {
    backgroundColor: '#DCFCE7',
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 3,
  },
  verifiedBadgeText: {
    color: '#15803D',
    fontSize: 10,
    fontWeight: '800',
    textTransform: 'uppercase',
  },
  proofDescription: {
    fontSize: 12,
    color: '#3F6212',
    lineHeight: 18,
    marginBottom: 12,
  },
  proofImagesRow: {
    flexDirection: 'row',
    gap: 12,
  },
  proofImageWrapper: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    padding: 6,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  proofImage: {
    width: '100%',
    height: 100,
    borderRadius: 8,
  },
  proofImageTag: {
    fontSize: 9.5,
    fontWeight: '800',
    color: '#64748B',
    marginTop: 6,
    letterSpacing: 0.5,
  },
  proofImageTagSuccess: {
    color: '#16A34A',
  },
  shareCard: {
    backgroundColor: '#EFF6FF',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#DBEAFE',
    marginTop: 12,
    marginBottom: 8,
  },
  shareHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  shareTitle: {
    fontSize: 14.5,
    fontWeight: '700',
    color: '#1E40AF',
  },
  shareSubtitle: {
    fontSize: 11.5,
    color: '#1E3A8A',
    opacity: 0.8,
    lineHeight: 16,
    marginBottom: 12,
  },
  shareSuccessContainer: {
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    padding: 12,
    borderWidth: 1,
    borderColor: '#BFDBFE',
  },
  shareSuccessTitle: {
    fontSize: 12,
    fontWeight: '700',
    color: '#15803D',
    marginBottom: 4,
  },
  shareSuccessText: {
    fontSize: 11,
    color: '#1E3A8A',
    backgroundColor: '#F1F5F9',
    paddingHorizontal: 8,
    paddingVertical: 6,
    borderRadius: 6,
    marginBottom: 10,
  },
  shareActionButtons: {
    flexDirection: 'row',
    gap: 8,
  },
  shareButtonSecondary: {
    flex: 1,
    backgroundColor: '#F8FAFC',
    borderRadius: 8,
    paddingVertical: 8,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#CBD5E1',
  },
  shareButtonSecondaryText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#475569',
  },
  shareButtonPrimary: {
    flex: 1,
    backgroundColor: '#3B82F6',
    borderRadius: 8,
    paddingVertical: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  shareButtonPrimaryText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  generateLinkButton: {
    backgroundColor: '#3B82F6',
    borderRadius: 10,
    paddingVertical: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  generateLinkButtonText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#FFFFFF',
  },
});
