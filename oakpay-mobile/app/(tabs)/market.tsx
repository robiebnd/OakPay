import { Ionicons } from '@expo/vector-icons';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';

const BG = '#0D1017';
const CARD = '#171B24';
const TEXT = '#F4F6F8';
const MUTED = '#8D95A3';
const LIME = '#D8FF3E';

export default function MarketScreen() {
  return (
    <ScrollView style={styles.screen} contentContainerStyle={styles.container} showsVerticalScrollIndicator={false}>
      <View style={styles.header}>
        <View><Text style={styles.eyebrow}>P2P MARKET</Text><Text style={styles.title}>Buy & sell USDT</Text></View>
        <View style={styles.iconButton}><Ionicons name="options-outline" size={20} color={TEXT} /></View>
      </View>
      <Text style={styles.subtitle}>Trade directly with other OakPay users using ZWG.</Text>

      <View style={styles.switcher}>
        <Pressable style={styles.active}><Text style={styles.activeText}>Buy USDT</Text></Pressable>
        <Pressable style={styles.inactive}><Text style={styles.inactiveText}>Sell USDT</Text></Pressable>
      </View>

      <View style={styles.filters}>
        <View style={styles.filter}><Text style={styles.filterText}>USDT</Text><Ionicons name="chevron-down" size={14} color={MUTED} /></View>
        <View style={styles.filter}><Text style={styles.filterText}>ZWG</Text><Ionicons name="chevron-down" size={14} color={MUTED} /></View>
        <View style={styles.filter}><Text style={styles.filterText}>Payment</Text><Ionicons name="chevron-down" size={14} color={MUTED} /></View>
      </View>

      <View style={styles.rateCard}><View><Text style={styles.rateLabel}>REFERENCE RATE</Text><Text style={styles.rate}>1 USDT ≈ 26.56 ZWG</Text></View><View style={styles.liveDot} /></View>

      <View style={styles.sectionHeader}><Text style={styles.sectionTitle}>Available offers</Text><Text style={styles.count}>ACTIVE</Text></View>
      <View style={styles.empty}>
        <View style={styles.emptyIcon}><Ionicons name="people-outline" size={25} color={LIME} /></View>
        <Text style={styles.emptyTitle}>No offers available</Text>
        <Text style={styles.emptyText}>Active P2P advertisements will appear here when sellers publish offers.</Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: BG },
  container: { padding: 20, paddingTop: 16, paddingBottom: 35 },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: 8 },
  eyebrow: { color: LIME, fontSize: 10, fontWeight: '900', letterSpacing: 1.7 },
  title: { color: TEXT, fontSize: 27, fontWeight: '900', marginTop: 5 },
  subtitle: { color: MUTED, fontSize: 13, lineHeight: 19, marginTop: 7, marginBottom: 20 },
  iconButton: { width: 43, height: 43, borderRadius: 14, backgroundColor: CARD, alignItems: 'center', justifyContent: 'center' },
  switcher: { flexDirection: 'row', backgroundColor: CARD, borderRadius: 15, padding: 4, marginBottom: 13 },
  active: { flex: 1, backgroundColor: LIME, borderRadius: 11, paddingVertical: 12, alignItems: 'center' },
  activeText: { color: BG, fontWeight: '900', fontSize: 13 },
  inactive: { flex: 1, paddingVertical: 12, alignItems: 'center' },
  inactiveText: { color: MUTED, fontWeight: '800', fontSize: 13 },
  filters: { flexDirection: 'row', gap: 8, marginBottom: 13 },
  filter: { flexDirection: 'row', alignItems: 'center', gap: 5, backgroundColor: CARD, borderRadius: 12, paddingHorizontal: 11, paddingVertical: 10 },
  filterText: { color: TEXT, fontSize: 11, fontWeight: '700' },
  rateCard: { backgroundColor: '#202617', borderRadius: 16, padding: 15, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 },
  rateLabel: { color: MUTED, fontSize: 9, fontWeight: '900', letterSpacing: 1.2 },
  rate: { color: TEXT, fontSize: 14, fontWeight: '800', marginTop: 5 },
  liveDot: { width: 9, height: 9, borderRadius: 5, backgroundColor: LIME },
  sectionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 11 },
  sectionTitle: { color: TEXT, fontSize: 17, fontWeight: '800' },
  count: { color: MUTED, fontSize: 9, fontWeight: '900', letterSpacing: 1 },
  empty: { backgroundColor: CARD, borderRadius: 21, padding: 27, alignItems: 'center' },
  emptyIcon: { width: 58, height: 58, borderRadius: 18, backgroundColor: '#242A17', alignItems: 'center', justifyContent: 'center', marginBottom: 17 },
  emptyTitle: { color: TEXT, fontSize: 17, fontWeight: '800' },
  emptyText: { color: MUTED, textAlign: 'center', lineHeight: 20, fontSize: 12, marginTop: 8 },
});
