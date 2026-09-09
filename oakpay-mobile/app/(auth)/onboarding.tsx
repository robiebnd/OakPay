import { Ionicons } from '@expo/vector-icons';
import { Image, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';

const OAKPAY_LOGO = require('../../assets/Logo_OakPay.png');

export default function OnboardingScreen() {
  return (
    <View style={styles.screen}>
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <Pressable style={styles.back} onPress={() => router.replace('/(auth)/login')}>
          <Ionicons name="chevron-back" size={26} color="#111713" />
        </Pressable>

        <View style={styles.progress}>
          <View style={styles.progressActive} /><View style={styles.progressTrack} /><View style={styles.progressTrack} /><View style={styles.progressTrack} />
        </View>

        <View style={styles.brand}>
          <Image source={OAKPAY_LOGO} style={styles.logo} resizeMode="contain" />
        </View>

        <Text style={styles.title}>Welcome Onboard</Text>
        <Text style={styles.subtitle}>What you need to open a live OakPay account:</Text>

        <View style={styles.card}>
          <View style={styles.iconBox}><Ionicons name="card-outline" size={28} color="#123B2A" /></View>
          <View style={styles.cardCopy}>
            <Text style={styles.cardTitle}>Identity document</Text>
            <Text style={styles.cardText}>e.g. National ID, Passport, or Driver's License.</Text>
          </View>
        </View>

        <View style={styles.card}>
          <View style={styles.iconBox}><Ionicons name="home-outline" size={28} color="#123B2A" /></View>
          <View style={styles.cardCopy}>
            <Text style={styles.cardTitle}>Proof of address</Text>
            <Text style={styles.cardText}>Documents with your name and address within the last 6 months, e.g. bank statements or utility bills.</Text>
          </View>
        </View>

        <View style={styles.secureRow}>
          <View style={styles.secureIcon}><Ionicons name="shield-checkmark" size={20} color="#FFFFFF" /></View>
          <Text style={styles.secureText}>Your information is encrypted and kept secure.</Text>
        </View>

        <Pressable style={styles.continueButton} onPress={() => router.push('/(auth)/register')}>
          <Text style={styles.continueText}>Continue</Text>
          <Text style={styles.continueArrow}>→</Text>
        </Pressable>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: '#F7F8F6' },
  content: { flexGrow: 1, paddingHorizontal: 28, paddingTop: 54, paddingBottom: 28 },
  back: { width: 42, height: 42, alignItems: 'center', justifyContent: 'center', marginLeft: -8 },
  progress: { flexDirection: 'row', gap: 5, marginTop: 8, marginBottom: 42 },
  progressActive: { height: 5, flex: 1.2, borderRadius: 3, backgroundColor: '#123B2A' },
  progressTrack: { height: 5, flex: 1, borderRadius: 3, backgroundColor: '#DDE2DE' },
  brand: { alignItems: 'center', marginBottom: 24 },
  logo: { width: 190, height: 64 },
  title: { textAlign: 'center', color: '#111713', fontFamily: 'Inter_800ExtraBold', fontSize: 31, letterSpacing: -0.7 },
  subtitle: { textAlign: 'center', color: '#707A73', fontFamily: 'Inter_400Regular', fontSize: 16, lineHeight: 23, marginTop: 9, marginBottom: 25 },
  card: { flexDirection: 'row', backgroundColor: '#FFFFFF', borderRadius: 20, padding: 18, marginBottom: 14, borderWidth: 1, borderColor: '#E8EBE8', alignItems: 'center' },
  iconBox: { width: 62, height: 62, borderRadius: 18, backgroundColor: '#EAF2ED', alignItems: 'center', justifyContent: 'center', marginRight: 16 },
  cardCopy: { flex: 1 },
  cardTitle: { color: '#111713', fontFamily: 'Inter_700Bold', fontSize: 16, marginBottom: 5 },
  cardText: { color: '#707A73', fontFamily: 'Inter_400Regular', fontSize: 14, lineHeight: 20 },
  secureRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', paddingHorizontal: 18, marginTop: 16, marginBottom: 26 },
  secureIcon: { width: 34, height: 34, borderRadius: 17, backgroundColor: '#123B2A', alignItems: 'center', justifyContent: 'center', marginRight: 10 },
  secureText: { flex: 1, color: '#657069', fontFamily: 'Inter_400Regular', fontSize: 14, lineHeight: 20 },
  continueButton: { height: 58, borderRadius: 29, backgroundColor: '#123B2A', flexDirection: 'row', alignItems: 'center', justifyContent: 'center' },
  continueText: { color: '#FFFFFF', fontFamily: 'Inter_700Bold', fontSize: 17 },
  continueArrow: { color: '#FFFFFF', fontFamily: 'Inter_700Bold', fontSize: 23, marginLeft: 12, marginTop: -2 },
});