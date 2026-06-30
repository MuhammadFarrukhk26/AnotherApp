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
} from 'react-native';
import { useBookingStore } from '../state/bookingStore';
import { Card } from '../components/Card';
import { StatusBadge } from '../components/StatusBadge';
import { CategoryFilter } from '../components/CategoryFilter';
import { Booking } from '../../domain/models/Booking';

const BookingCardSkeleton: React.FC = () => {
  const pulseAnim = useRef(new Animated.Value(0.3)).current;

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
          <Animated.View style={[styles.skeletonIcon, { opacity: pulseAnim }]} />
          
          <View style={{ gap: 6 }}>
            {/* Title Placeholder */}
            <Animated.View style={[styles.skeletonTitle, { opacity: pulseAnim }]} />
            {/* Subtitle/ID Placeholder */}
            <Animated.View style={[styles.skeletonSubtitle, { opacity: pulseAnim }]} />
          </View>
        </View>

        {/* Status Badge Placeholder */}
        <Animated.View style={[styles.skeletonBadge, { opacity: pulseAnim }]} />
      </View>

      {/* Address Placeholder */}
      <Animated.View style={[styles.skeletonAddress, { opacity: pulseAnim }]} />

      <View style={styles.cardDivider} />

      <View style={styles.cardFooter}>
        <View style={{ gap: 6 }}>
          {/* Label Placeholder */}
          <Animated.View style={[styles.skeletonMetaLabel, { opacity: pulseAnim }]} />
          {/* Value Placeholder */}
          <Animated.View style={[styles.skeletonMetaValue, { opacity: pulseAnim }]} />
        </View>

        {/* Price Placeholder */}
        <Animated.View style={[styles.skeletonPrice, { opacity: pulseAnim }]} />
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
  const { bookings, loading, error, fetchCustomerBookings } = useBookingStore();
  const [activeTab, setActiveTab] = useState<'pending' | 'past'>('pending');
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [refreshing, setRefreshing] = useState(false);

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

  // Categorize bookings
  const pendingBookings = bookings.filter(
    (b) => b.status === 'PENDING' || b.status === 'ACCEPTED' || b.status === 'IN_PROGRESS'
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
              <View style={styles.iconContainer}>
                <Text style={styles.serviceIcon}>{getServiceIcon(item.serviceType)}</Text>
              </View>
              <View>
                <Text style={styles.serviceName}>{getServiceTitle(item.serviceType)}</Text>
                <Text style={styles.bookingId}>Booking #{item.id}</Text>
              </View>
            </View>
            <StatusBadge status={item.status} />
          </View>

          <Text style={styles.addressText} numberOfLines={1}>
            📍 {item.address}
          </Text>

          <View style={styles.cardDivider} />

          <View style={styles.cardFooter}>
            <View>
              <Text style={styles.metaLabel}>Scheduled For</Text>
              <Text style={styles.metaValue}>{item.scheduledTime}</Text>
            </View>
            <View style={styles.priceContainer}>
              <Text style={styles.priceText}>{item.formattedPrice}</Text>
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
        <Text style={styles.emptyTitle}>{title}</Text>
        <Text style={styles.emptyMessage}>{message}</Text>
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

  return (
    <SafeAreaView style={styles.screenContainer}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Hazir Bookings</Text>
        <Text style={styles.headerSubtitle}>Manage your home & office services</Text>
      </View>

      {/* Tabs */}
      <View style={styles.tabContainer}>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'pending' && styles.activeTab]}
          onPress={() => setActiveTab('pending')}
          testID="tab_pending"
        >
          <Text style={[styles.tabText, activeTab === 'pending' && styles.activeTabText]}>
            Active ({pendingBookings.length})
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'past' && styles.activeTab]}
          onPress={() => setActiveTab('past')}
          testID="tab_past"
        >
          <Text style={[styles.tabText, activeTab === 'past' && styles.activeTabText]}>
            Past ({pastBookings.length})
          </Text>
        </TouchableOpacity>
      </View>

      {/* Search Bar */}
      <View style={styles.searchBarContainer}>
        <View style={styles.searchInner}>
          <Text style={styles.searchIcon}>🔍</Text>
          <TextInput
            style={styles.searchInput}
            placeholder="Search provider, service, or address..."
            placeholderTextColor="#94A3B8"
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

      {error ? (
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
});
