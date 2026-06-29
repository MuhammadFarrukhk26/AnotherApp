import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  Animated,
  Dimensions,
  Image,
  ActivityIndicator,
} from 'react-native';

interface Message {
  id: string;
  text: string;
  sender: 'user' | 'worker';
  timestamp: string;
}

interface BookingChatDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  workerName?: string;
  workerAvatar?: string;
}

const QUICK_MESSAGES = [
  '👋 Hi, are you on your way?',
  '🏠 I am at the location.',
  '🚗 Parking is available inside.',
  '🔑 Gate/intercom code is 1234.',
  '📞 Please call me when you arrive.',
];

const WORKER_RESPONSES: Record<string, string> = {
  '👋 Hi, are you on your way?': 'Yes, I am en route! Navigating through the main road now. See you in about 10-12 minutes.',
  '🏠 I am at the location.': 'Perfect! Thanks for confirming. I am about 3 km away and moving steadily.',
  '🚗 Parking is available inside.': 'Excellent, thank you! That makes it much easier to park the ride safely.',
  '🔑 Gate/intercom code is 1234.': 'Got it! Gate code 1234. I will enter directly once I arrive.',
  '📞 Please call me when you arrive.': 'Will do! I will ring your phone as soon as I pull up outside.',
  'default': 'Thank you! I am on my way and focused on safe driving. I will update you as soon as I arrive.',
};

const { height: SCREEN_HEIGHT } = Dimensions.get('window');

