import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useAuth } from '../../context/AuthContext';

const BG = '#0D1017';
const CARD = '#171B24';
const CARD_2 = '#1D222C';
const TEXT = '#F4F6F8';
const MUTED = '#8D95A3';
const LIME = '#D8FF3E';

function getGreeting() {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

export default function HomeScreen() {
  const insets = useSafeAreaInsets();
  const { user } = useAuth();
  const firstName = user?.firstName?.trim() || 'there';
  const initial = firstName.charAt(0).toUpperCase();

  return (
    <View style={styles.screen}>
      <ScrollView
        style={styles.screen}
        contentContainerStyle={{ paddingTop: Math.max(insets.top + 8, 20), paddingBottom: 42 }}
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.container}>
          <View style={styles.header}>
            <View style={styles.brandBlock}>
              <Text style={styles.eyebrow}>OAKPAY</Text>
              <Text style={styles.greeting}>{getGreeting()}, {firstName}</Text>
            </View>
            <View style={styles.headerActions}>
              <Pressable style={styles.iconButton} accessibilityLabel="Notifications">
                <Ionicons name="notifications-outline" size={22} color={TEXT} />
              </Pressable>
              <Pressable style={styles.avatar} onPress={() => router.push('/(tabs)/profile')} accessibilityLabel="Profile">
                <Text style={styles.avatarText}>{initial}</Text>
              </Pressable>
            </View>
          </View>

          <View style={styles.balanceCard}>
            <View style={styles.balanceTop}>
              <View>
                <Text style={styles.balanceLabel}>TOTAL BALANCE</Text>
                <Text style={styles.balance}>0.00 <Text style={styles.currency}>USDT</Text></Text>
              </View>
              <Ionicons name="eye-outline" size={25} color={MUTED} />
            </View>
            <View style={styles.balanceBottom}>
              <Text style={styles.zwg}>≈ 0.00 ZWG</Text>
              <View style={styles.secure}>
                <Ionicons name="shield-checkmark-outline" size={15} color={LIME} />
                <Text style={styles.secureText}>Secure wallet</Text>
              </View>
            </View>
          </View>

          <View style={styles.actions}>
            <Pressable style={styles.primaryAction} onPress={() => router.push('/(tabs)/market')}>
              <View style={styles.actionCircle}><Ionicons name="arrow-down" size={20} color="#0D1017" /></View>
              <Text style={styles.primaryActionText}>Buy USDT</Text>
            </Pressable>
            <Pressable style={styles.secondaryAction} onPress={() => router.push('/(tabs)/market')}>
              <View style={styles.darkCircle}><Ionicons name="arrow-up" size={20} color={LIME} /></View>
              <Text style={styles.secondaryActionText}>Sell USDT</Text>
            </Pressable>
          </View>

          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>P2P marketplace</Text>
            <Pressable onPress={() => router.push('/(tabs)/market')}><Text style={styles.seeAll}>See all</Text></Pressable>
          </View>
          <Pressable style={styles.marketCard} onPress={() => router.push('/(tabs)/market')}>
            <View style={styles.marketIcon}><Ionicons name="people-outline" size={24} color={LIME} /></View>
            <View style={styles.marketCopy}>
              <Text style={styles.marketTitle}>Buy & sell with people</Text>
              <Text style={styles.marketText}>Find P2P offers using ZWG payment methods.</Text>
            </View>
            <Ionicons name="chevron-forward" size={22} color={MUTED} />
          </Pressable>

          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Wallet</Text>
            <Pressable onPress={() => router.push('/(tabs)/wallet')}><Text style={styles.seeAll}>Open</Text></Pressable>
          </View>
          <Pressable style={styles.walletRow} onPress={() => router.push('/(tabs)/wallet')}>
            <View style={styles.asset}><View style={styles.assetIcon}><Text style={styles.assetSymbol}>₮</Text></View><View><Text style={styles.assetName}>USDT</Text><Text style={styles.assetSub}>Tether</Text></View></View>
            <View style={styles.assetValue}><Text style={styles.amount}>0.00</Text><Text style={styles.assetSub}>USDT</Text></View>
          </Pressable>
          <Pressable style={styles.walletRow} onPress={() => router.push('/(tabs)/wallet')}>
            <View style={styles.asset}><View style={[styles.assetIcon, styles.zwgIcon]}><Text style={styles.assetSymbol}>Z</Text></View><View><Text style={styles.assetName}>ZWG</Text><Text style={styles.assetSub}>Zimbabwe Gold</Text></View></View>
            <View style={styles.assetValue}><Text style={styles.amount}>0.00</Text><Text style={styles.assetSub}>ZWG</Text></View>
          </Pressable>

          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Recent activity</Text>
            <Pressable onPress={() => router.push('/(tabs)/orders')}><Text style={styles.seeAll}>View</Text></Pressable>
          </View>
          <Pressable style={styles.empty} onPress={() => router.push('/(tabs)/orders')}>
            <View style={styles.emptyIcon}><Ionicons name="time-outline" size={21} color={LIME} /></View>
            <View style={styles.emptyCopy}><Text style={styles.emptyTitle}>No activity yet</Text><Text style={styles.emptyText}>Your P2P orders and wallet transactions will appear here.</Text></View>
            <Ionicons name="chevron-forward" size={20} color={MUTED} />
          </Pressable>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: BG },
  container: { paddingHorizontal: 20 },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 22 },
  brandBlock: { flex: 1, paddingRight: 10 },
  eyebrow: { color: LIME, fontFamily: 'Inter_800ExtraBold', fontSize: 11, letterSpacing: 2.2 },
  greeting: { color: TEXT, fontFamily: 'Inter_800ExtraBold', fontSize: 24, lineHeight: 31, marginTop: 4 },
  headerActions: { flexDirection: 'row', alignItems: 'center', gap: 9 },
  iconButton: { width: 45, height: 45, borderRadius: 15, backgroundColor: CARD, alignItems: 'center', justifyContent: 'center' },
  avatar: { width: 45, height: 45, borderRadius: 15, backgroundColor: LIME, alignItems: 'center', justifyContent: 'center' },
  avatarText: { color: '#0D1017', fontFamily: 'Inter_800ExtraBold', fontSize: 17 },
  balanceCard: { backgroundColor: CARD, borderRadius: 23, padding: 20, marginBottom: 14, borderWidth: 1, borderColor: '#242A34' },
  balanceTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  balanceLabel: { color: MUTED, fontFamily: 'Inter_700Bold', fontSize: 11, letterSpacing: 1.3 },
  balance: { color: TEXT, fontFamily: 'Inter_800ExtraBold', fontSize: 36, lineHeight: 43, marginTop: 7 },
  currency: { color: LIME, fontFamily: 'Inter_800ExtraBold', fontSize: 15 },
  balanceBottom: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: 18 },
  zwg: { color: MUTED, fontFamily: 'Inter_400Regular', fontSize: 14 },
  secure: { flexDirection: 'row', alignItems: 'center', gap: 5 },
  secureText: { color: LIME, fontFamily: 'Inter_700Bold', fontSize: 12 },
  actions: { flexDirection: 'row', gap: 10, marginBottom: 27 },
  primaryAction: { flex: 1, minHeight: 66, backgroundColor: LIME, borderRadius: 18, paddingHorizontal: 12, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 9 },
  secondaryAction: { flex: 1, minHeight: 66, backgroundColor: CARD_2, borderRadius: 18, paddingHorizontal: 12, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 9 },
  actionCircle: { width: 32, height: 32, borderRadius: 11, backgroundColor: '#C7EE35', alignItems: 'center', justifyContent: 'center' },
  darkCircle: { width: 32, height: 32, borderRadius: 11, backgroundColor: '#252B19', alignItems: 'center', justifyContent: 'center' },
  primaryActionText: { color: '#0D1017', fontFamily: 'Inter_800ExtraBold', fontSize: 14 },
  secondaryActionText: { color: TEXT, fontFamily: 'Inter_700Bold', fontSize: 14 },
  sectionHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 11, marginTop: 2 },
  sectionTitle: { color: TEXT, fontFamily: 'Inter_800ExtraBold', fontSize: 19 },
  seeAll: { color: LIME, fontFamily: 'Inter_700Bold', fontSize: 13 },
  marketCard: { backgroundColor: CARD, borderRadius: 19, padding: 16, flexDirection: 'row', alignItems: 'center', marginBottom: 25 },
  marketIcon: { width: 49, height: 49, borderRadius: 16, backgroundColor: '#242A17', alignItems: 'center', justifyContent: 'center' },
  marketCopy: { flex: 1, marginHorizontal: 12 },
  marketTitle: { color: TEXT, fontFamily: 'Inter_700Bold', fontSize: 14 },
  marketText: { color: MUTED, fontFamily: 'Inter_400Regular', marginTop: 4, fontSize: 12, lineHeight: 18 },
  walletRow: { backgroundColor: CARD, borderRadius: 17, padding: 15, marginBottom: 8, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  asset: { flexDirection: 'row', alignItems: 'center' },
  assetIcon: { width: 40, height: 40, borderRadius: 13, backgroundColor: '#263015', alignItems: 'center', justifyContent: 'center', marginRight: 10 },
  zwgIcon: { backgroundColor: '#25252A' },
  assetSymbol: { color: LIME, fontFamily: 'Inter_800ExtraBold', fontSize: 17 },
  assetName: { color: TEXT, fontFamily: 'Inter_700Bold', fontSize: 14 },
  assetSub: { color: MUTED, fontFamily: 'Inter_400Regular', fontSize: 11, marginTop: 2 },
  assetValue: { alignItems: 'flex-end' },
  amount: { color: TEXT, fontFamily: 'Inter_700Bold', fontSize: 14 },
  empty: { backgroundColor: CARD, borderRadius: 18, padding: 16, flexDirection: 'row', alignItems: 'center' },
  emptyIcon: { width: 42, height: 42, borderRadius: 13, backgroundColor: '#242A17', alignItems: 'center', justifyContent: 'center', marginRight: 12 },
  emptyCopy: { flex: 1 },
  emptyTitle: { color: TEXT, fontFamily: 'Inter_700Bold', fontSize: 13 },
  emptyText: { color: MUTED, fontFamily: 'Inter_400Regular', marginTop: 4, fontSize: 11, lineHeight: 17 },
});
