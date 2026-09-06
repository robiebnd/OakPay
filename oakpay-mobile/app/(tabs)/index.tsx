import { Ionicons } from '@expo/vector-icons';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';

const BG = '#0D1017';
const CARD = '#171B24';
const CARD_2 = '#1D222C';
const TEXT = '#F4F6F8';
const MUTED = '#8D95A3';
const LIME = '#D8FF3E';

export default function HomeScreen() {
  return (
    <ScrollView style={styles.screen} contentContainerStyle={styles.container} showsVerticalScrollIndicator={false}>
      <View style={styles.header}>
        <View>
          <Text style={styles.eyebrow}>OAKPAY</Text>
          <Text style={styles.greeting}>Good evening</Text>
        </View>
        <View style={styles.headerActions}>
          <Pressable style={styles.iconButton}><Ionicons name="notifications-outline" size={21} color={TEXT} /></Pressable>
          <Pressable style={styles.avatar}><Text style={styles.avatarText}>O</Text></Pressable>
        </View>
      </View>

      <View style={styles.balanceCard}>
        <View style={styles.balanceTop}>
          <View><Text style={styles.balanceLabel}>TOTAL BALANCE</Text><Text style={styles.balance}>0.00 <Text style={styles.currency}>USDT</Text></Text></View>
          <Ionicons name="eye-outline" size={21} color={MUTED} />
        </View>
        <View style={styles.balanceBottom}><Text style={styles.zwg}>≈ 0.00 ZWG</Text><View style={styles.secure}><Ionicons name="shield-checkmark-outline" size={13} color={LIME} /><Text style={styles.secureText}> Secure wallet</Text></View></View>
      </View>

      <View style={styles.actions}>
        <Pressable style={styles.primaryAction} onPress={() => router.push('/(tabs)/market')}>
          <View style={styles.actionCircle}><Ionicons name="arrow-down" size={18} color="#0D1017" /></View><Text style={styles.primaryActionText}>Buy USDT</Text>
        </Pressable>
        <Pressable style={styles.secondaryAction} onPress={() => router.push('/(tabs)/market')}>
          <View style={styles.darkCircle}><Ionicons name="arrow-up" size={18} color={LIME} /></View><Text style={styles.secondaryActionText}>Sell USDT</Text>
        </Pressable>
      </View>

      <View style={styles.sectionHeader}><Text style={styles.sectionTitle}>P2P marketplace</Text><Pressable onPress={() => router.push('/(tabs)/market')}><Text style={styles.seeAll}>See all</Text></Pressable></View>
      <Pressable style={styles.marketCard} onPress={() => router.push('/(tabs)/market')}>
        <View style={styles.marketIcon}><Ionicons name="people-outline" size={22} color={LIME} /></View>
        <View style={styles.marketCopy}><Text style={styles.marketTitle}>Buy & sell with people</Text><Text style={styles.marketText}>Find P2P offers using ZWG payment methods.</Text></View>
        <Ionicons name="chevron-forward" size={20} color={MUTED} />
      </Pressable>

      <View style={styles.sectionHeader}><Text style={styles.sectionTitle}>Wallet</Text><Pressable onPress={() => router.push('/(tabs)/wallet')}><Text style={styles.seeAll}>Open</Text></Pressable></View>
      <View style={styles.walletRow}>
        <View style={styles.asset}><View style={styles.assetIcon}><Text style={styles.assetSymbol}>₮</Text></View><View><Text style={styles.assetName}>USDT</Text><Text style={styles.assetSub}>Tether</Text></View></View>
        <View style={styles.assetValue}><Text style={styles.amount}>0.00</Text><Text style={styles.assetSub}>USDT</Text></View>
      </View>
      <View style={styles.walletRow}>
        <View style={styles.asset}><View style={[styles.assetIcon, styles.zwgIcon]}><Text style={styles.assetSymbol}>Z</Text></View><View><Text style={styles.assetName}>ZWG</Text><Text style={styles.assetSub}>Zimbabwe Gold</Text></View></View>
        <View style={styles.assetValue}><Text style={styles.amount}>0.00</Text><Text style={styles.assetSub}>ZWG</Text></View>
      </View>

      <View style={styles.sectionHeader}><Text style={styles.sectionTitle}>Recent activity</Text><Pressable onPress={() => router.push('/(tabs)/orders')}><Text style={styles.seeAll}>View</Text></Pressable></View>
      <View style={styles.empty}><View style={styles.emptyIcon}><Ionicons name="time-outline" size={20} color={LIME} /></View><View style={{ flex: 1 }}><Text style={styles.emptyTitle}>No activity yet</Text><Text style={styles.emptyText}>Your P2P orders and wallet transactions will appear here.</Text></View></View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: BG },
  container: { padding: 20, paddingTop: 16, paddingBottom: 34 },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 },
  eyebrow: { color: LIME, fontSize: 11, fontWeight: '900', letterSpacing: 2 },
  greeting: { color: TEXT, fontSize: 25, fontWeight: '800', marginTop: 4 },
  headerActions: { flexDirection: 'row', alignItems: 'center', gap: 9 },
  iconButton: { width: 43, height: 43, borderRadius: 14, backgroundColor: CARD, alignItems: 'center', justifyContent: 'center' },
  avatar: { width: 43, height: 43, borderRadius: 14, backgroundColor: LIME, alignItems: 'center', justifyContent: 'center' },
  avatarText: { color: '#0D1017', fontSize: 17, fontWeight: '900' },
  balanceCard: { backgroundColor: CARD, borderRadius: 23, padding: 20, marginBottom: 14, borderWidth: 1, borderColor: '#242A34' },
  balanceTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  balanceLabel: { color: MUTED, fontSize: 10, fontWeight: '800', letterSpacing: 1.2 },
  balance: { color: TEXT, fontSize: 34, fontWeight: '900', marginTop: 7 },
  currency: { color: LIME, fontSize: 14, fontWeight: '800' },
  balanceBottom: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: 17 },
  zwg: { color: MUTED, fontSize: 13 },
  secure: { flexDirection: 'row', alignItems: 'center' },
  secureText: { color: LIME, fontSize: 11, fontWeight: '700' },
  actions: { flexDirection: 'row', gap: 10, marginBottom: 27 },
  primaryAction: { flex: 1, backgroundColor: LIME, borderRadius: 17, padding: 14, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 9 },
  secondaryAction: { flex: 1, backgroundColor: CARD_2, borderRadius: 17, padding: 14, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 9 },
  actionCircle: { width: 29, height: 29, borderRadius: 10, backgroundColor: '#C7EE35', alignItems: 'center', justifyContent: 'center' },
  darkCircle: { width: 29, height: 29, borderRadius: 10, backgroundColor: '#252B19', alignItems: 'center', justifyContent: 'center' },
  primaryActionText: { color: '#0D1017', fontWeight: '900', fontSize: 13 },
  secondaryActionText: { color: TEXT, fontWeight: '800', fontSize: 13 },
  sectionHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 11, marginTop: 2 },
  sectionTitle: { color: TEXT, fontSize: 18, fontWeight: '800' },
  seeAll: { color: LIME, fontSize: 12, fontWeight: '800' },
  marketCard: { backgroundColor: CARD, borderRadius: 19, padding: 16, flexDirection: 'row', alignItems: 'center', marginBottom: 25 },
  marketIcon: { width: 47, height: 47, borderRadius: 15, backgroundColor: '#242A17', alignItems: 'center', justifyContent: 'center' },
  marketCopy: { flex: 1, marginHorizontal: 12 },
  marketTitle: { color: TEXT, fontWeight: '800', fontSize: 14 },
  marketText: { color: MUTED, marginTop: 4, fontSize: 12, lineHeight: 18 },
  walletRow: { backgroundColor: CARD, borderRadius: 17, padding: 15, marginBottom: 8, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  asset: { flexDirection: 'row', alignItems: 'center' },
  assetIcon: { width: 39, height: 39, borderRadius: 13, backgroundColor: '#263015', alignItems: 'center', justifyContent: 'center', marginRight: 10 },
  zwgIcon: { backgroundColor: '#25252A' },
  assetSymbol: { color: LIME, fontWeight: '900', fontSize: 17 },
  assetName: { color: TEXT, fontWeight: '800', fontSize: 14 },
  assetSub: { color: MUTED, fontSize: 11, marginTop: 2 },
  assetValue: { alignItems: 'flex-end' },
  amount: { color: TEXT, fontWeight: '800', fontSize: 14 },
  empty: { backgroundColor: CARD, borderRadius: 18, padding: 16, flexDirection: 'row', alignItems: 'center' },
  emptyIcon: { width: 42, height: 42, borderRadius: 13, backgroundColor: '#242A17', alignItems: 'center', justifyContent: 'center', marginRight: 12 },
  emptyTitle: { color: TEXT, fontWeight: '800', fontSize: 13 },
  emptyText: { color: MUTED, marginTop: 4, fontSize: 11, lineHeight: 17 },
});
