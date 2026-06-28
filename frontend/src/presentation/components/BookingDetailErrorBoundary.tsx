import React, { Component, ErrorInfo, ReactNode } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  TouchableOpacity, 
  ScrollView, 
  Clipboard, 
  SafeAreaView,
  ActivityIndicator
} from 'react-native';

interface Props {
  children: ReactNode;
  bookingId: string;
  onBack: () => void;
  onRetry: () => void;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
  isRetrying: boolean;
  copied: boolean;
  showDiagnostics: boolean;
}

export class BookingDetailErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
    errorInfo: null,
    isRetrying: false,
    copied: false,
    showDiagnostics: false,
  };

  public static getDerivedStateFromError(error: Error): Partial<State> {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('[BookingDetailErrorBoundary] Caught an uncaught error:', error, errorInfo);
    this.setState({ error, errorInfo });
  }

  private handleRetry = async () => {
    this.setState({ isRetrying: true });
    
    // Simulate reconnection and re-fetching wait to give polished visual feedback
    setTimeout(() => {
      this.setState({ 
        hasError: false, 
        error: null, 
        errorInfo: null, 
        isRetrying: false 
      });
      // Call store re-fetch
      this.props.onRetry();
    }, 1200);
  };

  private handleCopyError = () => {
    const { error, errorInfo } = this.state;
    if (error) {
      const errorMessage = `Error: ${error.message}\nStack: ${error.stack}\nComponent Stack: ${errorInfo?.componentStack}`;
      try {
        Clipboard.setString(errorMessage);
        this.setState({ copied: true });
        setTimeout(() => this.setState({ copied: false }), 2000);
      } catch (e) {
        console.warn('[BookingDetailErrorBoundary] Failed to copy error message:', e);
      }
    }
  };

  private toggleDiagnostics = () => {
    this.setState(prev => ({ showDiagnostics: !prev }));
  };

  public render() {
    if (this.state.hasError) {
      const { isRetrying, copied, showDiagnostics, error } = this.state;

      return (
        <SafeAreaView style={styles.safeArea}>
          <View 
            style={styles.container}
            accessible={true}
            accessibilityLabel="System connection and loading recovery panel."
            accessibilityRole="alert"
          >
            {/* Elegant Header */}
            <View style={styles.header}>
              <TouchableOpacity 
                onPress={this.props.onBack} 
                style={styles.backButton}
                accessible={true}
                accessibilityLabel="Back, exit to home screen"
                accessibilityRole="button"
              >
                <Text style={styles.backButtonText}>← Exit</Text>
              </TouchableOpacity>
              <Text style={styles.headerTitle} accessibilityRole="header">Recovery Console</Text>
              <View style={styles.headerSpacer} />
            </View>

            <ScrollView contentContainerStyle={styles.scrollContainer}>
              {/* Visual High-contrast Indicator */}
              <View style={styles.visualCard}>
                <View style={styles.alertIconBg}>
                  <Text style={styles.alertIcon}>⚠️</Text>
                </View>
                <Text style={styles.alertHeading}>Connection Interrupted</Text>
                <Text style={styles.alertMessage}>
                  We encountered a connection or rendering issue while loading details for booking ID:
                  <Text style={styles.bookingIdHighlight}> {this.props.bookingId}</Text>.
                </Text>
              </View>

              {/* Recovery Options Section */}
              <Text style={styles.sectionTitle}>RECOVERY OPTIONS</Text>
              
              {/* 1. Retry Connection */}
              <TouchableOpacity
                style={[styles.actionButton, styles.primaryButton]}
                onPress={this.handleRetry}
                disabled={isRetrying}
                accessible={true}
                accessibilityLabel="Retry connection and re-fetch booking data."
                accessibilityRole="button"
                accessibilityHint="Attempts to re-establish secure socket and API connection."
              >
                {isRetrying ? (
                  <View style={styles.loaderRow}>
                    <ActivityIndicator size="small" color="#FFFFFF" />
                    <Text style={styles.actionButtonTextPrimary}>Reconnecting...</Text>
                  </View>
                ) : (
                  <Text style={styles.actionButtonTextPrimary}>🔄 Reconnect & Refresh Details</Text>
                )}
              </TouchableOpacity>

              {/* 2. Socket Reset */}
              <TouchableOpacity
                style={[styles.actionButton, styles.secondaryButton]}
                onPress={this.handleRetry}
                disabled={isRetrying}
                accessible={true}
                accessibilityLabel="Reset tracking session"
                accessibilityRole="button"
                accessibilityHint="Forces a full fresh state reset on tracking pipelines."
              >
                <Text style={styles.actionButtonTextSecondary}>⚡ Reset Connection Pipelines</Text>
              </TouchableOpacity>

              {/* 3. Go back to Home */}
              <TouchableOpacity
                style={[styles.actionButton, styles.tertiaryButton]}
                onPress={this.props.onBack}
                accessible={true}
                accessibilityLabel="Exit to previous page"
                accessibilityRole="button"
              >
                <Text style={styles.actionButtonTextTertiary}>🏡 Safe Exit to Dashboard</Text>
              </TouchableOpacity>

              {/* Expandable Diagnostic details */}
              <View style={styles.diagnosticContainer}>
                <TouchableOpacity 
                  style={styles.diagnosticHeader} 
                  onPress={this.toggleDiagnostics}
                  accessible={true}
                  accessibilityLabel="Toggle developer diagnostics console log."
                  accessibilityRole="button"
                >
                  <Text style={styles.diagnosticHeaderTitle}>
                    {showDiagnostics ? '▼ Hide Diagnostic Logs' : '▶ Show Diagnostic Logs'}
                  </Text>
                  <Text style={styles.diagnosticInfoTag}>ADVANCED</Text>
                </TouchableOpacity>

                {showDiagnostics && (
                  <View style={styles.diagnosticBody}>
                    <Text style={styles.diagnosticLabel}>ERROR SUMMARY:</Text>
                    <View style={styles.errorLogBox}>
                      <Text style={styles.errorLogText}>
                        {error ? error.toString() : 'Unknown socket/render failure.'}
                      </Text>
                    </View>
                    
                    <TouchableOpacity
                      style={styles.copyButton}
                      onPress={this.handleCopyError}
                      accessible={true}
                      accessibilityLabel="Copy error trace to device clipboard."
                      accessibilityRole="button"
                    >
                      <Text style={styles.copyButtonText}>
                        {copied ? '✅ Stack Copied!' : '📋 Copy Diagnostic Stack'}
                      </Text>
                    </TouchableOpacity>
                  </View>
                )}
              </View>
            </ScrollView>
          </View>
        </SafeAreaView>
      );
    }

    return this.props.children;
  }
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#0F172A', // Deep slate high-contrast safety background
  },
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#1E293B',
    backgroundColor: '#1E293B',
  },
  backButton: {
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 8,
    backgroundColor: '#0F172A',
    borderWidth: 1,
    borderColor: '#334155',
  },
  backButtonText: {
    color: '#94A3B8',
    fontSize: 13,
    fontWeight: '700',
  },
  headerTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#F8FAFC',
    letterSpacing: 0.5,
  },
  headerSpacer: {
    width: 60,
  },
  scrollContainer: {
    padding: 20,
    paddingBottom: 40,
  },
  visualCard: {
    backgroundColor: '#1E293B',
    borderRadius: 20,
    padding: 20,
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: '#334155',
    marginBottom: 24,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.25,
    shadowRadius: 10,
    elevation: 4,
  },
  alertIconBg: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: '#451A03',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: '#F59E0B',
    marginBottom: 16,
  },
  alertIcon: {
    fontSize: 28,
  },
  alertHeading: {
    fontSize: 18,
    fontWeight: '800',
    color: '#F8FAFC',
    marginBottom: 8,
    textAlign: 'center',
  },
  alertMessage: {
    fontSize: 14,
    color: '#94A3B8',
    lineHeight: 20,
    textAlign: 'center',
  },
  bookingIdHighlight: {
    color: '#F59E0B',
    fontWeight: '800',
    fontFamily: 'monospace',
  },
  sectionTitle: {
    fontSize: 11,
    fontWeight: '800',
    color: '#94A3B8',
    letterSpacing: 1.5,
    marginBottom: 12,
  },
  actionButton: {
    borderRadius: 14,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 6,
    borderWidth: 1.5,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 4,
    elevation: 3,
  },
  primaryButton: {
    backgroundColor: '#F59E0B',
    borderColor: '#F59E0B',
  },
  secondaryButton: {
    backgroundColor: '#1E293B',
    borderColor: '#334155',
  },
  tertiaryButton: {
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
  },
  actionButtonTextPrimary: {
    color: '#0F172A',
    fontWeight: '800',
    fontSize: 15,
  },
  actionButtonTextSecondary: {
    color: '#F8FAFC',
    fontWeight: '700',
    fontSize: 14,
  },
  actionButtonTextTertiary: {
    color: '#94A3B8',
    fontWeight: '700',
    fontSize: 14,
  },
  loaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  diagnosticContainer: {
    marginTop: 24,
    backgroundColor: '#1E293B',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#334155',
    overflow: 'hidden',
  },
  diagnosticHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 14,
    backgroundColor: '#0F172A',
  },
  diagnosticHeaderTitle: {
    fontSize: 12,
    fontWeight: '700',
    color: '#94A3B8',
  },
  diagnosticInfoTag: {
    fontSize: 9,
    fontWeight: '800',
    color: '#F59E0B',
    backgroundColor: '#451A03',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  diagnosticBody: {
    padding: 14,
  },
  diagnosticLabel: {
    fontSize: 10,
    fontWeight: '800',
    color: '#64748B',
    marginBottom: 6,
  },
  errorLogBox: {
    backgroundColor: '#0F172A',
    borderRadius: 8,
    padding: 10,
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  errorLogText: {
    color: '#EF4444',
    fontFamily: 'monospace',
    fontSize: 11,
    lineHeight: 16,
  },
  copyButton: {
    marginTop: 12,
    alignSelf: 'flex-start',
    backgroundColor: '#334155',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 6,
  },
  copyButtonText: {
    color: '#F8FAFC',
    fontSize: 11,
    fontWeight: '700',
  },
});
