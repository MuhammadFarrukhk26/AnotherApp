import React, { useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  RefreshControl,
  SafeAreaView,
  Animated,
  TextInput,
  Alert,
  Modal,
  ScrollView,
} from 'react-native';
import { useBookingStore } from '../state/bookingStore';
import { Card } from '../components/Card';
import { StatusBadge } from '../components/StatusBadge';
import { CategoryFilter } from '../components/CategoryFilter';
import { Booking } from '../../domain/models/Booking';
import { useTheme } from '../state/ThemeContext';

export interface Professional {
  id: string;
  name: string;
  title: string;
  category: string;
  categoryLabel: string;
  rating: number;
  jobsCount: number;
  ratePerHour: number;
  avatar: string;
  isAvailable: boolean;
  skills: string[];
}

export const PROFESSIONALS: Professional[] = [
  {
    id: 'worker_ayaan_sheikh',
    name: 'Ayaan Sheikh',
    title: 'Senior Plumber & Pipe Specialist',
    category: 'plumbing',
    categoryLabel: 'Plumbing',
    rating: 4.9,
    jobsCount: 186,
    ratePerHour: 800,
    avatar: '👨‍🔧',
    isAvailable: true,
    skills: ['Leak Detection', 'Pipe Repair', 'Drain Cleaning'],
  },
  {
    id: 'worker_kamran_khan',
    name: 'Kamran Khan',
    title: 'Master Gas & Pipe Fitter',
    category: 'plumbing',
    categoryLabel: 'Plumbing',
    rating: 4.8,
    jobsCount: 124,
    ratePerHour: 950,
    avatar: '👷‍♂️',
    isAvailable: true,
    skills: ['Water Heater', 'Gas Pipeline', 'Fixture Install'],
  },
  {
    id: 'worker_sajid_khan',
    name: 'Sajid Khan',
    title: 'Licensed Senior Electrician',
    category: 'electrical',
    categoryLabel: 'Electrical',
    rating: 4.9,
    jobsCount: 215,
    ratePerHour: 1000,
    avatar: '⚡',
    isAvailable: true,
    skills: ['Short Circuit Repair', 'House Wiring', 'DB Box Setup'],
  },
  {
    id: 'worker_faisal_mahmood',
    name: 'Faisal Mahmood',
    title: 'Appliance & Wiring Expert',
    category: 'electrical',
    categoryLabel: 'Electrical',
    rating: 4.7,
    jobsCount: 92,
    ratePerHour: 850,
    avatar: '⚙️',
    isAvailable: false,
    skills: ['Generator Install', 'UPS Setup', 'AC Wiring'],
  },
  {
    id: 'worker_zainab_bibi',
    name: 'Zainab Bibi',
    title: 'Elite Home Cleaning Expert',
    category: 'cleaning',
    categoryLabel: 'Cleaning',
    rating: 4.9,
    jobsCount: 320,
    ratePerHour: 700,
    avatar: '🧹',
    isAvailable: true,
    skills: ['Full House Deep Clean', 'Kitchen Sanitization', 'Sofa Washing'],
  },
  {
    id: 'worker_tahira_batool',
    name: 'Tahira Batool',
    title: 'Professional Maid & Organizer',
    category: 'cleaning',
    categoryLabel: 'Cleaning',
    rating: 4.8,
    jobsCount: 154,
    ratePerHour: 750,
    avatar: '👩‍💼',
    isAvailable: true,
    skills: ['Deep Carpet Clean', 'Window Washing', 'Closet Organize'],
  },
  {
    id: 'worker_imran_siddiqui',
    name: 'Imran Siddiqui',
    title: 'HVAC Air Conditioning Specialist',
    category: 'ac',
    categoryLabel: 'AC & Cooling',
    rating: 4.8,
    jobsCount: 142,
    ratePerHour: 1200,
    avatar: '❄️',
    isAvailable: true,
    skills: ['AC Gas Top-up', 'Inverter PCB Repair', 'Split AC Service'],
  },
  {
    id: 'worker_waseem_ahmed',
    name: 'Waseem Ahmed',
    title: 'Master Wall Artist & Painter',
    category: 'painting',
    categoryLabel: 'Painting',
    rating: 4.9,
    jobsCount: 98,
    ratePerHour: 900,
    avatar: '🎨',
    isAvailable: true,
    skills: ['Wall Putty', 'Texture Painting', 'Exterior Weather Sheet'],
  },
  {
    id: 'worker_bilal_wood',
    name: 'Bilal Woodworks',
    title: 'Senior Finished Carpenter',
    category: 'carpentry',
    categoryLabel: 'Carpentry',
    rating: 4.7,
    jobsCount: 85,
    ratePerHour: 1100,
    avatar: '🪚',
    isAvailable: true,
    skills: ['Door Lock Repair', 'Cabinet Installation', 'Sofa Polish'],
  },
  {
    id: 'worker_jamil_repairs',
    name: 'Jamil Repairs',
    title: 'Senior Home Appliance Mechanic',
    category: 'repair',
    categoryLabel: 'Repair',
    rating: 4.8,
    jobsCount: 167,
    ratePerHour: 800,
    avatar: '🔧',
    isAvailable: true,
    skills: ['Washing Machine Fix', 'Refrigerator Gas', 'Microwave Repair'],
  },
  {
    id: 'worker_express_movers',
    name: 'Express Movers',
    title: 'Professional Packer & Mover',
    category: 'moving',
    categoryLabel: 'Moving',
    rating: 4.6,
    jobsCount: 112,
    ratePerHour: 1500,
    avatar: '📦',
    isAvailable: true,
    skills: ['Furniture Disassembly', 'Fragile Item Wrap', 'Office Shifting'],
  },
];

const BookingCardSkeleton: React.FC = () => {
  const pulseAnim = useRef(new Animated.Value(0.3)).current;
  const { colors } = useTheme();

  useEffect(() => {
    const pulse = Animated.loop(
      Animated.sequence([
        Animated.timing(pulseAnim, {
          toValue: 0.8,
          duration: 1000,
          useNativeDriver: true,
        }),
        Animated.timing(pulseAnim, {
          toValue: 0.3,
          duration: 1000,
          useNativeDriver: true,
        }),
      ])
    );
    pulse.start();
    return () => pulse.stop();
  }, [pulseAnim]);

  return (
    <Card style={styles.bookingCard}>
      <View style={styles.cardHeader}>
        <View style={styles.serviceInfo}>
          {/* Icon Container Placeholder */}
          <Animated.View style={[styles.skeletonIcon, { opacity: pulseAnim, backgroundColor: colors.border }]} />
          
          <View style={{ gap: 6 }}>
            {/* Title Placeholder */}
            <Animated.View style={[styles.skeletonTitle, { opacity: pulseAnim, backgroundColor: colors.border }]} />
            {/* Subtitle/ID Placeholder */}
            <Animated.View style={[styles.skeletonSubtitle, { opacity: pulseAnim, backgroundColor: colors.border }]} />
          </View>
        </View>

        {/* Status Badge Placeholder */}
        <Animated.View style={[styles.skeletonBadge, { opacity: pulseAnim, backgroundColor: colors.border }]} />
      </View>

      {/* Address Placeholder */}
      <Animated.View style={[styles.skeletonAddress, { opacity: pulseAnim, backgroundColor: colors.border }]} />

      <View style={[styles.cardDivider, { backgroundColor: colors.border }]} />

      <View style={styles.cardFooter}>
        <View style={{ gap: 6 }}>
          {/* Label Placeholder */}
          <Animated.View style={[styles.skeletonMetaLabel, { opacity: pulseAnim, backgroundColor: colors.border }]} />
          {/* Value Placeholder */}
          <Animated.View style={[styles.skeletonMetaValue, { opacity: pulseAnim, backgroundColor: colors.border }]} />
        </View>

        {/* Price Placeholder */}
        <Animated.View style={[styles.skeletonPrice, { opacity: pulseAnim, backgroundColor: colors.border }]} />
      </View>
    </Card>
  );
};

