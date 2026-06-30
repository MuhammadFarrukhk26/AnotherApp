import React from 'react';
import { ScrollView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';

export interface Category {
  id: string;
  name: string;
  icon: string;
}

export const CATEGORIES: Category[] = [
  { id: 'all', name: 'All', icon: '🧹' }, // Default icon, but we will change them below
  { id: 'plumbing', name: 'Plumbing', icon: '🚰' },
  { id: 'cleaning', name: 'Cleaning', icon: '🧹' },
  { id: 'electrical', name: 'Electrical', icon: '⚡' },
  { id: 'ac', name: 'AC & Cooling', icon: '❄️' },
  { id: 'painting', name: 'Painting', icon: '🎨' },
  { id: 'carpentry', name: 'Carpentry', icon: '🪚' },
  { id: 'repair', name: 'Repair', icon: '🔧' },
  { id: 'moving', name: 'Moving', icon: '📦' },
];

// Let's fix the 'all' icon to something more general
CATEGORIES[0].icon = '✨';

interface CategoryFilterProps {
  selectedCategoryId: string;
  onSelectCategory: (id: string) => void;
}

export const CategoryFilter: React.FC<CategoryFilterProps> = ({
  selectedCategoryId,
  onSelectCategory,
}) => {
  return (
    <View style={styles.container}>
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}
      >
        {CATEGORIES.map((category) => {
          const isActive = selectedCategoryId === category.id;
          return (
            <TouchableOpacity
              key={category.id}
              activeOpacity={0.7}
              onPress={() => onSelectCategory(category.id)}
              style={[
                styles.chip,
                isActive ? styles.activeChip : styles.inactiveChip,
              ]}
              testID={`category_chip_${category.id}`}
            >
              <Text style={styles.chipIcon}>{category.icon}</Text>
              <Text
                style={[
                  styles.chipText,
                  isActive ? styles.activeChipText : styles.inactiveChipText,
                ]}
              >
                {category.name}
              </Text>
            </TouchableOpacity>
          );
        })}
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginVertical: 4,
    paddingBottom: 4,
  },
  scrollContent: {
    paddingHorizontal: 20,
    gap: 8,
  },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
  },
  inactiveChip: {
    backgroundColor: '#F1F5F9',
    borderColor: '#E2E8F0',
  },
  activeChip: {
    backgroundColor: '#10B981',
    borderColor: '#10B981',
    shadowColor: '#10B981',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 4,
    elevation: 3,
  },
  chipIcon: {
    fontSize: 14,
    marginRight: 6,
  },
  chipText: {
    fontSize: 13,
    fontWeight: '600',
  },
  inactiveChipText: {
    color: '#475569',
  },
  activeChipText: {
    color: '#FFFFFF',
    fontWeight: '700',
  },
});
