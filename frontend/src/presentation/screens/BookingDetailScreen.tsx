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
  Modal,
  TextInput,
  TouchableWithoutFeedback,
  Keyboard,
  Alert,
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
import { BookingCountdownTimer } from '../components/BookingCountdownTimer';
import { BookingReviewFeedback } from '../components/BookingReviewFeedback';
import { BookingChatDrawer } from '../components/BookingChatDrawer';
import { BookingActivityTimeline } from '../components/BookingActivityTimeline';
import { BookingCalendarSync } from '../components/BookingCalendarSync';
import { BookingFAQSection } from '../components/BookingFAQSection';
import { PaymentProcessingFlow } from '../components/PaymentProcessingFlow';
import { useNotifications } from '../components/NotificationManager';
import axios from 'axios';

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
  const { showNotification } = useNotifications();
  const { currentBooking, loading, error, fetchBookingDetails } = useBookingStore();
  const [localStatus, setLocalStatus] = useState<string | null>(null);
  const [localPaymentStatus, setLocalPaymentStatus] = useState<'UNPAID' | 'PAID'>('UNPAID');
  const [localPaymentMethod, setLocalPaymentMethod] = useState<string>('');
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
  const [isChatOpen, setIsChatOpen] = useState(false);

  // Network connection status
  const [isOnline, setIsOnline] = useState(true);
  const [wasOffline, setWasOffline] = useState(false);

  useEffect(() => {
    const handleOnline = () => {
      setIsOnline(true);
    };
    const handleOffline = () => {
      setIsOnline(false);
      setWasOffline(true);
    };

    if (typeof navigator !== 'undefined' && typeof navigator.onLine === 'boolean') {
      setIsOnline(navigator.onLine);
    }

    if (typeof window !== 'undefined') {
      window.addEventListener('online', handleOnline);
      window.addEventListener('offline', handleOffline);
    }

    return () => {
      if (typeof window !== 'undefined') {
        window.removeEventListener('online', handleOnline);
        window.removeEventListener('offline', handleOffline);
      }
    };
  }, []);

  useEffect(() => {
    if (isOnline && wasOffline) {
      Alert.alert('Back Online', 'Your internet connection has been restored. Live tracking resumed.');
      setWasOffline(false);
    }
  }, [isOnline, wasOffline]);

  // Cancellation Flow State
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReasonStep, setCancelReasonStep] = useState(1); // 1 = Reason selection, 2 = Confirmation
  const [selectedReason, setSelectedReason] = useState<string>('');
  const [otherReasonText, setOtherReasonText] = useState<string>('');

  const cancellationReasons = [
    "Technician is delayed / not responding",
    "Schedule conflict / changed plans",
    "Found alternative solution elsewhere",
    "Incorrect service type or details selected",
    "Emergency / no longer required",
    "Other reason"
  ];

  const handleConfirmCancel = () => {
    Alert.alert(
      'Confirm Cancellation',
      'Are you sure you want to cancel this service request? This action cannot be undone.',
      [
        {
          text: 'No, Keep It',
          style: 'cancel',
        },
        {
          text: 'Yes, Cancel',
          style: 'destructive',
          onPress: async () => {
            try {
              console.log('[Cancel Flow] Initiating cancellation request on backend...');
              await axios.patch(
                `https://api.hazir-app.com/api/v1/bookings/${bookingId}/status`,
                {
                  status: 'CANCELLED',
                }
              );
              await fetchBookingDetails(bookingId);
            } catch (err) {
              console.error('[Cancel Flow] Failed to cancel booking on backend, falling back to local state:', err);
            }
            setLocalStatus('CANCELLED');
            setShowCancelModal(false);
            // Reset state
            setCancelReasonStep(1);
            setSelectedReason('');
            setOtherReasonText('');
            Alert.alert(
              'Booking Cancelled',
              'Your booking has been successfully cancelled. The provider has been notified.',
              [{ text: 'OK' }]
            );
          }
        }
      ]
    );
  };

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
      if (!currentBooking) return;
      const serviceName = currentBooking.serviceType;
      const statusText = activeStatus.replace('_', ' ').toUpperCase();
      const worker = "Ayaan Sheikh"; // Default worker in mock
      const scheduledTimeStr = new Date(currentBooking.scheduledTime).toLocaleString();
      const address = currentBooking.address;
      const priceStr = formattedPrice;
      const message = `🛠️ HAZIR SERVICE BOOKING DETAILS 🛠️

Assalaamu Alaikum! Here are the tracking & booking details for my Hazir service:

• Booking ID: #${currentBooking.id}
• Service: ${serviceName}
• Technician: ${worker}
• Status: ${statusText}
• Scheduled/Arrival Time: ${scheduledTimeStr}
• Address: ${address}
• Estimated Price: ${priceStr}

📍 Live Tracking Link: ${generatedLink}

Thank you for using Hazir – your instant on-demand handyman partner!`;
      
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
      setLocalPaymentStatus(currentBooking.paymentStatus || 'UNPAID');
      setLocalPaymentMethod(currentBooking.paymentMethod || '');
    }
  }, [currentBooking]);

  const prevStatusRef = useRef<string | null>(null);
  const prevMessagesCountRef = useRef<number>(-1);
  const isChatOpenRef = useRef(isChatOpen);

  // Keep ref in sync to avoid effect re-binding
  useEffect(() => {
    isChatOpenRef.current = isChatOpen;
  }, [isChatOpen]);

  // Main Background Polling and Simulation Trigger
  useEffect(() => {
    // Initialize refs with initial values when booking loads
    if (currentBooking) {
      if (prevStatusRef.current === null) {
        prevStatusRef.current = currentBooking.status;
      }
    }

    // 1. Setup a poller to refresh booking details & status from backend
    const detailsInterval = setInterval(() => {
      if (isOnline) {
        fetchBookingDetails(bookingId).catch((err) =>
          console.log('[Background Tracker] Failed to fetch details:', err)
        );
      }
    }, 4000);

    // 2. Setup a simulation for PENDING booking acceptance
    let acceptTimeout: NodeJS.Timeout | null = null;
    if (currentBooking && currentBooking.status === 'PENDING') {
      acceptTimeout = setTimeout(async () => {
        try {
          console.log('[Simulation] Sending status update ACCEPTED for pending booking...');
          const response = await axios.patch(
            `https://api.hazir-app.com/api/v1/bookings/${bookingId}/status`,
            {
              status: 'ACCEPTED',
              workerId: 'worker_ayaan_sheikh',
            }
          );
          if (response.data?.success) {
            fetchBookingDetails(bookingId);
          }
        } catch (e) {
          console.warn('[Simulation] Failed to automatically advance status, falling back to local simulation:', e);
          // Fallback local status simulation
          setLocalStatus('ACCEPTED');
          showNotification(
            '🛠️ Technician Assigned!',
            'Ayaan Sheikh has accepted your booking and is preparing tools.',
            {
              icon: '🛠️',
              avatar: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200',
              onPress: () => setIsChatOpen(true),
            }
          );
        }
      }, 6000);
    }

    // 3. Setup message poller when chat is closed to alert of new incoming messages
    const chatInterval = setInterval(async () => {
      // Only poll when chat is NOT open to show notifications
      if (!isChatOpenRef.current && isOnline) {
        try {
          const response = await axios.get<{ success: boolean; data: any[] }>(
            `https://api.hazir-app.com/api/v1/bookings/${bookingId}/messages`
          );
          if (response.data?.success && response.data.data) {
            const messages = response.data.data;
            const currentCount = messages.length;

            if (prevMessagesCountRef.current === -1) {
              // Initial load of messages, just initialize count ref
              prevMessagesCountRef.current = currentCount;
            } else if (currentCount > prevMessagesCountRef.current) {
              // Find new messages
              const newMsgs = messages.slice(prevMessagesCountRef.current);
              newMsgs.forEach((msg) => {
                if (msg.sender === 'worker') {
                  showNotification(
                    '💬 Message from Ayaan',
                    msg.text,
                    {
                      avatar: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200',
                      onPress: () => setIsChatOpen(true),
                    }
                  );
                }
              });
              prevMessagesCountRef.current = currentCount;
            }
          }
        } catch (e) {
          console.log('[Background Messages Tracker] polling failed:', e);
        }
      } else if (isChatOpenRef.current && isOnline) {
        // If chat is open, keep count in sync so closing it later doesn't trigger backlogs
        try {
          const response = await axios.get<{ success: boolean; data: any[] }>(
            `https://api.hazir-app.com/api/v1/bookings/${bookingId}/messages`
          );
          if (response.data?.success && response.data.data) {
            prevMessagesCountRef.current = response.data.data.length;
          }
        } catch (e) {
          // Silent catch
        }
      }
    }, 3000);

    return () => {
      clearInterval(detailsInterval);
      clearInterval(chatInterval);
      if (acceptTimeout) clearTimeout(acceptTimeout);
    };
  }, [bookingId, currentBooking?.status, isOnline]);

  // Effect to watch status transitions of currentBooking and trigger local notifications
  useEffect(() => {
    if (currentBooking) {
      const prevStatus = prevStatusRef.current;
      const currentStatus = currentBooking.status;

      if (prevStatus && prevStatus !== currentStatus) {
        if (prevStatus === 'PENDING' && currentStatus === 'ACCEPTED') {
          showNotification(
            '🛠️ Technician Assigned!',
            'Ayaan Sheikh has accepted your plumbing booking and is preparing tools.',
            {
              icon: '🛠️',
              avatar: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200',
              onPress: () => setIsChatOpen(true),
            }
          );
        } else if (prevStatus === 'ACCEPTED' && currentStatus === 'IN_PROGRESS') {
          showNotification(
            '🚙 Technician En Route!',
            'Ayaan Sheikh is traveling to your location with your plumbing tools.',
            {
              icon: '🚙',
              avatar: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200',
              onPress: () => setIsChatOpen(true),
            }
          );
        } else if (prevStatus === 'IN_PROGRESS' && currentStatus === 'COMPLETED') {
          showNotification(
            '🎉 Service Completed!',
            'Your plumbing service has been completed. Please review the invoice and pay.',
            {
              icon: '🎉',
              avatar: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200',
            }
          );
        }
      }
      prevStatusRef.current = currentStatus;
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

      {!isOnline && (
        <View style={styles.offlineBanner}>
          <Text style={styles.offlineText}>⚠️ No internet connection. Tracking paused.</Text>
        </View>
      )}

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
            
            <BookingCalendarSync
              bookingId={currentBooking.id}
              serviceType={currentBooking.serviceType}
              scheduledTime={currentBooking.scheduledTime}
              address={currentBooking.address}
              workerName="Ayaan Sheikh"
            />

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
                      onMessagePressed={() => setIsChatOpen(true)}
                    />

                    <BookingCountdownTimer
                      customerLat={currentBooking.latitude}
                      customerLng={currentBooking.longitude}
                      workerName="Ayaan Sheikh"
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

            <BookingActivityTimeline currentStatus={activeStatus} />

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

            {activeStatus === 'COMPLETED' && (
              <BookingReviewFeedback
                workerName="Ayaan Sheikh"
                workerAvatar="https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200"
                initialRating={currentBooking?.rating}
                initialReview={currentBooking?.review}
                onReviewSubmitted={async (rating, review, tags) => {
                  try {
                    console.log(`[Inline Review] Worker: Ayaan Sheikh. Rating: ${rating}. Review: ${review}. Tags: ${tags.join(', ')}`);
                    const finalReview = review + (tags.length > 0 ? ` [${tags.join(', ')}]` : '');
                    const response = await axios.post(
                      `https://api.hazir-app.com/api/v1/bookings/${bookingId}/rate`,
                      {
                        rating,
                        review: finalReview,
                      }
                    );
                    if (response.data?.success) {
                      await fetchBookingDetails(bookingId);
                    }
                  } catch (err) {
                    console.error('Failed to submit feedback rating to backend API:', err);
                  }
                }}
              />
            )}

            {activeStatus === 'COMPLETED' && (
              <PaymentProcessingFlow
                bookingId={currentBooking.id}
                totalPrice={finalPrice}
                isAlreadyPaid={localPaymentStatus === 'PAID'}
                paymentMethod={localPaymentMethod}
                onPaymentSuccess={(method) => {
                  setLocalPaymentStatus('PAID');
                  setLocalPaymentMethod(method);
                  Alert.alert(
                    'Secure Payment Successful',
                    `Your payment of PKR ${finalPrice.toLocaleString()} via ${method} has been securely processed. Thank you for choosing Hazir!`,
                    [{ text: 'OK' }]
                  );
                }}
              />
            )}

            <BillingCard
              totalPrice={finalPrice}
              extraPrice={extraPrice}
              paymentMethod={localPaymentStatus === 'PAID' ? localPaymentMethod : 'Pending Secure Payment'}
            />

            <BookingFAQSection />

            {/* Quick Action to Complete Job and Rate */}
            {activeStatus === 'COMPLETED' ? (
              <TouchableOpacity
                style={styles.reviewButton}
                onPress={() => setShowRatingModal(true)}
                activeOpacity={0.8}
              >
                <Text style={styles.reviewButtonText}>★ Leave Another Star Review</Text>
              </TouchableOpacity>
            ) : activeStatus === 'CANCELLED' ? (
              <View style={styles.cancelledStatusCard}>
                <Text style={styles.cancelledStatusText}>❌ This booking has been cancelled.</Text>
              </View>
            ) : (
              <View>
                <TouchableOpacity
                  style={styles.completeJobButton}
                  onPress={async () => {
                    try {
                      await axios.patch(
                        `https://api.hazir-app.com/api/v1/bookings/${bookingId}/status`,
                        {
                          status: 'COMPLETED',
                        }
                      );
                      await fetchBookingDetails(bookingId);
                    } catch (err) {
                      console.log('Failed to update status to COMPLETED on backend:', err);
                    }
                    setLocalStatus('COMPLETED');
                    setShowRatingModal(true);
                  }}
                  activeOpacity={0.8}
                >
                  <Text style={styles.completeJobButtonText}>✓ Complete Service & Review</Text>
                </TouchableOpacity>

                <TouchableOpacity
                  style={styles.cancelBookingButton}
                  onPress={() => {
                    setCancelReasonStep(1);
                    setSelectedReason('');
                    setOtherReasonText('');
                    setShowCancelModal(true);
                  }}
                  activeOpacity={0.8}
                >
                  <Text style={styles.cancelBookingButtonText}>⚠️ Cancel Booking Request</Text>
                </TouchableOpacity>
              </View>
            )}

            <RatingModal
              visible={showRatingModal}
              onClose={() => setShowRatingModal(false)}
              onSubmit={async (rating, review) => {
                try {
                  console.log(`Submitted review for Ayaan Sheikh via modal: Rating ${rating}, ${review}`);
                  const response = await axios.post(
                    `https://api.hazir-app.com/api/v1/bookings/${bookingId}/rate`,
                    {
                      rating,
                      review,
                    }
                  );
                  if (response.data?.success) {
                    await fetchBookingDetails(bookingId);
                  }
                } catch (err) {
                  console.error('Failed to submit modal feedback rating to backend API:', err);
                }
              }}
              workerName="Ayaan Sheikh"
            />

            <Modal
              visible={showCancelModal}
              transparent
              animationType="slide"
              statusBarTranslucent
              onRequestClose={() => setShowCancelModal(false)}
            >
              <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
                <View style={styles.modalOverlay}>
                  <View style={styles.cancelModalContainer}>
                    {cancelReasonStep === 1 ? (
                      <View>
                        <View style={styles.modalHeader}>
                          <Text style={styles.modalTitle}>Cancel Booking</Text>
                          <Text style={styles.modalSubtitle}>Please let us know why you need to cancel this booking request.</Text>
                        </View>

                        <ScrollView style={styles.reasonsList} showsVerticalScrollIndicator={false}>
                          {cancellationReasons.map((reason) => {
                            const isSelected = selectedReason === reason;
                            return (
                              <TouchableOpacity
                                key={reason}
                                style={[
                                  styles.reasonItem,
                                  isSelected && styles.reasonItemSelected,
                                ]}
                                onPress={() => setSelectedReason(reason)}
                                activeOpacity={0.7}
                              >
                                <View style={styles.radioButton}>
                                  {isSelected && <View style={styles.radioButtonInner} />}
                                </View>
                                <Text style={[styles.reasonText, isSelected && styles.reasonTextSelected]}>
                                  {reason}
                                </Text>
                              </TouchableOpacity>
                            );
                          })}

                          {selectedReason === 'Other reason' && (
                            <TextInput
                              style={styles.customReasonInput}
                              placeholder="Please elaborate your reason..."
                              placeholderTextColor="#94A3B8"
                              value={otherReasonText}
                              onChangeText={setOtherReasonText}
                              multiline
                              numberOfLines={3}
                              maxLength={150}
                            />
                          )}
                        </ScrollView>

                        <View style={styles.modalFooter}>
                          <TouchableOpacity
                            style={[styles.modalBtn, styles.modalSecondaryBtn]}
                            onPress={() => setShowCancelModal(false)}
                            activeOpacity={0.7}
                          >
                            <Text style={styles.modalSecondaryBtnText}>Close</Text>
                          </TouchableOpacity>

                          <TouchableOpacity
                            style={[
                              styles.modalBtn,
                              styles.modalPrimaryBtn,
                              !selectedReason && styles.modalBtnDisabled
                            ]}
                            onPress={() => {
                              if (selectedReason) {
                                setCancelReasonStep(2);
                              }
                            }}
                            activeOpacity={0.7}
                            disabled={!selectedReason}
                          >
                            <Text style={styles.modalPrimaryBtnText}>Next</Text>
                          </TouchableOpacity>
                        </View>
                      </View>
                    ) : (
                      <View>
                        <View style={styles.modalHeader}>
                          <Text style={[styles.modalTitle, { color: '#EF4444' }]}>⚠️ Confirm Cancellation</Text>
                          <Text style={styles.modalSubtitle}>Are you absolutely sure you want to cancel this booking?</Text>
                        </View>

                        <View style={styles.warningContainer}>
                          <Text style={styles.warningTitle}>Cancellation Summary:</Text>
                          <View style={styles.warningDetailBox}>
                            <Text style={styles.warningLabel}>Service Type:</Text>
                            <Text style={styles.warningValue}>{currentBooking.serviceType}</Text>
                          </View>
                          <View style={styles.warningDetailBox}>
                            <Text style={styles.warningLabel}>Selected Reason:</Text>
                            <Text style={styles.warningValue} numberOfLines={2}>
                              {selectedReason === 'Other reason' ? otherReasonText || 'Other' : selectedReason}
                            </Text>
                          </View>
                          <Text style={styles.warningNoteText}>
                            * A cancellation fee may apply if the technician has already started traveling or arrived.
                          </Text>
                        </View>

                        <View style={styles.modalFooter}>
                          <TouchableOpacity
                            style={[styles.modalBtn, styles.modalSecondaryBtn]}
                            onPress={() => setCancelReasonStep(1)}
                            activeOpacity={0.7}
                          >
                            <Text style={styles.modalSecondaryBtnText}>← Back</Text>
                          </TouchableOpacity>

                          <TouchableOpacity
                            style={[styles.modalBtn, styles.modalDangerBtn]}
                            onPress={handleConfirmCancel}
                            activeOpacity={0.7}
                          >
                            <Text style={styles.modalDangerBtnText}>Yes, Cancel Booking</Text>
                          </TouchableOpacity>
                        </View>
                      </View>
                    )}
                  </View>
                </View>
              </TouchableWithoutFeedback>
            </Modal>

            <BookingChatDrawer
              isOpen={isChatOpen}
              onClose={() => setIsChatOpen(false)}
              bookingId={currentBooking.id}
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
  cancelBookingButton: {
    backgroundColor: '#FFF1F2',
    borderRadius: 14,
    height: 52,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 12,
    borderWidth: 1.5,
    borderColor: '#FECDD3',
  },
  cancelBookingButtonText: {
    color: '#E11D48',
    fontSize: 15,
    fontWeight: '700',
  },
  cancelledStatusCard: {
    backgroundColor: '#FEF2F2',
    borderRadius: 14,
    height: 56,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 20,
    borderWidth: 1,
    borderColor: '#FEE2E2',
  },
  cancelledStatusText: {
    color: '#EF4444',
    fontSize: 14,
    fontWeight: '700',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.6)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  cancelModalContainer: {
    backgroundColor: '#FFFFFF',
    borderRadius: 24,
    padding: 24,
    width: '100%',
    maxWidth: 380,
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.15,
    shadowRadius: 20,
    elevation: 8,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  modalHeader: {
    alignItems: 'center',
    marginBottom: 20,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#0F172A',
    marginBottom: 6,
  },
  modalSubtitle: {
    fontSize: 13.5,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 18,
  },
  reasonsList: {
    maxHeight: 250,
    marginBottom: 16,
  },
  reasonItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    padding: 12,
    marginBottom: 8,
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
  },
  reasonItemSelected: {
    borderColor: '#E11D48',
    backgroundColor: '#FFF1F2',
  },
  radioButton: {
    width: 18,
    height: 18,
    borderRadius: 9,
    borderWidth: 2,
    borderColor: '#CBD5E1',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  radioButtonInner: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#E11D48',
  },
  reasonText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#475569',
    flex: 1,
  },
  reasonTextSelected: {
    color: '#9F1239',
    fontWeight: '700',
  },
  customReasonInput: {
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    padding: 12,
    height: 80,
    textAlignVertical: 'top',
    fontSize: 13,
    color: '#0F172A',
    marginTop: 8,
    marginBottom: 16,
  },
  modalFooter: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 12,
  },
  modalBtn: {
    flex: 1,
    height: 48,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalSecondaryBtn: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#CBD5E1',
  },
  modalSecondaryBtnText: {
    color: '#475569',
    fontSize: 13.5,
    fontWeight: '600',
  },
  modalPrimaryBtn: {
    backgroundColor: '#E11D48',
  },
  modalPrimaryBtnText: {
    color: '#FFFFFF',
    fontSize: 13.5,
    fontWeight: '700',
  },
  modalDangerBtn: {
    backgroundColor: '#E11D48',
    shadowColor: '#E11D48',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 2,
  },
  modalDangerBtnText: {
    color: '#FFFFFF',
    fontSize: 13.5,
    fontWeight: '700',
  },
  modalBtnDisabled: {
    backgroundColor: '#E2E8F0',
    opacity: 0.5,
  },
  warningContainer: {
    backgroundColor: '#FFF1F2',
    borderRadius: 14,
    padding: 16,
    borderWidth: 1.5,
    borderColor: '#FECDD3',
    marginBottom: 16,
  },
  warningTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#9F1239',
    marginBottom: 10,
  },
  warningDetailBox: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  warningLabel: {
    fontSize: 12,
    color: '#BE123C',
    fontWeight: '600',
    width: '35%',
  },
  warningValue: {
    fontSize: 12,
    color: '#4C0519',
    fontWeight: '700',
    width: '65%',
    textAlign: 'right',
  },
  warningNoteText: {
    fontSize: 11,
    color: '#BE123C',
    fontStyle: 'italic',
    marginTop: 8,
    lineHeight: 15,
  },
  offlineBanner: {
    backgroundColor: '#D32F2F',
    paddingVertical: 8,
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
  },
  offlineText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '700',
  },
});
