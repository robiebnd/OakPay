import { Image, Pressable, StyleSheet, Text, View, Animated, Easing } from 'react-native';
import { router } from 'expo-router';
import { useEffect, useRef, useState } from 'react';
import { useAuth } from '../context/AuthContext';

const OAKPAY_LOGO = require('../assets/Logo_OakPay.png');
const SPLASH_DURATION = 30000;
const CONTENT_FADE_DURATION = 550;
const AUTH_REDIRECT_DELAY = 30000;

export default function Index() {
  const { accessToken, isLoading } = useAuth();
  const [showContent, setShowContent] = useState(false);
  const contentOpacity = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const splashTimer = setTimeout(() => {
      setShowContent(true);
      Animated.timing(contentOpacity, {
        toValue: 1,
        duration: CONTENT_FADE_DURATION,
        easing: Easing.out(Easing.ease),
        useNativeDriver: true,
      }).start();
    }, SPLASH_DURATION);

    return () => clearTimeout(splashTimer);
  }, [contentOpacity]);

  useEffect(() => {
    if (!isLoading && accessToken) {
      const timer = setTimeout(() => router.replace('/(tabs)'), AUTH_REDIRECT_DELAY);
      return () => clearTimeout(timer);
    }
  }, [accessToken, isLoading]);

  if (!showContent) {
    return (
      <View style={styles.splash}>
        <View style={styles.glowTop} />
        <View style={styles.glowSide} />
        <Image source={OAKPAY_LOGO} style={styles.splashLogo} resizeMode="contain" />
        <Text style={styles.splashTagline}>Simple. Secure. P2P.</Text>
      </View>
    );
  }

  return (
    <Animated.View style={[styles.container, { opacity: contentOpacity }]}>
      <View style={styles.glowTop} />
      <View style={styles.glowSide} />
      <View style={styles.brand}>
        <Image source={OAKPAY_LOGO} style={styles.logo} resizeMode="contain" />
        <Text style={styles.tagline}>Simple. Secure. P2P.</Text>
      </View>

      <View style={styles.message}>
        <Text style={styles.heading}>A smarter{`\n`}way to trade</Text>
        <Text style={styles.accent}>and grow</Text>
        <Text style={styles.description}>Buy, sell and store your digital assets with confidence.</Text>
      </View>

      <View style={styles.bottom}>
        <View style={styles.dots}>
          <View style={[styles.dot, styles.dotActive]} /><View style={styles.dot} /><View style={styles.dot} />
        </View>
        <Pressable style={styles.button} onPress={() => router.replace('/(auth)/login')}>
          <Text style={styles.buttonText}>Get Started</Text>
          <Text style={styles.arrow}>→</Text>
        </Pressable>
        <Pressable onPress={() => router.replace('/(auth)/login')}>
          <Text style={styles.signIn}>Already have an account? <Text style={styles.signInAccent}>Sign in</Text></Text>
        </Pressable>
      </View>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  splash: { flex: 1, backgroundColor: '#063A29', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' },
  splashLogo: { width: 270, height: 92 },
  splashTagline: { marginTop: 2, color: '#E8ECE9', fontFamily: 'Inter_400Regular', fontSize: 16 },
  container: { flex: 1, backgroundColor: '#063A29', paddingHorizontal: 30, paddingTop: 58, paddingBottom: 32, overflow: 'hidden' },
  glowTop: { position: 'absolute', width: 360, height: 360, borderRadius: 180, backgroundColor: '#0C5038', top: -210, right: -100 },
  glowSide: { position: 'absolute', width: 180, height: 180, borderRadius: 90, backgroundColor: '#2A641B', opacity: 0.75, left: -105, bottom: 210 },
  brand: { alignItems: 'center', marginTop: 72 },
  logo: { width: 270, height: 92 },
  tagline: { marginTop: 0, color: '#E8ECE9', fontFamily: 'Inter_400Regular', fontSize: 16 },
  message: { marginTop: 112, paddingHorizontal: 8 },
  heading: { color: '#FFFFFF', fontFamily: 'Inter_800ExtraBold', fontSize: 38, lineHeight: 43, letterSpacing: -1.2 },
  accent: { color: '#D8FF3E', fontFamily: 'Inter_800ExtraBold', fontSize: 38, lineHeight: 43, letterSpacing: -1.2 },
  description: { color: '#DCE5E0', fontFamily: 'Inter_400Regular', fontSize: 16, lineHeight: 23, marginTop: 18, maxWidth: 310 },
  bottom: { marginTop: 'auto' },
  dots: { flexDirection: 'row', justifyContent: 'center', gap: 9, marginBottom: 20 },
  dot: { width: 9, height: 9, borderRadius: 5, backgroundColor: '#6C887C' },
  dotActive: { width: 24, backgroundColor: '#D8FF3E' },
  button: { height: 60, borderRadius: 30, backgroundColor: '#D8FF3E', flexDirection: 'row', alignItems: 'center', justifyContent: 'center', marginBottom: 18 },
  buttonText: { color: '#08140E', fontFamily: 'Inter_700Bold', fontSize: 17 },
  arrow: { color: '#08140E', fontFamily: 'Inter_700Bold', fontSize: 24, marginLeft: 12, marginTop: -2 },
  signIn: { color: '#DCE5E0', textAlign: 'center', fontFamily: 'Inter_400Regular', fontSize: 14 },
  signInAccent: { color: '#D8FF3E', fontFamily: 'Inter_600SemiBold' },
});