interface BookingListScreenProps {
  customerId?: string;
  onSelectBooking: (bookingId: string) => void;
}

export const BookingListScreen: React.FC<BookingListScreenProps> = ({
  customerId = 'cust_mock_react_native',
  onSelectBooking,
}) => {
  const { bookings, loading, error, fetchCustomerBookings, scheduleBooking } = useBookingStore();
  const [activeTab, setActiveTab] = useState<'discover' | 'pending' | 'past'>('discover');
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [showOnlyAvailable, setShowOnlyAvailable] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const { colors, isDark, toggleTheme } = useTheme();

  // Hiring modal states
  const [selectedProfessional, setSelectedProfessional] = useState<Professional | null>(null);
  const [isHiringModalVisible, setIsHiringModalVisible] = useState(false);
  const [taskDescription, setTaskDescription] = useState('');
  const [selectedTimeSlot, setSelectedTimeSlot] = useState<'asap' | 'evening' | 'tomorrow_morning' | 'tomorrow_afternoon'>('asap');
  const [serviceAddress, setServiceAddress] = useState('House 12-B, Sector F-6, Islamabad');
  const [isHiringLoading, setIsHiringLoading] = useState(false);
  const [estimatedDuration, setEstimatedDuration] = useState<number>(2);

  useEffect(() => {
    fetchCustomerBookings(customerId);
  }, [customerId]);

  const onRefresh = async () => {
    setRefreshing(true);
    try {
      await fetchCustomerBookings(customerId);
    } catch (e) {
      console.error('[BookingListScreen] Failed to refresh bookings:', e);
    } finally {
      setRefreshing(false);
    }
  };

  const handleConfirmHiring = async () => {
    if (!selectedProfessional) return;
    if (!taskDescription.trim()) {
      Alert.alert('Task Description Required', 'Please enter a description of the task so the technician knows what to expect.');
      return;
    }
    if (!serviceAddress.trim()) {
      Alert.alert('Address Required', 'Please provide the service location address.');
      return;
    }

    // Determine scheduled time ISO string
    let isoTime = new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString();
    if (selectedTimeSlot === 'evening') {
      const eveningDate = new Date();
      eveningDate.setHours(18, 0, 0, 0);
      isoTime = eveningDate.toISOString();
    } else if (selectedTimeSlot === 'tomorrow_morning') {
      const tomorrowMorning = new Date(Date.now() + 24 * 60 * 60 * 1000);
      tomorrowMorning.setHours(10, 0, 0, 0);
      isoTime = tomorrowMorning.toISOString();
    } else if (selectedTimeSlot === 'tomorrow_afternoon') {
      const tomorrowAfternoon = new Date(Date.now() + 24 * 60 * 60 * 1000);
      tomorrowAfternoon.setHours(15, 0, 0, 0);
      isoTime = tomorrowAfternoon.toISOString();
    }

    // Estimate price: ratePerHour * estimatedDuration + safety fee (150) - discount (200)
    const basePrice = selectedProfessional.ratePerHour * estimatedDuration;
    const finalPrice = Math.max(500, basePrice + 150 - 200);

    try {
      setIsHiringLoading(true);
      
      // 1. Create the booking request on the server
      const newBooking = await scheduleBooking({
        serviceType: selectedProfessional.category,
        scheduledTime: isoTime,
        address: serviceAddress,
        price: finalPrice,
        latitude: 33.6844, // Default Islamabad coordinates
        longitude: 73.0479,
      });

      // 2. Automatically transition booking to ACCEPTED by this professional
      await useBookingStore.getState().updateBookingStatus(
        newBooking.id,
        'ACCEPTED',
        selectedProfessional.id
      );

      setIsHiringLoading(false);
      setIsHiringModalVisible(false);

      // Reset fields
      setTaskDescription('');
      setSelectedTimeSlot('asap');

      // Show beautiful success popup
      Alert.alert(
        'Hiring Request Confirmed! 🎉',
        `JazakAllah! ${selectedProfessional.name} has accepted your hire request.\n\nThey are preparing their tools and will be at your location at ${new Date(isoTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}.`,
        [
          {
            text: 'Track Progress',
            onPress: () => {
              // Switch tab to active pending bookings
              setActiveTab('pending');
              fetchCustomerBookings(customerId);
            }
          }
        ]
      );
    } catch (err: any) {
      setIsHiringLoading(false);
      Alert.alert('Hiring Failed', err.message || 'Something went wrong while hiring the professional. Please try again.');
    }
  };

  const handleQuickReorder = async (booking: Booking, event: any) => {
    if (event && typeof event.stopPropagation === 'function') {
      event.stopPropagation();
    }

    Alert.alert(
      'Quick Reorder',
      `Would you like to rebook ${getServiceTitle(booking.serviceType)} with the same address and details?`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Confirm',
          onPress: async () => {
            try {
              // 2 hours from now
              const futureTime = new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString();
              await scheduleBooking({
                serviceType: booking.serviceType,
                address: booking.address,
                price: booking.price,
                latitude: booking.latitude || 33.6844,
                longitude: booking.longitude || 73.0479,
                scheduledTime: futureTime,
              });
              Alert.alert('Success', 'Your booking request has been successfully created!');
              fetchCustomerBookings(customerId);
            } catch (err: any) {
              Alert.alert('Reorder Failed', err.message || 'Failed to clone booking');
            }
          }
        }
      ]
    );
  };

  const matchesCategory = (serviceType: string, categoryId: string): boolean => {
    if (categoryId === 'all') return true;
    const normalized = serviceType.toLowerCase();
    switch (categoryId) {
      case 'plumbing':
        return normalized.includes('plumb') || normalized.includes('pipe');
      case 'cleaning':
        return normalized.includes('clean') || normalized.includes('maid');
      case 'electrical':
        return normalized.includes('electric');
      case 'ac':
        return normalized.includes('ac') || normalized.includes('condition');
      case 'painting':
        return normalized.includes('paint');
      case 'carpentry':
        return normalized.includes('carpent') || normalized.includes('wood');
      case 'repair':
        return normalized.includes('mechanic') || normalized.includes('car') || normalized.includes('bike') || normalized.includes('repair') || normalized.includes('fix');
      case 'moving':
        return normalized.includes('mov') || normalized.includes('pack');
      default:
        return false;
    }
  };

  // Filter professionals based on search and category
  const filteredProfessionals = PROFESSIONALS.filter((prof) => {
    const matchesCat = selectedCategoryId === 'all' || prof.category === selectedCategoryId;
    if (!matchesCat) return false;

    if (showOnlyAvailable && !prof.isAvailable) return false;

    if (!searchQuery.trim()) return true;
    const query = searchQuery.toLowerCase().trim();
    return (
      prof.name.toLowerCase().includes(query) ||
      prof.title.toLowerCase().includes(query) ||
      prof.categoryLabel.toLowerCase().includes(query) ||
      prof.skills.some(skill => skill.toLowerCase().includes(query))
    );
  });

  // Categorize bookings with updated enroute/arrived/started status values
  const pendingBookings = bookings.filter(
    (b) => b.status === 'PENDING' || b.status === 'ACCEPTED' || b.status === 'EN_ROUTE' || b.status === 'ARRIVED' || b.status === 'IN_PROGRESS' || b.status === 'STARTED'
  );
  
  const pastBookings = bookings.filter(
    (b) => b.status === 'COMPLETED' || b.status === 'CANCELLED'
  );

  const displayedBookings = (activeTab === 'pending' ? pendingBookings : pastBookings).filter(
    (b) => {
      const matchesCat = matchesCategory(b.serviceType, selectedCategoryId);
      if (!matchesCat) return false;

      if (!searchQuery.trim()) return true;

      const query = searchQuery.toLowerCase().trim();
      const serviceTitle = getServiceTitle(b.serviceType).toLowerCase();
      const serviceTypeRaw = b.serviceType.toLowerCase();
      const address = b.address.toLowerCase();
      const bookingId = b.id.toLowerCase();
      
      const providerName = b.workerId ? "ayaan sheikh" : "";

      return (
        serviceTitle.includes(query) ||
        serviceTypeRaw.includes(query) ||
        address.includes(query) ||
        bookingId.includes(query) ||
        (providerName && providerName.includes(query))
      );
    }
  );

  const getServiceIcon = (type: string): string => {
    const normalized = type.toLowerCase();
    if (normalized.includes('electric')) return '⚡';
    if (normalized.includes('plumb') || normalized.includes('pipe')) return '🚰';
    if (normalized.includes('ac') || normalized.includes('condition')) return '❄️';
    if (normalized.includes('clean') || normalized.includes('maid')) return '🧹';
    if (normalized.includes('paint')) return '🎨';
    if (normalized.includes('carpent') || normalized.includes('wood')) return '🪚';
    if (normalized.includes('mechanic') || normalized.includes('car') || normalized.includes('bike')) return '🔧';
    if (normalized.includes('mov') || normalized.includes('pack')) return '📦';
    return '🛠️';
  };

  const getServiceTitle = (type: string): string => {
    return type
      .split('_')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  };

  const renderBookingItem = ({ item }: { item: Booking }) => {
    return (
      <TouchableOpacity
        activeOpacity={0.7}
        onPress={() => onSelectBooking(item.id)}
        testID={`booking_item_${item.id}`}
      >
        <Card style={styles.bookingCard}>
          <View style={styles.cardHeader}>
            <View style={styles.serviceInfo}>
              <View style={[styles.iconContainer, { backgroundColor: isDark ? '#2E2D56' : '#ECFDF5' }]}>
                <Text style={styles.serviceIcon}>{getServiceIcon(item.serviceType)}</Text>
              </View>
              <View>
                <Text style={[styles.serviceName, { color: colors.text }]}>{getServiceTitle(item.serviceType)}</Text>
                <Text style={[styles.bookingId, { color: colors.textMuted }]}>Booking #{item.id}</Text>
              </View>
            </View>
            <StatusBadge status={item.status} />
          </View>

          <Text style={[styles.addressText, { color: colors.textSecondary }]} numberOfLines={1}>
            📍 {item.address}
          </Text>

          <View style={[styles.cardDivider, { backgroundColor: colors.border }]} />

          <View style={styles.cardFooter}>
            <View style={{ flex: 1, marginRight: 8 }}>
              <Text style={styles.metaLabel}>Scheduled For</Text>
              <Text style={[styles.metaValue, { color: colors.textSecondary }]} numberOfLines={1}>{item.scheduledTime}</Text>
            </View>
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <View style={[styles.priceContainer, { backgroundColor: isDark ? '#115E59' : '#F0FDF4', marginRight: (item.status === 'COMPLETED' || item.status === 'CANCELLED') ? 8 : 0 }]}>
                <Text style={[styles.priceText, { color: isDark ? '#2DD4BF' : '#10B981' }]}>{item.formattedPrice}</Text>
              </View>
              {(item.status === 'COMPLETED' || item.status === 'CANCELLED') && (
                <TouchableOpacity
                  style={[styles.reorderButton, { backgroundColor: colors.primary }]}
                  onPress={(e) => handleQuickReorder(item, e)}
                  testID={`reorder_button_${item.id}`}
                  accessibilityLabel="Quick Reorder"
                  accessibilityRole="button"
                  hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
                >
                  <Text style={styles.reorderButtonText}>🔄</Text>
                </TouchableOpacity>
              )}
            </View>
          </View>
        </Card>
      </TouchableOpacity>
    );
  };

  const renderEmptyState = () => {
    if (loading) return null;

    const isPending = activeTab === 'pending';
    const isFiltered = selectedCategoryId !== 'all';
    const isSearched = searchQuery.trim() !== '';
    
    const title = (isFiltered || isSearched)
      ? 'No Matches Found' 
      : (isPending ? 'No Active Bookings' : 'No Past Bookings');
      
    let message = '';
    if (isSearched && isFiltered) {
      message = `No bookings match your search "${searchQuery}" in the ${selectedCategoryId} category.`;
    } else if (isSearched) {
      message = `No bookings match your search "${searchQuery}".`;
    } else if (isFiltered) {
      message = `You have no ${selectedCategoryId} bookings in this list. Try clearing the filter.`;
    } else {
      message = isPending
        ? 'You do not have any bookings currently pending or in progress.'
        : 'You have no historical or cancelled bookings.';
    }

    return (
      <View style={styles.emptyContainer}>
        <Text style={styles.emptyIcon}>{(isFiltered || isSearched) ? '🔍' : (isPending ? '📅' : '📂')}</Text>
        <Text style={[styles.emptyTitle, { color: colors.text }]}>{title}</Text>
        <Text style={[styles.emptyMessage, { color: colors.textMuted }]}>{message}</Text>
        <TouchableOpacity 
          style={styles.retryButton} 
          onPress={() => {
            if (isFiltered || isSearched) {
              setSelectedCategoryId('all');
              setSearchQuery('');
            } else {
              onRefresh();
            }
          }}
        >
          <Text style={styles.retryButtonText}>{(isFiltered || isSearched) ? 'Clear Filters' : 'Refresh'}</Text>
        </TouchableOpacity>
      </View>
    );
  };

  const renderProfessionalItem = ({ item }: { item: Professional }) => {
    return (
      <Card style={[styles.bookingCard, styles.proCard, { borderColor: colors.border }]}>
        <View style={styles.cardHeader}>
          <View style={styles.serviceInfo}>
            <View style={[styles.proAvatarContainer, { backgroundColor: isDark ? '#1E293B' : '#F1F5F9' }]}>
              <Text style={styles.proAvatarText}>{item.avatar}</Text>
            </View>
            <View style={{ flex: 1, paddingRight: 8 }}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
                <Text style={[styles.serviceName, { color: colors.text }]} numberOfLines={1}>
                  {item.name}
                </Text>
                <Text style={styles.verifiedBadge}>✔️</Text>
              </View>
              <Text style={[styles.bookingId, { color: colors.textMuted }]} numberOfLines={1}>
                {item.title}
              </Text>
            </View>
          </View>
          <View style={[styles.proStatusBadge, { backgroundColor: item.isAvailable ? '#DCFCE7' : '#FEE2E2' }]}>
            <View style={[styles.proStatusDot, { backgroundColor: item.isAvailable ? '#22C55E' : '#EF4444' }]} />
            <Text style={[styles.proStatusText, { color: item.isAvailable ? '#15803D' : '#B91C1C' }]}>
              {item.isAvailable ? 'Available' : 'Busy'}
            </Text>
          </View>
        </View>

        <View style={styles.proMetaRow}>
          <View style={styles.proRatingContainer}>
            <Text style={styles.proRatingStar}>⭐</Text>
            <Text style={[styles.proRatingText, { color: colors.text }]}>{item.rating}</Text>
            <Text style={[styles.proJobsText, { color: colors.textMuted }]}>({item.jobsCount} jobs)</Text>
          </View>
          <Text style={[styles.proRateText, { color: isDark ? '#2DD4BF' : '#0D9488' }]}>
            PKR {item.ratePerHour}/hr
          </Text>
        </View>

        <View style={styles.skillsContainer}>
          {item.skills.map((skill, index) => (
            <View key={index} style={[styles.skillChip, { backgroundColor: isDark ? '#334155' : '#F8FAFC', borderColor: colors.border }]}>
              <Text style={[styles.skillChipText, { color: colors.textSecondary }]}>{skill}</Text>
            </View>
          ))}
        </View>

        <View style={[styles.cardDivider, { backgroundColor: colors.border }]} />

        <TouchableOpacity
          style={[styles.hireButton, { backgroundColor: item.isAvailable ? colors.primary : '#94A3B8' }]}
          disabled={!item.isAvailable}
          onPress={() => {
            setSelectedProfessional(item);
            setIsHiringModalVisible(true);
          }}
          testID={`hire_button_${item.id}`}
        >
          <Text style={styles.hireButtonText}>
            {item.isAvailable ? '⚡ Instant Hire' : 'Unavailable'}
          </Text>
        </TouchableOpacity>
      </Card>
    );
  };

  const renderProEmptyState = () => {
    return (
      <View style={styles.emptyContainer}>
        <Text style={styles.emptyIcon}>👷‍♂️</Text>
        <Text style={[styles.emptyTitle, { color: colors.text }]}>No Professionals Found</Text>
        <Text style={[styles.emptyMessage, { color: colors.textMuted }]}>
          No experts match "{searchQuery}" in this category. Try adjusting your filters or search terms.
        </Text>
        <TouchableOpacity
          style={styles.retryButton}
          onPress={() => {
            setSelectedCategoryId('all');
            setSearchQuery('');
          }}
        >
          <Text style={styles.retryButtonText}>Clear Filters</Text>
        </TouchableOpacity>
      </View>
    );
  };

  const getQuickTemplates = (category: string) => {
    switch (category) {
      case 'plumbing':
        return ['Water pipe leakage', 'Bathroom fixture install', 'Unblock kitchen drain', 'Water pump repair'];
      case 'electrical':
        return ['Short circuit troubleshooting', 'Ceiling fan mounting', 'House DB box checkup', 'Socket installation'];
      case 'cleaning':
        return ['Full house deep clean', 'Kitchen sanitization', 'Sofa & carpet washing', 'Maid day cleaning'];
      case 'ac':
        return ['Split AC filter wash', 'AC gas charging', 'Inverter circuit fixing', 'AC install/uninstall'];
      case 'painting':
        return ['Room wall putty & paint', 'Wall touchup work', 'Doors polish/paint', 'Texture wall painting'];
      case 'carpentry':
        return ['Wooden door lock fix', 'Kitchen cabinet hinge', 'Sofa handle repair', 'New wooden shelf setup'];
      case 'repair':
        return ['Washing machine fix', 'Water dispenser repair', 'Refrigerator gas refill', 'Microwave repair'];
      case 'moving':
        return ['Single room shifting', 'Complete apartment shift', 'Fragile items packing', 'Furniture loading'];
      default:
        return ['General handyman work', 'Inspection & diagnosis', 'Emergency fix'];
    }
  };

  return (
    <SafeAreaView style={[styles.screenContainer, { backgroundColor: colors.background }]}>
      <View style={styles.header}>
        <View style={styles.headerTopRow}>
          <Text style={[styles.headerTitle, { color: colors.text }]}>Hazir Bookings</Text>
          <TouchableOpacity
            style={[styles.themeToggleBtn, { backgroundColor: colors.border }]}
            onPress={toggleTheme}
            activeOpacity={0.8}
            testID="theme_toggle_list"
          >
            <Text style={styles.themeToggleBtnText}>{isDark ? '☀️' : '🌙'}</Text>
          </TouchableOpacity>
        </View>
        <Text style={[styles.headerSubtitle, { color: colors.textMuted }]}>Manage your home & office services</Text>
      </View>

      {/* Tabs */}
      <View style={[styles.tabContainer, { backgroundColor: colors.border }]}>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'discover' && [styles.activeTab, { backgroundColor: colors.card }]]}
          onPress={() => setActiveTab('discover')}
          testID="tab_discover"
        >
          <Text style={[styles.tabText, activeTab === 'discover' ? [styles.activeTabText, { color: colors.text }] : { color: colors.textMuted }]}>
            🔍 Discover
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'pending' && [styles.activeTab, { backgroundColor: colors.card }]]}
          onPress={() => setActiveTab('pending')}
          testID="tab_pending"
        >
          <Text style={[styles.tabText, activeTab === 'pending' ? [styles.activeTabText, { color: colors.text }] : { color: colors.textMuted }]}>
            ⏳ Active ({pendingBookings.length})
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'past' && [styles.activeTab, { backgroundColor: colors.card }]]}
          onPress={() => setActiveTab('past')}
          testID="tab_past"
        >
          <Text style={[styles.tabText, activeTab === 'past' ? [styles.activeTabText, { color: colors.text }] : { color: colors.textMuted }]}>
            📂 Past ({pastBookings.length})
          </Text>
        </TouchableOpacity>
      </View>

      {/* Search Bar */}
      <View style={styles.searchBarContainer}>
        <View style={[styles.searchInner, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Text style={styles.searchIcon}>🔍</Text>
          <TextInput
            style={[styles.searchInput, { color: colors.text }]}
            placeholder={activeTab === 'discover' ? "Search plumbers, electricians, cleaners..." : "Search provider, service, or address..."}
            placeholderTextColor={isDark ? '#64748B' : '#94A3B8'}
            value={searchQuery}
            onChangeText={setSearchQuery}
            autoCapitalize="none"
            autoCorrect={false}
            testID="booking_search_input"
          />
          {searchQuery.length > 0 && (
            <TouchableOpacity
              onPress={() => setSearchQuery('')}
              style={styles.clearButton}
              testID="clear_search_button"
            >
              <Text style={styles.clearButtonText}>✕</Text>
            </TouchableOpacity>
          )}
        </View>
      </View>

      {/* Category Filter */}
      <CategoryFilter
        selectedCategoryId={selectedCategoryId}
        onSelectCategory={setSelectedCategoryId}
      />

      {/* Available Now Toggle (only in Discover tab) */}
      {activeTab === 'discover' && (
        <View style={[styles.availabilityToggleContainer, { borderBottomColor: colors.border, backgroundColor: colors.card }]}>
          <View style={styles.availabilityToggleLeft}>
            <Text style={styles.availabilityToggleIcon}>🟢</Text>
            <View>
              <Text style={[styles.availabilityToggleTitle, { color: colors.text }]}>Available Now Only</Text>
              <Text style={[styles.availabilityToggleSubtitle, { color: colors.textMuted }]}>Show active professionals ready to work</Text>
            </View>
          </View>
          <TouchableOpacity
            style={[
              styles.switchTrack,
              { backgroundColor: showOnlyAvailable ? '#10B981' : (isDark ? '#334155' : '#E2E8F0') }
            ]}
            onPress={() => setShowOnlyAvailable(!showOnlyAvailable)}
            activeOpacity={0.8}
            testID="available_now_toggle"
          >
            <View style={[
              styles.switchThumb,
              { 
                transform: [{ translateX: showOnlyAvailable ? 20 : 0 }],
                backgroundColor: '#FFFFFF' 
              }
            ]} />
          </TouchableOpacity>
        </View>
      )}

      {activeTab === 'discover' ? (
        <FlatList
          data={filteredProfessionals}
          renderItem={renderProfessionalItem}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.listContent}
          ListEmptyComponent={renderProEmptyState}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={onRefresh}
              colors={['#10B981']}
              tintColor="#10B981"
            />
          }
        />
      ) : error ? (
        <View style={styles.errorContainer}>
          <Text style={styles.errorIcon}>⚠️</Text>
          <Text style={styles.errorText}>{error}</Text>
          <TouchableOpacity style={styles.retryButton} onPress={onRefresh}>
            <Text style={styles.retryButtonText}>Try Again</Text>
          </TouchableOpacity>
        </View>
      ) : loading && bookings.length === 0 ? (
        <View style={styles.loadingListContainer}>
          <BookingCardSkeleton />
          <BookingCardSkeleton />
          <BookingCardSkeleton />
        </View>
      ) : (
        <FlatList
          data={displayedBookings}
          renderItem={renderBookingItem}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.listContent}
          ListEmptyComponent={renderEmptyState}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={onRefresh}
              colors={['#10B981']}
              tintColor="#10B981"
            />
          }
        />
      )}

      {/* Hiring Modal */}
      {selectedProfessional && (
        <Modal
          visible={isHiringModalVisible}
          animationType="slide"
          transparent={true}
          onRequestClose={() => setIsHiringModalVisible(false)}
        >
          <View style={styles.modalOverlay}>
            <View style={[styles.modalContent, { backgroundColor: colors.background }]}>
              {/* Header */}
              <View style={[styles.modalHeader, { borderBottomColor: colors.border }]}>
                <Text style={[styles.modalHeaderTitle, { color: colors.text }]}>Secure Professional Hiring</Text>
                <TouchableOpacity
                  onPress={() => setIsHiringModalVisible(false)}
                  style={styles.modalCloseBtn}
                >
                  <Text style={[styles.modalCloseBtnText, { color: colors.textSecondary }]}>✕</Text>
                </TouchableOpacity>
              </View>

              <ScrollView contentContainerStyle={styles.modalScroll}>
                {/* Pro profile recap */}
                <View style={[styles.modalProRecap, { backgroundColor: isDark ? '#1E293B' : '#F8FAFC' }]}>
                  <Text style={styles.modalProAvatar}>{selectedProfessional.avatar}</Text>
                  <View style={{ flex: 1 }}>
                    <Text style={[styles.modalProName, { color: colors.text }]}>{selectedProfessional.name}</Text>
                    <Text style={[styles.modalProTitle, { color: colors.textMuted }]}>{selectedProfessional.title}</Text>
                    <Text style={[styles.modalProRate, { color: colors.primary }]}>
                      PKR {selectedProfessional.ratePerHour}/hr • ⭐ {selectedProfessional.rating}
                    </Text>
                  </View>
                </View>

                {/* Input 1: Address */}
                <Text style={[styles.modalInputLabel, { color: colors.textSecondary }]}>Service Address</Text>
                <TextInput
                  style={[styles.modalTextInput, { color: colors.text, borderColor: colors.border, backgroundColor: colors.card }]}
                  value={serviceAddress}
                  onChangeText={setServiceAddress}
                  placeholder="Enter your address..."
                  placeholderTextColor="#94A3B8"
                />

                {/* Input 2: Date & Time slot */}
                <Text style={[styles.modalInputLabel, { color: colors.textSecondary }]}>Scheduled Time Slot</Text>
                <View style={styles.timeSlotRow}>
                  <TouchableOpacity
                    style={[
                      styles.timeSlotChip,
                      selectedTimeSlot === 'asap' 
                        ? { backgroundColor: colors.primary, borderColor: colors.primary }
                        : { backgroundColor: colors.card, borderColor: colors.border }
                    ]}
                    onPress={() => setSelectedTimeSlot('asap')}
                  >
                    <Text style={[styles.timeSlotText, selectedTimeSlot === 'asap' ? styles.activeTimeSlotText : { color: colors.textSecondary }]}>
                      ⚡ ASAP (2 hrs)
                    </Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    style={[
                      styles.timeSlotChip,
                      selectedTimeSlot === 'evening' 
                        ? { backgroundColor: colors.primary, borderColor: colors.primary }
                        : { backgroundColor: colors.card, borderColor: colors.border }
                    ]}
                    onPress={() => setSelectedTimeSlot('evening')}
                  >
                    <Text style={[styles.timeSlotText, selectedTimeSlot === 'evening' ? styles.activeTimeSlotText : { color: colors.textSecondary }]}>
                      🌇 Evening (6 PM)
                    </Text>
                  </TouchableOpacity>
                </View>
                <View style={[styles.timeSlotRow, { marginTop: 8 }]}>
                  <TouchableOpacity
                    style={[
                      styles.timeSlotChip,
                      selectedTimeSlot === 'tomorrow_morning' 
                        ? { backgroundColor: colors.primary, borderColor: colors.primary }
                        : { backgroundColor: colors.card, borderColor: colors.border }
                    ]}
                    onPress={() => setSelectedTimeSlot('tomorrow_morning')}
                  >
                    <Text style={[styles.timeSlotText, selectedTimeSlot === 'tomorrow_morning' ? styles.activeTimeSlotText : { color: colors.textSecondary }]}>
                      🌅 Tomorrow (10 AM)
                    </Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    style={[
                      styles.timeSlotChip,
                      selectedTimeSlot === 'tomorrow_afternoon' 
                        ? { backgroundColor: colors.primary, borderColor: colors.primary }
                        : { backgroundColor: colors.card, borderColor: colors.border }
                    ]}
                    onPress={() => setSelectedTimeSlot('tomorrow_afternoon')}
                  >
                    <Text style={[styles.timeSlotText, selectedTimeSlot === 'tomorrow_afternoon' ? styles.activeTimeSlotText : { color: colors.textSecondary }]}>
                      ☀️ Tomorrow (3 PM)
                    </Text>
                  </TouchableOpacity>
                </View>

                {/* Input 3: Task Description */}
                <Text style={[styles.modalInputLabel, { color: colors.textSecondary }]}>Describe your task</Text>
                <TextInput
                  style={[styles.modalTextInput, styles.modalTextArea, { color: colors.text, borderColor: colors.border, backgroundColor: colors.card }]}
                  value={taskDescription}
                  onChangeText={setTaskDescription}
                  placeholder="E.g. My sink drain is completely clogged and water is leaking..."
                  placeholderTextColor="#94A3B8"
                  multiline={true}
                  numberOfLines={4}
                />

                {/* Quick Task Tags */}
                <Text style={[styles.modalQuickTagsLabel, { color: colors.textMuted }]}>Quick Templates (Tap to add):</Text>
                <View style={styles.quickTagsContainer}>
                  {getQuickTemplates(selectedProfessional.category).map((tag, index) => (
                    <TouchableOpacity
                      key={index}
                      style={[styles.quickTagChip, { backgroundColor: isDark ? '#334155' : '#F1F5F9', borderColor: colors.border }]}
                      onPress={() => setTaskDescription(tag)}
                    >
                      <Text style={[styles.quickTagText, { color: colors.textSecondary }]}>+ {tag}</Text>
                    </TouchableOpacity>
                  ))}
                </View>

                {/* AUTOMATED PRICE ESTIMATOR TOOL */}
                <Text style={[styles.modalInputLabel, { color: colors.textSecondary }]}>⏱️ Automated Price Estimator</Text>
                <View style={[styles.estimatorContainer, { borderColor: colors.border, backgroundColor: isDark ? '#1E293B' : '#F8FAFC' }]}>
                  <Text style={[styles.estimatorSubtitle, { color: colors.textMuted }]}>
                    Adjust the estimated duration to preview your total budget range:
                  </Text>
                  
                  {/* Plus/Minus Adjuster */}
                  <View style={styles.estimatorAdjustRow}>
                    <TouchableOpacity 
                      style={[styles.durationAdjustBtn, { backgroundColor: colors.border }]}
                      onPress={() => setEstimatedDuration(prev => Math.max(0.5, prev - 0.5))}
                      testID="duration_minus_btn"
                    >
                      <Text style={[styles.durationAdjustText, { color: colors.text }]}>−</Text>
                    </TouchableOpacity>
                    <View style={{ alignItems: 'center' }}>
                      <Text style={[styles.durationValue, { color: colors.text }]}>
                        {estimatedDuration} {estimatedDuration === 1 ? 'Hour' : 'Hours'}
                      </Text>
                      <Text style={[styles.durationLabel, { color: colors.textMuted }]}>Estimated Time</Text>
                    </View>
                    <TouchableOpacity 
                      style={[styles.durationAdjustBtn, { backgroundColor: colors.border }]}
                      onPress={() => setEstimatedDuration(prev => Math.min(12, prev + 0.5))}
                      testID="duration_plus_btn"
                    >
                      <Text style={[styles.durationAdjustText, { color: colors.text }]}>+</Text>
                    </TouchableOpacity>
                  </View>

                  {/* Recommendations */}
                  <Text style={[styles.modalQuickTagsLabel, { color: colors.textMuted, marginTop: 8, marginBottom: 6 }]}>
                    Suggested Durations:
                  </Text>
                  <View style={styles.estimatorPresetsRow}>
                    <TouchableOpacity 
                      style={[styles.presetChip, estimatedDuration === 1 ? { backgroundColor: colors.primary, borderColor: colors.primary } : { borderColor: colors.border }]}
                      onPress={() => setEstimatedDuration(1)}
                    >
                      <Text style={[styles.presetChipText, estimatedDuration === 1 ? { color: '#FFFFFF' } : { color: colors.textSecondary }]}>
                        ⚡ Quick Fix (1h)
                      </Text>
                    </TouchableOpacity>
                    <TouchableOpacity 
                      style={[styles.presetChip, estimatedDuration === 2 ? { backgroundColor: colors.primary, borderColor: colors.primary } : { borderColor: colors.border }]}
                      onPress={() => setEstimatedDuration(2)}
                    >
                      <Text style={[styles.presetChipText, estimatedDuration === 2 ? { color: '#FFFFFF' } : { color: colors.textSecondary }]}>
                        🔧 Standard (2h)
                      </Text>
                    </TouchableOpacity>
                    <TouchableOpacity 
                      style={[styles.presetChip, estimatedDuration === 3.5 ? { backgroundColor: colors.primary, borderColor: colors.primary } : { borderColor: colors.border }]}
                      onPress={() => setEstimatedDuration(3.5)}
                    >
                      <Text style={[styles.presetChipText, estimatedDuration === 3.5 ? { color: '#FFFFFF' } : { color: colors.textSecondary }]}>
                        🛠️ Complex (3.5h)
                      </Text>
                    </TouchableOpacity>
                  </View>

                  {/* Projected Cost Range */}
                  <View style={[styles.rangeBox, { backgroundColor: isDark ? '#115E59' : '#E6F4F1' }]}>
                    <Text style={[styles.rangeLabel, { color: isDark ? '#E6F4F1' : '#0F766E' }]}>Projected Cost Range</Text>
                    <Text style={[styles.rangeValue, { color: isDark ? '#2DD4BF' : '#0D9488' }]}>
                      PKR {Math.max(500, (selectedProfessional.ratePerHour * estimatedDuration) + 150 - 200).toLocaleString()} - PKR {Math.max(500, (selectedProfessional.ratePerHour * (estimatedDuration + 1)) + 150 - 200).toLocaleString()}
                    </Text>
                    <Text style={[styles.rangeDisclaimer, { color: isDark ? '#CCFBF1' : '#115E59' }]}>
                      *Min: base + fees. Max: includes 1hr troubleshooting buffer.
                    </Text>
                  </View>
                </View>

                {/* Pricing Summary */}
                <View style={[styles.priceSummaryContainer, { backgroundColor: isDark ? '#115E59' : '#E6F4F1', borderColor: isDark ? '#0D9488' : '#B2DFDB' }]}>
                  <Text style={[styles.priceSummaryTitle, { color: isDark ? '#2DD4BF' : '#00796B' }]}>Transparent Booking Estimate</Text>
                  <View style={styles.priceSummaryRow}>
                    <Text style={[styles.priceSummaryLabel, { color: colors.textSecondary }]}>Technician Rate ({selectedProfessional.name})</Text>
                    <Text style={[styles.priceSummaryVal, { color: colors.text }]}>PKR {selectedProfessional.ratePerHour}/hr</Text>
                  </View>
                  <View style={styles.priceSummaryRow}>
                    <Text style={[styles.priceSummaryLabel, { color: colors.textSecondary }]}>Estimated Job Duration</Text>
                    <Text style={[styles.priceSummaryVal, { color: colors.text }]}>{estimatedDuration} {estimatedDuration === 1 ? 'Hour' : 'Hours'}</Text>
                  </View>
                  <View style={styles.priceSummaryRow}>
                    <Text style={[styles.priceSummaryLabel, { color: colors.textSecondary }]}>Base Service Cost</Text>
                    <Text style={[styles.priceSummaryVal, { color: colors.text }]}>PKR {selectedProfessional.ratePerHour * estimatedDuration}</Text>
                  </View>
                  <View style={styles.priceSummaryRow}>
                    <Text style={[styles.priceSummaryLabel, { color: colors.textSecondary }]}>Safety & Convenience Fee</Text>
                    <Text style={[styles.priceSummaryVal, { color: colors.text }]}>PKR 150</Text>
                  </View>
                  <View style={styles.priceSummaryRow}>
                    <Text style={[styles.priceSummaryLabel, { color: colors.textSecondary }]}>First Booking Promotion</Text>
                    <Text style={[styles.priceSummaryVal, { color: '#059669', fontWeight: '700' }]}>-PKR 200</Text>
                  </View>
                  <View style={[styles.cardDivider, { backgroundColor: colors.border, marginVertical: 8 }]} />
                  <View style={styles.priceSummaryRow}>
                    <Text style={[styles.priceSummaryTotalLabel, { color: colors.text }]}>Total Estimate</Text>
                    <Text style={[styles.priceSummaryTotalVal, { color: colors.primary }]}>
                      PKR {Math.max(500, selectedProfessional.ratePerHour * estimatedDuration + 150 - 200).toLocaleString()}
                    </Text>
                  </View>
                </View>
              </ScrollView>

              {/* Confirm Actions */}
              <View style={[styles.modalFooter, { borderTopColor: colors.border }]}>
                <TouchableOpacity
                  style={[styles.modalCancelBtn, { borderColor: colors.border }]}
                  onPress={() => setIsHiringModalVisible(false)}
                >
                  <Text style={[styles.modalCancelBtnText, { color: colors.textSecondary }]}>Cancel</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.modalConfirmBtn, { backgroundColor: colors.primary }]}
                  onPress={handleConfirmHiring}
                  disabled={isHiringLoading}
                >
                  {isHiringLoading ? (
                    <ActivityIndicator size="small" color="#FFFFFF" />
                  ) : (
                    <Text style={styles.modalConfirmBtnText}>Confirm & Book</Text>
                  )}
                </TouchableOpacity>
              </View>
            </View>
          </View>
        </Modal>
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  screenContainer: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  header: {
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 8,
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: '900',
    color: '#0F172A',
    letterSpacing: -0.5,
  },
  headerSubtitle: {
    fontSize: 14,
    color: '#64748B',
    marginTop: 2,
  },
  tabContainer: {
    flexDirection: 'row',
    backgroundColor: '#E2E8F0',
    borderRadius: 12,
    padding: 4,
    marginHorizontal: 20,
    marginVertical: 14,
  },
  tab: {
    flex: 1,
    paddingVertical: 10,
    alignItems: 'center',
    borderRadius: 10,
  },
  activeTab: {
    backgroundColor: '#FFFFFF',
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 4,
    elevation: 2,
  },
  tabText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#475569',
  },
  activeTabText: {
    color: '#0F172A',
    fontWeight: '700',
  },
  listContent: {
    paddingHorizontal: 20,
    paddingBottom: 24,
    flexGrow: 1,
  },
  bookingCard: {
    marginVertical: 6,
    borderColor: '#E2E8F0',
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  serviceInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  iconContainer: {
    width: 42,
    height: 42,
    backgroundColor: '#ECFDF5',
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  serviceIcon: {
    fontSize: 20,
  },
  serviceName: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  bookingId: {
    fontSize: 11,
    color: '#94A3B8',
    marginTop: 1,
  },
  addressText: {
    fontSize: 13,
    color: '#475569',
    marginBottom: 12,
  },
  cardDivider: {
    height: 1,
    backgroundColor: '#F1F5F9',
    marginBottom: 12,
  },
  cardFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  metaLabel: {
    fontSize: 10,
    color: '#94A3B8',
    textTransform: 'uppercase',
    fontWeight: '600',
    letterSpacing: 0.3,
  },
  metaValue: {
    fontSize: 12,
    color: '#334155',
    fontWeight: '600',
    marginTop: 2,
  },
  priceContainer: {
    backgroundColor: '#F0FDF4',
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 8,
  },
  priceText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#10B981',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  loadingText: {
    marginTop: 12,
    color: '#64748B',
    fontSize: 14,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 60,
  },
  emptyIcon: {
    fontSize: 50,
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 6,
  },
  emptyMessage: {
    fontSize: 13,
    color: '#64748B',
    textAlign: 'center',
    paddingHorizontal: 32,
    lineHeight: 18,
    marginBottom: 20,
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  errorIcon: {
    fontSize: 40,
    marginBottom: 12,
  },
  errorText: {
    fontSize: 14,
    color: '#DC2626',
    textAlign: 'center',
    marginBottom: 16,
  },
  retryButton: {
    backgroundColor: '#10B981',
    paddingVertical: 10,
    paddingHorizontal: 24,
    borderRadius: 8,
  },
  retryButtonText: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 14,
  },
  skeletonIcon: {
    width: 42,
    height: 42,
    backgroundColor: '#E2E8F0',
    borderRadius: 10,
    marginRight: 12,
  },
  skeletonTitle: {
    width: 140,
    height: 16,
    backgroundColor: '#E2E8F0',
    borderRadius: 4,
  },
  skeletonSubtitle: {
    width: 80,
    height: 11,
    backgroundColor: '#E2E8F0',
    borderRadius: 4,
  },
  skeletonBadge: {
    width: 76,
    height: 22,
    backgroundColor: '#E2E8F0',
    borderRadius: 12,
  },
  skeletonAddress: {
    width: '75%',
    height: 14,
    backgroundColor: '#E2E8F0',
    borderRadius: 4,
    marginVertical: 12,
  },
  skeletonMetaLabel: {
    width: 60,
    height: 10,
    backgroundColor: '#E2E8F0',
    borderRadius: 4,
  },
  skeletonMetaValue: {
    width: 110,
    height: 14,
    backgroundColor: '#E2E8F0',
    borderRadius: 4,
  },
  skeletonPrice: {
    width: 64,
    height: 28,
    backgroundColor: '#E2E8F0',
    borderRadius: 8,
  },
  loadingListContainer: {
    paddingHorizontal: 20,
    paddingTop: 6,
  },
  searchBarContainer: {
    paddingHorizontal: 20,
    marginBottom: 10,
  },
  searchInner: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 12,
    height: 48,
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  searchIcon: {
    fontSize: 16,
    marginRight: 8,
    color: '#64748B',
  },
  searchInput: {
    flex: 1,
    fontSize: 14,
    color: '#0F172A',
    fontWeight: '500',
    paddingVertical: 8,
  },
  clearButton: {
    padding: 6,
    justifyContent: 'center',
    alignItems: 'center',
  },
  clearButtonText: {
    fontSize: 14,
    color: '#94A3B8',
    fontWeight: '700',
  },
  headerTopRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  themeToggleBtn: {
    padding: 8,
    borderRadius: 12,
    minWidth: 38,
    height: 38,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'transparent',
  },
  themeToggleBtnText: {
    fontSize: 16,
  },
  reorderButton: {
    width: 36,
    height: 36,
    borderRadius: 18,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.15,
    shadowRadius: 2,
    elevation: 2,
  },
  reorderButtonText: {
    fontSize: 14,
  },
  proCard: {
    paddingVertical: 16,
  },
  proAvatarContainer: {
    width: 46,
    height: 46,
    borderRadius: 23,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  proAvatarText: {
    fontSize: 22,
  },
  verifiedBadge: {
    fontSize: 12,
  },
  proStatusBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    gap: 4,
  },
  proStatusDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
  },
  proStatusText: {
    fontSize: 11,
    fontWeight: '700',
  },
  proMetaRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 8,
  },
  proRatingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  proRatingStar: {
    fontSize: 13,
    marginRight: 4,
  },
  proRatingText: {
    fontSize: 13,
    fontWeight: '700',
    marginRight: 4,
  },
  proJobsText: {
    fontSize: 12,
  },
  proRateText: {
    fontSize: 15,
    fontWeight: '800',
  },
  skillsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
    marginBottom: 12,
  },
  skillChip: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    borderWidth: 1,
  },
  skillChipText: {
    fontSize: 11,
    fontWeight: '600',
  },
  hireButton: {
    height: 44,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#10B981',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  hireButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '700',
    letterSpacing: 0.2,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.6)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    maxHeight: '90%',
    paddingBottom: 20,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 18,
    borderBottomWidth: 1,
  },
  modalHeaderTitle: {
    fontSize: 18,
    fontWeight: '800',
  },
  modalCloseBtn: {
    padding: 4,
  },
  modalCloseBtnText: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  modalScroll: {
    paddingHorizontal: 20,
    paddingVertical: 16,
  },
  modalProRecap: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    borderRadius: 16,
    gap: 12,
    marginBottom: 16,
  },
  modalProAvatar: {
    fontSize: 32,
  },
  modalProName: {
    fontSize: 16,
    fontWeight: '700',
  },
  modalProTitle: {
    fontSize: 12,
    marginTop: 2,
  },
  modalProRate: {
    fontSize: 13,
    fontWeight: '600',
    marginTop: 4,
  },
  modalInputLabel: {
    fontSize: 13,
    fontWeight: '700',
    marginBottom: 8,
    marginTop: 12,
  },
  modalTextInput: {
    borderWidth: 1,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 14,
    fontWeight: '500',
    marginBottom: 12,
  },
  modalTextArea: {
    height: 80,
    textAlignVertical: 'top',
  },
  timeSlotRow: {
    flexDirection: 'row',
    gap: 8,
  },
  timeSlotChip: {
    flex: 1,
    borderWidth: 1,
    borderRadius: 10,
    paddingVertical: 10,
    alignItems: 'center',
  },
  timeSlotText: {
    fontSize: 12,
    fontWeight: '600',
  },
  activeTimeSlotText: {
    color: '#FFFFFF',
    fontWeight: '700',
  },
  modalQuickTagsLabel: {
    fontSize: 11,
    fontWeight: '600',
    marginBottom: 6,
  },
  quickTagsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
    marginBottom: 16,
  },
  quickTagChip: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 12,
    borderWidth: 1,
  },
  quickTagText: {
    fontSize: 11,
    fontWeight: '500',
  },
  priceSummaryContainer: {
    borderWidth: 1,
    borderRadius: 16,
    padding: 14,
    marginTop: 12,
    marginBottom: 16,
  },
  priceSummaryTitle: {
    fontSize: 13,
    fontWeight: '700',
    marginBottom: 10,
  },
  priceSummaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 4,
  },
  priceSummaryLabel: {
    fontSize: 12,
    fontWeight: '500',
  },
  priceSummaryVal: {
    fontSize: 12,
    fontWeight: '600',
  },
  priceSummaryTotalLabel: {
    fontSize: 14,
    fontWeight: '800',
  },
  priceSummaryTotalVal: {
    fontSize: 16,
    fontWeight: '800',
  },
  modalFooter: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    paddingTop: 12,
    borderTopWidth: 1,
    gap: 12,
  },
  modalCancelBtn: {
    flex: 1,
    height: 48,
    borderRadius: 10,
    borderWidth: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalCancelBtnText: {
    fontSize: 14,
    fontWeight: '600',
  },
  modalConfirmBtn: {
    flex: 2,
    height: 48,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalConfirmBtnText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '700',
  },
  availabilityToggleContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderBottomWidth: 1,
    marginHorizontal: 16,
    borderRadius: 12,
    marginTop: 8,
    marginBottom: 4,
    elevation: 1,
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  availabilityToggleLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    flex: 1,
  },
  availabilityToggleIcon: {
    fontSize: 16,
  },
  availabilityToggleTitle: {
    fontSize: 14,
    fontWeight: '700',
  },
  availabilityToggleSubtitle: {
    fontSize: 11,
    marginTop: 1,
  },
  switchTrack: {
    width: 44,
    height: 24,
    borderRadius: 12,
    padding: 2,
    justifyContent: 'center',
  },
  switchThumb: {
    width: 20,
    height: 20,
    borderRadius: 10,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 1.5,
  },
  estimatorContainer: {
    borderWidth: 1,
    borderRadius: 16,
    padding: 14,
    marginVertical: 10,
  },
  estimatorSubtitle: {
    fontSize: 12,
    marginBottom: 12,
    lineHeight: 16,
  },
  estimatorAdjustRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 14,
  },
  durationAdjustBtn: {
    width: 44,
    height: 44,
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
  },
  durationAdjustText: {
    fontSize: 22,
    fontWeight: '600',
  },
  durationValue: {
    fontSize: 18,
    fontWeight: '800',
  },
  durationLabel: {
    fontSize: 11,
    marginTop: 2,
  },
  estimatorPresetsRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 6,
    marginBottom: 14,
  },
  presetChip: {
    flex: 1,
    paddingVertical: 8,
    paddingHorizontal: 4,
    borderRadius: 10,
    borderWidth: 1,
    alignItems: 'center',
  },
  presetChipText: {
    fontSize: 11,
    fontWeight: '700',
    textAlign: 'center',
  },
  rangeBox: {
    borderRadius: 12,
    padding: 12,
    alignItems: 'center',
  },
  rangeLabel: {
    fontSize: 11,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  rangeValue: {
    fontSize: 20,
    fontWeight: '900',
    marginVertical: 4,
  },
  rangeDisclaimer: {
    fontSize: 10,
    textAlign: 'center',
    lineHeight: 14,
  },
});