export const BookingChatDrawer: React.FC<BookingChatDrawerProps> = ({
  isOpen,
  onClose,
  workerName = 'Ayaan Sheikh',
  workerAvatar = 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=200&h=200',
}) => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      text: `Hello! I am Ayaan, your assigned technician today. I am currently preparing my service equipment and will head out shortly.`,
      sender: 'worker',
      timestamp: '02:15 PM',
    },
  ]);
  const [inputText, setInputText] = useState<string>('');
  const [isWorkerTyping, setIsWorkerTyping] = useState<boolean>(false);

  // Animated sliding control
  const slideAnim = useRef(new Animated.Value(SCREEN_HEIGHT)).current;
  const opacityAnim = useRef(new Animated.Value(0)).current;
  const scrollViewRef = useRef<ScrollView>(null);

  useEffect(() => {
    if (isOpen) {
      // Animate Drawer opening
      Animated.parallel([
        Animated.timing(slideAnim, {
          toValue: 0,
          duration: 350,
          useNativeDriver: true,
        }),
        Animated.timing(opacityAnim, {
          toValue: 1,
          duration: 300,
          useNativeDriver: true,
        }),
      ]).start(() => {
        // Scroll to end once drawer opens
        setTimeout(() => scrollViewRef.current?.scrollToEnd({ animated: true }), 100);
      });
    } else {
      // Animate Drawer closing
      Animated.parallel([
        Animated.timing(slideAnim, {
          toValue: SCREEN_HEIGHT,
          duration: 300,
          useNativeDriver: true,
        }),
        Animated.timing(opacityAnim, {
          toValue: 0,
          duration: 250,
          useNativeDriver: true,
        }),
      ]).start();
    }
  }, [isOpen]);

  const handleSendMessage = (text: string) => {
    if (!text.trim()) return;

    const currentTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const userMsg: Message = {
      id: Math.random().toString(),
      text: text.trim(),
      sender: 'user',
      timestamp: currentTime,
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputText('');
    
    // Auto-scroll to bottom
    setTimeout(() => scrollViewRef.current?.scrollToEnd({ animated: true }), 50);

    // Simulate worker typing and reply
    setIsWorkerTyping(true);
    setTimeout(() => {
      setIsWorkerTyping(false);
      
      const responseText = WORKER_RESPONSES[text] || WORKER_RESPONSES['default'];
      const workerMsg: Message = {
        id: Math.random().toString(),
        text: responseText,
        sender: 'worker',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };

      setMessages((prev) => [...prev, workerMsg]);
      setTimeout(() => scrollViewRef.current?.scrollToEnd({ animated: true }), 100);
    }, 2000);
  };

  if (!isOpen) return null;

  return (
    <Animated.View style={[styles.overlay, { opacity: opacityAnim }]}>
      {/* Backdrop Press Area to Close */}
      <TouchableOpacity 
        style={styles.backdrop} 
        activeOpacity={1} 
        onPress={onClose} 
      />

      {/* Sliding Drawer Body */}
      <Animated.View 
        style={[
          styles.drawerContainer, 
          { transform: [{ translateY: slideAnim }] }
        ]}
      >
        <KeyboardAvoidingView
          behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
          style={styles.keyboardContainer}
          keyboardVerticalOffset={Platform.OS === 'ios' ? 40 : 0}
        >
          {/* Header Bar */}
          <View style={styles.header}>
            <View style={styles.dragBar} />
            <View style={styles.headerInfoRow}>
              <View style={styles.workerMeta}>
                <Image source={{ uri: workerAvatar }} style={styles.avatar} />
                <View style={styles.nameContainer}>
                  <Text style={styles.workerName}>{workerName}</Text>
                  <View style={styles.onlineStatusRow}>
                    <View style={styles.statusDot} />
                    <Text style={styles.statusText}>Active Secure Chat</Text>
                  </View>
                </View>
              </View>

              <TouchableOpacity 
                style={styles.closeButton} 
                onPress={onClose}
                accessible={true}
                accessibilityLabel="Close messaging panel"
                accessibilityRole="button"
              >
                <Text style={styles.closeButtonText}>✕</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* Messages Scroll Area */}
          <ScrollView
            ref={scrollViewRef}
            style={styles.messagesContainer}
            contentContainerStyle={styles.messagesContent}
            showsVerticalScrollIndicator={true}
          >
            <View style={styles.encryptedBanner}>
              <Text style={styles.encryptedBannerText}>
                🔒 End-to-end encrypted connection. Your conversation is secure.
              </Text>
            </View>

            {messages.map((msg) => {
              const isUser = msg.sender === 'user';
              return (
                <View 
                  key={msg.id} 
                  style={[
                    styles.messageRow, 
                    isUser ? styles.messageRowUser : styles.messageRowWorker
                  ]}
                >
                  {!isUser && (
                    <Image source={{ uri: workerAvatar }} style={styles.messageAvatar} />
                  )}
                  <View style={styles.bubbleWrapper}>
                    <View 
                      style={[
                        styles.messageBubble, 
                        isUser ? styles.bubbleUser : styles.bubbleWorker
                      ]}
                    >
                      <Text style={[styles.messageText, isUser ? styles.textUser : styles.textWorker]}>
                        {msg.text}
                      </Text>
                    </View>
                    <Text style={[styles.timestamp, isUser ? styles.timeUser : styles.timeWorker]}>
                      {msg.timestamp} {isUser && '✓✓'}
                    </Text>
                  </View>
                </View>
              );
            })}

            {/* Worker Typing Indicator */}
            {isWorkerTyping && (
              <View style={[styles.messageRow, styles.messageRowWorker]}>
                <Image source={{ uri: workerAvatar }} style={styles.messageAvatar} />
                <View style={styles.bubbleWrapper}>
                  <View style={[styles.messageBubble, styles.bubbleWorker, styles.typingBubble]}>
                    <ActivityIndicator size="small" color="#475569" style={{ marginRight: 6 }} />
                    <Text style={[styles.messageText, styles.textWorker, { fontStyle: 'italic' }]}>
                      {workerName} is typing...
                    </Text>
                  </View>
                </View>
              </View>
            )}
          </ScrollView>

          {/* Quick Predefined Messages Row */}
          <View style={styles.quickMessagesSection}>
            <Text style={styles.quickTitle}>QUICK REPLY TEMPLATES</Text>
            <ScrollView 
              horizontal 
              showsHorizontalScrollIndicator={false}
              contentContainerStyle={styles.quickScrollContent}
            >
              {QUICK_MESSAGES.map((msgText) => (
                <TouchableOpacity
                  key={msgText}
                  style={styles.quickMessageButton}
                  onPress={() => handleSendMessage(msgText)}
                  disabled={isWorkerTyping}
                >
                  <Text style={styles.quickMessageText}>{msgText}</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>
          </View>

          {/* Standard Message Input Bar */}
          <View style={styles.inputBar}>
            <TextInput
              style={styles.textInput}
              placeholder="Type secure message..."
              placeholderTextColor="#94A3B8"
              value={inputText}
              onChangeText={setInputText}
              onSubmitEditing={() => handleSendMessage(inputText)}
              returnKeyType="send"
              maxLength={300}
              editable={!isWorkerTyping}
            />
            <TouchableOpacity
              style={[styles.sendButton, !inputText.trim() && styles.sendButtonDisabled]}
              onPress={() => handleSendMessage(inputText)}
              disabled={!inputText.trim() || isWorkerTyping}
              activeOpacity={0.8}
            >
              <Text style={styles.sendButtonText}>➔</Text>
            </TouchableOpacity>
          </View>
        </KeyboardAvoidingView>
      </Animated.View>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  overlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(15, 23, 42, 0.45)', // Sleek darkened backdrop
    zIndex: 1000,
  },
  backdrop: {
    flex: 1,
  },
  drawerContainer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: SCREEN_HEIGHT * 0.75, // Sleek 75% height viewport
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 20,
    overflow: 'hidden',
  },
  keyboardContainer: {
    flex: 1,
  },
  header: {
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
    paddingBottom: 12,
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
  },
  dragBar: {
    width: 44,
    height: 5,
    backgroundColor: '#E2E8F0',
    borderRadius: 3,
    marginTop: 8,
    marginBottom: 12,
  },
  headerInfoRow: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
  },
  workerMeta: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    borderWidth: 1.5,
    borderColor: '#10B981',
  },
  nameContainer: {
    marginLeft: 12,
  },
  workerName: {
    fontSize: 14.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  onlineStatusRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 2,
  },
  statusDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#10B981',
    marginRight: 6,
  },
  statusText: {
    fontSize: 10.5,
    color: '#64748B',
    fontWeight: '700',
  },
  closeButton: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#F1F5F9',
    justifyContent: 'center',
    alignItems: 'center',
  },
  closeButtonText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#475569',
  },
  messagesContainer: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  messagesContent: {
    padding: 16,
    paddingBottom: 24,
  },
  encryptedBanner: {
    backgroundColor: '#EEF2FF',
    borderRadius: 10,
    paddingVertical: 6,
    paddingHorizontal: 12,
    alignSelf: 'center',
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#E0E7FF',
  },
  encryptedBannerText: {
    fontSize: 10,
    fontWeight: '700',
    color: '#4F46E5',
    textAlign: 'center',
  },
  messageRow: {
    flexDirection: 'row',
    marginBottom: 16,
    maxWidth: '82%',
  },
  messageRowUser: {
    alignSelf: 'flex-end',
    flexDirection: 'row-reverse',
  },
  messageRowWorker: {
    alignSelf: 'flex-start',
  },
  messageAvatar: {
    width: 28,
    height: 28,
    borderRadius: 14,
    marginRight: 8,
    alignSelf: 'flex-end',
  },
  bubbleWrapper: {
    flexDirection: 'column',
  },
  messageBubble: {
    paddingVertical: 10,
    paddingHorizontal: 14,
    borderRadius: 16,
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.02,
    shadowRadius: 2,
    elevation: 1,
  },
  bubbleUser: {
    backgroundColor: '#4F46E5',
    borderBottomRightRadius: 4,
  },
  bubbleWorker: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderBottomLeftRadius: 4,
  },
  typingBubble: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F1F5F9',
    borderColor: '#E2E8F0',
  },
  messageText: {
    fontSize: 13,
    lineHeight: 18,
  },
  textUser: {
    color: '#FFFFFF',
    fontWeight: '500',
  },
  textWorker: {
    color: '#1E293B',
    fontWeight: '500',
  },
  timestamp: {
    fontSize: 9,
    fontWeight: '600',
    marginTop: 4,
  },
  timeUser: {
    color: '#818CF8',
    textAlign: 'right',
  },
  timeWorker: {
    color: '#94A3B8',
    marginLeft: 4,
  },
  quickMessagesSection: {
    backgroundColor: '#FFFFFF',
    borderTopWidth: 1,
    borderTopColor: '#F1F5F9',
    paddingVertical: 10,
  },
  quickTitle: {
    fontSize: 8.5,
    fontWeight: '800',
    color: '#94A3B8',
    letterSpacing: 1,
    marginLeft: 16,
    marginBottom: 6,
  },
  quickScrollContent: {
    paddingHorizontal: 16,
    gap: 8,
  },
  quickMessageButton: {
    backgroundColor: '#F1F5F9',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 16,
    paddingVertical: 6,
    paddingHorizontal: 12,
  },
  quickMessageText: {
    fontSize: 11.5,
    color: '#334155',
    fontWeight: '600',
  },
  inputBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderTopWidth: 1,
    borderTopColor: '#F1F5F9',
    backgroundColor: '#FFFFFF',
  },
  textInput: {
    flex: 1,
    backgroundColor: '#F8FAFC',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 8,
    height: 40,
    fontSize: 13,
    color: '#0F172A',
    marginRight: 12,
  },
  sendButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#4F46E5',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#4F46E5',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 4,
    elevation: 3,
  },
  sendButtonDisabled: {
    backgroundColor: '#CBD5E1',
    shadowOpacity: 0,
    elevation: 0,
  },
  sendButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: 'bold',
  },
});
