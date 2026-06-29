import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, TextInput, ActivityIndicator, Image } from 'react-native';

interface BookingReviewFeedbackProps {
  workerName?: string;
  workerAvatar?: string;
  onReviewSubmitted?: (rating: number, reviewText: string, selectedTags: string[]) => void;
}

const FEEDBACK_TAGS = [
  { id: 'fast', label: '⚡ Fast Service' },
  { id: 'clean', label: '🧼 Clean Work' },
  { id: 'chat', label: '💬 Friendly & Polite' },
  { id: 'prof', label: '🎯 Highly Professional' },
  { id: 'fair', label: '💎 Great Value' },
];

export const BookingReviewFeedback: React.FC<BookingReviewFeedbackProps> = ({
  workerName = 'Ayaan Sheikh',
  workerAvatar = 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200',
  onReviewSubmitted,
}) => {
  const [rating, setRating] = useState<number>(0);
  const [reviewText, setReviewText] = useState<string>('');
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [isSubmitted, setIsSubmitted] = useState<boolean>(false);

  const handleToggleTag = (tagLabel: string) => {
    if (selectedTags.includes(tagLabel)) {
      setSelectedTags(prev => prev.filter(t => t !== tagLabel));
    } else {
      setSelectedTags(prev => [...prev, tagLabel]);
    }
  };

  const handleSubmit = () => {
    if (rating === 0) {
      alert('Please select a star rating first!');
      return;
    }

    setIsSubmitting(true);
    // Simulate real API network request delay
    setTimeout(() => {
      setIsSubmitting(false);
      setIsSubmitted(true);
      if (onReviewSubmitted) {
        onReviewSubmitted(rating, reviewText, selectedTags);
      }
    }, 1200);
  };

  if (isSubmitted) {
    return (
      <View style={styles.card}>
        <View style={styles.successContainer}>
          <Text style={styles.successIcon}>🎉</Text>
          <Text style={styles.successTitle}>Review Submitted!</Text>
          <Text style={styles.successSubtitle}>
            Thank you for helping us improve our service standards. Your feedback has been shared with {workerName}.
          </Text>

          <View style={styles.reviewSummaryBox}>
            <View style={styles.summaryHeader}>
              <Image source={{ uri: workerAvatar }} style={styles.summaryAvatar} />
              <View style={styles.summaryMeta}>
                <Text style={styles.summaryWorkerName}>{workerName}</Text>
                <View style={styles.summaryStarsRow}>
                  {[1, 2, 3, 4, 5].map((star) => (
                    <Text
                      key={star}
                      style={[
                        styles.summaryStarText,
                        star <= rating ? styles.summaryStarSelected : styles.summaryStarUnselected,
                      ]}
                    >
                      ★
                    </Text>
                  ))}
                </View>
              </View>
            </View>

            {selectedTags.length > 0 && (
              <View style={styles.summaryTagsContainer}>
                {selectedTags.map((tag) => (
                  <View key={tag} style={styles.summaryTag}>
                    <Text style={styles.summaryTagText}>{tag}</Text>
                  </View>
                ))}
              </View>
            )}

            {reviewText.trim().length > 0 && (
              <Text style={styles.summaryCommentText}>
                "{reviewText}"
              </Text>
            )}
          </View>

          <TouchableOpacity 
            style={styles.modifyButton}
            onPress={() => setIsSubmitted(false)}
            activeOpacity={0.8}
          >
            <Text style={styles.modifyButtonText}>Modify My Review</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.card}>
      <Text style={styles.title} accessibilityRole="header">Rate & Review Service</Text>
      <Text style={styles.subtitle}>
        Your worker has completed the service. Please rate your overall satisfaction.
      </Text>

      {/* Star Selector */}
      <View style={styles.starsRow}>
        {[1, 2, 3, 4, 5].map((star) => (
          <TouchableOpacity
            key={star}
            onPress={() => setRating(star)}
            style={styles.starTouch}
            activeOpacity={0.7}
            accessible={true}
            accessibilityLabel={`Rate ${star} star${star > 1 ? 's' : ''}`}
            accessibilityRole="imagebutton"
          >
            <Text
              style={[
                styles.starIcon,
                star <= rating ? styles.starSelected : styles.starUnselected,
              ]}
            >
              ★
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {rating > 0 && (
        <Text style={styles.ratingLabel}>
          {rating === 5 && 'Outstanding Job! 🌟'}
          {rating === 4 && 'Very Satisfied! 👍'}
          {rating === 3 && 'Good / Satisfactory'}
          {rating === 2 && 'Fair / Needs improvement'}
          {rating === 1 && 'Unsatisfactory Service'}
        </Text>
      )}

      {/* Quick feedback tags */}
      <Text style={styles.sectionLabel}>What did they do best?</Text>
      <View style={styles.tagsContainer}>
        {FEEDBACK_TAGS.map((tag) => {
          const isSelected = selectedTags.includes(tag.label);
          return (
            <TouchableOpacity
              key={tag.id}
              style={[styles.tagButton, isSelected && styles.tagButtonSelected]}
              onPress={() => handleToggleTag(tag.label)}
              activeOpacity={0.7}
            >
              <Text style={[styles.tagButtonText, isSelected && styles.tagButtonTextSelected]}>
                {tag.label}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* Freeform comment review */}
      <Text style={styles.sectionLabel}>Write detailed comment (optional)</Text>
      <TextInput
        style={styles.textInput}
        placeholder="How was the cleaning or technician's attitude? Any feedback help us improve..."
        placeholderTextColor="#94A3B8"
        value={reviewText}
        onChangeText={setReviewText}
        multiline
        numberOfLines={3}
        maxLength={250}
      />

      <TouchableOpacity
        style={[styles.submitButton, rating === 0 && styles.submitButtonDisabled]}
        onPress={handleSubmit}
        disabled={rating === 0 || isSubmitting}
        activeOpacity={0.8}
      >
        {isSubmitting ? (
          <ActivityIndicator size="small" color="#FFFFFF" />
        ) : (
          <Text style={styles.submitButtonText}>Submit Review & Close Order</Text>
        )}
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 20,
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.04,
    shadowRadius: 12,
    elevation: 3,
    marginBottom: 16,
  },
  title: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
    letterSpacing: -0.3,
  },
  subtitle: {
    fontSize: 12,
    color: '#64748B',
    fontWeight: '500',
    lineHeight: 18,
    marginTop: 4,
    marginBottom: 16,
  },
  starsRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 12,
    marginBottom: 8,
  },
  starTouch: {
    padding: 4,
  },
  starIcon: {
    fontSize: 38,
  },
  starSelected: {
    color: '#F59E0B',
  },
  starUnselected: {
    color: '#E2E8F0',
  },
  ratingLabel: {
    fontSize: 13,
    fontWeight: '800',
    color: '#D97706',
    textAlign: 'center',
    marginBottom: 16,
  },
  sectionLabel: {
    fontSize: 11,
    fontWeight: '800',
    color: '#475569',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginTop: 12,
    marginBottom: 8,
  },
  tagsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 12,
  },
  tagButton: {
    backgroundColor: '#F1F5F9',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 20,
    paddingVertical: 6,
    paddingHorizontal: 12,
  },
  tagButtonSelected: {
    backgroundColor: '#ECFDF5',
    borderColor: '#34D399',
  },
  tagButtonText: {
    fontSize: 11.5,
    fontWeight: '600',
    color: '#475569',
  },
  tagButtonTextSelected: {
    color: '#047857',
    fontWeight: '700',
  },
  textInput: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    padding: 12,
    height: 80,
    textAlignVertical: 'top',
    fontSize: 13,
    color: '#0F172A',
    marginBottom: 16,
  },
  submitButton: {
    backgroundColor: '#10B981',
    borderRadius: 12,
    height: 48,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#10B981',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 2,
  },
  submitButtonDisabled: {
    backgroundColor: '#CBD5E1',
    shadowOpacity: 0,
    elevation: 0,
  },
  submitButtonText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  successContainer: {
    alignItems: 'center',
    paddingVertical: 12,
  },
  successIcon: {
    fontSize: 48,
    marginBottom: 12,
  },
  successTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#069669',
  },
  successSubtitle: {
    fontSize: 12,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 18,
    marginTop: 6,
    marginBottom: 16,
  },
  reviewSummaryBox: {
    width: '100%',
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 16,
    padding: 14,
    marginBottom: 16,
  },
  summaryHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  summaryAvatar: {
    width: 36,
    height: 36,
    borderRadius: 18,
  },
  summaryMeta: {
    marginLeft: 12,
  },
  summaryWorkerName: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },
  summaryStarsRow: {
    flexDirection: 'row',
    marginTop: 2,
  },
  summaryStarText: {
    fontSize: 14,
    marginRight: 1,
  },
  summaryStarSelected: {
    color: '#F59E0B',
  },
  summaryStarUnselected: {
    color: '#CBD5E1',
  },
  summaryTagsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
    marginBottom: 10,
  },
  summaryTag: {
    backgroundColor: '#ECFDF5',
    borderRadius: 6,
    paddingVertical: 3,
    paddingHorizontal: 8,
  },
  summaryTagText: {
    fontSize: 10,
    fontWeight: '700',
    color: '#047857',
  },
  summaryCommentText: {
    fontSize: 12,
    fontStyle: 'italic',
    color: '#475569',
    lineHeight: 18,
    borderLeftWidth: 3,
    borderLeftColor: '#10B981',
    paddingLeft: 8,
  },
  modifyButton: {
    paddingVertical: 8,
    paddingHorizontal: 16,
  },
  modifyButtonText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#4F46E5',
  },
});
